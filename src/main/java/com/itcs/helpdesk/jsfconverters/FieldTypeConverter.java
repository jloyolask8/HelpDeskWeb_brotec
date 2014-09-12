/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfconverters;

import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import com.itcs.helpdesk.persistence.entities.FieldType;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author jonathan
 */
@FacesConverter(forClass = FieldType.class)
public class FieldTypeConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        ApplicationBean controller = (ApplicationBean) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "applicationBean");
        return controller.getJpaController().find(FieldType.class, (value));
    }

//    java.lang.String getKey(String value) {
//        java.lang.String key;
//        key = value;
//        return key;
//    }
//
//    String getStringKey(java.lang.String value) {
//        StringBuffer sb = new StringBuffer();
//        sb.append(value);
//        return sb.toString();
//    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof FieldType) {
            FieldType o = (FieldType) object;
            return (o.getFieldTypeId());
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + FieldType.class.getName());
        }
    }
}
