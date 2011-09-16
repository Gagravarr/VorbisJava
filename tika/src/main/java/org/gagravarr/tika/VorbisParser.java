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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisFile;
import org.gagravarr.vorbis.VorbisInfo;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Parser for OGG Vorbis audio files
 */
public class VorbisParser extends AbstractParser {
   private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
         MediaType.audio("ogg"),
         MediaType.audio("vorbis"),
   });
   
   public Set<MediaType> getSupportedTypes(ParseContext context) {
      return new HashSet<MediaType>(TYPES);
   }
   
   public void parse(
         InputStream stream, ContentHandler handler,
         Metadata metadata, ParseContext context)
         throws IOException, TikaException, SAXException {
      OggFile ogg = new OggFile(stream);
      VorbisFile vorbis = new VorbisFile(ogg);

      // Start
      XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
      xhtml.startDocument();

      // Extract the common Vorbis info
      extractInfo(metadata, vorbis.getInfo());
      
      // Extract any Vorbis comments
      extractComments(metadata, xhtml, vorbis.getComment());
      
      // Finish
      xhtml.endDocument();
   }
   
   protected void extractInfo(Metadata metadata, VorbisInfo info) throws TikaException {
      metadata.set(XMPDM.AUDIO_SAMPLE_RATE, (int)info.getRate());
      metadata.add("version", "Vorbis " + info.getVersion());
      
      if(info.getChannels() == 1) {
         metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "Mono"); 
      } else if(info.getChannels() == 2) {
         metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "Stereo");
      } else if(info.getChannels() == 5) {
         metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "5.1");
      } else if(info.getChannels() == 7) {
         metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "7.1");
      }
   }
   
   protected void extractComments(Metadata metadata, XHTMLContentHandler xhtml,
         VorbisComments comments) throws TikaException, SAXException {
      // Get the specific know comments
      metadata.set(Metadata.TITLE, comments.getTitle());
      metadata.set(Metadata.AUTHOR, comments.getArtist());
      metadata.set(XMPDM.ARTIST, comments.getArtist());
      metadata.set(XMPDM.ALBUM, comments.getAlbum());
      metadata.set(XMPDM.GENRE, comments.getGenre());
      metadata.set(XMPDM.RELEASE_DATE, comments.getDate());
      metadata.add("vendor", comments.getVendor());
      
      for(String comment : comments.getComments("comment")) {
         metadata.add(XMPDM.LOG_COMMENT.getName(), comment);
      }
      
      // Grab the rest just in case
      List<String> done = Arrays.asList(new String[] {
         "title", "author", "artist", "album", "genre", "date",
         "vendor", "tracknumber", "comment"
      });
      for(String key : comments.getAllComments().keySet()) {
         if(! done.contains(key)) {
            for(String value : comments.getAllComments().get(key)) {
               metadata.add(key, value);
            }
         }
      }

      // Output as text too
      xhtml.element("h1", comments.getTitle());
      xhtml.element("p", comments.getArtist());

       // Album and Track number
       if (comments.getTrackNumber() != null) {
           xhtml.element("p", comments.getAlbum() + ", track " + comments.getTrackNumber());
           metadata.set(XMPDM.TRACK_NUMBER, comments.getTrackNumber());
       } else {
           xhtml.element("p", comments.getAlbum());
       }
       
       // A few other bits
       xhtml.element("p", comments.getDate());
       for(String comment : comments.getComments("comment")) {
          xhtml.element("p", comment);
       }
       xhtml.element("p", comments.getGenre());
   }
}
