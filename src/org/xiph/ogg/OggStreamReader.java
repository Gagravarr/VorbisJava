package org.xiph.ogg;

/**
 * Implement this interface to be given the
 *  data from the stream of an Ogg file
 *  that you indicated interest in using
 *  a {@link OggStreamListener} 
 */
public interface OggStreamReader {
	public void processPacket(OggPacket packet);
}
