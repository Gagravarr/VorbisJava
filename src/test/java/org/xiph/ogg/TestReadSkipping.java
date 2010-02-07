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
package org.xiph.ogg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.xiph.ogg.OggPage.OggPacketIterator;

/**
 * Test that we can skip correctly through a file
 */
public class TestReadSkipping extends TestCase {
	private InputStream getTestFile() throws IOException {
		return this.getClass().getResourceAsStream("/testVORBIS.ogg");
	}
	
	public void testSkipToSequence() throws Exception {
		OggFile ogg;
		OggPacketReader r;
		OggPacket p;
		
		// Invalid sid, will run to end
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToSequenceNumber(-1, 0);
		assertEquals(null, r.getNextPacket());
		
		// Invalid sequence, will run to end
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToSequenceNumber(0x0473b45c, 100);
		assertEquals(null, r.getNextPacket());
		
		// Valid sequence, will go there
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToSequenceNumber(0x0473b45c, 0);
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(1, p.getSequenceNumber());
		
		// And another valid seq
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToSequenceNumber(0x0473b45c, 2);
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(2, p.getSequenceNumber());
	}
	
	public void testSkipToGranuleExact() throws Exception {
		OggFile ogg;
		OggPacketReader r;
		OggPacket p;
		
		// Invalid sid, will run to end
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToGranulePosition(-1, 0);
		assertEquals(null, r.getNextPacket());
		
		// Invalid gp, will run to end
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToGranulePosition(0x0473b45c, 0xedcba);
		assertEquals(null, r.getNextPacket());
		
		// Valid gp, will go there
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToGranulePosition(0x0473b45c, 0);
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(0, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0, p.getGranulePosition());
		assertEquals(1, p.getSequenceNumber());
		
		// And another valid gp
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToGranulePosition(0x0473b45c, 0x3c0);
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		
		// Now to a non-exact one
		ogg = new OggFile(getTestFile());
		r = ogg.getPacketReader();
		r.skipToGranulePosition(0x0473b45c, 0x321);
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(0x0473b45c, p.getSid());
		assertEquals(0x3c0, p.getGranulePosition());
		assertEquals(2, p.getSequenceNumber());
	}
}
