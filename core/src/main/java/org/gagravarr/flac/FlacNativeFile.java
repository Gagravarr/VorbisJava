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
import java.io.InputStream;

import org.gagravarr.ogg.IOUtils;

/**
 * This lets you work with FLAC files that
 *  are contained in a native FLAC Stream
 */
public class FlacNativeFile extends FlacFile {
   private InputStream input;
   
   /**
    * Opens the given file for reading
    */
   public FlacNativeFile(File f) throws IOException, FileNotFoundException {
      this(new FileInputStream(f));
   }
   
   /**
    * Opens the given FLAC file
    */
   public FlacNativeFile(InputStream inp) throws IOException {
      // Check the header
      byte[] header = new byte[4];
      IOUtils.readFully(inp, header);
      if(header[0] == (byte)'f' && header[1] == (byte)'L' &&
         header[2] == (byte)'a' && header[3] == (byte)'c') {
         // Good
      } else {
         throw new IllegalArgumentException("Not a FLAC file");
      }
      
      // Read the Metadata blocks
      // TODO
   }
   
	
	public FlacAudioFrame getNextAudioPacket() throws IOException {
	   // TODO
	   return null;
	}
	
	/**
	 * Skips the audio data to the next packet with a granule
	 *  of at least the given granule position.
	 * Note that skipping backwards is not currently supported!
	 */
	public void skipToGranule(long granulePosition) throws IOException {
      throw new RuntimeException("Not supported");
	}

	/**
	 * In Reading mode, will close the underlying ogg/flac
	 *  file and free its resources.
	 * In Writing mode, will write out the Info and 
	 *  Comments objects, and then the audio data.
	 */
	public void close() throws IOException {
	   if(input != null) {
	      input.close();
	      input = null;
	   } else {
	      throw new RuntimeException("Not supported");
	   }
	}
}
