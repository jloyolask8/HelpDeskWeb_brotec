package com.itcs.helpdesk.jsfcontrollers.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author Danilo
 */
public class LoginFilter implements Filter {

    private FilterConfig filterConfig = null;

    public LoginFilter() {
    }

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private String xmlPartialRedirectToPage(HttpServletRequest request, String page) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='UTF-8'?>");
        sb.append("<partial-response><redirect url=\"").append(request.getContextPath()).append(request.getServletPath()).append(page).append("\"/></partial-response>");
        return sb.toString();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        System.out.println("\n****** LoginFilter.doFilter ****** \n");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();
//        String requestedPage = req.getPathTranslated();//java.lang.NullPointerException in WLS
        String requestedPage = req.getRequestURI();
        UserSessionBean userSessionBean = (UserSessionBean) session.getAttribute("UserSessionBean");

//        System.out.println("LoginFilter.doFilter:" + requestedPage);
//        for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
//            String headerName = (String) e.nextElement();
//            System.out.println("Name = " + headerName + " = " + req.getHeader(headerName));
//        }
        try {
            if (!requestedPage.endsWith(".xhtml")) {
                if (userSessionBean != null) {
                    if (userSessionBean.isValidatedSession()) {
                        System.out.println("LoginFilter.sendRedirect:/script/index.xhtml");
                        res.sendRedirect(req.getContextPath() + req.getServletPath() + "/script/index.xhtml");
                        return;
                    }
                }
            }
            if (requestedPage.endsWith("login.xhtml") || requestedPage.endsWith("forgot.xhtml")) {
                if (userSessionBean != null) {
                    if (userSessionBean.isValidatedSession()) {
                        System.out.println("LoginFilter.sendRedirect:/script/index.xhtml");
                        res.sendRedirect(req.getContextPath() + req.getServletPath() + "/script/index.xhtml");
                        return;
                    }
                }
            } else {
                if (userSessionBean == null) {
                    System.out.println("userSessionBean == null LoginFilter.sendRedirect:/script/login.xhtml");
                    if (isAjax(req)) {
                        res.getWriter().print(xmlPartialRedirectToPage(req, "/script/login.xhtml"));
                        res.flushBuffer();
                    } else {
                        System.out.println("redirecting... LoginFilter.sendRedirect:/script/login.xhtml");
                        res.sendRedirect(req.getContextPath() + req.getServletPath() + "/script/login.xhtml");
                    }

                    return;
                } else {
                    if (!userSessionBean.isValidatedSession()) {
                        System.out.println("userSessionBean is not ValidatedSession LoginFilter.sendRedirect:" + "/script/login.xhtml");
                        if (isAjax(req)) {
                            res.getWriter().print(xmlPartialRedirectToPage(req, "/script/login.xhtml"));
                            res.flushBuffer();
                        } else {
                            res.sendRedirect(req.getContextPath() + req.getServletPath() + "/script/login.xhtml");
                        }
                        return;
                    }
                }
            }
//            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "LoginFilter.doFilter", e);
            if (isAjax(req)) {
                res.getWriter().print(xmlPartialRedirectToPage(req, "/script/login.xhtml"));
                res.flushBuffer();
            } else {
                res.sendRedirect(req.getContextPath() + "/faces/script/login.xhtml");
                return;
            }
        }

        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("LoginFilter.init");
        this.filterConfig = filterConfig;
//        if (filterConfig != null) {
//
//        }
    }

    @Override
    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
}
