package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Componente;
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
import javax.persistence.EntityExistsException;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "componenteController")
@SessionScoped
public class ComponenteController extends AbstractManagedBean<Componente> implements Serializable {

    private int selectedItemIndex;

    public ComponenteController() {
        super(Componente.class);
    }

    @Override
    protected String getListPage() {
        return "/script/componente/List";
    }

//    public String prepareView() {
//        if (getSelectedItems().length != 1) {
//            JsfUtil.addSuccessMessage("Es requerido que seleccione una fila para visualizar.");
//            return "";
//        } else {
//            current = getSelectedItems()[0];
//        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        return "View";
//    }
    public String prepareCreate() {
        current = new Componente();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ComponenteCreated"));
            recreatePagination();
            return prepareList();
        } catch (Exception e) {
            if (e instanceof EntityExistsException) {
                addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("EntityExistsError"));
            } else {
                addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
            return null;
        }
    }

//    public void prepareEdit(Componente comp) {
//        if (comp != null) {
//            current = comp;
//        } else {
//            JsfUtil.addWarningMessage("Se requiere un elemento para editar.");
//        }
//    }
    @Override
    protected String getEditPage() {
        return "/script/componente/Edit";
    }

//    public String prepareEdit() {
//        if (getSelectedItems().length != 1) {
//            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
//            return "";
//        } else {
//            current = getSelectedItems()[0];
//        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        return "Edit";
//    }
    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ComponenteUpdated"));
            return prepareList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public void destroy(Componente item) {
        if (item == null) {
            return;
        }
        try {
            getJpaController().remove(Componente.class, item.getIdComponente());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ComponenteDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
        recreateModel();
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return getListPage();
    }

//    public String destroySelected() {
//
//        if (getSelectedItems().length <= 0) {
//            return "";
//        } else {
//            for (int i = 0; i < getSelectedItems().length; i++) {
//                current = getSelectedItems()[i];
//                performDestroy();
//            }
//        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        recreateModel();
//        return "List";
//    }
//    public String destroyAndView() {
//        performDestroy();
//        recreateModel();
//        updateCurrentItem();
//        if (selectedItemIndex >= 0) {
//            return "View";
//        } else {
//            // all items were removed - go back to list
//            recreateModel();
//            return "List";
//        }
//    }
    private void performDestroy() {
        try {
            getJpaController().remove(Componente.class, current.getIdComponente());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ComponenteDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    private void updateCurrentItem() {
//        int count = getJpaController().count(Componente.class).intValue();
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = (Componente) getJpaController().queryByRange(Componente.class, 1, selectedItemIndex).get(0);
//        }
//    }
    @Override
    public Class getDataModelImplementationClass() {
        return ComponenteDataModel.class;
    }

//    /**
//     * @return the selectedItems
//     */
//    public Componente[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Componente[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }
//
//
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getComponenteFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getComponenteFindAll(), true);
//    }
    @FacesConverter(forClass = Componente.class)
    public static class ComponenteControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "UserSessionBean");
            return controller.getJpaController().find(Componente.class, getKey(value));
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
            if (object instanceof Componente) {
                Componente o = (Componente) object;
                return getStringKey(o.getIdComponente());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Componente.class.getName());
            }
        }
    }
}

class ComponenteDataModel extends ListDataModel<Componente> implements SelectableDataModel<Componente> {

    public ComponenteDataModel() {
        //nothing
    }

    public ComponenteDataModel(List<Componente> data) {
        super(data);
    }

    @Override
    public Componente getRowData(String rowKey) {
        List<Componente> listOfComponente = (List<Componente>) getWrappedData();

        for (Componente obj : listOfComponente) {
            if (obj.getIdComponente().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Componente classname) {
        return classname.getIdComponente();
    }
}
