package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.util.Log;
import java.io.*;
import java.util.logging.Level;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.*;

/**
 *
 * @author Danilo
 */
public class LoginCustomerFilter implements Filter {

    private FilterConfig filterConfig = null;

    public LoginCustomerFilter() {
    }

//    private String getHostSubDomain(String url) {
//        try {
//            URL url_ = new URL(url);
//            String host = url_.getHost();
//            if (host != null && !host.equalsIgnoreCase("localhost")) {
//                return host.substring(0, host.indexOf(".godesk.cl"));
//            } else {
//                return host;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        String requestedPage = req.getPathTranslated();//getRequestURL().toString();

        UserSessionBean userSessionBean = (UserSessionBean) session.getAttribute("UserSessionBean");

        //System.out.println("userSessionBean:" + userSessionBean);

        //System.out.println("Filtering Request:" + req.getRequestURL().toString());

//        String tenantId = null;
//        if (userSessionBean != null && userSessionBean.getTenantId() != null) {
//            tenantId = userSessionBean.getTenantId();
//        } else {
//            tenantId = getHostSubDomain(req.getRequestURL().toString());
//        }
//        //System.out.println("tenantId:" + tenantId);
        try {

            if (requestedPage.endsWith(".xhtml")) {
                if (requestedPage.endsWith("login.xhtml")) {
                    if (userSessionBean != null) {
                        if (userSessionBean.isValidatedCustomerSession()) {
                            res.sendRedirect(req.getContextPath() + "/faces/customer/casos.xhtml");
                            return;
                        }
                    }
                } else if (requestedPage.endsWith("casos.xhtml")
                        || requestedPage.endsWith("ticket.xhtml")) {

                    if (userSessionBean != null) {

                        if (!userSessionBean.isValidatedCustomerSession()) {
                            res.sendRedirect(req.getContextPath() + "/faces/customer/login.xhtml");
                            return;
                        }

                    } else {
                        res.sendRedirect(req.getContextPath() + "/faces/customer/login.xhtml");
                        return;
                    }
                }
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error en login filtering", e);
            return;
        }
        chain.doFilter(request, response);

    }

//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest req = (HttpServletRequest) request;
//        HttpServletResponse res = (HttpServletResponse) response;
//        HttpSession session = req.getSession();
////        String requestedPage = req.getRequestURL().toString();
//        String requestedPage = req.getPathTranslated();
//
//        UserSessionBean userSessionBean = (UserSessionBean) session.getAttribute("UserSessionBean");
//
//        //System.out.println("userSessionBean:" + userSessionBean);
//
//        //System.out.println("Filtering Request:" + requestedPage);
//
//       try {
//            if (requestedPage.endsWith(".xhtml")) {
//                if (requestedPage.endsWith("login.xhtml")
//                        || requestedPage.endsWith("context.xhtml")) {
//                    if (userSessionBean != null) {
//                        if (userSessionBean.isValidatedCustomerSession()) {
//                            res.sendRedirect(req.getContextPath() + "/faces/customer/casos.xhtml");
//                            return;
//                        }
//                    }
//                } else if(requestedPage.endsWith("casos.xhtml")
//                        || requestedPage.endsWith("ticket.xhtml")){
//                    
//                    if (userSessionBean == null) {
//                        res.sendRedirect(req.getContextPath() + "/faces/customer/context.xhtml");
//                        return;
//                    } else {
//
//                        if (StringUtils.isEmpty(userSessionBean.getTenantId())) {
//                            // Check tenant context, if no context then redirect to invalid page and ask to provide a valid context.
//                            res.sendRedirect(req.getContextPath() + "/faces/customer/context.xhtml");
//                            return;
//                        }
//
//                        if (!userSessionBean.isValidatedCustomerSession()) {
//                            res.sendRedirect(req.getContextPath() + "/faces/customer/login.xhtml");
//                            return;
//                        }
//                    }
//                } else{
//                    
//                     if (userSessionBean == null) {
//                        res.sendRedirect(req.getContextPath() + "/faces/customer/context.xhtml");
//                        return;
//                    } else {
//
//                        if (StringUtils.isEmpty(userSessionBean.getTenantId())) {
//                            // Check tenant context, if no context then redirect to invalid page and ask to provide a valid context.
//                            res.sendRedirect(req.getContextPath() + "/faces/customer/context.xhtml");
//                            return;
//                        }
//                    }
//                    
//                }
//                        
//            }
//        } catch (Exception e) {
//            res.sendRedirect(req.getContextPath() + "/faces/customer/context.xhtml");
//            return;
//        }
//
//        chain.doFilter(request, response);
//
//    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
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
