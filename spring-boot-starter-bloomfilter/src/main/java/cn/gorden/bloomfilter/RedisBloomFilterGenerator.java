package cn.gorden.bloomfilter;

import cn.gorden.bloomfilter.core.RedisOperator;
import cn.gorden.bloomfilter.core.hash.HashFunction;
import cn.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import cn.gorden.bloomfilter.core.RedisBloomFilter;

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
        bloomFilterProperties.getNames().forEach(
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
