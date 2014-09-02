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
package org.gagravarr.vorbis;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for reading things using VorbisFile
 */
public class TestVorbisFileRead extends TestCase {
    private InputStream getTestFile() throws IOException {
        return this.getClass().getResourceAsStream("/testVORBIS.ogg");
    }
	
    private VorbisFile vf;
    @Override
    protected void tearDown() throws IOException {
        if (vf != null) {
            vf.close();
        }
    }

    public void testRead() throws IOException {
        OggFile ogg = new OggFile(getTestFile());
        vf = new VorbisFile(ogg);

        assertEquals(2, vf.getInfo().getChannels());
        assertEquals(44100, vf.getInfo().getRate());

        assertEquals(0, vf.getInfo().getBitrateLower());
        assertEquals(0, vf.getInfo().getBitrateUpper());
        assertEquals(80000, vf.getInfo().getBitrateNominal());

        assertEquals("Test Title", vf.getComment().getTitle());
        assertEquals("Test Artist", vf.getComment().getArtist());

        // TODO - proper setup packet checking
        assertEquals(255*13+0xa9, vf.getSetup().getData().length);

        // Has audio data
        assertNotNull( vf.getNextAudioPacket() );
        assertNotNull( vf.getNextAudioPacket() );
        assertNotNull( vf.getNextAudioPacket() );
        assertNotNull( vf.getNextAudioPacket() );

        VorbisAudioData ad = vf.getNextAudioPacket();
        assertEquals(0x3c0, ad.getGranulePosition());
    }
}
