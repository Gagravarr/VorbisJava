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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
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

   protected static final MediaType OGG_GENERAL = OggDetector.OGG_GENERAL;
   protected static final MediaType OGG_AUDIO = OggDetector.OGG_AUDIO;
   protected static final MediaType OGG_VIDEO = OggDetector.OGG_VIDEO;

   protected static final MediaType KATE =
           MediaType.parse(OggStreamIdentifier.KATE.mimetype);
   
   protected static final MediaType DAALA_VIDEO =
           MediaType.parse(OggStreamIdentifier.DAALA_VIDEO.mimetype);
   protected static final MediaType DIRAC_VIDEO =
           MediaType.parse(OggStreamIdentifier.DIRAC_VIDEO.mimetype);
   protected static final MediaType OGM_VIDEO =
           MediaType.parse(OggStreamIdentifier.OGM_VIDEO.mimetype);
   protected static final MediaType OGG_UVS =
           MediaType.parse(OggStreamIdentifier.OGG_UVS.mimetype);
   protected static final MediaType OGG_YUV =
           MediaType.parse(OggStreamIdentifier.OGG_YUV.mimetype);
   protected static final MediaType OGG_RGB =
           MediaType.parse(OggStreamIdentifier.OGG_RGB.mimetype);
   protected static final MediaType OGG_PCM =
           MediaType.parse(OggStreamIdentifier.OGG_PCM.mimetype);

   private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
         // General ones, which didn't fit a specific pattern (eg 2*Opus+2*Vorbis,
         //  Unknown audio codec, unknown video codec etc). We can't do much
         //  for these
         OGG_GENERAL, OGG_AUDIO, OGG_VIDEO,
         // Ones we lack a proper parser for, but may be able to support
         //  better in the future with a dedicated parser
         DAALA_VIDEO, DIRAC_VIDEO, OGM_VIDEO,
         OGG_UVS, OGG_YUV,  OGG_RGB, OGG_PCM,
         KATE
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
      Map<OggStreamType.Kind, Integer> streamKinds =
              new HashMap<OggStreamType.Kind, Integer>();
      List<Integer> sids = new ArrayList<Integer>();
      int totalStreams = 0;
      
      // Start
      XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
      xhtml.startDocument();

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

            prevValue = streamKinds.get(type.kind);
            if (prevValue == null) {
                prevValue = 0;
            }
            streamKinds.put(type.kind, (prevValue+1));
         }
      }

      // Report about the streams, which is all we really can do
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
      for (OggStreamType.Kind kind : streamKinds.keySet()) {
          String key = kind.name().toLowerCase(Locale.ROOT);
          metadata.add("streams-" + key, Integer.toString(streamKinds.get(kind)));
      }

      // If we found a skeleton stream, report the relations between the streams
      // TODO

      // Finish
      xhtml.endDocument();
      ogg.close();
   }
}
