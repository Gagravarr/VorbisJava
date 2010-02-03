package org.xiph.ogg;

import java.io.IOException;
import java.util.ArrayList;

public class OggPacketWriter {
	private boolean closed = false;
	private boolean doneFirstPacket = false;
	private OggFile file;
	private int sid;
	private int sequenceNumber;
	
	private ArrayList<OggPage> buffer =
		new ArrayList<OggPage>();
	
	protected OggPacketWriter(OggFile parentFile, int sid) {
		this.file = parentFile;
		this.sid = sid;
		
		this.sequenceNumber = 0;
	}
	
	/**
	 * Sets the current granule position.
	 */
	public void setGranulePosition(long position) {
		OggPage page = getCurrentPage(false);
		page.setGranulePosition(position);
	}
	
	private OggPage getCurrentPage(boolean forceNew) {
		if(buffer.size() == 0 || forceNew) {
			OggPage page = new OggPage(sid, sequenceNumber++); 
			buffer.add( page );
			return page;
		}
		return buffer.get( buffer.size()-1 );
	}
	
	/**
	 * Buffers the given packet up ready for
	 *  writing to the stream, but doesn't
	 *  write it to disk yet.
	 */
	public void bufferPacket(OggPacket packet) {
		if(closed) {
			throw new IllegalStateException("Can't buffer packets on a closed stream!");
		}
		if(! doneFirstPacket) {
			packet.setIsBOS();
			doneFirstPacket = true;
		}
		
		int size = packet.getData().length;
		boolean emptyPacket = (size==0);

		// Add to pages in turn
		OggPage page = getCurrentPage(false);
		int pos = 0;
		while( pos < size || emptyPacket) {
			int added = page.addPacket(packet, pos);
			pos += added;
			if(added < size) {
				page = getCurrentPage(true);
				page.setIsContinuation();
			}
			emptyPacket = false;
		}
		packet.setParent(page);
	}
	
	/**
	 * Buffers the given packet up ready for
	 *  writing to the file, and then writes
	 *  it to the stream if indicated.
	 */
	public void bufferPacket(OggPacket packet, boolean flush) throws IOException {
		bufferPacket(packet);
		if(flush) {
			flush();
		}
	}
	
	/**
	 * Returns the number of bytes (excluding headers)
	 *  currently waiting to be written to disk.
	 * RFC 3533 suggests that pages should normally 
	 *  be in the 4-8kb range.
	 * If this size exceeds just shy of 64kb, then
	 *  multiple pages will be needed in the underlying
	 *  stream.
	 */
	public int getSizePendingFlush() {
		int size = 0;
		for(OggPage p : buffer) {
			size += p.getDataSize();
		}
		return size;
	}
	
	/**
	 * Writes all pending packets to the stream,
	 *  splitting across pages as needed.
	 */
	public void flush() throws IOException {
		if(closed) {
			throw new IllegalStateException("Can't flush packets on a closed stream!");
		}
		
		// Write in one go
		OggPage[] pages = buffer.toArray(new OggPage[buffer.size()]); 
		file.writePages(pages);
			
		// Get ready for next time!
		buffer.clear();
	}
	
	/**
	 * Writes all pending packets to the stream,
	 *  with the last one containing the End Of Stream
	 *  Flag, and then closes down.
	 */
	public void close() throws IOException {
		if(buffer.size() > 0) {
			buffer.get( buffer.size()-1 ).setIsEOS();
		} else {
			OggPacket p = new OggPacket(new byte[0]);
			p.setIsEOS();
			bufferPacket(p);
		}
		flush();
		
		closed = true;
	}
}
