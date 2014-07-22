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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jorge
 */
public class ParseCotizacionPortalInmobiliarioAltAction extends ParseCotizacionAction {

    public ParseCotizacionPortalInmobiliarioAltAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    
    private final static String canalId = "portalInmobiliario";
    private final static String canalName = "Portal Inmobiliario";

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException{

        DatosCaso datos = new DatosCaso();
        txt = HtmlUtils.extractPlainText(txt);
        try {
            String re1 = "(Cotizante:)(.*?)(\\n)";//This finds the name!
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
            String re1 = "(RUT:)(.*?)(\\n)";
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
            String nombreProyecto = null;
            String re1 = "(Portalinmobiliario.com[\\r\\n]+)(.*?)(\\r\\n)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE );
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

        try {
            String re1 = "(Teléfono celular:)(.*?)(\\n)";
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
            String re1 = "(Modelo de casa, Dormitorios:)(.*?)(,)";
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
    
//    public static void main(String args[])
//    {
//        String test="<div id=\"formint2:panelDescripcion_content\" class=\"ui-panel-content ui-widget-content\"><table id=\"formint2:j_idt122\" class=\"ui-panelgrid ui-widget filtersGrid\" style=\"width: 100%;\" role=\"grid\"><tbody><tr class=\"ui-widget-content ui-panelgrid-even\" role=\"row\"><td role=\"gridcell\">\n" +
//"                                        <div align=\"right\">\n" +
//"                                        </div></td></tr></tbody></table><hr id=\"formint2:j_idt132\" class=\"ui-separator ui-state-default ui-corner-all\"><span style=\"height: 300px;\">\n" +
//"\n" +
//"\n" +
//"    \n" +
//"    \n" +
//"    Portalinmobiliario.com: Cotización en línea Proyecto\n" +
//"\n" +
//"\n" +
//"\n" +
//"\n" +
//"    <table>\n" +
//"        <tbody><tr>\n" +
//"            <td>\n" +
//"                <table>\n" +
//"                    <tbody><tr>\n" +
//"                        <td>\n" +
//"                            <br>\n" +
//"                            <img title=\"Portalinmobiliario.com\"><br>\n" +
//"\n" +
//"                        </td>\n" +
//"                    </tr>\n" +
//"\n" +
//"                    <tr>\n" +
//"                        <td>Ésta puede ser su siguiente venta</td>\n" +
//"\n" +
//"                    </tr>\n" +
//"\n" +
//"                    <tr>\n" +
//"                        <td>\n" +
//"                            <font>Estimado(a): Mirta Leiva Ortiz\n" +
//"<br>\n" +
//"<br>\n" +
//"Un usuario de Portalinmobiliario.com ha cotizado en su proyecto.\n" +
//"Con la siguiente información puede hacer seguimiento y convertir este contacto en su próxima venta.\n" +
//"<br>\n" +
//"<br>\n" +
//"</font>\n" +
//"                            <table>\n" +
//"                                <tbody><tr>\n" +
//"                                    <td><font>Fecha:</font></td>\n" +
//"                                    <td><font>19-06-2014 - Nº: 22520431</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><font>Cotizante:</font></td>\n" +
//"                                    <td><font>Erick Esteban Ordenes Espinoza</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><font>RUT:</font></td>\n" +
//"                                    <td><font>17048656-0</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><font>Dirección:</font></td>\n" +
//"                                    <td><font></font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><font>Comuna:</font></td>\n" +
//"                                    <td><font>Quilicura</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><font>E-mail:</font></td>\n" +
//"                                    <td><font><a href=\"mailto:erick-eoe@hotmail.com\">erick-eoe@hotmail.com</a></font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><font>Teléfono celular:</font></td>\n" +
//"                                    <td><font>9-85307338</font></td>\n" +
//"                                </tr>\n" +
//"                            </tbody></table>\n" +
//"                            <br>\n" +
//"                            <table>\n" +
//"                                <tbody><tr>\n" +
//"                                    <td><font>Características</font></td>\n" +
//"                                    <td><font>Áreas (m2)</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                     <td><font>Casa:</font></td>\n" +
//"                                     <td><font>1</font></td>\n" +
//"                                                                         <td><font>Construida:</font></td>\n" +
//"                                     <td><font>92</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                     <td><font>Modelo de casa, Dormitorios:</font></td>\n" +
//"                                     <td><font>Boyeruca, 3</font></td>\n" +
//"\n" +
//"                                    <td><font>Terreno:</font></td>\n" +
//"                                    <td><font>198</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                     <td><font>Baños:</font></td>\n" +
//"                                     <td><font>3</font></td>\n" +
//"                                                                   </tr>\n" +
//"                            </tbody></table>\n" +
//"                            <br>\n" +
//"                            <table>\n" +
//"                                <tbody><tr>\n" +
//"                                    <td><font>Precio</font></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><font>UF :</font></td>\n" +
//"                                    <td><font>3.255,00</font></td>\n" +
//"                                    <td><font>$ :</font></td>\n" +
//"                                    <td><font>78.360.968</font></td>\n" +
//"                                </tr>\n" +
//"                            </tbody></table>\n" +
//"                            <font><br>\n" +
//"Le  recordamos que un oportuno contacto de este potencial cliente puede mejorar su  gestión de ventas.\n" +
//"<br>\n" +
//"<br>\n" +
//"Los  detalles de la cotización pueden ser vistos en </font>\n" +
//"                            <br>\n" +
//"                            <br>\n" +
//"                            <table>\n" +
//"                                <tbody><tr>\n" +
//"                                    <td><a href=\"http://www.portalinmobiliario.com/MiPortal/MiPortal.aspx\">Mi Portal</a></td>\n" +
//"                                </tr>\n" +
//"                            </tbody></table>\n" +
//"                            <br>\n" +
//"                            <font>            Atentamente<br>\n" +
//"Portalinmobiliario.com</font>\n" +
//"                            <br>\n" +
//"                            <br>\n" +
//"                        </td>\n" +
//"                        <td>\n" +
//"                            <table>\n" +
//"                                <tbody><tr>\n" +
//"                                    <td>\n" +
//"                                        <img></td>\n" +
//"                                </tr>\n" +
//"                                <tr>\n" +
//"                                    <td><a href=\"#\"><font>El Rodeo de Chicauma</font></a>\n" +
//"                                        <br>\n" +
//"                                        <table>\n" +
//"                                            <tbody><tr>\n" +
//"                                                <td><font>Dirección:</font></td>\n" +
//"                                                <td><font>Ruta 5, Km 17, Lampa</font></td>\n" +
//"                                            </tr>\n" +
//"                                            <tr>\n" +
//"                                                <td><font>Teléfono(s):</font></td>\n" +
//"                                                <td><font>2-29527938</font></td>\n" +
//"                                            </tr>\n" +
//"                                        </tbody></table>\n" +
//"                                    </td>\n" +
//"                                </tr>\n" +
//"                            </tbody></table>\n" +
//"                        </td>\n" +
//"                    </tr>\n" +
//"                </tbody></table>\n" +
//"                <table>\n" +
//"                    <tbody><tr>\n" +
//"                            <td>\n" +
//"                                <br>\n" +
//"                                <span>Copyright © 1999-2014 VMK S.A.\n" +
//"                                <br>\n" +
//"                                    Todos los derechos reservados. Prohibida su reproducción total o parcial por cualquier medio.\n" +
//"                                </span>\n" +
//"                            </td>\n" +
//"                    </tr>\n" +
//"                </tbody></table>\n" +
//"            </td>\n" +
//"        </tr>\n" +
//"    </tbody></table>\n" +
//"\n" +
//"\n" +
//"\n" +
//"</span></div>";
//        
//        ParseCotizacionPortalInmobiliarioAltAction parser = new ParseCotizacionPortalInmobiliarioAltAction(null, null);
//        try {
//            parser.collectData(test);
//        } catch (ActionExecutionException ex) {
//            Logger.getLogger(ParseCotizacionPortalInmobiliarioAltAction.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
