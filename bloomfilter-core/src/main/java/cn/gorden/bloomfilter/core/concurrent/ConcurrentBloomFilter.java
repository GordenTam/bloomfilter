package cn.gorden.bloomfilter.core.concurrent;

import cn.gorden.bloomfilter.common.BitSet;
import cn.gorden.bloomfilter.core.bitset.LockFreeBitSet;
import cn.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import cn.gorden.bloomfilter.core.AbstractBloomFilter;
import cn.gorden.bloomfilter.core.hash.HashFunction;
import cn.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import cn.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;

/**
 * In memory bloom filter
 *
 * @author GordenTam
 **/

public class ConcurrentBloomFilter extends AbstractBloomFilter {

    private ConcurrentBloomFilter(String name, int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
        super(name, numHashFunctions, bitSet, bloomFilterSerializer, hashFunction);
    }

    public static ConcurrentBloomFilter create(String name, long expectedInsertions, double fpp) {
        return create(name, expectedInsertions, fpp, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
    }

    public static ConcurrentBloomFilter create(String name, long expectedInsertions, double fpp, BloomFilterSerializer bloomFilterSerializer) {
        return create(name, expectedInsertions, fpp, bloomFilterSerializer, new Murmur3_128HashFunction(0));
    }

    public static ConcurrentBloomFilter create(String name, long expectedInsertions, double fpp, HashFunction hashFunction) {
        return create(name, expectedInsertions, fpp, new JdkSerializationBloomFilterSerializer(), hashFunction);
    }

    public static ConcurrentBloomFilter create(String name, long expectedInsertions, double fpp, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
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
        return new ConcurrentBloomFilter(name, numHashFunctions, new LockFreeBitSet(numBits), bloomFilterSerializer, hashFunction);
    }

    public static ConcurrentBloomFilter create(Builder builder) {
        return create(builder.name, builder.expectedInsertions, builder.fpp, builder.bloomFilterSerializer, builder.hashFunction);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private long expectedInsertions;
        private double fpp;
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

        public Builder withBloomFilterSerializer(BloomFilterSerializer bloomFilterSerializer) {
            this.bloomFilterSerializer = bloomFilterSerializer;
            return this;
        }

        public Builder withHashFunction(HashFunction hashFunction) {
            this.hashFunction = hashFunction;
            return this;
        }

        public ConcurrentBloomFilter build() {
            return ConcurrentBloomFilter.create(this);
        }
    }

}
