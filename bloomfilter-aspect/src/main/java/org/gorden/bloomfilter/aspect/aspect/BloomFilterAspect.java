package org.gorden.bloomfilter.aspect.aspect;

import com.google.common.base.Throwables;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author: GordenTam
 * @create: 2019-12-30
 **/
@Aspect
public class BloomFilterAspect {

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFPut)")
    public void pointcut1(){}

    @Pointcut("@annotation(org.gorden.bloomfilter.aspect.annotation.BFMightContain)")
    public void pointcut2(){}

    @Around("pointcut1()||pointcut2()")
    public Object methodsAnnotatedWithBloomFilter(ProceedingJoinPoint joinPoint) throws Throwable{
        Method method = null;
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            method = getDeclaredMethod(joinPoint.getTarget().getClass(), signature.getName(), getParameterTypes(joinPoint));
        }
    }


}
