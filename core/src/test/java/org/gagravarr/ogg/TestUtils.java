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
package org.gagravarr.ogg;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Tests for our utils / helpers
 */
public class TestUtils extends TestCase {
    public void testBitsReaderSimple() throws IOException {
        BitsReader br = new BitsReader(new ByteArrayInputStream(
                new byte[] { (byte)0x80, (byte)0xc0, (byte)0xe0, (byte)0xf0,
                        (byte)0xf7, (byte)0xff, 7, 3, 1
        }));
        // 0x80 = 1000 0000
        assertEquals(1, br.read(1));
        assertEquals(0, br.read(1));
        assertEquals(0, br.read(1));
        assertEquals(0, br.read(1));
        assertEquals(0, br.read(4));
        // 0xc0 = 1100 0000
        assertEquals(1, br.read(1));
        assertEquals(1, br.read(1));
        assertEquals(0, br.read(1));
        assertEquals(0, br.read(4));
        assertEquals(0, br.read(1));
        // 0xe0 = 1110 0000
        assertEquals(3, br.read(2));
        assertEquals(1, br.read(1));
        assertEquals(0, br.read(1));
        assertEquals(0, br.read(4));
        // 0xf0 = 1111 0000
        assertEquals(3, br.read(2));
        assertEquals(3, br.read(2));
        assertEquals(0, br.read(4));
        // 0xf7 = 1111 0111
        assertEquals(7, br.read(3));
        assertEquals(5, br.read(3));
        assertEquals(3, br.read(2));
        // 0xff = 1111 1111
        assertEquals(255, br.read(8));
        // 7
        assertEquals(0, br.read(3));
        assertEquals(1, br.read(3));
        assertEquals(3, br.read(2));
        // 3
        assertEquals(0, br.read(1));
        assertEquals(1, br.read(6));
        assertEquals(1, br.read(1));
        // 1
        assertEquals(0, br.read(1));
        assertEquals(0, br.read(6));
        assertEquals(1, br.read(1));
    }
}
