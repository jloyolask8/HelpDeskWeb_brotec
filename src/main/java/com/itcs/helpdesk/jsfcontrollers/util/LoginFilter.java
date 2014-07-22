package com.itcs.helpdesk.jsfcontrollers.util;

import java.io.*;
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

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        String requestedPage = req.getPathTranslated();
        UserSessionBean userSessionBean = (UserSessionBean) session.getAttribute("UserSessionBean");
        try{
            if (requestedPage.endsWith(".xhtml")) 
            {
                if (requestedPage.endsWith("login.xhtml")) 
                {
                    if (userSessionBean != null) 
                    {
                        if (userSessionBean.isValidatedSession()) 
                        {
                            res.sendRedirect(req.getContextPath() + "/faces/script/index.xhtml");
                            return;
                        }
                    }
                }
                else
                {
                    if (userSessionBean == null)
                    {
                        res.sendRedirect(req.getContextPath() + "/faces/script/login.xhtml");
                        return;
                    }
                    else
                    {
                        if (!userSessionBean.isValidatedSession())
                        {
                            res.sendRedirect(req.getContextPath() + "/faces/script/login.xhtml");
                            return;
                        }
                    }
                }
            }
        }catch(Exception e){
            res.sendRedirect(req.getContextPath() + "/faces/script/login.xhtml");
            return;
        }
        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
//        if (filterConfig != null) {
//
//        }
    }

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
