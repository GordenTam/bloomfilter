package org.gorden.bloomfilter.core;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

public interface BloomFilterManager {
    @Nullable
    BloomFilter getBloomFilter(String name);

    Collection<String> getBloomFilterNames();
}
