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
public class NoInstanceConfigurationException extends Exception {

    public NoInstanceConfigurationException() {
        super("Existe un problema con su configuración. esto podría afectar el funcionamiento del sistema. Es importante que lo revise con el proveedor del servicio.");
    }

}
