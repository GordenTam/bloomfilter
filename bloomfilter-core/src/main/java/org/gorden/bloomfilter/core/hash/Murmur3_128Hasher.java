package org.gorden.bloomfilter.core.hash;

import com.google.common.hash.HashCode;
import com.google.common.primitives.UnsignedBytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author GordenTam
 * copy from guava
 **/

public class Murmur3_128Hasher implements Hasher {

    private static final long C1 = -8663945395140668459L;
    private static final long C2 = 5545529020109919103L;
    private final int seed;
    private long h1;
    private long h2;
    private int length;

    public Murmur3_128Hasher(int seed) {
        this.seed = seed;
        this.h1 = (long)seed;
        this.h2 = (long)seed;
        this.length = 0;
    }

    public byte[] hashBytes(byte[] bytes) {
        this.length = bytes.length;
        int len = bytes.length;
        int position = 0;
        while (len - position >= 16) {
            long k1 = convertLongFromByteArray(bytes, position, true);
            long k2 = convertLongFromByteArray(bytes, position+8, true);
            bmix64(k1, k2);
            position+=16;
        }

        return this.makeHash();
    }

    public byte[] hashBytes(byte[] var1, int var2, int var3) {

    }

    protected void process(long k1, long k2) {
        this.bmix64(k1, k2);
    }

    private void bmix64(long k1, long k2) {
        this.h1 ^= mixK1(k1);
        this.h1 = Long.rotateLeft(this.h1, 27);
        this.h1 += this.h2;
        this.h1 = this.h1 * 5L + 1390208809L;
        this.h2 ^= mixK2(k2);
        this.h2 = Long.rotateLeft(this.h2, 31);
        this.h2 += this.h1;
        this.h2 = this.h2 * 5L + 944331445L;
    }

    protected void processRemaining(byte [] bytes, int position) {
        long k1 = 0L;
        long k2 = 0L;
        int remain = bytes.length - position;
        switch(remain) {
            case 7:
                k1 ^= (long) UnsignedBytes.toInt(bytes[position+6]) << 48;
            case 6:
                k1 ^= (long)UnsignedBytes.toInt(bytes[position+5]) << 40;
            case 5:
                k1 ^= (long)UnsignedBytes.toInt(bytes[position+4]) << 32;
            case 4:
                k1 ^= (long)UnsignedBytes.toInt(bytes[position+3]) << 24;
            case 3:
                k1 ^= (long)UnsignedBytes.toInt(bytes[position+2]) << 16;
            case 2:
                k1 ^= (long)UnsignedBytes.toInt(bytes[position+1]) << 8;
            case 1:
                k1 ^= (long)UnsignedBytes.toInt(bytes[position]);
                break;
            case 15:
                k2 ^= (long)UnsignedBytes.toInt(bytes[position+14]) << 48;
            case 14:
                k2 ^= (long)UnsignedBytes.toInt(bytes[position+13]) << 40;
            case 13:
                k2 ^= (long)UnsignedBytes.toInt(bytes[position+12]) << 32;
            case 12:
                k2 ^= (long)UnsignedBytes.toInt(bytes[position+11]) << 24;
            case 11:
                k2 ^= (long)UnsignedBytes.toInt(bytes[position+10]) << 16;
            case 10:
                k2 ^= (long)UnsignedBytes.toInt(bytes[position+9]) << 8;
            case 9:
                k2 ^= (long)UnsignedBytes.toInt(bytes[position+8]);
            case 8:
                k1 ^= bytes[position+14];
                break;
            default:
                throw new AssertionError("Should never get here.");
        }

        this.h1 ^= mixK1(k1);
        this.h2 ^= mixK2(k2);
    }

    public byte[] makeHash() {
        this.h1 ^= (long)this.length;
        this.h2 ^= (long)this.length;
        this.h1 += this.h2;
        this.h2 += this.h1;
        this.h1 = fmix64(this.h1);
        this.h2 = fmix64(this.h2);
        this.h1 += this.h2;
        this.h2 += this.h1;
        byte[] byteArrayOfH1 = convertLongToByteArray(h1);
        byte[] byteArrayOfH2 = convertLongToByteArray(h2);
        byte[] combine = new byte[16];

    }

    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= -49064778989728563L;
        k ^= k >>> 33;
        k *= -4265267296055464877L;
        k ^= k >>> 33;
        return k;
    }

    private static long convertLongFromByteArray(byte[] bytes, int offset, boolean littleEndian){
        if(offset < 0){
            throw new IllegalArgumentException("offset must larger be positive");
        }
        long value = 0;
        for(int count = 0; count < 8; ++count){
            int shift= (littleEndian ? count : (7 - count)) << 3;
            value |= ((long)0xff << shift) & ((long)bytes[offset + count] << shift);
        }
        return value;
    }

    private static byte[] convertLongToByteArray(long x){
        byte[] bytes = new byte[18];
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

    private static long mixK1(long k1) {
        k1 *= -8663945395140668459L;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= 5545529020109919103L;
        return k1;
    }

    private static long mixK2(long k2) {
        k2 *= 5545529020109919103L;
        k2 = Long.rotateLeft(k2, 33);
        k2 *= -8663945395140668459L;
        return k2;
    }
}
