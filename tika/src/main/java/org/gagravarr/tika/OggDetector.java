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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.ogg.OggStreamIdentifier.OggStreamType;
import org.gagravarr.skeleton.SkeletonPacket;
import org.gagravarr.skeleton.SkeletonPacketFactory;

/**
 * Detector for identifying specific file types stored
 *  within an Ogg container.
 * Xiph provide a fairly unhelpful guide to mimetypes at
 *  https://wiki.xiph.org/index.php/MIME_Types_and_File_Extensions
 *  but we try to use more specific ones, as given by the Tika
 *  mimetypes xml file.
 */
public class OggDetector implements Detector {
   private static final long serialVersionUID = 591382028699008553L;

   public static final MediaType OGG_GENERAL = MediaType.application("ogg");

   public MediaType detect(InputStream input, Metadata metadata)
         throws IOException {
      // Check if we have access to the document
      if (input == null) {
          return MediaType.OCTET_STREAM;
      }

      // Ensure we can mark and reset before detecting
      // Otherwise bail, to avoid us nibbling an unwindable stream
      if (!input.markSupported()) {
          return MediaType.OCTET_STREAM;
      }

      // Check if the document starts with the OGG header
      input.mark(4);
      try {
          if (input.read() != (byte)'O' || input.read() != (byte)'g'
                  || input.read() != (byte)'g' || input.read() != (byte)'S') {
              return MediaType.OCTET_STREAM;
          }
      } finally {
          input.reset();
      }

      // We can only detect the exact type when given a TikaInputStream
      TikaInputStream tis = TikaInputStream.cast(input);
      if (tis != null) {
         // We could potentially need to go a long way through the
         //  file in order to figure out what it is
         tis.mark((int)tis.getLength()+1);

         OggFile ogg = new OggFile(tis);

         // The things we'll want to keep track of
         int totalStreams = 0;
         List<Integer> sids = new ArrayList<Integer>();
         Map<OggStreamType, Integer> streams = 
                 new HashMap<OggStreamType, Integer>();
         Map<Integer, List<SkeletonPacket>> skeletonStreams =
                 new HashMap<Integer, List<SkeletonPacket>>();

         // Check the streams in turn
         OggPacketReader r = ogg.getPacketReader();
         OggPacket p;
         Integer sid;
         while( (p = r.getNextPacket()) != null ) {
            if(p.isBeginningOfStream()) {
               totalStreams++;
               sids.add(p.getSid());

               OggStreamType type = OggStreamIdentifier.identifyType(p);

               // If it's a Skeleton stream, start tracking
               if (type == OggStreamIdentifier.SKELETON) {
                   List<SkeletonPacket> sp = new ArrayList<SkeletonPacket>();
                   sp.add(SkeletonPacketFactory.create(p));
                   skeletonStreams.put(p.getSid(), sp);
               }

               // Increment the per-type count
               Integer num = streams.get(type);
               if (num == null) {
                   num = 1;
               } else {
                   num = num + 1;
               }
               streams.put(type, num);
            } else {
                sid = p.getSid();

                // Is it a skeleton stream?
                if (skeletonStreams.containsKey(sid)) {
                    skeletonStreams.get(sid).add(SkeletonPacketFactory.create(p));
                } else {
                    // We don't worry about later packets in non-skeleton
                    // streams at this stage, only parsers mind about them
                }
            }
         }

         // Tidy
         tis.reset();

         // TODO See if we found any of the Ogg Metadata streams,
         //  eg Ogg Skeleton / Annodex or CMML, and if so use them
         //  to help identify the relationship between the different
         //  streams and hence the type

         // Can we identify what it really is?
         // First up, is it a simple single stream file?
         if (totalStreams == 1) {
             OggStreamType type = streams.keySet().iterator().next();
             return toMediaType(type);
         }

         // Is it one with a single non-metadata stream?
         int nonMetadataStreams = 0;
         for (OggStreamType type : streams.keySet()) {
             if (type.kind != OggStreamType.Kind.METADATA) {
                 nonMetadataStreams += streams.get(type);
             }
         }

         if (nonMetadataStreams == 0) {
             // Pure metadata, report as general ogg
             return OGG_GENERAL;
         }

         if (nonMetadataStreams == 1) {
             // Report as the non metadata kind
             for (OggStreamType type : streams.keySet()) {
                 if (type.kind != OggStreamType.Kind.METADATA) {
                     return toMediaType(type);
                 }
             }
         }


         // Is it a single video stream, with zero or more audio streams?
         int videoCount = 0;
         int audioCount = 0;
         OggStreamType videoType = null;
         for (OggStreamType type : streams.keySet()) {
             if (type.kind == OggStreamType.Kind.VIDEO) {
                 videoType = type;
                 videoCount += streams.get(type);
             }
             if (type.kind == OggStreamType.Kind.AUDIO) {
                 audioCount += streams.get(type);
             }
         }
         if (videoCount == 1) {
             // Report it as the video type, not the audio within that
             return toMediaType(videoType);
         }


         // Is it multiple audio streams, with no video?
         if (videoCount == 0 && audioCount > 1) {
             // Are they all the same audio kind?
             // TODO
         }

         // Is it multiple video streams?
         if (videoCount > 1) {
             // Are they all the same video kind?
             // TODO
         }

         // If we get here, then we can't work out what it is
      }

      // Couldn't determine a more specific type
      return OGG_GENERAL;
   }

   /**
    * Converts from our type to Tika's type
    */
   protected static MediaType toMediaType(OggStreamType type) {
       if (type == OggStreamIdentifier.UNKNOWN) {
           // We don't have a specific type available to return
           return OGG_GENERAL;
       } else {
           // Say it's the specific type we found
           return MediaType.parse(type.mimetype);
       }
   }
}
