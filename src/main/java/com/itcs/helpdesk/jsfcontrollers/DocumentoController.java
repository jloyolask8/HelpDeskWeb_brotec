package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Documento;
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


@ManagedBean(name = "documentoController")
@SessionScoped
public class DocumentoController extends AbstractManagedBean<Documento> implements Serializable {

//    private Documento current;
//    private Documento[] selectedItems;
    private int selectedItemIndex;

    public DocumentoController() {
        super(Documento.class);
    }

//    public Documento getSelected() {
//        if (current == null) {
//            current = new Documento();
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
                    return getJpaController().count(Documento.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new DocumentoDataModel(getJpaController().queryByRange(Documento.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

  

    public String prepareCreate() {
        current = new Documento();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().persistDocumento(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DocumentoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

  

    public String update() {
        try {
            getJpaController().mergeDocumento(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DocumentoUpdated"));
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
            getJpaController().removeDocumento(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DocumentoDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Documento.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Documento) getJpaController().queryByRange(Documento.class, 1, selectedItemIndex).get(0);
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
//    public Documento[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Documento[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getDocumentoFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getDocumentoFindAll(), true);
//    }

    @FacesConverter(forClass = Documento.class)
    public static class DocumentoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DocumentoController controller = (DocumentoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "documentoController");
            return controller.getJpaController().getDocumentoFindByPK(getKey(value));
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
            if (object instanceof Documento) {
                Documento o = (Documento) object;
                return getStringKey(o.getIdDocumento());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Documento.class.getName());
            }
        }
    }
}
class DocumentoDataModel extends ListDataModel<Documento> implements SelectableDataModel<Documento> {

    public DocumentoDataModel() {
        //nothing
    }

    public DocumentoDataModel(List<Documento> data) {
        super(data);
    }

    @Override
    public Documento getRowData(String rowKey) {
        List<Documento> listOfDocumento = (List<Documento>) getWrappedData();

        for (Documento obj : listOfDocumento) {
            if (obj.getIdDocumento().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Documento classname) {
        return classname.getIdDocumento();
    }
}
