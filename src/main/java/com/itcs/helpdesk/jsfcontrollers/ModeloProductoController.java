package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.ModeloProducto;
import com.itcs.helpdesk.persistence.entities.ModeloProductoPK;
import com.itcs.helpdesk.persistence.entities.Producto;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import org.primefaces.model.UploadedFile;

@ManagedBean(name = "modeloProductoController")
@SessionScoped
public class ModeloProductoController extends AbstractManagedBean<ModeloProducto> implements Serializable {

    private transient UploadedFile fileUpload;//not in use yet

    public ModeloProductoController() {
        super(ModeloProducto.class);
    }
    
     public SelectItem[] getItemsAvailableSelectOneByProduct(String idProducto) {        
        Producto p = getJpaController().find(Producto.class, idProducto);
        return JsfUtil.getSelectItems(p.getModeloProductoList(), true);
    }

    @Override
    protected String getListPage() {
        return "List";
    }

    @Override
    protected String getEditPage() {
        return "Edit";
    }
 

    public void uploadModelosFile() {
        if (fileUpload != null) {
            addMessage(FacesMessage.SEVERITY_INFO, "Archivo " + fileUpload.getFileName() + "subido exitósamente.");
        }
    }

    public void prepareCreateMany() {
    }

    public void prepareView() {
        current = (ModeloProducto) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        mode = "View";
    }

    public void prepareCreate(Producto p) {
        current = new ModeloProducto();
        current.setProducto(p);
//        selectedItemIndex = -1;
//        mode = "Create";
//        return "Create";
    }

    //custom
    public void create() {
        ModeloProductoPK pk = new ModeloProductoPK(current.getIdModelo(), current.getProducto().getIdProducto());
        try {            
            if (getJpaController().find(ModeloProducto.class, pk) != null) {
                //already exists!
                JsfUtil.addSuccessMessage("El código de Modelo ingresado ya está siendo utilizado por otro modelo. Favor cambiar.");
            } else {
                current.setModeloProductoPK(pk);
                //<p:collector value="#{modeloProductoController.selected}" addTo="#{productoController.selected.modeloProductoList}" unique="true"/>
                current.getProducto().getModeloProductoList().add(current);
               // getJpaController().persist(current);
               // getJpaController().merge(current.getProducto());
               // JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ModeloProductoCreated"));
            }

//            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
        }
    }

//    public void prepareEdit(ModeloProducto model) {
//        current = model;//(ModeloProducto) getItems().getRowData();
////        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
////        mode = "Edit";
//    }

    
    
    

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ModeloProductoUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public void destroy() {
        //current = (ModeloProducto) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        //return "List";
    }

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

            Producto p = current.getProducto();
            p.getModeloProductoList().remove(current);
            // <p:collector value="#{modeloProductoController.selected}" removeFrom="#{productoController.selected.modeloProductoList}" unique="true"/>
            getJpaController().remove(ModeloProducto.class, current.getModeloProductoPK());
            getJpaController().merge(p);

            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ModeloProductoDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    /**
     * @return the fileUpload
     */
    public UploadedFile getFileUpload() {
        return fileUpload;
    }

    /**
     * @param fileUpload the fileUpload to set
     */
    public void setFileUpload(UploadedFile fileUpload) {
        this.fileUpload = fileUpload;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FacesConverter(forClass = ModeloProducto.class)
    public static class ModeloProductoControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
           UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "UserSessionBean");
        return controller.getJpaController().find(ModeloProducto.class, getKey(value));
        }

        ModeloProductoPK getKey(String value) {
            ModeloProductoPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new ModeloProductoPK();
            key.setIdModelo(values[0]);
            key.setIdProducto(values[1]);
            return key;
        }

        String getStringKey(ModeloProductoPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdModelo());
            sb.append(SEPARATOR);
            sb.append(value.getIdProducto());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ModeloProducto) {
                ModeloProducto o = (ModeloProducto) object;
                return getStringKey(o.getModeloProductoPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ModeloProducto.class.getName());
            }
        }
    }
}