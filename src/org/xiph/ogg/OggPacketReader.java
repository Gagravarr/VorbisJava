package org.xiph.ogg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class OggPacketReader {
	private InputStream inp;
	private Iterator<OggPacket> it;
	
	public OggPacketReader(InputStream inp) {
		this.inp = inp;
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
		if(it != null && it.hasNext()) {
			return it.next();
		}

		// Find the next packet
		int searched = 0;
		int pos = -1;
		boolean found = false;
		int r;
		while(searched < 65536 && !found) {
			r = inp.read();
			switch(pos) {
			case -1:
				if(r == (int)'O') {
					pos = 0;
				}
				break;
			case 0:
				if(r == (int)'g') {
					pos = 1;
				} else {
					pos = -1;
				}
				break;
			case 1:
				if(r == (int)'g') {
					pos = 2;
				} else {
					pos = -1;
				}
				break;
			case 2:
				if(r == (int)'S') {
					found = true;
				} else {
					pos = -1;
				}
				break;
			}
			
			if(!found) {
				searched++;
			}
		}
		
		if(!found) {
			throw new IOException("Next ogg packet header not found after searching " + searched + " bytes");
		}
		
		if(searched > 0) {
			System.err.println("Warning - had to skip " + searched + " bytes of junk data before finding the next packet header");
		}
		
		// Create the page and return its first packet
		OggPage page = new OggPage(inp);
		it = page.getPacketIterator();
		return it.next();
	}
}
