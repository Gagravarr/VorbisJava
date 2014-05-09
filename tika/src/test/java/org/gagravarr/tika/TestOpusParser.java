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

import junit.framework.TestCase;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TestOpusParser extends TestCase {
    private InputStream getTestFile() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS.opus");
    }

    @SuppressWarnings("deprecation")
    public void testParser() throws Exception {
        OpusParser parser = new OpusParser();

        ContentHandler handler = new BodyContentHandler();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();

        parser.parse(
                TikaInputStream.get(getTestFile()), handler,
                metadata, context
        );

        // Check legacy style metadata
        assertEquals("Test Artist", metadata.get(Metadata.AUTHOR));
        assertEquals("Test Title", metadata.get(Metadata.TITLE));

        // Check new-style metadata
        assertEquals("Test Artist", metadata.get(Office.AUTHOR));
        assertEquals("Test Artist", metadata.get(TikaCoreProperties.CREATOR));
        assertEquals("Test Title", metadata.get(TikaCoreProperties.TITLE));

        assertEquals(null, metadata.get(XMPDM.ALBUM));
        assertEquals("Test Artist", metadata.get(XMPDM.ARTIST));
        assertEquals("Stereo", metadata.get(XMPDM.AUDIO_CHANNEL_TYPE));
        assertEquals("44100", metadata.get(XMPDM.AUDIO_SAMPLE_RATE));
        assertEquals(null, metadata.get(XMPDM.GENRE));
        assertEquals("Test Comment", metadata.get(XMPDM.LOG_COMMENT));
        assertEquals(null, metadata.get(XMPDM.RELEASE_DATE));
        assertEquals(null, metadata.get(XMPDM.TRACK_NUMBER));
        assertEquals("0.03", metadata.get(XMPDM.DURATION));

        assertEquals("libopus 0.9.14", metadata.get("vendor"));
        assertEquals("Opus 0.1", metadata.get("version"));

        // Check text
        String content = handler.toString();
        assertTrue(content.contains("Test Title"));
        assertTrue(content.contains("Test Artist"));
        assertTrue(content.contains("Test Comment"));
        assertTrue(content.contains("00:00:00.03"));
    }
}
