package org.gorden.bloomfilter.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.hash.Hashing;
import com.google.common.math.DoubleMath;
import java.io.Serializable;
import java.math.RoundingMode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Bloom filter that support local memory or redis
 *
 * @author GordenTam
 */
public final class DefaultBloomFilter<T> implements BloomFilter<T>, Serializable {

    private final BitSet bits;

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
    }

    /**
     * Returns {@code true} if the element <i>might</i> have been put in this Bloom filter, {@code
     * false} if this is <i>definitely</i> not the case.
     */
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


    /**
     * Puts an element into this {@code BloomFilter}. Ensures that subsequent invocations of {@link
     * #mightContain(T Object)} with the same element will always return {@code true}.
     *
     * @return true if the Bloom filter's bits changed as a result of this operation. If the bits
     *     changed, this is <i>definitely</i> the first time {@code object} has been added to the
     *     filter. If the bits haven't changed, this <i>might</i> be the first time {@code object} has
     *     been added to the filter. Note that {@code put(t)} always returns the <i>opposite</i>
     *     result to what {@code mightContain(t)} would have returned at the time it is called.
     * @since 12.0 (present in 11.0 with {@code void} return type})
     */
    public boolean put(T object) {
        return strategy.put(object, numHashFunctions, bits);
    }

    /**
     * Returns the probability that {@linkplain #mightContain(T Object)} will erroneously return {@code
     * true} for an object that has not actually been put in the {@code BloomFilter}.
     *
     * <p>Ideally, this number should be close to the {@code fpp} parameter passed in {@linkplain
     * #create(int, double)}, or smaller. If it is significantly higher, it is usually the
     * case that too many elements (more than expected) have been put in the {@code BloomFilter},
     * degenerating it.
     *
     * @since 14.0 (since 11.0 as expectedFalsePositiveProbability())
     */
    public double expectedFpp() {
        // You down with FPP? (Yeah you know me!) Who's down with FPP? (Every last homie!)
        return Math.pow((double) bits.bitCount() / bitSize(), numHashFunctions);
    }

    /**
     * Returns an estimate for the total number of distinct elements that have been added to this
     * Bloom filter. This approximation is reasonably accurate if it does not exceed the value of
     * {@code expectedInsertions} that was used when constructing the filter.
     *
     * @since 22.0
     */
    public long approximateElementCount() {
        long bitSize = bits.bitSize();
        long bitCount = bits.bitCount();

        /**
         * Each insertion is expected to reduce the # of clear bits by a factor of
         * `numHashFunctions/bitSize`. So, after n insertions, expected bitCount is `bitSize * (1 - (1 -
         * numHashFunctions/bitSize)^n)`. Solving that for n, and approximating `ln x` as `x - 1` when x
         * is close to 1 (why?), gives the following formula.
         */
        double fractionOfBitsSet = (double) bitCount / bitSize;
        return DoubleMath.roundToLong(
                -Math.log1p(-fractionOfBitsSet) * bitSize / numHashFunctions, RoundingMode.HALF_UP);
    }

    /** Returns the number of bits in the underlying bit array. */
    long bitSize() {
        return bits.bitSize();
    }


    /**
     * @param expectedInsertions the number of expected insertions to the constructed {@code
     *     BloomFilter}; must be positive
     * @param fpp the desired false positive probability (must be positive and less than 1.0)
     * @return a {@code BloomFilter}
     */
    public static <T extends Serializable> DefaultBloomFilter<T> create(int expectedInsertions, double fpp) {
        return create((long) expectedInsertions, fpp);
    }

    /**
     *
     * @param expectedInsertions the number of expected insertions to the constructed {@code
     *     BloomFilter}; must be positive
     * @param fpp the desired false positive probability (must be positive and less than 1.0)
     * @return a {@code BloomFilter}
     * @since 19.0
     */
    public static <T extends Serializable> DefaultBloomFilter<T> create(long expectedInsertions, double fpp) {
        return create(expectedInsertions, fpp, BloomFilterStrategies.MURMUR128_MITZ_64);
    }

    @VisibleForTesting
    static <T extends Serializable> DefaultBloomFilter<T> create(long expectedInsertions, double fpp, Strategy strategy) {
        checkArgument(expectedInsertions >= 0, "Expected insertions (%s) must be >= 0", expectedInsertions);
        checkArgument(fpp > 0.0, "False positive probability (%s) must be > 0.0", fpp);
        checkArgument(fpp < 1.0, "False positive probability (%s) must be < 1.0", fpp);
        checkNotNull(strategy);

        if (expectedInsertions == 0) {
            expectedInsertions = 1;
        }

        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
        try {
            return new DefaultBloomFilter<T>(new LockFreeBitSet(numBits), numHashFunctions, strategy);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create BloomFilter of " + numBits + " bits", e);
        }
    }

    /**
     * @param expectedInsertions the number of expected insertions to the constructed {@code
     *     BloomFilter}; must be positive
     * @return a {@code BloomFilter}
     */
    public static <T extends Serializable> DefaultBloomFilter<T> create(int expectedInsertions) {
        return create((long) expectedInsertions);
    }

    /**
     * @param expectedInsertions the number of expected insertions to the constructed {@code
     *     BloomFilter}; must be positive
     * @return a {@code BloomFilter}
     * @since 19.0
     */
    public static <T extends Serializable> DefaultBloomFilter<T> create(long expectedInsertions) {
        return create(expectedInsertions, 0.03); // FYI, for 3%, we always get 5 hash functions
    }

    // Cheat sheet:
    //
    // m: total bits
    // n: expected insertions
    // b: m/n, bits per insertion
    // p: expected false positive probability
    //
    // 1) Optimal k = b * ln2
    // 2) p = (1 - e ^ (-kn/m))^k
    // 3) For optimal k: p = 2 ^ (-k) ~= 0.6185^b
    // 4) For optimal k: m = -nlnp / ((ln2) ^ 2)

    /**
     * Computes the optimal k (number of hashes per element inserted in Bloom filter), given the
     * expected insertions and total number of bits in the Bloom filter.
     *
     * <p>See http://en.wikipedia.org/wiki/File:Bloom_filter_fp_probability.svg for the formula.
     *
     * @param n expected insertions (must be positive)
     * @param m total number of bits in Bloom filter (must be positive)
     */
    @VisibleForTesting
    static int optimalNumOfHashFunctions(long n, long m) {
        // (m / n) * log(2), but avoid truncation due to division!
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    @VisibleForTesting
    static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }
}
