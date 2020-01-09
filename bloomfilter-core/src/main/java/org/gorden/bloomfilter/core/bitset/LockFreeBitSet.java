package org.gorden.bloomfilter.core.bitset;

import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author GordenTam
 */
public class LockFreeBitSet implements BitSet {

    private static final int LONG_ADDRESSABLE_BITS = 6;
    private final AtomicLongArray data;
    private final AtomicLong bitCount;

    LockFreeBitSet(long bits) {
        if(bits <= 0){
            throw new IllegalArgumentException("data length is zero!");
        }

        this.data = new AtomicLongArray(Ints.checkedCast(LongMath.divide(bits, 64, RoundingMode.CEILING)));
        this.bitCount = new AtomicLong(0);
    }

    LockFreeBitSet(long[] data) {
        if(data.length <= 0){
            throw new IllegalArgumentException("data length is zero!");
        }
        this.data = new AtomicLongArray(data);
        this.bitCount = new AtomicLong(0);
        long bitCount = 0;
        for (long value : data) {
            bitCount += Long.bitCount(value);
        }
        this.bitCount.getAndAdd(bitCount);
    }

    public boolean set(long bitIndex) {
        if (get(bitIndex)) {
            return false;
        }

        int longIndex = (int) (bitIndex >>> LONG_ADDRESSABLE_BITS);
        long mask = 1L << bitIndex;

        long oldValue;
        long newValue;
        do {
            oldValue = data.get(longIndex);
            newValue = oldValue | mask;
            if (oldValue == newValue) {
                return false;
            }
        } while (!data.compareAndSet(longIndex, oldValue, newValue));

        bitCount.getAndIncrement();
        return true;
    }

    public boolean get(long bitIndex) {
        return (data.get((int) (bitIndex >>> LONG_ADDRESSABLE_BITS)) & (1L << bitIndex)) != 0;
    }

    public long bitSize() {
        return (long) data.length() * Long.SIZE;
    }

    public long bitCount() {
        return bitCount.get();
    }
}
