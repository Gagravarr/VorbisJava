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
package org.gagravarr.opus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.gagravarr.ogg.OggFile;

import static org.gagravarr.opus.TestOpusFileWrite.saveAndReload;

/**
 * Tests for the Opus-specific audio statistics calculations
 */
public class TestOpusStatistics extends AbstractOpusTest {
    public void testReadInfo11() throws IOException {
        OggFile ogg = new OggFile(getTest11File());
        of = new OpusFile(ogg);
        OpusStatistics stats = new OpusStatistics(of);
        stats.calculate();

        // General Stats
        assertEquals(2, stats.getAudioPacketsCount());
        assertEquals(927, stats.getAudioDataSize());
        assertEquals(4.947, stats.getOggOverheadPercentage(), 0.001);

        // Opus-specific Stats
        assertEquals(20.0, stats.getMaxPacketDuration());
        assertEquals(20.0, stats.getAvgPacketDuration());
        assertEquals(20.0, stats.getMinPacketDuration());
        assertEquals(40.0, stats.getMaxPageDuration());
        assertEquals(40.0, stats.getAvgPageDuration());
        assertEquals(40.0, stats.getMinPageDuration());

        assertEquals(0.021, stats.getDurationSeconds(), 0.001);
        assertEquals("00:00:00.02", stats.getDuration());
    }

    public void testReadInfo09() throws IOException {
        OggFile ogg = new OggFile(getTest09File());
        of = new OpusFile(ogg);
        OpusStatistics stats = new OpusStatistics(of);
        stats.calculate();

        // General Stats
        assertEquals(2, stats.getAudioPacketsCount());
        assertEquals(815, stats.getAudioDataSize());
        assertEquals(8.215, stats.getOggOverheadPercentage(), 0.001);

        // Opus-specific Stats
        assertEquals(20.0, stats.getMaxPacketDuration());
        assertEquals(20.0, stats.getAvgPacketDuration());
        assertEquals(20.0, stats.getMinPacketDuration());
        assertEquals(40.0, stats.getMaxPageDuration());
        assertEquals(40.0, stats.getAvgPageDuration());
        assertEquals(40.0, stats.getMinPageDuration());

        assertEquals(0.021, stats.getDurationSeconds(), 0.001);
        assertEquals("00:00:00.02", stats.getDuration());
        assertEquals("00:00:00.02", stats.getDuration(Locale.ROOT));
        assertEquals("00:00:00,02", stats.getDuration(Locale.FRENCH));
    }
    
    public void testReadWriteReadInfo() throws IOException {
        for (InputStream testFile : new InputStream[] {
                getTest09File(), getTest11File()
        }) {
            OggFile in = new OggFile(testFile);
            of = new OpusFile(in);
            
            // Write and Read Back
            of = new OpusFile(saveAndReload(of, -1));
            
            // Check
            OpusStatistics stats = new OpusStatistics(of);
            stats.calculate();
            
            // General Stats
            assertEquals(2, stats.getAudioPacketsCount());
            
            // Opus-specific Stats
            assertEquals(20.0, stats.getMaxPacketDuration());
            assertEquals(20.0, stats.getAvgPacketDuration());
            assertEquals(20.0, stats.getMinPacketDuration());
            assertEquals(40.0, stats.getMaxPageDuration());
            assertEquals(40.0, stats.getAvgPageDuration());
            assertEquals(40.0, stats.getMinPageDuration());

            assertEquals(0.021, stats.getDurationSeconds(), 0.001);
            assertEquals("00:00:00.02", stats.getDuration(Locale.ROOT));
        }
    }
}