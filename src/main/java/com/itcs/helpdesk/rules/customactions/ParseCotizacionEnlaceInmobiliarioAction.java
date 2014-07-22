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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jorge
 */
public class ParseCotizacionEnlaceInmobiliarioAction extends ParseCotizacionAction {

    public ParseCotizacionEnlaceInmobiliarioAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }
    
    private final static String canalId = "EnlaceInmobiliario";
    private final static String canalName = "Enlace Inmobiliario";

    @Override
    protected DatosCaso collectData(String txt) throws ActionExecutionException{

        DatosCaso datos = new DatosCaso();

        try {
            String re1 = "(Nombre:)(.*?)(<)";//This finds the name!
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
            String re1 = "(Rut:)(.*?)(<)";
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
            String re1 = "(Proyecto:)(.*?)(<)";
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

        try {
            String re1 = "(Teléfono Particular:)(.*?)(<)";
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
            String re1 = "(Teléfono Movil:)(.*?)(<)";
            Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                String string1 = m.group(2);
//                    fonoCotizanteInput = string1.toString().trim();
                datos.setTelefono2(string1.toString().trim());
            }
//                System.out.println("fonoCotizanteInput:" + fonoCotizanteInput);
        } catch (Exception e) {
          Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer Teléfono", e);
        }

        try {
            String modelo = null;
            String re1 = "(Unidad: )(.*?)(<)";
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
