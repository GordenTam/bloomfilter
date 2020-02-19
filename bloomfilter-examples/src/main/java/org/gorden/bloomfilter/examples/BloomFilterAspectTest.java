package org.gorden.bloomfilter.examples;

import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
import org.gorden.bloomfilter.aspect.annotation.BFPut;

/**
 * @author GordenTam
 **/
public class BloomFilterAspectTest {

    @BFPut(value = "test")
    public String test(String str) {
        System.out.println("bloomfilter put string" + str);
        return str;
    }

    @BFMightContain(value = "test", fallback = "BFFallback")
    public String test1(String str) {
        return str;
    }

    public String BFFallback(String str) {
        System.out.println("str");
        return str;
    }
}
