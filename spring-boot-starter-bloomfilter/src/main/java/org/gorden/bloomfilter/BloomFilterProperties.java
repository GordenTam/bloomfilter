package org.gorden.bloomfilter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: GordenTam
 * @create: 2019-12-18
 **/
@ConfigurationProperties(prefix = "bloom-filter")
public class BloomFilterProperties {

    private List<String> names = new ArrayList<>();

    private long expectedInsertions;

    private double fpp;

    public List<String> getNames() {
        return this.names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public long getExpectedInsertions(){
        return this.expectedInsertions;
    }

    public void setExpectedInsertions(long expectedInsertions){
        this.expectedInsertions = expectedInsertions;
    }

    public double getFpp() {
        return fpp;
    }

    public void setFpp(double fpp) {
        this.fpp = fpp;
    }
}
