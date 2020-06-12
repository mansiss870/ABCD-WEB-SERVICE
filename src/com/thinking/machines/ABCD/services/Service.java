package com.thinking.machines.ABCD.services;
import com.thinking.machines.ABCD.annotations.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
public class Service
{
Class clss;
Class requestAuthenticatorClss;
Map<String,Method> methodsMap=new HashMap<>();
Object instance;
Object requestAuthenticator;
boolean isSecured;
public Service(String className)
{
try{
this.instance=null;
clss=Class.forName(className);
Method methods[]=clss.getDeclaredMethods();
String path;
com.thinking.machines.ABCD.annotations.Path a;
Secured secured;
this.isSecured=false;
this.requestAuthenticator=null;
if(clss.isAnnotationPresent(Secured.class))
{
this.isSecured=true;
secured=(Secured)clss.getAnnotation(Secured.class);
requestAuthenticatorClss=Class.forName(secured.value());
}
if(clss.isAnnotationPresent(com.thinking.machines.ABCD.annotations.Path.class))
{
a=(com.thinking.machines.ABCD.annotations.Path)clss.getAnnotation(com.thinking.machines.ABCD.annotations.Path.class);
path=a.value();
for(Method m:methods)
{
if(m.isAnnotationPresent(com.thinking.machines.ABCD.annotations.Path.class))
{
a=(com.thinking.machines.ABCD.annotations.Path)m.getAnnotation(com.thinking.machines.ABCD.annotations.Path.class);
methodsMap.put(path+a.value(),m);
}
}
}
}catch(Exception e)
{
e.printStackTrace();
}
}
public boolean isSecured()
{
return isSecured;
}
public boolean exists(String path)
{
return methodsMap.containsKey(path);
}
public Method getMethodInstance(String path)
{
return methodsMap.get(path);
}
public Object getClassInstance()
{
try{
if(this.instance==null)
{
this.instance=clss.newInstance();
}
}catch(Exception e)
{
e.printStackTrace();
}
return this.instance;
}
public Object getRequestAuthenticator()
{
try{
if(this.requestAuthenticator==null)
{
this.requestAuthenticator=requestAuthenticatorClss.newInstance();
}
}catch(Exception e)
{
e.printStackTrace();
}
return this.requestAuthenticator;
}
}