package org.gorden.bloomfilter.support;

import org.gorden.bloomfilter.examples.*;
import org.gorden.bloomfilter.examples.concurrent.AbstractBloomFilterManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RedisBloomFilterManager extends AbstractBloomFilterManager {
    private final RedisOperator redisOperator;
    private final BloomFilterConfiguration defaultConfiguration;
    private final Map<String, BloomFilterConfiguration> initialConfiguration = new ConcurrentHashMap<>(16);
    private final boolean allowCreatedWhenMissing;

    private RedisBloomFilterManager(RedisOperator redisOperator, BloomFilterConfiguration defaultConfiguration, boolean allowCreatedWhenMissing) {
        this.redisOperator = redisOperator;
        this.defaultConfiguration = defaultConfiguration;
        this.allowCreatedWhenMissing = allowCreatedWhenMissing;
        initBloomFilterMap();
    }

    public RedisBloomFilterManager(RedisOperator redisOperator, BloomFilterConfiguration defaultConfiguration) {
        this(redisOperator, defaultConfiguration, true);
    }

    public RedisBloomFilterManager(RedisOperator redisOperator, BloomFilterConfiguration defaultConfiguration, String... initialNames) {
        this(redisOperator, defaultConfiguration, true);
        String[] names = initialNames;
        int length = initialNames.length;

        for (int i = 0; i < length; ++i) {
            String cacheName = names[i];
            this.initialConfiguration.put(cacheName, defaultConfiguration);
        }
    }

    public RedisBloomFilterManager(RedisOperator redisOperator, BloomFilterConfiguration defaultConfiguration, Map<String, BloomFilterConfiguration> initialConfigurations) {
        this(redisOperator, defaultConfiguration, initialConfigurations, true);
    }

    public RedisBloomFilterManager(RedisOperator redisOperator, BloomFilterConfiguration defaultConfiguration, Map<String, BloomFilterConfiguration> initialConfigurations, boolean allowCreatedWhenMissing) {
        this(redisOperator, defaultConfiguration, allowCreatedWhenMissing);
        this.initialConfiguration.putAll(initialConfigurations);
    }

    public static RedisBloomFilterManager create(RedisConnectionFactory connectionFactory) {
        return new RedisBloomFilterManager(new SpringDataRedisOperator(connectionFactory), BloomFilterConfiguration.defaultConfig(), true);
    }

    public static RedisBloomFilterManager create(RedisConnectionFactory connectionFactory, Map<String, BloomFilterConfiguration> initialConfigurations) {
        return new RedisBloomFilterManager(new SpringDataRedisOperator(connectionFactory), BloomFilterConfiguration.defaultConfig(), initialConfigurations, true);
    }

    private RedisBloomFilter createBloomFilter(String name, RedisOperator redisOperator, BloomFilterConfiguration bloomFilterConfiguration) {
        return RedisBloomFilter.create(name, bloomFilterConfiguration.getExpectedInsertions(), bloomFilterConfiguration.getFpp(), redisOperator, bloomFilterConfiguration.getBloomFilterSerializer(), bloomFilterConfiguration.getHashFunction());
    }

    @Override
    protected void initBloomFilterMap() {
        Map<String, BloomFilter> bloomFilterMap = new HashMap<>();
        Set<String> names = this.initialConfiguration.keySet();
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String name = it.next();
            BloomFilterConfiguration configuration = this.initialConfiguration.get(name);
            RedisBloomFilter bloomFilter = createBloomFilter(name, this.redisOperator, configuration);
            bloomFilterMap.put(name, bloomFilter);
        }
        this.bloomFilterMap.putAll(bloomFilterMap);
    }

    protected BloomFilter getMissingBloomFilter(String name) {
        return this.allowCreatedWhenMissing ? this.createBloomFilter(name, this.redisOperator, this.defaultConfiguration) : null;
    }

}
