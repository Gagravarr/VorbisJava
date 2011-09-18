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


/**
 * This comes before the audio data.
 * Made up of a series of:
 *  1 byte type
 *  3 byte length
 *  <data>
 */
public class FlacMetadataBlock extends FlacFrame {
	public static final int STREAMINFO = 0;
	public static final int PADDING = 1;
	public static final int APPLICATION = 2;
	public static final int SEEKTABLE = 3;
	public static final int VORBIS_COMMENT = 4;
	public static final int CUESHEET = 5;
	public static final int PICTURE = 6;
	// 7-126 : reserved
	// 127 : invalid, to avoid confusion with a frame sync code
	
	protected FlacMetadataBlock(byte type, int length, byte[] data) {
		this.type = type;
		this.length = length;
		this.data = data;
	}
	
	private byte type;
	private int length;
	private byte[] data;
	
	public int getType() {
		return type & 0x7f;
	}
	public int getLength() {
		return length;
	}
	public byte[] getData() {
		return data;
	}
	public boolean isLastMetadataBlock() {
		return (type < 0);
	}
}
