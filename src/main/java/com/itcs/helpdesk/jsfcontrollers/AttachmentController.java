package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Attachment;
import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.UploadedFile;


@ManagedBean(name = "attachmentController")
@SessionScoped
public class AttachmentController extends AbstractManagedBean<Attachment> implements Serializable {

    private transient UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public void subir(){

    }

    public AttachmentController() {
        super(Attachment.class);
    }

// 
//    public String create() {
//        try {
//            getJpaController().persist(current);
//            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AttachmentCreated"));
//            return prepareCreate();
//        } catch (Exception e) {
//            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
//            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
//        }
//    }


//    public String update() {
//        try {
//            getJpaController().merge(current);
//            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AttachmentUpdated"));
//            return "View";
//        } catch (Exception e) {
//            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
//            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
//        }
//    }

//    public String destroy() {
//        if (current == null) {
//            return "";
//        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        performDestroy();
//        recreateModel();
//        return "List";
//    }


//    private void performDestroy() {
//        try {
//            getJpaController().remove(Attachment.class, current);
//            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AttachmentDeleted"));
//        } catch (Exception e) {
//            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
//            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//        }
//    }

   

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    /**
//     * @return the selectedItems
//     */
//    public Attachment[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Attachment[] selectedItems) {
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
//        return JsfUtil.getSelectItems(getJpaController().getAttachmentFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getAttachmentFindAll(), true);
//    }

    @FacesConverter(forClass = Attachment.class)
    public static class AttachmentControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
//            System.out.println("AttachmentControllerConverter getAsObject " + value);
            if (value == null || value.length() == 0) {
                return null;
            }
           UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "UserSessionBean");
        return controller.getJpaController().find(Attachment.class, getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
//             System.out.println("AttachmentControllerConverter getAsString " + object);
            if (object == null) {
                return null;
            }
            if (object instanceof Attachment) {
                Attachment o = (Attachment) object;
                return getStringKey(o.getIdAttachment());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + AttachmentController.class.getName());
            }
        }
    }
}
class AttachmentDataModel extends ListDataModel<Attachment> implements SelectableDataModel<Attachment> {

    public AttachmentDataModel() {
        //nothing
    }

    public AttachmentDataModel(List<Attachment> data) {
        super(data);
    }

    @Override
    public Attachment getRowData(String rowKey) {
        List<Attachment> listOfAttachment = (List<Attachment>) getWrappedData();

        for (Attachment obj : listOfAttachment) {
            if (obj.getIdAttachment().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Attachment classname) {
        return classname.getIdAttachment();
    }
}