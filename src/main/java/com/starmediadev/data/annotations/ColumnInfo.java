package com.starmediadev.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnInfo {
    int length() default 100;

    boolean ignored() default false;

    //boolean autoIncrement() default false;

    //boolean unique() default false;
    
    String name() default "";
}
