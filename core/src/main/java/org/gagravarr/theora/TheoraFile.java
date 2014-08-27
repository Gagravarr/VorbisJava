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
package org.gagravarr.theora;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggPacketWriter;
import org.gagravarr.ogg.OggStreamAudioData;

/**
 * This is a wrapper around an OggFile that lets you
 *  get at all the interesting bits of a Theora file.
 * TODO including soundtracks
 * TODO including skeletons
 */
public class TheoraFile extends HighLevelOggStreamPacket implements Closeable {
    private OggFile ogg;
    private OggPacketReader r;
    private OggPacketWriter w;
    private int sid = -1;

    private TheoraInfo info;
    private TheoraComments comments;
    private TheoraSetup setup;

    // TODO Soundtracks

    private List<TheoraVideoData> writtenPackets;

    /**
     * Opens the given file for reading
     * TODO Support soundtracks
     */
    public TheoraFile(File f) throws IOException, FileNotFoundException {
        this(new OggFile(new FileInputStream(f)));
    }
    /**
     * Opens the given file for reading
     * TODO Support soundtracks
     */
    public TheoraFile(OggFile ogg) throws IOException {
        this(ogg.getPacketReader());
        this.ogg = ogg;
    }
    /**
     * Loads a Theora File from the given packet reader.
     * TODO Support soundtracks
     */
    public TheoraFile(OggPacketReader r) throws IOException {
        this.r = r;

        OggPacket p = null;
        while( (p = r.getNextPacket()) != null ) {
            if (p.isBeginningOfStream() && p.getData().length > 10) {
                if (TheoraPacketFactory.isTheoraStream(p)) {
                    sid = p.getSid();
                    break; // TODO Soundtracks?
                } else {
                    // TODO Is it a soundtrack though?
                }
            }
        }
        if (sid == -1) {
            throw new IllegalArgumentException("Supplied File is not Theora");
        }

        // First three packets are required to be info, comments, setup
        info = (TheoraInfo)TheoraPacketFactory.create( p );
        comments = (TheoraComments)TheoraPacketFactory.create( r.getNextPacketWithSid(sid) );
        setup = (TheoraSetup)TheoraPacketFactory.create( r.getNextPacketWithSid(sid) );
        // TODO What about audio / soundtracks?

        // Everything else should be video data
    }

    /**
     * Opens for writing.
     */
    public TheoraFile(OutputStream out) {
        this(out, new TheoraInfo(), new TheoraComments(), new TheoraSetup());
    }
    /**
     * Opens for writing, based on the settings
     *  from a pre-read file. The Steam ID (SID) is
     *  automatically allocated for you.
     */
    public TheoraFile(OutputStream out, TheoraInfo info, TheoraComments comments, TheoraSetup setup) {
        this(out, -1, info, comments, setup);
    }
    /**
     * Opens for writing, based on the settings
     *  from a pre-read file, with a specific
     *  Steam ID (SID). You should only set the SID
     *  when copying one file to another!
     */
    public TheoraFile(OutputStream out, int sid, TheoraInfo info, TheoraComments comments, TheoraSetup setup) {
        ogg = new OggFile(out);

        if(sid > 0) {
            w = ogg.getPacketWriter(sid);
            this.sid = sid;
        } else {
            w = ogg.getPacketWriter();
            this.sid = w.getSid();
        }

        writtenPackets = new ArrayList<TheoraVideoData>();

        // TODO What about soundtracks?

        this.info = info;
        this.comments = comments;
        this.setup = setup;
    }

    /**
     * Returns the Ogg Stream ID
     */
    public int getSid() {
        return sid;
    }

    public TheoraInfo getInfo() {
        return info;
    }
    public TheoraComments getComments() {
        return comments;
    }
    public TheoraSetup getSetup() {
        return setup;
    }


    /**
     * Buffers the given video ready for writing
     *  out. Data won't be written out yet, you
     *  need to call {@link #close()} to do that,
     *  because we assume you'll still be populating
     *  the Info/Comment/Setup objects
     */
    public void writeVideoData(TheoraVideoData data) {
        writtenPackets.add(data);
    }
    /**
     * Buffers the given audio ready for writing
     *  out, to a given (pre-existing) audio stream. 
     * Data won't be written out yet, you
     *  need to call {@link #close()} to do that,
     *  because we assume you'll still be populating
     *  the Info/Comment/Setup objects
     */
    public void writeAudioData(OggStreamAudioData data, int sid) {
        // TODO
    }

    /**
     * In Reading mode, will close the underlying ogg
     *  file and free its resources.
     * In Writing mode, will write out the Info, Comment
     *  Tags objects, and then the video and audio data.
     * TODO Support Skeletons too
     */
    public void close() throws IOException {
        if(r != null) {
            r = null;
            ogg.close();
            ogg = null;
        }
        if(w != null) {
            w.bufferPacket(info.write(), true);
            w.bufferPacket(comments.write(), true);
            w.bufferPacket(setup.write(), true);

            // TODO Write video, with some sort of granule
            // TODO Write audio
            // TODO Write skeleton

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
