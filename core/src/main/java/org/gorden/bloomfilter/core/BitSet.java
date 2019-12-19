package org.gorden.bloomfilter.core;

import com.google.common.base.Predicate;

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
     * bit set used bit
     */
    long bitCount();

}
