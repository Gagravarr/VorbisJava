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
import org.gagravarr.ogg.BytesCapturingInputStream;
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

   private FlacAudioSubFrame[] subFrames;

   private byte[] frameData;

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
    */
   public FlacAudioFrame(int first2, InputStream rawStream, FlacInfo info) throws IOException {
       // Wrap the InputStream so that it captures the contents
       BytesCapturingInputStream stream = new BytesCapturingInputStream(rawStream);

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
       subFrames = new FlacAudioSubFrame[numChannels];
       for (int cn=0; cn<numChannels; cn++) {
           // Zero
           br.read(1);
           // Type
           int type = br.read(6);
           // Wasted Bits per Sample
           int wb = br.read(1);
           if (wb == 1) {
               wb = br.bitsToNextOne() + 1;
           }
           // Check there's data
           if (br.isEOF())
               throw new IllegalArgumentException("No data left to read subframe for channel "
                                                  + (cn+1) + " of " + numChannels);

           // Sub-Frame data
           subFrames[cn] = FlacAudioSubFrame.create(type, cn, wb, this, br);
       }

       // Skip any remaining bits, to hit the boundary
       br.readToByteBoundary();

       // Footer CRC, not checked
       stream.read();
       stream.read();

       // Capture the raw bytes read
       frameData = stream.getData();
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
   
   /**
    * Returns the contents, including the sync header
    */
   @Override
   public byte[] getData() {
       byte[] data = new byte[frameData.length+2];

       int first2 = (FRAME_SYNC<<2);
       if (blockSizeVariable) first2++;

       IOUtils.putInt2BE(data, 0, first2);
       System.arraycopy(frameData, 0, data, 2, frameData.length);

       return data;
   }
   /**
    * How big is the compressed audio frame, including headers?
    */
   public int getCompresedSize() {
      return frameData.length+2;
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
   protected int getChannelType() {
       return channelType;
   }
   /**
    * The well-known Channel Type, or null if a non-standard one
    */
   public ChannelType getChannelTypeEnum() {
      for (ChannelType t : ChannelType.values()) {
         if (t.type == channelType) return t;
      }
      return null;
   }
   /**
    * If {@link #isBlockSizeVariable()}, then this is the
    *  sample number, otherwise the frame number
    */
   public long getCodedNumber() {
       return codedNumber;
   }

   /**
    * SubFrames hold the encoded audio data on a per-channel basis
    */
   public FlacAudioSubFrame[] getSubFrames() {
       return subFrames;
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

   public static enum ChannelType {
      INDEPENDENT(0, "Independent"),
      LEFT (0x9, "Left Side"),
      RIGHT(0xa, "Right Side"),
      MID  (0xb, "Mid Side");

      public final int type;
      public final String description; 
      ChannelType(int type, String desc) { 
         this.type = type;
         this.description = desc;
      }
   }
}
