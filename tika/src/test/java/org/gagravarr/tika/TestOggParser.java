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

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.gagravarr.ogg.AbstractIdentificationTest;
import org.xml.sax.ContentHandler;

public class TestOggParser extends AbstractIdentificationTest {
    private OggParser parser = new OggParser();
    private ParseContext context = new ParseContext();
    private ContentHandler handler = new BodyContentHandler();
    private Metadata m;
    
    public void testSingleStreamFiles() throws Exception {
        // Vorbis
        m = new Metadata();
        parser.parse(getTestVorbisFile(), handler, m, context);
        assertEquals("1", m.get("streams-total"));
        assertEquals("1", m.get("streams-vorbis"));
        assertEquals(null, m.get("streams-theora"));
        
        // Opus
        m = new Metadata();
        parser.parse(getTestOpusFile(), handler, m, context);
        assertEquals("1", m.get("streams-total"));
        assertEquals("1", m.get("streams-opus"));
        assertEquals(null, m.get("streams-theora"));
    }
    
    public void testMultiStreamFiles() throws Exception {
        // Theora + Annodex
        m = new Metadata();
        parser.parse(getTestTheoraSkeletonFile(), handler, m, context);
        assertEquals("2", m.get("streams-total"));
        assertEquals("1", m.get("streams-theora"));
        assertEquals("1", m.get("streams-annodex"));
        assertEquals(null, m.get("streams-vorbis"));
        
        // Theora + Annodex + CMML
        m = new Metadata();
        parser.parse(getTestTheoraSkeletonCMMLFile(), handler, m, context);
        assertEquals("3", m.get("streams-total"));
        assertEquals("1", m.get("streams-theora"));
        assertEquals("1", m.get("streams-annodex"));
        assertEquals("1", m.get("streams-cmml"));
        assertEquals(null, m.get("streams-vorbis"));
    }
    
    public void testOtherFiles() throws Exception {
        // Non-standard stream
        m = new Metadata();
        parser.parse(getTestOggFile(), handler, m, context);
        assertEquals("1", m.get("streams-total"));
        assertEquals("1", m.get("streams-unknown"));
        
        // Flac Native - Not Ogg
        m = new Metadata();
        parser.parse(getTestFlacNativeFile(), handler, m, context);
        assertEquals("0", m.get("streams-total"));
    }
}
