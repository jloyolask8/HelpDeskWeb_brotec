/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import com.itcs.helpdesk.persistence.entities.Caso;
import java.beans.Expression;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.text.WordUtils;

/**
 *
 * @author jonathan
 */
public class ClippingsPlaceHolders {

    private static final Properties placeHolders;
    //--
    public static final String FechaCreacion = "fechaCreacion";
    public static final String descripcion = "descripcion";
    public static final String asunto = "tema";
    public static final String NumeroCaso = "idCaso";
    public static final String NombreCliente = "emailCliente.cliente.capitalName";
    public static final String EmailCliente = "emailCliente.emailCliente";
    public static final String NombreAgente = "owner.capitalName";
    public static final String FirmaAgente = "owner.firma";
    public static final String EmailAgente = "owner.email";
    public static final String EstadoAlerta = "estadoAlerta.nombre";
    public static final String SubEstado = "idSubEstado.nombre";
    public static final String Producto = "idProducto.nombre";
    public static final String Prioridad = "idPrioridad.nombre";
    public static final String Canal = "idCanal.nombre";
    public static final String tipoCaso = "tipoCaso.nombre";
    //--
    public static final String SALUDO_CLIENTE = "SaludoCliente";
    public static final String GODESK_CONTEXT_URL = "ContextUrl";
    public static final String NUMERO_CASO = "NumeroCaso";

    static {
        placeHolders = new Properties();
        placeHolders.put(FechaCreacion, "FechaCreacion");
        placeHolders.put(NumeroCaso, NUMERO_CASO);
        placeHolders.put(NombreCliente, "NombreCliente");
        placeHolders.put(EmailCliente, "EmailCliente");
        placeHolders.put(NombreAgente, "NombreAgente");
        placeHolders.put(EmailAgente, "EmailAgente");
        placeHolders.put(FirmaAgente, "FirmaAgente");
        placeHolders.put(EstadoAlerta, "EstadoAlerta");
        placeHolders.put(SubEstado, "SubEstado");
        placeHolders.put(Producto, "Producto");
        placeHolders.put(Prioridad, "Prioridad");
        placeHolders.put(Canal, "Canal");
        placeHolders.put(tipoCaso, "TipoCaso");
        placeHolders.put(asunto, "Asunto");
        placeHolders.put(descripcion, "Descripcion");

    }

    public static Collection<Object> getAvailablePlaceHolders() {
        final Collection<Object> values = placeHolders.values();
//        values.add(SALUDO_CLIENTE);
        return values;
    }

    public static String buildFinalText(String templateString, Caso caso) {
//        System.out.println("templateString:" + templateString);
        Map<String, String> valuesMap = new HashMap<>();
        for (Object key : placeHolders.keySet()) {
            valuesMap.put(placeHolders.getProperty((String) key), StringUtils.defaultString(String.valueOf(getValueObjectFor((String) key, caso))));
        }

        try {
            final String saludoH = ApplicationConfig.getSaludoClienteHombre();
            final String saludoM = ApplicationConfig.getSaludoClienteMujer();
            valuesMap.put(SALUDO_CLIENTE, (caso.getEmailCliente().getCliente().getSexo().equalsIgnoreCase("Hombre") ? saludoH : saludoM));
        } catch (Exception e) {
            valuesMap.put(SALUDO_CLIENTE, ApplicationConfig.getSaludoClienteUnknown());
        }
        
        valuesMap.put(GODESK_CONTEXT_URL, ApplicationConfig.getCompanyContextURL());

        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        final String replace = sub.replace(templateString);
//        System.out.println("Text:" + replace);
        return replace;
    }

    public static Object getValueObjectFor(String placeHolderKey, Object fromObject) {
        try {

            if (fromObject == null) {
                return null;
            }

            if (placeHolderKey == null) {
                return null;
            }

            if (placeHolderKey.contains(".")) {
                //
                String[] parts = placeHolderKey.split("\\.");
                Object base = fromObject;
                for (String property : parts) {
                    base = getValueObjectFor(property, base);
                }
                return base;

            } else {
                String methodName = "get" + WordUtils.capitalize(placeHolderKey);
                Expression expresion = new Expression(fromObject, methodName, new Object[0]);
                expresion.execute();
                final Object value = expresion.getValue();
                return value;
            }

        } catch (Exception ex) {
            Logger.getLogger(ClippingsPlaceHolders.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
