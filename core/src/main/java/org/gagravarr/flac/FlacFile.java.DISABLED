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

/**
 * This wrapper lets you work with FLAC files,
 *  whether they're Ogg or Native framed.
 */
public abstract class FlacFile {
	protected FlacInfo info;
	protected FlacComments comment;
	
	/**
	 * Opens the given file for reading
	 */
	public static FlacFile open(File f) throws IOException, FileNotFoundException {
		this(new OggFile(new FileInputStream(f)));
	}
	/**
	 * Opens the given file for reading
	 */
	public static FlacFile open(OggFile ogg) throws IOException {
		return new FlacOggFile(ogg);
	}
	
	public abstract FlacAudioData getNextAudioPacket() throws IOException;
	
	/**
	 * Skips the audio data to the next packet with a granule
	 *  of at least the given granule position.
	 * Note that skipping backwards is not currently supported!
	 */
	public abstract void skipToGranule(long granulePosition) throws IOException;
	
	public FlacInfo getInfo() {
		return info;
	}
	public FlacComments getComment() {
		return comment;
	}

	
	/**
	 * In Reading mode, will close the underlying ogg/fac
	 *  file and free its resources.
	 * In Writing mode, will write out the Info and 
	 *  Comments objects, and then the audio data.
	 */
	public abstract void close() throws IOException;
}
