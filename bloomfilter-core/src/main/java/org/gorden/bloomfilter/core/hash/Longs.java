package org.gorden.bloomfilter.core.hash;

import java.math.BigDecimal;

/**
 * @author GordenTam
 **/

public class Longs {

    public static long fromByteArray(byte[] bytes, int offset, boolean littleEndian) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must larger be positive");
        }
        long value = 0;
        for (int count = 0; count < 8; ++count) {
            int shift = (littleEndian ? count : (7 - count)) << 3;
            value |= ((long) 0xff << shift) & ((long) bytes[offset + count] << shift);
        }
        return value;
    }

    public static byte[] toByteArray(long x) {
        byte[] bytes = new byte[8];
        int index = 0;
        bytes[index + 7] = (byte) (x >> 56);
        bytes[index + 6] = (byte) (x >> 48);
        bytes[index + 5] = (byte) (x >> 40);
        bytes[index + 4] = (byte) (x >> 32);
        bytes[index + 3] = (byte) (x >> 24);
        bytes[index + 2] = (byte) (x >> 16);
        bytes[index + 1] = (byte) (x >> 8);
        bytes[index] = (byte) (x);
        return bytes;
    }

    public static long fromBytes(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        return ((long) b1 & 255L) << 56 | ((long) b2 & 255L) << 48 | ((long) b3 & 255L) << 40 | ((long) b4 & 255L) << 32 | ((long) b5 & 255L) << 24 | ((long) b6 & 255L) << 16 | ((long) b7 & 255L) << 8 | (long) b8 & 255L;
    }

    public static long doubleToLong(double x) {
        BigDecimal bigDecimal = BigDecimal.valueOf(x);
        return bigDecimal.longValue();
    }

    public static long divideCeiling(long p, long q) {
        long div = p / q;
        long rem = p - q * div;
        if (rem == 0L) {
            return div;
        } else {
            int signum = 1 | (int) ((p ^ q) >> 63);
            boolean increment = signum > 0;
            return increment ? div + (long) signum : div;
        }
    }
}
