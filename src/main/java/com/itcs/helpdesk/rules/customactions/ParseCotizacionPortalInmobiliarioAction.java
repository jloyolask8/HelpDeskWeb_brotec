/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.rules.ActionInfo;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webservices.DatosCaso;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author jonathan
 */
@ActionInfo(name = "Analizar cotización de Portal Inmobiliario",
        description = "Analiza y extrae información de cotizaciones enviadas desde Portal Inmobiliario", mustShow = true)
public class ParseCotizacionPortalInmobiliarioAction extends ParseCotizacionAction {

    public ParseCotizacionPortalInmobiliarioAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    private final static String canalId = "portalInmobiliario";
    private final static String canalName = "Portal Inmobiliario";

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException {
        DatosCaso datos = new DatosCaso();
        Document doc = Jsoup.parse(txt);

        try {
            String emailCotizanteInput = doc.getElementsContainingOwnText("E-mail:").first().parent().nextElementSibling().text();

            if (emailCotizanteInput != null) {
                datos.setEmail(emailCotizanteInput);
            } else {

                throw new Exception("no email no caso");

            }

        } catch (Exception e) {
            throw new ActionExecutionException("gether E-mail error, abortar collectData", e);
        }

        try {
            String rutCotizanteInput = doc.getElementsContainingOwnText("RUT:").first().parent().nextElementSibling().text();
            rutCotizanteInput = rutCotizanteInput.trim();
            String rutFormated = UtilesRut.formatear(rutCotizanteInput);
            datos.setRut(rutFormated);
        } catch (Exception e) {
            throw new ActionExecutionException("gether RUT error, abortar collectData", e);
        }

        try {
            String nombreCotizanteInput = doc.getElementsContainingOwnText("Cotizante:").first().parent().parent().nextElementSibling().text();
            datos.parseNombre(nombreCotizanteInput);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Cotizante", e);
        }

//        try {
//            String direccionCotizanteInput = doc.getElementsContainingOwnText("Dirección").first().parent().nextElementSibling().text();
//            //datos.setComuna(direccionCotizanteInput);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            String comunaCotizanteInput = doc.getElementsContainingOwnText("Comuna").first().parent().nextElementSibling().text();
            datos.setComuna(comunaCotizanteInput);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Comuna", e);
        }

        try {
            String fonoCotizanteInput = doc.getElementsContainingOwnText("Teléfono").first().parent().nextElementSibling().text();
            datos.setTelefono(fonoCotizanteInput);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Teléfono", e);
        }

        try {

            final String modeloInfo = doc.getElementsContainingOwnText("Modelo").first().parent().nextElementSibling().text();
            if (ApplicationConfig.isAppDebugEnabled()) {
                System.out.println("modelo:" + modeloInfo);
            }
            String[] parts = modeloInfo.trim().split(",");
            if (parts != null && parts.length > 0) {
                String modelo = parts[0].trim();
                if (modelo != null && !StringUtils.isEmpty(modelo)) {
                    modelo = modelo.replace("Casa", "");
                    modelo = modelo.replace("Departamento", "");
                    modelo = modelo.replace("Depto", "");
                    modelo = modelo.replace("Dpto", "");
                    modelo = modelo.replace("Oficina", "");
                    modelo = modelo.trim();
                    datos.setModelo(modelo);
                }
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Modelo", e);
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
