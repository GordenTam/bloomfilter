package org.gorden.bloomfilter.examples;

import org.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
import org.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import org.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;

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

        BloomFilterAspectTest bloomFilterAspectTest = new BloomFilterAspectTest();
        bloomFilterAspectTest.test("test");
        bloomFilterAspectTest.test1("a");
        concurrentBloomFilter.clear();
    }

    @Test
    public String
}
