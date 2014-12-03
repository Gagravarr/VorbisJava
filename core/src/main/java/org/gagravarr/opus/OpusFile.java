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
package org.gagravarr.opus;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggPacketWriter;
import org.gagravarr.ogg.OggPage;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.ogg.OggStreamIdentifier.OggStreamType;
import org.gagravarr.ogg.audio.OggAudioHeaders;
import org.gagravarr.ogg.audio.OggAudioSetupHeader;
import org.gagravarr.ogg.audio.OggAudioStream;

/**
 * This is a wrapper around an OggFile that lets you
 *  get at all the interesting bits of an Opus file.
 */
public class OpusFile implements OggAudioStream, OggAudioHeaders, Closeable {
	private OggFile ogg;
	private OggPacketReader r;
	private OggPacketWriter w;
	private int sid = -1;

	private OpusInfo info;
	private OpusTags tags;

	private List<OpusAudioData> writtenPackets;

	/**
	 * Opens the given file for reading
	 */
	public OpusFile(File f) throws IOException, FileNotFoundException {
		this(new OggFile(new FileInputStream(f)));
	}
	/**
	 * Opens the given file for reading
	 */
	public OpusFile(OggFile ogg) throws IOException {
		this(ogg.getPacketReader());
		this.ogg = ogg;
	}
	/**
	 * Loads a Opus File from the given packet reader.
	 */
	public OpusFile(OggPacketReader r) throws IOException {	
		this.r = r;

		OggPacket p = null;
		//p = r.getNextPacket();
		OggPacket firstPacket = null;
		OggPacket secondPacket = null;
		while( (p = r.getNextPacket()) != null ) {
			if (p.isBeginningOfStream() && p.getData().length > 10) {
				if (OpusPacketFactory.isOpusStream(p)) {
					sid = p.getSid();
					if (sid == -1) {
						throw new IllegalArgumentException("Supplied File is not Opus");
					}
					firstPacket = p;
					secondPacket = r.getNextPacketWithSid(sid);
					break;

				}
			}
		}
		int overhead_bytes = 0;
		int max_packet_duration = 0;
		int min_packet_duration =5760;
        int page_samples = 0;
		int total_samples = 0;
		int bytes = 0;
		long lastlastgranulepos = -1;
		long lastgranulepos = 0;
		long firstgranulepos = -1;
		int max_packet_bytes = 0;
		int min_packet_bytes = 2147483647;
		int packets = 0;
		
		int total_packets = 0;
		int last_packet_duration = 0;
		int last_page_duration = 0;
		int max_page_duration = -1;
		int min_page_duration = 5760*255;
		int page_count = 1;
		int page_packets = 0;
		OggPage currentPage = null;
		while( (p = r.getNextPacketWithSid(sid)) != null ) {
			packets++;
			page_packets++;
			long gp = p.getGranulePosition();
			OggPage c = r.getCurrentPage();
			if (currentPage==null || currentPage.getSequenceNumber()!=c.getSequenceNumber()) {
				page_count++;
				//OggPage oldpage = currentPage;
				//if (oldpage!= null) {
				//System.err.println("Page "+oldpage.getSequenceNumber()+": samples:"+page_samples+" page packets:"+page_packets);
				//}
				currentPage = c;
				//System.err.println("New Page "+page_count+"/"+currentPage.getSequenceNumber()+" datasize:"+currentPage.getDataSize()+" gp:"+currentPage.getGranulePosition());
				page_packets=0;
				
				if (gp>0) {
					if (gp < lastgranulepos) {
						System.err.println("WARNING: granulepos in stream "+sid+" decreases from "
								+lastgranulepos+ " to "+gp);
					}
					if (lastgranulepos == 0 && firstgranulepos == -1) {
						/*First timed page, now we can recover the start time.*/
						firstgranulepos = gp;
						if (firstgranulepos<0) {
							if (!p.isEndOfStream()) {
								System.err.println("WARNING:Samples with negative granpos in stream "+sid);
							} else {
								firstgranulepos = 0;
							}
						}
					}
					if (lastlastgranulepos == 0) {
						firstgranulepos = firstgranulepos-page_samples;
					}
					if ((total_samples) < (lastgranulepos - firstgranulepos)) {
						System.err.println("WARNING: Sample count behind granule ("+(total_samples)+"<"+(lastgranulepos-firstgranulepos)+") in stream "+sid);
					}
					if (!p.isEndOfStream() && total_samples > (gp - firstgranulepos)) {
						System.err.println("WARNING: Sample count ahead granule ("+total_samples+"<"+firstgranulepos+") in stream"+sid);
					}
					lastlastgranulepos = lastgranulepos;
					lastgranulepos = gp;
					if (packets == 0) {
						System.err.println("WARNING: Page with positive granpos ("+gp+") on a page with no completed packets in stream "+sid);
					}
				} // gp
				else if (packets == 0) {
					System.err.println("Negative or zero granulepos ("+gp+") on Opus stream outside of headers. This file was created by a buggy encoder");
				}
				int body_len = currentPage.getDataSize();
				int header_len = currentPage.getPageSize()-body_len;
				overhead_bytes += header_len;
				last_page_duration = page_samples;
				if (max_page_duration<page_samples) max_page_duration=page_samples;
				if (min_page_duration>page_samples) min_page_duration=page_samples;
				page_samples = 0;
			}
			if (p.getSid() != sid) {
				System.err.println("WARNING: Ignoring sid "+p.getSid());
				continue;
			}
			byte[] d = p.getData();
			if (d.length < 1) {
				System.err.println("WARNING: Invalid packet TOC in stream with sid "+sid);
				continue;
			}
			int spp = packet_get_nb_frames(d);
			spp *= packet_get_samples_per_frame(d, 48000);
			if(spp<120 || spp>5760 || (spp%120)!=0) {
				System.err.println("WARNING: Invalid packet TOC in stream with sid "+sid);
				continue;
			}
			total_samples += spp;
			page_samples += spp;
			total_packets++;
			last_packet_duration = spp;
			if (max_packet_duration<spp) max_packet_duration=spp;
			if (min_packet_duration>spp) min_packet_duration = spp;
			if (max_packet_bytes<d.length) max_packet_bytes = d.length;
			if (min_packet_bytes>d.length) min_packet_bytes = d.length;
			
		}
		
		// First two packets are required to be info then tags
		info = (OpusInfo)OpusPacketFactory.create( firstPacket );
		tags = (OpusTags)OpusPacketFactory.create( secondPacket );
		overhead_bytes += firstPacket.getData().length;
        overhead_bytes += secondPacket.getData().length;
        System.err.println("Packet duration:"+(max_packet_duration/48.0)+"ms (max), "
        +(total_samples/total_packets/48.0)+"ms (avg), "+min_packet_duration/48.0+"ms (min)");
        double time = (lastgranulepos-firstgranulepos-info.getPreSkip()) / 48000.;
        if (time<=0) time =0;
        int minutes = (int)time / 60;
        int seconds = (int)(time - (minutes*60));
        int milliseconds = (int)((time - minutes*60 - seconds)*1000);
        System.err.println("Playback duration:"+minutes+"m:"+seconds+"."+milliseconds+"s");
        
        // Everything else should be audio data
	}

	private static int packet_get_samples_per_frame(byte[] data, int Fs) {
		int audiosize;
		if ((data[0]&0x80) != 0)
		{
			audiosize = ((data[0]>>3)&0x3);
			audiosize = (Fs<<audiosize)/400;
		} else if ((data[0]&0x60) == 0x60)
		{
			audiosize = ((data[0]&0x08) != 0) ? Fs/50 : Fs/100;
		} else {
			audiosize = ((data[0]>>3)&0x3);
			if (audiosize == 3)
				audiosize = Fs*60/1000;
			else
				audiosize = (Fs<<audiosize)/100;
		}
		return audiosize;

	}

	private static int packet_get_nb_frames(byte[] packet) {
		int count = 0;
		if (packet.length < 1) {
			return -1;
		}
		count = packet[0]&0x3;
		if (count==0)
			return 1;
		else if (count!=3)
			return 2;
		else if (packet.length<2)
			return -4;
		else
			return packet[1]&0x3F;
	}



	/**
	 * Opens for writing.
	 */
	 public OpusFile(OutputStream out) {
		 this(out, new OpusInfo(), new OpusTags());   
	 }
	 /**
	  * Opens for writing, based on the settings
	  *  from a pre-read file. The Steam ID (SID) is
	  *  automatically allocated for you.
	  */
	 public OpusFile(OutputStream out, OpusInfo info, OpusTags tags) {
		 this(out, -1, info, tags);
	 }
	 /**
	  * Opens for writing, based on the settings
	  *  from a pre-read file, with a specific
	  *  Steam ID (SID). You should only set the SID
	  *  when copying one file to another!
	  */
	 public OpusFile(OutputStream out, int sid, OpusInfo info, OpusTags tags) {
		 ogg = new OggFile(out);

		 if(sid > 0) {
			 w = ogg.getPacketWriter(sid);
			 this.sid = sid;
		 } else {
			 w = ogg.getPacketWriter();
			 this.sid = w.getSid();
		 }

		 writtenPackets = new ArrayList<OpusAudioData>();

		 this.info = info;
		 this.tags = tags;
	 }

	 public OpusAudioData getNextAudioPacket() throws IOException {
		 OggPacket p = null;
		 OpusPacket op = null;
		 while( (p = r.getNextPacketWithSid(sid)) != null ) {
			 op = OpusPacketFactory.create(p);
			 if(op instanceof OpusAudioData) {
				 return (OpusAudioData)op;
			 } else {
				 System.err.println("Skipping non audio packet " + op + " mid audio stream");
			 }
		 }
		 return null;
	 }

	 /**
	  * Skips the audio data to the next packet with a granule
	  *  of at least the given granule position.
	  * Note that skipping backwards is not currently supported!
	  */
	 public void skipToGranule(long granulePosition) throws IOException {
		 r.skipToGranulePosition(sid, granulePosition);
	 }

	 /**
	  * Returns the Ogg Stream ID
	  */
	 public int getSid() {
		 return sid;
	 }

	 /**
	  * This is an Opus file
	  */
	 public OggStreamType getType() {
		 return OggStreamIdentifier.OPUS_AUDIO;
	 }

	 public OpusInfo getInfo() {
		 return info;
	 }
	 public OpusTags getTags() {
		 return tags;
	 }
	 /**
	  * Opus doesn't have setup headers, so this is always null
	  */
	 public OggAudioSetupHeader getSetup() {
		 return null;
	 }

	 /**
	  * Buffers the given audio ready for writing
	  *  out. Data won't be written out yet, you
	  *  need to call {@link #close()} to do that,
	  *  because we assume you'll still be populating
	  *  the Info/Comment/Setup objects
	  */
	 public void writeAudioData(OpusAudioData data) {
		 writtenPackets.add(data);
	 }

	 /**
	  * In Reading mode, will close the underlying ogg
	  *  file and free its resources.
	  * In Writing mode, will write out the Info and
	  *  Tags objects, and then the audio data.
	  */
	 public void close() throws IOException {
		 if(r != null) {
			 r = null;
			 ogg.close();
			 ogg = null;
		 }
		 if(w != null) {
			 w.bufferPacket(info.write(), true);
			 w.bufferPacket(tags.write(), false);

			 long lastGranule = 0;
			 for(OpusAudioData vd : writtenPackets) {
				 // Update the granule position as we go
				 if(vd.getGranulePosition() >= 0 &&
						 lastGranule != vd.getGranulePosition()) {
					 w.flush();
					 lastGranule = vd.getGranulePosition();
					 w.setGranulePosition(lastGranule);
				 }

				 // Write the data, flushing if needed
				 w.bufferPacket(vd.write());
				 if(w.getSizePendingFlush() > 16384) {
					 w.flush();
				 }
			 }

			 w.close();
			 w = null;
			 ogg.close();
			 ogg = null;
		 }
	 }

	 /**
	  * Returns the underlying Ogg File instance
	  * @return
	  */
	 public OggFile getOggFile() {
		 return ogg;
	 }
}
