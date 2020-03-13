package org.gorden.bloomfilter;

import org.gorden.bloomfilter.aspect.BloomFilterOperationAspect;
import org.gorden.bloomfilter.core.hash.HashFunction;
import org.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import org.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import org.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnMissingBean(BloomFilterSerializer.class)
    public BloomFilterSerializer bloomFilterSerializer() {
        return new JdkSerializationBloomFilterSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(HashFunction.class)
    public HashFunction hashFunction() {
        return new Murmur3_128HashFunction(0);
    }

}
