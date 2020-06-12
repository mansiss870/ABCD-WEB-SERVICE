package com.thinking.machines.ABCD.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestData 
{
public String value() default "";
}