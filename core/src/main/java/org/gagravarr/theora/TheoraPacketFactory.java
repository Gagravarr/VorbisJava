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
 * Identifies the right kind of {@link TheoraPacket} for a given
 *  incoming {@link OggPacket}, and creates it
 */
public class TheoraPacketFactory extends HighLevelOggStreamPacket {
    /**
     * Popupulates the metadata packet header,
     *  which is "#theora" where # is the type.
     */
    protected static void populateMetadataHeader(byte[] b, int type, int dataLength) {
        b[0] = IOUtils.fromInt(type);
        b[1] = (byte)'t';
        b[2] = (byte)'h';
        b[3] = (byte)'e';
        b[4] = (byte)'o';
        b[5] = (byte)'r';
        b[6] = (byte)'a';
    }

    /**
     * Does this packet (the first in the stream) contain
     *  the magic string indicating that it's an theora
     *  one?
     */
    public static boolean isTheoraStream(OggPacket firstPacket) {
        if(! firstPacket.isBeginningOfStream()) {
            return false;
        }
        return isTheoraSpecial(firstPacket);
    }

    protected static boolean isTheoraSpecial(OggPacket packet) {
        byte type = packet.getData()[0];

        // Ensure "theora" on the special types
        if(type >= (byte)0x80 && type <= (byte)0x82) {
            byte[] d = packet.getData();
            if(d[1] == (byte)'t' &&
                    d[2] == (byte)'h' &&
                    d[3] == (byte)'e' &&
                    d[4] == (byte)'o' &&
                    d[5] == (byte)'r' &&
                    d[6] == (byte)'a') {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the appropriate {@link TheoraPacket}
     *  instance based on the type.
     */
    public static TheoraPacket create(OggPacket packet) {
        // Special header types detection
        if(isTheoraSpecial(packet)) {
            byte type = packet.getData()[0];
            // TODO Identify and return
            return null;
        }

//       return new TheoraVideoData(packet);
        return null;
    }
}
