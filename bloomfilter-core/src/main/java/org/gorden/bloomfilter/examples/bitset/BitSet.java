package org.gorden.bloomfilter.examples.bitset;

/**
 * @author GordenTam
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
