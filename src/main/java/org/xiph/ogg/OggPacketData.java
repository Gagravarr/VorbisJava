package org.xiph.ogg;

/**
 * The data part of an {@link OggPacket}.
 * RFC3533 suggests that these should usually be
 *  around 50-200 bytes long.
 * Normally wrapped as a full {@link OggPacket},
 *  but may be used internally when a
 *  Packet is split across more than one
 *  {@link OggPage}.
 */
public class OggPacketData {
	private byte[] data;
	
	protected OggPacketData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Returns the data that makes up the packet.
	 */
	public byte[] getData() {
		return data;
	}
}
