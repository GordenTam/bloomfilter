package org.gorden.bloomfilter.examples;

import org.gorden.bloomfilter.examples.bitset.BitSet;
import org.gorden.bloomfilter.examples.bitset.RedisBitSet;
import org.gorden.bloomfilter.examples.hash.HashFunction;
import org.gorden.bloomfilter.examples.observer.BloomFilterObserver;
import org.gorden.bloomfilter.examples.serializer.BloomFilterSerializer;

/**
 * @author GordenTam
 **/

public class RedisBloomFilter<T> extends AbstractBloomFilter<T> {

    private String name;

    private RedisBloomFilter(int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
        super(numHashFunctions, bitSet, bloomFilterSerializer, hashFunction);
    }

    public static <T> RedisBloomFilter<T> create(String name, long expectedInsertions, double fpp, RedisOperator redisOperator, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException(String.format("expectedInsertions (%s) must be > 0", expectedInsertions));
        }
        if (fpp >= 1.0) {
            throw new IllegalArgumentException(String.format("numHashFunctions (%s) must be < 1.0", fpp));
        }
        if (fpp <= 0.0) {
            throw new IllegalArgumentException(String.format("numHashFunctions (%s) must be > 0.0", fpp));
        }
        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
        RedisBloomFilter<T> redisBloomFilter = new RedisBloomFilter<T>(numHashFunctions, new RedisBitSet(numBits, name, redisOperator), bloomFilterSerializer, hashFunction);
        BloomFilterObserver.registered(name, redisBloomFilter);
        return redisBloomFilter;
    }

}
