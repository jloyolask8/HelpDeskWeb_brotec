package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.persistence.entities.EstadoCaso;
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


@ManagedBean(name = "estadoCasoController")
@SessionScoped
public class EstadoCasoController extends AbstractManagedBean<EstadoCaso> implements Serializable {

    public EstadoCasoController() {
        super(EstadoCaso.class);
    }

    @Override
    public Class getDataModelImplementationClass() {
        return EstadoCasoDataModel.class;
    }

    @FacesConverter(forClass = EstadoCaso.class)
    public static class EstadoCasoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EstadoCasoController controller = (EstadoCasoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "estadoCasoController");
            return controller.getJpaController().getReference(EstadoCaso.class, getKey(value));
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
            if (object instanceof EstadoCaso) {
                EstadoCaso o = (EstadoCaso) object;
                return getStringKey(o.getIdEstado());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + EstadoCasoController.class.getName());
            }
        }
    }
}
class EstadoCasoDataModel extends ListDataModel<EstadoCaso> implements SelectableDataModel<EstadoCaso> {

    public EstadoCasoDataModel() {
        //nothing
    }

    public EstadoCasoDataModel(List<EstadoCaso> data) {
        super(data);
    }

    @Override
    public EstadoCaso getRowData(String rowKey) {
        List<EstadoCaso> listOfEstadoCaso = (List<EstadoCaso>) getWrappedData();

        for (EstadoCaso obj : listOfEstadoCaso) {
            if (obj.getIdEstado().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(EstadoCaso classname) {
        return classname.getIdEstado();
    }
}