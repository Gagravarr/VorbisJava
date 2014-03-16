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
 * The Fisbone (note - no h) provides details about
 *  what the other streams in the file are
 */
public class SkeletonFisbone extends HighLevelOggStreamPacket implements SkeletonPacket {
    // TODO
    
    public SkeletonFisbone() {
        super();
    }

    public SkeletonFisbone(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_FISBONE_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Skeleton Fisbone Header");
        }

        // Parse
        // TODO
    }

    @Override
    public OggPacket write() {
        // TODO
        return null;
    }
}
