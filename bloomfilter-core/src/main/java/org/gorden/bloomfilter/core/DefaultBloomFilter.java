package org.gorden.bloomfilter.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.hash.Hashing;
import com.google.common.math.DoubleMath;
import org.gorden.bloomfilter.core.bitset.BitSet;
import org.gorden.bloomfilter.core.bitset.LockFreeBitSet;

import java.io.Serializable;
import java.math.RoundingMode;

/**
 * A Bloom filter that support local memory or redis
 *
 * @author GordenTam
 */
public final class DefaultBloomFilter<T> implements BloomFilter<T>, Serializable {

    private final BitSet bitSet;

    private final int numHashFunctions;

    public DefaultBloomFilter(long expectedInsertions, double fpp) {
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
        this.numHashFunctions = numHashFunctions;
        this.bitSet = new LockFreeBitSet();
    }

    public boolean mightContain(T object) {
        long bitSize = bits.bitSize();
        byte[] bytes = Hashing.murmur3_128().hashBytes(serialize(object)).asBytes();
        long hash1 = lowerEight(bytes);
        long hash2 = upperEight(bytes);

        long combinedHash = hash1;
        for (int i = 0; i < numHashFunctions; i++) {
            // Make the combined hash positive and indexable
            if (!bits.get((combinedHash & Long.MAX_VALUE) % bitSize)) {
                return false;
            }
            combinedHash += hash2;
        }
        return true;
    }

    public boolean put(T object) {
        return .put(object, numHashFunctions, bits);
    }

    public double expectedFpp() {
        return Math.pow((double) bits.bitCount() / bitSize(), numHashFunctions);
    }

    public long approximateElementCount() {
        long bitSize = bits.bitSize();
        long bitCount = bits.bitCount();

        double fractionOfBitsSet = (double) bitCount / bitSize;
        return DoubleMath.roundToLong(-Math.log1p(-fractionOfBitsSet) * bitSize / numHashFunctions, RoundingMode.HALF_UP);
    }

    long bitSize() {
        return bits.bitSize();
    }

    public static <T extends Serializable> DefaultBloomFilter<T> create(int expectedInsertions, double fpp) {
        return create((long) expectedInsertions, fpp);
    }

    public static <T extends Serializable> DefaultBloomFilter<T> create(long expectedInsertions, double fpp) {
        return create(expectedInsertions, fpp, BloomFilterStrategies.MURMUR128_MITZ_64);
    }

    static <T extends Serializable> DefaultBloomFilter<T> create(long expectedInsertions, double fpp) {
        checkArgument(expectedInsertions >= 0, "Expected insertions (%s) must be >= 0", expectedInsertions);
        checkArgument(fpp > 0.0, "False positive probability (%s) must be > 0.0", fpp);
        checkArgument(fpp < 1.0, "False positive probability (%s) must be < 1.0", fpp);

        if (expectedInsertions == 0) {
            expectedInsertions = 1;
        }

        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
        try {
            return new DefaultBloomFilter<T>(new LockFreeBitSet(numBits), numHashFunctions);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create BloomFilter of " + numBits + " bits", e);
        }
    }

    static int optimalNumOfHashFunctions(long n, long m) {
        // (m / n) * log(2), but avoid truncation due to division!
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }
}
