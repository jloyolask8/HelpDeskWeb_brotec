package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import com.itcs.helpdesk.persistence.entities.ProductoContratadoPK;
import com.itcs.helpdesk.persistence.entities.SubComponente;
import com.itcs.helpdesk.persistence.jpa.exceptions.PreexistingEntityException;
import com.itcs.helpdesk.util.UtilesRut;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "clienteController")
@SessionScoped
public class ClienteController extends AbstractManagedBean<Cliente> implements Serializable {

//    private Cliente current;
//    private Cliente[] selectedItems;
    private int selectedItemIndex;
    private String searchPattern;
    private EmailCliente emailToAdd;
    private boolean canCreate = false;
    private boolean canEdit;

    public ClienteController() {
        super(Cliente.class);
    }

    public void chooseSubComponente() {
        RequestContext.getCurrentInstance().openDialog("/script/subComponente/select");
    }

    public void onSubComponenteChosen(SelectEvent event) {
        SubComponente subComponente = (SubComponente) event.getObject();
        ProductoContratado currentProductoContratado = new ProductoContratado();
        currentProductoContratado.setCliente(current);
        currentProductoContratado.setFechaCompra(null);
        currentProductoContratado.setSubComponente(subComponente);
        currentProductoContratado.setComponente(subComponente.getIdComponente());
        currentProductoContratado.setProducto(subComponente.getIdComponente().getIdProducto());
        currentProductoContratado.setProductoContratadoPK(new ProductoContratadoPK(current.getIdCliente(), subComponente.getIdComponente().getIdProducto().getIdProducto(), subComponente.getIdComponente().getIdComponente(), subComponente.getIdSubComponente()));

        if (current.getProductoContratadoList() == null) {
            current.setProductoContratadoList(new LinkedList<ProductoContratado>());
        }

        if (!this.current.getProductoContratadoList().contains(currentProductoContratado)) {
            current.getProductoContratadoList().add(currentProductoContratado);
            try {
                getJpaController().persist(currentProductoContratado);
                getJpaController().merge(current);
                addInfoMessage("OK");
            } catch (Exception ex) {
                JsfUtil.addWarningMessage("No se puede asociar: " + ex.getMessage());
                Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
            }

            
        } else {
            JsfUtil.addWarningMessage("No se puede asociar, el cliente ya tiene asociado el mismo item.");
        }

    }

    @Override
    protected String getListPage() {
        return "/script/cliente/List";
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
        current.setEmailClienteList(new LinkedList<EmailCliente>());
        emailToAdd = new EmailCliente();
        selectedItemIndex = -1;
        return "/script/cliente/Create";
    }

    public void reinit() {
        emailToAdd = new EmailCliente();
        executeInClient("PF('addEmail').hide()");
    }

    public String create() {
        if (!UtilesRut.validar(current.getRut())) {
            JsfUtil.addErrorMessage("Rut invalido");
            return null;
        }
        try {
            List<EmailCliente> listaEmails = current.getEmailClienteList();
            current.setEmailClienteList(null);
            getJpaController().persistCliente(current);
            for (EmailCliente emailCliente : listaEmails) {
                EmailCliente oldEmailCliente = getJpaController().find(EmailCliente.class, emailCliente.getEmailCliente());
                if (null == oldEmailCliente) {
                    emailCliente.setCliente(current);
                    getJpaController().persist(emailCliente);
                } else {
                    oldEmailCliente.getCliente().getEmailClienteList().remove(oldEmailCliente);
                    oldEmailCliente.setCliente(current);
                    getJpaController().merge(oldEmailCliente);
                }
            }
            current.setEmailClienteList(listaEmails);
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClienteCreated"));
            return prepareCreate();
        } catch (PreexistingEntityException existing) {
            JsfUtil.addErrorMessage("El usuario con Id " + current.getIdCliente() + "ya Existe, favor usar un Id distinto.");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
        return null;
    }

//    public Cliente persistirCliente(Cliente cliente)
//    {
//        //Cliente trae rut, y el rut es valido
//        //buscamos por rut
//        Cliente oldClient = null;
//        if((!StringUtils.isEmpty(cliente.getRut())) && 
//                UtilesRut.validar(cliente.getRut())){
//            oldClient = getJpaController().getClienteJpaController().findByRut(cliente.getRut());
//        }
//        //Si no encontramos al cliente por rut, buscamos por email
//        //Si la lista de emails no esta vacia
//        if ((oldClient == null) &&
//                !cliente.getEmailClienteList().isEmpty()){
//            
//        }
//    }
    @Override
    protected String getViewPage() {
        return "/script/cliente/View";
    }

//    public String prepareView(Cliente c) {
//        if (c != null) {
//            current = c;
//        }
//        this.backOutcome = null;
//        return "/script/cliente/View";
//    }
//
//    public String prepareView(Cliente c, String backOutcome) {
//        if (c != null) {
//            current = c;
//        }
//
//        this.backOutcome = backOutcome;
//        return "/script/cliente/View";
//    }
    @Override
    public String prepareEdit(Cliente item) {
        try {
            emailToAdd = new EmailCliente();
            Cliente i = getJpaController().find(Cliente.class, item.getIdCliente());
            setSelected(i);

        } catch (Exception ex) {
            Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "/script/cliente/Edit";
    }

    public String prepareCreateMasivo() {
        return "/script/cliente/cargarClientes";
    }

    public boolean puedeEliminar(Cliente item) {
        if (item != null && item.getCasoList() != null) {
            return item.getCasoList().isEmpty();
        }
        return false;
    }

    public String prepareEdit() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/cliente/Edit";
    }

    public String update() {
        try {
            List<EmailCliente> listaEmails = current.getEmailClienteList();
            for (EmailCliente emailCliente : listaEmails) {
                if (getJpaController().find(EmailCliente.class, emailCliente.getEmailCliente()) == null) {
                    emailCliente.setCliente(current);
                    getJpaController().persist(emailCliente);
                }
            }
            getJpaController().mergeCliente(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClienteUpdated"));
            return "/script/cliente/View";
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
        return "/script/cliente/List";
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
        return "/script/cliente/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/cliente/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/cliente/List";
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

//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//        return items;
//    }
    @Override
    public Class getDataModelImplementationClass() {
        return ClienteDataModel.class;
    }

    /**
     * @return the searchPattern
     */
    public String getSearchPattern() {
        return searchPattern;
    }

    /**
     * @param searchPattern the searchPattern to set
     */
    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public void onBlurRutInput() {
        String rutFormateado = UtilesRut.formatear(getSelected().getRut());
        getSelected().setRut(rutFormateado);

        Cliente c = getJpaController().getClienteJpaController().findByRut(rutFormateado);
        if (c != null) {//this client exists
            setSelected(c);
            setCanCreate(false);
            setCanEdit(false);
        } else {
            setCanCreate(true);
            setCanEdit(true);
        }
    }

    public void setCanCreate(boolean b) {
        this.canCreate = b;
    }

    /**
     * @return the canCreate
     */
    public boolean isCanCreate() {
        return canCreate;
    }

    /**
     * @return the emailToAdd
     */
    public EmailCliente getEmailToAdd() {
        return emailToAdd;
    }

    /**
     * @param emailToAdd the emailToAdd to set
     */
    public void setEmailToAdd(EmailCliente emailToAdd) {
        this.emailToAdd = emailToAdd;
    }

    private void setCanEdit(boolean b) {
        this.canEdit = b;
    }

    /**
     * @return the canEdit
     */
    public boolean isCanEdit() {
        return canEdit;
    }

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
