package org.gorden.bloomfilter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/

public abstract class BloomFilterGenerator implements CommandLineRunner {


    public void run(String... args) throws Exception {
        createBloomFilter();
    }

    public abstract void createBloomFilter();

}
