package cn.gorden.bloomfilter.aspect;

import cn.gorden.bloomfilter.aspect.observer.BloomFilterObserver;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.ConstructorSignature;
import cn.gorden.bloomfilter.common.BloomFilter;

/**
 * @author GordenTam
 **/
@Aspect
public class BloomFilterCreatedAspect {

    @Pointcut("initialization(*.new())")
    void anyDefaultConstructor() {
    }

    /**
     * Defines a Pointcut for any constructor in a class implementing BloomFilter -
     * except default constructors (i.e. those having no arguments).
     *
     * @param joinPoint    The currently executing joinPoint.
     * @param bloomFilter The bloomFilter instance just created.
     */
    @Pointcut(value = "initialization(cn.gorden.bloomfilter.common.BloomFilter+.new(..)) "
            + "&& this(bloomFilter) "
            + "&& !anyDefaultConstructor()", argNames = "joinPoint, bloomFilter")
    void anyNonDefaultConstructor(final JoinPoint joinPoint, final BloomFilter bloomFilter) {
    }

    /**
     * bloom created aspect, performing its job after calling any constructor except
     * non-private default ones (having no arguments).
     *
     * @param joinPoint   The currently executing joinPoint.
     * @param bloomFilter The bloomFilter instance just created.
     */
    @AfterReturning(value = "anyNonDefaultConstructor(joinPoint, bloomFilter)", argNames = "joinPoint, bloomFilter")
    public void registeredBloomFilterAfter(final JoinPoint joinPoint, final BloomFilter bloomFilter) {

        if (joinPoint.getStaticPart() == null) {
            return;
        }

        // Ignore calling validateInternalState when we execute constructors in
        // any class but the concrete Validatable class.
        final ConstructorSignature sig = (ConstructorSignature) joinPoint.getSignature();
        final Class<?> constructorDefinitionClass = sig.getConstructor().getDeclaringClass();
        if (bloomFilter.getClass() == constructorDefinitionClass) {
            String name = bloomFilter.getName();
            BloomFilterObserver.registered(name, bloomFilter);
        }
    }
}
