package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.jpa.exceptions.PreexistingEntityException;
import com.itcs.helpdesk.util.UtilesRut;
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


@ManagedBean(name = "clienteController")
@SessionScoped
public class ClienteController extends AbstractManagedBean<Cliente> implements Serializable {

//    private Cliente current;
//    private Cliente[] selectedItems;
    private int selectedItemIndex;

    public ClienteController() {
        super(Cliente.class);
    }

//    public Cliente getSelected() {
//        if (current == null) {
//            current = new Cliente();
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
                    return getJpaController().count(Cliente.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ClienteDataModel(getJpaController().queryByRange(Cliente.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
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
        current = new Cliente();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        if (!UtilesRut.validar(current.getRut())) {
            JsfUtil.addErrorMessage("Rut invalido");
            return null;
        }
        try {
            getJpaController().persistCliente(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClienteCreated"));
            return prepareCreate();
        } catch (PreexistingEntityException existing) {
            JsfUtil.addErrorMessage("El usuario con Id " + current.getIdCliente() + "ya Existe, favor usar un Id distinto.");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
        return null;
    }

    public String prepareEdit() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getJpaController().mergeCliente(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClienteUpdated"));
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
            getJpaController().removeCliente(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClienteDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Cliente.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Cliente) getJpaController().queryByRange(Cliente.class, 1, selectedItemIndex).get(0);
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
//    public Cliente[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Cliente[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getClienteFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getClienteFindAll(), true);
//    }

    @FacesConverter(forClass = Cliente.class)
    public static class ClienteControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClienteController controller = (ClienteController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clienteController");
            return controller.getJpaController().findCliente(getKey(value));
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
            if (object instanceof Cliente) {
                Cliente o = (Cliente) object;
                return getStringKey(o.getIdCliente());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Cliente.class.getName());
            }
        }
    }
}
class ClienteDataModel extends ListDataModel<Cliente> implements SelectableDataModel<Cliente> {

    public ClienteDataModel() {
        //nothing
    }

    public ClienteDataModel(List<Cliente> data) {
        super(data);
    }

    @Override
    public Cliente getRowData(String rowKey) {
        List<Cliente> listOfCliente = (List<Cliente>) getWrappedData();

        for (Cliente obj : listOfCliente) {
            if (obj.getIdCliente().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Cliente classname) {
        return classname.getIdCliente();
    }
}
