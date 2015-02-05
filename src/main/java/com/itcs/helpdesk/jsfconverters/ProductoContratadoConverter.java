/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfconverters;

import com.itcs.helpdesk.jsfcontrollers.CasoController;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author jonathan
 */
@FacesConverter(forClass = ProductoContratado.class)
public class ProductoContratadoConverter implements Converter {

    private static final String SEPARATOR = "#";
    private static final String SEPARATOR_ESCAPED = "\\#";

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        CasoController controller = (CasoController) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "casoController");
        return controller.getJpaController().find(ProductoContratado.class, getKey(value));
    }

    com.itcs.helpdesk.persistence.entities.ProductoContratadoPK getKey(String value) {
        com.itcs.helpdesk.persistence.entities.ProductoContratadoPK key;
        String values[] = value.split(SEPARATOR_ESCAPED);
        key = new com.itcs.helpdesk.persistence.entities.ProductoContratadoPK();
        key.setIdCliente(Integer.parseInt(values[0]));
        key.setIdProducto(values[1]);
        key.setIdComponente(values[2]);
        key.setIdSubComponente(values[3]);
        return key;
    }

    String getStringKey(com.itcs.helpdesk.persistence.entities.ProductoContratadoPK value) {
        StringBuilder sb = new StringBuilder();
        sb.append(value.getIdCliente());
        sb.append(SEPARATOR);
        sb.append(value.getIdProducto());
        sb.append(SEPARATOR);
        sb.append(value.getIdComponente());
        sb.append(SEPARATOR);
        sb.append(value.getIdSubComponente());
        return sb.toString();
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof ProductoContratado) {
            ProductoContratado o = (ProductoContratado) object;
            return getStringKey(o.getProductoContratadoPK());
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ProductoContratado.class.getName());
        }
    }

}
