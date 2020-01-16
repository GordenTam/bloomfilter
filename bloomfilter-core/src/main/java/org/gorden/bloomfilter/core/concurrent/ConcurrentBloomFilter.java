package org.gorden.bloomfilter.core.concurrent;

import org.gorden.bloomfilter.core.BloomFilter;

/**
 * @author: GordenTam
 * @create: 2020-01-15
 **/

public class ConcurrentBloomFilter<T> extends BloomFilter<T> {

    public ConcurrentBloomFilter(long expectedInsertions, double fpp) {
        int hashNums = optimalNumOfHashFunctions(expectedInsertions, fpp);
        this(expectedInsertions, fpp);
    }

}
