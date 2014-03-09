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
package org.gagravarr.tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.gagravarr.flac.FlacFirstOggPacket;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.opus.OpusPacket;
import org.gagravarr.vorbis.VorbisPacket;

/**
 * Detector for identifying specific file types stored
 *  within an Ogg container.
 */
public class OggDetector implements Detector {
   private static final long serialVersionUID = 591382028699008553L;

   public static final MediaType OGG_VIDEO = MediaType.video("ogg");
   public static final MediaType OGG_GENERAL = MediaType.application("ogg");
   public static final MediaType OGG_AUDIO = MediaType.audio("ogg");
   public static final MediaType OGG_VORBIS = MediaType.audio("vorbis");
   public static final MediaType OPUS_AUDIO = MediaType.audio("ogg; codecs=opus");
   public static final MediaType FLAC = MediaType.audio("x-flac");
   
   public MediaType detect(InputStream input, Metadata metadata)
         throws IOException {
      // Check if we have access to the document
      if (input == null) {
          return MediaType.OCTET_STREAM;
      }
      
      // Ensure we can mark and reset before detecting
      // Otherwise bail, to avoid us nibbling an unwindable stream
      if (!input.markSupported()) {
          return MediaType.OCTET_STREAM;
      }

      // Check if the document starts with the OGG header
      input.mark(4);
      try {
          if (input.read() != (byte)'O' || input.read() != (byte)'g'
                  || input.read() != (byte)'g' || input.read() != (byte)'S') {
              return MediaType.OCTET_STREAM;
          }
      } finally {
          input.reset();
      }
      
      // We can only detect the exact type when given a TikaInputStream
      TikaInputStream tis = TikaInputStream.cast(input);
      if (tis != null) {
         // We could potentially need to go a long way through the
         //  file in order to figure out what it is
         tis.mark((int)tis.getLength()+1);
         
         OggFile ogg = new OggFile(tis);
         
         // For tracking
         int streams = 0;
         int flacCount = 0;
         int opusCount = 0;
         int vorbisCount = 0;
         List<Integer> sids = new ArrayList<Integer>();
         
         
         // Check the streams in turn
         OggPacketReader r = ogg.getPacketReader();
         OggPacket p;
         while( (p = r.getNextPacket()) != null ) {
            if(p.isBeginningOfStream()) {
               streams++;
               sids.add(p.getSid());
               
               if(p.getData() != null && p.getData().length > 10) {
                  if(VorbisPacket.isVorbisStream(p)) {
                     // Vorbis Audio stream
                     vorbisCount++;
                  }
                  if(OpusPacket.isOpusStream(p)) {
                      // Opus Audio stream
                      opusCount++;
                   }
                  if(FlacFirstOggPacket.isFlacStream(p)) {
                     // FLAC-in-Ogg Audio stream
                     flacCount++;
                  }
               }
            }
         }
         
         // Tidy
         tis.reset();
         
         // Can we identify what it really is
         if(vorbisCount == 1 && streams == 1) {
            // Single Vorbis stream, regular Vorbis Audio file
            return OGG_VORBIS;
         } else if(opusCount == 1 && streams == 1) {
             // Single Opus stream, regular Opus Audio file
             return OPUS_AUDIO;
         } else if(flacCount == 1 && streams == 1) {
            // Single FLAC-in-Ogg stream, regular FLAC Audio file
            return FLAC;
         } else if(vorbisCount > 1 && vorbisCount == streams) {
            // Multiple Vorbis streams, multi-track Vorbis audio
            return OGG_VORBIS;
         } else if(opusCount > 1 && vorbisCount == streams) {
             // Multiple Opus streams, multi-track Opus audio
             return OPUS_AUDIO;
         } else if(flacCount > 1 && flacCount == streams) {
            // Multiple FLAC streams, multi-track FLAC audio
            return FLAC;
         } else if(streams > 0) {
            // Something else...
            // TODO Detect video
         } else {
            // Empty file
            return OGG_GENERAL;
         }
      }
      
      // Couldn't determine a more specific type
      return OGG_GENERAL;
   }
}
