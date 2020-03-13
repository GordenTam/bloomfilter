package cn.gorden.bloomfilter;

import cn.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
import cn.gorden.bloomfilter.core.hash.HashFunction;
import cn.gorden.bloomfilter.core.serializer.BloomFilterSerializer;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/

public class JdkBloomFilterGenerator extends BloomFilterGenerator {

    private BloomFilterProperties bloomFilterProperties;
    private HashFunction hashFunction;
    private BloomFilterSerializer bloomFilterSerializer;

    public JdkBloomFilterGenerator(BloomFilterProperties bloomFilterProperties, HashFunction hashFunction, BloomFilterSerializer bloomFilterSerializer) {
        this.bloomFilterProperties = bloomFilterProperties;
        this.hashFunction = hashFunction;
        this.bloomFilterSerializer = bloomFilterSerializer;
    }

    @Override
    public void createBloomFilter() {
        bloomFilterProperties.getNames().forEach(
                name -> ConcurrentBloomFilter.builder()
                        .withName(name)
                        .withFpp(bloomFilterProperties.getFpp())
                        .withExpectedInsertions(bloomFilterProperties.getExpectedInsertions())
                        .withHashFunction(hashFunction)
                        .withBloomFilterSerializer(bloomFilterSerializer)
                        .build());
    }
}
