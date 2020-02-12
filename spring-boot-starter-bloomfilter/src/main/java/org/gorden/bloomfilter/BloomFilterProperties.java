package org.gorden.bloomfilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: GordenTam
 * @create: 2019-12-18
 **/
public class BloomFilterProperties {

    private BloomFilterType type;

    private List<String> names = new ArrayList<>();

    public BloomFilterProperties() {
    }

    public BloomFilterType getType() {
        return this.type;
    }

    public void setType(BloomFilterType mode) {
        this.type = mode;
    }

    private long expectedInsertions;

    private double fpp;

    public List<String> getNames() {
        return this.names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public long getExpectedInsertions() {
        return this.expectedInsertions;
    }

    public void setExpectedInsertions(long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public double getFpp() {
        return fpp;
    }

    public void setFpp(double fpp) {
        this.fpp = fpp;
    }

    public enum BloomFilterType {
        GENERIC,
        REDIS;

        private BloomFilterType() {
        }
    }
}
