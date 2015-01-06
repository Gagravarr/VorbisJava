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
package org.gagravarr.opus.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.gagravarr.ogg.tools.OggAudioInfoTool;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusStatistics;

/**
 * A tool for looking at the innards of an Opus File
 */
public class OpusInfoTool extends OggAudioInfoTool {
    public static void main(String[] args) throws Exception {
        handleMain(args, new OpusInfoTool());
    }

    @Override
    public String getToolName() {
        return "OpusInfoTool";
    }
    @Override
    public String getDefaultExtension() {
        return "opus";
    }

    @Override
    public void process(File file, boolean debugging) throws IOException {
        InfoPacketReader r = new InfoPacketReader(
                new FileInputStream(file));
        OpusFile of = new OpusFile(r);

        System.out.println("Processing file \"" + file + "\"");

        System.out.println("");
        System.out.println("Opus Headers:");
        System.out.println("  Version: " + of.getInfo().getVersion());
        System.out.println("  Vendor: " + of.getTags().getVendor());
        System.out.println("  Channels: " + of.getInfo().getNumChannels());
        System.out.println("  Rate: " + of.getInfo().getRate());
        System.out.println("");

        System.out.println("User Comments:");
        listTags(of);
        System.out.println("");

        OpusStatistics stats = new OpusStatistics(of);
        stats.calculate();
        System.out.println("");
        System.out.println("Opus Audio:");
        System.out.println("  Total Data Packets: " + stats.getAudioPacketsCount());
        System.out.println("  Total Data Length: " + stats.getAudioDataSize());
        System.out.println("  Audio Length Seconds: " + stats.getDurationSeconds());
        System.out.println("  Audio Length: " + stats.getDuration());
        System.out.println("  Packet duration: "+ format2(stats.getMaxPacketDuration())+"ms (max), "
                           +format2(stats.getAvgPacketDuration())+"ms (avg), "
                           +format2(stats.getMinPacketDuration())+"ms (min)");
        System.out.println("  Page duration:   "+ format2(stats.getMaxPageDuration())+"ms (max), "
                           +format2(stats.getAvgPageDuration())+"ms (avg), "
                           +format2(stats.getMinPageDuration())+"ms (min)");
        System.out.println("  Total data length: "+ stats.getAudioDataSize() + 
                           " (overhead: " + format1(stats.getOggOverheadPercentage())+"%)");
        System.out.println("  Playback length: "+ stats.getDuration());
//        double avgbr = (stats.getAudioDataSize()*8) / stats.getDurationSeconds();
//        double avgbrover = ((stats.getBytes()-stats.getOverheadBytes())*8) / stats.getPlayTime();
//        String cbr = "";
//        if (stats.getMinPacketDuration() == stats.getMaxPacketDuration()
//                && stats.getMinPacketBytes() == stats.getMaxPacketBytes()) {
//            cbr= " (hard-CBR)";
//        }
//        System.out.println("  Average bitrate: "+ format1(avgbr) + " kb/s, w/o overhead: "+format1(avgbrover)+" kb/s"+cbr);
    }
}
