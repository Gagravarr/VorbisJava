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
package org.xiph.vorbis.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xiph.vorbis.VorbisAudioData;
import org.xiph.vorbis.VorbisFile;

/**
 * A class for listing and editing Comments
 *  within a Vorbis File
 */
public class VorbisComment {
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			printHelp();
		}
		
		if(args.length == 1) {
			listTags(args[0]);
			return;
		}
		if(args.length == 2 && args[0].equals("-l")) {
			listTags(args[1]);
			return;
		}
		
		if(args[0].equals("-w")) {
			editTags(false, args);
			return;
		}
		if(args[0].equals("-a")) {
			editTags(true, args);
			return;
		}
		printHelp();
	}
	
	public static void printHelp() {
		System.err.println("Use:");
		System.err.println("  VorbisComment [-l] file.ogg");
		System.err.println("  VorbisComment -a [-t name=value] [-t name=value] in.ogg [out.ogg]");
		System.err.println("  VorbisComment -w [-t name=value] [-t name=value] in.ogg [out.ogg]");
		System.exit(1);
	}
	
	public static void listTags(String filename) throws Exception {
		VorbisFile vf = new VorbisFile(new File(filename));
		
		Map<String, List<String>> comments =
			vf.getComment().getAllComments();
		for(String tag : comments.keySet()) {
			for(String value : comments.get(tag)) {
				System.out.println(tag + "=" + value);
			}
		}
	}
	
	public static void editTags(boolean append, String[] args) throws Exception {
		String outFile = args[args.length - 1];
		String inFile = outFile;
		if( args.length % 2 == 1 ) {
			inFile = args[args.length - 2];
		}
		
		System.out.println("Source file: " + inFile);
		System.out.println("Output file: " + outFile);
		
		VorbisFile in = new VorbisFile(new File(inFile));
		if(! append) {
			in.getComment().removeAllComments();
		}
		
		for(int i=1; i<args.length-2; i += 2) {
			if(! "-t".equals(args[i])) {
				throw new IllegalArgumentException(
						"Expecting '-t name=value' but found '" +
						args[i] + " " + args[i+1] + "'"
				);
			}
			int split = args[i+1].indexOf('=');
			String name = args[i+1].substring(0, split);
			String value = args[i+1].substring(split+1);
			System.out.println("   Setting '" + name + " = " + value + "'");
			
			in.getComment().addComment(name, value);
		}
		
		List<VorbisAudioData> audio = new ArrayList<VorbisAudioData>();
		VorbisAudioData ad;
		while( (ad = in.getNextAudioPacket()) != null ) {
			audio.add(ad);
		}
		
		// Now write out
		in.close();
		VorbisFile out = new VorbisFile(
				new FileOutputStream(outFile),
				in.getInfo(),
				in.getComment(),
				in.getSetup()
		);
		for(VorbisAudioData vad : audio) {
			out.writeAudioData(vad);
		}
		out.close();
	}
}
