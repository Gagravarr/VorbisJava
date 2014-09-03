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

import org.gagravarr.ogg.audio.OggAudioInfoHeader;

/**
 * The {@link FlacInfo} plus the version data from
 *  {@link FlacFirstOggPacket}
 */
public class FlacOggInfo extends FlacInfo implements OggAudioInfoHeader {
    private String version;

    /**
     * Creates a new, empty info
     */
    public FlacOggInfo() {
        super();
        version = "1.0";
    }

    /**
     * Reads the Info from the specified data
     */
    public FlacOggInfo(byte[] data, int offset, String version) {
        super(data, offset);
        this.version = version;
    }

    /**
     * The version comes from the parent packet
     */
    public String getVersionString() {
        return version;
    }
}
