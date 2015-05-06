/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
//        System.out.println("\n****** CharacterEncodingFilter.doFilter: " + ((HttpServletRequest) req).getRequestURI() );
        try {
            req.setCharacterEncoding("UTF-8");
        } catch (Exception e) {
           System.out.println("Error on setCharacterEncoding for request : " + e.getMessage());
        }

        try {
            resp.setCharacterEncoding("UTF-8");
        } catch (Exception e) {
           System.out.println("Error on setCharacterEncoding for response : " + e.getMessage());
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
