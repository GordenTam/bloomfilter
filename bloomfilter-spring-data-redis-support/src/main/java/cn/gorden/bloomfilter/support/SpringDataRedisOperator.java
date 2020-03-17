package cn.gorden.bloomfilter.support;

import cn.gorden.bloomfilter.core.RedisOperator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.util.function.Function;

/**
 * redis operator using spring data redis, refer to DefaultRedisCacheWriter.java
 * @author GordenTam
 * @since 1.0
 **/
public class SpringDataRedisOperator implements RedisOperator {

    private RedisConnectionFactory connectionFactory;

    public SpringDataRedisOperator(RedisConnectionFactory redisConnectionFactory) {
        this.connectionFactory = redisConnectionFactory;
    }

    public void setBit(byte[] rawKey, long offset) {
        this.setBit(rawKey, offset, true);
    }

    public void setBit(byte[] rawKey, long offset, boolean value) {
        this.execute((connection) -> {
            connection.setBit(rawKey, offset, value);
            return "OK";
        });
    }

    public boolean getBit(byte[] rawKey, long offset) {
        return this.execute((connection) -> {
            return connection.getBit(rawKey, offset);
        });
    }

    public long bitCount(byte[] rawKey) {
        return this.execute((connection) -> {
            return connection.bitCount(rawKey);
        });
    }

    public void del(byte[] rawKey) {
        this.execute((connection) -> {
            return connection.del(rawKey);
        });
    }

    private <T> T execute(Function<RedisConnection, T> callback) {
        RedisConnection connection = this.connectionFactory.getConnection();

        T var4;
        try {
            var4 = callback.apply(connection);
        } finally {
            connection.close();
        }

        return var4;
    }
}
