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
package org.gagravarr.opus;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggStreamAudioData;

/**
 * Raw, compressed audio data
 */
public class OpusAudioData extends OggStreamAudioData implements OpusPacket {
    public OpusAudioData(OggPacket pkt) {
        super(pkt);
    }
    public OpusAudioData(byte[] data) {
        super(data);
    }

    protected boolean isEndOfStream() {
        return getOggPacket().isEndOfStream();
    }
}
