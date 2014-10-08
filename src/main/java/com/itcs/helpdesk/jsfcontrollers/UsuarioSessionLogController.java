/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.persistence.entities.UsuarioSessionLog;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "usuarioSessionLogController")
@SessionScoped
public class UsuarioSessionLogController extends AbstractManagedBean<UsuarioSessionLog> implements Serializable {
    
    public UsuarioSessionLogController(Class<UsuarioSessionLog> entityClass) {
        super(entityClass);
    }

    public UsuarioSessionLogController() {
        super(UsuarioSessionLog.class);
    }
    
    

    @Override
    public Class getDataModelImplementationClass() {
        return ListDataModel.class;
    }

    @Override
    protected String getListPage() {
        return "/script/sessions/List";
    }

    @FacesConverter(forClass = UsuarioSessionLog.class)
    public static class UsuarioSessionLogControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UsuarioSessionLogController controller = (UsuarioSessionLogController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "usuarioSessionLogController");
            return controller.getJpaController().find(UsuarioSessionLog.class, getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof UsuarioSessionLog) {
                UsuarioSessionLog o = (UsuarioSessionLog) object;
                return getStringKey(o.getIdSessionLog());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + UsuarioSessionLog.class.getName());
            }
        }

    }
}
