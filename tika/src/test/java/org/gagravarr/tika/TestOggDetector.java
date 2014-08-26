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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.gagravarr.ogg.AbstractIdentificationTest;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketWriter;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.speex.SpeexInfo;
import org.gagravarr.vorbis.VorbisInfo;

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
                VorbisParser.OGG_VORBIS,
                d.detect(TikaInputStream.get(getTestVorbisFile()), m)
        );

        // Various Ogg Audio based formats
        assertEquals(
                VorbisParser.OGG_VORBIS,
                d.detect(TikaInputStream.get(getTestVorbisFile()), m)
        );
        assertEquals(
                SpeexParser.SPEEX_AUDIO,
                d.detect(TikaInputStream.get(getTestSpeexFile()), m)
        );
        assertEquals(
                OpusParser.OPUS_AUDIO,
                d.detect(TikaInputStream.get(getTestOpusFile()), m)
        );
        assertEquals(
                FlacParser.OGG_FLAC,
                d.detect(TikaInputStream.get(getTestFlacOggFile()), m)
        );
        assertEquals(
                OggDetector.OGG_GENERAL, 
                d.detect(TikaInputStream.get(getTestOggFile()), m)
        );

        // Various Ogg Video based formats
        assertEquals(
                TheoraParser.THEORA_VIDEO,
                d.detect(TikaInputStream.get(getTestTheoraFile()), m)
        );
        assertEquals(
                TheoraParser.THEORA_VIDEO,
                d.detect(TikaInputStream.get(getTestTheoraSkeletonFile()), m)
        );
        assertEquals(
                TheoraParser.THEORA_VIDEO,
                d.detect(TikaInputStream.get(getTestTheoraSkeletonCMMLFile()), m)
        );
        // TODO More video formats

        // Kate-only stream
        assertEquals(
                OggParser.KATE,
                d.detect(TikaInputStream.get(getTestKateFile()), m)
        );

        
        // Files with a mixture of things

        // Two Vorbis Streams counts as still vorbis
        assertEquals(
                VorbisParser.OGG_VORBIS,
                d.detect(TikaInputStream.get(getDoubleVorbis()), m)
        );
        // Two Opus Streams counts as still opus
        assertEquals(
                OpusParser.OPUS_AUDIO,
                d.detect(TikaInputStream.get(getDoubleOpus()), m)
        );
        // Three different audio streams is just general audio
        assertEquals(
                OggDetector.OGG_AUDIO,
                d.detect(TikaInputStream.get(getVorbisOpusSpeex()), m)
        );
        // Double theora is theora
        assertEquals(
                TheoraParser.THEORA_VIDEO,
                d.detect(TikaInputStream.get(getDoubleTheora()), m)
        );
        // Theora plus two differnt audio streams is theora
        assertEquals(
                TheoraParser.THEORA_VIDEO,
                d.detect(TikaInputStream.get(getTheoraVorbisOpus()), m)
        );
        // Theora plus Dirac plus Audio is just general video
        assertEquals(
                OggDetector.OGG_VIDEO,
                d.detect(TikaInputStream.get(getTheoraDiracOpus()), m)
        );
        // Kate + Audio is that audio
        assertEquals(
                VorbisParser.OGG_VORBIS,
                d.detect(TikaInputStream.get(getTestKateVorbisFile()), m)
        );


        // Non-Ogg files

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

    // These fake up mixed streams
    protected InputStream getDoubleVorbis() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(out);

        OggPacketWriter w1 = ogg.getPacketWriter();
        OggPacketWriter w2 = ogg.getPacketWriter();
        w1.bufferPacket(new VorbisInfo().write(), true);
        w2.bufferPacket(new VorbisInfo().write(), true);
        w1.close();
        w2.close();
        ogg.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
    protected InputStream getDoubleOpus() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(out);

        OggPacketWriter w1 = ogg.getPacketWriter();
        OggPacketWriter w2 = ogg.getPacketWriter();
        w1.bufferPacket(new OpusInfo().write(), true);
        w2.bufferPacket(new OpusInfo().write(), true);
        w1.close();
        w2.close();
        ogg.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
    protected InputStream getVorbisOpusSpeex() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(out);

        OggPacketWriter w1 = ogg.getPacketWriter();
        OggPacketWriter w2 = ogg.getPacketWriter();
        OggPacketWriter w3 = ogg.getPacketWriter();
        w1.bufferPacket(new VorbisInfo().write(), true);
        w2.bufferPacket(new OpusInfo().write(), true);
        w3.bufferPacket(new SpeexInfo().write(), true);
        w1.close();
        w2.close();
        w3.close();
        ogg.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    protected OggPacket fakeTheoraPacket() {
        byte[] data = new byte[80];
        data[0] = (byte)0x80;
        IOUtils.putUTF8(data, 1, "theora");
        return new OggPacket(data);
    }
    protected OggPacket fakeDiracPacket() {
        byte[] data = new byte[80];
        IOUtils.putUTF8(data, 0, "BBCD");
        return new OggPacket(data);
    }
    protected InputStream getDoubleTheora() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(out);

        OggPacketWriter w1 = ogg.getPacketWriter();
        OggPacketWriter w2 = ogg.getPacketWriter();
        w1.bufferPacket(fakeTheoraPacket(), true);
        w2.bufferPacket(fakeTheoraPacket(), true);
        w1.close();
        w2.close();
        ogg.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
    protected InputStream getTheoraVorbisOpus() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(out);

        OggPacketWriter w1 = ogg.getPacketWriter();
        OggPacketWriter w2 = ogg.getPacketWriter();
        OggPacketWriter w3 = ogg.getPacketWriter();
        w1.bufferPacket(fakeTheoraPacket(), true);
        w2.bufferPacket(new VorbisInfo().write(), true);
        w3.bufferPacket(new OpusInfo().write(), true);
        w1.close();
        w2.close();
        w3.close();
        ogg.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
    protected InputStream getTheoraDiracOpus() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(out);

        OggPacketWriter w1 = ogg.getPacketWriter();
        OggPacketWriter w2 = ogg.getPacketWriter();
        OggPacketWriter w3 = ogg.getPacketWriter();
        w1.bufferPacket(fakeTheoraPacket(), true);
        w2.bufferPacket(fakeDiracPacket(), true);
        w3.bufferPacket(new OpusInfo().write(), true);
        w1.close();
        w2.close();
        w3.close();
        ogg.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
