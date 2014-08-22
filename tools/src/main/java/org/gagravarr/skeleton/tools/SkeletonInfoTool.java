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
import org.gagravarr.skeleton.SkeletonPacket;
import org.gagravarr.skeleton.SkeletonPacketFactory;

/**
 * A tool for looking at the innards of a Skeleton-described
 *  Ogg File
 */
public class SkeletonInfoTool {
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
        
        SkeletonInfoTool info = new SkeletonInfoTool(new File(filename));
        info.printStreamInfo();
    }

    public static void printHelp() {
        System.err.println("Use:");
        System.err.println("  SkeletonInfoTool file.ogg");
        System.exit(1);
    }
    
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
                    SkeletonStream skel = new SkeletonStream(p.getSid(), streams);
                    skelIds.put(sidI, skel);
                    skels.add(skel);
                }
            }
            if (skelIds.containsKey(p.getSid())) {
                skelIds.get(sidI).packets.add(
                        SkeletonPacketFactory.create(p)
                );
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
            System.out.println("Skeleton at " + skel.streamNumber + " with serial " + skel.sid);
            
            // TODO Output the head info
            
            // Output the bones
            int bones = 0;
            for (SkeletonPacket sp : skel.packets) {
                if (sp instanceof SkeletonFisbone) {
                    SkeletonFisbone bone = (SkeletonFisbone)sp;
                    System.out.print(" * Bone " + (++bones));
                    
                    // TODO Output the other bits of hte bone
                    
                    System.out.println(" - Message Headers:");
                    for (String key : bone.getMessageHeaders().keySet()) {
                        System.out.println("  * " + key + " = " + bone.getMessageHeaders().get(key));
                    }
                }
            }
            
            // TODO Output something of the key frame(s)
        }
        if (skeletons.isEmpty()) {
            System.out.println();
            System.out.println("There are no Skeleton streams in the file");
        }
    }
    
    public static class SkeletonStream {
        private int sid;
        private int streamNumber;
        private List<SkeletonPacket> packets;
        
        protected SkeletonStream(int sid, int streamNumber) {
            this.sid = sid;
            this.streamNumber = streamNumber;
            this.packets = new ArrayList<SkeletonPacket>();
        }
    }
}
