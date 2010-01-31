package org.xiph.ogg;

/**
 * Represents a logical group of data.
 * RFC3533 suggests that these should usually be
 *  around 50-200 bytes long.
 */
public class OggPacket {
	private OggPage parent;
	private byte[] data;
	private boolean bos;
	private boolean eos;
	
	/**
	 * Creates a new Ogg Packet based on data read
	 *  from within an Ogg Page.
	 */
	protected OggPacket(int sid, byte[] data, boolean bos, boolean eos) {
		this.sid = sid;
		this.data = data;
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
		this.data = data;
		this.sid = -1;
	}
	
	protected void setSid(int sid) {
		this.sid = sid;
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
		return sid;
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
	/**
	 * Returns the data that makes up the packet.
	 */
	public byte[] getData() {
		return data;
	}
}
