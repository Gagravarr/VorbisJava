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

       // Decode those, as best we can
       boolean readBlockSize8 = false;
       boolean readBlockSize16 = false;
       if (blockSizeRaw == 0) {
           // Reserved
           blockSize = 0;
       } else if (blockSizeRaw == 1) {
           blockSize = 192;
       } else if (blockSizeRaw >= 2 && blockSizeRaw < 5) {
           blockSize = 576 * (int)Math.pow(2, blockSizeRaw-2);
       } else if (blockSizeRaw == 6) {
           readBlockSize8 = true;
       } else if (blockSizeRaw == 7) {
           readBlockSize16 = true;
       } else {
           blockSize = 256 * (int)Math.pow(2, blockSizeRaw-8);
       }

       if (sampleRateRaw < RATES.length) {
           sampleRate = RATES[sampleRateRaw].Hz;
       }

       // Channel Assignment + Sample Size + Res
       int caSs = stream.read();
       // TODO Decode

       // coded number - TODO

       // Ext block size
       if (readBlockSize8) {
           // TODO
       }
       if (readBlockSize16) {
           // TODO
       }

       // Ext sample rate
       if (sampleRateRaw == 12) {
           // 8 bit Hz
           // TODO
       }
       if (sampleRateRaw == 13) {
           // 16 bit Hz
           // TODO
       }
       if (sampleRateRaw == 14) {
           // 16 bit tens-of-Hz
           // TODO
       }

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
    * Sample rate in Hz
    * <p>A value of 0 means the value in the {@link FlacInfo} applies.
    * @return sample rate in HZ, or 0=read from info
    */
   public int getSampleRate() {
       return sampleRate;
   }

   protected static class SampleRate {
       protected final double kHz;
       protected final int Hz;
       private SampleRate(double kHz) {
           this.kHz = kHz;
           this.Hz = (int)Math.rint(kHz*1000);
       }
   };
   private SampleRate[] RATES = {
           new SampleRate(0), new SampleRate(88.2),
           new SampleRate(176.4), new SampleRate(192),
           new SampleRate(8), new SampleRate(16),
           new SampleRate(22.05), new SampleRate(24),
           new SampleRate(32), new SampleRate(44.1),
           new SampleRate(48), new SampleRate(96)
   };
}
