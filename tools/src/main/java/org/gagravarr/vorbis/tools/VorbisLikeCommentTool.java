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
package org.gagravarr.vorbis.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gagravarr.vorbis.VorbisStyleComments;
import org.gagravarr.vorbis.tools.VorbisLikeCommentTool.Command.Commands;

/**
 * A general class for working on files which have Vorbis-like
 *  comments in them, eg Vorbis or Opus.
 * Works much like the  vorbiscomment program.
 */
public abstract class VorbisLikeCommentTool {
    public static Command processArgs(String[] args, String programName) 
            throws Exception {
        if(args.length == 0) {
            printHelp(programName);
            return null; // Never reached
        }

        if(args.length == 1) {
            return new Command(args[0], null, null, Command.Commands.List);
        }
        if(args.length == 2 && args[0].equals("-l")) {
            return new Command(args[1], null, null, Command.Commands.List);
        }

        if(args[0].equals("-w")) {
            return processTags(args, Command.Commands.Overwrite);
        }
        if(args[0].equals("-a")) {
            return processTags(args, Command.Commands.Append);
        }
        
        printHelp(programName);
        return null; // Never reached
    }

    public static void printHelp(String programName) {
        System.err.println("Use:");
        System.err.println("  "+programName+" [-l] file.ogg");
        System.err.println("  "+programName+" -a [-t name=value] [-t name=value] in.ogg [out.ogg]");
        System.err.println("  "+programName+" -w [-t name=value] [-t name=value] in.ogg [out.ogg]");
        System.exit(1);
    }
    
    protected static class Command {
        protected String inFile;
        protected String outFile;
        protected List<String> tagPairs;
        protected Commands command;
        protected enum Commands { List, Append, Overwrite, Help }
        
        protected Command(String inFile, String outFile, List<String> tagPairs, Commands command) {
            this.inFile = inFile;
            this.outFile = outFile;
            this.tagPairs = tagPairs;
            this.command = command;
        };
    }

    
    public static void listTags(VorbisStyleComments vorbisComments) throws Exception {
        Map<String, List<String>> comments =
                vorbisComments.getAllComments();
        for(String tag : comments.keySet()) {
            for(String value : comments.get(tag)) {
                System.out.println(tag + "=" + value);
            }
        }
    }

    public static Command processTags(String[] args, Command.Commands type) {
        String outFile = args[args.length - 1];
        String inFile = outFile;
        if (args.length % 2 == 1 ) {
            inFile = args[args.length - 2];
        }

        System.out.println("Source file: " + inFile);
        System.out.println("Output file: " + outFile);

        List<String> tags = new ArrayList<String>();
        for (int i=1; i<args.length-2; i += 2) {
            if (! "-t".equals(args[i]) || ! args[i+1].contains("=")) {
                throw new IllegalArgumentException(
                        "Expecting '-t name=value' but found '" +
                                args[i] + " " + args[i+1] + "'"
                );
            }
            tags.add(args[i+1]);
        }
        
        return new Command(inFile, outFile, tags, type);
    }
    
    public static void addTags(VorbisStyleComments vorbisComments, Command command) {
        if (command.command == Commands.Overwrite) {
            vorbisComments.removeAllComments();
        }
        
        for (String tagPair : command.tagPairs) {
            int split = tagPair.indexOf('=');
            String name = tagPair.substring(0, split);
            String value = tagPair.substring(split+1);
            System.out.println("   Setting '" + name + " = " + value + "'");

            vorbisComments.addComment(name, value);
        }
    }
}
