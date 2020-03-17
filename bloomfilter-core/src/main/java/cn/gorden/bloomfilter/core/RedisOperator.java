package cn.gorden.bloomfilter.core;

public interface RedisOperator {

    void setBit(byte[] key, long offset, boolean value);

    boolean getBit(byte[] key, long offset);

    long bitCount(byte[] key);

    void del(byte[] key);

}
