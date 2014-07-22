package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Funcion;
import com.itcs.helpdesk.persistence.entities.Rol;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumRoles;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "rolController")
@SessionScoped
public class RolController extends AbstractManagedBean<Rol> implements Serializable {

    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;
//    private Rol current;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    private int selectedItemIndex;
    private List<Funcion> funciones = new ArrayList<Funcion>();

    public RolController() {
        super(Rol.class);
    }

//    public Rol getSelected() {
//        if (current == null) {
//            current = new Rol();
//            selectedItemIndex = -1;
//        }
//        return current;
//    }
//
//    public void setSelected(Rol rol) {
//        this.current = rol;
//    }
    public List<Funcion> getFunciones() {
        return funciones;
    }

    public void setFunciones(List<Funcion> funciones) {
        this.funciones = funciones;
    }

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(Rol.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(Rol.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "/script/rol/List";
    }

    public String prepareView() {
        if (current == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila.");
            return null;
        }
        funciones = current.getFuncionList();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareView(Rol item) throws Exception {
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
        Usuario user = userSessionBean.getCurrent();
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
            getJpaController().persistRol(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RolCreated"));
            return prepareList();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(Rol item) {
        setSelected(item);
        return prepareEdit();
    }

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
                getJpaController().mergeRol(current);
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
                getJpaController().removeRol(current);
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

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<Rol> listOfRol = new ArrayList<Rol>();
        while (iter.hasNext()) {
            listOfRol.add((Rol) iter.next());
        }

        return new RolDataModel(listOfRol);
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

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
//
//    public String last() {
//        getPagination().lastPage();
//        recreateModel();
//        return "List";
//    }
//
//    public String first() {
//        getPagination().firstPage();
//        recreateModel();
//        return "List";
//    }
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getRolFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getRolFindAll(), true);
//    }
    @FacesConverter(forClass = Rol.class)
    public static class RolControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RolController controller = (RolController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "rolController");
            return controller.getJpaController().getRolFindByIdRol(getKey(value));
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
