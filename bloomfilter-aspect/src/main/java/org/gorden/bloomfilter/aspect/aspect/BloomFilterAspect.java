package org.gorden.bloomfilter.aspect.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gorden.bloomfilter.aspect.annotation.BFPut;
import org.gorden.bloomfilter.core.AbstractBloomFilter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author GordenTam
 **/
@Aspect
public class BloomFilterAspect {

    private final static ConcurrentHashMap<String, AbstractBloomFilter> BloomFilterCache = new ConcurrentHashMap<>(32);

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFPut)")
    public void pointcut1(){}

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFMightContain)")
    public void pointcut2(){}

    @Around("pointcut1()&& @annotation(bfput)")
    public Object methodsAnnotatedWithBloomFilter(ProceedingJoinPoint joinPoint, BFPut bfput) throws Throwable{
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("the annotation can only be used in methods");
        }
        //MethodSignature methodSignature = (MethodSignature)signature;
        String name = bfput.name();
        Object returnObject = joinPoint.proceed();
        AbstractBloomFilter bf = BloomFilterCache.get(name);
        bf.put(returnObject);
        return returnObject;
    }

}
