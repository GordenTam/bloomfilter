package org.gorden.bloomfilter.core;

public interface RedisOperator {

    void setBit(byte[] key, long offset);

    boolean getBit(byte[] key, long offset);

    long bitCount(byte[] key);

    void del(byte[] key);

}
