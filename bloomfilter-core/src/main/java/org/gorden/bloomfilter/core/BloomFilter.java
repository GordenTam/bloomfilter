package org.gorden.bloomfilter.core;

import com.google.common.math.DoubleMath;
import org.gorden.bloomfilter.core.bitset.BitSet;
import org.gorden.bloomfilter.core.bitset.LockFreeBitSet;
import org.gorden.bloomfilter.core.hash.Hasher;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import org.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;

import java.math.RoundingMode;

/**
 * A Bloom filter implements refer to guava.Support optional bitSet,hashFunction and serializer implements.
 *
 * @author GordenTam
 */
public class BloomFilter<T> implements Membership<T> {

    private int numHashFunctions;

    private BloomFilterSerializer bloomFilterSerializer;

    private Hasher hasher;

    private BitSet bitSet;

    public BloomFilter(long expectedInsertions, double fpp) {
        this(expectedInsertions, fpp, new JdkSerializationBloomFilterSerializer());
    }

    public BloomFilter(long expectedInsertions, double fpp, Hasher hasher) {
        this(expectedInsertions, fpp, new JdkSerializationBloomFilterSerializer(), bloomFilterSerializer);
    }

    public BloomFilter(long expectedInsertions, double fpp, BloomFilterSerializer bloomFilterSerializer) {
        this(expectedInsertions, fpp, new JdkSerializationBloomFilterSerializer(), bloomFilterSerializer);
    }

    public BloomFilter(long expectedInsertions, double fpp, BloomFilterSerializer bloomFilterSerializer, Hasher hasher) {
        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        this(expectedInsertions, fpp, new LockFreeBitSet(numBits), bloomFilterSerializer, hasher);
    }

    public BloomFilter(long expectedInsertions, double fpp, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, Hasher hasher) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException(String.format("expectedInsertions (%s) must be > 0", expectedInsertions));
        }
        if (fpp >= 1.0) {
            throw new IllegalArgumentException(String.format("numHashFunctions (%s) must be < 1.0", fpp));
        }
        if (fpp <= 0.0) {
            throw new IllegalArgumentException(String.format("numHashFunctions (%s) must be > 0.0", fpp));
        }
        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
        this(numHashFunctions, new LockFreeBitSet(numBits), bloomFilterSerializer, hasher);
    }

    private BloomFilter(int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, Hasher hasher) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException(String.format("expectedInsertions (%s) must be > 0", expectedInsertions));
        }
        if (fpp >= 1.0) {
            throw new IllegalArgumentException(String.format("numHashFunctions (%s) must be < 1.0", fpp));
        }
        if (fpp <= 0.0) {
            throw new IllegalArgumentException(String.format("numHashFunctions (%s) must be > 0.0", fpp));
        }
        if (bitSet == null) {
            throw new IllegalArgumentException("bitSet can not be null");
        }
        if (bloomFilterSerializer == null) {
            throw new IllegalArgumentException("bloomFilterSerializer can not be null");
        }
        if (hasher == null) {
            throw new IllegalArgumentException("hasher can not be null");
        }
        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        this.numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
        this.bitSet = bitSet;
        this.bloomFilterSerializer = bloomFilterSerializer;
        this.hasher = hasher;
    }

    public boolean mightContain(T object) {
        long bitSize = bitSet.bitSize();
        byte[] bytes = hasher.hashBytes(raw(object));
        long hash1 = lowerEight(bytes);
        long hash2 = upperEight(bytes);

        long combinedHash = hash1;
        for (int i = 0; i < numHashFunctions; i++) {
            if (!bitSet.get((combinedHash & Long.MAX_VALUE) % bitSize)) {
                return false;
            }
            combinedHash += hash2;
        }
        return true;
    }

    public boolean put(T object) {
        long bitSize = bitSet.bitSize();
        //使用murmurHash求得128位byte数组
        byte[] bytes = hasher.hashBytes(raw(object));
        //低64位
        long hash1 = lowerEight(bytes);
        //高64位
        long hash2 = upperEight(bytes);

        boolean bitsChanged = false;
        long combinedHash = hash1;

        //gi(x) = h1(x) + ih2(x) ，其中0<=i<=k-1； 使用两个函数模拟k个函数,这里h2(x)就是上面求得的hash2
        //也就是通过循环,每次累加hash2，然后& Long.MAX_VALUE将其转换为正数,对bitSize取模就得到索引位，将其set为1
        for (int i = 0; i < numHashFunctions; i++) {
            // Make the combined hash positive and indexable
            bitsChanged |= bitSet.set((combinedHash & Long.MAX_VALUE) % bitSize);
            combinedHash += hash2;
        }
        //返回修改了多少位
        return bitsChanged;
    }

    public double expectedFpp() {
        return Math.pow((double) bitSet.bitCount() / bitSize(), numHashFunctions);
    }

    public long approximateElementCount() {
        long bitSize = bitSet.bitSize();
        long bitCount = bitSet.bitCount();

        double fractionOfBitsSet = (double) bitCount / bitSize;
        return DoubleMath.roundToLong(-Math.log1p(-fractionOfBitsSet) * bitSize / numHashFunctions, RoundingMode.HALF_UP);
    }

    long bitSize() {
        return bitSet.bitSize();
    }

    static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    private byte[] raw(T element){
        return bloomFilterSerializer.serialize(element);
    }

    private long lowerEight(byte[] bytes) {
        return Longs.fromBytes(
                bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    }

    private long upperEight(byte[] bytes) {
        return Longs.fromBytes(
                bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
    }
}
