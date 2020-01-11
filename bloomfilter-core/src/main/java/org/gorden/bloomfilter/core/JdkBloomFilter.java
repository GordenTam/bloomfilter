package org.gorden.bloomfilter.core;

import org.gorden.bloomfilter.core.bitset.BitSet;
import org.gorden.bloomfilter.core.bitset.LockFreeBitSet;

/**
 * @author: GordenTam
 * @create: 2020-01-10
 **/

public class JdkBloomFilter<T> extends BloomFilter<T> {

    public JdkBloomFilter(long expectedInsertions, double fpp) {
        super(expectedInsertions, fpp);
    }

}
