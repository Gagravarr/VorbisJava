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

import org.gagravarr.flac.FlacAudioSubFrame.SubFrameFixed;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;

/**
 * Tests for reading things using FlacFile, both for
 *  Flac-Native and Flac-in-Ogg
 * 
 * TODO Lengths and/or Granules
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

   /**
    * Test that the first audio frame used all the data
    *  from it's ogg packet, and it's all still available
    *  unchanged for if you wanted to write it back out again
    */
   public void testOggFirstAudioFrame() throws IOException {
      // Find the first audio packet
      OggFile ogg = new OggFile(getTestOggFile());
      OggPacketReader r = ogg.getPacketReader();
      // Will have Info then tags
      OggPacket p = r.getNextPacket();
      FlacInfo info = (new FlacFirstOggPacket(p)).getInfo();
      p = r.getNextPacket();
      // Next are other metadata
      while ((p = r.getNextPacket()) != null) {
          if (p.getData()[0] == -1) break;
      }
      // Decode audio as Flac
      FlacAudioFrame audio = new FlacAudioFrame(p.getData(), info);
      // Check same
      assertEquals(p.getData().length, audio.getData().length);
   }

   public void testReadOgg() throws IOException {
      OggFile ogg = new OggFile(getTestOggFile());
      flac = new FlacOggFile(ogg);

      // Check the first packet for general info
      FlacFirstOggPacket first = ((FlacOggFile)flac).getFirstPacket();
      assertNotNull(first);
      assertEquals(1, first.getMajorVersion());
      assertEquals(0, first.getMinorVersion());

      // Check the rest
      assertFlacContents(flac);
   }

   public void testReadFlacNative() throws IOException {
       flac = new FlacNativeFile(getTestFlacFile());

       // TODO Check some native-specific parts

       // Check the rest
       assertFlacContents(flac);
   }

   /**
    * Checks that the right information is stored in the file,
    *  both header and audio contents
    * Information for these tests generated from running "flac -a"
    *  against the test files.
    */
   protected void assertFlacContents(FlacFile flac) throws IOException {
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


      // Has audio data, all with mostly info-based metadata
      FlacAudioFrame audio;
      FlacAudioSubFrame sf;
      SubFrameFixed sff;

      audio = flac.getNextAudioPacket();
      assertNotNull(audio);
      assertEquals(0, audio.getCodedNumber());
      assertEquals(44100, audio.getSampleRate());
      assertEquals(960, audio.getBlockSize());
      assertEquals(16, audio.getBitsPerSample());
      assertEquals(2, audio.getNumChannels());
      //assertEquals(0x3c0, ad.getGranulePosition()); // TODO Check granule

      // Should have one subframe per channel
      // First should be Fixed with Warmup, Second Fixed no Warmup
      // (You can check this using "flac -a" on the test files)
      assertEquals(2, audio.getSubFrames().length);

      sf = audio.getSubFrames()[0];
      assertEquals(SubFrameFixed.class, sf.getClass());
      assertEquals(0, sf.getWastedBits());
      sff = (SubFrameFixed)sf;
      assertEquals(1, sff.predictorOrder);
      assertEquals(1, sff.warmUpSamples.length);
      assertEquals(1, sff.warmUpSamples[0]);
      assertEquals(FlacAudioSubFrame.SubFrameResidualRice.class, sff.residual.getClass());
      assertEquals(1, sff.residual.numPartitions);
      assertEquals(1, sff.residual.riceParams[0]);

      sf = audio.getSubFrames()[1];
      assertEquals(SubFrameFixed.class, sf.getClass());
      assertEquals(0, sf.getWastedBits());
      sff = (SubFrameFixed)sf;
      assertEquals(0, sff.predictorOrder);
      assertEquals(0, sff.warmUpSamples.length);
      assertEquals(FlacAudioSubFrame.SubFrameResidualRice.class, sff.residual.getClass());
      assertEquals(1, sff.residual.numPartitions);
      assertEquals(13, sff.residual.riceParams[0]);

      // There is only a single audio frame
      assertNull( flac.getNextAudioPacket() );
   }
}
