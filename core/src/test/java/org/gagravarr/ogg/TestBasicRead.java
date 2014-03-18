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
package org.gagravarr.ogg;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggPage.OggPacketIterator;

/**
 * Test that we can open a file and read packets from it
 */
public class TestBasicRead extends TestCase {
        protected static final String testVorbisFile = "/testVORBIS.ogg";
        protected static final String testFlacOggFile = "/testFLAC.oga";

	private InputStream getTestFile() throws IOException {
		return this.getClass().getResourceAsStream(testVorbisFile);
	}
	private InputStream getAltTestFile() throws IOException {
            return this.getClass().getResourceAsStream(testFlacOggFile);
	}

	public void testOpen() throws IOException {
		OggFile ogg = new OggFile(getTestFile());
		OggPacketReader r = ogg.getPacketReader();
		r.getNextPacket();
		
		// Can't write to a reading file
		try {
			ogg.getPacketWriter();
			fail();
		} catch(IllegalStateException e) {}
	}
	
	public void testPages() throws IOException {
		// We have three pages
		InputStream inp = getTestFile();
		inp.read();
		inp.read();
		inp.read();
		inp.read();
		
		OggPage page = new OggPage(inp);
		assertEquals(false, page.isContinuation());
		assertEquals(false, page.hasContinuation());
		assertEquals(0, page.getGranulePosition());
		assertEquals(0x0473b45c, page.getSid());
		assertEquals(0, page.getSequenceNumber());
		
		assertEquals(true, page.isChecksumValid());
		
		assertEquals(0x1e, page.getDataSize());
		assertEquals(0x1e + 28, page.getPageSize());
		
		// Should hold one packet
		assertEquals(1, page.getNumLVs());
		OggPacketIterator i = page.getPacketIterator();
		
		assertEquals(true, i.hasNext());
		OggPacket p = (OggPacket)i.next();
		
		assertEquals(0x1e, p.getData().length);
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		
		assertEquals(false, i.hasNext());
		
		
		// Next page
		inp.read();
		inp.read();
		inp.read();
		inp.read();
		
		page = new OggPage(inp);
		assertEquals(false, page.isContinuation());
		assertEquals(false, page.hasContinuation());
		assertEquals(0, page.getGranulePosition());
		assertEquals(0x0473b45c, page.getSid());
		assertEquals(1, page.getSequenceNumber());
		
		assertEquals(true, page.isChecksumValid());
		
		// Two packets, one small and one big
		assertEquals(0x0f, page.getNumLVs());
		i = page.getPacketIterator();
		
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		
		assertEquals(0xdb, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		
		assertEquals(255*13+0xa9, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		
		
		assertEquals(false, i.hasNext());	
		
		
		// Final page
		inp.read();
		inp.read();
		inp.read();
		inp.read();
		
		page = new OggPage(inp);
		assertEquals(false, page.isContinuation());
		assertEquals(false, page.hasContinuation());
		assertEquals(0x3c0, page.getGranulePosition());
		assertEquals(0x0473b45c, page.getSid());
		assertEquals(2, page.getSequenceNumber());
		
		assertEquals(true, page.isChecksumValid());
		
		// 9 small packets
		assertEquals(0x09, page.getNumLVs());
		i = page.getPacketIterator();
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x23, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x1c, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x1e, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x34, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x33, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x45, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x3b, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x28, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		
		assertEquals(true, i.hasNext());
		p = (OggPacket)i.next();
		assertEquals(0x26, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		
		assertEquals(false, i.hasNext());
		
		// end!
		assertEquals(-1, inp.read());
	}
	
	public void testPackets() throws IOException {
		OggFile ogg = new OggFile(getTestFile());
		OggPacketReader r = ogg.getPacketReader();
		OggPacket p;

		// Has 3 pages
		//  1 packet
		//  2 packets (2nd big)
		//  9 packets
		
		// Page 1
		p = r.getNextPacket();
		assertEquals(0x1e, p.getData().length);
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(0, p.getSequenceNumber());
		
		// Page 2
		p = r.getNextPacket();
		assertEquals(0xdb, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(1, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(255*13+0xa9, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(1, p.getSequenceNumber());
		
		// Page 3
		p = r.getNextPacket();
		assertEquals(0x23, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x1c, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x1e, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x34, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x33, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x45, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x3b, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x28, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(0x26, p.getData().length);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertEquals(null, p);
	}
	
	public void testCRC() throws IOException {
		InputStream inp = getTestFile();
		inp.read();
		inp.read();
		inp.read();
		inp.read();
		
		OggPage page = new OggPage(inp);
		assertEquals( 0x69e0b860, page.getChecksum() );
		assertTrue( page.isChecksumValid() );
		
		// Grab the header, won't have a checksum in it 
		byte[] header = page.getHeader();
		assertEquals(28, header.length);
		
		
		// Now re-create the page
		OggPacket p = (OggPacket)
			page.getPacketIterator().next();
		page = new OggPage(0x0473b45c, 0);
		page.addPacket(p, 0);
		
		// Checksum starts empty
		assertEquals(0, page.getChecksum() );
		assertTrue( page.isChecksumValid() );
		
		// Look at the headers
		byte[] header2 = page.getHeader();
		assertEquals(28, header2.length);
		// Check they're the same so far
		for(int i=0; i<28; i++) {
			assertEquals(header[i], header2[i]);
		}
		
		// Call the checksum ourselves
		int crc = CRCUtils.getCRC(header);
		crc = CRCUtils.getCRC(page.getData(), crc);
		assertEquals(0x69e0b860, crc);
		
		crc = CRCUtils.getCRC(header);
		crc = CRCUtils.getCRC(page.getData(), crc);
		assertEquals(0x69e0b860, crc);
		
		// Ensure there's nothing funny in the crc
		CRCUtils.getCRC(new byte[] {1,2,3,4});
		
		
		// Write out - will calculate
		page.writeHeader(new ByteArrayOutputStream());
		assertEquals( 0x69e0b860, page.getChecksum() );
		assertTrue( page.isChecksumValid() );
	}
	
	/**
	 * Issue-5 - Certain pages are giving "invalid checksum" warnings
	 */
        public void testCRCProblem() throws IOException {
            InputStream inp = getAltTestFile();

            // Skip to page 1
            assertEquals((int)'O', inp.read());
            assertEquals((int)'g', inp.read());
            assertEquals((int)'g', inp.read());
            assertEquals((int)'S', inp.read());

            // Check page 1
            OggPage page = new OggPage(inp);
            assertEquals( 0x41aacbc9, page.getChecksum() );
            assertTrue( page.isChecksumValid() );

            // Move on to page 2
            assertEquals((int)'O', inp.read());
            assertEquals((int)'g', inp.read());
            assertEquals((int)'g', inp.read());
            assertEquals((int)'S', inp.read());

            // Check page 2
            page = new OggPage(inp);
            assertEquals( 0x5dda5a9b, page.getChecksum() );
            assertTrue( page.isChecksumValid() );

            // Move on to page 3
            assertEquals((int)'O', inp.read());
            assertEquals((int)'g', inp.read());
            assertEquals((int)'g', inp.read());
            assertEquals((int)'S', inp.read());

            // Check page 3
            page = new OggPage(inp);
            assertEquals( 0x652c44eb, page.getChecksum() );
            assertTrue( page.isChecksumValid() );

            // Page 3 is standalone
            assertEquals(false, page.isContinuation());
            assertEquals(false, page.hasContinuation());


            // Buffer, so we can re-read it
            BufferedInputStream binp = new BufferedInputStream(inp, 5000);
            binp.mark(5000);

            // Move on to page 4
            assertEquals((int)'O', binp.read());
            assertEquals((int)'g', binp.read());
            assertEquals((int)'g', binp.read());
            assertEquals((int)'S', binp.read());

            // Check page 4, which is a big page
            page = new OggPage(binp);
            assertEquals( 0x8dff7da8, page.getChecksum() );

            // Page 4 carries on
            assertEquals(false, page.isContinuation());
            assertEquals(true, page.hasContinuation());

            // Ensure that page 4 is as we expect it to be
            assertEquals(4379, page.getPageSize());
            assertEquals(44, page.getHeader().length);
            assertEquals(4335, page.getDataSize());
            assertEquals(17, page.getNumLVs());

            assertEquals(-1, page.getGranulePosition());
            assertEquals(0x6e630f49, page.getSid());
            assertEquals(3, page.getSequenceNumber());

            // Wind back, and get the real header
            binp.reset();
            byte[] realHeader = new byte[44];
            IOUtils.readFully(binp, realHeader);
            byte[] calcHeader = page.getHeader();

            // Compare the two, other than the checksums
            for (int i=0; i<realHeader.length; i++) {
                if (i == 22) i += 4;
                assertEquals("Wrong value at " + i, realHeader[i], calcHeader[i]);
            }

            // Finally check the checksum
            assertTrue( page.isChecksumValid() );
        }

        // Which file extensions aren't actually Ogg files
	protected static final List<String> NON_OGG_EXTENSIONS =
	        Arrays.asList(new String[] { "flac" });

	/**
	 * Ensures that we can read all of the ogg-based files in
	 *  the Test Resources directory, and correctly fail on
	 *  the non-ogg ones.
	 */
	public void testManyFormats() throws IOException {
	    // Get the directory holding our test files
	    File testFilesDir = new File(getClass().getResource(testVorbisFile).getFile()).getParentFile();

	    // Check each one
	    int validFiles = 0;
	    for (File file : testFilesDir.listFiles()) {
	        String filename = file.getName();
	        if (filename.startsWith("test") && 
	                filename.contains(".")) {
	            // Work out if we expect it to pass or not
	            String extension = filename.substring(filename.lastIndexOf('.')+1);
	            boolean isOgg = ! NON_OGG_EXTENSIONS.contains(extension);
	            
	            // Try opening it
	            try {
	                OggFile ogg = new OggFile(new FileInputStream(file));
	                OggPacketReader r = ogg.getPacketReader();

	                // Ensure we can process the file
	                int packets = 0;
	                while (r.getNextPacket() != null && packets <= 2000) {
	                    packets++;
	                }
	                assertTrue("Bogus packets found in " + filename, packets < 1750);
	                
	                // Check that it worked for the right reason
	                if (isOgg) {
                            assertTrue("No packets found in " + filename, packets >= 1);
	                    validFiles ++;
	                } else {
	                    if (packets == 0) {
	                        // Good, non-ogg file shouldn't have any
	                    } else {
	                        fail("Non Ogg file processed " + filename);
	                    }
	                }
	            } catch (Exception e) {
	                if (isOgg) {
	                    fail("Error processing Ogg file " + filename + "\n" + e);
	                } else {
	                    // Not an Ogg file, so this is correct
	                }
	            }
	        } else {
	            // Not a test file, ignore
	        }
	    }
	    
	    assertTrue("Not enough files, found " + validFiles, validFiles >= 4);
	}
}
