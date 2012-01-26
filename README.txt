           Ogg and Vorbis Tools for Java
           -----------------------------

This library is a pure Java, Apache v2 licensed project for working with
Ogg and Vorbis files

Currently, support for the Ogg container is fairly complete, offering
the ability to read, write, add and change streams within an Ogg file.
It should be possible to use the Ogg parts as a basis for dealing with
any multimedia data stored in an Ogg container.

Support for the Vorbis audio format so far concentrates on metadata.
It is possible to retrieve and change metadata (such as sampling rates,
user comments etc), and tools are provided to query and alter these
(see the Tools section below). At this time, there is no support for 
decoding or encoding audio data. Contributions for these areas would
be welcomed!

Very limited support is also included for FLAC comments (user metadata),
which use the same scheme as Vorbis.


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

  org.gagravarr.vorbis.tools.VorbisCommentTool
     Allows the listing and editing of comments (user metadata) of a Vorbis
     file. Works similar to *vorbiscomment* commandline tool that ships
     with libvorbis


Apache Tika
-----------
Included in the tika module are Parser and Detector plugins for
Apache Tika for Ogg based file formats (current Ogg, Vorbis and FLAC).

These include appropriate service registry entries for Tika to allow
these plugins to be automatically loaded by Tika. Simply ensure that the
tika module, and the core module are present on the Tika classpath and 
they will be used.


Getting Started
---------------
There are four main classes that you can start with, depending on the
kind of file you have, and your interests. These are:

  org.gagravarr.ogg.OggFile
     Provides read and write support for Ogg files of all types, working
     at the packet / stream level

  org.gagravarr.vorbis.VorbisFile
     Provides read and write support for Ogg Vorbis audio files. Provides
     access to the key parts of the Vorbis file, such as info and comments.
     (No support yet for encoding or decoding the audio packets though)

  org.gagravarr.flac.FlacOggFile
     Provides read support for FLAC files stored in an Ogg container. Allows
     access to the key parts of the file, such as Info, Tags and compressed
     audio. (No encoding or decoding of audio packets though)

  org.gagravarr.flac.FlacFile
     Provides read support for regular FLAC files. Provides access to the 
     key parts of the file, such as Info, Tags and compressed audio. 
     (No encoding or decoding of audio packets though)

The best way to see how to use the code is to look at the Tools Package,
which has examples of reading and writing tags, and the Tika Package, which
has examples of reading audio metadata. In addition, the unit tests can
provide examples of uses too.


Information Resources
---------------------
 * Ogg file format - RFC3533, plus http://xiph.org/vorbis/doc/oggstream.html 
   and http://xiph.org/vorbis/doc/framing.html
 * Vorbis - http://xiph.org/vorbis/doc/
 * FLAC - http://flac.sourceforge.net/format.html
 * FLAC in Ogg - http://flac.sourceforge.net/ogg_mapping.html
 * Tika - http://tika.apache.org/
