package org.gorden.bloomfilter.core.aspect;

import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
import org.gorden.bloomfilter.aspect.annotation.BFPut;
import org.gorden.bloomfilter.common.BloomFilter;
import org.gorden.bloomfilter.core.concurrent.ConcurrentBloomFilter;
import org.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import org.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author GordenTam
 **/
public class BasicBloomFilterTest {

    private static UserService userService;
    private static BloomFilter bloomFilter;

    @BeforeClass
    public static void setUp() {
        userService = new UserService();
        bloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
    }

    @Test
    public void testBloomFilterPut() {
        userService.createUser("1");
        assertTrue(bloomFilter.mightContain("1"));
        assertFalse(bloomFilter.mightContain("2"));
    }

    @Test
    public void testBloomFilterMightContain() {
        assertEquals("1", userService.getUser("1"));
        assertEquals("user:2 not exists", userService.getUser("2"));
    }

    public static class UserService {

        @BFPut(value = "test")
        public String createUser(String id) {
            System.out.println("user:" + id + " has been created");
            return id;
        }

        @BFMightContain(value = "test", fallback = "BFFallback")
        public String getUser(String id) {
            return id;
        }

        public String BFFallback(String id) {
            System.out.println("user:" + id + " not exists");
            return "user:" + id + " not exists";
        }
    }
}
