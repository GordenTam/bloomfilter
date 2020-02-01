package org.gorden.bloomfilter.core.bitset;

import org.gorden.bloomfilter.core.RedisOperator;
import java.nio.charset.Charset;

/**
 * BitSet implements by redis bitmaps
 * @author GordenTam
 **/

public class RedisBitSet implements BitSet {

    private final byte[] RedisRawKey;

    private RedisOperator redisOperator;

    private long bitSize;

    public RedisBitSet(long bits, String bitSetKey, RedisOperator redisOperator) {
        //bit size must be n*64
        this.redisOperator = redisOperator;
        this.RedisRawKey = bitSetKey.getBytes(Charset.forName("utf-8"));
        this.bitSize = bits;
        initBitSet();
    }

    private void initBitSet() {

    }

    public boolean get(long[] offsets) {
        boolean result = true;
        for(long offset : offsets) {
            result = result & this.get(offsets);
        }
        return result;
    }

    public boolean get(final long offset) {
        return this.execute((connection) -> {
            return connection.getBit(RedisRawKey, offset);
        });
    }

    public boolean set(long[] offsets) {
        boolean bitsChanged = false;
        for (long offset : offsets)
            bitsChanged |= set(offset);
        return bitsChanged;
    }

    public boolean set(long offset) {
        this.execute((connection) -> {
            connection.setBit(RedisRawKey, bitSize - 1 ,false);
            return "OK";
        });
        return false;
    }

    public long bitCount() {
        return this.execute((connection) -> {
            return connection.bitCount(RedisRawKey);
        });
    }

    public long bitSize() {
        return this.bitSize;
    }

    /**
     * reset the bit map
     */
    public void clear() {
        this.execute((connection) -> {
            return connection.del(RedisRawKey);
        });
        initBitSet();
    }


}