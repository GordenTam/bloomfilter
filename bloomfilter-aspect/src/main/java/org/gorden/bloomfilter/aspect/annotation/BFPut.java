package org.gorden.bloomfilter.aspect.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface BFPut {

    String name() default "";

}
