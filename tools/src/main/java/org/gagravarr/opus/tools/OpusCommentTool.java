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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.vorbis.tools.VorbisLikeCommentTool;
import org.gagravarr.vorbis.tools.VorbisLikeCommentTool.Command.Commands;

/**
 * A class for listing and editing Comments (Tags) within an
 *  Opus File, much like the vorbiscomments program (but Opus)
 */
public class OpusCommentTool extends VorbisLikeCommentTool {
	
	
    public static void main(String[] args) throws Exception {
        Command command = processArgs(args, "OpusComment");
        
        OpusFile op = new OpusFile(new File(command.inFile));
        
        if (command.command == Commands.List) {
            listTags(op.getTags());
        } else {
            // Have the new tags added
            addTags(op.getTags(), command);
            
            // Write out
            List<OpusAudioData> audio = new ArrayList<OpusAudioData>();
            OpusAudioData ad;
            while( (ad = op.getNextAudioPacket()) != null ) {
                audio.add(ad);
            }

            // Now write out
            op.close();
            OpusFile out = new OpusFile(
                    new FileOutputStream(command.outFile),
                    op.getSid(),
                    op.getInfo(),
                    op.getTags()
            );
            for(OpusAudioData oad : audio) {
                out.writeAudioData(oad);
            }
            out.close();
        }
    }
}
