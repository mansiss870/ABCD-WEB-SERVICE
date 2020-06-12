package com.thinking.machines.ABCD.services;
import java.util.jar.*;
import com.google.gson.*;
import com.google.gson.stream.*;
import java.lang.reflect.*;
import java.util.*;
import com.thinking.machines.ABCD.annotations.*;
import com.thinking.machines.ABCD.services.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
public class ABCDInitializer extends HttpServlet
{
public void init()
{
try{
List<Service> services=new LinkedList<>();
ServletContext servletContext=getServletContext();
String path=servletContext.getRealPath("")+"WEB-INF"+File.separator+"servicesConfig.config";
Service service;
String className="";
JarFile jarFile;
Enumeration enumm;
JsonReader reader=new JsonReader(new FileReader(path));
JsonObject jo=new Gson().fromJson(reader,JsonObject.class);
JsonArray jarFiles = (JsonArray)jo.get("jars");
JsonArray packages=(JsonArray)jo.get("packages");
String packageName;
String folders;

List<String> classNames=new LinkedList<>();
for(int i=0;i<packages.size();i++)
{
packageName=packages.get(i).toString().substring(1,packages.get(i).toString().length()-1);
if(packageName.endsWith(".class"))
{
service=new Service(packageName.substring(0,packageName.length()-6));
services.add(service);
}else{
if(packageName.endsWith(".*"))
{
packageName=packageName.substring(0,packageName.length()-2);
folders=packageName.replace(".",""+File.separator);
File listOfFiles[]=new File(servletContext.getRealPath("")+"WEB-INF"+File.separator+"classes"+File.separator+folders).listFiles();
if(listOfFiles!=null)
{
for(File file:listOfFiles)
{
if(!file.isDirectory() && file.getName().endsWith(".class"))
{
service=new Service(packageName+"."+(file.getName().substring(0,file.getName().length()-6)));
services.add(service);
}
}
}
}else
{
System.out.println("Invalid package name : "+packageName);
return;
}
}
}
for(int i=0;i<jarFiles.size();i++)
{
packageName=jarFiles.get(i).toString().substring(1,jarFiles.get(i).toString().length()-1);
jarFile = new JarFile(servletContext.getRealPath("")+"WEB-INF"+File.separator+"lib"+File.separator+packageName);
enumm = jarFile.entries();
while (enumm.hasMoreElements()) 
{
JarEntry entry = (JarEntry)enumm.nextElement();
String name = entry.getName();
if(name.endsWith(".class"))
{
packageName=name.replace("/",".");
service=new Service(packageName.substring(0,packageName.length()-6));
services.add(service);
} 
}
i++; 
}
servletContext.setAttribute("services",services);
}catch(Exception e)
{
e.printStackTrace();
}
}
}
