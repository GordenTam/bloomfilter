package cn.gorden.bloomfilter.core.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.function.Function;

/**
 * @author GordenTam
 * @since 2020-03-17
 **/
public class SimpleJedisOperator implements RedisOperator {

    private static final JedisPoolConfig poolConfig = new JedisPoolConfig();

    private JedisPool jedisPool;

    private volatile static SimpleJedisOperator simpleJedisOperator = null;

    public static SimpleJedisOperator getInstance(JedisPool jedisPool) {
        if (simpleJedisOperator == null) {
            synchronized (SimpleJedisOperator.class) {
                if (simpleJedisOperator == null) {
                    simpleJedisOperator = new SimpleJedisOperator(jedisPool);
                }
            }
        }
        return simpleJedisOperator;
    }

    public static SimpleJedisOperator getInstance(String host, int port) {
        if (simpleJedisOperator == null) {
            synchronized (SimpleJedisOperator.class) {
                if (simpleJedisOperator == null) {
                    simpleJedisOperator = new SimpleJedisOperator(new JedisPool(poolConfig, host, port));
                }
            }
        }
        return simpleJedisOperator;
    }

    public static SimpleJedisOperator getInstance() {
        if (simpleJedisOperator == null) {
            synchronized (SimpleJedisOperator.class) {
                if (simpleJedisOperator == null) {
                    simpleJedisOperator = new SimpleJedisOperator(new JedisPool(poolConfig, "localhost", 6379));
                }
            }
        }
        return simpleJedisOperator;
    }

    private SimpleJedisOperator(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void setBit(byte[] rawKey, long offset, boolean value) {
        this.execute((jedis) -> {
            jedis.setbit(rawKey, offset, value);
            return "OK";
        });
    }

    public boolean getBit(byte[] rawKey, long offset) {
        return this.execute((jedis) -> {
            return jedis.getbit(rawKey, offset);
        });
    }

    public long bitCount(byte[] rawKey) {
        return this.execute((jedis) -> {
            return jedis.bitcount(rawKey);
        });
    }

    public void del(byte[] rawKey) {
        this.execute((jedis) -> {
            return jedis.del(rawKey);
        });
    }

    private <T> T execute(Function<Jedis, T> function) {
        Jedis jedis = null;
        T var;
        try {
            jedis = jedisPool.getResource();
            var = function.apply(jedis);
        } finally {
            if(jedis != null) {
                jedis.close();
            }
        }
        return var;
    }
}
