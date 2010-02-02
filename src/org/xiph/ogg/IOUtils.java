package org.xiph.ogg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
	public static void readFully(InputStream inp, byte[] destination) throws IOException {
		readFully(inp, destination, 0, destination.length);
	}
	public static void readFully(InputStream inp, byte[] destination, int offset, int length) throws IOException {
		// TODO
	}

	
	public static int toInt(byte b) {
		if(b < 0)
			return b+256;
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

	
	public static long getInt4(byte[] data) {
		return getInt4(data, 0);
	}
	public static long getInt4(byte[] data, int offset) {
		
	}
	
	public static long getInt8(byte[] data) {
		return getInt8(data, 0);
	}
	public static long getInt8(byte[] data, int offset) {
		
	}
	
	public static long getInt(int i1, int i2, int i3, int i4) {
		// TODO
	}
	public static long getInt(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
		// TODO
	}
	
	
	public static void writeInt4(OutputStream out, long v) throws IOException {
		// TODO
	}
	
	public static void writeInt8(OutputStream out, long v) throws IOException {
		// TODO
	}
}
