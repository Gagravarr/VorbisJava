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

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggPage;
import org.gagravarr.ogg.audio.OggAudioInfoHeader;

/**
 * The identification header identifies the bitstream as Opus, 
 *  and includes the Opus version, the simple audio characteristics 
 *  of the stream such as sample rate and number of channels etc.
 */
public class OpusInfo extends HighLevelOggStreamPacket implements OpusPacket, OggAudioInfoHeader {
    private byte version;
    private int majorVersion;
    private int minorVersion;

    private int channels;
    private int preSkip;
    private long rate;
    private int outputGain;
    private byte channelMappingFamily;
    private byte streamCount;
    private byte twoChannelStreamCount;
    private byte[] channelMapping;

    private boolean isExtendedInfoAvailable;
    private OggPage secondPage;
    private int sid;
    private OggPacketReader oggPacketReader;

    private long playtime;
    private int overhead_bytes;
    private int total_pages;
    private int total_packets;
    private int total_samples;
    private int bytes;
    private int max_packet_duration;
    private int min_packet_duration;
    private int max_page_duration;
    private int min_page_duration;
    private int max_packet_bytes;
    private int min_packet_bytes;

    public OpusInfo() {
        super();
        version = 1;
        isExtendedInfoAvailable = false;
    }

    public OpusInfo(OggPacket pkt) {
        super(pkt);

        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_HEADER_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Opus Header");
        }

        // Parse
        version = data[8];
        parseVersion();
        if (majorVersion != 0) {
            throw new IllegalArgumentException("Unsupported Opus version " + version + " at major version " + majorVersion + " detected");
        }

        channels = (int)data[9];
        preSkip = IOUtils.getInt2(data, 10);
        rate    = IOUtils.getInt4(data, 12);
        outputGain = IOUtils.getInt2(data, 16);

        channelMappingFamily = data[18];
        if (channelMappingFamily != 0) {
            streamCount = data[19];
            twoChannelStreamCount = data[20];
            channelMapping = new byte[channels];
            System.arraycopy(data, 21, channelMapping, 0, channels);
        }
        isExtendedInfoAvailable = false;
    }

    void prepareInfoFromStream(OggPacketReader r, int sid, OggPage initialPage, OggPacket firstPacket, OggPacket secondPacket) {
        overhead_bytes = initialPage.getPageSize()-initialPage.getDataSize();
        secondPage = r.getCurrentPage();
        overhead_bytes = overhead_bytes + (secondPage.getPageSize()-secondPage.getDataSize());
        bytes = initialPage.getPageSize() + secondPage.getPageSize();
        this.sid = sid;
        overhead_bytes += firstPacket.getData().length;
        overhead_bytes += secondPacket.getData().length;
        oggPacketReader = r;
        max_packet_duration = 0;
        min_packet_duration =5760;      
        total_samples = 0;        
        total_packets = 0;
        max_page_duration = -1;
        min_page_duration = 5760*255;
        max_packet_bytes = 0;
        min_packet_bytes = 2147483647;
    }
    /**
     * Updates the statistics from the opus stream. This method will cause the processing of the entire stream
     * by this class.
     */
    public void consumeStream() {
        try {
            updateInfoFromStream();
        } catch (IOException e) {
            System.err.println("WARNING: Problem reading Opus stream:"+e.getMessage());
        }
    }

    boolean updateInfoFromStream() throws IOException {
        if (total_samples > 0 || oggPacketReader == null) {
            // already read from stream
            return false;
        }
        long lastlastgranulepos = -1;
        long lastgranulepos = 0;
        long firstgranulepos = -1;
        int page_samples = 0;
        int packets = 0;
        int page_count = 0;
        OggPacket p = null;
        //int page_packets = 0;
        OggPage currentPage = null;
        while( (p = oggPacketReader.getNextPacketWithSid(sid)) != null ) {
            packets++;
            //page_packets++;
            long gp = p.getGranulePosition();
            OggPage c = oggPacketReader.getCurrentPage();
            if (currentPage==null || currentPage.getSequenceNumber()!=c.getSequenceNumber()) {
                page_count++;
                //OggPage oldpage = currentPage;
                //if (oldpage!= null) {
                // System.err.println("Page "+oldpage.getSequenceNumber()+": samples:"+page_samples+" page packets:"+page_packets);
                //}
                currentPage = c;
                //System.err.println("New Page "+page_count+"/"+currentPage.getSequenceNumber()+" datasize:"+currentPage.getDataSize()+" gp:"+currentPage.getGranulePosition());
                //page_packets=0;

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
                //last_page_duration = page_samples;
                if (max_page_duration<page_samples) max_page_duration=page_samples;
                if (page_count > 1) {
                    if (min_page_duration>page_samples) min_page_duration=page_samples;
                }
                page_samples = 0;
                bytes = bytes + header_len+body_len;
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
            //last_packet_duration = spp;
            if (max_packet_duration<spp) max_packet_duration=spp;
            if (min_packet_duration>spp) min_packet_duration = spp;
            if (max_packet_bytes<d.length) max_packet_bytes = d.length;
            if (min_packet_bytes>d.length) min_packet_bytes = d.length;

        }
        if (max_page_duration<page_samples) max_page_duration=page_samples;
        if (min_page_duration>page_samples) min_page_duration=page_samples;

        //playtime = ((lastgranulepos-firstgranulepos-getPreSkip()) / 48);
        playtime = ((lastgranulepos-getPreSkip()) / 48);
        total_pages = page_count;
        //System.out.println("Total_samples:"+total_samples+" total_pages:"+total_pages+" total_packets:"
        // +total_packets+" maxpage:"+max_page_duration+" minpage:"+min_page_duration+ " last spp:"+page_samples);
        isExtendedInfoAvailable = true;
        return true;
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



    @Override
    public OggPacket write() {
        int length = 19;
        if (channelMappingFamily != 0) {
            length += 2;
            length += channels;
        }
        byte[] data = new byte[length];
        System.arraycopy(MAGIC_HEADER_BYTES, 0, data, 0, 8);

        data[8] = version;
        data[9] = (byte)channels; 
        IOUtils.putInt2(data, 10, preSkip);
        IOUtils.putInt4(data, 12, rate);
        IOUtils.putInt2(data, 16, outputGain);

        data[18] = channelMappingFamily;
        if (channelMappingFamily != 0) {
            data[19] = streamCount;
            data[20] = twoChannelStreamCount;
            System.arraycopy(channelMapping, 0, data, 21, channels);
        }

        setData(data);
        return super.write();
    }

    private void parseVersion() {
        minorVersion = version & 0xf;
        majorVersion = version >> 4;
    }

    public byte getVersion() {
        return version;
    }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }
    public String getVersionString() {
        return majorVersion + "." + minorVersion;
    }

    public int getChannels() {
        return channels;
    }
    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getPreSkip() {
        return preSkip;
    }
    public void setPreSkip(int preSkip) {
        this.preSkip = preSkip;
    }

    public long getRate() {
        return rate;
    }
    public void setRate(long rate) {
        this.rate = rate;
    }

    public int getOutputGain() {
        return outputGain;
    }
    public void setOutputGain(int outputGain) {
        this.outputGain = outputGain;
    }

    public byte getChannelMappingFamily() {
        return channelMappingFamily;
    }
    public byte getStreamCount() {
        return streamCount;
    }
    public byte getTwoChannelStreamCount() {
        return twoChannelStreamCount;
    }
    public byte[] getChannelMapping() {
        return channelMapping;
    }
    public double getMaxPacketDuration() {
        if (!isExtendedInfoAvailable) return 0.0;
        return (max_packet_duration/48.0);
    }
    public double getAvgPacketDuration() {
        if (!isExtendedInfoAvailable) return 0.0;
        if (total_packets > 0) {
            return (total_samples/total_packets/48.0);
        }
        return 0.0;
    }
    public double getMinPacketDuration() {
        if (!isExtendedInfoAvailable) return 0.0;
        return (min_packet_duration/48.0);
    }
    public double getMaxPageDuration() {
        if (!isExtendedInfoAvailable) return 0.0;
        return max_page_duration/48.0;
    }
    public double getAvgPageDuration() {
        if (!isExtendedInfoAvailable) return 0.0;
        if (total_pages > 0 ) {
            return total_samples/(double)total_pages/48.0;
        }
        return 0.0;
    }
    public double getMinPageDuration() {
        if (!isExtendedInfoAvailable) return 0.0;
        return min_page_duration/48.0;
    }
    public int getBytes() {
        if (!isExtendedInfoAvailable) return 0;
        return bytes;
    }
    public int getOverheadBytes() {
        if (!isExtendedInfoAvailable) return 0;
        return overhead_bytes;
    }
    public String getPlayTimeAsString() {
        if (!isExtendedInfoAvailable) return null;
        int minutes = (int)playtime / (60*1000);
        int seconds = (int)((playtime - (minutes*(60*1000))) / 1000 );
        int milliseconds = (int)((playtime - (minutes*60*1000) - (seconds*1000)));
        return String.format("%dm:%d.%03ds",minutes,seconds,milliseconds);       
    }


    public long getPlayTime() {
        if (!isExtendedInfoAvailable) return 0;
        return playtime;
    }

    public int getMaxPacketBytes() {
        if (!isExtendedInfoAvailable) return 0;
        return max_packet_bytes;
    }

    public int getMinPacketBytes() {
        if (!isExtendedInfoAvailable) return 0;
        return min_packet_bytes;
    }
}
