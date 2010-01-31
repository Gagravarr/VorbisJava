package org.xiph.ogg;

/**
 * Implement this if doing Event based processing
 *  to know when new streams are found.
 */
public interface OggStreamListener {
	/**
	 * Called every time a new Stream is encountered.
	 * Should return a (possibly empty) array of
	 *  {@link OggStreamReader} instances which should
	 *  be passed all packets for the stream;
	 */
	public OggStreamReader[] processNewStream(int sid, byte[] magicData);
	
	/**
	 * Called after the last packet in Stream has been
	 *  processed, in case you want to do any tidying up.
	 */
	public void processStreamEnd(int sid);
}
