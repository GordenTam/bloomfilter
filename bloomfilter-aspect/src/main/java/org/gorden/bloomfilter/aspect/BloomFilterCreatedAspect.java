package org.gorden.bloomfilter.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author GordenTam
 **/
@Aspect
public class BloomFilterCreatedAspect {

    @Pointcut
    public void BloomFilterCreated() {

    }
}
