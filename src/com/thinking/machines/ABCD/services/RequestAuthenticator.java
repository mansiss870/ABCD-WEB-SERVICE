package com.thinking.machines.ABCD.services;
import javax.servlet.http.*;
import javax.servlet.*;
public interface RequestAuthenticator
{
public boolean isRequestValid(HttpServletRequest rq,HttpSession session,ServletContext sc);
public void ifRequestInvalid(HttpServletRequest rq,HttpSession session,ServletContext sc);
}