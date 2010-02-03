package org.xiph.vorbis;

import java.util.ArrayList;
import java.util.List;

import org.xiph.ogg.IOUtils;
import org.xiph.ogg.OggPacket;

/**
 * Holds encoder information and user specified tags
 */
public class VorbisComments extends VorbisPacket {
	private String vendor;
	private ArrayList<String> comments = new ArrayList<String>();
	
	public VorbisComments(OggPacket pkt) {
		super(pkt);
		
		byte[] d = pkt.getData();
		
		int vlen = (int)IOUtils.getInt4(d, 7);
		vendor = IOUtils.getUTF8(d, 11, vlen);
		
		int offset = 11 + vlen;
		int numComments = (int)IOUtils.getInt4(d, offset);
		comments.ensureCapacity(numComments);
		offset += 4;
		
		for(int i=0; i<numComments; i++) {
			int len = (int)IOUtils.getInt4(d, offset);
			offset += 4;
			String c = IOUtils.getUTF8(d, offset, len);
			offset += len;
			comments.add(c);
		}
		
		byte framingBit = d[offset];
		if(framingBit == 0) {
			throw new IllegalArgumentException("Framing bit not set, invalid");
		}
	}
	
	public VorbisComments() {
		super();
	}
	
	public List<String> getAllComments() {
		return comments;
	}

	@Override
	public OggPacket write() {
		// Serialise the comments
		//populateStart(b, 3);
		
		// Now write
		return super.write();
	}
}
