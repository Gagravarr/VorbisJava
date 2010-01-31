package org.xiph.ogg;

import java.util.Iterator;

public class OggPage {
	private int sid;
	private int seqNum;
	private int granulePosition;
	
	protected OggPage(int sid, int seqNum) {
		this.sid = sid;
		this.seqNum = seqNum;
	}
	
	/**
	 * Does this Page have space for the given
	 *  number of bytes?
	 */
	protected boolean hasSpaceFor(int bytes) {
		
	}
	/**
	 * How big is the page, including headers?
	 */
	public int getPageSize() {
		
	}
	/**
	 * How big is the page, excluding headers?
	 */
	public int getDataSize() {
		
	}
	
	
	public int getSid() {
		return sid;
	}
	public int getGranulePosition() {
		return granulePosition;
	}
	public int getSequenceNumber () {
		return seqNum;
	}
	
	protected void setGranulePosition(int position) {
		this.granulePosition = position;
	}

	/**
	 * Is there a subsequent page containing the
	 *  remainder of the packets?
	 */
	public boolean hasContinuation() {
		
	}
	/**
	 * Is this carrying on the packets from
	 *  a previous page?
	 */
	public boolean isContinuation() {
		
	}
	
	
	protected class OggPacketIterator implements Iterator<OggPacket> {
		
	}
}
