package uk.co.bitethebullet.android.token.util;

public class Hex {
	private static final String HEX_ENCODING_TABLE = "0123456789abcdef";

    private static void setHex(char[] hex, int i, byte b) {
        hex[2*i] = HEX_ENCODING_TABLE.charAt((b & 0xf0) >> 4);
        hex[2*i+1] = HEX_ENCODING_TABLE.charAt(b & 0x0f);
    }

    private static byte getHex(String hex, int i) {
        return (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
    }
    
    public static String byteArrayToHex(byte[] bts) {
        if (bts == null)
            return null;

        char[] hex = new char[bts.length * 2];

        for (int i = 0; i < bts.length; i++)
            setHex(hex, i, bts[i]);

        return new String(hex);
    }

    public static byte[] hexToByteArray(String hex) {
        if (hex == null)
            return null;

		byte[] bts = new byte[hex.length() / 2];
		
		for (int i = 0; i < bts.length; i++)
			bts[i] = getHex(hex, i);
		
		return bts;
    }

    public static String xorHexWith(String hex, byte[] bts) {
        if (bts == null || bts.length == 0)
            return hex;

        int bl = bts.length;
        char[] out = new char[hex.length()];
        for (int i = 0; i < out.length/2; i++)
            setHex(out, i, (byte)(getHex(hex, i) ^ bts[i%bl]));

        return new String(out);
    }
}
