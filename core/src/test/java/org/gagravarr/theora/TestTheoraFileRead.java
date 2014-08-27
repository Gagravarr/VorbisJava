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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for reading things using TheoraFile
 */
public class TestTheoraFileRead extends TestCase {
    private InputStream getTheoraFile() throws IOException {
        return this.getClass().getResourceAsStream("/testTheora.ogg");
    }
    private InputStream getTheoraVorbisFile() throws IOException {
        return this.getClass().getResourceAsStream("/testTheoraVORBIS.ogg");
    }
    private InputStream getTheoraVorbisSkeletonFile() throws IOException {
        return this.getClass().getResourceAsStream("/testTheoraVORBISSkeleton.ogg");
    }
    private InputStream getTheoraSpeexFile() throws IOException {
        return this.getClass().getResourceAsStream("/testTheoraSPEEX.ogg");
    }
    // TODO Finish the other test files and use them

    public void testReadBasics() throws IOException {
        OggFile ogg = new OggFile(getTheoraFile());
        TheoraFile vf = new TheoraFile(ogg);

        // Check the Info
        assertEquals("3.2.0", vf.getInfo().getVersion());
        assertEquals(3, vf.getInfo().getMajorVersion());
        assertEquals(2, vf.getInfo().getMinorVersion());
        assertEquals(0, vf.getInfo().getRevisionVersion());

        assertEquals(20, vf.getInfo().getFrameWidthMB());
        assertEquals(15, vf.getInfo().getFrameHeightMB());
        assertEquals(320, vf.getInfo().getFrameWidth());
        assertEquals(240, vf.getInfo().getFrameHeight());
        assertEquals(320, vf.getInfo().getPictureRegionWidth());
        assertEquals(240, vf.getInfo().getPictureRegionHeight());
        assertEquals(0, vf.getInfo().getPictureRegionXOffset());
        assertEquals(0, vf.getInfo().getPictureRegionYOffset());

        assertEquals(0x01c9c4ab, vf.getInfo().getFrameRateNumerator());
        assertEquals(1000000, vf.getInfo().getFrameRateDenominator());
        assertEquals(0, vf.getInfo().getPixelAspectNumerator());
        assertEquals(0, vf.getInfo().getPixelAspectDenomerator());

        assertEquals(0, vf.getInfo().getColourSpace());
        assertEquals(0, vf.getInfo().getNominalBitrate());
        assertEquals(44, vf.getInfo().getQualityHint());
        assertEquals(6, vf.getInfo().getKeyFrameNumberGranuleShift());
        assertEquals(0, vf.getInfo().getPixelFormat());

        // Check the Comments
        assertEquals(
                "Xiph.Org libTheora I 20040317 3 2 0",
                vf.getComments().getVendor()
        );
        assertEquals(0, vf.getComments().getAllComments().size());

        // TODO Test the setup

        // Has a handful of video frames
        TheoraVideoData vd = null;

//        vd = vf.getNextVideoPacket();
//        assertNotNull( vd );
//        assertEquals(0x3c0, vd.getGranulePosition());

//        vd = vf.getNextVideoPacket();
//        assertNotNull( vd );
//        assertEquals(0x3c0, vd.getGranulePosition());

//        vd = vf.getNextVideoPacket();
        assertNull( vd );
    }

    public void testReadWithVorbisAudio() throws IOException {
        OggFile ogg = new OggFile(getTheoraVorbisFile());
        TheoraFile vf = new TheoraFile(ogg);

        // Check the Info
        assertEquals("3.2.1", vf.getInfo().getVersion());
        assertEquals(3, vf.getInfo().getMajorVersion());
        assertEquals(2, vf.getInfo().getMinorVersion());
        assertEquals(1, vf.getInfo().getRevisionVersion());

        assertEquals(40, vf.getInfo().getFrameWidthMB());
        assertEquals(30, vf.getInfo().getFrameHeightMB());
        assertEquals(640, vf.getInfo().getFrameWidth());
        assertEquals(480, vf.getInfo().getFrameHeight());
        assertEquals(640, vf.getInfo().getPictureRegionWidth());
        assertEquals(480, vf.getInfo().getPictureRegionHeight());
        assertEquals(0, vf.getInfo().getPictureRegionXOffset());
        assertEquals(0, vf.getInfo().getPictureRegionYOffset());

        assertEquals(1, vf.getInfo().getFrameRateNumerator());
        assertEquals(1, vf.getInfo().getFrameRateDenominator());
        assertEquals(0, vf.getInfo().getPixelAspectNumerator());
        assertEquals(0, vf.getInfo().getPixelAspectDenomerator());

        assertEquals(0, vf.getInfo().getColourSpace());
        assertEquals(0, vf.getInfo().getNominalBitrate());
        assertEquals(38, vf.getInfo().getQualityHint());
        assertEquals(6, vf.getInfo().getKeyFrameNumberGranuleShift());
        assertEquals(0, vf.getInfo().getPixelFormat());

        // Check the Comments
        assertEquals(
                "Xiph.Org libtheora 1.1 20090822 (Thusnelda)",
                vf.getComments().getVendor()
        );
        assertEquals(
                "ffmpeg2theora-0.27",
                vf.getComments().getComments("ENCODER").get(0)
        );
        // TODO Test the rest of the comments

        // TODO Test the setup

        // Has a handful of video frames
        TheoraVideoData vd = null;

//        vd = vf.getNextVideoPacket();
//        assertNotNull( vd );
//        assertEquals(0x3c0, vd.getGranulePosition());

//        vd = vf.getNextVideoPacket();
//        assertNotNull( vd );
//        assertEquals(0x3c0, vd.getGranulePosition());

//        vd = vf.getNextVideoPacket();
        assertNull( vd );
    }

    public void testReadWithOpusAudio() throws IOException {
        // TODO
    }

    public void testReadWithSkeleton() throws IOException {
        // TODO
    }
}
