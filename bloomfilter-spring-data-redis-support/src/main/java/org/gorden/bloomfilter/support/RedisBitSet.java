package org.gorden.bloomfilter.support;

import com.google.common.math.LongMath;
import org.gorden.bloomfilter.core.bitset.BitSet;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * BitSet implements by redis bitmaps
 * @author: GordenTam
 * @create: 2019-12-17
 **/

public class RedisBitSet implements BitSet {

    private final byte[] RedisRawKey;

    private RedisConnectionFactory connectionFactory;

    private long bitSize;

    public RedisBitSet(long bits, String bitSetKey, RedisConnectionFactory connectionFactory) {
        //bit size must be n*64
        this.connectionFactory = connectionFactory;
        this.RedisRawKey = bitSetKey.getBytes(Charset.forName("utf-8"));;
        this.bitSize = LongMath.divide(bits, 64, RoundingMode.CEILING) * Long.SIZE;//位数组的长度，相当于n个long的长度
        initBitSet();
    }

    private void initBitSet() {
        this.execute((connection) -> {
            connection.setBit(RedisRawKey, bitSize - 1 ,false);
            return "OK";
        });
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

    private <T> T execute(Function<RedisConnection, T> callback) {
        RedisConnection connection = this.connectionFactory.getConnection();

        T var4;
        try {
            var4 = callback.apply(connection);
        } finally {
            connection.close();
        }

        return var4;
    }
}