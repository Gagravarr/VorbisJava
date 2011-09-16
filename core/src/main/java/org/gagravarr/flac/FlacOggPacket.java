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
import org.gagravarr.ogg.HighLevelStreamPacket;

/**
 * Parent of all Flac packets stored in Ogg
 */
public abstract class FlacOggPacket extends HighLevelStreamPacket {
   @Override
	protected void populateStart(byte[] b, int type) {
		b[0] = IOUtils.fromInt(type);
		b[1] = (byte)'F';
		b[2] = (byte)'L';
		b[3] = (byte)'A';
		b[4] = (byte)'C';
	}
	/**
	 * "#FLAC" then data
	 */
	protected int getDataBeginsAt() {
		return 5;
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
		byte type = packet.getData()[0];
		
		// Ensure "FLAC" on the special types
		if(type == 0x7f) {
			byte[] d = packet.getData();
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
