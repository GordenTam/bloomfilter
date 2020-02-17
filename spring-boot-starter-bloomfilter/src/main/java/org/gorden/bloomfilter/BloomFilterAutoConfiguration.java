package org.gorden.bloomfilter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author GordenTam
 **/

@EnableConfigurationProperties(value = BloomFilterProperties.class)
public class BloomFilterAutoConfiguration {

    private BloomFilterProperties bloomFilterProperties;

}
