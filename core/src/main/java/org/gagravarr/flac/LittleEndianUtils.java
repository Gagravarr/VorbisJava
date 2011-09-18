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
package org.gagravarr.flac;

import java.io.IOException;
import java.io.OutputStream;

import org.gagravarr.ogg.IOUtils;

/**
 * While Ogg and Vorbis are Big Endian, FLAC is 
 *  normally Little Endian
 */
public class LittleEndianUtils {
   public static int toInt(byte b) {
      return IOUtils.toInt(b);
   }
   public static byte fromInt(int i) {
      return IOUtils.fromInt(i);
   }
   
   public static int getLEInt2(byte[] data) {
      return getLEInt2(data, 0);
   }
   public static int getLEInt2(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        return getLEInt(b0, b1);
   }
   
	public static long getLEInt3(byte[] data) {
		return getLEInt3(data, 0);
	}
	public static long getLEInt3(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        return getLEInt(b0, b1, b2);
	}
	
	public static long getLEInt4(byte[] data) {
		return getLEInt4(data, 0);
	}
	public static long getLEInt4(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        return getLEInt(b0, b1, b2, b3);
	}
	
   public static int getLEInt(int i0, int i1) {
      return (i0 << 8) + (i1 << 0);
   }
	public static long getLEInt(int i0, int i1, int i2) {
        return (i0 << 16) + (i1 << 8) + (i2 << 0);
	}
	public static long getLEInt(int i0, int i1, int i2, int i3) {
        return (i0 << 24) + (i1 << 16) + (i2 << 8) + (i3 << 0);
	}
	
	
   public static void writeLEInt2(OutputStream out, int v) throws IOException {
      byte[] b2 = new byte[2];
      putLEInt2(b2, 0, v);
      out.write(b2);
   }
   public static void putLEInt2(byte[] data, int offset, int v) {
        int i = offset;
        data[i+1] = (byte)((v >>>  0) & 0xFF);
        data[i+0] = (byte)((v >>>  8) & 0xFF);
   }
   
	public static void writeLEInt3(OutputStream out, long v) throws IOException {
		byte[] b3 = new byte[3];
		putLEInt3(b3, 0, v);
		out.write(b3);
	}
	public static void putLEInt3(byte[] data, int offset, long v) {
        int i = offset;
        data[i+2] = (byte)((v >>>  0) & 0xFF);
        data[i+1] = (byte)((v >>>  8) & 0xFF);
        data[i+0] = (byte)((v >>> 16) & 0xFF);
	}
	
	public static void writeLEInt4(OutputStream out, long v) throws IOException {
		byte[] b4 = new byte[4];
		putLEInt4(b4, 0, v);
		out.write(b4);
	}
	public static void putLEInt4(byte[] data, int offset, long v) {
        int i = offset;
        data[i+3] = (byte)((v >>>  0) & 0xFF);
        data[i+2] = (byte)((v >>>  8) & 0xFF);
        data[i+1] = (byte)((v >>> 16) & 0xFF);
        data[i+0] = (byte)((v >>> 24) & 0xFF);
	}
}
