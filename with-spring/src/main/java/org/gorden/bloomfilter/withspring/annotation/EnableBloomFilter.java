package org.gorden.bloomfilter.withspring.annotation;

import org.gorden.bloomfilter.withspring.support.BloomFilterConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({BloomFilterConfigurationSelector.class})
public @interface EnableBloomFilter {

}
