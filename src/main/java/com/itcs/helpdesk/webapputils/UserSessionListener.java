/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
 * @author jonathan
 */
@WebListener
public class UserSessionListener implements HttpSessionListener {

    private static int totalActiveSessions;

    public static int getTotalActiveSession() {
        return totalActiveSessions;
    }

    @Override
    public void sessionCreated(HttpSessionEvent arg0) {
        totalActiveSessions++;
//        System.out.println("sessionCreated - add one session into counter");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent arg0) {
        totalActiveSessions--;
//        System.out.println("sessionDestroyed - from:" + arg0.getSource());
       
        ApplicationBean applicationBean = (ApplicationBean) arg0.getSession().getServletContext().getAttribute("applicationBean");
        try {
            final String sessionId = arg0.getSession().getId();
//            System.out.println("sessionId:"+sessionId);
            String idUsuario = applicationBean.getSessionIdMappings().get(sessionId);
            applicationBean.removeChannel(idUsuario);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("sessionDestroyed - deduct one session from counter");
    }
}
