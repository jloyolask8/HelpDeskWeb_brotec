/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules;

/**
 *
 * @author jonathan
 */
public class ActionExecutionException extends Exception {

    public ActionExecutionException(String message) {
        super(message);
    }
   

    public ActionExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
    
}
