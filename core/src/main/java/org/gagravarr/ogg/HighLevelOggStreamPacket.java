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
package org.gagravarr.ogg;


/**
 * A high level stream packet sat atop
 *  of an OggPacket
 */
public abstract class HighLevelOggStreamPacket {
	private OggPacket oggPacket;
	private byte[] data;
	
	protected HighLevelOggStreamPacket(OggPacket oggPacket) {
		this.oggPacket = oggPacket;
	}
	protected HighLevelOggStreamPacket() {
		this.oggPacket = null;
	}
	
	protected OggPacket getOggPacket() {
		return oggPacket;
	}
	
	public byte[] getData() {
		if(data != null) {
			return data;
		}
		if(oggPacket != null) {
			return oggPacket.getData();
		}
		return null;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public OggPacket write() {
		this.oggPacket = new OggPacket(getData());
		return this.oggPacket;
	}
	
	/**
	 * Populates the start of a new packet with the 
	 *  appropriate header
	 */
	protected abstract void populateStart(byte[] b, int type);
	/**
	 * How big is the header?
	 */
	protected abstract int getDataBeginsAt();
}
