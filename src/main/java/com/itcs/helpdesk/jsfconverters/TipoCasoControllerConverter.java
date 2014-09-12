/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfconverters;

import com.itcs.helpdesk.jsfcontrollers.TipoCasoController;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author jonathan
 */
@FacesConverter(forClass = TipoCaso.class, value="TipoCasoControllerConverter")
public class TipoCasoControllerConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        TipoCasoController controller = (TipoCasoController) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "tipoCasoController");
        return controller.getJpaController().find(TipoCaso.class, getKey(value));
    }

    java.lang.String getKey(String value) {
        java.lang.String key;
        key = value;
        return key;
    }

    String getStringKey(java.lang.String value) {
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        return sb.toString();
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof TipoCaso) {
            TipoCaso o = (TipoCaso) object;
            return getStringKey(o.getIdTipoCaso());
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + TipoCaso.class.getName());
        }
    }
}