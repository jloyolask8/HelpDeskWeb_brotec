/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfconverters;

import java.util.HashMap;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;

/**
 *
 * @author jonathan
 */
@FacesConverter("entityConverter")
public class EntityConverter implements Converter {

    private static final String converterKey = "com.itcs.helpdesk.jsfconverters.EntityConverter";
    private static final String empty = "";

    private Map<String, SelectItem> getViewMap(FacesContext context) {
        Map<String, Object> viewMap = context.getViewRoot().getViewMap();
//        //System.out.println("*********** viewMap:" + viewMap);
//        @SuppressWarnings({"unchecked", "rawtypes"})
        Map<String, SelectItem> idMap = (Map) viewMap.get(converterKey);
        if (idMap == null) {
            idMap = new HashMap<>();
            viewMap.put(converterKey, idMap);
        }
//        //System.out.println("*********** idMap:" + idMap);
        return idMap;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent c, String value) {
        //System.out.println("\n\n\nEntityConverter.getAsObject:" + value);
        if (value.isEmpty()) {
            return null;
        }
        try {
            final SelectItem selectItem = (SelectItem) getViewMap(context).get(value);
            if (selectItem != null) {
                //System.out.println("\n\n\n" + selectItem + " of class " + selectItem.getClass().getName() + " AsObject:" + selectItem);
                return selectItem;
            } else {
                //System.out.println("" + value + " is not in the map.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public String getAsString(FacesContext context, UIComponent c, Object value) {

        //System.out.println("\n\n\nEntityConverter.getAsString:" + value);
        if (value == null) {
            return empty;
        }

        try {
            final SelectItem selectItem = (SelectItem) value;
            
            final String key = ((selectItem.getValue() == null)?empty:selectItem.getValue().toString());
            final String label = selectItem.getLabel();
            //System.out.println(key + "#" + label);
//            //System.out.println("\n\n\nEntityConverter.getAsString:" + key + " of class " + value.getClass().getName() + " = " + label);
            if (label != null) {
                getViewMap(context).put(label, selectItem);
            }
            return label;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }

//        return value.toString();
    }
}
