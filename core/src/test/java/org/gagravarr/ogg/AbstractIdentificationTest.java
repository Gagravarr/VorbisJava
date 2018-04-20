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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.gagravarr.ogg.OggStreamIdentifier.OggStreamType;

import junit.framework.TestCase;

/**
 * Parent class of tests which perform identification on Ogg files
 */
public abstract class AbstractIdentificationTest extends TestCase {
    protected static InputStream getTestFile(String name) throws IOException {
        return AbstractIdentificationTest.class.getResourceAsStream("/"+name);
    }
    protected static InputStream getTestOggFile() throws IOException {
        return getTestFile("testBoundaries.ogg");
    }
    protected static InputStream getTestVorbisFile() throws IOException {
        return getTestFile("testVORBIS.ogg");
    }
    protected static InputStream getTestSpeexFile() throws IOException {
        return getTestFile("testSPEEX.spx");
    }
    protected static InputStream getTestOpusFile() throws IOException {
        return getTestFile("testOPUS_09.opus");
    }
    protected static InputStream getTestFlacOggFile() throws IOException {
        return getTestFile("testFLAC.oga");
    }
    protected static InputStream getTestFlacNativeFile() throws IOException {
        return getTestFile("testFLAC.flac");
    }
    protected static InputStream getTestDaalaFile() throws IOException {
        return getTestFile("testDaala.ogg");
    }
    protected static InputStream getTestTheoraFile() throws IOException {
        return getTestFile("testTheora.ogg");
    }
    protected static InputStream getTestTheoraSkeletonFile() throws IOException {
        return getTestFile("testTheoraSkeleton.ogg");
    }
    protected static InputStream getTestTheoraSkeletonCMMLFile() throws IOException {
        return getTestFile("testTheoraSkeletonCMML.ogg");
    }
    protected static InputStream getTestKateFile() throws IOException {
        return getTestFile("testKate.ogx");
    }
    protected static InputStream getTestKateVorbisFile() throws IOException {
        return getTestFile("testKateVORBIS.ogg");
    }
    protected static InputStream getDummy() throws IOException {
        return new ByteArrayInputStream(new byte[] { 0,1,2,3,4,5,6,7 });
    }

    /**
     * Truncates the given stream at a certain size
     */
    protected static InputStream truncate(InputStream source, int truncateAt) throws IOException {
        byte[] data = new byte[truncateAt];
        IOUtils.readFully(source, data);
        return new ByteArrayInputStream(data);
    }

    public static void assertTypeOfFirstStream(String expectedType, OggFile ogg) 
            throws IOException {
        OggPacket p = ogg.getPacketReader().getNextPacket();
        assertNotNull(p);
        assertEquals(expectedType, OggStreamIdentifier.identifyType(p).mimetype);
    }
    public static void assertTypeOfFirstStream(OggStreamType type, OggFile ogg)
            throws IOException {
        assertTypeOfFirstStream(type.mimetype, ogg);
    }
}
