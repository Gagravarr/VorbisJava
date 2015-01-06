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

import java.io.IOException;

import org.gagravarr.ogg.OggStreamAudioData;
import org.gagravarr.ogg.audio.OggAudioHeaders;
import org.gagravarr.ogg.audio.OggAudioStatistics;
import org.gagravarr.ogg.audio.OggAudioStream;

public class OpusStatistics extends OggAudioStatistics {
    private double durationSeconds;
    private int total_pages;
    private int total_packets;
    private int total_samples;
    private int max_packet_duration;
    private int min_packet_duration;
    private int max_page_duration;
    private int min_page_duration;
    private int max_packet_bytes;
    private int min_packet_bytes;
    private int sid;
    private int preSkip;

    private long lastlastgranulepos = -1;
    private long lastgranulepos = 0;
    private long firstgranulepos = -1;
    private int page_samples = 0;
    private int page_count = 0;

    private OpusInfo info;

    public OpusStatistics(OggAudioHeaders headers, OggAudioStream audio) throws IOException {
        super(headers, audio);

        if (headers.getInfo() instanceof OpusInfo) {
            info = (OpusInfo)headers.getInfo();
        } else {
            throw new IllegalArgumentException("Non-Opus stream " + headers.getInfo() + " supplied");
        }

        init(headers);
    }
    public OpusStatistics(OpusFile opus) throws IOException {
        super(opus, opus);
        this.info = opus.getInfo();
        init(opus);
    }
    private void init(OggAudioHeaders headers) throws IOException {
        sid = headers.getSid();
        preSkip = info.getPreSkip();
        max_packet_duration = 0;
        min_packet_duration = 5760;
        total_samples = 0;
        total_packets = 0;
        max_page_duration = -1;
        min_page_duration = 5760*255;
        max_packet_bytes = 0;
        min_packet_bytes = 2147483647;
    }

    @Override
    public void calculate() throws IOException {
        super.calculate();

        if (max_page_duration<page_samples) max_page_duration=page_samples;
        if (min_page_duration>page_samples) min_page_duration=page_samples;

        total_pages = page_count;

        // Opus can have a pre-skip of a certain number of samples
        // TODO Can we make this generic?
        durationSeconds = ((lastgranulepos-preSkip) / 48000.0);
        //durationSeconds = ((lastgranulepos-firstgranulepos-getPreSkip()) / 48);
    }

    @Override
    protected void handleAudioData(OggStreamAudioData audioData) {
        handleAudioData((OpusAudioData)audioData);
    }
    protected void handleAudioData(OpusAudioData audioData) {
        super.handleAudioData(audioData);

        long gp = audioData.getGranulePosition();
        if (gp != lastgranulepos) {
            page_count++;

            if (gp>0) {
                if (gp < lastgranulepos) {
                    System.err.println("WARNING: granulepos in stream "+sid+" decreases from "
                            +lastgranulepos+ " to "+gp);
                }
                if (lastgranulepos == 0 && firstgranulepos == -1) {
                    /*First timed page, now we can recover the start time.*/
                    firstgranulepos = gp;
                    if (firstgranulepos<0) {
                        if (! audioData.isEndOfStream()) {
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
                if (! audioData.isEndOfStream() && total_samples > (gp - firstgranulepos)) {
                    System.err.println("WARNING: Sample count ahead granule ("+total_samples+"<"+firstgranulepos+") in stream"+sid);
                }
                lastlastgranulepos = lastgranulepos;
                lastgranulepos = gp;
                if (getAudioPacketsCount() == 0) {
                    System.err.println("WARNING: Page with positive granpos ("+gp+") on a page with no completed packets in stream "+sid);
                }
            } // gp
            else if (getAudioPacketsCount() == 0) {
                System.err.println("Negative or zero granulepos ("+gp+") on Opus stream outside of headers. This file was created by a buggy encoder");
            }

            //last_page_duration = page_samples;
            if (max_page_duration<page_samples) max_page_duration=page_samples;
            if (page_count > 1) {
                if (min_page_duration>page_samples) min_page_duration=page_samples;
            }
            page_samples = 0;
        }
        //if (p.getSid() != sid) {
        //    System.err.println("WARNING: Ignoring sid "+p.getSid());
        //    continue;
        //}
        byte[] d = audioData.getData();
        if (d.length < 1) {
            System.err.println("WARNING: Invalid packet TOC in stream with sid "+sid);
            return;
        }

        int spp = packet_get_nb_frames(d);
        spp *= packet_get_samples_per_frame(d, 48000);
        if(spp<120 || spp>5760 || (spp%120)!=0) {
            System.err.println("WARNING: Invalid packet TOC in stream with sid "+sid);
            return;
        }
        total_samples += spp;
        page_samples += spp;
        total_packets++;
        //last_packet_duration = spp;
        if (max_packet_duration<spp) max_packet_duration=spp;
        if (min_packet_duration>spp) min_packet_duration = spp;
        if (max_packet_bytes<d.length) max_packet_bytes = d.length;
        if (min_packet_bytes>d.length) min_packet_bytes = d.length;
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

    public double getMaxPacketDuration() {
        return (max_packet_duration/48.0);
    }
    public double getAvgPacketDuration() {
        if (total_packets > 0) {
            return (total_samples/total_packets/48.0);
        }
        return 0.0;
    }
    public double getMinPacketDuration() {
        return (min_packet_duration/48.0);
    }
    public double getMaxPageDuration() {
        return max_page_duration/48.0;
    }
    public double getAvgPageDuration() {
        if (total_pages > 0 ) {
            return total_samples/(double)total_pages/48.0;
        }
        return 0.0;
    }
    public double getMinPageDuration() {
        return min_page_duration/48.0;
    }

    @Override
    public double getDurationSeconds() {
        return durationSeconds;
    }

    public int getMaxPacketBytes() {
        return max_packet_bytes;
    }
    public int getMinPacketBytes() {
        return min_packet_bytes;
    }
}
