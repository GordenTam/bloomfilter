package org.gorden.bloomfilter;

import org.gorden.bloomfilter.support.SpringDataRedisOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/

@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisBloomFilterConfiguration {

    @Bean
    @ConditionalOnMissingBean(SpringDataRedisOperator.class)
    public SpringDataRedisOperator springDataRedisOperator(RedisConnectionFactory redisConnectionFactory) {
        return new SpringDataRedisOperator(redisConnectionFactory);
    }


}
