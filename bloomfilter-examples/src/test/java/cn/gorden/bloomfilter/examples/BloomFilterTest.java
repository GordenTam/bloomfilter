package cn.gorden.bloomfilter.examples;

import cn.gorden.bloomfilter.core.BloomFilter;
import cn.gorden.bloomfilter.core.RedisBloomFilter;
import cn.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import cn.gorden.bloomfilter.core.observer.BloomFilterObserver;
import cn.gorden.bloomfilter.core.serializer.FastJsonBloomFilterSerializer;
import cn.gorden.bloomfilter.core.util.SimpleJedisOperator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author GordenTam
 **/
public class BloomFilterTest {

    @Test
    public void bloomFilterUsingExample() {
        SimpleJedisOperator jedisOperator = SimpleJedisOperator.getInstance();
        BloomFilter bf = RedisBloomFilter.builder()
                .withName("test")
                .withFpp(0.03)
                .withRedisOperator(jedisOperator)
                .withExpectedInsertions(1000000L)
                .withHashFunction(new Murmur3_128HashFunction(0))
                .withBloomFilterSerializer(new FastJsonBloomFilterSerializer())
                .build();

        bf.put("apple");
        assertTrue(bf.mightContain("apple"));
        assertFalse(bf.mightContain("banana"));
        BloomFilter b = BloomFilterObserver.getBloomFilter("test").get();
        System.out.println(b.getName());
    }
}
