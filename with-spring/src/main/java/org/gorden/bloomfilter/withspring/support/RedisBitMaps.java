package org.gorden.bloomfilter.withspring.support;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import com.google.common.primitives.Longs;
import org.gorden.bloomfilter.core.BitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.LongStream;

/**
 * bitSet implements by redis bitmaps and can Dynamic expansion
 * @author: GordenTam
 * @create: 2019-12-17
 **/

public class RedisBitMaps implements BitSet {

    private static final Logger log = LoggerFactory.getLogger(RedisBitMaps.class);

    private static final String BASE_KEY = "bloomFilter";

    private static final String CURSOR = "cursor";

    private RedisTemplate<String, String> redisTemplate;

    private long bitSize;

    public RedisBitMaps(long bits, RedisTemplate redisTemplate) {
        //bit size must be n*64
        this.bitSize = LongMath.divide(bits, 64, RoundingMode.CEILING) * Long.SIZE;//位数组的长度，相当于n个long的长度
        redisTemplate.execute((RedisCallback<Boolean>) con -> con.setBit(currentKey().getBytes(Charset.forName("utf-8")), bitSize - 1 ,false));
    }

    public boolean get(long[] offsets) {
        for (long i = 0; i < cursor() + 1; i++) {
            final long cursor = i;
            //只要有一个cursor对应的bitmap中，offsets全部命中，则表示可能存在
            boolean match = Arrays.stream(offsets).boxed()
                    .map(offset -> redisTemplate.execute((RedisCallback<Boolean>) con -> con.getBit(genkey(cursor).getBytes(Charset.forName("utf-8")), offset)))
                    .allMatch(b -> (Boolean) b);
            if (match)
                return true;
        }
        return false;
    }

    public boolean get(final long offset) {
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) con -> con.getBit(currentKey().getBytes(Charset.forName("utf-8")), offset));
        Preconditions.checkNotNull(result, "Operation on redis error.key:{}", currentKey());
        return result;
    }

    public boolean set(long[] offsets) {
        if (cursor() > 0 && get(offsets)) {
            return false;
        }
        boolean bitsChanged = false;
        for (long offset : offsets)
            bitsChanged |= set(offset);
        return bitsChanged;
    }

    public boolean set(long offset) {
        if (!get(offset)) {
            redisTemplate.execute((RedisCallback<Boolean>) con -> con.setBit(currentKey().getBytes(Charset.forName("utf-8")), offset, true));
            return true;
        }
        return false;
    }

    public long bitCount() {
        Long bitCount = redisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(currentKey().getBytes(Charset.forName("utf-8"))));
        Preconditions.checkNotNull(bitCount, "Operation on redis error.key:{}", currentKey());
        return bitCount;
    }

    public long bitSize() {
        return this.bitSize;
    }

    private String currentKey() {
        return genkey(cursor());
    }

    private String genkey(long cursor) {
        return BASE_KEY + "-" + cursor;
    }

    private Long cursor() {
        String cursor = redisTemplate.opsForValue().get(CURSOR);
        Preconditions.checkNotNull(cursor, "Operation on redis error.key:{}", CURSOR);
        return Longs.tryParse(cursor);
    }

    public void ensureCapacityInternal() {
        if (bitCount() * 2 > bitSize()){
            grow();
        }
    }

    private void grow() {
        Long cursor = redisTemplate.opsForValue().increment(CURSOR);
        Preconditions.checkNotNull(cursor, "Operation on redis error.key:{}", CURSOR);
        redisTemplate.execute((RedisCallback<Boolean>) con -> con.setBit(genkey(cursor).getBytes(Charset.forName("utf-8")), bitSize-1, false));
    }

    /**
     * reset the bit map
     */
    void reset() {
        String[] keys = LongStream.range(0, cursor() + 1).boxed().map(this::genkey).toArray(String[]::new);
        List<String> keysArray = new ArrayList<>();
        keysArray.
        redisTemplate.delete(keys);
        redisTemplate.opsForValue().set(CURSOR, "0");
        redisTemplate.execute((RedisCallback<Boolean>) con -> con.setBit(currentKey().getBytes(Charset.forName("utf-8")), bitSize-1, false));
    }
}