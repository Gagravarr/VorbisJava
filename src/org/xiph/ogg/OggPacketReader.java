package org.xiph.ogg;

import java.io.IOException;
import java.io.InputStream;

public class OggPacketReader {
	private InputStream inp;
	private byte[] buffer;
	private int length = 0;
	
	public OggPacketReader(InputStream inp) {
		this.inp = inp;
		this.buffer = new byte[65307];
		this.length = 0;
	}

	/**
	 * Returns the next packet in the file, or
	 *  null if no more packets remain.
	 * Call {@link OggPacket#isBeginningOfStream()}
	 *  to detect if it is the first packet in the
	 *  stream or not, and use
	 *  {@link OggPacket#getSid()} to track which
	 *  stream it belongs to.
	 */
	public OggPacket getNextPacket() throws IOException {
		
	}
}
