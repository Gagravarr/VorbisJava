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
package org.gagravarr.skeleton;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * The Fisbone (note - no h) provides details about
 *  what the other streams in the file are
 */
public class SkeletonFisbone extends HighLevelOggStreamPacket implements SkeletonPacket {
    private static final int MESSAGE_HEADER_OFFSET = 52 - MAGIC_FISBONE_BYTES.length;

    private int messageHeaderOffset;
    private int serialNumber;
    private int numHeaderPackets;
    private long granulerateNumerator;
    private long granulerateDenominator;
    private long baseGranule;
    private int preroll;
    private byte granuleShift;

    // TODO Message Headers

    public SkeletonFisbone() {
        super();
        messageHeaderOffset = MESSAGE_HEADER_OFFSET;
    }

    public SkeletonFisbone(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_FISBONE_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Skeleton Fisbone Header");
        }

        // Parse
        messageHeaderOffset = (int)IOUtils.getInt4(data, 8);
        if (messageHeaderOffset != MESSAGE_HEADER_OFFSET) {
            throw new IllegalArgumentException("Unsupported Skeleton message offset " + messageHeaderOffset + " detected");
        }

        serialNumber = (int)IOUtils.getInt4(data, 12);
        numHeaderPackets = (int)IOUtils.getInt4(data, 16);
        granulerateNumerator = IOUtils.getInt8(data, 20);
        granulerateDenominator = IOUtils.getInt8(data, 28);
        baseGranule = IOUtils.getInt8(data, 36);
        preroll = (int)IOUtils.getInt4(data, 44);
        granuleShift = data[48];
        // Next 3 are padding

        // TODO Message headers from 52+
    }

    @Override
    public OggPacket write() {
        // TODO How to work out how big message headers are?
        int size = 52;

        byte[] data = new byte[size];
        System.arraycopy(MAGIC_FISBONE_BYTES, 0, data, 0, 8);

        IOUtils.putInt4(data, 8, messageHeaderOffset);
        IOUtils.putInt4(data, 12, serialNumber);
        IOUtils.putInt4(data, 16, numHeaderPackets);
        IOUtils.putInt8(data, 20, granulerateNumerator);
        IOUtils.putInt8(data, 28, granulerateDenominator);
        IOUtils.putInt8(data, 36, baseGranule);
        IOUtils.putInt4(data, 44, preroll);
        data[48] = granuleShift;
        // Next 3 are zero padding

        // TODO Message Headers from 52+

        setData(data);
        return super.write();
    }

    public int getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getNumHeaderPackets() {
        return numHeaderPackets;
    }
    public void setNumHeaderPackets(int numHeaderPackets) {
        this.numHeaderPackets = numHeaderPackets;
    }

    public long getGranulerateNumerator() {
        return granulerateNumerator;
    }
    public void setGranulerateNumerator(long granulerateNumerator) {
        this.granulerateNumerator = granulerateNumerator;
    }

    public long getGranulerateDenominator() {
        return granulerateDenominator;
    }
    public void setGranulerateDenominator(long granulerateDenominator) {
        this.granulerateDenominator = granulerateDenominator;
    }

    public long getBaseGranule() {
        return baseGranule;
    }
    public void setBaseGranule(long baseGranule) {
        this.baseGranule = baseGranule;
    }

    public int getPreroll() {
        return preroll;
    }
    public void setPreroll(int preroll) {
        this.preroll = preroll;
    }

    public byte getGranuleShift() {
        return granuleShift;
    }
    public void setGranuleShift(byte granuleShift) {
        this.granuleShift = granuleShift;
    }
}
