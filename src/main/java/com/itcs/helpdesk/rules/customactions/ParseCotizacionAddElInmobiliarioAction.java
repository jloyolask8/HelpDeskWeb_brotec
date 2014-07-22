/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webservices.DatosCaso;
import java.util.logging.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author jonathan
 */
public class ParseCotizacionAddElInmobiliarioAction extends ParseCotizacionAction {

    public ParseCotizacionAddElInmobiliarioAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    
    private final static String canalId = "addElInmobiliario";
    private final static String canalName = "Add El Inmobiliario";

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException{
        DatosCaso datos = new DatosCaso();
        Document doc = Jsoup.parse(txt);

        try {
            //        String emailCotizanteInput = doc.getElementsContainingOwnText("E-mail:").first().parent().parent().nextElementSibling().text();
            String rutCotizanteInput = doc.getElementsContainingOwnText("Rut:").first().parent().parent().nextElementSibling().text();
            rutCotizanteInput = rutCotizanteInput.trim();
            String rutFormated = UtilesRut.formatear(rutCotizanteInput);
            //        datos.setEmail(emailCotizanteInput);
            datos.setRut(rutFormated);
        } catch (Exception e) {
              throw new ActionExecutionException("gether Rut error", e);
        }

        try {
            String nombreCotizanteInput = doc.getElementsContainingOwnText("Nombre de la persona:").first().parent().parent().nextElementSibling().text();
            datos.parseNombre(nombreCotizanteInput);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Nombre de la persona", e);
        }


        try {
            String proyecto = doc.getElementsContainingOwnText("Nombre proyecto:").first().parent().parent().nextElementSibling().text();
            datos.setProducto(proyecto);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Nombre proyecto", e);
        }

        try {
            String modelo = doc.getElementsContainingOwnText("Propiedad:").first().parent().parent().nextElementSibling().text();
            if (modelo != null) {
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

        System.out.println("DATOS:" + datos);
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
