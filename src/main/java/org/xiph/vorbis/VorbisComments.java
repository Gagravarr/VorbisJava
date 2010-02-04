/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xiph.vorbis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
		vendor = "Xiph.org Java Vorbis Tools 20100203";
	}
	
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	
	public List<String> getAllComments() {
		return comments;
	}

	@Override
	public OggPacket write() {
		// Serialise the comments
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(new byte[7]);
			
			IOUtils.writeInt4(baos, vendor.length());
			IOUtils.writeUTF8(baos, vendor);
			
			IOUtils.writeInt4(baos, comments.size());
			for(String comment : comments) {
				IOUtils.writeInt4(baos, comment.length());
				IOUtils.writeUTF8(baos, comment);
			}
			baos.write(1);
		} catch(IOException e) {
			// Should never happen!
			throw new RuntimeException(e);
		}
		
		// Now do the header bit
		byte[] b = baos.toByteArray();
		populateStart(b, 3);
		setData(b);
		
		// Now write
		return super.write();
	}
}
