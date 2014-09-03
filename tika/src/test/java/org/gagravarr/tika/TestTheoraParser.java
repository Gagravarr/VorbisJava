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
import org.apache.tika.metadata.XMP;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TestTheoraParser extends TestCase {
    private InputStream getTheoraVorbisFile() throws IOException {
        return this.getClass().getResourceAsStream("/testTheoraVORBIS.ogg");
    }
    private InputStream getTheoraVorbisSkeletonFile() throws IOException {
        return this.getClass().getResourceAsStream("/testTheoraVORBISSkeleton.ogg");
    }
    private InputStream getTheoraSpeexFile() throws IOException {
        return this.getClass().getResourceAsStream("/testTheoraSPEEX.ogg");
    }
    private InputStream[] getAllTheoraFiles() throws IOException {
        return new InputStream[] {
                getTheoraVorbisFile(), getTheoraVorbisSkeletonFile(),
                getTheoraSpeexFile()
        };
    }
    // TODO Finish the other test files and use them

    public void testBasics() throws Exception {
        TheoraParser parser = new TheoraParser();
        for (InputStream inp : getAllTheoraFiles()) {
            ContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            Metadata metadata = new Metadata();

            parser.parse(
                    TikaInputStream.get(inp), handler,
                    metadata, context
            );

            // Check basic metadata
            assertEquals(TheoraParser.THEORA_VIDEO.toString(), metadata.get(Metadata.CONTENT_TYPE));
            assertEquals("Theora 3.2.1", metadata.get("version"));

            assertEquals("Theora", metadata.get(XMPDM.VIDEO_COMPRESSOR));

            assertEquals("Xiph.Org libtheora 1.1 20090822 (Thusnelda)", metadata.get(XMP.CREATOR_TOOL));
            assertEquals("Xiph.Org libtheora 1.1 20090822 (Thusnelda)", metadata.get("vendor"));
            assertEquals("ffmpeg2theora-0.27", metadata.get("encoder"));

            // Check text
            // TODO
        }
    }

    @SuppressWarnings("deprecation")
    public void DISABLEDtestParserOnVideo() throws Exception {
        TheoraParser parser = new TheoraParser();

        ContentHandler handler = new BodyContentHandler();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();

        parser.parse(
                TikaInputStream.get(getTheoraVorbisFile()), handler,
                metadata, context
        );

        // TODO Fix all this

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
        assertEquals("0.02", metadata.get(XMPDM.DURATION));

        assertEquals("Encoded with Speex 1.2rc1", metadata.get(XMP.CREATOR_TOOL));
        assertEquals("Encoded with Speex 1.2rc1", metadata.get("vendor"));
        assertEquals("Speex 1 - 1.2rc1", metadata.get("version"));

        // Check text
        String content = handler.toString();
        assertTrue(content.contains("Test Title"));
        assertTrue(content.contains("Test Artist"));
        assertTrue(content.contains("Test Album"));
        assertTrue(content.contains("2010"));
        assertTrue(content.contains("Test Comment"));
        assertTrue(content.contains("Test Genre"));
        assertTrue(content.contains("00:00:00.02"));
    }

    // TODO Test the remaining kinds of files
    // TODO Test for soundtrack contents
}
