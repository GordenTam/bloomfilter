package org.gorden.bloomfilter.examples;

import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
import org.gorden.bloomfilter.aspect.annotation.BFPut;
import org.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
import org.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import org.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
/**
 * @author GordenTam
 **/
public class BloomFilterAspectExample {

    @BFPut(value = "test")
    public String bfPut(String str) {
        System.out.println("bloomfilter put string" + str);
        return str;
    }

    @BFMightContain(value = "test", fallback = "BFFallback")
    public String mightContain(String str) {
        return str;
    }

    public String BFFallback(String str) {
        System.out.println("str");
        return str + "not in bloom filter";
    }

    public static class UnitTest {

        @Test
        public void testAspect() {
            ConcurrentBloomFilter<String> concurrentBloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
            BloomFilterAspectExample bloomFilterAspectExample = new BloomFilterAspectExample();
            bloomFilterAspectExample.bfPut("apple");
            String result = bloomFilterAspectExample.mightContain("banana");
            assertEquals("banana not in bloom filter", result);
        }
    }
}
