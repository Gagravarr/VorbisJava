package org.xiph.ogg;

/**
 * Represents a logical group of data.
 * RFC3533 suggests that these should usually be
 *  around 50-200 bytes long.
 */
public class OggPacket extends OggPacketData {
	private OggPage parent; // Last page if split
	private boolean bos;
	private boolean eos;
	
	/**
	 * Creates a new Ogg Packet based on data read
	 *  from within an Ogg Page.
	 */
	protected OggPacket(OggPage parent, byte[] data, boolean bos, boolean eos) {
		super(data);
		this.parent = parent;
		this.bos = bos;
		this.eos = eos;
	}
	/**
	 * Creates a new Ogg Packet filled with data to
	 *  be later written.
	 * The Sid, and begin/end flags will be available
	 *  after the packet has been flushed.
	 */
	public OggPacket(byte[] data) {
		super(data);
	}
	
	protected void setParent(OggPage parent) {
		this.parent = parent;
	}
	protected void setIsBOS() {
		this.bos = true;
	}
	protected void setIsEOS() {
		this.eos = true;
	}
	
	/**
	 * Returns the Stream ID (Sid) that
	 *  this packet belongs to.
	 */
	public int getSid() {
		return parent.getSid();
	}
	/**
	 * Returns the granule position of the page
	 *  that this packet belongs to. The meaning
	 *  of the granule depends on the codec.
	 */
	public int getGranulePosition() {
		return parent.getGranulePosition();
	}
	/**
	 * Returns the sequence number within the stream
	 *  of the page that this packet belongs to.
	 * You can use this to detect when pages have
	 *  been lost.
	 */
	public int getSequenceNumber() {
		return parent.getSequenceNumber();
	}
	
	/**
	 * Is this the first packet in the stream?
	 * If so, the data should hold the magic
	 *  information required to identify which
	 *  decoder will be needed.
	 */
	public boolean isBeginningOfStream() {
		return bos;
	}
	/**
	 * Is this the last packet in the stream?
	 */
	public boolean isEndOfStream() {
		return eos;
	}
}
