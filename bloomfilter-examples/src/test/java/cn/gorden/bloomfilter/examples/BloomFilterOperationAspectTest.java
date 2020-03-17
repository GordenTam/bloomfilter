//package cn.gorden.bloomfilter.examples;
//
//import cn.gorden.bloomfilter.aspect.annotation.BFMightContain;
//import cn.gorden.bloomfilter.aspect.annotation.BFPut;
//import cn.gorden.bloomfilter.core.BloomFilter;
//import cn.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
//import cn.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
//import cn.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;
//import org.junit.Test;
//import static org.junit.Assert.assertEquals;
//
///**
// * @author GordenTam
// **/
//public class BloomFilterOperationAspectTest {
//
//    @BFPut(value = "test")
//    public String createUser(String userId) {
//        System.out.println("user with id " + userId + " has been created");
//        return userId;
//    }
//
//    @BFMightContain(value = "test", fallback = "BFFallback")
//    public String getUser(String userId) {
//        return userId;
//    }
//
//    public String BFFallback(String userId) {
//        System.out.println(userId);
//        return "user with id " + userId + " not in bloom filter";
//    }
//
//    @Test
//    public void testAspect() {
//        BloomFilter concurrentBloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
//        BloomFilterOperationAspectExample bloomFilterAspectExample = new BloomFilterOperationAspectExample();
//        bloomFilterAspectExample.createUser("Tom");
//        String result = bloomFilterAspectExample.getUser("Jerry");
//        assertEquals("user with Jerry not in bloom filter", result);
//    }
//
//}
