package cn.gorden.bloomfilter.core;

import cn.gorden.bloomfilter.common.BitSet;
import cn.gorden.bloomfilter.core.bitset.RedisBitSet;
import cn.gorden.bloomfilter.core.hash.HashFunction;
import cn.gorden.bloomfilter.core.serializer.BloomFilterSerializer;

/**
 * @author GordenTam
 **/

public class RedisBloomFilter extends AbstractBloomFilter {

    private RedisBloomFilter(String name, int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
        super(name ,numHashFunctions, bitSet, bloomFilterSerializer, hashFunction);
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
        return new RedisBloomFilter(name, numHashFunctions, new RedisBitSet(numBits, name, redisOperator), bloomFilterSerializer, hashFunction);
    }

    public static RedisBloomFilter create(Builder builder) {
        return create(builder.name, builder.expectedInsertions, builder.fpp, builder.redisOperator, builder.bloomFilterSerializer, builder.hashFunction);
    }

    public static Builder builder() {
        return new Builder();
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
            return this;
        }

        public Builder withExpectedInsertions(long expectedInsertions) {
            this.expectedInsertions = expectedInsertions;
            return this;
        }

        public Builder withFpp(double fpp) {
            this.fpp = fpp;
            return this;
        }

        public Builder withRedisOperator(RedisOperator redisOperator) {
            this.redisOperator = redisOperator;
            return this;
        }

        public Builder withBloomFilterSerializer(BloomFilterSerializer bloomFilterSerializer) {
            this.bloomFilterSerializer = bloomFilterSerializer;
            return this;
        }

        public Builder withHashFunction(HashFunction hashFunction) {
            this.hashFunction = hashFunction;
            return this;
        }

        public RedisBloomFilter build() {
            return RedisBloomFilter.create(this);
        }
    }

}
