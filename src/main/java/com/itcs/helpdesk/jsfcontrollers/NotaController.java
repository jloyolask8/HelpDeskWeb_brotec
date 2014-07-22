package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Nota;
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


@ManagedBean(name = "notaController")
@SessionScoped
public class NotaController extends AbstractManagedBean<Nota> implements Serializable {

    public NotaController() {
        super(Nota.class);
    }  

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }  

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @FacesConverter(forClass = Nota.class)
    public static class NotaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            NotaController controller = (NotaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "notaController");
            return controller.getJpaController().getNotaFindByIdNota(getKey(value));
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
            if (object instanceof Nota) {
                Nota o = (Nota) object;
                return getStringKey(o.getIdNota());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + NotaController.class.getName());
            }
        }
    }
}
class NotaDataModel extends ListDataModel<Nota> implements SelectableDataModel<Nota> {

    public NotaDataModel() {
        //nothing
    }

    public NotaDataModel(List<Nota> data) {
        super(data);
    }

    @Override
    public Nota getRowData(String rowKey) {
        List<Nota> listOfNota = (List<Nota>) getWrappedData();

        for (Nota obj : listOfNota) {
            if (obj.getIdNota().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Nota classname) {
        return classname.getIdNota();
    }
}