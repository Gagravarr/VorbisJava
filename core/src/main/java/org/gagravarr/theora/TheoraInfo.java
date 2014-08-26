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
package org.gagravarr.theora;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * The identification header identifies the bitstream as Theora, 
 *  and includes the Theora version, the ?????
 */
public class TheoraInfo extends HighLevelOggStreamPacket implements TheoraPacket {
    private int majorVersion;
    private int minorVersion;
    private int revisionVersion;

    private int frameWidthMB;
    private int frameHeightMB;
    private long frameNumSuperBlocks;
    private long frameNumBlocks;
    private long frameNumMacroBlocks;

    public TheoraInfo() {
        super();
        majorVersion = 3;
        minorVersion = 2;
        revisionVersion = 1;
    }

    public TheoraInfo(OggPacket pkt) {
        super(pkt);

        // Parse
        byte[] data = getData();

        majorVersion = (int)data[8];
        minorVersion = (int)data[9];
        revisionVersion = (int)data[10];
        if (majorVersion != 3) {
            throw new IllegalArgumentException("Unsupported Theora version " + getVersion() + " detected");
        }

        frameWidthMB  = IOUtils.getInt2(data, 11);
        frameHeightMB = IOUtils.getInt2(data, 13);
        frameNumSuperBlocks = IOUtils.getInt4(data, 15);
        frameNumBlocks      = IOUtils.getInt4(data, 19);
        frameNumMacroBlocks = IOUtils.getInt4(data, 23);

        // TODO The rest
    }

    @Override
    public OggPacket write() {
        byte[] data = new byte[30]; // Is this right?
        TheoraPacketFactory.populateMetadataHeader(data, TYPE_IDENTIFICATION, data.length);

        data[8] = IOUtils.fromInt(majorVersion);
        data[9] = IOUtils.fromInt(minorVersion);
        data[10] = IOUtils.fromInt(revisionVersion);
        IOUtils.putInt2(data, 11, frameWidthMB);
        IOUtils.putInt2(data, 13, frameHeightMB);
        IOUtils.putInt4(data, 15, frameNumSuperBlocks);
        IOUtils.putInt4(data, 19, frameNumBlocks);
        IOUtils.putInt4(data, 23, frameNumMacroBlocks);

        // TODO The rest

        data[29] = 1;

        setData(data);
        return super.write();
    }

    public String getVersion() {
        return majorVersion + "." + minorVersion + "." + revisionVersion;
    }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }
    public int getRevisionVersion() {
        return revisionVersion;
    }

    /**
     * The width of a frame, in Macro Blocks
     */
    public int getFrameWidthMB() {
        return frameWidthMB;
    }
    public void setFrameWidthMB(int frameWidthMB) {
        this.frameWidthMB = frameWidthMB;
    }

    /**
     * The height of a frame, in Macro Blocks
     */
    public int getFrameHeightMB() {
        return frameHeightMB;
    }
    public void setFrameHeightMB(int frameHeightMB) {
        this.frameHeightMB = frameHeightMB;
    }

    /**
     * The number of super blocks in a frame
     */
    public long getFrameNumSuperBlocks() {
        return frameNumSuperBlocks;
    }
    public void setFrameNumSuperBlocks(long frameNumSuperBlocks) {
        this.frameNumSuperBlocks = frameNumSuperBlocks;
    }

    /**
     * The number of blocks in a frame
     */
    public long getFrameNumBlocks() {
        return frameNumBlocks;
    }
    public void setFrameNumBlocks(long frameNumBlocks) {
        this.frameNumBlocks = frameNumBlocks;
    }

    /**
     * The number of marco blocks in a frame
     */
    public long getFrameNumMacroBlocks() {
        return frameNumMacroBlocks;
    }
    public void setFrameNumMacroBlocks(long frameNumMacroBlocks) {
        this.frameNumMacroBlocks = frameNumMacroBlocks;
    }

}
