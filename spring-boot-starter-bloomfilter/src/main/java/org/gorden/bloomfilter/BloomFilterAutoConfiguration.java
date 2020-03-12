package org.gorden.bloomfilter;

import org.gorden.bloomfilter.aspect.BloomFilterOperationAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author GordenTam
 **/
@Configuration
@EnableConfigurationProperties(value = BloomFilterProperties.class)
public class BloomFilterAutoConfiguration {

    @Bean
    public BloomFilterOperationAspect bloomFilterAspect() {
        return new BloomFilterOperationAspect();
    }

    @Bean
    @Order(2147483647)
    @ConditionalOnMissingBean(BloomFilterGenerator.class)
    public BloomFilterGenerator BloomFilterGenerator(BloomFilterProperties bloomFilterProperties) {
        bloomFilterProperties();
        return new DefaultBloomFilterGenerator(bloomFilterProperties);
    }

}
