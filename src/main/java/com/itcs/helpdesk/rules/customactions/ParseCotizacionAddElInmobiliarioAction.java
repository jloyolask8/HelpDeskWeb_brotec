/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.rules.ActionInfo;
import com.itcs.helpdesk.util.HtmlUtils;
import com.itcs.helpdesk.webservices.DatosCaso;

/**
 *
 * @author jonathan
 */
@ActionInfo(name = "Analizar cotización de El Inmobiliario",
        description = "Analiza y extrae información de cotizaciones enviadas desde El Inmobiliario", mustShow = true)
public class ParseCotizacionAddElInmobiliarioAction extends ParseCotizacionAction {

    public ParseCotizacionAddElInmobiliarioAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    
    private final static String canalId = "addElInmobiliario";
    private final static String canalName = "Add El Inmobiliario";

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException{
        DatosCaso datos = new DatosCaso();
        txt = HtmlUtils.extractPlainText(txt);
        extractNombre(datos, txt);
        extractRut(datos, txt);
        extractEmail(txt, datos);
        extractProyecto(datos, txt);
        extractTelefono(datos, txt);
        extractModelo(txt, datos);
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
    
    @Override
    protected boolean complyWithMinimalDataRequirements(DatosCaso datos) {
        return true;
}
}
