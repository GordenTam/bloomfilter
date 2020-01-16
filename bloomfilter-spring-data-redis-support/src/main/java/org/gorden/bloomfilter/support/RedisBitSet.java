package org.gorden.bloomfilter.support;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import com.google.common.primitives.Longs;
import org.gorden.bloomfilter.core.bitset.BitSet;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * BitSet implements by redis bitmaps
 * @author: GordenTam
 * @create: 2019-12-17
 **/

public class RedisBitSet implements BitSet {

    private static final Logger logger = LoggerFactory.getLogger(RedisBitSet.class);

    private static final String BASE_KEY = "bloomFilter";

    private static final String CURSOR = "cursor";

    private RedisTemplate<String, String> redisTemplate;

    private long bitSize;

    public RedisBitSet(long bits, RedisTemplate<String,String> redisTemplate) {
        //bit size must be n*64
        this.redisTemplate = redisTemplate;
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
            //每次set之后,都进行扩容判断
            ensureCapacityInternal();
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

    private void ensureCapacityInternal() {
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
    public void reset() {
        List<String> keys = new ArrayList<>();
        for(int i = 0; i<=cursor(); i++){
            keys.add(genkey(i));
        }
        redisTemplate.delete(keys);
        redisTemplate.opsForValue().set(CURSOR, "0");
        redisTemplate.execute((RedisCallback<Boolean>) con -> con.setBit(currentKey().getBytes(Charset.forName("utf-8")), bitSize-1, false));
    }
}