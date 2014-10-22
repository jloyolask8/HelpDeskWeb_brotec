package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Accion;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;


@ManagedBean(name = "accionController")
@SessionScoped
public class AccionController extends AbstractManagedBean<Accion>  implements Serializable {


    private int selectedItemIndex;

    public AccionController() {
        super(Accion.class);
    }

    public String prepareCreate() {
        current = new Accion();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().persistAccion(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AccionCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String update() {
        try {
            getJpaController().mergeAccion(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AccionUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeAccion(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AccionDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Accion.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Accion)getJpaController().queryByRange(Accion.class, 1, selectedItemIndex).get(0);
        }
    }

   

    @Override
    public Class getDataModelImplementationClass() {
        return AccionDataModel.class;
    }

//    /**
//     * @return the selectedItems
//     */
//    public Accion[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Accion[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }

//    private void recreateModel() {
//        items = null;
//    }
//
//    public String next() {
//        getPagination().nextPage();
//        recreateModel();
//        return "List";
//    }
//
//    public String previous() {
//        getPagination().previousPage();
//        recreateModel();
//        return "List";
//    }

//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getAccionFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getAccionFindAll(), true);
//    }

    @FacesConverter(forClass = Accion.class)
    public static class AccionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AccionController controller = (AccionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "accionController");
            return controller.getJpaController().getAccionFindById(getKey(value));
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
