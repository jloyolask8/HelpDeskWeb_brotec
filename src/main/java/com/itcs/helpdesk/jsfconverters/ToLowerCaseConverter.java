/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfconverters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author jorge
 */
@FacesConverter("toLowerCaseConverter")
public class ToLowerCaseConverter implements Converter {

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object modelValue) {
        if (!(modelValue instanceof String)) { 
            return null;
        }

        return ((String)modelValue).toLowerCase();
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String submittedValue) {
        if (submittedValue == null) { 
            return null;
        }

        return submittedValue.toLowerCase();
    }

}