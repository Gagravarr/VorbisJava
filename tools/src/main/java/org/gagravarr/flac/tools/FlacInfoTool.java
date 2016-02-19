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
package org.gagravarr.flac.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.gagravarr.flac.FlacAudioFrame;
import org.gagravarr.flac.FlacAudioSubFrame;
import org.gagravarr.flac.FlacAudioSubFrame.SubFrameLPC;
import org.gagravarr.flac.FlacAudioSubFrame.SubFrameWithResidual;
import org.gagravarr.flac.FlacFile;
import org.gagravarr.flac.FlacInfo;
import org.gagravarr.flac.FlacOggFile;
import org.gagravarr.flac.FlacTags;

/**
 * Prints out information on the contents of a FLAC file,
 *  including metadata and frame-level details.
 * It's similar to the analysis from "flac -a"
 */
public class FlacInfoTool {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.err.println("Use:");
            System.err.println("   FlacInfoTool <file> [file] [file]");
            System.exit(1);
        }

        for(String f : args) {
            FlacInfoTool info = new FlacInfoTool(new File(f));
            info.printMetadataInfo();
            info.printFrameInfo();
            info.flac.close();
        }
    }

    private FlacFile flac;
    public FlacInfoTool(File f) throws FileNotFoundException, IOException {
        if(! f.exists()) {
            throw new FileNotFoundException(f.toString());
        }

        flac = FlacFile.open(f);
    }

    private static final String SPACER = "  ";
    private static final String INDENT1 = "   ";
    private static final String INDENT2 = "      ";

    public void printMetadataInfo() throws IOException {
        if (flac instanceof FlacOggFile) {
            FlacOggFile ogg = (FlacOggFile)flac;
            System.out.println("FLAC-in-Ogg, in stream " + ogg.getSid());
        } else {
            System.out.println("FLAC Native");
        }

        // Output The information
        FlacInfo info = flac.getInfo();
        System.out.println(INDENT1 + "Min Block Size=" + info.getMinimumBlockSize());
        System.out.println(INDENT1 + "Max Block Size=" + info.getMaximumBlockSize());
        System.out.println(INDENT1 + "Min Frame Size=" + info.getMinimumFrameSize());
        System.out.println(INDENT1 + "Max Frame Size=" + info.getMaximumFrameSize());
        System.out.println(INDENT1 + "Num Channels=" + info.getNumChannels());
        System.out.println(INDENT1 + "Bits Per Sample=" + info.getBitsPerSample());
        System.out.println(INDENT1 + "Sample Rate=" + info.getSampleRate());
        System.out.println(INDENT1 + "Num Samples=" + info.getNumberOfSamples());
        System.out.println(INDENT1 + "Pre Skip=" + info.getPreSkip());

        // Output a comments summary
        FlacTags tags = flac.getTags();
        System.out.println(tags.getAllComments().size() + " Comments:");
        for (String tag : tags.getAllComments().keySet()) {
            System.out.println(INDENT1 + tag);
            for (String value : tags.getAllComments().get(tag)) {
                System.out.println(INDENT2 + value);
            }
        }
        System.out.println();
    }

    public void printFrameInfo() throws IOException {
        int fn = -1;
        FlacAudioFrame audio;
        while ((audio=flac.getNextAudioPacket()) != null) {
            fn++;

            System.out.print("frame="+fn);
            System.out.print(SPACER);
            System.out.print("offset=??");
            System.out.print(SPACER);
            System.out.print("bits="+(audio.getData().length*8));
            System.out.print(SPACER);
            System.out.print("blocksize="+audio.getBlockSize());
            System.out.print(SPACER);
            System.out.print("sample_rate="+audio.getSampleRate());
            System.out.print(SPACER);
            System.out.print("channels="+audio.getNumChannels());
            System.out.print(SPACER);
            System.out.print("channel_assignment=??");
            System.out.println();

            for (int sfn=0; sfn<audio.getSubFrames().length; sfn++) {
                FlacAudioSubFrame sf = audio.getSubFrames()[sfn];
                System.out.print(INDENT1);
                System.out.print("subframe="+sfn);
                System.out.print(SPACER);
                System.out.print("wasted_bits="+sf.getWastedBits());
                System.out.print(SPACER);
                System.out.print("type="+sf.getType());
                System.out.print(SPACER);
                System.out.print("order="+sf.getPredictorOrder());

                if (sf instanceof SubFrameLPC) {
                    SubFrameLPC sflpc = (SubFrameLPC)sf;
                    System.out.print(SPACER);
                    System.out.print("qlp_coeff_precision="+sflpc.getLinearPredictorCoefficientPrecision());
                    System.out.print(SPACER);
                    System.out.print("quantization_level="+sflpc.getLinearPredictorCoefficientShift());
                }
                if (sf instanceof SubFrameWithResidual) {
                    SubFrameWithResidual sfr = (SubFrameWithResidual)sf;
                    System.out.print(SPACER);
                    System.out.print("residual_type="+sfr.getResidual().getType());
                    System.out.print(SPACER);
                    System.out.print("partition_order="+sfr.getResidual().getPartitionOrder());
                    System.out.println();

                    if (sf instanceof SubFrameLPC) {
                        SubFrameLPC sflpc = (SubFrameLPC)sf;
                        for (int qc=0; qc<sflpc.getCoefficients().length; qc++) {
                            System.out.print(INDENT2);
                            System.out.print("qlp_coeff["+qc+"]="+sflpc.getCoefficients()[qc]);
                            System.out.println();
                        }
                    }
                    for (int wn=0; wn<sfr.getWarmUpSamples().length; wn++) {
                        System.out.print(INDENT2);
                        System.out.print("warmup["+wn+"]="+sfr.getWarmUpSamples()[wn]);
                        System.out.println();
                    }
                    for (int pn=0; pn<sfr.getResidual().getNumPartitions(); pn++) {
                        System.out.print(INDENT2);
                        System.out.print("parameter["+pn+"]="+sfr.getResidual().getRiceParams()[pn]);
                        System.out.println();
                    }
                } else {
                    // Rest TODO
                    System.out.println();
                }
            }
        }
    }
}
