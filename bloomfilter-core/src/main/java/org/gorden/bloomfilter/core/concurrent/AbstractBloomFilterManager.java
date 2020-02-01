package org.gorden.bloomfilter.core.concurrent;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.gorden.bloomfilter.core.BloomFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractBloomFilterManager {
    private final ConcurrentMap<String, BloomFilter> bloomFilterMap = new ConcurrentHashMap<>(16);
    private volatile Set<String> bloomFilterNames = Collections.emptySet();

    public AbstractBloomFilterManager(Map<String, BloomFilter> initBloomFilters) {
        Collection<? extends Cache> caches = this.loadCaches();
        ConcurrentMap var2 = this.cacheMap;
        synchronized(this.cacheMap) {
            this.cacheNames = Collections.emptySet();
            this.cacheMap.clear();
            Set<String> cacheNames = new LinkedHashSet(caches.size());
            Iterator var4 = caches.iterator();

            while(var4.hasNext()) {
                Cache cache = (Cache)var4.next();
                String name = cache.getName();
                this.cacheMap.put(name, this.decorateCache(cache));
                cacheNames.add(name);
            }

            this.cacheNames = Collections.unmodifiableSet(cacheNames);
        }
    }

    @Nullable
    public BloomFilter getBloomFilter(String name) {
        BloomFilter bf = (BloomFilter)this.bloomFilterMap.get(name);
        if (bf != null) {
            return bf;
        } else {
            ConcurrentMap var3 = this.bloomFilterMap;
            synchronized(this.bloomFilterMap) {
                bf = (BloomFilter)this.bloomFilterMap.get(name);
                if (bf == null) {
                    bf = this.getMissingBloomFilter(name);
                    if (bf != null) {
                        this.bloomFilterMap.put(name, bf);
                        this.updateCacheNames(name);
                    }
                }

                return bf;
            }
        }
    }

    public Collection<String> getBloomFilterNames() {
        return this.bloomFilterNames;
    }

    @Nullable
    protected final BloomFilter lookupBloomFilter(String name) {
        return (BloomFilter)this.bloomFilterMap.get(name);
    }

    private void updateCacheNames(String name) {
        Set<String> cacheNames = new LinkedHashSet<>(this.bloomFilterNames.size() + 1);
        cacheNames.addAll(this.bloomFilterNames);
        cacheNames.add(name);
        this.bloomFilterNames = Collections.unmodifiableSet(cacheNames);
    }

    @Nullable
    protected BloomFilter getMissingBloomFilter(String name) {
        return null;
    }
}
