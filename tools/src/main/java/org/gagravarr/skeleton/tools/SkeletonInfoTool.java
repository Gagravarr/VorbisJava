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
package org.gagravarr.skeleton.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.skeleton.SkeletonFisbone;
import org.gagravarr.skeleton.SkeletonFishead;
import org.gagravarr.skeleton.SkeletonPacketFactory;
import org.gagravarr.skeleton.SkeletonStream;

/**
 * A tool for looking at the innards of a Skeleton-described
 *  Ogg File
 */
public class SkeletonInfoTool {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            printHelp();
        }

        String filename = args[0];
        if(args.length > 1 && args[0].equals("-d")) {
            filename = args[1];
            debugging = true;
        }

        SkeletonInfoTool info = new SkeletonInfoTool(new File(filename));
        info.printStreamInfo();
    }

    public static void printHelp() {
        System.err.println("Use:");
        System.err.println("  SkeletonInfoTool file.ogg");
        System.exit(1);
    }

    private static boolean debugging = false;

    private File file;
    private OggFile ogg;
    public SkeletonInfoTool(File f) throws IOException {
        this(new OggFile(new FileInputStream(f)));
        this.file = f;
    }
    protected SkeletonInfoTool(OggFile ogg) {
        this.ogg = ogg;
    }

    public static List<SkeletonStream> getSkeletonStreams(OggPacketReader r) throws IOException {
        Map<Integer, SkeletonStream> skelIds = new HashMap<Integer, SkeletonStream>();
        List<SkeletonStream> skels = new ArrayList<SkeletonStream>();

        int streams = 0;
        OggPacket p;
        while( (p = r.getNextPacket()) != null ) {
            Integer sidI = p.getSid();
            if (p.isBeginningOfStream()) {
                streams++;

                if (SkeletonPacketFactory.isSkeletonStream(p)) {
                    SkeletonStream skel = new SkeletonStream(p);
                    skelIds.put(sidI, skel);
                    skels.add(skel);

                    if (debugging) {
                        System.out.println("Found skeleton in stream at " + streams + " with SID " + skel.getSid());
                    }
                } else {
                    if (debugging) {
                        System.out.println("Found non-skeleton stream at " + streams + " with SID " + p.getSid());
                    }
                }
            }
            if (skelIds.containsKey(p.getSid())) {
                skelIds.get(sidI).processPacket(p);
            }
        }

        return skels;
    }
    public void printStreamInfo() throws IOException {
        OggPacketReader r = ogg.getPacketReader();

        System.out.println("Processing file \"" + file + "\"");

        // Normally there's only one Skeleton stream, but sometimes
        //  there can be more. Collect all of them
        List<SkeletonStream> skeletons = getSkeletonStreams(r);

        // Report what we found
        for (SkeletonStream skel : skeletons) {
            System.out.println();
            System.out.println("Skeleton with serial " + formatSid(skel.getSid()));

            // Output the head info
            SkeletonFishead head = skel.getFishead();
            System.out.println(" - Skeleton version: " + head.getVersion());
            System.out.println(" - Created at: " + head.getUtc());

            // Output the bones
            int bones = 0;
            for (SkeletonFisbone bone : skel.getFisbones()) {
                System.out.println(" * Bone " + (++bones));
                System.out.println("  - For stream " + formatSid(bone.getSerialNumber()));

                // TODO Output the other bits of the bone

                System.out.println("  - Message Headers:");
                for (String key : bone.getMessageHeaders().keySet()) {
                    System.out.println("   * " + key + " = " + bone.getMessageHeaders().get(key));
                }
            }

            if (skel.getKeyFrames().isEmpty()) {
                System.out.println(" * No key frames found");
            } else {
                // TODO Output something of the key frame(s)
                System.out.println(" * " + skel.getKeyFrames().size() + " keyframes");
            }
        }
        if (skeletons.isEmpty()) {
            System.out.println();
            System.out.println("There are no Skeleton streams in the file");
        }
    }

    protected static String formatSid(int sid) {
        StringBuffer s = new StringBuffer();
        s.append(sid);
        s.append(" (0x");
        s.append(Integer.toHexString(sid));
        s.append(")");
        return s.toString();
    }
}
