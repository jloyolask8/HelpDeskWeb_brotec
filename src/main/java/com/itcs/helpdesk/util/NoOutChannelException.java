/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.itcs.helpdesk.util;

/**
 *
 * @author jonathan
 */
public class NoOutChannelException extends Exception {

    public NoOutChannelException() {
    }

    @Override
    public String getMessage() {
        return "No se puede determinar el canal de salida del caso.";
    }
    
}
