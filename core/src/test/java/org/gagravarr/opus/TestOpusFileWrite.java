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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for round-tripping with OpusFile
 */
public class TestOpusFileWrite extends AbstractOpusTest {
    public static OpusFile save(OpusFile opOrig, int maxAudioPerPage,
                                ByteArrayOutputStream baos) throws IOException {
        // Have it written
        OpusFile opOUT = new OpusFile(
                baos,
                opOrig.getInfo(),
                opOrig.getTags()
        );

        // Granules controlled by packets per page
        opOUT.setMaxPacketsPerPage(maxAudioPerPage);

        OpusAudioData oad;
        while( (oad = opOrig.getNextAudioPacket()) != null ) {
            opOUT.writeAudioData(oad);
        }

        opOrig.close();
        opOUT.close();
        
        return opOUT;
    }

    public static OggFile saveAndReload(OpusFile opOrig, int maxAudioPerPage) throws IOException {
        // To write to
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Have written
        save(opOrig, maxAudioPerPage, baos);
        
        // Open the new one
        return new OggFile(
                new ByteArrayInputStream(baos.toByteArray()));
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
        InputStream[] testFiles = new InputStream[] {
                getTest09File(), getTest11File()
        };
        boolean[] hasFullTags = new boolean[] { false, true };
        for (int i=0; i<testFiles.length; i++) {
            InputStream inpStream = testFiles[i];
            boolean fullTags = hasFullTags[i];

            OggFile in = new OggFile(inpStream);
            OpusFile opOrig = new OpusFile(in);

            int infoSize = opOrig.getInfo().getData().length;
            int tagsSize = opOrig.getTags().getData().length;

            // Tags could be null padded in the original, so adjust
            while (tagsSize > 0 && opOrig.getTags().getData()[tagsSize-1] == 0) {
                tagsSize--;
            }


            // Have it written and read back
            // Preserve granules and page boundaries
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OpusFile opOUT = save(opOrig, -1, baos);

            // Open the new one
            OpusFile opIN = new OpusFile(new OggFile(
                    new ByteArrayInputStream(baos.toByteArray())));


            // And check
            assertEquals(2, opIN.getInfo().getNumChannels());
            assertEquals(44100, opIN.getInfo().getRate());

            assertEquals("Test Title", opIN.getTags().getTitle());
            assertEquals("Test Artist", opIN.getTags().getArtist());

            if (fullTags) {
                assertEquals("Test Album", opIN.getTags().getAlbum());
                assertEquals("2010-01-26", opIN.getTags().getDate());
                assertEquals("Test Genre", opIN.getTags().getGenre());
                assertEquals("1", opIN.getTags().getTrackNumber());

                assertEquals(2, opIN.getTags().getComments("Comment").size());
                assertEquals("Test Comment", opIN.getTags().getComments("Comment").get(0));
                assertEquals("Another Test Comment", opIN.getTags().getComments("Comment").get(1));
            } else {
                assertEquals(null, opIN.getTags().getAlbum());
                assertEquals(null, opIN.getTags().getDate());
                assertEquals(null, opIN.getTags().getGenre());
                assertEquals(null, opIN.getTags().getTrackNumber());

                assertEquals(1, opIN.getTags().getComments("Comment").size());
                assertEquals("Test Comment", opIN.getTags().getComments("Comment").get(0));
            }

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


            // Check the core packets stayed the same size (modulo padding!)
            assertEquals(infoSize, opOUT.getInfo().getData().length);
            assertEquals(tagsSize, opOUT.getTags().getData().length);

            assertEquals(infoSize, opIN.getInfo().getData().length);
            assertEquals(tagsSize, opIN.getTags().getData().length);

            // Tidy up
            opIN.close();
        }
    }

    public void testGranuleOnWrite() throws IOException {
        int[] packetsPerPage = new int[] { 1, 2 };
        for (int ppp : packetsPerPage) {
            for (InputStream testFile : new InputStream[] {
                    getTest09File(), getTest11File()
            }) {
                OggFile in = new OggFile(testFile);
                OpusFile opOrig = new OpusFile(in);

                // Have it written and read back
                OpusFile opIN = new OpusFile(saveAndReload(opOrig, ppp));

                // And check
                assertEquals(2, opIN.getInfo().getNumChannels());
                assertEquals(44100, opIN.getInfo().getRate());

                assertEquals("Test Title", opIN.getTags().getTitle());
                assertEquals("Test Artist", opIN.getTags().getArtist());

                // Has some audio data, but not very much
                OpusAudioData ad = null;
    
                // First Packet may be alone, or may join other one,
                //  depending on the number of packets in a page
                ad = opIN.getNextAudioPacket();
                assertNotNull( ad );
                assertEquals(0x3c0, ad.getNumberOfSamples());
                if (ppp == 1) {
                    // Just for this one
                    assertEquals(0x3c0, ad.getGranulePosition());
                } else {
                    // Shared with Packet 2
                    assertEquals(0x3c0+0x3c0, ad.getGranulePosition());
                }
    
                ad = opIN.getNextAudioPacket();
                assertNotNull( ad );
                assertEquals(0x3c0+0x3c0, ad.getGranulePosition());
                assertEquals(0x3c0, ad.getNumberOfSamples());
    
                ad = opIN.getNextAudioPacket();
                assertNull( ad );

                // Tidy up
                opIN.close();
            }
        }
    }

    /**
     * Write generated sample frames.
     * 
     * @throws Exception
     */
    public void testWriteAudio() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Setup a new empty file
        OpusFile opus = new OpusFile(baos);
        opus.getInfo().setSampleRate(48000);
        opus.getInfo().setNumChannels(2);
        opus.getTags().addComment("title","Test Dummy Audio");
        OpusAudioData audio = null;

        // Add some dummy audio data to it
        // This should really be proper PCM data, but we're just testing!
        byte[][] data = new byte[20][];
        for (int i=0; i<data.length; i++) {
            byte[] td = new byte[i*50];
            for (int j=0; j<td.length; j++) {
                td[j] = (byte)(j%99);
            }
            data[i] = td;

            audio = new OpusAudioData(td);
            opus.writeAudioData(audio);
        }

        // Write it out and re-read
        opus.close();
        OggFile ogg = new OggFile(new ByteArrayInputStream(baos.toByteArray()));
        opus = new OpusFile(ogg);

        // Check it looks as expected
        assertEquals(2, opus.getInfo().getNumChannels());
        assertEquals(48000, opus.getInfo().getSampleRate());
        assertEquals("Test Dummy Audio", opus.getTags().getTitle());

        // Check the dummy data
        int count = 0;
        while ((audio = opus.getNextAudioPacket()) != null) {
           byte[] exp = data[count];
           assertEquals(exp.length, audio.getData().length);
           for (int i=0; i<exp.length; i++) {
              assertEquals(exp[i], audio.getData()[i]);
           }
           count++;
        }
        assertEquals(data.length, count);
    }


    /**
     * Write pre-encoded sample frames created as a continuous tone.
     * 
     * @throws Exception
     */
    public void testWritePreEncodedAudio() throws Exception {
        // test file with hex values
        File tf = new File("target/test-classes/opus-tone.log");
        // output file for auditory verification
        File of = new File("target/test-classes/out.opus");
        // delete old file if its there
        if (of.exists()) {
            of.delete();
        }
        // create a new file
        of.createNewFile();
        OpusFile opus = null;
        // set opus out
        OpusInfo oi = new OpusInfo();
        oi.setSampleRate(48000);
        oi.setNumChannels(2);
        //oi.setOutputGain(0);
        //oi.setPreSkip(0);
        OpusTags ot = new OpusTags();
        ot.addComment("title", "Test Dummy Audio");
        opus = new OpusFile(new FileOutputStream(of), oi, ot);
        // read encoded hex values and convert to byte arrays for writing
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tf)));
        String line;
        while ((line = br.readLine()) != null)   {
            OpusAudioData audio = new OpusAudioData(hexStringToByteArray(line));
            opus.writeAudioData(audio);
        }
        br.close();
        // Write it out and re-read
        opus.close();
        // read the output file and check the configuration post-write
        OggFile ogg = new OggFile(new FileInputStream(of));
        opus = new OpusFile(ogg);
        // Check it looks as expected
        assertEquals(2, opus.getInfo().getNumChannels());
        assertEquals(48000, opus.getInfo().getSampleRate());
        assertEquals("Test Dummy Audio", opus.getTags().getTitle());
        // Check the dummy data with ffplay or opus tools
    }

    /**
     * Converts an hexadecimal string into a proper byte array.
     * 
     * @param s hex encoded string
     * @return byte[]
     */
    public final static byte[] hexStringToByteArray(String s) {
        // remove all the whitespace first
        s = s.replaceAll("\\s+", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }    
    
}
