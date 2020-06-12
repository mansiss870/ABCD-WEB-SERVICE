package com.thinking.machines.ABCD.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FileMaxSize 
{
public long value() default 0;
}