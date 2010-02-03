package org.xiph.ogg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Test that we can do basic writing without error
 */
public class TestBasicWrite extends TestCase {
	public void testOpen() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OggFile ogg = new OggFile(baos);
		
		// Can't read
		try {
			ogg.getPacketReader();
			fail();
		} catch(IllegalStateException e) {}
		
		// Can add
		OggPacketWriter w = ogg.getPacketWriter(1234);
		w.close();
		ogg.close();
		
	}
	
	public void testEmptyPages() throws IOException {
		InputStream inp;
		OggFile opened;
		OggPage page;
		OggPacket p;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OggFile ogg = new OggFile(baos);
		
		// Write an empty stream
		OggPacketWriter w = ogg.getPacketWriter(1234);
		w.close();
		
		// Should have created an empty single page
		//  with a single empty packet
		assertEquals(28, baos.size());
		
		
		// Check at page level
		inp = new ByteArrayInputStream(baos.toByteArray());
		inp.read(); inp.read(); inp.read(); inp.read();
		page = new OggPage(inp);
		assertEquals(1, page.getNumLVs());
		
		
		// Check
		opened = new OggFile( new ByteArrayInputStream(baos.toByteArray()) );
		OggPacketReader r = opened.getPacketReader();
		p = r.getNextPacket();
		
		assertNotNull(p);
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		assertEquals(1234, p.getSid());
		assertEquals(0, p.getSequenceNumber());
		assertEquals(0, p.getData().length);
		
		assertNull(r.getNextPacket());
		
		
		// Now add a 2nd stream
		w = ogg.getPacketWriter(54321);
		
		// Will do nothing as no pages yet
		w.flush();
		
		// Add an empty packet
		p = new OggPacket(new byte[0]);
		w.bufferPacket(p);
		w.flush();
		
		// And a packet with something in it
		p = new OggPacket(new byte[] {22});
		w.bufferPacket(p);
		w.close();
		
		// Check again
		opened = new OggFile( new ByteArrayInputStream(baos.toByteArray()) );
		r = opened.getPacketReader();
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		assertEquals(1234, p.getSid());
		assertEquals(0, p.getSequenceNumber());
		assertEquals(0, p.getData().length);
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(54321, p.getSid());
		assertEquals(0, p.getSequenceNumber());
		assertEquals(0, p.getData().length);
		
		p = r.getNextPacket();
		assertNotNull(p);
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		assertEquals(54321, p.getSid());
		assertEquals(1, p.getSequenceNumber());
		assertEquals(1, p.getData().length);
		
		assertNull(r.getNextPacket());
	}
		
	
	public void testInterleaved() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OggFile ogg = new OggFile(baos);
		
		OggPacketWriter w1 = ogg.getPacketWriter(1234);
		OggPacketWriter w2 = ogg.getPacketWriter(4321);

		OggPacket p;
		
		// Add plus flush
		p = new OggPacket(new byte[] {1});
		w1.bufferPacket(p, true);
		
		p = new OggPacket(new byte[] {2});
		w2.bufferPacket(p, true);
		
		// Add to one, flush another
		p = new OggPacket(new byte[] {1,1});
		w1.bufferPacket(p);
		p = new OggPacket(new byte[] {1,2});
		w1.bufferPacket(p, false);
		
		p = new OggPacket(new byte[] {2,2});
		w2.bufferPacket(p);
		w2.close();
		
		// Close 1
		w1.close();
		
		
		// Check
		OggFile opened = new OggFile( new ByteArrayInputStream(baos.toByteArray()) );
		OggPacketReader r = opened.getPacketReader();
		
		p = r.getNextPacket();
		assertEquals(1234, p.getSid());
		assertEquals(0, p.getSequenceNumber());
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(1, p.getData().length);
		assertEquals(1, p.getData()[0]);
		
		p = r.getNextPacket();
		assertEquals(4321, p.getSid());
		assertEquals(0, p.getSequenceNumber());
		assertEquals(true, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(1, p.getData().length);
		assertEquals(2, p.getData()[0]);
		
		// Didn't flush w1, so next is on w2
		
		p = r.getNextPacket();
		assertEquals(4321, p.getSid());
		assertEquals(1, p.getSequenceNumber());
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		assertEquals(2, p.getData().length);
		assertEquals(2, p.getData()[0]);
		assertEquals(2, p.getData()[1]);
		
		// Now the w1 ones
		p = r.getNextPacket();
		assertEquals(1234, p.getSid());
		assertEquals(1, p.getSequenceNumber());
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(false, p.isEndOfStream());
		assertEquals(2, p.getData().length);
		assertEquals(1, p.getData()[0]);
		assertEquals(1, p.getData()[1]);
		
		p = r.getNextPacket();
		assertEquals(1234, p.getSid());
		assertEquals(1, p.getSequenceNumber());
		assertEquals(false, p.isBeginningOfStream());
		assertEquals(true, p.isEndOfStream());
		assertEquals(2, p.getData().length);
		assertEquals(1, p.getData()[0]);
		assertEquals(2, p.getData()[1]);
		
		assertEquals(null, r.getNextPacket());
	}
}
