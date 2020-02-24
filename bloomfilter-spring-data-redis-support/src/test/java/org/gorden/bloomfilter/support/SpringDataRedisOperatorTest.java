package org.gorden.bloomfilter.support;

import org.gorden.bloomfilter.core.RedisBloomFilter;
import org.junit.Test;
public class SpringDataRedisOperatorTest {

    @Test
    public void test() {
        RedisBloomFilter redisBloomFilter = RedisBloomFilter.create(RedisBloomFilter.builder().withName("testRedisBloomFilter").withBloomFilterSerializer().withExpectedInsertions(100000L).withFpp(0.03).withHashFunction().withRedisOperator());
    }

}
