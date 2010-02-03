package org.xiph.ogg;

import java.io.IOException;
import java.io.InputStream;

import org.xiph.ogg.OggPage.OggPacketIterator;

import junit.framework.TestCase;

/**
 * Test that we can open a file and read packets from it
 */
public class TestBasicRead extends TestCase {
	private InputStream getTestFile() throws IOException {
		return this.getClass().getResourceAsStream("/testVORBIS.ogg");
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
		
		assertEquals(0xbb, p.getData().length);
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
		assertEquals(0xbb, p.getData().length);
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
}
