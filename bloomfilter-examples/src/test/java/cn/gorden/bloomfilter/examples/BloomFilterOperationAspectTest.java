package cn.gorden.bloomfilter.examples;

import cn.gorden.bloomfilter.core.BloomFilter;
import cn.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
import cn.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import cn.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author GordenTam
 **/
public class BloomFilterOperationAspectTest {

    @Test
    public void testAspect() {
        BloomFilter concurrentBloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
        UserService userService = new UserService();
        userService.createUser("Tom");
        String result = userService.getUser("Jerry");
        assertEquals("user with id Jerry not in bloom filter", result);
    }

}
