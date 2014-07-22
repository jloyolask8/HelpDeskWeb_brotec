/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfconverters;

import com.itcs.helpdesk.jsfcontrollers.RolController;
import com.itcs.helpdesk.persistence.entities.Rol;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author jonathan
 */
@FacesConverter(forClass = Rol.class)
public class RolConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
//          final JPAServiceFacade jpaController = new JPAServiceFacade();
//        return jpaController.getRolFindByIdRol(getKey(value));
        
        RolController controller = (RolController) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "rolController");
        return controller.getJpaController().getRolFindByIdRol(getKey(value));
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

    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Rol) {
            Rol o = (Rol) object;
            return getStringKey(o.getIdRol());
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + RolController.class.getName());
        }
    }
}
