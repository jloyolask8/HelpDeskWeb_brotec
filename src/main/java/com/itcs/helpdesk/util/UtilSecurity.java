/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Danilo
 */
public class UtilSecurity {

    /**
     * Genera codigo MD5 de un texto dado
     *
     * @param texto Texto a sacar el MD5
     * @return Retorno del texto MD5 del String dado.
     */
    synchronized public static String getMD5(String texto) {
        String output = "";
        try {

            byte[] textBytes = texto.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(textBytes);
            byte[] codigo = md.digest();

            for (int i = 0; i < codigo.length; i++) {
                output += Integer.toHexString((codigo[i] >> 4) & 0xf);
                output += Integer.toHexString(codigo[i] & 0xf);
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UtilSecurity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output.trim();

    }
}
