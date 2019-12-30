package org.gorden.bloomfilter.aop.aspect;

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

    @Pointcut("@annotation(org.gorden.bloomfilter.aop.annotation.BFPut)")
    public void pointcut(){}

    @Around("pointcut()")
    public Object methodsAnnotatedWithBloomFilter(ProceedingJoinPoint joinPoint) throws Throwable{
        Method method = null;
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            method = getDeclaredMethod(joinPoint.getTarget().getClass(), signature.getName(), getParameterTypes(joinPoint));
        }

        return method;
        Validate.notNull(method, "failed to get method from joinPoint: %s", new Object[]{joinPoint});
        HystrixCommandAspect.MetaHolderFactory metaHolderFactory = (HystrixCommandAspect.MetaHolderFactory)META_HOLDER_FACTORY_MAP.get(HystrixCommandAspect.HystrixPointcutType.of(method));
        MetaHolder metaHolder = metaHolderFactory.create(joinPoint);
        HystrixInvokable invokable = HystrixCommandFactory.getInstance().create(metaHolder);
        ExecutionType executionType = metaHolder.isCollapserAnnotationPresent() ? metaHolder.getCollapserExecutionType() : metaHolder.getExecutionType();

        try {
            Object result;
            if (!metaHolder.isObservable()) {
                result = CommandExecutor.execute(invokable, executionType, metaHolder);
            } else {
                result = this.executeObservable(invokable, executionType, metaHolder);
            }

            return result;
        } catch (HystrixBadRequestException var9) {
            throw (Throwable)(var9.getCause() != null ? var9.getCause() : var9);
        } catch (HystrixRuntimeException var10) {
            throw this.hystrixRuntimeExceptionToThrowable(metaHolder, var10);
        }
    }

    public static Method getDeclaredMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
        Method method = null;

        try {
            method = type.getDeclaredMethod(methodName, parameterTypes);
            if (method.isBridge()) {
                method = MethodProvider.getInstance().unbride(method, type);
            }
        } catch (NoSuchMethodException var6) {
            Class<?> superclass = type.getSuperclass();
            if (superclass != null) {
                method = getDeclaredMethod(superclass, methodName, parameterTypes);
            }
        } catch (ClassNotFoundException var7) {
            Throwables.propagate(var7);
        } catch (IOException var8) {
            Throwables.propagate(var8);
        }

        return method;
    }

}
