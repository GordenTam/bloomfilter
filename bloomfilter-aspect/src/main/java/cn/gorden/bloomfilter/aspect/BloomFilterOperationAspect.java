package cn.gorden.bloomfilter.aspect;

import cn.gorden.bloomfilter.aspect.annotation.BFMightContain;
import cn.gorden.bloomfilter.aspect.annotation.BFPut;
import cn.gorden.bloomfilter.aspect.exception.FallbackDefinitionException;
import cn.gorden.bloomfilter.aspect.exception.FallbackInvocationException;
import cn.gorden.bloomfilter.aspect.observer.BloomFilterObserver;
import cn.gorden.bloomfilter.common.BloomFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author GordenTam
 **/
@Aspect
public class BloomFilterOperationAspect {

    @Pointcut("@annotation(cn.gorden.bloomfilter.aspect.annotation.BFPut)")
    public void pointcut1() {
    }

    @Pointcut("@annotation(cn.gorden.bloomfilter.aspect.annotation.BFMightContain)")
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
        BFPut bfput = method.getAnnotation(BFPut.class);
        String name = bfput.value();
        if(name == null || name.trim().equals("")) {
            throw new IllegalStateException("the BloomFilter's name can not be null");
        }
        Object returnObject = joinPoint.proceed();
        Optional<BloomFilter> bf = BloomFilterObserver.getBloomFilter(name);
        if(!bf.isPresent()) {
            throw new IllegalStateException("the BloomFilter with name: " + name + " has not been created");
        }
        bf.get().put(returnObject);
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
        BFMightContain bfMightContain = methodAnnotatedWithBFMightContain.getAnnotation(BFMightContain.class);
        String name = bfMightContain.value();
        if (name == null || name.trim().equals("")) {
            throw new IllegalStateException("the BloomFilter's name can not be null");
        }
        Object returnObject = joinPoint.proceed();
        Optional<BloomFilter> bf = BloomFilterObserver.getBloomFilter(name);
        if (!bf.isPresent()) {
            throw new IllegalStateException("the BloomFilter with name: " + name + " has not been created");
        }
        if (bf.get().mightContain(returnObject)) {
            return returnObject;
        }
        //if the element not in bloomFilter, invoke the fallback method
        else {
            Object target = joinPoint.getTarget();
            Object[] fallbackMethodArgs = joinPoint.getArgs();
            return invokeFallbackMethod(target, fallbackMethodArgs, methodAnnotatedWithBFMightContain);
        }
    }

    private Object invokeFallbackMethod(Object target, Object[] fallbackMethodArgs, Method methodAnnotatedWithBFMightContain) {
        Class<?>[] parameterTypes = methodAnnotatedWithBFMightContain.getParameterTypes();
        String fallBackMethodName = getFallbackName(methodAnnotatedWithBFMightContain);
        Optional<Method> fallbackMethod = findFallBackMethod(target.getClass(), fallBackMethodName, parameterTypes);
        if (!fallbackMethod.isPresent()) {
            throw new FallbackDefinitionException("fallback method with name "+ fallBackMethodName + "with parameter types "
                    + Arrays.toString(parameterTypes) + "is not found.");
        } else {
            Class<?> methodAnnotatedWithBFMightContainReturnType = methodAnnotatedWithBFMightContain.getReturnType();
            Method fMethod = fallbackMethod.get();
            Class<?> fallbackReturnType = fMethod.getReturnType();
            if (!methodAnnotatedWithBFMightContainReturnType.isAssignableFrom(fallbackReturnType)) {
                throw new FallbackDefinitionException("fallback method with name " + fallBackMethodName
                        + " must return methodAnnotatedWithBFMightContainReturnType or its subclass");
            }
            try {
                fMethod.setAccessible(true);
                return fMethod.invoke(target, fallbackMethodArgs);
            } catch (Throwable e) {
                throw new FallbackInvocationException(e.getCause());
            }
        }
    }

    private static Optional<Method> findFallBackMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return Optional.of(method);
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            return findFallBackMethod(superClass, name, parameterTypes);
        } else {
            return Optional.empty();
        }
    }

    private String getFallbackName(Method methodAnnotatedWithBFMightContain) {
        return methodAnnotatedWithBFMightContain.getAnnotation(BFMightContain.class).fallback();
    }

}
