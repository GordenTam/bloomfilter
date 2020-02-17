package org.gorden.bloomfilter.aspect.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface BFPut {

    String value();

}
