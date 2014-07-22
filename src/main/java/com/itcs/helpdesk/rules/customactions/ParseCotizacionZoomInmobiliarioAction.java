/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webservices.DatosCaso;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jonathan
 */
public class ParseCotizacionZoomInmobiliarioAction extends ParseCotizacionAction {

    public ParseCotizacionZoomInmobiliarioAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    
    private final static String canalId = "ZoomInmobiliario";
    private final static String canalName = "Zoom Inmobiliario";

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException{

        DatosCaso datos = new DatosCaso();

        try {
            String re1 = "([^>]+)(<br>RUT:)";//This finds the name!
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(1);
                //String int1 = m.group(2);                  
                datos.parseNombre(string1.toString().trim());
            }
//                System.out.println("nombreCotizanteInput:" + nombreCotizanteInput);
        } catch (Exception e) {
             Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer nombre del solicitante", e);
        }

        try {
            String re1 = "(RUT:)(.*?)(<)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                datos.setRut(UtilesRut.formatear(string1.toString().trim()));
            }
//                System.out.println("rutCotizanteInput:" + rutCotizanteInput);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer RUT", e);
        }

        try {
            String re1 = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group();
//                    System.out.print("(" + string1.toString() + ")\n");
                datos.setEmail(string1.toString().trim());
            }
//                System.out.println("emailCotizanteInput:" + emailCotizanteInput);
        } catch (Exception e) {
              throw new ActionExecutionException("gether E-mail error", e);
//            e.printStackTrace();
        }

        try {
            String re1 = "(Teléfono:)(.*?)(<)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
//                    fonoCotizanteInput = string1.toString().trim();
                datos.setTelefono(string1.toString().trim());
            }
//                System.out.println("fonoCotizanteInput:" + fonoCotizanteInput);
        } catch (Exception e) {
          Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Teléfono", e);
        }

        try {
            String modelo = null;
            String re1 = "(Propiedad:)(.*?)(<)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                //String int1 = m.group(2);
                modelo = string1.toString();

                modelo = modelo.replace("Casa", "");
                modelo = modelo.replace("Departamento", "");
                modelo = modelo.replace("Depto", "");
                modelo = modelo.replace("Dpto", "");
                modelo = modelo.replace("Oficina", "");
                modelo = modelo.trim();
                datos.setModelo(modelo);
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Propiedad", e);
        }



        return datos;
    }

    @Override
    protected String getCanalId() {
        return canalId;
    }

    @Override
    protected String getCanalName() {
       return canalName;
    }
}
