package org.gorden.bloomfilter.core.bitset;

/**
 * @author: GordenTam
 * @create: 2019-12-17
 **/

public interface BitSet {

    boolean set(long bitIndex);

    boolean get(long bitIndex);

    /**
     * bit set total bit
     */
    long bitSize();

    /**
     * bit num of 1
     */
    long bitCount();

    void clear();

}
