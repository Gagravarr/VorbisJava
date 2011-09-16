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
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
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
      // TODO
   }
}
