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

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.gagravarr.ogg.AbstractIdentificationTest;

public class TestOggDetector extends AbstractIdentificationTest {
    public void testDetect() throws IOException {
        OggDetector d = new OggDetector();
        Metadata m = new Metadata();

        // Needs to be a TikaInputStream to be detected properly
        assertEquals(
                OggDetector.OGG_GENERAL,
                d.detect(new BufferedInputStream(getTestOggFile()), m)
        );
        assertEquals(
                OggDetector.OGG_VORBIS, 
                d.detect(TikaInputStream.get(getTestVorbisFile()), m)
        );

        // Various Ogg based formats
        assertEquals(
                OggDetector.OGG_VORBIS, 
                d.detect(TikaInputStream.get(getTestVorbisFile()), m)
        );
        assertEquals(
                OggDetector.SPEEX_AUDIO, 
                d.detect(TikaInputStream.get(getTestSpeexFile()), m)
        );
        assertEquals(
                OggDetector.OPUS_AUDIO, 
                d.detect(TikaInputStream.get(getTestOpusFile()), m)
        );
        assertEquals(
                OggDetector.OGG_FLAC,
                d.detect(TikaInputStream.get(getTestFlacOggFile()), m)
        );
        assertEquals(
                OggDetector.OGG_GENERAL, 
                d.detect(TikaInputStream.get(getTestOggFile()), m)
        );

        // TODO Video

        // It won't detect native FLAC files however
        assertEquals(
                MediaType.OCTET_STREAM, 
                d.detect(TikaInputStream.get(getTestFlacNativeFile()), m)
        );

        // Random junk is a Octet Stream too
        assertEquals(
                MediaType.OCTET_STREAM, 
                d.detect(TikaInputStream.get(getDummy()), m)
        );
    }
}
