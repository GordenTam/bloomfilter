package org.gorden.bloomfilter.examples.bitset;

import org.gorden.bloomfilter.examples.RedisOperator;

import java.nio.charset.Charset;

/**
 * BitSet implements by redis bitmaps
 *
 * @author GordenTam
 **/

public class RedisBitSet implements BitSet {

    private final byte[] rawKey;

    private RedisOperator redisOperator;

    private long bitSize;

    public RedisBitSet(long bits, String bitSetKey, RedisOperator redisOperator) {
        //bit size must be n*64
        this.redisOperator = redisOperator;
        this.rawKey = bitSetKey.getBytes(Charset.forName("utf-8"));
        this.bitSize = bits;
        initBitSet();
    }

    private void initBitSet() {

    }

    public boolean get(long[] offsets) {
        boolean result = true;
        for (long offset : offsets) {
            result = result & this.get(offsets);
        }
        return result;
    }

    public boolean get(final long offset) {
        return redisOperator.getBit(rawKey, offset);
    }

    public boolean set(long[] offsets) {
        boolean bitsChanged = false;
        for (long offset : offsets)
            bitsChanged |= set(offset);
        return bitsChanged;
    }

    public boolean set(long offset) {
        redisOperator.setBit(rawKey, offset);
        return false;
    }

    public long bitCount() {
        return redisOperator.bitCount(rawKey);
    }

    public long bitSize() {
        return this.bitSize;
    }

    /**
     * reset the bit map
     */
    public void clear() {
        redisOperator.del(rawKey);
        initBitSet();
    }


}