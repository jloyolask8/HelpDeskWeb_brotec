package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.SubComponente;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;


@ManagedBean(name = "subComponenteController")
@SessionScoped
public class SubComponenteController extends AbstractManagedBean<SubComponente> implements Serializable {


    public SubComponenteController() {
        super(SubComponente.class);
    }
    
    public void destroy(SubComponente item) {
        try {
            getJpaController().remove(SubComponente.class, item);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SubComponenteDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FacesConverter(forClass = SubComponente.class)
    public static class SubComponenteControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            SubComponenteController controller = (SubComponenteController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "subComponenteController");
            return controller.getJpaController().getSubComponenteFindByIdSubComponente(getKey(value));
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
            if (object instanceof SubComponente) {
                SubComponente o = (SubComponente) object;
                return getStringKey(o.getIdSubComponente());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + SubComponente.class.getName());
            }
        }
    }
}
class SubComponenteDataModel extends ListDataModel<SubComponente> implements SelectableDataModel<SubComponente> {

    public SubComponenteDataModel() {
        //nothing
    }

    public SubComponenteDataModel(List<SubComponente> data) {
        super(data);
    }

    @Override
    public SubComponente getRowData(String rowKey) {
        List<SubComponente> listOfSubComponente = (List<SubComponente>) getWrappedData();

        for (SubComponente obj : listOfSubComponente) {
            if (obj.getIdSubComponente().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(SubComponente classname) {
        return classname.getIdSubComponente();
    }
}
