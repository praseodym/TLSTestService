package tlstestservice;

import java.util.Arrays;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class Utils {
    public static byte[] concat(byte[] first, byte[] second) {
        if (first == null) return second;
        if (second == null) return first;

        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] xor(byte[] first, byte[] second) throws Exception {
        if (first.length != second.length) throw new Exception("Arguments have different lengths");

        byte[] output = new byte[first.length];

        for (int i = 0; i < first.length; i++) {
            output[i] = (byte) (first[i] ^ second[i]);
        }

        return output;
    }

    public static int getuint16(byte byte1, byte byte2) {
        return ((byte1 & 0xFF) << 8) | (byte2 & 0xFF);
    }

    public static int getuint24(byte byte1, byte byte2, byte byte3) {
        return ((byte1 & 0xFF) << 16) | ((byte2 & 0xFF) << 8) | (byte3 & 0xFF);
    }

    public static byte[] getbytes64(long val) {
        byte[] out = new byte[8];
        out[0] = (byte) (0xFF & (val >>> 56));
        out[1] = (byte) (0xFF & (val >>> 48));
        out[2] = (byte) (0xFF & (val >>> 40));
        out[3] = (byte) (0xFF & (val >>> 32));
        out[4] = (byte) (0xFF & (val >>> 24));
        out[5] = (byte) (0xFF & (val >>> 16));
        out[6] = (byte) (0xFF & (val >>> 8));
        out[7] = (byte) (0xFF & val);
        return out;
    }

    public static byte[] getbytes24(int val) {
        byte[] out = new byte[3];
        out[0] = (byte) (0xFF & (val >>> 16));
        out[1] = (byte) (0xFF & (val >>> 8));
        out[2] = (byte) (0xFF & val);
        return out;
    }

    public static byte[] getbytes16(int val) {
        byte[] out = new byte[2];
        out[0] = (byte) (0xFF & (val >>> 8));
        out[1] = (byte) (0xFF & val);
        return out;
    }

    public static byte[] getbytes8(int val) {
        byte[] out = new byte[1];
        out[0] = (byte) (0xFF & val);
        return out;
    }

    // From http://stackoverflow.com/a/9855338
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
