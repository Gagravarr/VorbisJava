package org.xiph.vorbis;

import org.xiph.ogg.IOUtils;
import org.xiph.ogg.OggPacket;

/**
 * Parent of all Vorbis packets
 */
public abstract class VorbisPacket {
	private OggPacket oggPacket;
	private byte[] data;
	
	protected VorbisPacket(OggPacket oggPacket) {
		this.oggPacket = oggPacket;
	}
	protected VorbisPacket() {
		this.oggPacket = null;
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
	protected void populateStart(byte[] b, int type) {
		b[0] = IOUtils.fromInt(type);
		b[1] = (byte)'v';
		b[2] = (byte)'o';
		b[3] = (byte)'r';
		b[4] = (byte)'b';
		b[5] = (byte)'i';
		b[6] = (byte)'s';
	}
	
	/**
	 * Creates the appropriate {@link VorbisPacket}
	 *  instance based on the type.
	 */
	public static VorbisPacket create(OggPacket packet) {
		byte type = packet.getData()[0];
		
		// Ensure "vorbis" on the special types
		if(type == 1 || type == 3 || type == 5) {
			byte[] d = packet.getData();
			if(d[1] == (byte)'v' &&
				d[2] == (byte)'o' &&
				d[3] == (byte)'r' &&
				d[4] == (byte)'b' &&
				d[5] == (byte)'i' &&
				d[6] == (byte)'s') {
				switch(type) {
				case 1:
					return new VorbisInfo(packet);
				case 3:
					return new VorbisComments(packet);
				case 5:
					return new VorbisSetup(packet);
				}
			} else {
				throw new IllegalArgumentException("Magic string 'vorbis' not found for packet of type " + type);
			}
		}
		
		return new VorbisAudioData(packet);
	}
}
