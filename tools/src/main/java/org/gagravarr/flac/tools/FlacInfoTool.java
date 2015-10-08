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
package org.gagravarr.flac.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.gagravarr.flac.FlacAudioFrame;
import org.gagravarr.flac.FlacAudioSubFrame;
import org.gagravarr.flac.FlacFile;
import org.gagravarr.flac.FlacOggFile;

/**
 * Prints out information on the contents of a FLAC file,
 *  including metadata and frame-level details.
 * It's similar to the analysis from "flac -a"
 */
public class FlacInfoTool {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.err.println("Use:");
            System.err.println("   FlacInfoTool <file> [file] [file]");
            System.exit(1);
        }

        for(String f : args) {
            FlacInfoTool info = new FlacInfoTool(new File(f));
            info.printMetadataInfo();
            info.printFrameInfo();
            info.flac.close();
        }
    }

    private File file;
    private FlacFile flac;
    public FlacInfoTool(File f) throws FileNotFoundException, IOException {
        if(! f.exists()) {
            throw new FileNotFoundException(f.toString());
        }

        file = f;
        flac = FlacFile.open(f);
    }

    public void printMetadataInfo() throws IOException {
        if (flac instanceof FlacOggFile) {
            FlacOggFile ogg = (FlacOggFile)flac;
            System.out.println("FLAC-in-Ogg, in stream " + ogg.getSid());
        } else {
            System.out.println("FLAC Native");
        }
        // TODO Output more of this
        System.out.println(flac.getInfo());
        System.out.println(flac.getTags());
    }

    public void printFrameInfo() throws IOException {
        int fn = -1;
        FlacAudioFrame audio;
        while ((audio=flac.getNextAudioPacket()) != null) {
            fn++;

            System.out.print("frame="+fn);
            System.out.print("  ");
            System.out.print("offset=??");
            System.out.print("  ");
            System.out.print("bits="+(audio.getData().length*8));
            System.out.print("  ");
            System.out.print("blocksize="+audio.getBlockSize());
            System.out.print("  ");
            System.out.print("sample_rate="+audio.getSampleRate());
            System.out.print("  ");
            System.out.print("channels="+audio.getNumChannels());
            System.out.print("  ");
            System.out.print("channel_assignment=??");
            System.out.println();

            for (int sfn=0; sfn<audio.getSubFrames().length; sfn++) {
                FlacAudioSubFrame sf = audio.getSubFrames()[sfn];
                System.out.print("   ");
                System.out.print("subframe="+sfn);
                // Rest TODO
                System.out.println();
            }
        }
    }
}
