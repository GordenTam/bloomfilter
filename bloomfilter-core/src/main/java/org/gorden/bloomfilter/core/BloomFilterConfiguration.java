package org.gorden.bloomfilter.core;

import org.gorden.bloomfilter.core.hash.HashFunction;
import org.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import org.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;

public class BloomFilterConfiguration {

    private final long expectedInsertions;

    private final double fpp;

    private final BloomFilterSerializer bloomFilterSerializer;

    private final HashFunction hashFunction;

    private BloomFilterConfiguration(long expectedInsertions, double fpp, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.bloomFilterSerializer = bloomFilterSerializer;
        this.hashFunction = hashFunction;
    }

    public static BloomFilterConfiguration defaultConfig() {
        return new BloomFilterConfiguration(100000L, 0.01, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public double getFpp() {
        return fpp;
    }

    public BloomFilterSerializer getBloomFilterSerializer() {
        return bloomFilterSerializer;
    }

    public HashFunction getHashFunction() {
        return hashFunction;
    }
}
