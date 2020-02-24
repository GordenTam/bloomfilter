package org.gorden.bloomfilter.common;

/**
 * @author GordenTam
 * @since 1.0
 **/
public interface BloomFilter {
    /**
     * put an object to bloom filter
     *
     * @param object the object to be put into bloom filter
     */
    boolean put(Object object);

    /**
     * if the object might contain in bloom filter
     *
     * @param object the object to judge
     */
    boolean mightContain(Object object);

    /**
     * clear the bloom filter
     */
    void clear();

    String getName();

}
