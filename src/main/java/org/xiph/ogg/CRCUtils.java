package org.xiph.ogg;

public class CRCUtils {
	protected static final Long CRC_POLYNOMIAL = Long.valueOf(0x04c11db7);
	private static int[] CRC_TABLE = new int[256];
	
	static {
		int crc;
		for(int i=0; i<256; i++) {
			crc = i;
			for (int j = 8; j > 0; j--) {
	            if ((crc & 1) == 1) {
	            	crc = (int)((crc >>> 1) ^ CRC_POLYNOMIAL);
	            } else {
	               crc >>>= 1;
	            }
			}
			CRC_TABLE[i] = crc;
		}
	}

	public static int getCRC(byte[] data) {
		return getCRC(data, 0xffffffff);
	}
	public static int getCRC(byte[] data, int previous) {
		int crc = previous;
		int a,b;
		
		for(int i=0; i<data.length; i++) {
			a = crc >>> 8;
			b = CRC_TABLE[ (crc ^ data[i]) & 0xff ];
			crc = a ^ b;
		}
		return crc;
	}
}
