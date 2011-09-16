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
package org.gagravarr.vorbis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for round-tripping with VorbisFile
 * @author nick
 *
 */
public class TestVorbisFileWrite extends TestCase {
	private InputStream getTestFile() throws IOException {
		return this.getClass().getResourceAsStream("/testVORBIS.ogg");
	}
	
	public void testReadWrite() throws IOException {
		OggFile in = new OggFile(getTestFile());
		VorbisFile vfIN = new VorbisFile(in);
		
		int infoSize = vfIN.getInfo().getData().length;
		int commentSize = vfIN.getComment().getData().length;
		int setupSize = vfIN.getSetup().getData().length;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		VorbisFile vfOUT = new VorbisFile(
				baos,
				vfIN.getInfo(),
				vfIN.getComment(),
				vfIN.getSetup()
		);
		
		VorbisAudioData vad;
		while( (vad = vfIN.getNextAudioPacket()) != null ) {
			vfOUT.writeAudioData(vad);
		}
		
		vfIN.close();
		vfOUT.close();
		
		assertEquals(infoSize, vfOUT.getInfo().getData().length);
		assertEquals(commentSize, vfOUT.getComment().getData().length);
		assertEquals(setupSize, vfOUT.getSetup().getData().length);
	}
	
	public void testReadWriteRead() throws IOException {
		OggFile in = new OggFile(getTestFile());
		VorbisFile vfOrig = new VorbisFile(in);
		
		int infoSize = vfOrig.getInfo().getData().length;
		int commentSize = vfOrig.getComment().getData().length;
		int setupSize = vfOrig.getSetup().getData().length;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		VorbisFile vfOUT = new VorbisFile(
				baos,
				vfOrig.getInfo(),
				vfOrig.getComment(),
				vfOrig.getSetup()
		);
		
		VorbisAudioData vad;
		while( (vad = vfOrig.getNextAudioPacket()) != null ) {
			vfOUT.writeAudioData(vad);
		}
		
		vfOrig.close();
		vfOUT.close();
		
		
		// Now open the new one
		VorbisFile vfIN = new VorbisFile(new OggFile(
				new ByteArrayInputStream(baos.toByteArray())
		));
		
		// And check
		assertEquals(2, vfIN.getInfo().getChannels());
		assertEquals(44100, vfIN.getInfo().getRate());
		
		assertEquals(0, vfIN.getInfo().getBitrateLower());
		assertEquals(0, vfIN.getInfo().getBitrateUpper());
		assertEquals(80000, vfIN.getInfo().getBitrateNominal());
		
		assertEquals("Test Title", vfIN.getComment().getTitle());
		assertEquals("Test Artist", vfIN.getComment().getArtist());
		
		// Has audio data
		assertNotNull( vfIN.getNextAudioPacket() );
		assertNotNull( vfIN.getNextAudioPacket() );
		assertNotNull( vfIN.getNextAudioPacket() );
		assertNotNull( vfIN.getNextAudioPacket() );
		
		VorbisAudioData ad = vfIN.getNextAudioPacket();
		assertEquals(0x3c0, ad.getGranulePosition());
		

		// Check the core packets stayed the same size
		assertEquals(infoSize, vfOUT.getInfo().getData().length);
		assertEquals(commentSize, vfOUT.getComment().getData().length);
		assertEquals(setupSize, vfOUT.getSetup().getData().length);
		
		assertEquals(infoSize, vfIN.getInfo().getData().length);
		assertEquals(commentSize, vfIN.getComment().getData().length);
		assertEquals(setupSize, vfIN.getSetup().getData().length);
	}
}
