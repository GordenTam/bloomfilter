package org.gorden.bloomfilter;

import org.gorden.bloomfilter.core.RedisOperator;
import org.gorden.bloomfilter.core.hash.HashFunction;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import org.gorden.bloomfilter.support.SpringDataRedisOperator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/

@Configuration
@AutoConfigureAfter(BloomFilterAutoConfiguration.class)
@ConditionalOnProperty(value = "bloom-filter.type", havingValue = "JDK")
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisBloomFilterConfiguration {

    @Bean
    @ConditionalOnMissingBean(SpringDataRedisOperator.class)
    public SpringDataRedisOperator springDataRedisOperator(RedisConnectionFactory redisConnectionFactory) {
        return new SpringDataRedisOperator(redisConnectionFactory);
    }

    @Bean
    @Order(2147483647)
    @ConditionalOnMissingBean(BloomFilterGenerator.class)
    public BloomFilterGenerator BloomFilterGenerator(BloomFilterProperties bloomFilterProperties, RedisOperator redisOperator, HashFunction hashFunction, BloomFilterSerializer bloomFilterSerializer) {
        return new RedisBloomFilterGenerator(bloomFilterProperties, redisOperator, hashFunction, bloomFilterSerializer);
    }
}
