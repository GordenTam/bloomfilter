package org.gorden.bloomfilter.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.gorden.bloomfilter.aspect.observer.BloomFilterObserver;
import org.gorden.bloomfilter.core.BloomFilter;

import java.lang.reflect.Constructor;

/**
 * @author GordenTam
 **/
@Aspect
public class BloomFilterCreatedAspect {

    @Pointcut("execution(org.gorden.bloomfilter.core.AbstractBloomFilter+.new(..))")
    public void bloomFilterConstructorPointCut() { }

    @After("bloomFilterConstructorPointCut()")
    public void bloomFilterConstructorExecuted(JoinPoint joinPoint) {
        System.out.println("joinpoint" + joinPoint);
        ConstructorSignature signature = (ConstructorSignature) joinPoint.getSignature();
        System.out.println("before->" + joinPoint.getThis().toString() + "#" + joinPoint.getSignature().getName());
//        System.out.println("aspect" + name);
//        BloomFilterObserver.registered(name, bloomFilter);
    }
}
