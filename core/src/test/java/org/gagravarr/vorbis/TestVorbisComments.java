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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;

public class TestVorbisComments extends TestCase {
	private InputStream getTestFile() throws IOException {
		return this.getClass().getResourceAsStream("/testVORBIS.ogg");
	}
	
	public void testRead() throws IOException {
		OggFile ogg = new OggFile(getTestFile());
		OggPacketReader r = ogg.getPacketReader();

		// 2nd packet
		r.getNextPacket();
		VorbisPacket p = VorbisPacketFactory.create(r.getNextPacket());
		assertEquals(VorbisComments.class, p.getClass());
		
		VorbisComments c = (VorbisComments)p;
		assertEquals("Xiph.Org libVorbis I 20070622", c.getVendor());
		
		assertEquals(7, c.getAllComments().size());
		
		assertEquals("Test Title", c.getTitle());
		assertEquals(1, c.getComments("TiTlE").size());
		assertEquals("Test Title", c.getComments("tItLe").get(0));
		
		assertEquals("Test Artist", c.getArtist());
		assertEquals(1, c.getComments("ArTiSt").size());
		assertEquals("Test Artist", c.getComments("aRTiST").get(0));
		
		assertEquals("Test Genre", c.getGenre());
		assertEquals(1, c.getComments("GEnRE").size());
		assertEquals("Test Genre", c.getComments("genRE").get(0));
		
		assertEquals("Test Album", c.getAlbum());
		assertEquals(1, c.getComments("alBUm").size());
		assertEquals("Test Album", c.getComments("ALbUM").get(0));
		
		assertEquals(1, c.getComments("DAte").size());
		assertEquals("2010-01-26", c.getComments("dAtE").get(0));
		
		assertEquals(2, c.getComments("COmmENT").size());
		assertEquals("Test Comment", c.getComments("COMMent").get(0));
		assertEquals("Another Test Comment", c.getComments("COMMent").get(1));
	}
	
	public void testModify() throws IOException {
		VorbisComments c = new VorbisComments();
		
		assertEquals(0, c.getComments("a").size());
		assertEquals(0, c.getAllComments().size());
		assertEquals(null, c.getAlbum());
		
		c.addComment("a", "1");
		assertEquals(1, c.getComments("a").size());
		assertEquals("1", c.getComments("a").get(0));
		
		c.addComment("A", "2");
		c.addComment("a", "3");
		assertEquals(3, c.getComments("A").size());
		assertEquals("1", c.getComments("a").get(0));
		assertEquals("2", c.getComments("a").get(1));
		assertEquals("3", c.getComments("a").get(2));
		
		c.setComments("a", new ArrayList<String>());
		assertEquals(0, c.getComments("a").size());
		c.addComment("a", "F");
		assertEquals(1, c.getComments("a").size());
		assertEquals("F", c.getComments("a").get(0));
		
		c.removeComments("A");
		assertEquals(0, c.getComments("a").size());
		assertEquals(0, c.getAllComments().size());
	}
	
	public void testWrite() throws IOException {
		VorbisComments c = new VorbisComments();
		c.setVendor("A1");
		c.addComment("b", "c2a");
		c.addComment("B", "c2b");
		c.addComment("1", "c1");
		
		OggPacket p = c.write();
		assertEquals(7 + 6 + 4 + 8 + 9 + 9 + 1, p.getData().length);
		
		assertEquals(3, p.getData()[0]);
		assertEquals('v', p.getData()[1]);
		assertEquals('o', p.getData()[2]);
		assertEquals('r', p.getData()[3]);
		assertEquals('b', p.getData()[4]);
		assertEquals('i', p.getData()[5]);
		assertEquals('s', p.getData()[6]);
		
		assertEquals(2, p.getData()[7]);
		assertEquals(0, p.getData()[8]);
		assertEquals(0, p.getData()[9]);
		assertEquals(0, p.getData()[10]);
		assertEquals('A', p.getData()[11]);
		assertEquals('1', p.getData()[12]);
		
		assertEquals(3, p.getData()[13]);
		assertEquals(0, p.getData()[14]);
		assertEquals(0, p.getData()[15]);
		assertEquals(0, p.getData()[16]);
		
		assertEquals(4, p.getData()[17]);
		assertEquals(0, p.getData()[18]);
		assertEquals(0, p.getData()[19]);
		assertEquals(0, p.getData()[20]);
		assertEquals('1', p.getData()[21]);
		assertEquals('=', p.getData()[22]);
		assertEquals('c', p.getData()[23]);
		assertEquals('1', p.getData()[24]);
		
		assertEquals(5, p.getData()[25]);
		assertEquals(0, p.getData()[26]);
		assertEquals(0, p.getData()[27]);
		assertEquals(0, p.getData()[28]);
		assertEquals('b', p.getData()[29]);
		assertEquals('=', p.getData()[30]);
		assertEquals('c', p.getData()[31]);
		assertEquals('2', p.getData()[32]);
		assertEquals('a', p.getData()[33]);
		
		assertEquals(5, p.getData()[34]);
		assertEquals(0, p.getData()[35]);
		assertEquals(0, p.getData()[36]);
		assertEquals(0, p.getData()[37]);
		assertEquals('b', p.getData()[38]);
		assertEquals('=', p.getData()[39]);
		assertEquals('c', p.getData()[40]);
		assertEquals('2', p.getData()[41]);
		assertEquals('b', p.getData()[42]);
		
		assertEquals(1, p.getData()[43]);
	}
}
