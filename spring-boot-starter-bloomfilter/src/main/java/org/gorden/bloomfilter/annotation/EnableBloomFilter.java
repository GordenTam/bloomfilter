package org.gorden.bloomfilter.annotation;

import org.gorden.bloomfilter.support.BloomFilterConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({BloomFilterConfigurationSelector.class})
public @interface EnableBloomFilter {

}
