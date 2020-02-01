package org.gorden.bloomfilter.core.serializer;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class JdkSerializationAbstractBloomFilterSerializerTest {

    @Test
    public void JdkSerializationBloomFilterSerializerSerializeTest() {
        byte[] expectedBytes = new byte[]{-84, -19, 0, 5, 116, 0, 4, 116, 101, 115, 116};
        JdkSerializationBloomFilterSerializer jdkSerializationBloomFilterSerializer = new JdkSerializationBloomFilterSerializer();
        byte[] bytes = jdkSerializationBloomFilterSerializer.serialize("test");
        assertArrayEquals(expectedBytes, bytes);
    }

}
