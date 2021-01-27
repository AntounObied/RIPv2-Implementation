/**
 * @author Antoun Obied
 *
 * This class contains the operations used to convert certain data types for compatibility
 */

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Operations {

    public static byte stringToByte(String s){
        int temp = Integer.parseInt(s);
        return (byte) temp;
    }

    public static byte intToByte(int i){
        return Byte.parseByte(String.valueOf(i));
    }

    public static int byteToInt(byte b){
        return (b & 0xFF);
    }

    public static String getSenderAddress(int ID){
        return "10.0."+ ID + ".0";
    }

    public static String byteToHex(byte b){
        return String.format("%02X", b);
    }

    public static String hexToBin(String hex){
        String value = new BigInteger(hex, 16).toString(2);
        return String.format("%8s", value).replace(" ", "0");
    }

    public static int hexToDecimal(String hex){
        return Integer.parseInt(hex, 16);
    }

    public static String byteToString(byte b){
        return String.valueOf(hexToDecimal(byteToHex(b)));
    }

    /**
     * This method converts an int value to a byte array of length 4
     * Reference: https://stackoverflow.com/questions/6374915/java-convert-int-to-byte-array-of-4-bytes
     * @param value Integer value
     * @return Byte array equivalent
     */
    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value };
    }

    /**
     * This method converts a byte array to an int value
     * @param bytes Byte array
     * @return Integer equivalent
     */
    public static int byteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }


}
