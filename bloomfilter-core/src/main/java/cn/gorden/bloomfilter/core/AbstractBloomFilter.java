package cn.gorden.bloomfilter.core;

import cn.gorden.bloomfilter.core.bitset.BitSet;
import cn.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import cn.gorden.bloomfilter.core.hash.HashFunction;
import cn.gorden.bloomfilter.core.hash.Longs;
import cn.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import cn.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;

/**
 * A Bloom filter implements refer to guava. Support optional bitSet, hashFunction and serializer implements.
 *
 * @author GordenTam
 */
public abstract class AbstractBloomFilter implements BloomFilter {

    private String name;
    private int numHashFunctions;
    private BloomFilterSerializer bloomFilterSerializer;
    private HashFunction hashFunction;
    private BitSet bitSet;

    private AbstractBloomFilter() {
    }

    protected AbstractBloomFilter(String name, int numHashFunctions, BitSet bitSet) {
        this(name, numHashFunctions, bitSet, null, null);
    }

    protected AbstractBloomFilter(String name, int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer) {
        this(name, numHashFunctions, bitSet, bloomFilterSerializer, null);
    }

    protected AbstractBloomFilter(String name, int numHashFunctions, BitSet bitSet, HashFunction hashFunction) {
        this(name, numHashFunctions, bitSet, null, hashFunction);
    }

    protected AbstractBloomFilter(String name, int numHashFunctions, BitSet bitSet, BloomFilterSerializer bloomFilterSerializer, HashFunction hashFunction) {
        if(name == null || name.trim().equals("")) {
            throw new IllegalArgumentException("bloomfilter name can not be null");
        }
        if(bitSet == null) {
            throw new IllegalArgumentException("bitset must not be null");
        }
        if(numHashFunctions <= 0) {
            throw new IllegalArgumentException(("numHashFunctions must greater than zero"));
        }
        if(bloomFilterSerializer == null) {
            bloomFilterSerializer = new JdkSerializationBloomFilterSerializer();
        }
        if(hashFunction == null) {
            hashFunction = new Murmur3_128HashFunction(0);
        }
        this.name = name;
        this.numHashFunctions = numHashFunctions;
        this.bitSet = bitSet;
        this.bloomFilterSerializer = bloomFilterSerializer;
        this.hashFunction = hashFunction;
    }

    public String getName() {
        return this.name;
    }

    public boolean mightContain(Object object) {
        long bitSize = bitSet.bitSize();
        byte[] bytes = hashFunction.hashBytes(raw(object));
        long hash1 = lowerEight(bytes);
        long hash2 = upperEight(bytes);

        long combinedHash = hash1;
        for (int i = 0; i < numHashFunctions; i++) {
            if (!bitSet.get((combinedHash & Long.MAX_VALUE) % bitSize)) {
                return false;
            }
            combinedHash += hash2;
        }
        return true;
    }

    public boolean put(Object object) {
        long bitSize = bitSet.bitSize();
        //使用murmurHash求得128位byte数组
        byte[] bytes = hashFunction.hashBytes(raw(object));
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
            bitsChanged |= bitSet.set((combinedHash & Long.MAX_VALUE) % bitSize);
            combinedHash += hash2;
        }
        //返回是否修改成功
        return bitsChanged;
    }

    public double expectedFpp() {
        return Math.pow((double) bitSet.bitCount() / bitSize(), numHashFunctions);
    }

    public long approximateElementCount() {
        long bitSize = bitSet.bitSize();
        long bitCount = bitSet.bitCount();

        double fractionOfBitsSet = (double) bitCount / bitSize;
        return Longs.doubleToLong(-Math.log1p(-fractionOfBitsSet) * bitSize / numHashFunctions);
    }

    private long bitSize() {
        return bitSet.bitSize();
    }

    protected static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    protected static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    private byte[] raw(Object element) {
        return bloomFilterSerializer.serialize(element);
    }

    private long lowerEight(byte[] bytes) {
        return Longs.fromBytes(bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    }

    private long upperEight(byte[] bytes) {
        return Longs.fromBytes(bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
    }

    public void clear() {
        this.bitSet.clear();
    }
}
