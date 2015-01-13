package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Accion;
import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;


@ManagedBean(name = "accionController")
@SessionScoped
public class AccionController extends AbstractManagedBean<Accion>  implements Serializable {


    public AccionController() {
        super(Accion.class);
    }

    @Override
    public Class getDataModelImplementationClass() {
        return AccionDataModel.class;
    }

    @FacesConverter(forClass = Accion.class)
    public static class AccionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "UserSessionBean");
        return controller.getJpaController().find(Accion.class, getKey(value));
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
            if (object instanceof Accion) {
                Accion o = (Accion) object;
                return getStringKey(o.getIdAccion());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Accion.class.getName());
            }
        }
    }
}
class AccionDataModel extends ListDataModel<Accion> implements SelectableDataModel<Accion> {

    public AccionDataModel() {
        //nothing
    }

    public AccionDataModel(List<Accion> data) {
        super(data);
    }

    @Override
    public Accion getRowData(String rowKey) {
        List<Accion> listOfAccion = (List<Accion>) getWrappedData();

        for (Accion obj : listOfAccion) {
            if (obj.getIdAccion().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Accion classname) {
        return classname.getIdAccion();
    }
}
