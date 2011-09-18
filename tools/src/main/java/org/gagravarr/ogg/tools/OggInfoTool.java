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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.gagravarr.flac.FlacFirstOggPacket;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.vorbis.VorbisPacket;

/**
 * Prints out information on the Steams within
 *  an Ogg File
 */
public class OggInfoTool {
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.err.println("Use:");
			System.err.println("   OggInfoTool <file> [file] [file]");
			System.exit(1);
		}
		
		for(String f : args) {
			OggInfoTool info = new OggInfoTool(new File(f));
			info.printStreamInfo();
		}
	}
	
	private File file;
	private OggFile ogg;
	public OggInfoTool(File f) throws FileNotFoundException {
		if(! f.exists()) {
			throw new FileNotFoundException(f.toString());
		}
		
		file = f;
		ogg = new OggFile( new FileInputStream(f) );
	}
	
	public void printStreamInfo() throws IOException {
		OggPacketReader r = ogg.getPacketReader();
		
		System.out.println("Processing file \"" + file.toString() + "\"");
		
		int pc = 0;
		int streams = 0;
		int lastSid = -1;
		
		OggPacket p;
		while( (p = r.getNextPacket()) != null ) {
			if(p.isBeginningOfStream()) {
				streams++;
				lastSid = p.getSid();
				
				System.out.println("");
				System.out.println("New logical stream #"+streams + ", serial: " +
						Integer.toHexString(p.getSid()) + " (" + p.getSid() + ")");
				
				if(p.getData() != null && p.getData().length > 10) {
					if(VorbisPacket.isVorbisStream(p)) {
						System.out.println("\tVorbis Info detected");
					}
					if(FlacFirstOggPacket.isFlacStream(p)) {
						System.out.println("\tFLAC Info detected");
					}
				}
			} else if(p.isEndOfStream()) {
				System.out.println("Stream " + Integer.toHexString(p.getSid()) + 
						" ended");
			} else {
				if(p.getSid() != lastSid) {
					System.out.println("(" + pc + " packets of stream " +
							Integer.toHexString(p.getSid()) + ")");
					
					lastSid = p.getSid();
					pc = 0;
				} else {
					pc++;
				}
			}
		}
	}
}
