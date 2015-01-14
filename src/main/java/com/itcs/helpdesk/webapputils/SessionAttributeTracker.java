/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.Thread.State;
import java.util.logging.Logger;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 *
 * @author Jorge
 */
public class SessionAttributeTracker implements HttpSessionAttributeListener {

    @Override
    public void attributeAdded(HttpSessionBindingEvent sessionBindingEvent) {
        Object obj = sessionBindingEvent.getValue();
        
        if(!isSerializable(obj)){
            Logger.getGlobal().warning("Attribute '"+sessionBindingEvent.getName()+
                    "' added to session with Not Serializable Object: "+
                    sessionBindingEvent.getValue());
            Logger.getGlobal().warning("Thread Stack for NotSerializable Object:\n"+threadDump());
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String threadDump(){
        StringBuilder fullThreadDump = new StringBuilder();
        Thread t = Thread.currentThread();
        State state = t.getState();
        String tName = t.getName();
        if(state != null){
            fullThreadDump
                    .append("   ")
                    .append(tName)
                    .append(": ")
                    .append(state)
                    .append("\n");
        }
        StackTraceElement[] stes = t.getStackTrace();
        for (StackTraceElement trace : stes) {
            fullThreadDump
                    .append("      at ")
                    .append(trace)
                    .append("\n");
        }
        return fullThreadDump.toString();
    }

    private boolean isSerializable(Object obj) {
        boolean ret = false;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(out);
            oos.writeObject(obj);
            ret = true;
        } catch (IOException e) {
            return ret;
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
            }
        }
        return ret;
    }

}
