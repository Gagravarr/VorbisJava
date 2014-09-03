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
import org.gagravarr.ogg.OggStreamAudioData;
import org.gagravarr.ogg.OggStreamAudioVisualData;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.skeleton.SkeletonStream;
import org.gagravarr.vorbis.VorbisAudioData;

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

    private TheoraFile tf;
    @Override
    protected void tearDown() throws IOException {
        if (tf != null) {
            tf.close();
        }
    }

    public void testReadBasics() throws IOException {
        OggFile ogg = new OggFile(getTheoraFile());
        tf = new TheoraFile(ogg);

        // Check the Info
        assertEquals("3.2.0", tf.getInfo().getVersion());
        assertEquals(3, tf.getInfo().getMajorVersion());
        assertEquals(2, tf.getInfo().getMinorVersion());
        assertEquals(0, tf.getInfo().getRevisionVersion());

        assertEquals(20, tf.getInfo().getFrameWidthMB());
        assertEquals(15, tf.getInfo().getFrameHeightMB());
        assertEquals(320, tf.getInfo().getFrameWidth());
        assertEquals(240, tf.getInfo().getFrameHeight());
        assertEquals(320, tf.getInfo().getPictureRegionWidth());
        assertEquals(240, tf.getInfo().getPictureRegionHeight());
        assertEquals(0, tf.getInfo().getPictureRegionXOffset());
        assertEquals(0, tf.getInfo().getPictureRegionYOffset());

        assertEquals(0x01c9c4ab, tf.getInfo().getFrameRateNumerator());
        assertEquals(1000000, tf.getInfo().getFrameRateDenominator());
        assertEquals(0, tf.getInfo().getPixelAspectNumerator());
        assertEquals(0, tf.getInfo().getPixelAspectDenomerator());

        assertEquals(0, tf.getInfo().getColourSpace());
        assertEquals(0, tf.getInfo().getNominalBitrate());
        assertEquals(44, tf.getInfo().getQualityHint());
        assertEquals(6, tf.getInfo().getKeyFrameNumberGranuleShift());
        assertEquals(0, tf.getInfo().getPixelFormat());


        // Check the Comments
        assertEquals(
                "Xiph.Org libTheora I 20040317 3 2 0",
                tf.getComments().getVendor()
        );
        assertEquals(0, tf.getComments().getAllComments().size());


        // TODO - proper setup packet checking
        assertEquals(255*10+0x57, tf.getSetup().getData().length);


        // Doesn't have a skeleton stream
        assertEquals(null, tf.getSkeleton());


        // Doesn't have any soundtracks
        assertNotNull(tf.getSoundtracks());
        assertNotNull(tf.getSoundtrackStreams());
        assertEquals(0, tf.getSoundtracks().size());
        assertEquals(0, tf.getSoundtrackStreams().size());


        // Has a quote a few video frames, but no audio
        OggStreamAudioVisualData avd = null;

        avd = tf.getNextAudioVisualPacket();
        assertNotNull( avd );
        assertEquals(TheoraVideoData.class, avd.getClass());
        assertEquals(0, avd.getGranulePosition());

        avd = tf.getNextAudioVisualPacket();
        assertNotNull( avd );
        assertEquals(TheoraVideoData.class, avd.getClass());
        assertEquals(1, avd.getGranulePosition());

        avd = tf.getNextAudioVisualPacket();
        assertNotNull( avd );
        assertEquals(TheoraVideoData.class, avd.getClass());
        assertEquals(3, avd.getGranulePosition());

        avd = tf.getNextAudioVisualPacket();
        assertNotNull( avd );
        assertEquals(TheoraVideoData.class, avd.getClass());
        assertEquals(3, avd.getGranulePosition());

        avd = tf.getNextAudioVisualPacket();
        assertNotNull( avd );
        assertEquals(TheoraVideoData.class, avd.getClass());
        assertEquals(5, avd.getGranulePosition());

        int count = 5;
        while ((avd = tf.getNextAudioVisualPacket()) != null) {
            assertEquals(TheoraVideoData.class, avd.getClass());
            count++;
        }
        assertTrue("Not enough, found " + count, count > 100);

        avd = tf.getNextAudioVisualPacket();
        assertNull( avd );
    }

    public void testReadWithVorbisAudio() throws IOException {
        OggFile ogg = new OggFile(getTheoraVorbisFile());
        tf = new TheoraFile(ogg);

        // Check the Info
        assertEquals("3.2.1", tf.getInfo().getVersion());
        assertEquals(3, tf.getInfo().getMajorVersion());
        assertEquals(2, tf.getInfo().getMinorVersion());
        assertEquals(1, tf.getInfo().getRevisionVersion());

        assertEquals(40, tf.getInfo().getFrameWidthMB());
        assertEquals(30, tf.getInfo().getFrameHeightMB());
        assertEquals(640, tf.getInfo().getFrameWidth());
        assertEquals(480, tf.getInfo().getFrameHeight());
        assertEquals(640, tf.getInfo().getPictureRegionWidth());
        assertEquals(480, tf.getInfo().getPictureRegionHeight());
        assertEquals(0, tf.getInfo().getPictureRegionXOffset());
        assertEquals(0, tf.getInfo().getPictureRegionYOffset());

        assertEquals(1, tf.getInfo().getFrameRateNumerator());
        assertEquals(1, tf.getInfo().getFrameRateDenominator());
        assertEquals(0, tf.getInfo().getPixelAspectNumerator());
        assertEquals(0, tf.getInfo().getPixelAspectDenomerator());

        assertEquals(0, tf.getInfo().getColourSpace());
        assertEquals(0, tf.getInfo().getNominalBitrate());
        assertEquals(38, tf.getInfo().getQualityHint());
        assertEquals(6, tf.getInfo().getKeyFrameNumberGranuleShift());
        assertEquals(0, tf.getInfo().getPixelFormat());

        // Check the Comments
        assertEquals(
                "Xiph.Org libtheora 1.1 20090822 (Thusnelda)",
                tf.getComments().getVendor()
        );
        assertEquals(
                "ffmpeg2theora-0.27",
                tf.getComments().getComments("ENCODER").get(0)
        );
        assertEquals(2, tf.getComments().getAllComments().size());

        // TODO - proper setup packet checking
        assertEquals(255*12+0x90, tf.getSetup().getData().length);

        // Doesn't have a skeleton stream
        assertEquals(null, tf.getSkeleton());

        // Has a single, vorbis soundtrack
        assertNotNull(tf.getSoundtracks());
        assertNotNull(tf.getSoundtrackStreams());
        assertEquals(1, tf.getSoundtracks().size());
        assertEquals(1, tf.getSoundtrackStreams().size());
        assertEquals(OggStreamIdentifier.OGG_VORBIS, 
                     tf.getSoundtracks().iterator().next().getType());


        // Has a handful of video and audio frames interleaved
        OggStreamAudioVisualData avd = null;

        int numTheora = 0;
        int numVorbis = 0;
        while ((avd = tf.getNextAudioVisualPacket()) != null) {
            if (avd instanceof TheoraVideoData) numTheora++;
            if (avd instanceof VorbisAudioData) numVorbis++;
        }

        assertTrue("Not enough video, found " + numTheora, numTheora > 0);
        assertTrue("Not enough audio, found " + numVorbis, numVorbis > 5);

        avd = tf.getNextAudioVisualPacket();
        assertNull( avd );
    }

    public void testReadWithOpusAudio() throws IOException {
        // TODO
    }

    public void testReadWithSpeexAudio() throws IOException {
        OggFile ogg = new OggFile(getTheoraSpeexFile());
        tf = new TheoraFile(ogg);

        // Check the Info
        assertEquals("3.2.1", tf.getInfo().getVersion());
        assertEquals(3, tf.getInfo().getMajorVersion());
        assertEquals(2, tf.getInfo().getMinorVersion());
        assertEquals(1, tf.getInfo().getRevisionVersion());

        assertEquals(40, tf.getInfo().getFrameWidthMB());
        assertEquals(30, tf.getInfo().getFrameHeightMB());
        assertEquals(640, tf.getInfo().getFrameWidth());
        assertEquals(480, tf.getInfo().getFrameHeight());
        assertEquals(640, tf.getInfo().getPictureRegionWidth());
        assertEquals(480, tf.getInfo().getPictureRegionHeight());
        assertEquals(0, tf.getInfo().getPictureRegionXOffset());
        assertEquals(0, tf.getInfo().getPictureRegionYOffset());

        assertEquals(1, tf.getInfo().getFrameRateNumerator());
        assertEquals(1, tf.getInfo().getFrameRateDenominator());
        assertEquals(0, tf.getInfo().getPixelAspectNumerator());
        assertEquals(0, tf.getInfo().getPixelAspectDenomerator());

        assertEquals(0, tf.getInfo().getColourSpace());
        assertEquals(0, tf.getInfo().getNominalBitrate());
        assertEquals(38, tf.getInfo().getQualityHint());
        assertEquals(6, tf.getInfo().getKeyFrameNumberGranuleShift());
        assertEquals(0, tf.getInfo().getPixelFormat());

        // Check the Comments
        assertEquals(
                "Xiph.Org libtheora 1.1 20090822 (Thusnelda)",
                tf.getComments().getVendor()
        );
        assertEquals(
                "ffmpeg2theora-0.27",
                tf.getComments().getComments("ENCODER").get(0)
        );
        assertEquals(1, tf.getComments().getAllComments().size());

        // TODO - proper setup packet checking
        assertEquals(255*12+0x90, tf.getSetup().getData().length);

        // Doesn't have a skeleton stream
        assertEquals(null, tf.getSkeleton());

        // Has a single, speex soundtrack
        assertNotNull(tf.getSoundtracks());
        assertNotNull(tf.getSoundtrackStreams());
        assertEquals(1, tf.getSoundtracks().size());
        assertEquals(1, tf.getSoundtrackStreams().size());
        assertEquals(OggStreamIdentifier.SPEEX_AUDIO, 
                     tf.getSoundtracks().iterator().next().getType());


        // Has a handful of video and audio frames interleaved
        OggStreamAudioVisualData avd = null;

        int numTheora = 0;
        int numAudio = 0;
        while ((avd = tf.getNextAudioVisualPacket()) != null) {
            if (avd instanceof TheoraVideoData) numTheora++;
            if (avd instanceof OggStreamAudioData) numAudio++;
        }

        assertTrue("Not enough video, found " + numTheora, numTheora > 0);
        assertTrue("Not enough audio, found " + numAudio,  numAudio > 0);

        avd = tf.getNextAudioVisualPacket();
        assertNull( avd );
    }

    public void testReadWithSkeleton() throws IOException {
        OggFile ogg = new OggFile(getTheoraVorbisSkeletonFile());
        tf = new TheoraFile(ogg);

        // Check the Info
        assertEquals("3.2.1", tf.getInfo().getVersion());
        assertEquals(3, tf.getInfo().getMajorVersion());
        assertEquals(2, tf.getInfo().getMinorVersion());
        assertEquals(1, tf.getInfo().getRevisionVersion());

        assertEquals(40, tf.getInfo().getFrameWidthMB());
        assertEquals(30, tf.getInfo().getFrameHeightMB());
        assertEquals(640, tf.getInfo().getFrameWidth());
        assertEquals(480, tf.getInfo().getFrameHeight());
        assertEquals(640, tf.getInfo().getPictureRegionWidth());
        assertEquals(480, tf.getInfo().getPictureRegionHeight());
        assertEquals(0, tf.getInfo().getPictureRegionXOffset());
        assertEquals(0, tf.getInfo().getPictureRegionYOffset());

        assertEquals(1, tf.getInfo().getFrameRateNumerator());
        assertEquals(1, tf.getInfo().getFrameRateDenominator());
        assertEquals(0, tf.getInfo().getPixelAspectNumerator());
        assertEquals(0, tf.getInfo().getPixelAspectDenomerator());

        assertEquals(0, tf.getInfo().getColourSpace());
        assertEquals(0, tf.getInfo().getNominalBitrate());
        assertEquals(38, tf.getInfo().getQualityHint());
        assertEquals(6, tf.getInfo().getKeyFrameNumberGranuleShift());
        assertEquals(0, tf.getInfo().getPixelFormat());

        // Check the Comments
        assertEquals(
                "Xiph.Org libtheora 1.1 20090822 (Thusnelda)",
                tf.getComments().getVendor()
        );
        assertEquals(
                "ffmpeg2theora-0.27",
                tf.getComments().getComments("ENCODER").get(0)
        );
        assertEquals(2, tf.getComments().getAllComments().size());

        // TODO - proper setup packet checking
        assertEquals(255*12+0x90, tf.getSetup().getData().length);

        // It has a skeleton stream
        SkeletonStream skel = tf.getSkeleton();
        assertNotNull(skel);
        assertNotNull(skel.getFishead());
        assertEquals("4.0", skel.getFishead().getVersion());
        assertEquals(2, skel.getFisbones().size());
        assertEquals("video/main", skel.getFisbones().get(0).getMessageHeaders().get("Role"));
        assertEquals("audio/main", skel.getFisbones().get(1).getMessageHeaders().get("Role"));

        // Has a single, vorbis soundtrack
        assertNotNull(tf.getSoundtracks());
        assertNotNull(tf.getSoundtrackStreams());
        assertEquals(1, tf.getSoundtracks().size());
        assertEquals(1, tf.getSoundtrackStreams().size());
        assertEquals(OggStreamIdentifier.OGG_VORBIS, 
                     tf.getSoundtracks().iterator().next().getType());


        // Has a handful of video and audio frames
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

    /**
     * Ensure that when we request audio-visual data with
     *  sid restrictions, we get (only) the data we wanted
     */
    public void testGetAudioVisualDataBySid() throws Exception {
        // Ask for everything, ensure we get everything

        // Ask for just video, shouldn't get audio

        // Ask for video + one audio, rest skipped

        // Ask for both audio, no video, get just that

        // TODO Implement this test
    }
}
