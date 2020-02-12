package org.gorden.bloomfilter.aspect.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
import org.gorden.bloomfilter.aspect.annotation.BFPut;
import org.gorden.bloomfilter.examples.BloomFilter;
import org.gorden.bloomfilter.examples.observer.BloomFilterObserver;

import java.lang.reflect.Method;

/**
 * @author GordenTam
 **/
@Aspect
public class BloomFilterAspect {

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFPut)")
    public void pointcut1() {
    }

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFMightContain)")
    public void pointcut2() {
    }

    @Around("pointcut1() && @annotation(bfput)")
    public Object methodsAnnotatedWithBloomFilter(ProceedingJoinPoint joinPoint, BFPut bfput) throws Throwable {
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("the annotation can only be used in methods");
        }
        //MethodSignature methodSignature = (MethodSignature)signature;
        String name = bfput.value();
        Object returnObject = joinPoint.proceed();
        BloomFilter bf = BloomFilterObserver.getBloomFilter(name);
        bf.put(returnObject);
        return returnObject;
    }

    @Around("pointcut2() && @annotation(bfMightContain)")
    public Object methodsAnnotatedWithBloomFilter(ProceedingJoinPoint joinPoint, BFMightContain bfMightContain) throws Throwable {
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("the annotation can only be used in methods");
        }
        MethodSignature methodSignature = (MethodSignature)signature;
        String name = bfMightContain.value();
        Object returnObject = joinPoint.proceed();
        BloomFilter bf = BloomFilterObserver.getBloomFilter(name);
        if (!bf.mightContain(returnObject)) {
            return joinPoint.proceed();
        }
        Object target = joinPoint.getTarget();
        Method fallback = target.getClass().getMethod(bfMightContain.fallback(), methodSignature.getParameterTypes());
        return fallback.invoke(target, joinPoint.getArgs());
    }

}
