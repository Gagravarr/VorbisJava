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

/**
 * Per-channel, compressed audio
 */
public abstract class FlacAudioSubFrame {
    public abstract boolean matchesType(final int type);
    
    public static class SubFrameConstant extends FlacAudioSubFrame {
        public boolean matchesType(final int type) {
            if (type == 0) return true;
            return false;
        }
        
    }
    public static class SubFrameVerbatim extends FlacAudioSubFrame {
        public boolean matchesType(final int type) {
            if (type == 1) return true;
            return false;
        }
        
    }
    public static class SubFrameFixed extends FlacAudioSubFrame {
        public boolean matchesType(final int type) {
            if (type >= 8  && type <= 15) return true;
            return false;
        }
        
    }
    public static class SubFrameLPC extends FlacAudioSubFrame {
        public boolean matchesType(final int type) {
            if (type >= 32) return true;
            return false;
        }
        
    }
    public static class SubFrameReserved extends FlacAudioSubFrame {
        public boolean matchesType(final int type) {
            if (type >= 2  && type <= 7) return true;
            if (type >= 16 && type <= 31) return true;
            return false;
        }
        
    }
}
