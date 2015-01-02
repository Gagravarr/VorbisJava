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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.gagravarr.ogg.tools.OggAudioInfoTool;
import org.gagravarr.vorbis.VorbisFile;

/**
 * A tool for looking at the innards of a Vorbis File
 */
public class VorbisInfoTool extends OggAudioInfoTool {
    public static void main(String[] args) throws Exception {
        handleMain(args, new VorbisInfoTool());
    }

    @Override
    public String getToolName() {
        return "VorbisInfoTool";
    }
    @Override
    public String getDefaultExtension() {
        return "ogg";
    }

    @Override
    public void process(File file, boolean debugging) throws IOException {
        InfoPacketReader r = new InfoPacketReader(
                new FileInputStream(file));
        VorbisFile vf = new VorbisFile(r);

        System.out.println("Processing file \"" + file + "\"");

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

        InfoAudioStats stats = new InfoAudioStats(vf, vf, r.getLastSeqNum(), debugging);
        stats.calculate();
        System.out.println("");
        System.out.println("Vorbis Audio:");
        System.out.println("  Total Data Packets: " + stats.getAudioPacketsCount());
        System.out.println("  Total Data Length: " + stats.getAudioDataSize());
        System.out.println("  Audio Length Seconds: " + stats.getDurationSeconds());
        System.out.println("  Audio Length: " + stats.getDuration());
    }
}
