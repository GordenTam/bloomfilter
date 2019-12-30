package org.gorden.bloomfilter.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.math.DoubleMath;
import java.io.Serializable;
import java.math.RoundingMode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Bloom filter that support local memory or redis
 *
 * @param <T> the type of instances that the {@code BloomFilter} accepts
 * @author GordenTam
 */
@Beta
public final class BloomFilter<T extends Serializable> implements Predicate<T>, Serializable {
    /**
     * A strategy to translate T instances, to {@code numHashFunctions} bit indexes.
     *
     * <p>Implementations should be collections of pure functions (i.e. stateless).
     */

    interface Strategy extends java.io.Serializable {

        /**
         * Sets {@code numHashFunctions} bits of the given bit array, by hashing a user element.
         *
         * <p>Returns whether any bits changed as a result of this operation.
         */
        <T> boolean put(T object, int numHashFunctions, BitSet bits);

        /**
         * Queries {@code numHashFunctions} bits of the given bit array, by hashing a user element;
         * returns {@code true} if and only if all selected bits are set.
         */
        <T> boolean mightContain(
                T object, int numHashFunctions, BitSet bits);

        /**
         * Identifier used to encode this strategy, when marshalled as part of a BloomFilter. Only
         * values in the [-128, 127] range are valid for the compact serial form. Non-negative values
         * are reserved for enums defined in BloomFilterStrategies; negative values are reserved for any
         * custom, stateful strategy we may define (e.g. any kind of strategy that would depend on user
         * input).
         */
        int ordinal();
    }

    //布隆过滤器的比特数组
    private final BitSet bits;

    //哈希函数个数
    private final int numHashFunctions;

    /** The strategy we employ to map an element T to {@code numHashFunctions} bit indexes. */
    private final Strategy strategy;

    /** Creates a BloomFilter. */
    private BloomFilter(BitSet bits, int numHashFunctions, Strategy strategy) {
        checkArgument(numHashFunctions > 0, "numHashFunctions (%s) must be > 0", numHashFunctions);
        checkArgument(numHashFunctions <= 255, "numHashFunctions (%s) must be <= 255", numHashFunctions);
        this.bits = checkNotNull(bits);
        this.numHashFunctions = numHashFunctions;
        this.strategy = checkNotNull(strategy);
    }

    /**
     * Returns {@code true} if the element <i>might</i> have been put in this Bloom filter, {@code
     * false} if this is <i>definitely</i> not the case.
     */
    public boolean mightContain(T object) {
        return strategy.mightContain(object, numHashFunctions, bits);
    }

    /**
     * @deprecated Provided only to satisfy the {@link Predicate} interface; use {@link #mightContain}
     *     instead.
     */
    @Deprecated
    @Override
    public boolean apply(T input) {
        return mightContain(input);
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
    @VisibleForTesting
    long bitSize() {
        return bits.bitSize();
    }

    /**
     * Determines whether a given Bloom filter is compatible with this Bloom filter. For two Bloom
     * filters to be compatible, they must:
     *
     * <ul>
     *   <li>not be the same instance
     *   <li>have the same number of hash functions
     *   <li>have the same bit size
     *   <li>have the same strategy
     *   <li>have equal funnels
     * </ul>
     *
     * @param that The Bloom filter to check for compatibility.
     * @since 15.0
     */
    public boolean isCompatible(BloomFilter<T> that) {
        checkNotNull(that);
        return this != that
                && this.numHashFunctions == that.numHashFunctions
                && this.bitSize() == that.bitSize()
                && this.strategy.equals(that.strategy);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof BloomFilter) {
            BloomFilter<?> that = (BloomFilter<?>) object;
            return this.numHashFunctions == that.numHashFunctions
                    && this.bits.equals(that.bits)
                    && this.strategy.equals(that.strategy);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numHashFunctions, strategy, bits);
    }

    /**
     * @param expectedInsertions the number of expected insertions to the constructed {@code
     *     BloomFilter}; must be positive
     * @param fpp the desired false positive probability (must be positive and less than 1.0)
     * @return a {@code BloomFilter}
     */
    public static <T extends Serializable> BloomFilter<T> create(int expectedInsertions, double fpp) {
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
    public static <T extends Serializable> BloomFilter<T> create(long expectedInsertions, double fpp) {
        return create(expectedInsertions, fpp, BloomFilterStrategies.MURMUR128_MITZ_64);
    }

    @VisibleForTesting
    static <T extends Serializable> BloomFilter<T> create(long expectedInsertions, double fpp, Strategy strategy) {
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
            return new BloomFilter<T>(new LockFreeBitSet(numBits), numHashFunctions, strategy);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create BloomFilter of " + numBits + " bits", e);
        }
    }

    /**
     * @param expectedInsertions the number of expected insertions to the constructed {@code
     *     BloomFilter}; must be positive
     * @return a {@code BloomFilter}
     */
    public static <T extends Serializable> BloomFilter<T> create(int expectedInsertions) {
        return create((long) expectedInsertions);
    }

    /**
     * @param expectedInsertions the number of expected insertions to the constructed {@code
     *     BloomFilter}; must be positive
     * @return a {@code BloomFilter}
     * @since 19.0
     */
    public static <T extends Serializable> BloomFilter<T> create(long expectedInsertions) {
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

    /**
     * Computes m (total bits of Bloom filter) which is expected to achieve, for the specified
     * expected insertions, the required false positive probability.
     *
     * <p>See http://en.wikipedia.org/wiki/Bloom_filter#Probability_of_false_positives for the
     * formula.
     *
     * @param n expected insertions (must be positive)
     * @param p false positive rate (must be 0 < p < 1)
     */
    @VisibleForTesting
    static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }
}
