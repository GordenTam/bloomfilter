package org.gorden.bloomfilter.aspect.observer;

import org.gorden.bloomfilter.core.BloomFilter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BloomFilterObserver {
    private static Map<String, BloomFilter> map = new ConcurrentHashMap<>();

    public static void registered(String name, BloomFilter bloomFilter) {
        map.put(name, bloomFilter);
    }

    public static BloomFilter getBloomFilter(String name) {
        return map.get(name);
    }
}
