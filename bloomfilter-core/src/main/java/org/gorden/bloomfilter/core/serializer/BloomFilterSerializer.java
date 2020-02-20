package org.gorden.bloomfilter.core.serializer;

/**
 * @author GordenTam
 **/

public interface BloomFilterSerializer {

    byte[] serialize(Object var1) throws SerializationException;

}
