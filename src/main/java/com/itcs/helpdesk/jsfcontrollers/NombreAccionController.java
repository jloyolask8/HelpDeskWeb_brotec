package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.TipoAccion;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "nombreAccionController")
@SessionScoped
public class NombreAccionController extends AbstractManagedBean<TipoAccion> implements Serializable {

    private int selectedItemIndex;

    public NombreAccionController() {
        super(TipoAccion.class);
    }

    public SelectItem[] getItemsAvailableSelectOneImplementingActionClass() {
        List<TipoAccion> lista = new LinkedList<>();
        final List<TipoAccion> findAll = (List<TipoAccion>) getJpaController().findAll(TipoAccion.class);
        for (TipoAccion tipoAccion : findAll) {
            if (tipoAccion.getImplementationClassName() != null) {
                lista.add(tipoAccion);
            }
        }

        return JsfUtil.getSelectItems(lista, true);
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FacesConverter(forClass = TipoAccion.class)
    public static class NombreAccionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "UserSessionBean");
        return controller.getJpaController().find(TipoAccion.class, getKey(value));
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(java.lang.String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof TipoAccion) {
                TipoAccion o = (TipoAccion) object;
                return getStringKey(o.getIdNombreAccion());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + TipoAccion.class.getName());
            }
        }
    }
}

class NombreAccionDataModel extends ListDataModel<TipoAccion> implements SelectableDataModel<TipoAccion> {

    public NombreAccionDataModel() {
        //nothing
    }

    public NombreAccionDataModel(List<TipoAccion> data) {
        super(data);
    }

    @Override
    public TipoAccion getRowData(String rowKey) {
        List<TipoAccion> listOfNombreAccion = (List<TipoAccion>) getWrappedData();

        for (TipoAccion obj : listOfNombreAccion) {
            if (obj.getIdNombreAccion().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(TipoAccion classname) {
        return classname.getIdNombreAccion();
    }
}
