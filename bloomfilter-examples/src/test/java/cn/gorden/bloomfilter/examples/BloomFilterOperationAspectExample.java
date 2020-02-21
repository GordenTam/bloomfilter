package cn.gorden.bloomfilter.examples;

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
public class BloomFilterOperationAspectExample {

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
        return str + " not in bloom filter";
    }

    @Test
    public void testAspect() {
        ConcurrentBloomFilter concurrentBloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
        BloomFilterOperationAspectExample bloomFilterAspectExample = new BloomFilterOperationAspectExample();
        bloomFilterAspectExample.bfPut("apple");
        String result = bloomFilterAspectExample.mightContain("banana");
        assertEquals("banana not in bloom filter", result);
    }

}
