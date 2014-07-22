/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfconverters;

import com.itcs.helpdesk.jsfcontrollers.FuncionController;
import com.itcs.helpdesk.persistence.entities.Funcion;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author jonathan
 */
@FacesConverter(forClass = Funcion.class)
public class FuncionConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
//        final JPAServiceFacade jpaController = new JPAServiceFacade();
//        return jpaController.getFuncionFindByIdFuncion(getKey(value));

            FuncionController controller = (FuncionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "funcionController");
            return controller.getJpaController().getFuncionFindByIdFuncion(getKey(value));
    }

    java.lang.Integer getKey(String value) {
        java.lang.Integer key;
        key = Integer.valueOf(value);
        return key;
    }

    String getStringKey(java.lang.Integer value) {
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        return sb.toString();
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Funcion) {
            Funcion o = (Funcion) object;
            return getStringKey(o.getIdFuncion());
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + FuncionController.class.getName());
        }
    }
}
