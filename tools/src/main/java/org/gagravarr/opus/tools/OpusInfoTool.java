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

import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;

/**
 * A class for listing tags and more info from a 
 *  Opus File
 */
public class OpusInfoTool {

	private static String format1(double d) {
		return String.format("%.2f",d);
	}
	private static String format2(double d) {
		return String.format("%8.1f",d);
	}
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			printHelp();
		}
		
		String filename = args[0];
		OpusFile op = new OpusFile(new File(filename));

		OpusInfo info = op.getInfo();
		info.consumeStream();
		
		System.out.println("\tPre-skip: " + info.getPreSkip());
		System.out.println("\tPlayback gain: "+info.getOutputGain()+" dB");
		System.out.println("\tChannels: "+info.getChannels());
		System.out.println("\tOriginal sample rate: "+ info.getRate()+"Hz");
		System.out.println("\tPacket duration: "+ format2(info.getMaxPacketDuration())+"ms (max), "
				+format2(info.getAvgPacketDuration())+"ms (avg), "
				+format2(info.getMinPacketDuration())+"ms (min)");
		System.out.println("\tPage duration:   "+ format2(info.getMaxPageDuration())+"ms (max), "
				+format2(info.getAvgPageDuration())+"ms (avg), "          		
				+format2(info.getMinPageDuration())+"ms (min)");
		//DecimalFormat df2 = new DecimalFormat("#.00");
		System.out.println("\tTotal data length: "+ info.getBytes()+" (overhead: "
				+format1(((double)info.getOverheadBytes()/info.getBytes()*100.0))+"%)");
		System.out.println("\tPlayback length: "+ info.getPlayTimeAsString());
		double avgbr = (info.getBytes()*8) / info.getPlayTime();
		double avgbrover = ((info.getBytes()-info.getOverheadBytes())*8) / info.getPlayTime();
		String cbr = "";
		if (info.getMinPacketDuration() == info.getMaxPacketDuration()
				&& info.getMinPacketBytes() == info.getMaxPacketBytes()) {
			cbr= " (hard-CBR)";
		}
		System.out.println("\tAverage bitrate: "+ format1(avgbr) + " kb/s, w/o overhead: "+format1(avgbrover)+" kb/s"+cbr);
		op.close();

	}
	
	public static void printHelp() {
		System.err.println("Use:");
		System.err.println("  OpusInfoTool file.opus");
		System.exit(1);
	}
}
