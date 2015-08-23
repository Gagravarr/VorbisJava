/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gagravarr.flac;

import java.io.IOException;

import org.gagravarr.ogg.BitsReader;

/**
 * Per-channel, compressed audio
 */
public abstract class FlacAudioSubFrame {
    public static FlacAudioSubFrame create(int type, int wastedBits, FlacAudioFrame audioFrame,
                                           BitsReader data) throws IOException {
        // Sanity check
        if (type < 0 || type >= 64) {
            throw new IllegalArgumentException("Type must be a un-signed 6 bit number, found " + type);
        }

        // Create the right type
        FlacAudioSubFrame subFrame;
        if (SubFrameConstant.matchesType(type))
            subFrame = new SubFrameConstant(audioFrame, data);
        else if (SubFrameVerbatim.matchesType(type))
            subFrame =  new SubFrameVerbatim(audioFrame, data);
        else if (SubFrameFixed.matchesType(type))
            subFrame =  new SubFrameFixed(type, audioFrame, data);
        else if (SubFrameLPC.matchesType(type))
            subFrame =  new SubFrameLPC(type, audioFrame, data);
        else subFrame =  new SubFrameReserved(audioFrame);

        // Record details, and return
        subFrame.wastedBits = wastedBits;
        return subFrame;
    }

    protected final FlacAudioFrame audioFrame;
    protected final int predictorOrder;
    protected final int sampleSizeBits;
    protected final int blockSize;
    private int wastedBits;

    protected FlacAudioSubFrame(int predictorOrder, FlacAudioFrame audioFrame) {
        this.predictorOrder = predictorOrder;
        this.audioFrame = audioFrame;
        this.sampleSizeBits = audioFrame.getBitsPerSample();
        this.blockSize = audioFrame.getBlockSize();
    }
    /**
     * The number of wasted bits per sample
     */
    public int getWastedBits() {
        return wastedBits;
    }

    public static class SubFrameConstant extends FlacAudioSubFrame {
        protected SubFrameConstant(FlacAudioFrame audioFrame, BitsReader data) throws IOException {
            super(-1, audioFrame);
            data.read(sampleSizeBits);
        }
        public static boolean matchesType(final int type) {
            if (type == 0) return true;
            return false;
        }
    }
    public static class SubFrameVerbatim extends FlacAudioSubFrame {
        protected SubFrameVerbatim(FlacAudioFrame audioFrame, BitsReader data) throws IOException {
            super(-1, audioFrame);
            for (int i=0; i<blockSize; i++) {
                data.read(sampleSizeBits);
            }
        }
        public static boolean matchesType(final int type) {
            if (type == 1) return true;
            return false;
        }
    }
    public static class SubFrameFixed extends FlacAudioSubFrame {
        protected SubFrameFixed(int type, FlacAudioFrame audioFrame, BitsReader data) throws IOException {
            super((type & 8), audioFrame);

            int[] warmUpSamples = new int[predictorOrder];
            for (int i=0; i<predictorOrder; i++) {
                warmUpSamples[i] = data.read(sampleSizeBits);
            }

            createResidual(data);
        }
        public static boolean matchesType(final int type) {
            if (type >= 8  && type <= 15) return true;
            return false;
        }
    }
    public static class SubFrameLPC extends FlacAudioSubFrame {
        protected final int linearPredictorCoefficientPrecision;
        protected final int linearPredictorCoefficientShift;
        protected SubFrameLPC(int type, FlacAudioFrame audioFrame, BitsReader data) throws IOException {
            super((type & 32) + 1, audioFrame);

            int[] warmUpSamples = new int[predictorOrder];
            for (int i=0; i<predictorOrder; i++) {
                warmUpSamples[i] = data.read(sampleSizeBits);
            }

            this.linearPredictorCoefficientPrecision = data.read(4)+1;
            this.linearPredictorCoefficientShift = data.read(5);

            int[] coefficients = new int[predictorOrder];
            for (int i=0; i<predictorOrder; i++) {
                coefficients[i] = data.read(linearPredictorCoefficientPrecision);
            }

            createResidual(data);
        }
        public static boolean matchesType(final int type) {
            if (type >= 32) return true;
            return false;
        }
    }
    public static class SubFrameReserved extends FlacAudioSubFrame {
        public static boolean matchesType(final int type) {
            if (type >= 2  && type <= 7) return true;
            if (type >= 16 && type <= 31) return true;
            return false;
        }
        private SubFrameReserved(FlacAudioFrame audioFrame) {
            super(-1, audioFrame);
        }
    }

    protected SubFrameResidual createResidual(BitsReader data) throws IOException {
        int type = data.read(2);
        if (type > 1) {
            // Un-supported / reserved type
            return null;
        }
        
        int partitionOrder = data.read(4);
        if (type == 0) {
            return new SubFrameResidualRice(partitionOrder, data);
        } else {
            return new SubFrameResidualRice2(partitionOrder, data);
        }
    }

    public class SubFrameResidual {
        private SubFrameResidual(int partitionOrder, int bits, int escapeCode, BitsReader data) throws IOException {
            int numPartitions = 1<<partitionOrder;
            for (int pn=0; pn<numPartitions; pn++) {
                int riceParam = data.read(bits);
                if (riceParam == escapeCode) {
                    int numUnencoded = data.read(5);
                    for (int i=0; i<numUnencoded; i++) {
                        data.read(sampleSizeBits);
                    }
                } else {
                    int numSamples = 0;
                    if (partitionOrder == 0) {
                        numSamples = blockSize - predictorOrder;
                    } else if (pn > 0) {
                        numSamples = blockSize / numPartitions;
                    } else {
                        numSamples = (blockSize / numPartitions) - predictorOrder;
                    }

                    for (int sn=0; sn<numSamples; sn++) {
                        data.read(riceParam);
                    }
                }
            }
        }
    }
    public class SubFrameResidualRice extends SubFrameResidual {
        private static final int PARAM_BITS = 4;
        private static final int ESCAPE_CODE = 15;
        public SubFrameResidualRice(int partitionOrder, BitsReader data) throws IOException {
            super(partitionOrder, PARAM_BITS, ESCAPE_CODE, data);
        }
    }
    public class SubFrameResidualRice2 extends SubFrameResidual {
        private static final int PARAM_BITS = 5;
        private static final int ESCAPE_CODE = 31;
        public SubFrameResidualRice2(int partitionOrder, BitsReader data) throws IOException {
            super(partitionOrder, PARAM_BITS, ESCAPE_CODE, data);
        }
    }
}
