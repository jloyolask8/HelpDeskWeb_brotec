package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.TipoAlerta;
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


@ManagedBean(name = "tipoAlertaController")
@SessionScoped
public class TipoAlertaController extends AbstractManagedBean<TipoAlerta> implements Serializable {

    public TipoAlertaController() {
        super(TipoAlerta.class);
    }   

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FacesConverter(forClass = TipoAlerta.class)
    public static class TipoAlertaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            TipoAlertaController controller = (TipoAlertaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "tipoAlertaController");
            return controller.getJpaController().getTipoAlertaFindByIdTipoAlerta(getKey(value));
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
            if (object instanceof TipoAlerta) {
                TipoAlerta o = (TipoAlerta) object;
                return getStringKey(o.getIdalerta());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + TipoAlerta.class.getName());
            }
        }
    }
}
class TipoAlertaDataModel extends ListDataModel<TipoAlerta> implements SelectableDataModel<TipoAlerta> {

    public TipoAlertaDataModel() {
        //nothing
    }

    public TipoAlertaDataModel(List<TipoAlerta> data) {
        super(data);
    }

    @Override
    public TipoAlerta getRowData(String rowKey) {
        List<TipoAlerta> list = (List<TipoAlerta>) getWrappedData();

        for (TipoAlerta obj : list) {
            if (obj.getIdalerta().equals(Integer.valueOf(rowKey))) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(TipoAlerta classname) {
        return classname.getIdalerta();
    }
}