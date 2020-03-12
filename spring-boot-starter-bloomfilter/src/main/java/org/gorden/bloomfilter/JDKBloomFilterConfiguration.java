package org.gorden.bloomfilter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author GordenTam
 * @since 2020-03-12
 **/
@Configuration
@ConditionalOnMissingBean()
public class JDKBloomFilterConfiguration {
}
