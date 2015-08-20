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

import org.gagravarr.ogg.BitsReader;
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
   private long codedNumber;

   private int blockSizeRaw;
   private int blockSize;
   private int sampleRateRaw;
   private int sampleRate;

   private int numChannels;
   private int channelType;
   private int sampleSizeRaw;
   private int sampleSizeBits;

   private byte[] subframeData;

   public FlacAudioFrame(byte[] data, FlacInfo info) throws IOException {
       this(new ByteArrayInputStream(data), info);
   }

   /**
    * Creates the frame from the stream, with header sync checking
    */
   public FlacAudioFrame(InputStream stream, FlacInfo info) throws IOException {
       this(getAndCheckFirstTwo(stream), stream, info);
   }
   /**
    * Creates the frame from the pre-read 2 bytes and stream, with header sync checking
    */
   public FlacAudioFrame(int byte1, int byte2, InputStream stream, FlacInfo info) throws IOException {
       this(getAndCheckFirstTwo(byte1, byte2), stream, info);
   }
   /**
    * Creates the frame from the pre-read 2 bytes and stream, no sync checks.
    * Info is needed, as values of 0 often mean "as per info defaults".
    * TODO Track the data, so we can write it back out again
    */
   public FlacAudioFrame(int first2, InputStream stream, FlacInfo info) throws IOException {
       // First 14 bits are the sync, 15 is reserved, 16 is block size
       blockSizeVariable = ((first2 & 1) == 1);

       // Mostly, this works in bits not nicely padded bytes
       BitsReader br = new BitsReader(stream);
       
       // Block Size + Sample Rate
       blockSizeRaw = br.read(4); 
       sampleRate = br.read(4);

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

       if (sampleRateRaw == 0) {
           sampleRate = info.getSampleRate();
       } else if (sampleRateRaw < RATES.length) {
           sampleRate = RATES[sampleRateRaw].Hz;
       }

       // Channel Assignment + Sample Size + Res
       channelType = br.read(4);
       if (channelType < 8) {
           numChannels = channelType + 1;
       } else {
           numChannels = 2;
       }
       
       sampleSizeRaw = br.read(3);
       br.read(1);
       if (sampleSizeRaw == 0) {
           sampleSizeBits = info.getBitsPerSample();
       } else if (sampleSizeRaw == 1) {
           sampleSizeBits = 8;
       } else if (sampleSizeRaw == 2) {
           sampleSizeBits = 12;
       } else if (sampleSizeRaw == 3) {
           // Reserved
           sampleSizeBits = 0;
       } else if (sampleSizeRaw == 4) {
           sampleSizeBits = 16;
       } else if (sampleSizeRaw == 5) {
           sampleSizeBits = 20;
       } else if (sampleSizeRaw == 6) {
           sampleSizeBits = 24;
       } else if (sampleSizeRaw == 7) {
           // Reserved
           sampleSizeBits = 0;
       }

       // Coded Number - either sample or frame, based on blockSizeVariable
       codedNumber = IOUtils.readUE7(stream);

       // Ext block size
       if (readBlockSize8) {
           blockSize = stream.read()+1;
       }
       if (readBlockSize16) {
           blockSize = IOUtils.getIntBE(stream.read(), stream.read())+1;
       }

       // Ext sample rate
       if (sampleRateRaw == 12) {
           // 8 bit Hz
           sampleRate = stream.read();
       }
       if (sampleRateRaw == 13) {
           // 16 bit Hz
           sampleRate = IOUtils.getIntBE(stream.read(), stream.read());
       }
       if (sampleRateRaw == 14) {
           // 16 bit tens-of-Hz
           sampleRate = 10*IOUtils.getIntBE(stream.read(), stream.read());
       }

       // Header CRC, not checked
       stream.read();

       // One sub-frame per channel
       for (int cn=0; cn<numChannels; cn++) {
           // Zero
           br.read(1);
           // Type
           int type = br.read(6);
           // Wasted Bits per Sample
           int wb = br.read(1);
           if (wb == 1) {
               while (br.read(1) == 0) {
                   wb++;
               }
           }

           if (type == 0) {
               // TODO Constant
           } else if (type == 1) {
               // TODO Verbatim
           } else if (type >= 2 && type <= 7) {
               // Reserved, skip
           } else if (type >= 8 && type <16) {
               // Fixed
               int order = type & 7;
               int size = order * sampleSizeBits;
               
               // TODO Save this
               br.read(size);
               // TODO Read the rest
           } else if (type >= 16 && type <= 31) {
               // Reserved, skip
           } else if (type >= 32) {
               // TODO LPC
           }
       }

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
    */
   public int getSampleRate() {
       return sampleRate;
   }
   /**
    * Sample size in bits
    */
   public int getBitsPerSample() {
       return sampleSizeBits;
   }
   /**
    * Number of channels
    */
   public int getNumChannels() {
       return numChannels;
   }
   /**
    * If {@link #isBlockSizeVariable()}, then this is the
    *  sample number, otherwise the frame number
    */
   public long getCodedNumber() {
       return codedNumber;
   }

   protected static class SampleRate {
       protected final double kHz;
       protected final int Hz;
       private SampleRate(double kHz) {
           this.kHz = kHz;
           this.Hz = (int)Math.rint(kHz*1000);
       }
   };
   private static final SampleRate[] RATES = {
           new SampleRate(0), new SampleRate(88.2),
           new SampleRate(176.4), new SampleRate(192),
           new SampleRate(8), new SampleRate(16),
           new SampleRate(22.05), new SampleRate(24),
           new SampleRate(32), new SampleRate(44.1),
           new SampleRate(48), new SampleRate(96)
   };
}
