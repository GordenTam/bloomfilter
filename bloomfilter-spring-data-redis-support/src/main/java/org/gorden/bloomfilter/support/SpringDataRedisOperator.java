package org.gorden.bloomfilter.support;

import org.gorden.bloomfilter.core.RedisOperator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.function.Function;

public class SpringDataRedisOperator implements RedisOperator {

    private RedisConnectionFactory connectionFactory;

    public SpringDataRedisOperator(RedisConnectionFactory redisConnectionFactory) {
        this.connectionFactory = redisConnectionFactory;
    }

    public void setBit(byte[] RedisRawKey, long offset) {
        this.execute((connection) -> {
            connection.setBit(RedisRawKey, offset ,false);
            return "OK";
        });
    }

    public boolean getBit(byte[] key, long offset) {

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
