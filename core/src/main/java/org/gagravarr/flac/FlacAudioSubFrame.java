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
    public static FlacAudioSubFrame create(int type, int sampleSizeBits, BitsReader data) throws IOException {
        if (type < 0 || type >= 64) {
            throw new IllegalArgumentException("Type must be a un-signed 6 bit number");
        }

        if (SubFrameConstant.matchesType(type))
            return new SubFrameConstant(data);
        if (SubFrameVerbatim.matchesType(type))
            return new SubFrameVerbatim(data);
        if (SubFrameFixed.matchesType(type))
            return new SubFrameFixed(type, sampleSizeBits, data);
        if (SubFrameLPC.matchesType(type))
            return new SubFrameLPC(type, sampleSizeBits, data);
        return new SubFrameReserved();
    }

    public static class SubFrameConstant extends FlacAudioSubFrame {
        protected SubFrameConstant(BitsReader data) {
        }
        public static boolean matchesType(final int type) {
            if (type == 0) return true;
            return false;
        }
    }
    public static class SubFrameVerbatim extends FlacAudioSubFrame {
        protected SubFrameVerbatim(BitsReader data) {
        }
        public static boolean matchesType(final int type) {
            if (type == 1) return true;
            return false;
        }
    }
    public static class SubFrameFixed extends FlacAudioSubFrame {
        protected final int order;
        protected SubFrameFixed(int type, int sampleSizeBits, BitsReader data) throws IOException {
            this.order = type & 8;

            int[] warmUpSamples = new int[order];
            for (int i=0; i<order; i++) {
                warmUpSamples[i] = data.read(sampleSizeBits);
            }

            SubFrameResidual.create(order, data);
        }
        public static boolean matchesType(final int type) {
            if (type >= 8  && type <= 15) return true;
            return false;
        }
    }
    public static class SubFrameLPC extends FlacAudioSubFrame {
        protected final int order;
        protected final int linearPredictorCoefficientPrecision;
        protected final int linearPredictorCoefficientShift;
        protected SubFrameLPC(int type, int sampleSizeBits, BitsReader data) throws IOException {
            this.order = (type & 32) + 1;

            int[] warmUpSamples = new int[order];
            for (int i=0; i<order; i++) {
                warmUpSamples[i] = data.read(sampleSizeBits);
            }

            this.linearPredictorCoefficientPrecision = data.read(4)+1;
            this.linearPredictorCoefficientShift = data.read(5);

            SubFrameResidual.create(order, data);
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
    }

    public static class SubFrameResidual {
        protected static SubFrameResidual create(int order, BitsReader data) throws IOException {
            int type = data.read(2);
            if (type == 0) {
                return new SubFrameResidualRice(order, data);
            } else if (type == 1) {
                return new SubFrameResidualRice2(order, data);
            } else {
                // Un-supported / reserved type
                return null;
            }
        }
    }
    public static class SubFrameResidualRice extends SubFrameResidual {
        public SubFrameResidualRice(int order, BitsReader data) throws IOException {
            // TODO
        }
    }
    public static class SubFrameResidualRice2 extends SubFrameResidual {
        public SubFrameResidualRice2(int order, BitsReader data) throws IOException {
            // TODO
        }
    }
}
