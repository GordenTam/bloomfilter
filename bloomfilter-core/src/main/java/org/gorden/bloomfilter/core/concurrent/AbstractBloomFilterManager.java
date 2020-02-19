package org.gorden.bloomfilter.core.concurrent;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.gorden.bloomfilter.core.BloomFilter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractBloomFilterManager {
    protected final ConcurrentMap<String, BloomFilter> bloomFilterMap = new ConcurrentHashMap<>(16);

    protected AbstractBloomFilterManager() {
    }

    protected abstract void initBloomFilterMap();

    @Nullable
    public BloomFilter getBloomFilter(String name) {
        BloomFilter bf = (BloomFilter) this.bloomFilterMap.get(name);
        if (bf != null) {
            return bf;
        } else {
            ConcurrentMap var3 = this.bloomFilterMap;
            synchronized (this.bloomFilterMap) {
                bf = (BloomFilter) this.bloomFilterMap.get(name);
                if (bf == null) {
                    bf = this.getMissingBloomFilter(name);
                    if (bf != null) {
                        this.bloomFilterMap.put(name, bf);
                    }
                }

                return bf;
            }
        }
    }

    @Nullable
    protected final BloomFilter lookupBloomFilter(String name) {
        return (BloomFilter) this.bloomFilterMap.get(name);
    }

    @Nullable
    protected BloomFilter getMissingBloomFilter(String name) {
        return null;
    }
}
