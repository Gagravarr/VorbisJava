package org.xiph.vorbis;

import org.xiph.ogg.OggPacket;

/**
 * Includes extensive CODEC setup information as well as the 
 *  complete VQ and Huffman codebooks needed for decode
 */
public class VorbisSetup extends VorbisPacket {
	public VorbisSetup(OggPacket pkt) {
		super(pkt);
	}
}
