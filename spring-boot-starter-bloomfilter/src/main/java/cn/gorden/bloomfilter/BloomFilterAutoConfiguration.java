package cn.gorden.bloomfilter;

import cn.gorden.bloomfilter.core.hash.HashFunction;
import cn.gorden.bloomfilter.core.hash.Murmur3_128HashFunction;
import cn.gorden.bloomfilter.core.serializer.BloomFilterSerializer;
import cn.gorden.bloomfilter.core.serializer.JdkSerializationBloomFilterSerializer;
import cn.gorden.bloomfilter.aspect.BloomFilterOperationAspect;
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
    public BloomFilterOperationAspect bloomFilterOperationAspect() {
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
