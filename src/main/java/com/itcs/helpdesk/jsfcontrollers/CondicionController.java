package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Condicion;
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


@ManagedBean(name = "condicionController")
@SessionScoped
public class CondicionController extends AbstractManagedBean<Condicion> implements Serializable {

//    private Condicion current;
//    private Condicion[] selectedItems;   
    private int selectedItemIndex;

    public CondicionController() {
        super(Condicion.class);
    }

//    public Condicion getSelected() {
//        if (current == null) {
//            current = new Condicion();
//            selectedItemIndex = -1;
//        }
//        return current;
//    }

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(Condicion.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new CondicionDataModel(getJpaController().queryByRange(Condicion.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "/script/condicion/List";
    }

    

    public String prepareCreate() {
        current = new Condicion();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().persistCondicion(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CondicionCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

  

    public String update() {
        try {
            getJpaController().mergeCondicion(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CondicionUpdated"));
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
            getJpaController().removeCondicion(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CondicionDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Condicion.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Condicion) getJpaController().queryByRange(Condicion.class, 1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    /**
//     * @return the selectedItems
//     */
//    public Condicion[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Condicion[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }
//
//  
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getCondicionFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getCondicionFindAll(), true);
//    }

    @FacesConverter(forClass = Condicion.class)
    public static class CondicionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CondicionController controller = (CondicionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "condicionController");
            return controller.getJpaController().getCondicionFindByIdCondicion(getKey(value));
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
            if (object instanceof Condicion) {
                Condicion o = (Condicion) object;
                return getStringKey(o.getIdCondicion());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Condicion.class.getName());
            }
        }
    }
}
class CondicionDataModel extends ListDataModel<Condicion> implements SelectableDataModel<Condicion> {

    public CondicionDataModel() {
        //nothing
    }

    public CondicionDataModel(List<Condicion> data) {
        super(data);
    }

    @Override
    public Condicion getRowData(String rowKey) {
        List<Condicion> listOfCondicion = (List<Condicion>) getWrappedData();

        for (Condicion obj : listOfCondicion) {
            if (obj.getIdCondicion().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Condicion classname) {
        return classname.getIdCondicion();
    }
}
