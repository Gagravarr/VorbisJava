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
package org.gagravarr.theora;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * The identification header identifies the bitstream as Theora, 
 *  and includes the Theora version, the ?????
 */
public class TheoraInfo extends HighLevelOggStreamPacket implements TheoraPacket {
    private int majorVersion;
    private int minorVersion;
    private int revisionVersion;
    private int frameWidthMB;
    private int frameHeightMB;
    // TODO Do the rest

    public TheoraInfo() {
        super();
        majorVersion = 3;
        minorVersion = 2;
        revisionVersion = 1;
    }

    public TheoraInfo(OggPacket pkt) {
        super(pkt);

        // Parse
        byte[] data = getData();

        majorVersion = (int)data[8];
        minorVersion = (int)data[9];
        revisionVersion = (int)data[10];
        if (majorVersion != 3) {
            throw new IllegalArgumentException("Unsupported Theora version " + getVersion() + " detected");
        }

        frameWidthMB  = IOUtils.getInt2(data, 11);
        frameHeightMB = IOUtils.getInt2(data, 13);

        // TODO The rest
    }

    @Override
    public OggPacket write() {
        // TODO Implement
        return null;
    }

    public String getVersion() {
        return majorVersion + "." + minorVersion + "." + revisionVersion;
    }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }
    public int getRevisionVersion() {
        return revisionVersion;
    }

    // TODO The rest
}
