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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.gagravarr.ogg.IOUtils;

/**
 * Raw, compressed audio data.
 */
public class FlacAudioFrame extends FlacFrame {
   /**
    * Fixed or Variable block size?
    * Fixed = frame header encodes the frame number
    * Variable = frame header encodes the sample number
    */
   private boolean blockSizeVariable;

   private int blockSizeRaw;
   private int blockSize;
   private int sampleRateRaw;
   private int sampleRate;

   private byte[] subframeData;

   public FlacAudioFrame(byte[] data) throws IOException {
       this(new ByteArrayInputStream(data));
   }

   /**
    * Creates the frame from the stream, with header sync checking
    */
   public FlacAudioFrame(InputStream stream) throws IOException {
       this(getAndCheckFirstTwo(stream), stream);
   }
   /**
    * Creates the frame from the pre-read 2 bytes and stream, with header sync checking
    */
   public FlacAudioFrame(int byte1, int byte2, InputStream stream) throws IOException {
       this(getAndCheckFirstTwo(byte1, byte2), stream);
   }
   /**
    * Creates the frame from the pre-read 2 bytes and stream, no sync checks
    */
   public FlacAudioFrame(int first2, InputStream stream) throws IOException {
       // First 14 bits are the sync, 15 is reserved, 16 is block size
       blockSizeVariable = ((first2 & 1) == 1);

       // Block Size + Sample Rate
       int bsSr = stream.read();
       blockSizeRaw = (bsSr >> 4);
       sampleRate = (bsSr & 15);

       // Channel Assignment + Sample Size + Res
       int caSs = stream.read();
       // TODO Decode

       // coded number - TODO

       // ext block size or sample rate - TODO

       // Header CRC, not checked
       stream.read();

       // One sub-frame per channel
       // TODO

       // Footer CRC, not checked
       stream.read();
       stream.read();
   }

   private static int getAndCheckFirstTwo(InputStream stream) throws IOException {
       int byte1 = stream.read();
       int byte2 = stream.read();
       return getAndCheckFirstTwo(byte1, byte2);
   }
   private static int getAndCheckFirstTwo(int byte1, int byte2) throws IOException {
       int first2 = IOUtils.getIntBE(byte1, byte2);
       if (! isFrameHeaderStart(first2)) {
           throw new IllegalArgumentException("Frame Header start sync not found");
       }
       return first2;
   }
   public static boolean isFrameHeaderStart(int byte1, int byte2) {
       return isFrameHeaderStart(IOUtils.getIntBE(byte1, byte2));
   }
   public static boolean isFrameHeaderStart(int first2) {
       // First 14 bytes must be 11111111111110
       return (first2>>2) == FRAME_SYNC;
   }
   private static final int FRAME_SYNC = 0x3ffe;
   
   @Override
   public byte[] getData() {
       return subframeData;
   }

   /**
    * Is the block size fixed (frame header encodes the frame number)
    * or variable (frame header encodes the sample number)
    */
   public boolean isBlockSizeVariable() {
       return blockSizeVariable;
   }

   /**
    * Block size in inter-channel samples
    */
   public int getBlockSize() {
       return blockSize;
   }
   /**
    * Sample rate in kHz
    */
   public int getSampleRate() {
       return sampleRate;
   }
}
