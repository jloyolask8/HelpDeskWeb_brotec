package com.itcs.helpdesk.webapputils.filters;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
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

//      //System.out.println("\n****** LoginFilter.doFilter ****** \n");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        String requestedPage = req.getPathTranslated();//java.lang.NullPointerException in WLS
//      String requestedPage = req.getRequestURI();//Bug in login filter in GF

//        System.out.println("Filtering Request:" + req.getRequestURL().toString());

        UserSessionBean userSessionBean = (UserSessionBean) session.getAttribute("UserSessionBean");

        if (userSessionBean != null) {
            //System.out.println("userSessionBean:" + userSessionBean);
//            if (StringUtils.isEmpty(userSessionBean.getTenantId())) {
                /*not set yet, set tenant from url*/
            if (!userSessionBean.isValidatedSession()) {
                String tenantId = JsfUtil.getHostSubDomain(req.getRequestURL().toString());
                System.out.println("tenantId:" + tenantId);
                if (tenantId != null) {
                    userSessionBean.setTenantId(tenantId);
                } else {
                    res.sendRedirect(req.getContextPath() + "/faces/customer/system_failure.xhtml");
                    return;
                }
            }
//            }
        } else {
            System.out.println("userSessionBean is null now");
        }

//        //System.out.println("LoginFilter.doFilter:" + requestedPage);
//        for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
//            String headerName = (String) e.nextElement();
//            //System.out.println("Name = " + headerName + " = " + req.getHeader(headerName));
//        }
        try {
            if (requestedPage.endsWith(".xhtml")) {
                if (requestedPage.endsWith("login.xhtml") || requestedPage.endsWith("forgot.xhtml")) {
                    if (userSessionBean != null) {
                        if (userSessionBean.isValidatedSession()) {
                            //user logged in already
                            res.sendRedirect(req.getContextPath() + req.getServletPath() + "/script/index.xhtml");
                            return;
                        }
                    }
                } else if (requestedPage.endsWith("signup.xhtml") || requestedPage.endsWith("VerifyAccount.xhtml")) {
                    //let pass
                } else {
                    if (userSessionBean == null) {
                        System.out.println("userSessionBean == null LoginFilter.sendRedirect:/public/login.xhtml");
                        if (isAjax(req)) {
                            res.getWriter().print(xmlPartialRedirectToPage(req, "/public/login.xhtml"));
                            res.flushBuffer();
                        } else {
                            System.out.println("redirecting... LoginFilter.sendRedirect:/public/login.xhtml");
                            res.sendRedirect(req.getContextPath() + req.getServletPath() + "/public/login.xhtml");
                        }

                        return;
                    } else {
                        if (!userSessionBean.isValidatedSession()) {
                            System.out.println("userSessionBean is not ValidatedSession LoginFilter.sendRedirect:" + "/public/login.xhtml");
                            if (isAjax(req)) {
                                res.getWriter().print(xmlPartialRedirectToPage(req, "/public/login.xhtml"));
                                res.flushBuffer();
                            } else {
                                res.sendRedirect(req.getContextPath() + req.getServletPath() + "/public/login.xhtml");
                            }
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "LoginFilter.doFilter", e);
            if (isAjax(req)) {
                res.getWriter().print(xmlPartialRedirectToPage(req, "/public/login.xhtml"));
                res.flushBuffer();
            } else {
                res.sendRedirect(req.getContextPath() + "/faces/public/login.xhtml");
                return;
            }
        }

        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //System.out.println("LoginFilter.init");
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
