package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
import javax.faces.model.SelectItem;
import org.primefaces.model.DualListModel;
import org.primefaces.model.SelectableDataModel;


@ManagedBean(name = "grupoController")
@SessionScoped
public class GrupoController extends AbstractManagedBean<Grupo> implements Serializable {

    private int selectedItemIndex;
    private DualListModel<Usuario> usuariosDualListModel = new DualListModel<Usuario>();
    private DualListModel<Producto> productoDualListModel = new DualListModel<Producto>();

    public GrupoController() {
        super(Grupo.class);
    }

//    @Override
//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(getPaginationPageSize()) {
//                @Override
//                public int getItemsCount() {
//                    return getJpaController().count(Grupo.class).intValue();
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    return new ListDataModel(getJpaController().queryByRange(Grupo.class, getPageSize(), getPageFirstItem()));
//                }
//            };
//        }
//        return pagination;
//    }

    public String prepareList() {
        recreateModel();
        return "/script/grupo/List";
    }

    public String prepareView() {
        if (getSelected() == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para visualizar.");
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/grupo/View";
    }

    public String prepareCreate() {
        current = new Grupo();
        selectedItemIndex = -1;
        setUsuariosDualListModel(new DualListModel<Usuario>(getJpaController().getUsuarioFindAll(), new ArrayList<Usuario>()));
        setProductoDualListModel(new DualListModel<Producto>((List<Producto>)getJpaController().findAll(Producto.class), new ArrayList<Producto>()));
        //setCategoriasDualListModel(new DualListModel<Categoria>(getJpaController().getCategoriaFindAll(), new ArrayList<Categoria>()));
        return "/script/grupo/Create";
    }

    public boolean puedeVer(Grupo item) {
        return item != null;
    }

    public boolean puedeEditar(Grupo item) {
        return (puedeVer(item) && item.isEditable());
    }

    public String create() {
        try {
//            Area area = ((UserSessionBean) JsfUtil.getManagedBean("UserSessionBean")).getCurrent().getIdGrupo().getIdArea();
//            current.setIdArea(area);
            current.setEditable(true);

            current.setUsuarioList(getUsuariosDualListModel().getTarget());
            current.setProductoList(getProductoDualListModel().getTarget());
            

            getJpaController().persistGrupo(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GrupoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(Grupo item) throws Exception {
        setSelected(item);
        setUsuariosDualListModel(new DualListModel<Usuario>(getJpaController().getUsuarioFindAll(), current.getUsuarioList()));
        for (Usuario user : current.getUsuarioList()) {
            if (getUsuariosDualListModel().getSource().contains(user)) {
                getUsuariosDualListModel().getSource().remove(user);
            }
        }
        setProductoDualListModel(new DualListModel<Producto>((List<Producto>)getJpaController().findAll(Producto.class), current.getProductoList()));
        for (Producto producto : current.getProductoList()) {
            if (getProductoDualListModel().getSource().contains(producto)) {
                getProductoDualListModel().getSource().remove(producto);
            }
        }
        
        return "/script/grupo/Edit";
    }

    public String prepareView(Grupo item) throws Exception {
        selectItem(item);
        if (getSelected() == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione un grupo para visualizar.");
            return "";
        }
        return "/script/grupo/View";
    }

    public void selectItem(Grupo item) {
        //System.out.println("selectItem="+item.getIdGrupo());
        current = item;
    }
    
    public SelectItem[] getStringItemsAvailableSelectOne() {
        List<Grupo> lista = (List<Grupo>) getJpaController().findAll(Grupo.class);
        List<String> ids = new LinkedList<String>();
        for (Grupo grupo : lista) {
            ids.add(grupo.getIdGrupo());
        }
        return JsfUtil.getSelectItems(ids, true);
    }

    public String prepareEdit() {
        if (getSelected() == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return null;
        }
        setUsuariosDualListModel(new DualListModel<Usuario>(getJpaController().getUsuarioFindAll(), current.getUsuarioList()));
        for (Usuario user : current.getUsuarioList()) {
            if (getUsuariosDualListModel().getSource().contains(user)) {
                getUsuariosDualListModel().getSource().remove(user);
            }
        }
         setProductoDualListModel(new DualListModel<Producto>((List<Producto>)getJpaController().findAll(Producto.class), current.getProductoList()));
        for (Producto producto : current.getProductoList()) {
            if (getProductoDualListModel().getSource().contains(producto)) {
                getProductoDualListModel().getSource().remove(producto);
            }
        }
        try {
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        } catch (Exception e) {
            //ignore
        }
        //setSelectedCategoriasNodes(generarCatTreeNodesSeleccionados(current.getCategoriaList(), getTreeNodeCategorias()));

        return "/script/grupo/Edit";
    }

    public String update() {
        try {
            current.setUsuarioList(getUsuariosDualListModel().getTarget());
            current.setProductoList(getProductoDualListModel().getTarget());

            getJpaController().mergeGrupo(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GrupoUpdated"));
            return "/script/grupo/Edit";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            JsfUtil.addErrorMessage("Debe seleccionar un grupo.");
            return null;
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (performDestroy()) {
            recreateModel();
            return "/script/grupo/List";
        }
        return null;
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/grupo/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/grupo/List";
        }
    }

    private boolean performDestroy() {
        try {
            if (current.getUsuarioList().size() > 0) {
                JsfUtil.addErrorMessage("No se puede eliminar, el grupo aun tiene usuarios asociados");
                return false;
            }
            getJpaController().removeGrupo(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GrupoDeleted"));
            return true;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
        return false;
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Grupo.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Grupo) getJpaController().queryByRange(Grupo.class, 1, selectedItemIndex).get(0);
        }
    }

   

    /**
     * @return the usuariosDualListModel
     */
    public DualListModel<Usuario> getUsuariosDualListModel() {
        return usuariosDualListModel;
    }

    /**
     * @param usuariosDualListModel the usuariosDualListModel to set
     */
    public void setUsuariosDualListModel(DualListModel<Usuario> usuariosDualListModel) {
        this.usuariosDualListModel = usuariosDualListModel;
    }

    @Override
    public Class getDataModelImplementationClass() {
       return GrupoDataModel.class;
    }

    /**
     * @return the productoDualListModel
     */
    public DualListModel<Producto> getProductoDualListModel() {
        return productoDualListModel;
    }

    /**
     * @param productoDualListModel the productoDualListModel to set
     */
    public void setProductoDualListModel(DualListModel<Producto> productoDualListModel) {
        this.productoDualListModel = productoDualListModel;
    }

    @FacesConverter(forClass = Grupo.class)
    public static class GrupoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            GrupoController controller = (GrupoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "grupoController");
            return controller.getJpaController().getGrupoFindByIdGrupo(getKey(value));
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
            if (object instanceof Grupo) {
                Grupo o = (Grupo) object;
                return getStringKey(o.getIdGrupo());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + GrupoController.class.getName());
            }
        }
    }
}
class GrupoDataModel extends ListDataModel<Grupo> implements SelectableDataModel<Grupo> {

    public GrupoDataModel() {
        //nothing
    }

    public GrupoDataModel(List<Grupo> data) {
        super(data);
    }

    @Override
    public Grupo getRowData(String rowKey) {
        List<Grupo> listOfGrupo = (List<Grupo>) getWrappedData();

        for (Grupo obj : listOfGrupo) {
            if (obj.getIdGrupo().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Grupo classname) {
        return classname.getIdGrupo();
    }
}
