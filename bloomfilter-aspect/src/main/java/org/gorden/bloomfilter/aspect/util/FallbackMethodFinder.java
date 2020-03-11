package org.gorden.bloomfilter.aspect.util;

import org.gorden.bloomfilter.aspect.annotation.BFMightContain;
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

    public Optional<Method> getFallbackMethod(Class<?> enclosingType, Method methodAnnotatedWithBFMightContain) {
        if (methodAnnotatedWithBFMightContain.isAnnotationPresent(BFMightContain.class)) {
            return this.find(enclosingType, methodAnnotatedWithBFMightContain);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Method> find(Class<?> enclosingType, Method methodAnnotatedWithBFMightContain) {
        String name = getFallbackName(methodAnnotatedWithBFMightContain);
        Class<?>[] fallbackParameterTypes = methodAnnotatedWithBFMightContain.getParameterTypes();
        Class<?> methodAnnotatedWithBFMightContainReturnType = methodAnnotatedWithBFMightContain.getReturnType();
        return getMethod(enclosingType, name, methodAnnotatedWithBFMightContainReturnType, fallbackParameterTypes);
    }

    public String getFallbackName(Method bfContainMethod) {
        return bfContainMethod.getAnnotation(BFMightContain.class).fallback();
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

}
