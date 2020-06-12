package com.thinking.machines.ABCD.tools;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import com.google.gson.*;
import com.google.gson.stream.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.annotation.*;
import com.thinking.machines.ABCD.services.*;
import com.thinking.machines.ABCD.annotations.*;
public class GetReports 
{
public GetReports (String args) throws IOException 
{
JarFile jarFile;
Enumeration enumm;
JsonReader reader=new JsonReader(new FileReader(args));
JsonObject jo=new Gson().fromJson(reader,JsonObject.class);
JsonArray jarFiles = (JsonArray)jo.get("jars");
JsonArray packages=(JsonArray)jo.get("packages");
String path=jo.get("path").getAsString();
String packageName;
String folders;
List<String> classNames=new LinkedList<>();
for(int i=0;i<packages.size();i++)
{
packageName=packages.get(i).toString().substring(1,packages.get(i).toString().length()-1);
if(packageName.endsWith(".class"))
{
classNames.add(packageName.substring(0,packageName.length()-6));
}else{
if(packageName.endsWith(".*"))
{
packageName=packageName.substring(0,packageName.length()-2);
folders=packageName.replace(".",""+File.separator);
File listOfFiles[]=new File(path+File.separator+"classes"+File.separator+folders).listFiles();
if(listOfFiles!=null)
{
for(File file:listOfFiles)
{
if(!file.isDirectory() && file.getName().endsWith(".class"))
{
classNames.add(packageName+"."+(file.getName().substring(0,file.getName().length()-6)));
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
jarFile = new JarFile(path+File.separator+"lib"+File.separator+packageName);
enumm = jarFile.entries();
while (enumm.hasMoreElements()) 
{
JarEntry entry = (JarEntry)enumm.nextElement();
String name = entry.getName();
if(name.endsWith(".class"))
{
packageName=name.replace("/",".");
classNames.add(packageName.substring(0,packageName.length()-6));
} 
}
i++; 
}
createServicesPdf(classNames);
createErrorPdf(classNames);
System.out.println("Services.pdf and errors.pdf saved.");
}
private void createServicesPdf(List<String> classNames)
{
String className;
Class clss;
Method methods[]=null;
String path="";
String returnType="";
Parameter parameters[]=null;
int count=0;
String paramTypes="";
com.thinking.machines.ABCD.annotations.Path a=null;
try
{
com.itextpdf.text.Document document=new com.itextpdf.text.Document();
com.itextpdf.text.pdf.PdfWriter pdfWriter=com.itextpdf.text.pdf.PdfWriter.getInstance(document,new FileOutputStream("Service.pdf"));
document.open();
com.itextpdf.text.Paragraph p=new com.itextpdf.text.Paragraph();
int k=classNames.size(); 
int pageNumber=0;
int pageSize=250;
boolean newPage=true;
com.itextpdf.text.Image logo;
float[] widths = {0.05f, 0.35f, 0.20f,0.20f,0.15f};
com.itextpdf.text.pdf.PdfPTable table=new com.itextpdf.text.pdf.PdfPTable(widths);
table.setWidthPercentage(100);
table.setSpacingBefore(0.2f);
table.setSpacingAfter(0.2f);
com.itextpdf.text.Paragraph paragraph;
com.itextpdf.text.Font firmNameFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,16,com.itextpdf.text.Font.BOLD);
com.itextpdf.text.Font titleFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,14,com.itextpdf.text.Font.BOLD);
com.itextpdf.text.Font columnTitleFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,12,com.itextpdf.text.Font.BOLD);
com.itextpdf.text.Font dataFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,12,com.itextpdf.text.Font.NORMAL);
String firmName="Web-Services";
int x=0;
while(x<k)
{
if(newPage)
{
paragraph=new com.itextpdf.text.Paragraph(firmName,firmNameFont);
paragraph.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
document.add(paragraph);
paragraph=new com.itextpdf.text.Paragraph("\n\n",titleFont);
paragraph.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
document.add(paragraph);
table.addCell("S.No");
table.addCell("Service");
table.addCell("Path");
table.addCell("accepts");
table.addCell("returns");
pageNumber++;
newPage=false;
}
try
{
path="";
returnType="";
paramTypes="";
className=classNames.get(x);
clss=Class.forName(className);
methods=clss.getDeclaredMethods();
if(clss.isAnnotationPresent(com.thinking.machines.ABCD.annotations.Path.class))
{
a=(com.thinking.machines.ABCD.annotations.Path)clss.getAnnotation(com.thinking.machines.ABCD.annotations.Path.class);
path=a.value();
for(Method m:methods)
{
if(m.isAnnotationPresent(com.thinking.machines.ABCD.annotations.Path.class))
{
a=(com.thinking.machines.ABCD.annotations.Path)m.getAnnotation(com.thinking.machines.ABCD.annotations.Path.class);
path=path+a.value()+",";
}
parameters=m.getParameters();
for(Parameter param:parameters)
{
path=path+"\n";
paramTypes=paramTypes+param.getType().getName()+",\n";
}
returnType=m.getReturnType().getName();
}
}
table.addCell(""+(x+1));
table.addCell(className);
table.addCell(path);
table.addCell(paramTypes);
table.addCell(returnType);
}catch(Exception ex)
{
ex.printStackTrace();
}
x++;
if(x%pageSize==0 || x==k)
{
document.add(table);
if(x!=k)
{
document.newPage();
newPage=true;
}
}
}
document.close();
}catch(Throwable t)
{
t.printStackTrace();
}
}
private void createErrorPdf(List<String> classNames)
{
String className;
Class clss;
Method methods[]=null;
String path="";
String returnType="";
String error="";
Set<String> sets=new HashSet<>();
Parameter parameters[]=null;
int count=0;
String paramTypes="";
com.thinking.machines.ABCD.annotations.Path a=null;
try
{
com.itextpdf.text.Document document=new com.itextpdf.text.Document();
com.itextpdf.text.pdf.PdfWriter pdfWriter=com.itextpdf.text.pdf.PdfWriter.getInstance(document,new FileOutputStream("errors.pdf"));
document.open();
com.itextpdf.text.Paragraph p=new com.itextpdf.text.Paragraph();
int k=classNames.size(); 
int pageNumber=0;
int pageSize=250;
boolean newPage=true;
com.itextpdf.text.Image logo;
float[] widths = {0.05f, 0.45f, 0.30f,0.45f};
com.itextpdf.text.pdf.PdfPTable table=new com.itextpdf.text.pdf.PdfPTable(widths);
table.setWidthPercentage(100);
table.setSpacingBefore(0.2f);
table.setSpacingAfter(0.2f);
com.itextpdf.text.Paragraph paragraph;
com.itextpdf.text.Font firmNameFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,16,com.itextpdf.text.Font.BOLD);
com.itextpdf.text.Font titleFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,14,com.itextpdf.text.Font.BOLD);
com.itextpdf.text.Font columnTitleFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,12,com.itextpdf.text.Font.BOLD);
com.itextpdf.text.Font dataFont=new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,12,com.itextpdf.text.Font.NORMAL);
String firmName="Web-Services";
int x=0;
while(x<k)
{
if(newPage)
{
paragraph=new com.itextpdf.text.Paragraph(firmName,firmNameFont);
paragraph.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
document.add(paragraph);
paragraph=new com.itextpdf.text.Paragraph("\nError\n",titleFont);
paragraph.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
document.add(paragraph);
table.addCell("S.No");
table.addCell("Service");
table.addCell("Path");
table.addCell("Error");
pageNumber++;
newPage=false;
}
try
{
path="";
returnType="";
paramTypes="";
error="";
className=classNames.get(x);
clss=Class.forName(className);
methods=clss.getDeclaredMethods();
if(clss.isAnnotationPresent(com.thinking.machines.ABCD.annotations.Path.class))
{
a=(com.thinking.machines.ABCD.annotations.Path)clss.getAnnotation(com.thinking.machines.ABCD.annotations.Path.class);
path=a.value();
for(Method m:methods)
{
if(m.isAnnotationPresent(com.thinking.machines.ABCD.annotations.Path.class))
{
a=(com.thinking.machines.ABCD.annotations.Path)m.getAnnotation(com.thinking.machines.ABCD.annotations.Path.class);
path=path+a.value()+",";
if(sets.contains(path+a.value()))
{
error=error+"path already exists : "+path+a.value()+",\n";
}
else{
sets.add(path+a.value());
}
}else
{
path="------";
error="Path annotation missing on a service : "+m.getName()+",\n";
}
parameters=m.getParameters();
for(Parameter param:parameters)
{
if(((param.getType().getName()).equals("java.io.File") || (param.getType().getName()).equals("java.util.List<java.io.File>") ) && !(m.isAnnotationPresent(FileMaxSize.class)))
{
path=path+"\n";
error=error+"service has parameter of type file(s),FileMaxSize annotation required.,\n";
}
}
if(m.isAnnotationPresent(Forward.class) && !m.getReturnType().getName().equals("void"))
{
path=path+"\n";
error=error+"Forward annotation present,return type should be void"+",\n";
}
}
}
table.addCell(""+(x+1));
table.addCell(className);
table.addCell(path);
table.addCell(error);
}catch(Exception ex)
{
ex.printStackTrace();
}
x++;
if(x%pageSize==0 || x==k)
{
document.add(table);
if(x!=k)
{
document.newPage();
newPage=true;
}
}
}
document.close();
}catch(Throwable t)
{
t.printStackTrace();
}
}

public static void main(String[] gg)
{
try{
GetReports jd=new GetReports(gg[0]);
}catch(Exception e)
{
e.printStackTrace();
}
}
}