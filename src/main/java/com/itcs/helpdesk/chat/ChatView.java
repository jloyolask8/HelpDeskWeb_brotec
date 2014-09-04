package com.itcs.helpdesk.chat;
/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.itcs.helpdesk.ejb.notifications.NotificationData;
import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import java.io.Serializable;
import javax.enterprise.event.Observes;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class ChatView implements Serializable {

    //private final PushContext pushContext = PushContextFactory.getDefault().getPushContext();
    private final EventBus eventBus = EventBusFactory.getDefault().eventBus();

    @ManagedProperty("#{applicationBean}")
    private ApplicationBean applicationBean;

    @ManagedProperty("#{UserSessionBean}")
    private UserSessionBean userSessionBean;

    private String privateMessage;

    private String globalMessage;

    private String privateUser;

    private final static String CHANNEL = "/{room}";

    public String getPrivateUser() {
        return privateUser;
    }

    public void setPrivateUser(String privateUser) {
        this.privateUser = privateUser;
    }

    public String getGlobalMessage() {
        return globalMessage;
    }

    public void setGlobalMessage(String globalMessage) {
        this.globalMessage = globalMessage;
    }

    public String getPrivateMessage() {
        return privateMessage;
    }

    public void setPrivateMessage(String privateMessage) {
        this.privateMessage = privateMessage;
    }

//    public boolean isLoggedIn() {
//        return loggedIn;
//    }
//
//    public void setLoggedIn(boolean loggedIn) {
//        this.loggedIn = loggedIn;
//    }
    public void sendGlobal() {
//        final String channel = CHANNEL + "*";
        final String channel = CHANNEL + "/*";
        System.out.println("sendGlobal: " + channel + " , globalMessage:" + globalMessage);
        eventBus.publish(channel, userSessionBean.getCurrent().getIdUsuario() + ": " + globalMessage);
        globalMessage = null;
    }

    public void sendPrivate() {
//        final String channel = CHANNEL + privateUser;
        if (applicationBean.containsChannel(privateUser)) {
            final String channel = CHANNEL + applicationBean.getChannel(privateUser);
            System.out.println("sendPrivate:" + channel + ",privateMessage:" + privateMessage);
//            eventBus.publish(channel, "[PM] " + userSessionBean.getCurrent().getIdUsuario() + ": " + privateMessage);
            eventBus.publish(channel, userSessionBean.getCurrent().getIdUsuario() + ": " + "[PM] " + privateMessage);

            privateMessage = null;
        }

    }

    

//    public void login() {
//        RequestContext requestContext = RequestContext.getCurrentInstance();
//
//        if (users.contains(username)) {
//            loggedIn = false;
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username taken", "Try with another username."));
//            requestContext.update("growl");
//        } else {
//            users.add(username);
//            requestContext.execute("PF('subscriber').connect('/" + username + "')");
//            loggedIn = true;
//        }
//    }
//    public void disconnect() {
//        //remove user and update ui
//        applicationBean.removeChannel(userSessionBean.getCurrent().getIdUsuario());
//        RequestContext.getCurrentInstance().update("form:users");
//
//        //push leave information
//        eventBus.publish(CHANNEL + "*", userSessionBean.getCurrent().getIdUsuario() + " left the channel.");
//
//        //reset state
////        loggedIn = false;
//    }
    /**
     * @return the applicationBean
     */
    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    /**
     * @param applicationBean the applicationBean to set
     */
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    /**
     * @return the userSessionBean
     */
    public UserSessionBean getUserSessionBean() {
        return userSessionBean;
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }
}
