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
import org.gagravarr.ogg.OggPacketReader;

public class TestFlacComments extends TestCase {
	private InputStream getTestOggFile() throws IOException {
		return this.getClass().getResourceAsStream("/testFLAC.oga");
	}
   private InputStream getTestFlacFile() throws IOException {
      return this.getClass().getResourceAsStream("/testFLAC.flac");
   }
	
	public void testReadOgg() throws IOException {
	   // Try low level
		OggFile ogg = new OggFile(getTestOggFile());
		OggPacketReader r = ogg.getPacketReader();

		// Should be in the 2nd packet
		r.getNextPacket();
		FlacTags tags = new FlacTags(r.getNextPacket());
		doTestComments(tags);
		
		// Now direct from the file
		ogg = new OggFile(getTestOggFile());
		FlacOggFile flac = new FlacOggFile(ogg);
      doTestComments(flac.getTags());
	}
   public void testReadFlac() throws IOException {
      FlacNativeFile flac = new FlacNativeFile(getTestFlacFile());
      doTestComments(flac.getTags());
   }
    
   private void doTestComments(FlacTags tags) {
      assertEquals("Xiph.Org libVorbis I 20070622", tags.getVendor());
      
      assertEquals(7, tags.getAllComments().size());
      
      assertEquals("Test Title", tags.getTitle());
      assertEquals(1, tags.getComments("TiTlE").size());
      assertEquals("Test Title", tags.getComments("tItLe").get(0));
      
      assertEquals("Test Artist", tags.getArtist());
      assertEquals(1, tags.getComments("ArTiSt").size());
      assertEquals("Test Artist", tags.getComments("aRTiST").get(0));
      
      assertEquals("Test Genre", tags.getGenre());
      assertEquals(1, tags.getComments("GEnRE").size());
      assertEquals("Test Genre", tags.getComments("genRE").get(0));
      
      assertEquals("Test Album", tags.getAlbum());
      assertEquals(1, tags.getComments("alBUm").size());
      assertEquals("Test Album", tags.getComments("ALbUM").get(0));
      
      assertEquals(1, tags.getComments("DAte").size());
      assertEquals("2010-01-26", tags.getComments("dAtE").get(0));
      
      assertEquals(2, tags.getComments("COmmENT").size());
      assertEquals("Test Comment", tags.getComments("COMMent").get(0));
      assertEquals("Another Test Comment", tags.getComments("COMMent").get(1));
   }
	
	// TODO Write related tests
}
