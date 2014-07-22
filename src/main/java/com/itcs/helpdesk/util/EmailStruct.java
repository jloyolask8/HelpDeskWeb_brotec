/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

/**
 *
 * @author jorge
 */
public class EmailStruct
{
    private String toAdress;
    private String subject;
    private String body;

    /**
     * @return the toAdress
     */
    public String getToAdress() {
        return toAdress;
    }

    /**
     * @param toAdress the toAdress to set
     */
    public void setToAdress(String toAdress) {
        this.toAdress = toAdress;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
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
}
