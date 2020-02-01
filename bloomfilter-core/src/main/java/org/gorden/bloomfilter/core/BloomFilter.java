package org.gorden.bloomfilter.core;

/**
 * @author GordenTam
 * @since 1.0
 **/
public interface BloomFilter<T> {

    String getName();

    /**
     * put an object to bloom filter
     * @param object the object to be put into bloom filter
     */
    boolean put(T object);

    /**
     * if the object might contain in bloom filter
     * @param object the object to judge
     */
    boolean mightContain(T object);

    /**
     * clear the bloom filter
     */
    void clear();

}
