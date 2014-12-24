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
package org.gagravarr.speex;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for reading things using SpeexFile
 */
public class TestSpeexFileRead extends TestCase {
    private InputStream getTestFile() throws IOException {
        return this.getClass().getResourceAsStream("/testSPEEX.spx");
    }

    private SpeexFile sf;
    @Override
    protected void tearDown() throws IOException {
        if (sf != null) {
            sf.close();
        }
    }

    public void testRead() throws IOException {
        OggFile ogg = new OggFile(getTestFile());
        sf = new SpeexFile(ogg);

        assertEquals("1.2rc1", sf.getInfo().getVersionString());
        assertEquals(1, sf.getInfo().getVersionId());
        
        assertEquals(44100, sf.getInfo().getRate());
        assertEquals(2, sf.getInfo().getMode());
        assertEquals(4, sf.getInfo().getModeBitstreamVersion());
        assertEquals(2, sf.getInfo().getNumChannels());

        assertEquals(-1, sf.getInfo().getBitrate());
        assertEquals(0x280, sf.getInfo().getFrameSize());
        assertEquals(0, sf.getInfo().getVbr());
        assertEquals(1, sf.getInfo().getFramesPerPacket());
        assertEquals(0, sf.getInfo().getExtraHeaders());
        assertEquals(0, sf.getInfo().getReserved1());
        assertEquals(0, sf.getInfo().getReserved2());

        assertEquals("Test Title", sf.getTags().getTitle());
        assertEquals("Test Artist", sf.getTags().getArtist());

        // Has some audio data, but not very much
        SpeexAudioData ad = null;
        
        ad = sf.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x3c0, ad.getGranulePosition());
        
        ad = sf.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x3c0, ad.getGranulePosition());
        
        ad = sf.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x3c0, ad.getGranulePosition());
        
        ad = sf.getNextAudioPacket();
        assertNull( ad );
    }
}
