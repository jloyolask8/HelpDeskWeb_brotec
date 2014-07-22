/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 *
 * @author jonathan
 */
public class ViewExpiredExceptionHandlerFactory extends ExceptionHandlerFactory {
 
    private ExceptionHandlerFactory parent;
 
    public ViewExpiredExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }
 
    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler result = parent.getExceptionHandler();
        result = new ViewExpiredExceptionHandler(result);
 
        return result;
    }
 
 
}