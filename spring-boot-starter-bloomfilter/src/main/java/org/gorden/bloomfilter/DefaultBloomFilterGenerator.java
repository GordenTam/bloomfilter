package org.gorden.bloomfilter;

import org.gorden.bloomfilter.core.RedisBloomFilter;
import org.gorden.bloomfilter.core.RedisOperator;
import org.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/

public class DefaultBloomFilterGenerator extends BloomFilterGenerator {

    private BloomFilterProperties bloomFilterProperties;
    private RedisOperator redisOperator;

    public DefaultBloomFilterGenerator(BloomFilterProperties bloomFilterProperties, RedisOperator redisOperator) {
        this.bloomFilterProperties = bloomFilterProperties;
        this.redisOperator = redisOperator;
    }

    @Override
    public void createBloomFilter() {
        BloomFilterProperties.BloomFilterType type = bloomFilterProperties.getType();
        Class<?> serializationClass = bloomFilterProperties.getSerializationClass();
        serializationClass.();
        if (type.equals(BloomFilterProperties.BloomFilterType.JDK)) {
            bloomFilterProperties.getNames()
                    .stream()
                    .map( name -> ConcurrentBloomFilter
                            .create(ConcurrentBloomFilter.builder()
                                    .withName(name)
                                    .withFpp().withExpectedInsertions().build()));
        } else if (type.equals(BloomFilterProperties.BloomFilterType.REDIS)) {
            bloomFilterProperties.getNames()
                    .stream()
                    .map( name -> RedisBloomFilter
                            .create(RedisBloomFilter.builder()
                                    .withName(name)
                                    .withFpp().withRedisOperator(redisOperator).withwithExpectedInsertions().build()));
        }
    }
}
