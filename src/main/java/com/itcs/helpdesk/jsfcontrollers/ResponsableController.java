package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Responsable;
import java.io.Serializable;
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


@ManagedBean(name = "responsableController")
@SessionScoped
public class ResponsableController extends AbstractManagedBean<Responsable> implements Serializable {

    public ResponsableController() {
        super(Responsable.class);
    }

    @Override
    public Class getDataModelImplementationClass() {
        return ResponsableDataModel.class;
    }

    @Override
    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(getJpaController().findAll(Responsable.class), false);
    }

    @Override
    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(getJpaController().findAll(Responsable.class), true);
    }

    @FacesConverter(forClass = Responsable.class)
    public static class ResponsableControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ResponsableController controller = (ResponsableController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "responsableController");
            return controller.getJpaController().find(Responsable.class, getKey(value));
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
            if (object instanceof Responsable) {
                Responsable o = (Responsable) object;
                return getStringKey(o.getIdResponsable());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Responsable.class.getName());
            }
        }
    }
}
class ResponsableDataModel extends ListDataModel<Responsable> implements SelectableDataModel<Responsable> {

    public ResponsableDataModel() {
        //nothing
    }

    public ResponsableDataModel(List<Responsable> data) {
        super(data);
    }

    @Override
    public Responsable getRowData(String rowKey) {
        List<Responsable> listOfResponsable = (List<Responsable>) getWrappedData();

        for (Responsable obj : listOfResponsable) {
            if (obj.getIdResponsable() == Integer.parseInt(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Responsable classname) {
        return classname.getIdResponsable();
    }
}
