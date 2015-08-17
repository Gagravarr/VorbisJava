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
package org.gagravarr.flac;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;

/**
 * Tests for reading things using VorbisFile
 * @author nick
 *
 */
public class TestFlacFileRead extends TestCase {
   private InputStream getTestOggFile() throws IOException {
      return this.getClass().getResourceAsStream("/testFLAC.oga");
   }
   private InputStream getTestFlacFile() throws IOException {
      return this.getClass().getResourceAsStream("/testFLAC.flac");
   }

   private FlacFile flac;
   @Override
   protected void tearDown() throws IOException {
       if (flac != null) {
           flac.close();
       }
   }

   public void testReadOgg() throws IOException {
      OggFile ogg = new OggFile(getTestOggFile());
      flac = new FlacOggFile(ogg);

      // Check the first packet for general info
      FlacFirstOggPacket first = ((FlacOggFile)flac).getFirstPacket();
      assertNotNull(first);
      assertEquals(1, first.getMajorVersion());
      assertEquals(0, first.getMinorVersion());

      // Check the info
      FlacInfo info = flac.getInfo();
      assertNotNull(info);
      
      assertEquals(0x1000, info.getMinimumBlockSize());
      assertEquals(0x1000, info.getMaximumBlockSize());
      assertEquals(0x084e, info.getMinimumFrameSize());
      assertEquals(0x084e, info.getMaximumFrameSize());
      
      assertEquals(44100, info.getSampleRate());
      assertEquals(16, info.getBitsPerSample());
      assertEquals(2, info.getNumChannels());
      assertEquals(0x3c0, info.getNumberOfSamples());

      // Check the basics of the comments
      FlacTags tags = flac.getTags();
      assertNotNull(tags);
      assertEquals(7, tags.getAllComments().size());
      assertEquals("Test Album", tags.getAlbum());

      // TODO Check other metadata


      // Has audio data, all with info-based metadata
      FlacAudioFrame audio;

      audio = flac.getNextAudioPacket();
      assertNotNull(audio);
      assertEquals(0, audio.getSampleRate());
      assertEquals(0, audio.getBlockSize());
      //assertEquals(0x3c0, ad.getGranulePosition()); // TODO Check granule

      // TODO Is this right? Only a single audio frame
      assertNull( flac.getNextAudioPacket() );
   }

   public void testReadFlacNative() throws IOException {
      flac = new FlacNativeFile(getTestFlacFile());

      // Check the info
      FlacInfo info = flac.getInfo();
      assertNotNull(info);
      
      assertEquals(0x1000, info.getMinimumBlockSize());
      assertEquals(0x1000, info.getMaximumBlockSize());
      assertEquals(0x084e, info.getMinimumFrameSize());
      assertEquals(0x084e, info.getMaximumFrameSize());
      
      assertEquals(44100, info.getSampleRate());
      assertEquals(16, info.getBitsPerSample());
      assertEquals(2, info.getNumChannels());
      assertEquals(0x3c0, info.getNumberOfSamples());

      // Check the basics of the comments
      FlacTags tags = flac.getTags();
      assertNotNull(tags);
      assertEquals(7, tags.getAllComments().size());
      assertEquals("Test Album", tags.getAlbum());


      // Has audio data, all with info-based metadata
      FlacAudioFrame audio;

      audio = flac.getNextAudioPacket();
      assertNotNull(audio);
      assertEquals(0, audio.getSampleRate());
      assertEquals(0, audio.getBlockSize());
      //assertEquals(0x3c0, ad.getGranulePosition()); // TODO Check granule

      // TODO Check the rest
   }
}
