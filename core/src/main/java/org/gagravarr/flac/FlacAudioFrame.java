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

import java.io.IOException;
import java.io.InputStream;

import org.gagravarr.ogg.IOUtils;

/**
 * Raw, compressed audio data.
 * TODO Parse into constituent parts
 */
public class FlacAudioFrame extends FlacFrame {
   private byte[] data; // TODO Parse
   private long position; // TODO Is this always there?
   
   public FlacAudioFrame(byte[] data) {
      this.data = data;
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
       // TODO Decode
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
       return isFrameHeaderStart(IOUtils.getInt(byte1, byte2));
   }
   public static boolean isFrameHeaderStart(int first2) {
       // First 14 bytes must be 11111111111110
       return (first2>>2) == FRAME_SYNC;
   }
   private static final int FRAME_SYNC = 0x3ffe;
   
   public byte[] getData() {
      return data;
   }
}
