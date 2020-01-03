package org.gorden.bloomfilter.core;

import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * lock-free bitSet
 *
 * We use this instead of java.util.BitSet because we need access to the array of longs and we
 * need compare-and-swap.
 *
 * @author GordenTam
 * @create: 2019-12-17
 */
public class LockFreeBitSet implements BitSet{

    private static final int LONG_ADDRESSABLE_BITS = 6;
    private final AtomicLongArray data;
    private final AtomicLong bitCount;

    LockFreeBitSet(long bits) {
        checkArgument(bits > 0, "data length is zero!");
        // Avoid delegating to this(long[]), since AtomicLongArray(long[]) will clone its input and
        // thus double memory usage.
        this.data = new AtomicLongArray(Ints.checkedCast(LongMath.divide(bits, 64, RoundingMode.CEILING)));
        this.bitCount = new AtomicLong(0);
    }

    // Used by serialization
    LockFreeBitSet(long[] data) {
        checkArgument(data.length > 0, "data length is zero!");
        this.data = new AtomicLongArray(data);
        this.bitCount = new AtomicLong(0);
        long bitCount = 0;
        for (long value : data) {
            bitCount += Long.bitCount(value);
        }
        this.bitCount.getAndAdd(bitCount);
    }

    /** Returns true if the bit changed value. */
    public boolean set(long bitIndex) {
        if (get(bitIndex)) {
            return false;
        }

        int longIndex = (int) (bitIndex >>> LONG_ADDRESSABLE_BITS);
        long mask = 1L << bitIndex; // only cares about low 6 bits of bitIndex

        long oldValue;
        long newValue;
        do {
            oldValue = data.get(longIndex);
            newValue = oldValue | mask;
            if (oldValue == newValue) {
                return false;
            }
        } while (!data.compareAndSet(longIndex, oldValue, newValue));

        // We turned the bit on, so increment bitCount.
        bitCount.getAndIncrement();
        return true;
    }

    public boolean get(long bitIndex) {
        return (data.get((int) (bitIndex >>> LONG_ADDRESSABLE_BITS)) & (1L << bitIndex)) != 0;
    }

    /** Number of bits */
    public long bitSize() {
        return (long) data.length() * Long.SIZE;
    }

    /**Number of set bits*/
    public long bitCount() {
        return bitCount.get();
    }
}
