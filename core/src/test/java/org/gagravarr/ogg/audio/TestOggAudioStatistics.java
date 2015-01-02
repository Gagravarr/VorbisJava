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
        assertEquals(0, stats.getAudioDataSize());
        assertEquals(0, stats.getAudioPacketsCount());
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
        // TODO
    }

    public void testOpusStats() throws IOException {
        OggFile ogg = new OggFile(getTestOpusFile());
        OpusFile of = new OpusFile(ogg);
        af = of;

        OggAudioStatistics stats = new OggAudioStatistics(of, of);

        // Nothing until calculated
        assertEquals(0, stats.getAudioDataSize());
        assertEquals(0, stats.getAudioPacketsCount());
        assertEquals(0.0, stats.getDurationSeconds());
        assertEquals(0, stats.getHeaderOverheadSize());
        assertEquals(0, stats.getOggOverheadSize());

        // Have it calculated
        stats.calculate();

        // Check the resulting values
        // opus-info reports:
        //    ????
        // oggz-info reports:
        //    2 packets in 3 pages, 0.7 packets/page, 8.215% Ogg overhead
        // TODO
    }
}
