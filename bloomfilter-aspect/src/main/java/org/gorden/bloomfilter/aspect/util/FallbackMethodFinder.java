package org.gorden.bloomfilter.aspect.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
import org.gorden.bloomfilter.aspect.exception.FallbackDefinitionException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public final class FallbackMethodFinder {

    private FallbackMethodFinder() {

    }

    private static final FallbackMethodFinder INSTANCE = new FallbackMethodFinder();

    public static FallbackMethodFinder getInstance() {
        return INSTANCE;
    }


    public Optional<Method> getFallbackMethod(Class<?> enclosingType, Method bfMightContainMethod) {
        if (bfMightContainMethod.isAnnotationPresent(BFMightContain.class)) {
            return this.find(enclosingType, bfMightContainMethod);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Method> find(Class<?> enclosingType, Method bfMightContainMethod) {
        String name = getFallbackName(enclosingType, bfMightContainMethod);
        Class<?>[] fallbackParameterTypes = bfMightContainMethod.getParameterTypes();

        if (fallbackParameterTypes[fallbackParameterTypes.length - 1] == Throwable.class) {
            fallbackParameterTypes = ArrayUtils.remove(fallbackParameterTypes, fallbackParameterTypes.length - 1);
        }

        Class<?>[] extendedFallbackParameterTypes = Arrays.copyOf(fallbackParameterTypes,
                fallbackParameterTypes.length + 1);
        extendedFallbackParameterTypes[fallbackParameterTypes.length] = Throwable.class;

        Optional<Method> exFallbackMethod = getMethod(enclosingType, name, extendedFallbackParameterTypes);
        Optional<Method> fMethod = getMethod(enclosingType, name, fallbackParameterTypes);
        Method method = exFallbackMethod.orElse(bfMightContainMethod);
        return method;
    }

    public String getFallbackName(Class<?> enclosingType, Method bfContainMethod) {
        return bfContainMethod.getAnnotation(BFMightContain.class).fallback();
    }

    @Override
    boolean canHandle(Class<?> enclosingType, Method commandMethod) {
        return StringUtils.isNotBlank(getFallbackName(enclosingType, commandMethod));
    }

    public static Optional<Method> getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return Optional.of(method);
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            return getMethod(superClass, name, parameterTypes);
        } else {
            return Optional.empty();
        }
    }

}
