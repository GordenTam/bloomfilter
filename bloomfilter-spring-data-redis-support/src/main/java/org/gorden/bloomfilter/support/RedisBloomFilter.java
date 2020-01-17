package org.gorden.bloomfilter.support;

import org.gorden.bloomfilter.core.BloomFilter;
import org.gorden.bloomfilter.core.bitset.BitSet;
import org.gorden.bloomfilter.core.bitset.LockFreeBitSet;
import org.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
import org.gorden.bloomfilter.core.hash.Hasher;
import org.gorden.bloomfilter.core.hash.Murmur3_128Hasher;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import org.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author: GordenTam
 * @create: 2020-01-16
 **/

public class RedisBloomFilter<T> extends BloomFilter<T> {

    private String name;

    private RedisBloomFilter(int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, Hasher hasher) {
        super(numHashFunctions, bitSet, bloomFilterSerializer, hasher);
    }

    public static <T> RedisBloomFilter<T> create(String name, long expectedInsertions, double fpp, RedisConnectionFactory redisConnectionFactory, BloomFilterSerializer bloomFilterSerializer, Hasher hasher) {
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
        return new RedisBloomFilter<T>(numHashFunctions, new RedisBitSet(numBits, name, redisConnectionFactory), bloomFilterSerializer, hasher);
    }

}
