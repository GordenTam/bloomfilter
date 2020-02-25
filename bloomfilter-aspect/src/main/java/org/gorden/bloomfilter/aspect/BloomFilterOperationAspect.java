package org.gorden.bloomfilter.aspect;

import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gorden.bloomfilter.aspect.exception.FallbackDefinitionException;
import org.gorden.bloomfilter.common.BloomFilter;
import org.gorden.bloomfilter.aspect.observer.BloomFilterObserver;

import java.lang.reflect.Method;

/**
 * @author GordenTam
 **/
@Aspect
public class BloomFilterOperationAspect {

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFPut)")
    public void pointcut1() {
    }

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFMightContain)")
    public void pointcut2() {
    }

    @Around("pointcut1()")
    public Object methodsAnnotatedWithBFPut(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalStateException("the annotation can only be used in methods");
        }
        MethodSignature methodSignature = (MethodSignature)signature;
        Method method = methodSignature.getMethod();
        org.gorden.bloomfilter.aspect.annotation.BFPut bfput = method.getAnnotation(org.gorden.bloomfilter.aspect.annotation.BFPut.class);
        String name = bfput.value();
        if(name == null || name.trim().equals("")) {
            throw new IllegalStateException("the BloomFilter name can not be null");
        }
        Object returnObject = joinPoint.proceed();
        BloomFilter bf = BloomFilterObserver.getBloomFilter(name);
        if(bf == null) {
            throw new IllegalStateException("the BloomFilter with name: " + name + " has not been created");
        }
        bf.put(returnObject);
        return returnObject;
    }

    @Around("pointcut2()")
    public Object methodsAnnotatedWithBFMightContain(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalStateException("the annotation can only be used in methods");
        }
        MethodSignature methodSignature = (MethodSignature)signature;
        Method method = methodSignature.getMethod();
        org.gorden.bloomfilter.aspect.annotation.BFMightContain bfMightContain = method.getAnnotation(org.gorden.bloomfilter.aspect.annotation.BFMightContain.class);
        String name = bfMightContain.value();
        if (name == null || name.trim().equals("")) {
            throw new IllegalStateException("the BloomFilter name can not be null");
        }
        Object returnObject = joinPoint.proceed();
        BloomFilter bf = BloomFilterObserver.getBloomFilter(name);
        if (bf == null) {
            throw new IllegalStateException("the BloomFilter with name: " + name + " has not been created");
        }
        if(bf.mightContain(returnObject)) {
            return returnObject;
        }
        //if the element not in bloomfilter, invocation the fallback method
        else {
            Object target = joinPoint.getTarget();
            Method fallback = target.getClass().getMethod(bfMightContain.fallback(), methodSignature.getParameterTypes());
            if(fallback == null) {
                throw new FallbackDefinitionException();
            }
            try {
                fallback.invoke(target, joinPoint.getArgs());
            } catch (Throwable e) {
                throw new FallbackDefinitionException();
            }
        }

        else {
            Object target = joinPoint.getTarget();
        }
    }

}
