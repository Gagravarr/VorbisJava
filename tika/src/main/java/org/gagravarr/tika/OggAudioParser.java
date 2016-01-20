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
package org.gagravarr.tika;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMP;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.gagravarr.ogg.audio.OggAudioHeaders;
import org.gagravarr.ogg.audio.OggAudioInfoHeader;
import org.gagravarr.ogg.audio.OggAudioStatistics;
import org.gagravarr.ogg.audio.OggAudioStream;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisStyleComments;
import org.xml.sax.SAXException;

/**
 * Parent parser for the various Ogg Audio formats, such as
 *  Vorbis and Opus
 */
public abstract class OggAudioParser extends AbstractParser {
    private static final long serialVersionUID = 5168743829615945633L;
    private static final DecimalFormat DURATION_FORMAT = new DecimalFormat("0.0#");

    protected static void extractChannelInfo(Metadata metadata, OggAudioInfoHeader info) {
        extractChannelInfo(metadata, info.getNumChannels());
    }
    protected static void extractChannelInfo(Metadata metadata, int channelCount) {
        if(channelCount == 1) {
            metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "Mono"); 
        } else if(channelCount == 2) {
            metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "Stereo");
        } else if(channelCount == 5) {
            metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "5.1");
        } else if(channelCount == 7) {
            metadata.set(XMPDM.AUDIO_CHANNEL_TYPE, "7.1");
        }
    }

    protected static void extractComments(Metadata metadata, XHTMLContentHandler xhtml,
            VorbisStyleComments comments) throws TikaException, SAXException {
        // Get the specific know comments
        metadata.set(TikaCoreProperties.TITLE, comments.getTitle());
        metadata.set(TikaCoreProperties.CREATOR, comments.getArtist());
        metadata.set(XMPDM.ARTIST, comments.getArtist());
        metadata.set(XMPDM.ALBUM, comments.getAlbum());
        metadata.set(XMPDM.GENRE, comments.getGenre());
        metadata.set(XMPDM.RELEASE_DATE, comments.getDate());
        metadata.add(XMP.CREATOR_TOOL, comments.getVendor());
        metadata.add("vendor", comments.getVendor());

        for(String comment : comments.getComments("comment")) {
            metadata.add(XMPDM.LOG_COMMENT.getName(), comment);
        }

        // Grab the rest just in case
        List<String> done = Arrays.asList(new String[] {
                VorbisComments.KEY_TITLE, VorbisComments.KEY_ARTIST,
                VorbisComments.KEY_ALBUM, VorbisComments.KEY_GENRE,
                VorbisComments.KEY_DATE, VorbisComments.KEY_TRACKNUMBER,
                "vendor", "comment"
        });
        for(String key : comments.getAllComments().keySet()) {
            if(! done.contains(key)) {
                for(String value : comments.getAllComments().get(key)) {
                    metadata.add(key, value);
                }
            }
        }

        // Output as text too
        xhtml.element("h1", comments.getTitle());
        xhtml.element("p", comments.getArtist());

        // Album and Track number
        if (comments.getTrackNumber() != null) {
            xhtml.element("p", comments.getAlbum() + ", track " + comments.getTrackNumber());
            metadata.set(XMPDM.TRACK_NUMBER, comments.getTrackNumber());
        } else {
            xhtml.element("p", comments.getAlbum());
        }

        // A few other bits
        xhtml.element("p", comments.getDate());
        for(String comment : comments.getComments("comment")) {
            xhtml.element("p", comment);
        }
        xhtml.element("p", comments.getGenre());
    }

    protected static void extractDuration(Metadata metadata, XHTMLContentHandler xhtml,
            OggAudioHeaders headers, OggAudioStream audio) throws IOException, SAXException {
        // Have the statistics calculated
        OggAudioStatistics stats = new OggAudioStatistics(headers, audio);
        stats.calculate();

        // Record the duration, if available
        double duration = stats.getDurationSeconds();
        if (duration > 0) {
            // Save as metadata to the nearest .01 seconds
            metadata.add(XMPDM.DURATION, DURATION_FORMAT.format(duration));

            // Output as Hours / Minutes / Seconds / Parts
            xhtml.element("p", stats.getDuration());
        }
    }
}
