package org.gorden.bloomfilter.aspect;

import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
import org.gorden.bloomfilter.aspect.exception.FallbackDefinitionException;
import org.gorden.bloomfilter.aspect.exception.FallbackInvocationException;
import org.gorden.bloomfilter.aspect.util.FallbackMethodFinder;
import org.gorden.bloomfilter.common.BloomFilter;
import org.gorden.bloomfilter.aspect.observer.BloomFilterObserver;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

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
            throw new IllegalStateException("the BloomFilter's name can not be null");
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
        Method methodAnnotatedWithBFMightContain = methodSignature.getMethod();
        org.gorden.bloomfilter.aspect.annotation.BFMightContain bfMightContain = methodAnnotatedWithBFMightContain.getAnnotation(org.gorden.bloomfilter.aspect.annotation.BFMightContain.class);
        String name = bfMightContain.value();
        if (name == null || name.trim().equals("")) {
            throw new IllegalStateException("the BloomFilter's name can not be null");
        }
        Object returnObject = joinPoint.proceed();
        BloomFilter bf = BloomFilterObserver.getBloomFilter(name);
        if (bf == null) {
            throw new IllegalStateException("the BloomFilter with name: " + name + " has not been created");
        }
        if (bf.mightContain(returnObject)) {
            return returnObject;
        }
        //if the element not in bloomFilter, invoke the fallback method
        else {
            Object target = joinPoint.getTarget();
            return invokeFallbackMethod(target, methodAnnotatedWithBFMightContain);
        }
    }

    private Object invokeFallbackMethod(Object target, Method methodAnnotatedWithBFMightContain) {
        Method method = getFallbackMethod(target.getClass(), methodAnnotatedWithBFMightContain);
        String name = getFallbackName(methodAnnotatedWithBFMightContain);
        Class<?>[] fallbackParameterTypes = methodAnnotatedWithBFMightContain.getParameterTypes();
        try {
            method.invoke(target, fallbackParameterTypes);
        } catch (Exception e) {

        }
    }

    public static Optional<Method> getMethod(Class<?> type, String name, Class<?> methodAnnotatedWithBFMightContainReturnType, Class<?>... parameterTypes) {
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes) && method.getReturnType().isAssignableFrom(methodAnnotatedWithBFMightContainReturnType)) {
                return Optional.of(method);
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            return getMethod(superClass, name, methodAnnotatedWithBFMightContainReturnType, parameterTypes);
        } else {
            return Optional.empty();
        }
    }

    public String getFallbackName(Method bfContainMethod) {
        return bfContainMethod.getAnnotation(BFMightContain.class).fallback();
    }


}
