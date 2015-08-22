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
    public static FlacAudioSubFrame create(int type, int sampleSizeBits, int blockSize, 
                                           BitsReader data) throws IOException {
        if (type < 0 || type >= 64) {
            throw new IllegalArgumentException("Type must be a un-signed 6 bit number");
        }

        if (SubFrameConstant.matchesType(type))
            return new SubFrameConstant(sampleSizeBits, blockSize, data);
        if (SubFrameVerbatim.matchesType(type))
            return new SubFrameVerbatim(sampleSizeBits, blockSize, data);
        if (SubFrameFixed.matchesType(type))
            return new SubFrameFixed(type, sampleSizeBits, blockSize, data);
        if (SubFrameLPC.matchesType(type))
            return new SubFrameLPC(type, sampleSizeBits, blockSize, data);
        return new SubFrameReserved();
    }

    protected final int sampleSizeBits;
    protected final int blockSize;
    protected final int order;
    protected FlacAudioSubFrame(int sampleSizeBits, int blockSize, int order) {
        this.sampleSizeBits = sampleSizeBits;
        this.blockSize = blockSize;
        this.order = order;
    }

    public static class SubFrameConstant extends FlacAudioSubFrame {
        protected SubFrameConstant(int sampleSizeBits, int blockSize, BitsReader data) throws IOException {
            super(sampleSizeBits, blockSize, -1);
            data.read(sampleSizeBits);
        }
        public static boolean matchesType(final int type) {
            if (type == 0) return true;
            return false;
        }
    }
    public static class SubFrameVerbatim extends FlacAudioSubFrame {
        protected SubFrameVerbatim(int sampleSizeBits, int blockSize, BitsReader data) throws IOException {
            super(sampleSizeBits, blockSize, -1);
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
        protected SubFrameFixed(int type, int sampleSizeBits, int blockSize, BitsReader data) throws IOException {
            super(sampleSizeBits, blockSize, (type & 8));

            int[] warmUpSamples = new int[order];
            for (int i=0; i<order; i++) {
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
        protected SubFrameLPC(int type, int sampleSizeBits, int blockSize, BitsReader data) throws IOException {
            super(sampleSizeBits, blockSize, (type & 32) + 1);

            int[] warmUpSamples = new int[order];
            for (int i=0; i<order; i++) {
                warmUpSamples[i] = data.read(sampleSizeBits);
            }

            this.linearPredictorCoefficientPrecision = data.read(4)+1;
            this.linearPredictorCoefficientShift = data.read(5);

            int[] coefficients = new int[order];
            for (int i=0; i<order; i++) {
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
        private SubFrameReserved() {
            super(-1,-1,-1);
        }
    }

    protected SubFrameResidual createResidual(BitsReader data) throws IOException {
        int type = data.read(2);
        if (type > 1) {
            // Un-supported / reserved type
            return null;
        }
        
        int order = data.read(4);
        if (type == 0) {
            return new SubFrameResidualRice(order, data);
        } else {
            return new SubFrameResidualRice2(order, data);
        }
    }

    public class SubFrameResidual {
        private SubFrameResidual(int order, int bits, int escapeCode, BitsReader data) throws IOException {
            int riceParam = data.read(bits);
            if (riceParam == escapeCode) {
                int numUnencoded = data.read(5);
                for (int i=0; i<numUnencoded; i++) {
                    data.read(sampleSizeBits);
                }
            } else {
                int numSamples = 0;
                if (order == 0) {
                    numSamples = blockSize - order; 
                }
            }
        }
    }
    public class SubFrameResidualRice extends SubFrameResidual {
        private static final int PARAM_BITS = 4;
        private static final int ESCAPE_CODE = 15;
        public SubFrameResidualRice(int order, BitsReader data) throws IOException {
            super(order, PARAM_BITS, ESCAPE_CODE, data);
        }
    }
    public class SubFrameResidualRice2 extends SubFrameResidual {
        private static final int PARAM_BITS = 5;
        private static final int ESCAPE_CODE = 31;
        public SubFrameResidualRice2(int order, BitsReader data) throws IOException {
            super(order, PARAM_BITS, ESCAPE_CODE, data);
        }
    }
}
