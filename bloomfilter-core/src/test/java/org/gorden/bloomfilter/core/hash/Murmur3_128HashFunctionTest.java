package org.gorden.bloomfilter.core.hash;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Murmur3_128HashFunctionTest {

    @Test
    public void hashTest() {
        final byte[] byteArray1 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        final byte[] byteArray2 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8};
        final byte[] byteArray3 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final byte[] expectedResult1 = new byte[]{-36, -65, -109, -17, -91, 12, -24, -100, 7, -84, 85, -74, -26, -27, 103, -59};
        final byte[] expectedResult2 = new byte[]{95, 125, 50, 64, 50, 58, 67, -116, -88, 107, -63, -72, 24, 10, -47, 38};
        final byte[] expectedResult3 = new byte[]{-72, -100, 71, -66, 93, 70, -127, -110, 46, -92, -117, -107, 9, 96, -67, -16};
        Murmur3_128HashFunction murmur3_128Hasher = new Murmur3_128HashFunction(0);

        byte[] actualResult1 = murmur3_128Hasher.hashBytes(byteArray1);
        byte[] actualResult2 = murmur3_128Hasher.hashBytes(byteArray2);
        byte[] actualResult3 = murmur3_128Hasher.hashBytes(byteArray3);

        assertEquals(16, actualResult1.length);
        assertEquals(16, actualResult2.length);
        assertEquals(16, actualResult3.length);

        assertArrayEquals(expectedResult1, actualResult1);
        assertArrayEquals(expectedResult2, actualResult2);
        assertArrayEquals(expectedResult3, actualResult3);

    }
}
