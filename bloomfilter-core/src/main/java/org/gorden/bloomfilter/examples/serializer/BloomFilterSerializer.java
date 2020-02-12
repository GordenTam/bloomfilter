package org.gorden.bloomfilter.examples.serializer;

/**
 * @author: GordenTam
 * @create: 2020-01-10
 **/

public interface BloomFilterSerializer<T> {

    byte[] serialize(T var1) throws SerializationException;

    T deserialize(byte[] var1) throws SerializationException;
}
