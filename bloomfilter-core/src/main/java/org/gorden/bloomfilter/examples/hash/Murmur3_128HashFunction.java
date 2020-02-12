package org.gorden.bloomfilter.examples.hash;

/**
 * @author GordenTam
 * murmur3 hash 128 bit implement refer to guava
 **/

public class Murmur3_128HashFunction implements HashFunction {

    private int seed;

    public Murmur3_128HashFunction(int seed) {
        this.seed = seed;
    }

    public Hasher newHasher() {
        return new Murmur3_128Hasher(seed);
    }

    public byte[] hashBytes(byte[] var1) {
        return newHasher().hashBytes(var1);
    }

    public byte[] hashBytes(byte[] var1, int var2, int var3) {
        return newHasher().hashBytes(var1, var2, var3);
    }

    private static class Murmur3_128Hasher implements Hasher {
        private static final long C1 = -8663945395140668459L;
        private static final long C2 = 5545529020109919103L;
        private final int seed;
        private long h1;
        private long h2;
        private int length;

        public Murmur3_128Hasher(int seed) {
            this.seed = seed;
            this.h1 = (long) seed;
            this.h2 = (long) seed;
            this.length = 0;
        }

        public byte[] hashBytes(byte[] bytes) {
            return hashBytes(bytes, 0, bytes.length);
        }

        public byte[] hashBytes(byte[] bytes, int offset, int length) {
            if (length <= 0 || length > bytes.length) {
                throw new IllegalArgumentException("Negative length: " + length);
            }
            if (offset < 0 || offset + length > bytes.length) {
                throw new IndexOutOfBoundsException();
            }
            this.length = length;
            int remainByteLength = length;
            int position = offset;

            //processing
            while (remainByteLength >= 16) {
                long k1 = Longs.fromByteArray(bytes, position, true);
                long k2 = Longs.fromByteArray(bytes, position + 8, true);
                bmix64(k1, k2);
                position += 16;
                remainByteLength -= 16;
            }

            if (remainByteLength != 0) {
                processRemaining(bytes, position, remainByteLength);
            }

            return this.makeHash();
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

        private void processRemaining(byte[] bytes, int position, int length) {
            long k1 = 0L;
            long k2 = 0L;
            switch (length) {
                case 7:
                    k1 ^= (long) (bytes[position + 6] & 255) << 48;
                case 6:
                    k1 ^= (long) (bytes[position + 5] & 255) << 40;
                case 5:
                    k1 ^= (long) (bytes[position + 4] & 255) << 32;
                case 4:
                    k1 ^= (long) (bytes[position + 3] & 255) << 24;
                case 3:
                    k1 ^= (long) (bytes[position + 2] & 255) << 16;
                case 2:
                    k1 ^= (long) (bytes[position + 1] & 255) << 8;
                case 1:
                    k1 ^= (long) (bytes[position] & 255);
                    break;
                case 15:
                    k2 ^= (long) (bytes[position + 14] & 255) << 48;
                case 14:
                    k2 ^= (long) (bytes[position + 13] & 255) << 40;
                case 13:
                    k2 ^= (long) (bytes[position + 12] & 255) << 32;
                case 12:
                    k2 ^= (long) (bytes[position + 11] & 255) << 24;
                case 11:
                    k2 ^= (long) (bytes[position + 10] & 255) << 16;
                case 10:
                    k2 ^= (long) (bytes[position + 9] & 255) << 8;
                case 9:
                    k2 ^= (long) (bytes[position + 8] & 255);
                case 8:
                    k1 ^= Longs.fromByteArray(bytes, position, true);
                    ;
                    break;
                default:
                    throw new AssertionError("Should never get here.");
            }

            this.h1 ^= mixK1(k1);
            this.h2 ^= mixK2(k2);
        }

        private byte[] makeHash() {
            this.h1 ^= (long) this.length;
            this.h2 ^= (long) this.length;
            this.h1 += this.h2;
            this.h2 += this.h1;
            this.h1 = fmix64(this.h1);
            this.h2 = fmix64(this.h2);
            this.h1 += this.h2;
            this.h2 += this.h1;
            byte[] byteArrayOfH1 = Longs.toByteArray(h1);
            byte[] byteArrayOfH2 = Longs.toByteArray(h2);
            byte[] combine = new byte[byteArrayOfH1.length + byteArrayOfH2.length];
            System.arraycopy(byteArrayOfH1, 0, combine, 0, byteArrayOfH1.length);
            System.arraycopy(byteArrayOfH2, 0, combine, byteArrayOfH1.length, byteArrayOfH2.length);
            return combine;
        }

        private static long fmix64(long k) {
            k ^= k >>> 33;
            k *= -49064778989728563L;
            k ^= k >>> 33;
            k *= -4265267296055464877L;
            k ^= k >>> 33;
            return k;
        }

        private static long mixK1(long k1) {
            k1 *= C1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= C2;
            return k1;
        }

        private static long mixK2(long k2) {
            k2 *= C2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= C1;
            return k2;
        }
    }

}
