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

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for reading things using OpusFile
 */
public class TestOpusFileRead extends TestCase {
    /** Get a test file created with libopus 0.9.x */
    private InputStream getTest09File() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS_09.opus");
    }
    /** Get a test file created with libopus 1.1.x */
    private InputStream getTest11File() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS_11.opus");
    }

    private OpusFile of;
    @Override
    protected void tearDown() throws IOException {
        if (of != null) {
            of.close();
        }
    }

    public void testRead09() throws IOException {
        OggFile ogg = new OggFile(getTest09File());
        of = new OpusFile(ogg);

        assertEquals(1, of.getInfo().getVersion());
        assertEquals(0, of.getInfo().getMajorVersion());
        assertEquals(1, of.getInfo().getMinorVersion());

        assertEquals(2, of.getInfo().getChannels());

        assertEquals(44100, of.getInfo().getRate());
        assertEquals(0x164, of.getInfo().getPreSkip());
        assertEquals(0, of.getInfo().getOutputGain());
        
        assertEquals(0, of.getInfo().getChannelMappingFamily());

        assertEquals("Test Title", of.getTags().getTitle());
        assertEquals("Test Artist", of.getTags().getArtist());
        // TODO Add null checks for tags not in the 0.9 file

        // Has some audio data, but not very much
        OpusAudioData ad = null;

        ad = of.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x579, ad.getGranulePosition());

        ad = of.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x579, ad.getGranulePosition());

        ad = of.getNextAudioPacket();
        assertNull( ad );
    }

    // TODO Fix this test to work
    public void DISABLEDtestRead11() throws IOException {
        OggFile ogg = new OggFile(getTest11File());
        of = new OpusFile(ogg);

        assertEquals(1, of.getInfo().getVersion());
        assertEquals(0, of.getInfo().getMajorVersion());
        assertEquals(1, of.getInfo().getMinorVersion());

        assertEquals(2, of.getInfo().getChannels());

        assertEquals(44100, of.getInfo().getRate());
        assertEquals(0x164, of.getInfo().getPreSkip());
        assertEquals(0, of.getInfo().getOutputGain());

        assertEquals(0, of.getInfo().getChannelMappingFamily());

        // TODO Check the rest of the tags
        assertEquals("Test Title", of.getTags().getTitle());
        assertEquals("Test Artist", of.getTags().getArtist());

        // Has some audio data, but not very much
        OpusAudioData ad = null;

        ad = of.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x579, ad.getGranulePosition());

        ad = of.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x579, ad.getGranulePosition());

        ad = of.getNextAudioPacket();
        assertNull( ad );
    }
}
