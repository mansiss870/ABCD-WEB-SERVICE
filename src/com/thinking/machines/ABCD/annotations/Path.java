package com.thinking.machines.ABCD.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Path 
{
public String value() default "";
}