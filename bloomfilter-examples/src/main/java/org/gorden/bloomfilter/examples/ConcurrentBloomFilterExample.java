package org.gorden.bloomfilter.examples;

import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
import org.gorden.bloomfilter.aspect.annotation.BFPut;
import org.gorden.bloomfilter.examples.concurrent.ConcurrentBloomFilter;
import org.gorden.bloomfilter.examples.hash.Murmur3_128HashFunction;
import org.gorden.bloomfilter.examples.serializer.JdkSerializationBloomFilterSerializer;

public class ConcurrentBloomFilterExample {

    public static void main(String[] args) {
        ConcurrentBloomFilter<String> concurrentBloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
        concurrentBloomFilter.put("a");
        concurrentBloomFilter.put("b");
        concurrentBloomFilter.put("c");
        concurrentBloomFilter.mightContain("a");
        concurrentBloomFilter.mightContain("b");
        concurrentBloomFilter.mightContain("c");
        concurrentBloomFilter.mightContain("d");
        concurrentBloomFilter.clear();
    }

    @BFPut(value = "test")
    private String test(String str) {
        System.out.println("bloomfilter put string" + str);
        return str;
    }

    @BFMightContain(value = "test", fallback = "BFFallback")
    private String test1(String str) {
        return str;
    }

    private String BFFallback(String str) {
        System.out.println();
        return str;
    }

}
