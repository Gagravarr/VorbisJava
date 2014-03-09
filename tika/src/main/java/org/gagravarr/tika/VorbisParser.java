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
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.vorbis.VorbisFile;
import org.gagravarr.vorbis.VorbisInfo;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Parser for OGG Vorbis audio files
 */
public class VorbisParser extends OggAudioParser {
   private static final long serialVersionUID = 5904981674814527529L;

   private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
         OggDetector.OGG_AUDIO, OggDetector.OGG_VORBIS 
   });
   
   public Set<MediaType> getSupportedTypes(ParseContext context) {
      return new HashSet<MediaType>(TYPES);
   }
   
   public void parse(
         InputStream stream, ContentHandler handler,
         Metadata metadata, ParseContext context)
         throws IOException, TikaException, SAXException {
      metadata.set(Metadata.CONTENT_TYPE, OggDetector.OGG_VORBIS.toString());
      metadata.set(XMPDM.AUDIO_COMPRESSOR, "Vorbis");

      // Open the process the files
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
    
      extractChannelInfo(metadata, info.getChannels());
   }
}
