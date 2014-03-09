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
package org.gagravarr.opus;

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * The identification header identifies the bitstream as Opus, 
 *  and includes the Opus version, the simple audio characteristics 
 *  of the stream such as sample rate and number of channels etc.
 */
public class OpusInfo extends OpusPacket {
    private byte version;
    private int majorVersion;
    private int minorVersion;

    private int channels;

    public OpusInfo() {
        super();
        version = 1;
    }

    public OpusInfo(OggPacket pkt) {
        super(pkt);

        // Parse
        byte[] data = getData();
        version = data[8];
        parseVersion();
        if(majorVersion != 0) {
            throw new IllegalArgumentException("Unsupported Opus version " + version + " at major version " + majorVersion + " detected");
        }

        channels = (int)data[9];
        // Rest TODO
    }

    @Override
    public OggPacket write() {
        byte[] data = new byte[30];
        IOUtils.putUTF8(data, 0, MAGIC_HEADER);

        data[8] = version;
        data[9] = (byte)channels; 

        // Rest TODO
        
        setData(data);
        return super.write();
    }
    
    private void parseVersion() {
        minorVersion = version & 0xf;
        majorVersion = version >> 4;
    }

    public byte getVersion() {
        return version;
    }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }

    public int getChannels() {
        return channels;
    }
    public void setChannels(int channels) {
        this.channels = channels;
    }
}
