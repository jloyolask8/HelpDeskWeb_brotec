package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.TipoComparacion;
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


@ManagedBean(name = "tipoComparacionController")
@SessionScoped
public class TipoComparacionController extends AbstractManagedBean<TipoComparacion> implements Serializable {

    public TipoComparacionController() {
        super(TipoComparacion.class);
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FacesConverter(forClass = TipoComparacion.class)
    public static class TipoComparacionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            TipoComparacionController controller = (TipoComparacionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "tipoComparacionController");
            return controller.getJpaController().getTipoComparacionFindByIdComparador(value);
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
            if (object instanceof TipoComparacion) {
                TipoComparacion o = (TipoComparacion) object;
                return o.getIdComparador();
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + TipoComparacion.class.getName());
            }
        }
    }
}
class TipoComparacionDataModel extends ListDataModel<TipoComparacion> implements SelectableDataModel<TipoComparacion> {

    public TipoComparacionDataModel() {
        //nothing
    }

    public TipoComparacionDataModel(List<TipoComparacion> data) {
        super(data);
    }

    @Override
    public TipoComparacion getRowData(String rowKey) {
        List<TipoComparacion> listOfTipoComparacion = (List<TipoComparacion>) getWrappedData();

        for (TipoComparacion obj : listOfTipoComparacion) {
            if (obj.getIdComparador().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(TipoComparacion classname) {
        return classname.getIdComparador();
    }
}
