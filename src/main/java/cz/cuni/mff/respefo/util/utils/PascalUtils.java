package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class PascalUtils extends UtilityClass {

    /**
     * Extracts a short from its byte array representation.
     * @param bytes byte array containing the short number in little endian order
     * @return extracted short value
     */
    public static short bytesToShort(byte[] bytes) {
        Objects.requireNonNull(bytes);
        return ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN).getShort();
    }

    /**
     * Extracts an integer from its byte array representation.
     * @param bytes byte array containing the integer number in little endian order
     * @return extracted integer value
     */
    public static int bytesToInt(byte[] bytes) {
        Objects.requireNonNull(bytes);
        return ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN).getInt();
    }

    /**
     * Converts a Turbo Pascal Extended type to double.
     * @param data byte array containing the extended value in little endian byte order
     * @return converted double value
     */
    public static double pascalExtendedToDouble(byte[] data) {
        Objects.requireNonNull(data);
        if (data.length != 10) {
            throw new IllegalArgumentException("Extended format takes up 10 bytes");
        }

        byte sign = (byte) ((data[9] & 0x80) >> 7); // 0 positive, 1 negative

        byte[] bytes = Arrays.copyOfRange(data, 8, 10);
        bytes[1] &= 0x7F; // except the sign bit
        int exponent = bytesToShort(bytes) - 16382; // subtract to get the actual value

        double result;
        if (exponent >= -1024) {
            double mantissa = 0;

            for (int i = 0; i < 8; i++) {
                mantissa += data[i] & 0xFF;
                mantissa /= 256;
            }

            result = Math.pow(-1, sign) * mantissa * Math.pow(2, exponent); // compute the value
        } else {
            result = 0;
        }

        return result;
    }

    /**
     * Converts a Turbo Pascal Real type to double.
     * @param data byte array containing the real value in little endian byte order
     * @return converted double value
     */
    public static double pascalRealToDouble(byte[] data) {
        Objects.requireNonNull(data);
        if (data.length != 6) {
            throw new IllegalArgumentException("Real format takes up 6 bytes");
        }

        byte sign = (byte) ((data[5] & 0x80) >> 7); // 0 positive, 1 negative

        int exponent = (byte) (data[0] & 0xFF);
        exponent -= 129;

        double mantissa = 0;
        for (int i = 1; i < 5; i++) {
            mantissa += data[i] & 0xFF;
            mantissa /= 256;
        }

        mantissa += data[5] & 0x7F;
        mantissa /= 128;
        mantissa += 1;

        if ((byte) (data[0] & 0xFF) == 0 && mantissa == 1) {
            return 0;
        }

        return Math.pow(-1, sign) * mantissa * Math.pow(2, exponent);
    }

    protected PascalUtils() throws IllegalAccessException {
        super();
    }
}
