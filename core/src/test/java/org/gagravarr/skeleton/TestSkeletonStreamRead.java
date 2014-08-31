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
import org.gagravarr.ogg.OggStreamIdentifier;

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

        int expectFishSid   = 0x794f5545;
        int expectCMMLSid   = 0x2ea9944e;
        int expectTheoraSid = 0x3aa5a330;

        // Should be the first stream
        p = r.getNextPacket();
        assertTrue(SkeletonPacketFactory.isSkeletonSpecial(p));
        assertEquals(expectFishSid, p.getSid());

        // Check the Fishead
        SkeletonFishead head = (SkeletonFishead)SkeletonPacketFactory.create(p);
        assertEquals(3, head.getVersionMajor());
        assertEquals(0, head.getVersionMinor());

        assertEquals(1000, head.getPresentationTimeDenominator());
        assertEquals(0, head.getPresentationTimeNumerator());
        assertEquals(1000, head.getBaseTimeDenominator());
        assertEquals(0, head.getBaseTimeNumerator());
        assertEquals(null, head.getUtc());

        // These two are zero as they're v4 only
        assertEquals(0, head.getContentOffset());
        assertEquals(0, head.getSegmentLength());


        // Next two should be the first packets of the CMML and Theora
        OggPacket cmml = r.getNextPacket();
        OggPacket theora = r.getNextPacket();

        assertEquals(expectCMMLSid, cmml.getSid());
        assertEquals(OggStreamIdentifier.CMML, OggStreamIdentifier.identifyType(cmml));

        assertEquals(expectTheoraSid, theora.getSid());
        assertEquals(OggStreamIdentifier.THEORA_VIDEO, OggStreamIdentifier.identifyType(theora));


        // Check the Fisbone
        p = r.getNextPacketWithSid(expectFishSid);
        SkeletonFisbone bone1 = (SkeletonFisbone)SkeletonPacketFactory.create(p);

        assertEquals(expectCMMLSid, bone1.getSerialNumber());
        assertEquals(3, bone1.getNumHeaderPackets());
        assertEquals(1000, bone1.getGranulerateNumerator());
        assertEquals(1, bone1.getGranulerateDenominator());
        assertEquals(0, bone1.getBaseGranule());
        assertEquals(0, bone1.getPreroll());
        assertEquals(32, bone1.getGranuleShift());

        assertEquals(1, bone1.getMessageHeaders().size());
        assertEquals(OggStreamIdentifier.CMML.mimetype, bone1.getContentType());


        // Check the second bone
        p = r.getNextPacketWithSid(expectFishSid);
        SkeletonFisbone bone2 = (SkeletonFisbone)SkeletonPacketFactory.create(p);

        assertEquals(expectTheoraSid, bone2.getSerialNumber());
        assertEquals(3, bone2.getNumHeaderPackets());
        assertEquals(30000299, bone2.getGranulerateNumerator());
        assertEquals(1000000, bone2.getGranulerateDenominator());
        assertEquals(0, bone2.getBaseGranule());
        assertEquals(0, bone2.getPreroll());
        assertEquals(6, bone2.getGranuleShift());

        assertEquals(1, bone2.getMessageHeaders().size());
        assertEquals(OggStreamIdentifier.THEORA_VIDEO_ALT.mimetype, bone2.getContentType());


        // We have a single key frame, that's empty
        p = r.getNextPacketWithSid(expectFishSid);
        SkeletonKeyFramePacket kf = (SkeletonKeyFramePacket)SkeletonPacketFactory.create(p);
        assertEquals(0, kf.getData().length);


        // And that's it
        p = r.getNextPacketWithSid(expectFishSid);
        assertNull(p);


        // Tidy up
        ogg.close();
    }
}
