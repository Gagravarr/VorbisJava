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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for round-tripping with SpeexFile
 */
public class TestSpeexFileWrite extends TestCase {
    private InputStream getTestFile() throws IOException {
        return this.getClass().getResourceAsStream("/testSPEEX.spx");
    }

    public void testReadWrite() throws IOException {
        OggFile in = new OggFile(getTestFile());
        SpeexFile spIN = new SpeexFile(in);

        int infoSize = spIN.getInfo().getData().length;
        int tagsSize = spIN.getTags().getData().length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SpeexFile spOUT = new SpeexFile(
                baos,
                spIN.getInfo(),
                spIN.getTags()
        );

        SpeexAudioData sad;
        while( (sad = spIN.getNextAudioPacket()) != null ) {
            spOUT.writeAudioData(sad);
        }

        spIN.close();
        spOUT.close();

        assertEquals(infoSize, spOUT.getInfo().getData().length);
        assertEquals(tagsSize, spOUT.getTags().getData().length);
    }

    public void testReadWriteRead() throws IOException {
        OggFile in = new OggFile(getTestFile());
        SpeexFile spOrig = new SpeexFile(in);

        int infoSize = spOrig.getInfo().getData().length;
        int tagsSize = spOrig.getTags().getData().length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SpeexFile spOut = new SpeexFile(
                baos,
                spOrig.getInfo(),
                spOrig.getTags()
        );

        SpeexAudioData sad;
        while( (sad = spOrig.getNextAudioPacket()) != null ) {
            spOut.writeAudioData(sad);
        }

        spOrig.close();
        spOut.close();


        // Now open the new one
        SpeexFile spIN = new SpeexFile(new OggFile(
                new ByteArrayInputStream(baos.toByteArray())
        ));

        // And check
        assertEquals(2, spIN.getInfo().getNumChannels());
        assertEquals(44100, spIN.getInfo().getRate());

        assertEquals("Test Title", spIN.getTags().getTitle());
        assertEquals("Test Artist", spIN.getTags().getArtist());

        // Has some audio data, but not very much
        SpeexAudioData ad = null;
        
        ad = spIN.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x3c0, ad.getGranulePosition());
        
        ad = spIN.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x3c0, ad.getGranulePosition());
        
        ad = spIN.getNextAudioPacket();
        assertNotNull( ad );
        assertEquals(0x3c0, ad.getGranulePosition());
        
        ad = spIN.getNextAudioPacket();
        assertNull( ad );


        // Check the core packets stayed the same size
        assertEquals(infoSize, spOut.getInfo().getData().length);
        assertEquals(tagsSize, spOut.getTags().getData().length);

        assertEquals(infoSize, spIN.getInfo().getData().length);
        assertEquals(tagsSize, spIN.getTags().getData().length);


        // Tidy up
        spIN.close();
    }
}
