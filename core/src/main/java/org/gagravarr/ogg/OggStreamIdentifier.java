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
package org.gagravarr.ogg;

import org.gagravarr.flac.FlacFirstOggPacket;
import org.gagravarr.opus.OpusPacketFactory;
import org.gagravarr.skeleton.SkeletonPacketFactory;
import org.gagravarr.speex.SpeexPacketFactory;
import org.gagravarr.vorbis.VorbisPacketFactory;

/**
 * Detector for identifying the kind of data stored in a given stream.
 * This is normally used on the first packet in a stream, to work out 
 *  the type, if recognised.
 * TODO Provide a description too
 */
public class OggStreamIdentifier {
   // General types
   public static final String OGG_GENERAL = "application/ogg";
   public static final String OGG_VIDEO = "video/ogg";
   public static final String OGG_AUDIO = "audio/ogg";
   public static final String UNKNOWN = "application/octet-stream";
   
   // Audio types
   public static final String OGG_VORBIS = "audio/vorbis";
   public static final String OPUS_AUDIO = "audio/opus";
   public static final String OPUS_AUDIO_ALT = "audio/ogg; codecs=opus";
   public static final String SPEEX_AUDIO = "audio/speex";
   public static final String SPEEX_AUDIO_ALT = "audio/ogg; codecs=speex";
   public static final String OGG_PCM = "audio/x-oggpcm";
   
   public static final String NATIVE_FLAC = "audio/x-flac";
   public static final String OGG_FLAC = "audio/x-oggflac";
   
   // Video types
   public static final String THEORA_VIDEO = "video/theora";
   public static final String THEORA_VIDEO_ALT = "video/x-theora";
   public static final String DIRAC_VIDEO = "video/x-dirac";
   public static final String OGM_VIDEO = "video/x-ogm";
   
   public static final String OGG_UVS = "video/x-ogguvs";
   public static final String OGG_YUV = "video/x-oggyuv";
   public static final String OGG_RGB = "video/x-oggrgb";

   // Metadata types
   public static final String SKELETON = "application/annodex";
   public static final String CMML = "text/x-cmml";
   public static final String KATE = "application/kate";
   
   public static String identifyType(OggPacket p) {
       if (! p.isBeginningOfStream()) {
           // All streams so far can be identified from their first packet
           // Very few can be identified past about their 2nd or 3rd
           // So, we only support identifying off the first one
           throw new IllegalArgumentException("Can only Identify from the first packet in a stream");
       } else {
           if (p.getData() != null && p.getData().length > 10) {
               // Is it a Metadata related stream?
               if (SkeletonPacketFactory.isSkeletonStream(p)) {
                   return SKELETON;
               }
               if (isAnnodex2Stream(p)) {
                   return SKELETON;
               }
               if (isCMMLStream(p)) {
                   return CMML;
               }
               if (isKateStream(p)) {
                   return KATE;
               }

               // Is it an Audio stream?
               if (VorbisPacketFactory.isVorbisStream(p)) {
                   // Vorbis Audio stream
                   return OGG_VORBIS;
               }
               if (SpeexPacketFactory.isSpeexStream(p)) {
                   // Speex Audio stream
                   return SPEEX_AUDIO;
               }
               if (OpusPacketFactory.isOpusStream(p)) {
                   // Opus Audio stream
                   return OPUS_AUDIO;
               }
               if (FlacFirstOggPacket.isFlacStream(p)) {
                   // FLAC-in-Ogg Audio stream
                   return OGG_FLAC;
               }
               if (isOggPCMStream(p)) {
                   // PCM-in-Ogg Audio stream
                   return OGG_PCM;
               }

               // Is it a video stream?
               if (isTheoraStream(p)) {
                   return THEORA_VIDEO;
               }
               if (isDiracStream(p)) {
                   return DIRAC_VIDEO;
               }
               if (isOggOGMStream(p)) {
                   return OGM_VIDEO;
               }
               if (isOggUVSStream(p)) {
                   return OGG_UVS;
               }
               if (isOggYUVStream(p)) {
                   return OGG_YUV;
               }
               if (isOggRGBStream(p)) {
                   return OGG_RGB;
               }
           }
           // Couldn't determine what it is
           return UNKNOWN;
       }
   }

   // These methods provide first packet type detection for the
   //  various Ogg-based formats we lack general support for
   protected static final byte[] MAGIC_OGG_PCM = IOUtils.toUTF8Bytes("PCM     ");
   protected static boolean isOggPCMStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_PCM, p.getData(), 0);
   }

   protected static final byte[] MAGIC_THEORA = new byte[7];
   static {
       MAGIC_THEORA[0] = (byte)0x80;
       IOUtils.putUTF8(MAGIC_THEORA, 1, "theora");
   }
   protected static boolean isTheoraStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_THEORA, p.getData(), 0);
   }
   protected static final byte[] MAGIC_DIRAC = IOUtils.toUTF8Bytes("BBCD");
   protected static boolean isDiracStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_DIRAC, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_OGM = IOUtils.toUTF8Bytes("video");
   protected static boolean isOggOGMStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_OGM, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_UVS = IOUtils.toUTF8Bytes("UVS ");
   protected static boolean isOggUVSStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_UVS, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_YUV = IOUtils.toUTF8Bytes("\1YUV");
   protected static boolean isOggYUVStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_YUV, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_RGB = IOUtils.toUTF8Bytes("\1GBP");
   protected static boolean isOggRGBStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_RGB, p.getData(), 0);
   }

   protected static final byte[] MAGIC_CMML = IOUtils.toUTF8Bytes("CMML\0\0\0\0");
   protected static boolean isCMMLStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_CMML, p.getData(), 0);
   }
   protected static final byte[] MAGIC_KATE = IOUtils.toUTF8Bytes("kate\0\0\0");
   protected static boolean isKateStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_KATE, p.getData(), 0);
   }
   protected static final byte[] MAGIC_ANNODEX2 = IOUtils.toUTF8Bytes("Annodex\0");
   protected static boolean isAnnodex2Stream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_ANNODEX2, p.getData(), 0);
   }
}
