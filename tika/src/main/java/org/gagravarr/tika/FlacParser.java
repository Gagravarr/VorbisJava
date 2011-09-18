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
import java.util.Collections;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.gagravarr.flac.FlacFile;
import org.gagravarr.flac.FlacInfo;
import org.gagravarr.flac.FlacOggFile;
import org.gagravarr.vorbis.VorbisInfo;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * TODO Finish FLAC support then implement
 */
public class FlacParser extends AbstractParser {
   private static MediaType TYPE = MediaType.audio("x-flac");

   public Set<MediaType> getSupportedTypes(ParseContext context) {
      return Collections.singleton(TYPE);
   }

   public void parse(
         InputStream stream, ContentHandler handler,
         Metadata metadata, ParseContext context)
         throws IOException, TikaException, SAXException {
      FlacFile flac = FlacFile.open(stream);
      
      // Start
      XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
      xhtml.startDocument();

      // Extract the common FLAC info
      extractInfo(metadata, flac.getInfo());
      if(flac instanceof FlacOggFile) {
         FlacOggFile ogg = (FlacOggFile)flac;
         metadata.add("version", "Flac " + ogg.getFirstPacket().getMajorVersion() +
                                 "." + ogg.getFirstPacket().getMinorVersion());
      }
      
      // Extract any Vorbis comments
      VorbisParser.extractComments(metadata, xhtml, flac.getTags());
      
      // Finish
      xhtml.endDocument();
   }
   
   protected void extractInfo(Metadata metadata, FlacInfo info) throws TikaException {
      metadata.set(XMPDM.AUDIO_SAMPLE_RATE, (int)info.getSampleRate());
      //metadata.set(XMPDM.AUDIO_SAMPLE_TYPE, info.getBitsPerSample()+"int"); // TODO
      
      VorbisParser.extractChannelInfo(metadata, info.getNumChannels());
   }
}
