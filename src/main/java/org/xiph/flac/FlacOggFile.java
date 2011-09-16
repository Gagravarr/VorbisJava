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
package org.xiph.flac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.xiph.ogg.OggFile;
import org.xiph.ogg.OggPacket;
import org.xiph.ogg.OggPacketReader;
import org.xiph.vorbis.VorbisAudioData;
import org.xiph.vorbis.VorbisComments;
import org.xiph.vorbis.VorbisInfo;
import org.xiph.vorbis.VorbisPacket;
import org.xiph.vorbis.VorbisSetup;

/**
 * This lets you work with FLAC files that
 *  are contained in an Ogg Stream
 */
public class FlacOggFile extends FlacFile {
	private OggFile ogg;
	private OggPacketReader r;
	private int sid = -1;
	
	/**
	 * Opens the given file for reading
	 */
	public FlacOggFile(File f) throws IOException, FileNotFoundException {
		this(new OggFile(new FileInputStream(f)));
	}
	/**
	 * Opens the given file for reading
	 */
	public FlacOggFile(OggFile ogg) throws IOException {
		this(ogg.getPacketReader());
		this.ogg = ogg;
	}
	/**
	 * Loads a Vorbis File from the given packet reader.
	 */
	public FlacOggFile(OggPacketReader r) throws IOException {	
		this.r = r;
		
		OggPacket p = null;
		while( (p = r.getNextPacket()) != null ) {
			if(p.isBeginningOfStream() && p.getData().length > 10) {
				try {
					VorbisPacket.create(p);
					sid = p.getSid();
					break;
				} catch(IllegalArgumentException e) {
					// Not a vorbis stream, don't worry
				}
			}
		}
		
		// First three packets are required to be info, comments, setup
		info = (VorbisInfo)VorbisPacket.create( p );
		comment = (VorbisComments)VorbisPacket.create( r.getNextPacketWithSid(sid) );
		setup = (VorbisSetup)VorbisPacket.create( r.getNextPacketWithSid(sid) );
		
		// Everything else should be audio data
	}
	
	/**
	 * Opens for writing.
	 */
	public FlacOggFile(OutputStream out) {
		this(out, new VorbisInfo(), new VorbisComments(), new VorbisSetup());   
	}
	/**
	 * Opens for writing, based on the settings
	 *  from a pre-read file. The Steam ID (SID) is
	 *  automatically allocated for you.
	 */
	public FlacFile(OutputStream out, VorbisInfo info, VorbisComments comments, VorbisSetup setup) {
		this(out, -1, info, comments, setup);
	}
	/**
	 * Opens for writing, based on the settings
	 *  from a pre-read file, with a specific
	 *  Steam ID (SID). You should only set the SID
	 *  when copying one file to another!
	 */
	public FlacFile(OutputStream out, int sid, VorbisInfo info, VorbisComments comments, VorbisSetup setup) {
		ogg = new OggFile(out);
		
		if(sid > 0) {
			w = ogg.getPacketWriter(sid);
			this.sid = sid;
		} else {
			w = ogg.getPacketWriter();
			this.sid = w.getSid();
		}
		
		writtenPackets = new ArrayList<VorbisAudioData>();
		
		this.info = info;
		this.comment = comments;
		this.setup = setup;
	}
	
	public FlacAudioData getNextAudioPacket() throws IOException {
		OggPacket p = null;
		VorbisPacket vp = null;
		while( (p = r.getNextPacketWithSid(sid)) != null ) {
			vp = VorbisPacket.create(p);
			if(vp instanceof VorbisAudioData) {
				return (VorbisAudioData)vp;
			} else {
				System.err.println("Skipping non audio packet " + vp + " mid audio stream");
			}
		}
		return null;
	}
	
	/**
	 * Skips the audio data to the next packet with a granule
	 *  of at least the given granule position.
	 * Note that skipping backwards is not currently supported!
	 */
	public void skipToGranule(long granulePosition) throws IOException {
		r.skipToGranulePosition(sid, granulePosition);
	}
	
	/**
	 * Returns the Ogg Stream ID
	 */
	public int getSid() {
		return sid;
	}

	public VorbisInfo getInfo() {
		return info;
	}
	public VorbisComments getComment() {
		return comment;
	}
	public VorbisSetup getSetup() {
		return setup;
	}
	
	
	/**
	 * Buffers the given audio ready for writing
	 *  out. Data won't be written out yet, you
	 *  need to call {@link #close()} to do that,
	 *  because we assume you'll still be populating
	 *  the Info/Comment/Setup objects
	 */
	public void writeAudioData(VorbisAudioData data) {
		writtenPackets.add(data);
	}
	
	/**
	 * In Reading mode, will close the underlying ogg
	 *  file and free its resources.
	 * In Writing mode, will write out the Info, Comments
	 *  and Setup objects, and then the audio data.
	 */
	public void close() throws IOException {
		if(r != null) {
			r = null;
			ogg.close();
			ogg = null;
		}
		if(w != null) {
			w.bufferPacket(info.write(), true);
			w.bufferPacket(comment.write(), false);
			w.bufferPacket(setup.write(), true);
			
			long lastGranule = 0;
			for(VorbisAudioData vd : writtenPackets) {
				// Update the granule position as we go
				if(vd.getGranulePosition() >= 0 &&
							lastGranule != vd.getGranulePosition()) {
					w.flush();
					lastGranule = vd.getGranulePosition();
					w.setGranulePosition(lastGranule);
				}
				
				// Write the data, flushing if needed
				w.bufferPacket(vd.write());
				if(w.getSizePendingFlush() > 16384) {
					w.flush();
				}
			}
			
			w.close();
			w = null;
			ogg.close();
			ogg = null;
		}
	}
	
	/**
	 * Returns the underlying Ogg File instance
	 * @return
	 */
	public OggFile getOggFile() {
		return ogg;
	}
}
