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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for round-tripping with OpusFile
 */
public class TestOpusFileWrite extends TestCase {
    private InputStream getTest09File() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS_09.opus");
    }
    private InputStream getTest11File() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS_11.opus");
    }

    public void testReadWrite() throws IOException {
        OggFile in = new OggFile(getTest09File());
        OpusFile opIN = new OpusFile(in);

        int infoSize = opIN.getInfo().getData().length;
        int tagsSize = opIN.getTags().getData().length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OpusFile opOUT = new OpusFile(
                baos,
                opIN.getInfo(),
                opIN.getTags()
        );

        OpusAudioData oad;
        while( (oad = opIN.getNextAudioPacket()) != null ) {
            opOUT.writeAudioData(oad);
        }

        opIN.close();
        opOUT.close();

        assertEquals(infoSize, opOUT.getInfo().getData().length);
        assertEquals(tagsSize, opOUT.getTags().getData().length);
    }

    public void testReadWriteRead() throws IOException {
        for (InputStream inpStream : new InputStream[] {
            getTest09File()//, getTest11File() // TODO Fix to work with LibOpus 1.1    
        }) {
            OggFile in = new OggFile(inpStream);
            OpusFile opOrig = new OpusFile(in);

            int infoSize = opOrig.getInfo().getData().length;
            int tagsSize = opOrig.getTags().getData().length;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OpusFile opOUT = new OpusFile(
                    baos,
                    opOrig.getInfo(),
                    opOrig.getTags()
            );

            OpusAudioData oad;
            while( (oad = opOrig.getNextAudioPacket()) != null ) {
                opOUT.writeAudioData(oad);
            }

            opOrig.close();
            opOUT.close();


            // Now open the new one
            OpusFile opIN = new OpusFile(new OggFile(
                    new ByteArrayInputStream(baos.toByteArray())
            ));

            // And check
            assertEquals(2, opIN.getInfo().getChannels());
            assertEquals(44100, opIN.getInfo().getRate());

            assertEquals("Test Title", opIN.getTags().getTitle());
            assertEquals("Test Artist", opIN.getTags().getArtist());

            // Has some audio data, but not very much
            OpusAudioData ad = null;

            ad = opIN.getNextAudioPacket();
            assertNotNull( ad );
            assertEquals(0x579, ad.getGranulePosition());

            ad = opIN.getNextAudioPacket();
            assertNotNull( ad );
            assertEquals(0x579, ad.getGranulePosition());

            ad = opIN.getNextAudioPacket();
            assertNull( ad );


            // Check the core packets stayed the same size
            assertEquals(infoSize, opOUT.getInfo().getData().length);
            assertEquals(tagsSize, opOUT.getTags().getData().length);

            assertEquals(infoSize, opIN.getInfo().getData().length);
            assertEquals(tagsSize, opIN.getTags().getData().length);

            // Tidy up
            opIN.close();
        }
    }
}
