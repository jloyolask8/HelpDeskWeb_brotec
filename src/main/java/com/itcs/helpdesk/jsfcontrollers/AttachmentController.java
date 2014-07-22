package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Attachment;
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
import org.primefaces.model.UploadedFile;


@ManagedBean(name = "attachmentController")
@SessionScoped
public class AttachmentController extends AbstractManagedBean<Attachment> implements Serializable {

//    private Attachment current;
//    private Attachment[] selectedItems;   
    private int selectedItemIndex;

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

//    public Attachment getSelected() {
//        if (current == null) {
//            current = new Attachment();
//            selectedItemIndex = -1;
//        }
//        return current;
//    }


    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper() {

                @Override
                public int getItemsCount() {
                    return getJpaController().count(Attachment.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(Attachment.class, getPageSize(), getPageFirstItem()));
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
        current = new Attachment();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().persistAttachment(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AttachmentCreated"));
            return prepareCreate();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
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
            getJpaController().mergeAttachment(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AttachmentUpdated"));
            return "View";
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
        return "List";
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
            getJpaController().removeAttachment(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AttachmentDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Attachment.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Attachment)getJpaController().queryByRange(Attachment.class, 1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<Attachment> listOfAttachment = new ArrayList<Attachment>();
        while (iter.hasNext()) {
            listOfAttachment.add((Attachment) iter.next());
        }

        return new AttachmentDataModel(listOfAttachment);
    }

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
            if (value == null || value.length() == 0) {
                return null;
            }
            AttachmentController controller = (AttachmentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "attachmentController");
            return controller.getJpaController().getAttachmentFindByIdAttachment(getKey(value));
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