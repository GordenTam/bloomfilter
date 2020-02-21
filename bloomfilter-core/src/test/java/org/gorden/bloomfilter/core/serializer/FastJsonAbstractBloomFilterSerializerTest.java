package org.gorden.bloomfilter.core.serializer;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class FastJsonAbstractBloomFilterSerializerTest {

    @Test
    public void FastJsonBloomFilterSerializerSerializeTest() {
        byte[] expectedBytes = new byte[]{34, 116, 101, 115, 116, 34};
        BloomFilterSerializer fastJsonBloomFilterSerializer = new FastJsonBloomFilterSerializer();
        byte[] bytes = fastJsonBloomFilterSerializer.serialize("test");
        assertArrayEquals(expectedBytes, bytes);
    }
}
