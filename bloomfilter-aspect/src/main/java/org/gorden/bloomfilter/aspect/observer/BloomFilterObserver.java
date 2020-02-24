package org.gorden.bloomfilter.aspect.observer;

import com.sun.istack.internal.Nullable;
import org.gorden.bloomfilter.common.BloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BloomFilterObserver {
    private static final Logger logger = LoggerFactory.getLogger(BloomFilterObserver.class);
    private static Map<String, BloomFilter> map = new ConcurrentHashMap<>(32);

    public static void registered(String name, BloomFilter bloomFilter) {
        if(map.containsKey(name)) {
            logger.warn("the BloomFilter with name: " + name + " has already bean created, the new created with the same name will cover it");
        }
        map.put(name, bloomFilter);
    }

    @Nullable
    public static BloomFilter getBloomFilter(String name) {
        return map.get(name);
    }
}
