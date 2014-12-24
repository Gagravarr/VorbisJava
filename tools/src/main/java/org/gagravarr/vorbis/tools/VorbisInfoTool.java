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
package org.gagravarr.vorbis.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.vorbis.VorbisAudioData;
import org.gagravarr.vorbis.VorbisFile;

/**
 * A tool for looking at the innards of a Vorbis File
 */
public class VorbisInfoTool {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            printHelp();
        }

        boolean debugging = false;
        String filename = args[0];
        if(args.length > 1 && args[0].equals("-d")) {
            filename = args[1];
            debugging = true;
        }

        InfoPacketReader r = new InfoPacketReader(
                new FileInputStream(filename));
        VorbisFile vf = new VorbisFile(r);

        System.out.println("Processing file \"" + filename + "\"");

        System.out.println("");
        System.out.println("Vorbis Headers:");
        System.out.println("  Version: " + vf.getInfo().getVersion());
        System.out.println("  Vendor: " + vf.getComment().getVendor());
        System.out.println("  Channels: " + vf.getInfo().getChannels());
        System.out.println("  Rate: " + vf.getInfo().getRate());
        System.out.println("");
        System.out.println("  Nominal Bitrate: " + vf.getInfo().getBitrateNominal());
        System.out.println("  Lower Bitrate: " + vf.getInfo().getBitrateLower());
        System.out.println("  Upper Bitrate: " + vf.getInfo().getBitrateUpper());

        System.out.println("");
        System.out.println("User Comments:");
        listTags(vf);
        System.out.println("");

        System.out.println("Vorbis Setup:");
        System.out.println("  Codebooks: " + vf.getSetup().getNumberOfCodebooks());
        System.out.println("");

        VorbisAudioData vad;
        int dataPackets = 0;
        long dataSize = 0;
        long lastGranule = 0;
        while( (vad = vf.getNextAudioPacket()) != null ) {
            dataPackets += 1;
            dataSize += vad.getData().length;
            lastGranule = vad.getGranulePosition();

            if(debugging) {
                System.out.println(
                        r.lastSeqNum + " - " +
                        vad.getGranulePosition() + " - " +
                        vad.getData().length + " bytes"
                );
            }
        }

        float seconds = lastGranule / vf.getInfo().getRate();
        int minutes = (int)(seconds / 60);
        seconds = seconds - (minutes*60); 

        System.out.println("");
        System.out.println("Vorbis Audio:");
        System.out.println("  Total Data Packets: " + dataPackets);
        System.out.println("  Total Data Length: " + dataSize);
        System.out.println("  Audio Length: " + minutes + "m:" +
                (int)seconds + "s");
    }

    public static void printHelp() {
        System.err.println("Use:");
        System.err.println("  VorbisInfoTool file.ogg");
        System.exit(1);
    }

    public static void listTags(VorbisFile vf) throws Exception {
        Map<String, List<String>> comments =
                vf.getComment().getAllComments();
        for(String tag : comments.keySet()) {
            for(String value : comments.get(tag)) {
                System.out.println("  " + tag + "=" + value);
            }
        }
    }

    protected static class InfoPacketReader extends OggPacketReader {
        private boolean inProgress = false;
        private int lastSeqNum = 0;

        public InfoPacketReader(InputStream inp) {
            super(inp);
        }

        @Override
        public OggPacket getNextPacket() throws IOException {
            if(inProgress) {
                inProgress = false;
                return super.getNextPacket();
            } else {
                inProgress = true;
            }

            OggPacket p = super.getNextPacket();
            inProgress = false;

            if(p != null) {
                lastSeqNum = p.getSequenceNumber();

                if(p.isBeginningOfStream()) {
                    System.out.println(
                            "New logical stream " + 
                            Integer.toHexString(p.getSid()) +
                            " (" + p.getSid() + ") found"
                    );
                }
                if(p.isEndOfStream()) {
                    System.out.println(
                            "Logical stream " + 
                            Integer.toHexString(p.getSid()) +
                            " (" + p.getSid() + ") completed"
                    );
                }
            }
            return p;
        }

        @Override
        public OggPacket getNextPacketWithSid(int sid) throws IOException {
            OggPacket p;
            while( (p = getNextPacket()) != null ) {
                if(p.getSid() != sid) {
                    System.out.println("Ignoring packet from stream " +
                            Integer.toHexString(p.getSid()));
                } else {
                    return p;
                }
            }
            return null;
        }
    }
}
