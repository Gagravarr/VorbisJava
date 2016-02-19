           Ogg and Vorbis Tools for Java
           -----------------------------

This library is a pure Java, Apache v2 licensed project for working with
Ogg, Vorbis, FLAC, Opus, Speex and Theora files

Currently, support for the Ogg container is fairly complete, offering
the ability to read, write, add and change streams within an Ogg file.
It should be possible to use the Ogg parts as a basis for dealing with
any multimedia data stored in an Ogg container. There is basic support for
Skeleton Annodex streams, which provide metadata on top of Ogg files about
the streams, but it isn't fully integrated.

Support for the Vorbis audio format so far concentrates on metadata.
It is possible to retrieve and change metadata (such as sampling rates,
user comments etc), and tools are provided to query and alter these
(see the Tools section below). At this time, there is no support for 
decoding or encoding audio data. Contributions for these areas would
be welcomed!

Opus and Speex support is slightly less than that of Vorbis, covering 
retrieving of metadata (such as sampling rates, user comments etc). However, 
basic Opus or Speex audio frame support is outstanding. Tooling exists
for querying and changing metadata for Opus only. Contributions to expand 
Opus or Speex support are most appreciated!

Very limited support is also included for FLAC comments (user metadata),
which use the same scheme as Vorbis. FLAC-native and FLAC-in-Ogg files
are both supproted for extracting the user metadata.

Theora support is of a similar level to that for Opus and Speex, providing
basic access to metadata (both video and soundtrack), but not much else.


Tools
-----
As part of the project, a small number of pure Java tools are provided
to help when working with Ogg and Vorbis files.

Two jar files are available. **vorbis-java-tools.jar** should be used when
embedding the code, while **vorbis-java-tools-jar-with-dependencies.jar**
is what should be used for standlone cases, as all dependencies are
included.

  org.gagravarr.ogg.tools.OggInfoTool
     Prints basic information on the streams in a file

  org.gagravarr.vorbis.tools.VorbisInfoTool
     Prints detailed information on the contents of a Vorbis file, including
     versions, comments, bitrates, channels and audio rates, codebooks
     and file length

  org.gagravarr.opus.tools.OpusInfoTool
     Prints summary information on the contents of a Opus file, including
     versions, comments, channels and audio rates and file length

  org.gagravarr.flac.tools.FlacInfoTool
     Prints detailed information on the contents of a FLAC file, including
     versions, comments, channels, frames and subframes.

  org.gagravarr.skeleton.tools.SkeletonInfoTool
     Prints information on a skeleton-described ogg file, reporting on
     a per-bone basis about the streams described and their metadata

  org.gagravarr.vorbis.tools.VorbisCommentTool
     Allows the listing and editing of comments (user metadata) of a Vorbis
     file. Works similar to *vorbiscomment* commandline tool that ships
     with libvorbis

  org.gagravarr.opus.tools.OpusCommentTool
     Allows the listing and editing of comments (user metadata) of an Opus
     file. Works similar to *vorbiscomment* commandline tool that ships
     with libvorbis, except for Opus files

  org.gagravarr.flac.tools.FlacCommentTool
     Allows the listing of comments (user metadata) of a FlAC file (native or
     ogg contained) file. Works similar to *vorbiscomment* commandline tool
     that ships with libvorbis, except for FLAC files, and only supports listing


Apache Tika
-----------
Included in the tika module are Parser and Detector plugins for Apache Tika for
Ogg based file formats. Currently, parsers are only available for the Audio
formats (Vorbis, Opus, Speex, Flac), but a basic stream info outputting parser
exists for other Ogg types. Basic Theora video support exists, but full support
is blocked pending a Tika decision on the best way to expose metadata for
multiple streams.

Detection should handle all the well known Ogg formats (Audio and Video).

The parsers and detector include appropriate service registry entries for Tika 
to allow these plugins to be automatically loaded by Tika. Simply ensure that 
the tika module, and the core module are present on the Tika classpath and 
they will be used.


Getting Started
---------------
There are seven main classes that you can start with, depending on the
kind of file you have, and your interests. These are:

  org.gagravarr.ogg.OggFile
     Provides read and write support for Ogg files of all types, working
     at the packet / stream level

  org.gagravarr.vorbis.VorbisFile
     Provides read and write support for Ogg Vorbis audio files. Provides
     access to the key parts of the Vorbis file, such as info and comments.
     (No support yet for encoding or decoding the audio packets though)

  org.gagravarr.opus.OpusFile
     Provides read support for Ogg Opus audio files. Provides access to 
     the key parts of the Opus file, such as info and comments.
     (No support at all for audio packets, for now)

  org.gagravarr.speex.SpeexFile
     Provides read support for Ogg Speex audio files. Provides access to 
     the key parts of the Speex file, such as info and comments.
     (No support at all for audio packets, for now)

  org.gagravarr.flac.FlacOggFile
     Provides read support for FLAC files stored in an Ogg container. Allows
     access to the key parts of the file, such as Info, Tags and compressed
     audio. (No encoding or decoding of audio packets though)

  org.gagravarr.flac.FlacFile
     Provides read support for regular FLAC files. Provides access to the 
     key parts of the file, such as Info, Tags and compressed audio. 
     (No encoding or decoding of audio packets though)

  org.gagravarr.skeleton.SkeletonStream
     Provides read support for a Skeleton metadata stream, typically
     contained within a video or audio file.

The best way to see how to use the code is to look at the Tools Package,
which has examples of reading and writing tags, and the Tika Package, which
has examples of reading audio metadata. In addition, the unit tests can
provide examples of uses too.


Information Resources
---------------------
 * Ogg file format - RFC3533, plus http://xiph.org/vorbis/doc/oggstream.html 
   and http://xiph.org/vorbis/doc/framing.html
 * Vorbis - http://xiph.org/vorbis/doc/
 * Opus - http://www.opus-codec.org/docs/
 * FLAC - https://xiph.org/flac/format.html
 * FLAC in Ogg - https://xiph.org/flac/ogg_mapping.html
 * Tika - http://tika.apache.org/
