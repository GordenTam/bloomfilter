package org.gorden.bloomfilter.core;

/**
 * @author: GordenTam
 * @create: 2019-12-17
 **/
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Collections of strategies of generating the k * log(M) bits required for an element to be mapped
 * to a BloomFilter of M bits and k hash functions. These strategies are part of the serialized form
 * of the Bloom filters that use them, thus they must be preserved as is (no updates allowed, only
 * introduction of new versions).
 *
 * <p>Important: the order of the constants cannot change, and they cannot be deleted - we depend on
 * their ordinal for BloomFilter serialization.
 *
 * @author Dimitris Andreou
 * @author Kurt Alfred Kluever
 */
enum BloomFilterStrategies implements BloomFilter.Strategy {
    /**
     * See "Less Hashing, Same Performance: Building a Better Bloom Filter" by Adam Kirsch and Michael
     * Mitzenmacher. The paper argues that this trick doesn't significantly deteriorate the
     * performance of a Bloom filter (yet only needs two 32bit hash functions).
     */
    MURMUR128_MITZ_32() {
        @Override
        public <T> boolean put(T object, int numHashFunctions, BitSet bits) {
            long bitSize = bits.bitSize();
            long hash64 = Hashing.murmur3_128().hashBytes(serialize(object)).asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);

            boolean bitsChanged = false;
            for (int i = 1; i <= numHashFunctions; i++) {
                int combinedHash = hash1 + (i * hash2);
                // Flip all the bits if it's negative (guaranteed positive number)
                if (combinedHash < 0) {
                    combinedHash = ~combinedHash;
                }
                bitsChanged |= bits.set(combinedHash % bitSize);
            }
            return bitsChanged;
        }

        @Override
        public <T> boolean mightContain(T object, int numHashFunctions, BitSet bits) {
            long bitSize = bits.bitSize();
            long hash64 = Hashing.murmur3_128().hashBytes(serialize(object)).asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);

            for (int i = 1; i <= numHashFunctions; i++) {
                int combinedHash = hash1 + (i * hash2);
                // Flip all the bits if it's negative (guaranteed positive number)
                if (combinedHash < 0) {
                    combinedHash = ~combinedHash;
                }
                if (!bits.get(combinedHash % bitSize)) {
                    return false;
                }
            }
            return true;
        }
    },
    /**
     * This strategy uses all 128 bits of {@link Hashing#murmur3_128} when hashing. It looks different
     * than the implementation in MURMUR128_MITZ_32 because we're avoiding the multiplication in the
     * loop and doing a (much simpler) += hash2. We're also changing the index to a positive number by
     * AND'ing with Long.MAX_VALUE instead of flipping the bits.
     */
    MURMUR128_MITZ_64() {
        @Override
        public <T> boolean put(T object, int numHashFunctions, BitSet bits) {
            long bitSize = bits.bitSize();
            //使用murmurHash求得128位byte数组
            byte[] bytes = Hashing.murmur3_128().hashBytes(serialize(object)).asBytes();
            //低64位
            long hash1 = lowerEight(bytes);
            //高64位
            long hash2 = upperEight(bytes);

            boolean bitsChanged = false;
            long combinedHash = hash1;

            //gi(x) = h1(x) + ih2(x) ，其中0<=i<=k-1； 使用两个函数模拟k个函数,这里h2(x)就是上面求得的hash2
            //也就是通过循环,每次累加hash2，然后& Long.MAX_VALUE将其转换为正数,对bitSize取模就得到索引位，将其set为1
            for (int i = 0; i < numHashFunctions; i++) {
                // Make the combined hash positive and indexable
                bitsChanged |= bits.set((combinedHash & Long.MAX_VALUE) % bitSize);
                combinedHash += hash2;
            }
            //返回修改了多少位
            return bitsChanged;
        }

        @Override
        public <T> boolean mightContain(T object, int numHashFunctions, BitSet bits) {
            long bitSize = bits.bitSize();
            byte[] bytes = Hashing.murmur3_128().hashBytes(serialize(object)).asBytes();
            long hash1 = lowerEight(bytes);
            long hash2 = upperEight(bytes);

            long combinedHash = hash1;
            for (int i = 0; i < numHashFunctions; i++) {
                // Make the combined hash positive and indexable
                if (!bits.get((combinedHash & Long.MAX_VALUE) % bitSize)) {
                    return false;
                }
                combinedHash += hash2;
            }
            return true;
        }

        private long lowerEight(byte[] bytes) {
            return Longs.fromBytes(
                    bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
        }

        private long upperEight(byte[] bytes) {
            return Longs.fromBytes(
                    bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
        }
    };

    /**
     * Serialize and object
     */
    static byte[] serialize(Object obj){
        Class clazz = obj.getClass();
        if(clazz.isPrimitive()){
            ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            if(obj instanceof Character){
                byteBuffer.putChar((Character) obj);
            } else if(obj instanceof Integer){
                byteBuffer.putInt((Integer) obj);
            } else if(obj instanceof Long){
                byteBuffer.putLong((Long) obj);
            } else if(obj instanceof Double){
                byteBuffer.putDouble((Double) obj);
            } else if(obj instanceof Float){
                byteBuffer.putFloat((Float) obj);
            } else if(obj instanceof Short){
                byteBuffer.putShort((Short) obj);
            } else if(obj instanceof Byte){
                byteBuffer.put((Byte) obj);
            }
            return byteBuffer.array();
        } else if(obj instanceof String){
            return ((String) obj).getBytes(Charset.forName("UTF-8"));
        } else{
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            serialize(obj, baos);
            return baos.toByteArray();
        }
    }

    static void serialize(Object obj, OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        } else {
            ObjectOutputStream out = null;

            try {
                out = new ObjectOutputStream(outputStream);
                out.writeObject(obj);
            } catch (IOException var11) {
                throw new SerializationException(var11);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException var10) {
                }

            }

        }
    }

}
