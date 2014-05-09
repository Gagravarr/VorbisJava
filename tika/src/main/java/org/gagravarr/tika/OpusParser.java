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
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Parser for OGG Opus audio files
 */
public class OpusParser extends OggAudioParser {
   private static final long serialVersionUID = 5904981674814527529L;

   protected static final MediaType OPUS_AUDIO =
           MediaType.parse(OggStreamIdentifier.OPUS_AUDIO.mimetype);
   protected static final MediaType OPUS_AUDIO_ALT =
           MediaType.parse(OggStreamIdentifier.OPUS_AUDIO_ALT.mimetype);

   private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
           OPUS_AUDIO, OPUS_AUDIO_ALT
   });

   public Set<MediaType> getSupportedTypes(ParseContext context) {
      return new HashSet<MediaType>(TYPES);
   }
   
   public void parse(
         InputStream stream, ContentHandler handler,
         Metadata metadata, ParseContext context)
         throws IOException, TikaException, SAXException {
      metadata.set(Metadata.CONTENT_TYPE, OPUS_AUDIO.toString());
      metadata.set(XMPDM.AUDIO_COMPRESSOR, "Opus");

      // Open the process the files
      OggFile ogg = new OggFile(stream);
      OpusFile opus = new OpusFile(ogg);

      // Start
      XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
      xhtml.startDocument();

      // Extract the common Opus info
      extractInfo(metadata, opus.getInfo());

      // Extract any Vorbis comments
      extractComments(metadata, xhtml, opus.getTags());

      // Extract the audio length
      extractDuration(metadata, xhtml, opus.getInfo().getRate(), opus);

      // Finish
      xhtml.endDocument();
   }
   
   protected void extractInfo(Metadata metadata, OpusInfo info) throws TikaException {
      metadata.set(XMPDM.AUDIO_SAMPLE_RATE, (int)info.getRate());
      metadata.add("version", "Opus " + info.getMajorVersion() + "." + info.getMinorVersion());
    
      extractChannelInfo(metadata, info.getChannels());
   }
}
