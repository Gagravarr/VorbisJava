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
package org.xiph.ogg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class IOUtils {
	public static void readFully(InputStream inp, byte[] destination) throws IOException {
		readFully(inp, destination, 0, destination.length);
	}
	public static void readFully(InputStream inp, byte[] destination, int offset, int length) throws IOException {
		int read = 0;
		int r;
		while(read < length) {
			r = inp.read(destination, offset+read, length-read);
			if(r == -1) {
				throw new IOException("Asked to read " + length + " bytes from " + offset + " but hit EoF at " + read);
			}
			read += r;
		}
	}

	
	public static int toInt(byte b) {
		if(b < 0)
			return b & 0xff;
		return b;
	}
	public static byte fromInt(int i) {
		if(i > 256) {
			throw new IllegalArgumentException("Number " + i + " too big");
		}
		if(i > 127) {
			return (byte)(i-256);
		}
		return (byte)i;
	}

	
	public static long getInt3(byte[] data) {
		return getInt3(data, 0);
	}
	public static long getInt3(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        return getInt(b0, b1, b2);
	}
	
	public static long getInt4(byte[] data) {
		return getInt4(data, 0);
	}
	public static long getInt4(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        return getInt(b0, b1, b2, b3);
	}
	
	public static long getInt8(byte[] data) {
		return getInt8(data, 0);
	}
	public static long getInt8(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        int b4 = data[i++] & 0xFF;
        int b5 = data[i++] & 0xFF;
        int b6 = data[i++] & 0xFF;
        int b7 = data[i++] & 0xFF;
        return getInt(b0, b1, b2, b3, b4, b5, b6, b7);
	}
	
	public static long getInt(int i0, int i1, int i2) {
        return (i2 << 16) + (i1 << 8) + (i0 << 0);
	}
	public static long getInt(int i0, int i1, int i2, int i3) {
        return (i3 << 24) + (i2 << 16) + (i1 << 8) + (i0 << 0);
	}
	public static long getInt(int i0, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        return (long)(i7 << 56) + (long)(i6 << 48) + 
               (long)(i5 << 40) + (long)(i4 << 32) +
               (i3 << 24) + (i2 << 16) + (i1 << 8) + (i0 << 0);
	}
	
	
	public static void writeInt3(OutputStream out, long v) throws IOException {
		byte[] b3 = new byte[3];
		putInt3(b3, 0, v);
		out.write(b3);
	}
	public static void putInt3(byte[] data, int offset, long v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
        data[i++] = (byte)((v >>> 16) & 0xFF);
	}
	
	public static void writeInt4(OutputStream out, long v) throws IOException {
		byte[] b4 = new byte[4];
		putInt4(b4, 0, v);
		out.write(b4);
	}
	public static void putInt4(byte[] data, int offset, long v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
        data[i++] = (byte)((v >>> 16) & 0xFF);
        data[i++] = (byte)((v >>> 24) & 0xFF);
	}
	
	public static void writeInt8(OutputStream out, long v) throws IOException {
		byte[] b8 = new byte[8];
		putInt8(b8, 0, v);
		out.write(b8);
	}
	public static void putInt8(byte[] data, int offset, long v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
        data[i++] = (byte)((v >>> 16) & 0xFF);
        data[i++] = (byte)((v >>> 24) & 0xFF);
        data[i++] = (byte)((v >>> 32) & 0xFF);
        data[i++] = (byte)((v >>> 40) & 0xFF);
        data[i++] = (byte)((v >>> 48) & 0xFF);
        data[i++] = (byte)((v >>> 56) & 0xFF);
	}
	
	/**
	 * @param length The length in BYTES
	 */
	public static String getUTF8(byte[] data, int offset, int length) {
		try {
			return new String(data, offset, length, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("Broken JVM, UTF-8 not found", e);
		}
	}
	/**
	 * @return The length in BYTES
	 */
	public static int putUTF8(byte[] data, int offset, String str) {
		try {
			byte[] s = str.getBytes("UTF-8");
			System.arraycopy(s, 0, data, offset, s.length);
			return s.length;
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("Broken JVM, UTF-8 not found", e);
		}
	}
	/**
	 * Writes the string out as UTF-8
	 */
	public static void writeUTF8(OutputStream out, String str) throws IOException {
		try {
			byte[] s = str.getBytes("UTF-8");
			out.write(s);
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("Broken JVM, UTF-8 not found", e);
		}
	}
	/**
	 * Writes out a 4 byte integer of the length (in bytes!) of the
	 *  String, followed by the String (as UTF-8)
	 */
	public static void writeUTF8WithLength(OutputStream out, String str) throws IOException {
		try {
			byte[] s = str.getBytes("UTF-8");
			writeInt4(out, s.length);
			out.write(s);
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("Broken JVM, UTF-8 not found", e);
		}
	}
}
