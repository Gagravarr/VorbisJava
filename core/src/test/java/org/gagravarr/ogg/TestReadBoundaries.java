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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggPage.OggPacketIterator;

/**
 * Test that we do the right things when reading files
 *  that have unusual (but permitted!) packet/page
 *  boundaries
 */
public class TestReadBoundaries extends TestCase {
	private InputStream getTestFile() throws IOException {
		return this.getClass().getResourceAsStream("/testBoundaries.ogg");
	}
	
	public static byte[] getBytes(int len) {
		byte[] b = new byte[len];
		for(int i=0; i<len; i++) {
			b[i] = IOUtils.fromInt(	i % 256 );
		}
		return b;
	}
	private static void assertEquals(byte[] a, byte[] b) {
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(a.length, b.length);
		for(int i=0; i<a.length; i++) {
			assertEquals(a[i], b[i]);
		}
	}
	
	/**
	 * Files is made up of:
	 *  Page 1: 
	 *    6 byte packet
	 *  Page 2:
	 *    6 byte packet
	 *    6 byte packet
	 *    765 out of a 1028 byte packet
	 *  Page 3:
	 *    263 out of a 1028 byte packet 
	 */
	private void doTest(OggPacketReader r) throws IOException {
		OggPacket p = r.getNextPacket();
		assertEquals(0x1234, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(0, p.getSequenceNumber());
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(6, p.getData().length);
		assertEquals(new byte[] {0,1,2,3,4,5}, p.getData());
		
		p = r.getNextPacket();
		assertEquals(0x1234, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(1, p.getSequenceNumber());
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(6, p.getData().length);
		assertEquals(new byte[] {0,1,2,3,4,5}, p.getData());
		
		p = r.getNextPacket();
		assertEquals(0x1234, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(1, p.getSequenceNumber());
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(6, p.getData().length);
		assertEquals(new byte[] {10,11,12,13,14,15}, p.getData());
		
		p = r.getNextPacket();
		assertEquals(0x1234, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber()); // Spans 1 and 2
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		assertEquals(1028, p.getData().length);
		assertEquals(getBytes(1028), p.getData());
	}
	
	public void testPages() throws IOException {
		InputStream i = getTestFile();
		OggPage p;
		OggPacketData d;
		OggPacketIterator it;
		
		
		i.read(); i.read(); i.read(); i.read();
		p = new OggPage(i);
		assertEquals(1, p.getNumLVs());
		assertEquals(false, p.isContinuation());
		assertEquals(false, p.hasContinuation());
		
		it = p.getPacketIterator();
		assertEquals(true, it.hasNext());
		d = it.next();
		assertEquals(6, d.getData().length);
		assertEquals(false, it.hasNext());

		
		i.read(); i.read(); i.read(); i.read();
		p = new OggPage(i);
		assertEquals(5, p.getNumLVs());
		assertEquals(false, p.isContinuation());
		assertEquals(true, p.hasContinuation());
		
		it = p.getPacketIterator();
		assertEquals(true, it.hasNext());
		d = it.next();
		assertEquals(6, d.getData().length);
		assertEquals(true, it.hasNext());
		d = it.next();
		assertEquals(6, d.getData().length);
		assertEquals(true, it.hasNext());
		d = it.next();
		assertEquals(255*3, d.getData().length);
		assertEquals(false, it.hasNext());

		
		i.read(); i.read(); i.read(); i.read();
		p = new OggPage(i);
		assertEquals(2, p.getNumLVs());
		assertEquals(true, p.isContinuation());
		assertEquals(false, p.hasContinuation());
		
		it = p.getPacketIterator();
		assertEquals(true, it.hasNext());
		d = it.next();
		assertEquals(255+8, d.getData().length);
		assertEquals(false, it.hasNext());
	}
	
	public void testProcessRaw() throws IOException {
		OggPacketReader r = new OggPacketReader( getTestFile() );
		doTest(r);
	}
	
	public void testProcessOggFile() throws IOException {
		OggFile ogg = new OggFile( getTestFile() );
		OggPacketReader r = ogg.getPacketReader();
		doTest(r);
	}
}
