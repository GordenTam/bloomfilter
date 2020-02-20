package org.gorden.bloomfilter.core;

import org.gorden.bloomfilter.core.bitset.BitSet;
import org.gorden.bloomfilter.core.bitset.RedisBitSet;
import org.gorden.bloomfilter.core.hash.HashFunction;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;

/**
 * @author GordenTam
 **/

public class RedisBloomFilter extends AbstractBloomFilter {

    private String name;

    private RedisBloomFilter(int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
        super(numHashFunctions, bitSet, bloomFilterSerializer, hashFunction);
    }

    public static RedisBloomFilter create(String name, long expectedInsertions, double fpp, RedisOperator redisOperator, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
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
        return new RedisBloomFilter(numHashFunctions, new RedisBitSet(numBits, name, redisOperator), bloomFilterSerializer, hashFunction);
    }

    public static RedisBloomFilter create(Builder builder) {
        return create(builder.name, builder);
    }

    public static class Builder {

        private String name;
        private long expectedInsertions;
        private double fpp;
        private RedisOperator redisOperator;
        private BloomFilterSerializer bloomFilterSerializer;
        private HashFunction hashFunction;

        public Builder withName(String name) {
            this.name = name;
        }

        public Builder withExpectedInsertions(long expectedInsertions) {
            this.expectedInsertions = expectedInsertions;
        }

        public Builder withName() {
            this.name = name;
        }

        public Builder withName() {
            this.name = name;
        }

        public Builder withName() {
            this.name = name;
        }
    }

}
