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
 * The Stream Info metadata block holds useful
 *  information on the audio data of the file
 */
public class FlacInfo {
	/**
	 * <16> The minimum block size (in samples) used in the stream. 
	 */
	private int minimumBlockSize;
	/**
	 * <16> The maximum block size (in samples) used in the stream. (Minimum blocksize == maximum blocksize) implies a fixed-blocksize stream.
	 */
	private int maximumBlockSize;
	/**
	 * <24> The minimum frame size (in bytes) used in the stream. May be 0 to imply the value is not known.
	 */
	private int minimumFrameSize;
	/**
	 * <24> The maximum frame size (in bytes) used in the stream. May be 0 to imply the value is not known.
	 */
	private int maximumFrameSize;
	/**
	 * <20> Sample rate in Hz. Though 20 bits are available, the maximum sample rate is limited by the structure of frame headers to 655350Hz. Also, a value of 0 is invalid.
	 */
	private int sampleRate;
	/**
	 * <3> (number of channels)-1. FLAC supports from 1 to 8 channels
	 */
	private int numChannels;
	/**
	 * <5> (bits per sample)-1. FLAC supports from 4 to 32 bits per sample. Currently the reference encoder and decoders only support up to 24 bits per sample.
	 */
	private int bitsPerSample;
	/**
	 * <36> Total samples in stream. 'Samples' means inter-channel sample, i.e. one second of 44.1Khz audio will have 44100 samples regardless of the number of channels. A value of zero here means the number of total samples is unknown.
	 */
	private long numberOfSamples;
	/* <128> MD5 signature of the unencoded audio data. */
	
	
}
