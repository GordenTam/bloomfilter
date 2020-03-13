package cn.gorden.bloomfilter.aspect.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface BFMightContain {

    String value();

    String fallback() default "";

}
