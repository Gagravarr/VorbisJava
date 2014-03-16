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
package org.gagravarr.skeleton;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * The Fishead (note - one h) provides some basic information
 *  on the Skeleton / Annodex stream
 */
public class SkeletonFishead extends HighLevelOggStreamPacket implements SkeletonPacket {
    private int versionMajor;
    private int versionMinor;
    // TODO
    
    public SkeletonFishead() {
        super();
        versionMajor = 4;
        versionMinor = 0;
    }

    public SkeletonFishead(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_FISHEAD_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Skeleton Fishead Header");
        }

        // Parse
        versionMajor = IOUtils.getInt2(data, 8);
        versionMinor = IOUtils.getInt2(data, 10);
        if (versionMajor < 3 || versionMajor > 4) {
            throw new IllegalArgumentException("Unsupported Skeleton version " + versionMajor + " detected");
        }

        // TODO
    }

    @Override
    public OggPacket write() {
        // TODO
        return null;
    }
}
