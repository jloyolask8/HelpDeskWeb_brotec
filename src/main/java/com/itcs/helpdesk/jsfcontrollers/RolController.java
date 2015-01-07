package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Funcion;
import com.itcs.helpdesk.persistence.entities.Rol;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumRoles;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "rolController")
@SessionScoped
public class RolController extends AbstractManagedBean<Rol> implements Serializable {

    private int selectedItemIndex;
    private List<Funcion> funciones = new ArrayList<>();

    public RolController() {
        super(Rol.class);
    }

    public List<Funcion> getFunciones() {
        return funciones;
    }

    public void setFunciones(List<Funcion> funciones) {
        this.funciones = funciones;
    }

    @Override
    protected String getEditPage() {
        return "/script/rol/Edit";
    }

    @Override
    protected String getViewPage() {
        return "/script/rol/View";
    }

    @Override
    protected String getListPage() {
        return "/script/rol/List";
    }

    public String prepareView() {
        if (current == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila.");
            return null;
        }
        funciones = current.getFuncionList();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
       return "/script/rol/View";
    }

    @Override
    public String prepareView(Rol item) {
        current = item;
        funciones = current.getFuncionList();

        return "/script/rol/View";
    }

    public String prepareCreate() {
        current = new Rol();
        selectedItemIndex = -1;
        return "/script/rol/Create";
    }

    public boolean puedeVer(Rol item) {
        return item != null;
    }

    public boolean esUsuarioSistema() {
        Usuario user = getUserSessionBean().getCurrent();
        if (user != null && user.equals(EnumUsuariosBase.SISTEMA.getUsuario())) {
            return true;
        }
        return false;
    }

    public boolean puedeEditar(Rol item) {
        return (esUsuarioSistema()) || (puedeVer(item) && item.isEditable());
    }

    public String create() {
        try {
            current.setEditable(true);
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RolCreated"));
            return prepareList();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

//    public String prepareEdit(Rol item) {
//        setSelected(item);
//        return prepareEdit();
//    }

    public String prepareEdit() {
        if (current == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila.");
            return null;
        }
        String idRol = current.getIdRol();
        if (EnumRoles.ADMINISTRADOR.getRol().getIdRol().equalsIgnoreCase(idRol)) {
            JsfUtil.addErrorMessage("No se puede modificar este rol.");
        } else {

            funciones = current.getFuncionList();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        }
        return "Edit";
    }

    public String update() {
        try {
            String idRol = current.getIdRol();
            if (EnumRoles.ADMINISTRADOR.getRol().getIdRol().equalsIgnoreCase(idRol)) {
                JsfUtil.addErrorMessage("No se puede modificar este rol.");
            } else {
                getJpaController().merge(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RolUpdated"));
            }
            return "View";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            return null;
        }

        String idRol = current.getIdRol();
        if (EnumRoles.ADMINISTRADOR.getRol().getIdRol().equalsIgnoreCase(idRol)) {
            JsfUtil.addErrorMessage("No se puede modificar este rol.");
        } else {
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
            performDestroy();
            recreateModel();
        }
        return "List";
    }

    public String destroySelected() {

        performDestroy();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        String idRol = current.getIdRol();
        if (EnumRoles.ADMINISTRADOR.getRol().getIdRol().equalsIgnoreCase(idRol)) {
            JsfUtil.addErrorMessage("No se puede modificar este rol.");
        } else {
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
        return null;
    }

    private void performDestroy() {
        try {
            String idRol = current.getIdRol();
            if (EnumRoles.ADMINISTRADOR.getRol().getIdRol().equalsIgnoreCase(idRol)) {
                JsfUtil.addErrorMessage("No se puede modificar este rol.");
            } else {
                getJpaController().remove(Rol.class, current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RolDeleted").replace("{1}", idRol));
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Rol.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Rol) getJpaController().queryByRange(Rol.class, 1, selectedItemIndex).get(0);
        }
    }

    @Override
    public Class getDataModelImplementationClass() {
        return RolDataModel.class;
    }
   
    @FacesConverter(forClass = Rol.class)
    public static class RolControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RolController controller = (RolController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "rolController");
            return controller.getJpaController().find(Rol.class, getKey(value));
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
            if (object instanceof Rol) {
                Rol o = (Rol) object;
                return getStringKey(o.getIdRol());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + RolController.class.getName());
            }
        }
    }
}

class RolDataModel extends ListDataModel<Rol> implements SelectableDataModel<Rol> {

    public RolDataModel() {
        //nothing
    }

    public RolDataModel(List<Rol> data) {
        super(data);
    }

    @Override
    public Rol getRowData(String rowKey) {
        List<Rol> listOfRol = (List<Rol>) getWrappedData();

        for (Rol obj : listOfRol) {
            if (obj.getIdRol().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Rol classname) {
        return classname.getIdRol();
    }
}
