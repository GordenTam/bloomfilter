package org.gorden.bloomfilter;

import org.gorden.bloomfilter.core.RedisBloomFilter;
import org.gorden.bloomfilter.core.RedisOperator;
import org.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
import org.gorden.bloomfilter.core.hash.HashFunction;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/

public class RedisBloomFilterGenerator extends BloomFilterGenerator {

    private BloomFilterProperties bloomFilterProperties;
    private RedisOperator redisOperator;
    private HashFunction hashFunction;
    private BloomFilterSerializer bloomFilterSerializer;

    public RedisBloomFilterGenerator(BloomFilterProperties bloomFilterProperties, RedisOperator redisOperator,
                                       HashFunction hashFunction, BloomFilterSerializer bloomFilterSerializer) {
        this.bloomFilterProperties = bloomFilterProperties;
        this.redisOperator = redisOperator;
        this.hashFunction = hashFunction;
        this.bloomFilterSerializer = bloomFilterSerializer;
    }

    @Override
    public void createBloomFilter() {
        BloomFilterProperties.BloomFilterType type = bloomFilterProperties.getType();
        bloomFilterProperties.getNames().stream().map(
                name -> RedisBloomFilter.builder()
                        .withName(name)
                        .withFpp(bloomFilterProperties.getFpp())
                        .withRedisOperator(redisOperator)
                        .withExpectedInsertions(bloomFilterProperties.getExpectedInsertions())
                        .withHashFunction(hashFunction)
                        .withBloomFilterSerializer(bloomFilterSerializer)
                        .build());
    }


}
