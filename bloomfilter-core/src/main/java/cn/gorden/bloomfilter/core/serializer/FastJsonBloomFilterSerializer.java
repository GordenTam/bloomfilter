package cn.gorden.bloomfilter.core.serializer;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.Charset;

/**
 * @author GordenTam
 **/

public class FastJsonBloomFilterSerializer implements BloomFilterSerializer {

    private static final byte[] EMPTYBYTE = new byte[0];

    public byte[] serialize(Object source) {
        if (source == null) {
            return EMPTYBYTE;
        }
        try {
            String jsonString = JSONObject.toJSONString(source);
            return jsonString.getBytes(Charset.forName("UTF-8"));
        } catch (Throwable var) {
            throw new SerializationException("Failed to serialize object using fast json serializer", var);
        }
    }
}
