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
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMP;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TestFlacParser extends TestCase {
    private InputStream getTestOggFile() throws IOException {
        return this.getClass().getResourceAsStream("/testFLAC.oga");
     }
     private InputStream getTestFlacFile() throws IOException {
        return this.getClass().getResourceAsStream("/testFLAC.flac");
     }
    private InputStream getTestFlacFileComments() throws IOException {
        return this.getClass().getResourceAsStream("/testFLACWithoutPadding.flac");
    }
     public void testFlacNative() throws Exception {
         doTestFlac(getTestFlacFile(), false);
     }
     public void testFlacOgg() throws Exception {
         doTestFlac(getTestOggFile(), true);
     }

    public void testFlacNativeithoutPadding() throws Exception {
        doTestFlacComments(getTestFlacFileComments(), false);
    }
    @SuppressWarnings("deprecation")
    public void doTestFlac(InputStream input, boolean hasVersion) throws Exception {
        FlacParser parser = new FlacParser();

        ContentHandler handler = new BodyContentHandler();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();

        parser.parse(
                TikaInputStream.get(input), handler, metadata, context
        );

        // Check legacy style metadata
        assertEquals("Test Artist", metadata.get(Metadata.AUTHOR));
        assertEquals("Test Title", metadata.get(Metadata.TITLE));

        // Check new-style metadata
        assertEquals("Test Artist", metadata.get(Office.AUTHOR));
        assertEquals("Test Artist", metadata.get(TikaCoreProperties.CREATOR));
        assertEquals("Test Title", metadata.get(TikaCoreProperties.TITLE));

        assertEquals("Test Album", metadata.get(XMPDM.ALBUM));
        assertEquals("Test Artist", metadata.get(XMPDM.ARTIST));
        assertEquals("Stereo", metadata.get(XMPDM.AUDIO_CHANNEL_TYPE));
        assertEquals("44100", metadata.get(XMPDM.AUDIO_SAMPLE_RATE));
        assertEquals("Test Genre", metadata.get(XMPDM.GENRE));
        assertEquals("Test Comment", metadata.get(XMPDM.LOG_COMMENT));
        assertEquals("2010-01-26", metadata.get(XMPDM.RELEASE_DATE));
        assertEquals("1", metadata.get(XMPDM.TRACK_NUMBER));

        assertEquals("reference libFLAC 1.2.1 20070917", metadata.get(XMP.CREATOR_TOOL));
        assertEquals("reference libFLAC 1.2.1 20070917", metadata.get("vendor"));
        assertEquals("FLAC", metadata.get(XMPDM.AUDIO_COMPRESSOR));
        if (hasVersion) {
            assertEquals("Flac 1.0", metadata.get("version"));
        }

        // Check text
        String content = handler.toString();
        assertTrue(content.contains("Test Title"));
        assertTrue(content.contains("Test Artist"));
        assertTrue(content.contains("Test Album"));
        assertTrue(content.contains("2010"));
        assertTrue(content.contains("Test Comment"));
        assertTrue(content.contains("Test Genre"));
    }

    public void doTestFlacComments(InputStream input, boolean hasVersion) throws Exception {
        FlacParser parser = new FlacParser();

        ContentHandler handler = new BodyContentHandler();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();

        parser.parse(
                TikaInputStream.get(input), handler, metadata, context
        );

        assertEquals("Beirut", metadata.get(XMPDM.ARTIST));
    }
}
