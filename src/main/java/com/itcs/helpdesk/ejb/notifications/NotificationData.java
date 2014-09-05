/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.ejb.notifications;

/**
 *
 * @author jonathan
 */
public class NotificationData {

    private String title;
    private String body;
    private String idUsuario;//receiver, if null message is for all

    public NotificationData(String title, String body, String idUsuario) {
        this.title = title;
        this.body = body;
        this.idUsuario = idUsuario;
    }

    @Override
    public String toString() {
        return "NotificationData{" + "title=" + title + ", body=" + body + ", idUsuario=" + getIdUsuario() + '}';
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the idUsuario
     */
    public String getIdUsuario() {
        return idUsuario;
    }

    /**
     * @param idUsuario the idUsuario to set
     */
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
