package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.EstadoCaso;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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


@ManagedBean(name = "estadoCasoController")
@SessionScoped
public class EstadoCasoController extends AbstractManagedBean<EstadoCaso> implements Serializable {

//    private EstadoCaso current;
//    private EstadoCaso[] selectedItems;
    private int selectedItemIndex;

    public EstadoCasoController() {
        super(EstadoCaso.class);
    }

//    public EstadoCaso getSelected() {
//        if (current == null) {
//            current = new EstadoCaso();
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
                    return getJpaController().count(EstadoCaso.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(EstadoCaso.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "/script/estado_caso/List";
    }

    public String prepareView() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Es requerido que seleccione una fila para visualizar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/estado_caso/View";
    }

    public String prepareCreate() {
        current = new EstadoCaso();
        selectedItemIndex = -1;
        return "/script/estado_caso/Create";
    }

    public String create() {
        try {
            getJpaController().persistEstadoCaso(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EstadoCasoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/estado_caso/Edit";
    }

    public String update() {
        try {
            getJpaController().mergeEstadoCaso(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EstadoCasoUpdated"));
            return "/script/estado_caso/View";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
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
        return "/script/estado_caso/List";
    }

    public String destroySelected() {

        if (getSelectedItems().size() <= 0) {
            return "";
        } else {
            for (int i = 0; i < getSelectedItems().size(); i++) {
                current = getSelectedItems().get(i);
                performDestroy();
            }
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        recreateModel();
        return "/script/estado_caso/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/estado_caso/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/estado_caso/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeEstadoCaso(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EstadoCasoDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(EstadoCaso.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (EstadoCaso) getJpaController().queryByRange(EstadoCaso.class, 1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<EstadoCaso> listOfEstadoCaso = new ArrayList<EstadoCaso>();
        while (iter.hasNext()) {
            listOfEstadoCaso.add((EstadoCaso) iter.next());
        }

        return new EstadoCasoDataModel(listOfEstadoCaso);
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    /**
//     * @return the selectedItems
//     */
//    public EstadoCaso[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(EstadoCaso[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }
//
//
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getEstadoCasoFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getEstadoCasoFindAll(), true);
//    }

    @FacesConverter(forClass = EstadoCaso.class)
    public static class EstadoCasoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EstadoCasoController controller = (EstadoCasoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "estadoCasoController");
            return controller.getJpaController().getEstadoCasoFindByIdEstado(getKey(value));
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