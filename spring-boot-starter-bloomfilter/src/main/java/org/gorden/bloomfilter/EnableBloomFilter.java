package org.gorden.bloomfilter;

import org.gorden.bloomfilter.support.BloomFilterImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@Import(BloomFilterImportSelector.class)
public @interface EnableBloomFilter {

}

