package org.gorden.bloomfilter;

import org.gorden.bloomfilter.core.hash.HashFunction;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/
@Configuration
@AutoConfigureAfter(BloomFilterAutoConfiguration.class)
@ConditionalOnProperty(value = "bloom-filter.type", havingValue = "JDK")
public class JDKBloomFilterConfiguration {

    @Bean
    @Order(2147483647)
    @ConditionalOnMissingBean(BloomFilterGenerator.class)
    public BloomFilterGenerator BloomFilterGenerator(BloomFilterProperties bloomFilterProperties, HashFunction hashFunction, BloomFilterSerializer bloomFilterSerializer) {
        return new JdkBloomFilterGenerator(bloomFilterProperties, hashFunction, bloomFilterSerializer);
    }

}
