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
package org.gagravarr.flac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggPacketWriter;
import org.gagravarr.vorbis.VorbisPacket;

/**
 * This lets you work with FLAC files that
 *  are contained in an Ogg Stream
 */
public class FlacOggFile extends FlacFile {
	private OggFile ogg;
	private OggPacketReader r;
   private OggPacketWriter w;
	private int sid = -1;
	
	private FlacFirstOggPacket firstPacket;
   private List<FlacAudioFrame> writtenAudio;
   
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
			   if(FlacFirstOggPacket.isFlacStream(p)) {
			      sid = p.getSid();
			      break;
			   }
			}
		}

		// First packet is special
		firstPacket = new FlacFirstOggPacket(p);
		
		// Next must be the Tags (Comments)
		
		// Then continue until the last metadata
		
		// Everything else should be audio data
	}
	
	/**
	 * Opens for writing.
	 */
	public FlacOggFile(OutputStream out) {
		this(out, new FlacInfo(), new FlacTags());   
	}
	/**
	 * Opens for writing, based on the settings
	 *  from a pre-read file. The Steam ID (SID) is
	 *  automatically allocated for you.
	 */
	public FlacOggFile(OutputStream out, FlacInfo info, FlacTags tags) {
		this(out, -1, info, tags);
	}
	/**
	 * Opens for writing, based on the settings
	 *  from a pre-read file, with a specific
	 *  Steam ID (SID). You should only set the SID
	 *  when copying one file to another!
	 */
	public FlacOggFile(OutputStream out, int sid, FlacInfo info, FlacTags tags) {
		ogg = new OggFile(out);
		
		if(sid > 0) {
			w = ogg.getPacketWriter(sid);
			this.sid = sid;
		} else {
			w = ogg.getPacketWriter();
			this.sid = w.getSid();
		}
		
		writtenAudio = new ArrayList<FlacAudioFrame>();
		
		this.firstPacket = new FlacFirstOggPacket(info);
		this.info = info;
		this.tags = tags;
	}
	
	public FlacAudioFrame getNextAudioPacket() throws IOException {
		OggPacket p = null;
		VorbisPacket vp = null;
		while( (p = r.getNextPacketWithSid(sid)) != null ) {
		   //return new FlacAudioFrame(p.getData()); // TODO
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

	/**
	 * Buffers the given audio ready for writing
	 *  out. Data won't be written out yet, you
	 *  need to call {@link #close()} to do that,
	 *  because we assume you'll still be populating
	 *  the Info/Comment/Setup objects
	 */
	public void writeAudioData(FlacAudioFrame data) {
		writtenAudio.add(data);
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
			w.bufferPacket(firstPacket.write(), true);
			w.bufferPacket(tags.write(), false);
			// TODO Write the others
			//w.bufferPacket(setup.write(), true);
			
			long lastGranule = 0;
			for(FlacAudioFrame fa : writtenAudio) {
				// Update the granule position as we go
			   // TODO Track this
//				if(vd.getGranulePosition() >= 0 &&
//							lastGranule != vd.getGranulePosition()) {
//					w.flush();
//					lastGranule = vd.getGranulePosition();
//					w.setGranulePosition(lastGranule);
//				}
				
				// Write the data, flushing if needed
//				w.bufferPacket(new OggPacket(fa.getData())); // TODO
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
