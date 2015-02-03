/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.rules.ActionInfo;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webservices.DatosCaso;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jonathan ParseCotizacionAddElInmobiliarioAction
 */
@ActionInfo(name = "Analizar cotización de Vendedor Web",
        description = "Analiza y extrae información de cotizaciones enviadas desde Vendedor Web", mustShow = false)
public class ParseCotizacionVendedorWebAction extends ParseCotizacionAction {

    public ParseCotizacionVendedorWebAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    private final static String canalId = "VendedorWeb";
    private final static String canalName = "Vendedor Web";

    @Override
    protected void executeBeforeMerge(Caso caso) throws ActionExecutionException {
        super.executeBeforeMerge(caso);
        caso.setTema("Derivado Vendedor Web");
    }

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException {

        DatosCaso datos = new DatosCaso();

        try {
//          String re1 = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
            String re1 = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt.substring(txt.indexOf("Email:"), txt.indexOf("</a>")));
            if (m.find()) {
                String string1 = m.group();
                datos.setEmail(string1.toString().trim());
            } else {
                throw new ActionExecutionException("Error gathering email from cotizacion VendedorWeb");
            }

        } catch (Exception e) {
            throw new ActionExecutionException("Error gathering email from cotizacion VendedorWeb", e);
        }

        try {
            String re1 = "(Nombre:.*?</strong>)(.*?)(<)";//This finds the name!
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                datos.parseNombre(string1.toString().trim());
            } else {
                Log.createLogger(this.getClass().getName()).logWarning("Nombre no encontrado!");
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Nombre", e);
        }

        try {
            String re1 = "(Rut:.*?</strong>)(.*?)(<)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                datos.setRut(UtilesRut.formatear(string1.toString().trim()));
            } else {
                Log.createLogger(this.getClass().getName()).logWarning("Rut no encontrado!");
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Rut", e);
        }



        try {
            String re1 = "(Telefono:.*?</strong>)(.*?)(<)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                datos.setTelefono(string1.toString().trim());
            } else {
                Log.createLogger(this.getClass().getName()).logWarning("Telefono no encontrado!");
            }
//                //System.out.println("fonoCotizanteInput:" + fonoCotizanteInput);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Telefono", e);
        }

        try {
            String nombreProyecto = null;
            String re1 = "(Tipo:.*?</strong>)(.*?)(<)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                //String int1 = m.group(2);
                nombreProyecto = string1.toString();
                nombreProyecto = nombreProyecto.trim();
                datos.setProducto(nombreProyecto);
            } else {
                Log.createLogger(this.getClass().getName()).logWarning("nombreProyecto no encontrado!");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer nombreProyecto", e);
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
