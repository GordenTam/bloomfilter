package org.gorden.bloomfilter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author GordenTam
 **/
@Configuration
@EnableConfigurationProperties(value = BloomFilterProperties.class)
public class BloomFilterAutoConfiguration {

    private BloomFilterProperties bloomFilterProperties;

}
