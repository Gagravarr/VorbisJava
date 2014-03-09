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
package org.gagravarr.opus;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.OggPacket;

/**
 * Parent of all Opus packets
 */
public abstract class OpusPacket extends HighLevelOggStreamPacket {
   public static final String MAGIC_HEADER = "OpusHead";
   public static final String MAGIC_TAGS = "OpusTags";
   
   protected OpusPacket(OggPacket oggPacket) {
       super(oggPacket);
   }
   protected OpusPacket() {
       super();
   }

   /**
    * Does this packet (the first in the stream) contain
    *  the magic string indicating that it's an opus
    *  one?
    */
   public static boolean isOpusStream(OggPacket firstPacket) {
       if(! firstPacket.isBeginningOfStream()) {
           return false;
       }
       return isOpusSpecial(firstPacket);
   }

   private static boolean isOpusSpecial(OggPacket packet) {
       byte[] d = packet.getData();
       
       // Is it an Opus Header or Tags packet?
       if (d.length < 12) return false;
       if (d[0] == (byte)'O' &&
           d[1] == (byte)'p' &&
           d[2] == (byte)'u' &&
           d[3] == (byte)'s') {
           if (d[4] == (byte)'H' &&
               d[5] == (byte)'e' &&
               d[6] == (byte)'a' &&
               d[7] == (byte)'d') {
               return true;
           }
           if (d[4] == (byte)'T' &&
               d[5] == (byte)'a' &&
               d[6] == (byte)'g' &&
               d[7] == (byte)'s') {
               return true;
           }
           // Not a standard Opus special packet
       }
       return false;
   }

   /**
    * Creates the appropriate {@link OpusPacket}
    *  instance based on the type.
    */
   public static HighLevelOggStreamPacket create(OggPacket packet) {
       // Special header types detection
       if(isOpusSpecial(packet)) {
           byte type = packet.getData()[4];
           switch(type) {
           case (byte)'H': // OpusHead
               return new OpusInfo(packet);
           case (byte)'T': // OpusTags
               return new OpusTags(packet);
           }
       }

       // TODO
       //return new OpusAudioData(packet);
       return null;
   }
}