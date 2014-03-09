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

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.vorbis.VorbisComments;


/**
 * This is a {@link VorbisComments} with an Opus metadata
 *  block header, rather than the usual vorbis one.
 */
public class OpusTags extends VorbisComments {
   public OpusTags(OggPacket packet) {
      super(packet);
      
      // Verify the type
      String type = IOUtils.getUTF8(getData(), 0, 8);
      if(! OpusPacket.MAGIC_TAGS.equals(type)) {
         throw new IllegalArgumentException("Invalid type " + type);
      }
   }
   public OpusTags() {
      super();
   }
   
   /**
    * 8 byte OpusTags
    */
   @Override
   protected int getHeaderSize() {
      return 8;
   }

   /**
    * Magic string
    */
   @Override
   protected void populateMetadataHeader(byte[] b, int type, int dataLength) {
       IOUtils.putUTF8(b, 0, OpusPacket.MAGIC_TAGS);
   }
}
