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
package org.gagravarr.ogg.audio;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.gagravarr.ogg.OggStreamAudioData;
import org.gagravarr.ogg.OggStreamPacket;


/**
 * For computing statistics around an {@link OggAudioStream},
 *  such as how long it lasts.
 * Format specific subclasses may be able to also identify 
 *  additional statistics beyond these.
 */
public class OggAudioStatistics {
    private final OggAudioStream audio;
    private final OggAudioHeaders headers;

    private int audioPackets = 0;
    private long lastGranule = -1;
    private double durationSeconds = 0;

    private long oggOverheadSize = 0;
    private long headerOverheadSize = 0;
    private long audioDataSize = 0;

    public OggAudioStatistics(OggAudioHeaders headers, OggAudioStream audio) throws IOException {
        this.audio = audio;
        this.headers = headers;
    }

    /**
     * Calculate the statistics
     */
    public void calculate() throws IOException {
        OggStreamAudioData data;

        // Calculate the headers sizing
        OggAudioInfoHeader info = headers.getInfo();
        handleHeader(info);
        handleHeader(headers.getTags());
        handleHeader(headers.getSetup());

        // Have each audio packet handled, tracking at least granules
        while ((data = audio.getNextAudioPacket()) != null) {
            handleAudioData(data);
        }

        // Calculate the duration from the granules, if found
        if (lastGranule > 0) {
            durationSeconds = ((double)lastGranule) / info.getSampleRate();
        }
    }

    protected void handleHeader(OggStreamPacket header) {
        if (header != null) {
            oggOverheadSize += header.getOggOverheadSize();
            headerOverheadSize += header.getData().length;
        }
    }

    protected void handleAudioData(OggStreamAudioData audioData) {
        audioPackets++;
        audioDataSize += audioData.getData().length;
        oggOverheadSize += audioData.getOggOverheadSize();

        if (audioData.getGranulePosition() > lastGranule) {
            lastGranule = audioData.getGranulePosition();
        }
    }

    /**
     * Returns the duration of the audio, in seconds.
     */
    public double getDurationSeconds() {
        return durationSeconds;
    }
    /**
     * Returns the duration, in Hours:Minutes:Seconds.MS
     */
    public String getDuration() {
        long duration = (long)getDurationSeconds();

        // Output as Hours / Minutes / Seconds / Parts
        long hours = TimeUnit.SECONDS.toHours(duration);
        long mins = TimeUnit.SECONDS.toMinutes(duration) - (hours*60);
        double secs = getDurationSeconds() - (((hours*60)+mins)*60);

        return String.format("%02d:%02d:%05.2f", hours, mins, secs);
    }

    /**
     * The last granule (time position) in the audio stream
     */
    public long getLastGranule() {
        return lastGranule;
    }

    /**
     * The number of audio packets in the stream
     */
    public int getAudioPacketsCount() {
        return audioPackets;
    }
    /**
     * The size, in bytes, of all the audio data
     */
    public long getAudioDataSize() {
        return audioDataSize;
    }
    /**
     * The size, in bytes, of the audio headers at the
     *  start of the file
     */
    public long getHeaderOverheadSize() {
        return headerOverheadSize;
    }
    /**
     * The percentage, from 0 to 100, of the ogg page overhead 
     *  of all the packets (audio data and audio headers)
     */
    public float getOggOverheadPercentage() {
        long total = audioDataSize+headerOverheadSize+oggOverheadSize;
        return (100f * oggOverheadSize) / total;
    }
    /**
     * The size, in bytes, of the ogg page overhead of all
     *  the packets (audio data and audio headers)
     */
    public long getOggOverheadSize() {
        return oggOverheadSize;
    }
}
