package com.thinking.machines.ABCD.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseType 
{
public String value() default "";
}