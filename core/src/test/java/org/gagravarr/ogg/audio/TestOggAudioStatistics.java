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
package org.gagravarr.ogg.audio;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.vorbis.VorbisFile;

/**
 * Tests for the base Ogg Audio Statistics calculations
 */
public class TestOggAudioStatistics extends TestCase {
    private InputStream getTestVorbisFile() throws IOException {
        return this.getClass().getResourceAsStream("/testVORBIS.ogg");
    }
    private InputStream getTestOpusFile() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS_09.opus");
    }
    private Closeable af;

    @Override
    protected void tearDown() throws IOException {
        if (af != null) {
            af.close();
        }
    }

    public void testVorbisStats() throws IOException {
        OggFile ogg = new OggFile(getTestVorbisFile());
        VorbisFile vf = new VorbisFile(ogg);
        af = vf;

        OggAudioStatistics stats = new OggAudioStatistics(vf, vf);

        // Nothing until calculated
        assertEquals(0, stats.getAudioPacketsCount());
        assertEquals(0, stats.getAudioDataSize());
        assertEquals(0.0, stats.getDurationSeconds());
        assertEquals(0, stats.getHeaderOverheadSize());
        assertEquals(0, stats.getOggOverheadSize());

        // Have it calculated
        stats.calculate();

        // Check the resulting values
        // oggz-info reports:
        //    Content-Duration: 00:00:00.021
        //    Vorbis: serialno 0074691676
        //    12 packets in 3 pages, 4.0 packets/page, 2.499% Ogg overhead
        // File is 4241 bytes long
        assertEquals(9,    stats.getAudioPacketsCount());
        assertEquals(402,  stats.getAudioDataSize());

        assertEquals(21, (int)(stats.getDurationSeconds()*1000));
        assertEquals("00:00:00.02", stats.getDuration());

        assertEquals(30, vf.getInfo().getData().length);
        assertEquals(219, vf.getTags().getData().length);
        assertEquals(3484, vf.getSetup().getData().length);
        assertEquals(30+219+3484, stats.getHeaderOverheadSize());

        assertEquals(107,  stats.getOggOverheadSize()); // Should actually be 106 - rounding
        assertEquals(4242, stats.getAudioDataSize() +   // Rounding on overhead 
                           stats.getOggOverheadSize() +
                           stats.getHeaderOverheadSize());
        assertEquals(2.499, stats.getOggOverheadPercentage(), 0.03);
    }

    public void testOpusStats() throws IOException {
        OggFile ogg = new OggFile(getTestOpusFile());
        OpusFile of = new OpusFile(ogg);
        af = of;

        OggAudioStatistics stats = new OggAudioStatistics(of, of);

        // Nothing until calculated
        assertEquals(0, stats.getAudioPacketsCount());
        assertEquals(0, stats.getAudioDataSize());
        assertEquals(0.0, stats.getDurationSeconds());
        assertEquals(0, stats.getHeaderOverheadSize());
        assertEquals(0, stats.getOggOverheadSize());

        // Have it calculated
        stats.calculate();

        // Check the resulting values
        // opusinfo reports:
        //    Total data length: 1059 bytes (overhead: 23%)
        //    Playback length: 0m:00.021s
        //    Average bitrate: 389.1 kb/s, w/o overhead: 299.5 kb/s
        // oggz-info reports:
        //    2 packets in 3 pages, 0.7 packets/page, 8.215% Ogg overhead
        // File is 1059 bytes long
        assertEquals(2, stats.getAudioPacketsCount());
        assertEquals(815, stats.getAudioDataSize());

        assertEquals(21, (int)(stats.getDurationSeconds()*1000));
        assertEquals("00:00:00.02", stats.getDuration());

        assertEquals(19, of.getInfo().getData().length);
        assertEquals(138, of.getTags().getData().length);
        assertEquals(null, of.getSetup());
        assertEquals(19+138, stats.getHeaderOverheadSize());

        assertEquals(87, stats.getOggOverheadSize());
        assertEquals(1059, stats.getAudioDataSize() +
                           stats.getOggOverheadSize() +
                           stats.getHeaderOverheadSize());
        assertEquals(8.215, stats.getOggOverheadPercentage(), 0.001);
    }
}
