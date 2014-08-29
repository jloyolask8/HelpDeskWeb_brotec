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
 * @author jorge
 */
@ActionInfo(name = "Analizar cotización de Enlace Inmobiliario",
        description = "Analiza y extrae información de cotizaciones enviadas desde Enlace Inmobiliario", mustShow = true)
public class ParseCotizacionEnlaceInmobiliarioAction extends ParseCotizacionAction {

    public ParseCotizacionEnlaceInmobiliarioAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    
    private final static String canalId = "EnlaceInmobiliario";
    private final static String canalName = "Enlace Inmobiliario";

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
    
//    public static void main(String args[])
//    {
//        String textToTest = "<div id=\"formint2:panelDescripcion_content\" class=\"ui-panel-content ui-widget-content\"><table id=\"formint2:j_idt123\" class=\"ui-panelgrid ui-widget filtersGrid\" style=\"width: 100%;\" role=\"grid\"><tbody><tr class=\"ui-widget-content ui-panelgrid-even\" role=\"row\"><td role=\"gridcell\">\n" +
//"                                        <div align=\"right\">\n" +
//"                                        </div></td></tr></tbody></table><hr id=\"formint2:j_idt133\" class=\"ui-separator ui-state-default ui-corner-all\"><span style=\"height: 300px;\"><div><b>Cotizacion en Enlace BancoEstado</b><img></div>\n" +
//"<p>\n" +
//"	Inmobiliaria Brotec Icafal</p>\n" +
//"<p>\n" +
//"	Estimado(a) Ejecutivo(a):</p>\n" +
//"<p>\n" +
//"	Le informamos que el día Lunes 23 de Junio de 2014 a las 21:50 hrs se ha emitido la cotización Nº 1166558 por la propiedad Departamento I del proyecto Palmas de Tocornal, publicado en el portal <a href=\"http://mandrillapp.com/track/click.php?u=30184949&amp;id=53842650e3fb4b79b46ea0c4db8bfd10&amp;url=http%3A%2F%2Fbancoestado.enlacesinmobiliarios.cl&amp;url_id=98edcd0b7e1e540ad78dd782a083b56d52b2bdd8\">bancoestado.enlacesinmobiliarios.cl</a></p>\n" +
//"<p>\n" +
//"	Datos del Cotizante:</p>\n" +
//"<p>\n" +
//"	<font class=\"Apple-style-span\"><span class=\"Apple-style-span\"><span>Nombre: RONIE CARTAGENA</span></span></font></p>\n" +
//"<p>\n" +
//"	<font class=\"Apple-style-span\"><span class=\"Apple-style-span\"><span>Rut: 12622699-3</span></span></font></p>\n" +
//"<p>\n" +
//"	<font class=\"Apple-style-span\"><span class=\"Apple-style-span\"><span>Email: RONIE.CARTAGENA@GMAIL.COM</span></span></font></p>\n" +
//"<p>\n" +
//"	<font class=\"Apple-style-span\"><span class=\"Apple-style-span\"><span>Proyecto: Palmas de Tocornal</span></span></font></p>\n" +
//"<p>\n" +
//"	<font class=\"Apple-style-span\"><span class=\"Apple-style-span\"><span>Unidad: Departamento I</span></span></font></p>\n" +
//"<p>\n" +
//"	Precio Unidad UF: 2585</p>\n" +
//"<p>\n" +
//"	<span class=\"Apple-style-span\">Teléfono Particular: 2-</span></p>\n" +
//"<p>\n" +
//"	<font class=\"Apple-style-span\"><span class=\"Apple-style-span\"><span></span></span></font></p>\n" +
//"<p>\n" +
//"	<span class=\"Apple-style-span\">Teléfono Comercial: 2-</span></p>\n" +
//"<p>\n" +
//"	Teléfono Movil: 9-7415068</p>\n" +
//"<p>\n" +
//"	 </p>\n" +
//"<p>\n" +
//"	<span class=\"Apple-style-span\">Para revisar dicho documento debe acceder con sus datos de usuario a la plataforma \"enlace gestión\" </span><span class=\"Apple-style-span\"><a href=\"http://mandrillapp.com/track/click.php?u=30184949&amp;id=53842650e3fb4b79b46ea0c4db8bfd10&amp;url=http%3A%2F%2Fgestion.enlaceinmobiliario.cl&amp;url_id=04b3ace15792bab58306788c9d980f0fa8289f74\">gestion.enlaceinmobiliario.cl</a></span><span class=\"Apple-style-span\">, sección cotizaciones.</span></p>\n" +
//"<p>\n" +
//"	Cualquier duda, por favor comuníquese con el ejecutivo que le hemos asignado.</p>\n" +
//"<p>\n" +
//"	 </p>\n" +
//"<p>\n" +
//"	Le saluda cordialmente</p>\n" +
//"<p>\n" +
//"	Enlace Inmobiliario S.A.<br>\n" +
//"	info@enlaceinmobiliario.cl</p>\n" +
//"\n" +
//"\n" +
//"\n" +
//"<img></span></div>";
//        
//        ParseCotizacionEnlaceInmobiliarioAction parser = new ParseCotizacionEnlaceInmobiliarioAction(null);
//        try
//        {
//            DatosCaso datos = parser.collectData(textToTest);
//            System.out.println("datos:"+datos);
//        }
//        catch (ActionExecutionException ex)
//        {
//            Logger.getLogger(ParseCotizacionEnlaceInmobiliarioAction.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
