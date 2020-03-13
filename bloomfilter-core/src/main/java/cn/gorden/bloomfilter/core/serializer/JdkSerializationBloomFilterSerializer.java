package cn.gorden.bloomfilter.core.serializer;

import java.io.*;

/**
 * @author GordenTam
 **/

public class JdkSerializationBloomFilterSerializer implements BloomFilterSerializer{

    private static final byte[] EMPTYBYTE = new byte[0];

    public byte[] serialize(Object source) {
        if (source == null) {
            return EMPTYBYTE;
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
        try {
            if (!(source instanceof Serializable)) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + " requires a Serializable payload but received an object of type [" + source.getClass().getName() + "]");
            } else {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
                objectOutputStream.writeObject(source);
                objectOutputStream.flush();
            }
            return byteStream.toByteArray();
        } catch (Throwable var) {
            throw new SerializationException("Failed to serialize object using jdk serialization serializer", var);
        }
    }
}
