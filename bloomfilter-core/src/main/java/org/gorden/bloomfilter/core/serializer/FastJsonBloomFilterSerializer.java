package org.gorden.bloomfilter.core.serializer;

import com.alibaba.fastjson.JSONObject;
import java.nio.charset.Charset;

/**
 * @author GordenTam
 **/

public class FastJsonBloomFilterSerializer<T> implements BloomFilterSerializer<T>{

    private static final byte[] EMPTYBYTE = new byte[0];

    private Class<T> type;

    public FastJsonBloomFilterSerializer(Class<T> type) {
        this.type = type;
    }

    public byte[] serialize(Object source) {
        if(source == null){
            return EMPTYBYTE;
        }
        try {
            String jsonString = JSONObject.toJSONString(source);
            return jsonString.getBytes(Charset.forName("UTF-8"));
        } catch (Throwable var) {
            throw new SerializationException("Failed to serialize object using fast json serializer", var);
        }
    }

    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            String str = new String(bytes, Charset.forName("UTF-8"));
            try {
                return JSONObject.parseObject(str, type);
            } catch (Throwable var) {
                throw new SerializationException("Cannot deserialize", var);
            }
        }
    }
}
