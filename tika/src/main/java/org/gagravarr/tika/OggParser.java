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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.ogg.OggStreamIdentifier.OggStreamType;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * General parser for Ogg files where we don't know what 
 *  the specific kind is.
 * 
 * We provide a detector which should help specialise the more
 *  common kinds of Ogg files, based on their streams, to their
 *  appropriate types. This just handles the rest, as best we can.
 */
public class OggParser extends AbstractParser {
   private static final long serialVersionUID = -5686095376587813226L;

   private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
         // General ones, where we'll never be able to help
         OggDetector.OGG_GENERAL, OggDetector.OGG_AUDIO,
         OggDetector.OGG_VIDEO, 
         // Ones we lack a proper parser for
         OggDetector.THEORA_VIDEO, OggDetector.DIRAC_VIDEO,
         OggDetector.OGM_VIDEO, OggDetector.OGG_UVS,
         OggDetector.OGG_YUV, OggDetector.OGG_RGB,
         OggDetector.OGG_PCM
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
      
      // To track the streams we find
      Map<OggStreamType, Integer> streams = 
              new HashMap<OggStreamType, Integer>();
      List<Integer> sids = new ArrayList<Integer>();
      int totalStreams = 0;
      
      // Check the streams in turn
      OggPacketReader r = ogg.getPacketReader();
      OggPacket p;
      while( (p = r.getNextPacket()) != null ) {
         if (p.isBeginningOfStream()) {
            totalStreams++;
            sids.add(p.getSid());
            
            OggStreamType type = OggStreamIdentifier.identifyType(p);
            Integer prevValue = streams.get(type);
            if (prevValue == null) {
                prevValue = 0;
            }
            streams.put(type, (prevValue+1));
         }
      }
      
      // Report what little we can do
      metadata.add("streams-total", Integer.toString(totalStreams));
      for (OggStreamType type : streams.keySet()) {
          String key = type.mimetype.substring(type.mimetype.indexOf('/')+1);
          if (key.startsWith("x-")) {
              key = key.substring(2);
          }
          if (type == OggStreamIdentifier.UNKNOWN) {
              key = "unknown";
          }
          metadata.add("streams-" + key, Integer.toString(streams.get(type)));
      }
   }
}
