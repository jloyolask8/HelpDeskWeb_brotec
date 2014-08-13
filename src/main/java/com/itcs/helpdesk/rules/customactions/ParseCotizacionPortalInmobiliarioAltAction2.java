/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.util.HtmlUtils;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webservices.DatosCaso;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jorge
 */
public class ParseCotizacionPortalInmobiliarioAltAction2 extends ParseCotizacionAction {

    public ParseCotizacionPortalInmobiliarioAltAction2(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    
    private final static String canalId = "portalInmobiliario";
    private final static String canalName = "Portal Inmobiliario";

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException{

        DatosCaso datos = new DatosCaso();
        txt = HtmlUtils.extractPlainText(txt);
        try {
            String re1 = "(Nombre)(.*?)(\\n)";//This finds the name!
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                //String int1 = m.group(2);                  
                datos.parseNombre(string1.toString().trim());
            }
//                System.out.println("nombreCotizanteInput:" + nombreCotizanteInput);
        } catch (Exception e) {
             Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer nombre del solicitante", e);
        }

        try {
            String re1 = "(RUT)(.*?)(\\n)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                datos.setRut(UtilesRut.formatear(string1.replace(":", "").trim()));
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
                datos.setEmail(string1.trim());
            }
//                System.out.println("emailCotizanteInput:" + emailCotizanteInput);
        } catch (Exception e) {
              throw new ActionExecutionException("gether E-mail error", e);
//            e.printStackTrace();
        }
        
        try {
            String nombreProyecto = null;
            String re1 = "(Proyecto)(.*?)(\\r\\nProducto)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE );
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
                //String int1 = m.group(2);
                nombreProyecto = string1.replace(":", "");
                nombreProyecto = nombreProyecto.trim();
                datos.setProducto(nombreProyecto);
            } else {
                Log.createLogger(this.getClass().getName()).logWarning("nombreProyecto no encontrado!");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer nombreProyecto", e);
        }

        try {
            String re1 = "(Teléfono celular)(.*?)(\\n)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
//                    fonoCotizanteInput = string1.toString().trim();
                datos.setTelefono(string1.replace(":", "").trim());
            }
//                System.out.println("fonoCotizanteInput:" + fonoCotizanteInput);
        } catch (Exception e) {
          Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Teléfono", e);
        }
        
        try {
            String re1 = "(Teléfono particular)(.*?)(\\n)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
//                    fonoCotizanteInput = string1.toString().trim();
                datos.setTelefono2(string1.replace(":", "").trim());
            }
//                System.out.println("fonoCotizanteInput:" + fonoCotizanteInput);
        } catch (Exception e) {
          Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Teléfono", e);
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
    
//    public static void main(String args[])
//    {
//        String test="    Portalinmobiliario.com: Cotizaciones en línea\n" +
//"    \n" +
//"    \n" +
//"        a:link {\n" +
//"            color: #000080;\n" +
//"            text-decoration: none;\n" +
//"        }\n" +
//"\n" +
//"        a:visited {\n" +
//"            color: #000080;\n" +
//"            text-decoration: none;\n" +
//"        }\n" +
//"\n" +
//"        a:hover {\n" +
//"            color: #034A89;\n" +
//"            text-decoration: underline;\n" +
//"        }\n" +
//"\n" +
//"        a:active {\n" +
//"            color: #034A89;\n" +
//"            text-decoration: none;\n" +
//"        }\n" +
//"    \n" +
//"\n" +
//"\n" +
//"    <div>\n" +
//"        <center>\n" +
//"<p><a href=\"http://www.portalinmobiliario.com/\" title=\"www.portalinmobiliario.com\" target=\"_blank\"><img title=\"www.portalinmobiliario.com\"></a></p>\n" +
//"<table>\n" +
//"<tbody><tr><td><font>COTIZACIONES EN LÍNEA</font></td></tr>\n" +
//"<tr><td><p><font>La persona cuyos datos aparecen a continuación ha hecho una cotización en el proyecto Santa María del Peñón en Portalinmobiliario.com.</font></p>\n" +
//"<table>\n" +
//"<tbody><tr>\n" +
//"<td><font>DATOS DEL SOLICITANTE</font></td>\n" +
//"</tr>\n" +
//"<tr>\n" +
//"													<td><font>Nombre</font></td>\n" +
//"													<td><font>carla daniella campodonico farias</font></td>\n" +
//"													</tr>\n" +
//"<tr>\n" +
//"											<td><font>RUT</font></td>\n" +
//"											<td><font>128766812</font></td>\n" +
//"											</tr>\n" +
//"\n" +
//"<tr>\n" +
//"													<td><font>Comuna</font></td>\n" +
//"													<td><font>Ñuñoa</font></td>\n" +
//"													</tr>\n" +
//"<tr>\n" +
//"<td><font>Teléfono celular</font></td>\n" +
//"<td><font></font></td>\n" +
//"</tr>\n" +
//"<tr>\n" +
//"<td><font>Teléfono particular</font></td>\n" +
//"<td><font>8-2718357</font></td>\n" +
//"</tr>\n" +
//"<tr>\n" +
//"<td><font>Teléfono comercial</font></td>\n" +
//"<td><font></font></td>\n" +
//"</tr>\n" +
//"<tr>\n" +
//"<td><font>E-mail</font></td>\n" +
//"<td><font>catouc11@yahoo.es</font></td>\n" +
//"</tr>\n" +
//"<tr>\n" +
//"<td><font>Proyecto</font></td>\n" +
//"<td><font>Santa María del Peñón</font></td>\n" +
//"</tr>\n" +
//"<tr>\n" +
//"<td><font>Producto N°</font></td>\n" +
//"<td><font>3</font></td>\n" +
//"</tr>\n" +
//"</tbody></table>\n" +
//"<p><font>Le recordamos que un oportuno contacto de este potencial cliente puede mejorar su gestión de ventas.\n" +
//"<br><br>Recuerde que los detalles de la cotización pueden ser vistos en Mi Portal de <a href=\"http://www.portalinmobiliario.com\">Portalinmobiliario.com</a>.\n" +
//" Además, puede incorporar la cotización y los datos del cliente a Edigraph® usando el software Edisinc de VMK S.A.</font></p>\n" +
//"<p><font>Cotización ID: 22453825</font></p>\n" +
//"<p><font>Atentamente</font>,<br>\n" +
//"<font><a href=\"http://www.portalinmobiliario.com/\" title=\"www.portalinmobiliario.com\" target=\"_blank\">Portalinmobiliario.com</a></font><br>\n" +
//"</p>\n" +
//"<p></p></td>\n" +
//"</tr>\n" +
//"</tbody></table>\n" +
//"</center>\n" +
//"    </div>\n" +
//"\n" +
//"";
//        
//        ParseCotizacionPortalInmobiliarioAltAction2 parser = new ParseCotizacionPortalInmobiliarioAltAction2(null);
//        try {
//            parser.collectData(test);
//        } catch (ActionExecutionException ex) {
//            Logger.getLogger(ParseCotizacionPortalInmobiliarioAltAction.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
