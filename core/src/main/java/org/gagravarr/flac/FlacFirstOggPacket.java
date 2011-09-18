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

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.HighLevelOggStreamPacket;

/**
 * The first Flac packet stored in an Ogg stream is 
 *  special. This holds both the stream information,
 *  and the {@link FlacFrame}
 */
public abstract class FlacFirstOggPacket extends HighLevelOggStreamPacket {
   private int majorVersion;
   private int minorVersion;
   private int numberOfHeaderBlocks;
   
   public FlacFirstOggPacket() {
      super();
      majorVersion = 1;
      minorVersion = 0;
      numberOfHeaderBlocks = 0;
   }

   public FlacFirstOggPacket(OggPacket oggPacket) {
      super(oggPacket);
      
      // Extract the info
      byte[] data = getData();
      // 0-4 = FLAC
      majorVersion = IOUtils.toInt(data[4]);
      minorVersion = IOUtils.toInt(data[5]);
      numberOfHeaderBlocks = IOUtils.getInt2(data, 6);
   }

   /**
    * Returns the Major Version number
    */
	public int getMajorVersion() {
      return majorVersion;
   }

   public void setMajorVersion(int majorVersion) {
      if(majorVersion > 255) {
         throw new IllegalArgumentException("Version numbers must be in the range 0-255");
      }
      this.majorVersion = majorVersion;
   }

   /**
    * Returns the Minor Version number. Decoders should be able to
    *  handle anything at a given major number, no matter the minor one 
    */
   public int getMinorVersion() {
      return minorVersion;
   }

   public void setMinorVersion(int minorVersion) {
      if(minorVersion > 255) {
         throw new IllegalArgumentException("Version numbers must be in the range 0-255");
      }
      this.minorVersion = minorVersion;
   }

   /**
    * Gets the number of header blocks, excluding this one, or
    *  zero if not known
    */
   public int getNumberOfHeaderBlocks() {
      return numberOfHeaderBlocks;
   }

   public void setNumberOfHeaderBlocks(int numberOfHeaderBlocks) {
      this.numberOfHeaderBlocks = numberOfHeaderBlocks;
   }

   /**
	 * Does this packet (the first in the stream) contain
	 *  the magic string indicating that it's a FLAC
	 *  one?
	 */
	public static boolean isFlacStream(OggPacket firstPacket) {
		if(! firstPacket.isBeginningOfStream()) {
			return false;
		}
		return isFlacSpecial(firstPacket);
	}
	
	private static boolean isFlacSpecial(OggPacket packet) {
      byte[] d = packet.getData();
		byte type = d[0];
		
		// Ensure 0x7f then "FLAC"
		if(type == 0x7f) {
			if(d[1] == (byte)'F' &&
				d[2] == (byte)'L' &&
				d[3] == (byte)'A' &&
				d[4] == (byte)'C') {
				
				return true;
			}
		}
		return false;
	}
}
