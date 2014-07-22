package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.NombreAccion;
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
public class NombreAccionController extends AbstractManagedBean<NombreAccion> implements Serializable {

//    private NombreAccion current;
//    private NombreAccion[] selectedItems;  
    private int selectedItemIndex;

    public NombreAccionController() {
        super(NombreAccion.class);
    }

 public SelectItem[] getItemsAvailableSelectOneImplementingActionClass() {
        List<NombreAccion> lista = new LinkedList<NombreAccion>();
        final List<NombreAccion> findAll = (List<NombreAccion>)getJpaController().findAll(NombreAccion.class);
        for (NombreAccion tipoAccion : findAll) {
            if(tipoAccion.getImplementationClassName() != null){
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


//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//        return items;
//    }

//    /**
//     * @return the selectedItems
//     */
//    public NombreAccion[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(NombreAccion[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }
//
//  
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getNombreAccionFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getNombreAccionFindAll(), true);
//    }

    @FacesConverter(forClass = NombreAccion.class)
    public static class NombreAccionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            NombreAccionController controller = (NombreAccionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "nombreAccionController");
            return controller.getJpaController().getNombreAccionFindByIdNombreAccion(getKey(value));
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
            if (object instanceof NombreAccion) {
                NombreAccion o = (NombreAccion) object;
                return getStringKey(o.getIdNombreAccion());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + NombreAccion.class.getName());
            }
        }
    }
}
class NombreAccionDataModel extends ListDataModel<NombreAccion> implements SelectableDataModel<NombreAccion> {

    public NombreAccionDataModel() {
        //nothing
    }

    public NombreAccionDataModel(List<NombreAccion> data) {
        super(data);
    }

    @Override
    public NombreAccion getRowData(String rowKey) {
        List<NombreAccion> listOfNombreAccion = (List<NombreAccion>) getWrappedData();

        for (NombreAccion obj : listOfNombreAccion) {
            if (obj.getIdNombreAccion().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(NombreAccion classname) {
        return classname.getIdNombreAccion();
    }
}
