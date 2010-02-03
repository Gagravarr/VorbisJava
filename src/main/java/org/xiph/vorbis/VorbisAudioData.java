package org.xiph.vorbis;

import org.xiph.ogg.OggPacket;

/**
 * Raw, compressed audio data
 */
public class VorbisAudioData extends VorbisPacket {
	public VorbisAudioData(OggPacket pkt) {
		super(pkt);
	}
	public VorbisAudioData(byte[] data) {
		super();
		setData(data);
	}
}
