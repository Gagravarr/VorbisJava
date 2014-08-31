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
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.theora.TheoraFile;
import org.gagravarr.theora.TheoraInfo;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Parser for OGG Theora video files, which may also
 *  contain one or more soundtrack streams.
 *
 * TODO Finish this off
 */
public class TheoraParser extends AbstractParser {
    private static final long serialVersionUID = -5459916822092342944L;

    protected static final MediaType THEORA_VIDEO =
            MediaType.parse(OggStreamIdentifier.THEORA_VIDEO.mimetype);

    private static List<MediaType> TYPES = Arrays.asList(new MediaType[] {
            THEORA_VIDEO,
    });

    public Set<MediaType> getSupportedTypes(ParseContext context) {
       return new HashSet<MediaType>(TYPES);
    }

    public void parse(
          InputStream stream, ContentHandler handler,
          Metadata metadata, ParseContext context)
          throws IOException, TikaException, SAXException {
       metadata.set(Metadata.CONTENT_TYPE, THEORA_VIDEO.toString());
       metadata.set(XMPDM.VIDEO_COMPRESSOR, "Theora");

       // Open the process the files
       OggFile ogg = new OggFile(stream);
       TheoraFile theora = new TheoraFile(ogg);

       // Start
       XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
       xhtml.startDocument();

       // If possible, use the Skeleton to work out how things relate
       if (theora.getSkeleton() != null) {
           // TODO Use this to work out the prefixes on metadata keys
       } else {
           // TODO Guess some sensible defaults for prefixes
       }

       // Extract the common Theora info
       extractInfo(metadata, theora.getInfo());

       // Extract the common Theora comments

       // Extract any soundtracks
       // TODO

       // Finish
       xhtml.endDocument();
    }

    protected void extractInfo(Metadata metadata, TheoraInfo info) throws TikaException {
       metadata.add("version", "Theora " + info.getVersion());

       // TODO Do the rest
    }
}
