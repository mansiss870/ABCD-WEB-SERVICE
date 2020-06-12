package com.thinking.machines.ABCD.services;
import com.thinking.machines.ABCD.services.*;
import com.thinking.machines.ABCD.annotations.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.io.UnsupportedEncodingException;  
import java.net.URLDecoder;  
import java.net.URLEncoder;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.annotation.*;
import com.google.gson.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
@MultipartConfig(maxFileSize=(1024*1024*5))
public class TMService extends HttpServlet
{
HttpServletRequest rq;
HttpServletResponse rs;
String url;
ServletContext servletContext;
List<Service> services;
PrintWriter pw;
HttpSession session;
String path;
public void service(HttpServletRequest rq,HttpServletResponse rs)
{
try{
this.rq=rq;
this.rs=rs;
path=getServletContext().getRealPath("");
session=rq.getSession();
url=rq.getRequestURI();
servletContext=getServletContext();
url=url.substring(servletContext.getContextPath().length()+8);
services=(List<Service>)servletContext.getAttribute("services");
pw=rs.getWriter();
processRequest();
}catch(Exception e)
{
e.printStackTrace();
}
}

public void processRequest()
{
try{
Gson gson;
Object response;
for(Service service:services)
{
if(service.exists(url))
{
if(service.isSecured()) 
{
RequestAuthenticator requestAuthenticator=(RequestAuthenticator)service.getRequestAuthenticator();
if(requestAuthenticator.isRequestValid(rq,session,servletContext))
{
if(ServletFileUpload.isMultipartContent(rq))
{
processFileUploadRequest(service);
}else
{
invokeRequestedService(service);
}
}else{
requestAuthenticator.ifRequestInvalid(rq,session,servletContext);
}
}else
{
if(ServletFileUpload.isMultipartContent(rq))
{
processFileUploadRequest(service);
}else
{
invokeRequestedService(service);
}

}
return;
}
}
}catch(Exception e)
{
e.printStackTrace();
}
}





public void processFileUploadRequest(Service service)
{
try{
Object response;
Method method=service.getMethodInstance(url);
Object obj=service.getClassInstance();
String annotnValue="";
List<File> files=new LinkedList<>();

if(method.isAnnotationPresent(Forward.class) && method.getReturnType().equals("void"))
{
System.out.println("Forward annotation presrent,return type should be void");
pw.println("");
return;
}
if(!method.isAnnotationPresent(FileMaxSize.class))
{
System.out.println("Service has parameter of type file(s),FileMaxSize annotation required");
pw.println("");
return;
}
long size=((FileMaxSize)method.getAnnotation(FileMaxSize.class)).value();
Parameter parameters[]=method.getParameters();
Object args[]=new Object[parameters.length];
String paramType;
int i=0;
File dir=new File(path+"WEB-INF"+File.separator+"filestore");
if(!dir.exists())
{
dir.mkdirs();
}
path=path+"WEB-INF"+File.separator+"filestore"+File.separator;
for(Part part:rq.getParts())
{
String cd=part.getHeader("content-disposition");
String pcs[]=cd.split(";");
for(String pc:pcs)
{
if(pc.indexOf("filename")!=-1)
{
String fn=pc.substring(pc.indexOf("=")+2,pc.length()-1);
File file=new File(path+fn);
if(file.length()>size)
{
System.out.println("length of file is greater than "+size);
pw.println("");
return;
}
if(file.exists()) file.delete();
part.write(path+fn);
files.add(file);
}
}
}
for(Parameter param:parameters)
{
paramType=param.getType().getName();
if(paramType.equals("java.io.File"))
{
args[i]=files.get(0);
}
else     
{
if(paramType.equals("java.util.List"))
{
args[i]=files;
}     
else 
{
if(paramType.equals("javax.servlet.http.HttpServletRequest"))
{
args[i]=rq;
}
else
{
if(paramType.equals("javax.servlet.http.HttpServletResponse"))
{
args[i]=rs;
}
else
{
if(paramType.equals("javax.servlet.http.HttpSession"))
{
args[i]=session;
}  
else
{
if(paramType.equals("javax.servlet.ServletContext"))
{
args[i]=servletContext;
}
else
{
if(param.isAnnotationPresent(RequestData.class))
{
annotnValue=((RequestData)param.getAnnotation(RequestData.class)).value();
if(paramType.equals("int"))
{
args[i]=Integer.parseInt(rq.getParameter(annotnValue));
}
else
{
if(paramType.equals("float"))
{
args[i]=Float.parseFloat(rq.getParameter(annotnValue));
}
else
{
if(paramType.equals("char"))
{
args[i]=rq.getParameter(annotnValue).charAt(0);
}
else
{
if(paramType.equals("long"))
{ 
args[i]=Long.parseLong(rq.getParameter(annotnValue));
}
else
{
if(paramType.equals("byte"))
{
args[i]=Byte.parseByte(rq.getParameter(annotnValue));
}
else
{
if(paramType.equals("double"))
{
args[i]=Double.parseDouble(rq.getParameter(annotnValue));
}
else
{
if(paramType.equals("short"))
{
args[i]=Short.parseShort(rq.getParameter(annotnValue));
}
else
{
if(paramType.equals("boolean"))
{
args[i]=Boolean.parseBoolean(rq.getParameter(annotnValue));
}
else
{
args[i]=Class.forName(paramType).cast(URLDecoder.decode( rq.getParameter(annotnValue), "UTF-8" ));
}
}           
}          
}               
}                    
}                         
}                            
}                                                
}                     
}                     
}                   
}                
}                    
}                     
}
i++;
}                     
response=method.invoke(obj,args);
if(method.isAnnotationPresent(Forward.class))
{
url=method.getAnnotation(Forward.class).value();
if(url.endsWith(".jsp") || url.endsWith(".html") || url.endsWith(".js"))
{
RequestDispatcher rd=rq.getRequestDispatcher(url);  
rd.forward(rq,rs);
}else
{
processRequest();
}
return;
}else
{
if(method.isAnnotationPresent(ResponseType.class))
{
String responseType=(method.getAnnotation(ResponseType.class).value()).toUpperCase();
sendResponse(responseType,response);
}else
{
pw.println((String)response);
}
}
}catch(Exception e)
{
e.printStackTrace();
}
}









private void invokeRequestedService(Service service)
{
try{
Gson gson;
Object response;
Method method=service.getMethodInstance(url);
Object obj=service.getClassInstance();
if(method.isAnnotationPresent(Forward.class) && method.getReturnType().equals("void"))
{
System.out.println("Forward annotation presrent,return type should be void");
pw.println("");
return;
}

Parameter parameters[]=method.getParameters();
Object args[]=new Object[parameters.length];
String paramType;
int i=0;
BufferedReader br=rq.getReader();
StringBuilder sb=new StringBuilder();
String line;
while(true)
{
line=br.readLine();
if(line==null) break;
sb.append(line);
}
String requestData=sb.toString();
if(requestData.startsWith("{") && requestData.endsWith("}") && parameters.length>=1)
{
gson=new Gson();
for(Parameter param:parameters)
{
paramType=param.getType().getName();
if(paramType.equals("javax.servlet.http.HttpServletRequest"))
{
args[i]=rq;
}else
{
if(paramType.equals("javax.servlet.http.HttpServletResponse"))
{
args[i]=rs;
}
else{
if(paramType.equals("javax.servlet.http.HttpSession"))
{
args[i]=session;
}else
{
if(paramType.equals("javax.servlet.ServletContext"))
{
args[i]=servletContext;
}else
{
args[i]=gson.fromJson(requestData,Class.forName(paramType));
}
}
}
}
i++;
}
response=method.invoke(obj,args);
if(method.isAnnotationPresent(Forward.class))
{
url=method.getAnnotation(Forward.class).value();
if(url.endsWith(".jsp") || url.endsWith(".html") || url.endsWith(".js"))
{
RequestDispatcher rd=rq.getRequestDispatcher(url);  //confusion hai yha
rd.forward(rq,rs);
}else
{
processRequest();
}
return;
}else
{
if(method.isAnnotationPresent(ResponseType.class))
{
String responseType=(method.getAnnotation(ResponseType.class).value()).toUpperCase();
sendResponse(responseType,response);
}else{
pw.println((String)response);
}
}
}
else{
if(url.endsWith(".jsp") || url.endsWith(".html") || url.endsWith(".js"))
{
RequestDispatcher rd=rq.getRequestDispatcher(url);  //confusion hai yha
rd.forward(rq,rs);
}else
{
String QSValues[];
if(!requestData.equals(""))
{
QSValues=requestData.split("&");
i=0;
String annotnValue;
String QSArg;
for(Parameter param:parameters)
{


paramType=param.getType().getName();
if(param.isAnnotationPresent(RequestData.class))
{
annotnValue=((RequestData)param.getAnnotation(RequestData.class)).value();
while(i<QSValues.length)
{
if(annotnValue.equals(QSValues[i].split("=")[0]))
{
QSArg=QSValues[i].split("=")[1];
if(paramType.equals("int"))
{
args[i]=Integer.parseInt(QSArg);
}else
{
if(paramType.equals("float"))
{
args[i]=Float.parseFloat(QSArg);
}else
{
if(paramType.equals("char"))
{
args[i]=QSArg.charAt(0);
}else
{
if(paramType.equals("long"))
{
args[i]=Long.parseLong(QSArg);
}else
{
if(paramType.equals("byte"))
{
args[i]=Byte.parseByte(QSArg);
}
else
{
if(paramType.equals("double"))
{
args[i]=Double.parseDouble(QSArg);
}
else
{
if(paramType.equals("short"))
{
args[i]=Short.parseShort(QSArg);
}
else
{
if(paramType.equals("boolean"))
{
args[i]=Boolean.parseBoolean(QSArg);
}else
{
args[i]=Class.forName(paramType).cast(URLDecoder.decode( QSArg, "UTF-8" ));
}
}
}
}
}
}
}
}


i=0;
break;
}
i++;
}
}
}
response=method.invoke(obj,args);
}else
{
response=method.invoke(obj);
}
if(method.isAnnotationPresent(Forward.class))
{
url=method.getAnnotation(Forward.class).value();
if(url.endsWith(".jsp") || url.endsWith(".html") || url.endsWith(".js"))
{
RequestDispatcher rd=rq.getRequestDispatcher(url);  
rd.forward(rq,rs);
}else
{
processRequest();
}
return;
}else
{
if(method.isAnnotationPresent(ResponseType.class))
{
String responseType=(method.getAnnotation(ResponseType.class).value()).toUpperCase();
sendResponse(responseType,response);
}else{
pw.println((String)response);
}
}



}
}
}catch(Exception e)
{
e.printStackTrace();
}
}
private void sendResponse(String responseType,Object response)
{
if(responseType.equals("NONE"))
{
pw.println("");
}else
{
if(responseType.equals("STRING"))
{
pw.println((String)response);
}else
{
if(responseType.equals("HTML/TEXT"))
{
rs.setContentType("html/text");
pw.println((String)response);
}else
{
if(responseType.equals("JSON"))
{
rs.setContentType("application/json");
Gson gson=new Gson();
String responseString=gson.toJson(response);
pw.print(responseString);
}
}
}
}
}
}