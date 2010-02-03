package org.xiph.ogg;

public class CRCUtils {
	protected static final Integer CRC_POLYNOMIAL = Integer.valueOf(0x04c11db7);
	private static int[] CRC_TABLE = new int[256];
	
	static {
		int crc;
		for(int i=0; i<256; i++) {
			crc = i << 24;
			for(int j=0; j<8; j++) {
	            if( (crc & 0x80000000) != 0 ) {
	            	crc = ((crc << 1) ^ CRC_POLYNOMIAL);
	            } else {
	               crc <<= 1;
	            }
			}
			CRC_TABLE[i] = crc;
		}
	}

	public static int getCRC(byte[] data) {
		return getCRC(data, 0);
	}
	public static int getCRC(byte[] data, int previous) {
		int crc = previous;
		int a,b;
		
		for(int i=0; i<data.length; i++) {
			a = crc << 8;
			b = CRC_TABLE[ ((crc>>>24) & 0xff) ^ (data[i] & 0xff) ];
			crc = a ^ b;
		}
		
		return crc;
	}
}
