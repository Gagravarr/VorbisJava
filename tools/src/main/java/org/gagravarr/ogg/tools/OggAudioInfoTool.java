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
package org.gagravarr.ogg.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggStreamAudioData;
import org.gagravarr.ogg.audio.OggAudioHeaders;
import org.gagravarr.ogg.audio.OggAudioStatistics;
import org.gagravarr.ogg.audio.OggAudioStream;

/**
 * Parent class of tools for looking at the innards
 *  of Ogg Audio File
 */
public abstract class OggAudioInfoTool {
    public static void handleMain(String[] args, OggAudioInfoTool tool) throws Exception {
        if(args.length == 0) {
            printHelp(tool);
        }

        boolean debugging = false;
        String filename = args[0];
        if(args.length > 1 && args[0].equals("-d")) {
            filename = args[1];
            debugging = true;
        }

        File file = new File(filename);
        if (! file.exists()) {
            System.err.println("Error - file not found");
            System.err.println("   " + file);
            System.exit(2);
        }

        tool.process(file, debugging);
    }

    public abstract void process(File file, boolean debugging) throws IOException;

    public abstract String getToolName();
    public abstract String getDefaultExtension();

    public static void printHelp(OggAudioInfoTool tool) {
        System.err.println("Use:");
        System.err.println("   " + tool.getToolName() + 
                           " file." + tool.getDefaultExtension());
        System.exit(1);
    }

    public static void listTags(OggAudioHeaders oa) {
        Map<String, List<String>> comments =
                oa.getTags().getAllComments();
        for(String tag : comments.keySet()) {
            for(String value : comments.get(tag)) {
                System.out.println("  " + tag + "=" + value);
            }
        }
    }

    protected static String format2(double d) {
        return String.format("%8.1f",d);
    }
    protected static String format1(double d) {
        return String.format("%.2f",d);
    }

    protected static class InfoAudioStats extends OggAudioStatistics {
        private boolean debugging;
        private int lastSeqNum;

        public InfoAudioStats(OggAudioStream audio, int lastSeqNum,
                   boolean debugging) throws IOException {
            super(audio);
            this.debugging = debugging;
            this.lastSeqNum = lastSeqNum;
        }

        @Override
        protected void handleAudioData(OggStreamAudioData audioData) {
            super.handleAudioData(audioData);

            if(debugging) {
                System.out.println(
                        lastSeqNum + " - " +
                        audioData.getGranulePosition() + " - " +
                        audioData.getData().length + " bytes"
                );
            }
        }
    }

    protected static class InfoPacketReader extends OggPacketReader {
        private boolean inProgress = false;
        private int lastSeqNum = 0;

        public InfoPacketReader(InputStream inp) {
            super(inp);
        }

        public int getLastSeqNum() {
            return lastSeqNum;
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
