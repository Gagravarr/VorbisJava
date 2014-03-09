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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.gagravarr.flac.FlacFirstOggPacket;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.opus.OpusPacketFactory;
import org.gagravarr.vorbis.VorbisPacket;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * General parser for non audio OGG files.
 * 
 * We provide a detector which should help specialise Audio OGG
 *  files to their appropriate types, so we just handle the rest
 */
public class OggParser extends AbstractParser {
   private static final long serialVersionUID = -5686095376587813226L;

   private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
         OggDetector.OGG_GENERAL, OggDetector.OGG_VIDEO 
   });
   
   public Set<MediaType> getSupportedTypes(ParseContext context) {
      return new HashSet<MediaType>(TYPES);
   }
   
   public void parse(
         InputStream stream, ContentHandler handler,
         Metadata metadata, ParseContext context)
         throws IOException, TikaException, SAXException {
      // Process the file straight through once
      OggFile ogg = new OggFile(stream);
      
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
               if(OpusPacketFactory.isOpusStream(p)) {
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
      
      // Report what little we can do
      metadata.add("streams-total", Integer.toString(streams));
      metadata.add("streams-vorbis", Integer.toString(vorbisCount));
      metadata.add("streams-opus", Integer.toString(opusCount));
      metadata.add("streams-flac", Integer.toString(flacCount));
   }
}
