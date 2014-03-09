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
import java.io.OutputStream;

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.vorbis.VorbisComments;

/**
 * This is a {@link VorbisComments} with a Flac metadata
 *  block header, rather than the usual vorbis one.
 */
public class FlacTags extends VorbisComments {
   public FlacTags(OggPacket packet) {
      super(packet);
      
      // Verify the type
      byte type = getData()[0];
      if(type != FlacMetadataBlock.VORBIS_COMMENT) {
         throw new IllegalArgumentException("Invalid type " + type);
      }
   }
   public FlacTags() {
      super();
   }
   
   /**
    * Type plus three byte length
    */
   @Override
   public int getHeaderSize() {
      return 4;
   }

   /**
    * Type plus three byte length
    */
   @Override
   public void populateMetadataHeader(byte[] b, int type, int dataLength) {
      b[0] = FlacMetadataBlock.VORBIS_COMMENT;
      IOUtils.putInt3BE(b, 1, dataLength);
   }
   
   protected static class FlacTagsAsMetadata extends FlacMetadataBlock {
      private FlacTags tags;
      
      protected FlacTagsAsMetadata(byte[] data) {
         super(VORBIS_COMMENT);
         
         // This is the only metadata which needs the type
         //  and length in addition to the main data
         byte[] d = new byte[data.length+4];
         d[0] = FlacMetadataBlock.VORBIS_COMMENT;
         System.arraycopy(data, 0, d, 4, data.length);
         this.tags = new FlacTags(new OggPacket(d));
      }
      
      @Override
      public byte[] getData() {
         return tags.getData();
      }
      
      @Override
      protected void write(OutputStream out) throws IOException {
         throw new IllegalStateException("Must not call directly");
      }

      public FlacTags getTags() {
         return tags;
      }
   }
}
