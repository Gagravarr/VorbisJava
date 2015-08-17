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
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

/**
 * Test that we do the right things when writing out
 *  things around page boundaries
 */
public class TestWriteBoundaries extends TestCase {
    public static byte[] getBytes(int len) {
        byte[] b = new byte[len];
        for(int i=0; i<len; i++) {
            b[i] = IOUtils.fromInt(	i % 256 );
        }
        return b;
    }

    public void testMix() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(baos);
        OggPacketWriter w = ogg.getPacketWriter(0x123456);

        OggPacket p;

        p = new OggPacket(new byte[] {
                0, 1, 2, 3, 4, 5
        });
        w.bufferPacket(p, false);
        p = new OggPacket(new byte[] {
                10, 11, 12, 13, 14, 15
        });
        w.bufferPacket(p, true);

        p = new OggPacket(new byte[] {
                0, 1, 2, 3, 4, 5
        });
        w.bufferPacket(p, false);

        p = new OggPacket(getBytes(131072));
        w.bufferPacket(p, false);

        p = new OggPacket(new byte[] {
                0, 1, 2, 3, 4, 5
        });
        w.bufferPacket(p, false);
        w.close();


        ogg = new OggFile(new ByteArrayInputStream(baos.toByteArray()));
        OggPacketReader r = ogg.getPacketReader();

        p = r.getNextPacket();
        assertEquals(0, p.getSequenceNumber());
        assertEquals(6, p.getData().length);

        p = r.getNextPacket();
        assertEquals(0, p.getSequenceNumber());
        assertEquals(6, p.getData().length);

        p = r.getNextPacket();
        assertEquals(1, p.getSequenceNumber());
        assertEquals(6, p.getData().length);

        p = r.getNextPacket();
        assertEquals(3, p.getSequenceNumber()); // over 3 pages
        assertEquals(131072, p.getData().length);

        p = r.getNextPacket();
        assertEquals(3, p.getSequenceNumber());
        assertEquals(6, p.getData().length);

        p = r.getNextPacket();
        assertEquals(null, p);
    }

    public void testWriteBigPackets() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(baos);
        OggPacketWriter w = ogg.getPacketWriter(0x123456);

        OggPacket p;

        // Small
        p = new OggPacket(new byte[] {
                0, 1, 2, 3, 4, 5
        });
        w.bufferPacket(p, true);

        // Over 2-and-a-bit packets
        p = new OggPacket(getBytes(131072));
        w.bufferPacket(p, true);

        // Over 2-and-a-bit packets, no flush
        p = new OggPacket(getBytes(144077));
        w.bufferPacket(p, false);
        // Ditto
        p = new OggPacket(getBytes(161077));
        w.bufferPacket(p, false);
        // Over 3-and-a-bit packets
        p = new OggPacket(getBytes(231079));
        w.bufferPacket(p, false);

        // Small, flush
        p = new OggPacket(new byte[] {
                0, 1, 2, 3, 4, 5
        });
        w.bufferPacket(p, true);
        // Small, close
        p = new OggPacket(new byte[] {
                0, 1, 2, 3, 4, 5
        });
        w.bufferPacket(p, false);
        w.close();


        // Check
        ogg = new OggFile(new ByteArrayInputStream(baos.toByteArray()));
        OggPacketReader r = ogg.getPacketReader();

        p = r.getNextPacket();
        assertEquals(0, p.getSequenceNumber());
        assertEquals(6, p.getData().length);

        p = r.getNextPacket();
        assertEquals(3, p.getSequenceNumber()); // 1,2,3
        assertEquals(131072, p.getData().length);

        p = r.getNextPacket();
        assertEquals(6, p.getSequenceNumber()); // 4,5,6
        assertEquals(144077, p.getData().length);

        p = r.getNextPacket();
        assertEquals(8, p.getSequenceNumber()); // 6,7,8
        assertEquals(161077, p.getData().length);

        p = r.getNextPacket();
        assertEquals(12, p.getSequenceNumber()); // 9,10,11,12
        assertEquals(231079, p.getData().length);

        p = r.getNextPacket();
        assertEquals(12, p.getSequenceNumber());
        assertEquals(6, p.getData().length);

        p = r.getNextPacket();
        assertEquals(13, p.getSequenceNumber());
        assertEquals(6, p.getData().length);

        p = r.getNextPacket();
        assertEquals(null, p);
    }

    public void testGranulePosition() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OggFile ogg = new OggFile(baos);
        OggPacketWriter w = ogg.getPacketWriter(0x123456);

        OggPacket p;

        // First packet will be at 0, as that's the default
        p = new OggPacket(new byte[] {
                0, 1, 2, 3, 4, 5
        });
        w.bufferPacket(p, true);
        assertEquals(0, p._getParent().getGranulePosition());

        // One without buffering can be changed with a subsequent
        //  set before a flush
        p = new OggPacket(getBytes(131072)); // 2-and-a-bit pages
        w.bufferPacket(p);
        assertEquals(0, p._getParent().getGranulePosition());
        w.setGranulePosition(0x1111l);
        assertEquals(0x1111l, p._getParent().getGranulePosition());
        w.setGranulePosition(0x12345l);
        assertEquals(0x12345l, p._getParent().getGranulePosition());
        w.flush();

        // If we set the granule as we go, the last set on
        //  a given page wins
        p = new OggPacket(getBytes(15));
        w.bufferPacket(p, 0x23456l);
        p = new OggPacket(getBytes(21));
        w.bufferPacket(p, 0x34567l);
        p = new OggPacket(getBytes(144077));
        w.bufferPacket(p, 0x45678l);
        w.close();

        // Check
        ogg = new OggFile(new ByteArrayInputStream(baos.toByteArray()));
        OggPacketReader r = ogg.getPacketReader();

        // Empty packet/page at 0
        p = r.getNextPacket();
        assertEquals(0, p.getSequenceNumber());
        assertEquals(0, p.getGranulePosition());
        assertEquals(6, p.getData().length);

        // 2-and-a-bit packet all at last value
        p = r.getNextPacket();
        assertEquals(3, p.getSequenceNumber());
        assertEquals(0x12345l, p.getGranulePosition());
        assertEquals(131072, p.getData().length);

        // Next two both take value set on the
        //  last whole one in the page
        p = r.getNextPacket();
        assertEquals(4, p.getSequenceNumber());
        assertEquals(0x34567l, p.getGranulePosition());
        assertEquals(15, p.getData().length);

        p = r.getNextPacket();
        assertEquals(4, p.getSequenceNumber());
        assertEquals(0x34567l, p.getGranulePosition());
        assertEquals(21, p.getData().length);

        // While the one that spans that page and the
        //  next two gets its own value
        p = r.getNextPacket();
        assertEquals(6, p.getSequenceNumber());
        assertEquals(0x45678l, p.getGranulePosition());
        assertEquals(144077, p.getData().length);
    }
}
