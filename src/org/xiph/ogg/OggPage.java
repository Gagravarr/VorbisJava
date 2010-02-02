package org.xiph.ogg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class OggPage {
	private int sid;
	private int seqNum;
	private long checksum;
	private long granulePosition;
	
	private boolean isBOS;
	private boolean isEOS;
	private boolean isContinue;
	
	private int numLVs = 0;
	private byte[] lvs = new byte[255];
	private byte[] data;
	private ByteArrayOutputStream tmpData;
	
	protected OggPage(int sid, int seqNum) {
		this.sid = sid;
		this.seqNum = seqNum;
		this.tmpData = new ByteArrayOutputStream();
	}
	/**
	 * InputStream should be positioned *just after*
	 *  the OggS capture pattern.
	 */
	protected OggPage(InputStream inp) throws IOException {
		int version = inp.read();
		if(version != 0) {
			throw new IllegalArgumentException("Found Ogg page in format " + version + " but we only support version 0");
		}
		
		int flags = inp.read();
		if((flags & 0x01) == 0x01) {
			isContinue = true;
		}
		if((flags & 0x02) == 0x02) {
			isBOS = true;
		}
		if((flags & 0x04) == 0x04) {
			isEOS = true;
		}

		granulePosition = getInt(
				inp.read(), inp.read(), inp.read(), inp.read(),
				inp.read(), inp.read(), inp.read(), inp.read()
		);
		sid = (int)getInt(
				inp.read(), inp.read(), inp.read(), inp.read()
		);
		seqNum = (int)getInt(
				inp.read(), inp.read(), inp.read(), inp.read()
		);
		checksum = getInt(
				inp.read(), inp.read(), inp.read(), inp.read()
		);
		
		numLVs = inp.read();
		lvs = new byte[numLVs];
		readFully(inp, lvs);
		
		data = new byte[ getDataSize() ];
		readFully(inp, data);
	}
	
	/**
	 * Adds as much of the packet's data as
	 *  we can do.
	 */
	protected int addPacket(OggPacket packet, int offset) {
		if(packet.isBeginningOfStream()) {
			isBOS = true;
		}
		if(packet.isEndOfStream()) {
			isEOS = true;
		}
		
		// Add on in 255 byte chunks
		int size = packet.getData().length;
		for(int i = numLVs; i< 255; i++) {
			int remains = size - offset;
			
			int toAdd = 255;
			if(remains < 255) {
				toAdd = remains;
			}
			lvs[i] = fromInt(toAdd);
			tmpData.write(packet.getData(), offset, toAdd);
			
			numLVs++;
			offset += toAdd;
			if(toAdd < 255) {
				break;
			}
		}
		
		return offset;
	}
	
	/**
	 * Does this Page have space for the given
	 *  number of bytes?
	 */
	protected boolean hasSpaceFor(int bytes) {
		// Do we have enough lvs spare?
		// (Each LV holds up to 255 bytes, and we're
		//  not allowed more than 255 of them)
		int reqLVs = (int)Math.ceil(bytes / 255.0);
		
		if(numLVs + reqLVs > 255) {
			return false;
		}
		return true;
	}
	
	/**
	 * How big is the page, including headers?
	 */
	public int getPageSize() {
		// Header is 27 bytes + number of headers
		int size = 27 + numLVs;
		// Data size is given by lvs
		size += getDataSize();
		return size;
	}
	/**
	 * How big is the page, excluding headers?
	 */
	public int getDataSize() {
		// Data size is given by lvs
		int size = 0;
		for(int i=0; i<numLVs; i++) {
			size += toInt(lvs[i]);
		}
		return size;
	}
	
	
	public int getSid() {
		return sid;
	}
	public int getSequenceNumber () {
		return seqNum;
	}
	public long getGranulePosition() {
		return granulePosition;
	}
	public byte[] getData() {
		if(tmpData != null) {
			if(data == null || tmpData.size() != data.length) {
				data = tmpData.toByteArray();
			}
		}
		return data;
	}
	
	protected void setGranulePosition(int position) {
		this.granulePosition = position;
	}

	/**
	 * Is there a subsequent page containing the
	 *  remainder of the packets?
	 */
	public boolean hasContinuation() {
		// Will only continuation if all
		//  255 LVs are used, and the last
		//  one is fully used
		if(numLVs < 255) {
			return false;
		}
		if(toInt( lvs[255] ) == 255) {
			return true;
		}
		return false;
	}
	/**
	 * Is this carrying on the packets from
	 *  a previous page?
	 */
	public boolean isContinuation() {
		return isContinue;
	}
	protected void setIsContinuation() {
		isContinue = true;
	}
	
	/**
	 * This should only ever be called by
	 *  {@link OggPacketWriter#close()} !
	 */
	protected void setIsEOS() {
		isEOS = true;
	}
	
	protected int toInt(byte b) {
		if(b < 0)
			return b+256;
		return b;
	}
	protected byte fromInt(int i) {
		if(i > 256) {
			throw new IllegalArgumentException("Number " + i + " too big");
		}
		if(i > 127) {
			return (byte)(i-256);
		}
		return (byte)i;
	}
	protected long getInt(int i1, int i2, int i3, int i4) {
		// TODO
	}
	protected long getInt(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
		// TODO
	}
	
	public void writeHeader(OutputStream out) throws IOException {
		out.write((int)'O');
		out.write((int)'g');
		out.write((int)'g');
		out.write((int)'S');
		
		// TODO
		
		out.write(numLVs);
		out.write(lvs, 0, numLVs);
	}
	
	public OggPacketIterator getPacketIterator() {
		return new OggPacketIterator(null);
	}
	public OggPacketIterator getPacketIterator(OggPacketData previousPart) {
		return new OggPacketIterator(previousPart);
	}
	/**
	 * Returns a full {@link OggPacket} if it can, otherwise
	 *  just the {@link OggPacketData} if the rest of the
	 *  packet is in another {@link OggPage}
	 */
	protected class OggPacketIterator implements Iterator<OggPacketData> {
		private OggPacketData prevPart;
		private int currentLV = 0;
		private int currentOffset = 0;
		
		private OggPacketIterator(OggPacketData previousPart) {
			this.prevPart = previousPart;
		}

		public boolean hasNext() {
			if(currentLV < numLVs) {
				return true;
			}
			return false;
		}

		public OggPacketData next() {
			boolean continues = false;
			int packetLVs = 0;
			int packetSize = 0;
			
			// How much data to we have?
			for(int i=currentLV; i<= numLVs; i++) {
				int size = toInt( lvs[i] );
				packetSize += size;
				packetLVs++;
				
				if(size < 255) {
					break;
				}
				if(i == 255) {
					continues = true;
				}
			}
			
			// Get the data
			byte[] pd = new byte[packetSize];
			for(int i=currentLV; i<(currentLV + packetLVs); i++) {
				int size = toInt( lvs[i] );
				int offset = i*255;
				System.arraycopy(data, currentOffset+offset, pd, offset, size);
			}
			// Tack on anything spare from last time too
			if(prevPart != null) {
				int prevSize = prevPart.getData().length;
				byte[] fpd = new byte[prevSize+pd.length];
				System.arraycopy(prevPart.getData(), 0, fpd, 0, prevSize);
				System.arraycopy(pd, 0, fpd, prevSize, pd.length);
				
				prevPart = null;
				pd = fpd;
			}
			
			// Create
			OggPacketData packet;
			if(continues) {
				packet = new OggPacketData(pd);
			} else {
				boolean packetBOS = false;
				boolean packetEOS = false;
				if(isBOS && currentLV == 0) {
					packetBOS = true;
				}
				if(isEOS && (currentLV+packetLVs) == numLVs) {
					packetEOS = true;
				}
				
				packet = new OggPacket(OggPage.this, pd, packetBOS, packetEOS);
			}
			
			// Wind on
			currentLV += packetLVs;
			currentOffset += packetSize;
			
			// Done!
			return packet;
		}

		public void remove() {
			throw new IllegalStateException("Remove not supported");
		}
	}
}
