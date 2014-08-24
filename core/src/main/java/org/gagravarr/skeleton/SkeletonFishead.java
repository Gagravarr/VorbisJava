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
 * The Fishead (note - one h) provides some basic information
 *  on the Skeleton / Annodex stream
 */
public class SkeletonFishead extends HighLevelOggStreamPacket implements SkeletonPacket {
    private int versionMajor;
    private int versionMinor;
    private long presentationTimeNumerator;
    private long presentationTimeDenominator;
    private long baseTimeNumerator;
    private long baseTimeDenominator;
    private long utc1; // ???
    private long utc2; // ???
    private long utc3; // ???
    private long segmentLength; // v4 only
    private long contentOffset; // v4 only
    
    public SkeletonFishead() {
        super();
        versionMajor = 4;
        versionMinor = 0;
    }

    public SkeletonFishead(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_FISHEAD_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Skeleton Fishead Header");
        }

        // Parse
        versionMajor = IOUtils.getInt2(data, 8);
        versionMinor = IOUtils.getInt2(data, 10);
        if (versionMajor < 3 || versionMajor > 4) {
            throw new IllegalArgumentException("Unsupported Skeleton version " + versionMajor + " detected");
        }

        presentationTimeNumerator   = IOUtils.getInt8(data, 12);
        presentationTimeDenominator = IOUtils.getInt8(data, 20);
        baseTimeNumerator   = IOUtils.getInt8(data, 28);
        baseTimeDenominator = IOUtils.getInt8(data, 36);
        utc1 = IOUtils.getInt8(data, 44);
        utc2 = IOUtils.getInt8(data, 52);
        utc3 = IOUtils.getInt4(data, 60);

        if (versionMajor == 4) {
            segmentLength = IOUtils.getInt8(data, 64);
            contentOffset = IOUtils.getInt8(data, 72);
        }
    }

    @Override
    public OggPacket write() {
        int len = 64;
        if (versionMajor == 4) {
            len = 80;
        }
        byte[] data = new byte[len];

        IOUtils.putUTF8(data, 0, MAGIC_FISHEAD_STR);
        IOUtils.putInt2(data, 8, versionMajor);
        IOUtils.putInt2(data, 10, versionMinor);

        IOUtils.putInt8(data, 12, presentationTimeNumerator);
        IOUtils.putInt8(data, 20, presentationTimeDenominator);
        IOUtils.putInt8(data, 28, baseTimeNumerator);
        IOUtils.putInt8(data, 36, baseTimeDenominator);

        IOUtils.putInt8(data, 44, utc1);
        IOUtils.putInt8(data, 52, utc2);
        IOUtils.putInt4(data, 60, utc3);

        if (versionMajor == 4) {
            IOUtils.putInt8(data, 64, segmentLength);
            IOUtils.putInt8(data, 72, contentOffset);
        }

        setData(data);
        return super.write();
    }

    public int getVersionMajor() {
        return versionMajor;
    }
    public void setVersionMajor(int versionMajor) {
        this.versionMajor = versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }
    public void setVersionMinor(int versionMinor) {
        this.versionMinor = versionMinor;
    }

    public String getVersion() {
        return versionMajor + "." + versionMinor;
    }

    public long getPresentationTimeNumerator() {
        return presentationTimeNumerator;
    }
    public void setPresentationTimeNumerator(long presentationTimeNumerator) {
        this.presentationTimeNumerator = presentationTimeNumerator;
    }

    public long getPresentationTimeDenominator() {
        return presentationTimeDenominator;
    }
    public void setPresentationTimeDenominator(long presentationTimeDenominator) {
        this.presentationTimeDenominator = presentationTimeDenominator;
    }

    public long getBaseTimeNumerator() {
        return baseTimeNumerator;
    }
    public void setBaseTimeNumerator(long baseTimeNumerator) {
        this.baseTimeNumerator = baseTimeNumerator;
    }

    public long getBaseTimeDenominator() {
        return baseTimeDenominator;
    }
    public void setBaseTimeDenominator(long baseTimeDenominator) {
        this.baseTimeDenominator = baseTimeDenominator;
    }

    public long getUtc1() {
        return utc1;
    }
    public void setUtc1(long utc1) {
        this.utc1 = utc1;
    }

    public long getUtc2() {
        return utc2;
    }
    public void setUtc2(long utc2) {
        this.utc2 = utc2;
    }

    public long getUtc3() {
        return utc3;
    }
    public void setUtc3(long utc3) {
        this.utc3 = utc3;
    }

    public void getUtc() {
        return null; // TODO
    }

    public long getSegmentLength() {
        return segmentLength;
    }
    public void setSegmentLength(long segmentLength) {
        this.segmentLength = segmentLength;
    }

    public long getContentOffset() {
        return contentOffset;
    }
    public void setContentOffset(long contentOffset) {
        this.contentOffset = contentOffset;
    }
}
