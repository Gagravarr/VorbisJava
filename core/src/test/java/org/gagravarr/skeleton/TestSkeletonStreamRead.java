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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;

/**
 * Tests for reading a Skeleton Stream
 */
public class TestSkeletonStreamRead extends TestCase {
    private InputStream getTestFileV3() throws IOException {
        return this.getClass().getResourceAsStream("/testTheoraSkeletonCMML.ogg");
    }
    // TODO Test files with other versions

    public void testReadV3() throws IOException {
        OggFile ogg = new OggFile(getTestFileV3());
        OggPacketReader r = ogg.getPacketReader();
        OggPacket p;
        
        int expectSid = 0x794f5545;
        
        // Should be the first stream
        p = r.getNextPacket();
        assertTrue(SkeletonPacketFactory.isSkeletonSpecial(p));
        assertEquals(expectSid, p.getSid());
        
        // Check the Fishead
        SkeletonFishead head = (SkeletonFishead)SkeletonPacketFactory.create(p);
        assertEquals(3, head.getVersionMajor());
        assertEquals(0, head.getVersionMinor());
        
        assertEquals(1000, head.getPresentationTimeDenominator());
        assertEquals(0, head.getPresentationTimeNumerator());
        assertEquals(1000, head.getBaseTimeDenominator());
        assertEquals(0, head.getBaseTimeNumerator());
        
        assertEquals(0, head.getUtc1());
        assertEquals(0, head.getUtc2());
        assertEquals(0, head.getUtc3());
        
        // These two are zero as they're v4 only
        assertEquals(0, head.getContentOffset());
        assertEquals(0, head.getSegmentLength());
        
        
        // Check the Fisbone
        p = r.getNextPacketWithSid(expectSid);
        SkeletonFisbone bone1 = (SkeletonFisbone)SkeletonPacketFactory.create(p);
        // TODO Check the bone

        
        // Check the second bone
        p = r.getNextPacketWithSid(expectSid);
        SkeletonFisbone bone2 = (SkeletonFisbone)SkeletonPacketFactory.create(p);
        // TODO Check the bone
        
        
        // We have a single key frame 
        p = r.getNextPacketWithSid(expectSid);
        SkeletonKeyFramePacket kf = (SkeletonKeyFramePacket)SkeletonPacketFactory.create(p);
        // TODO Check the frame
        
        
        // And that's it
        p = r.getNextPacketWithSid(expectSid);
        assertNull(p);
    }
}
