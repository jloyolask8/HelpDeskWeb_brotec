package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Recinto;
import com.itcs.helpdesk.persistence.jpa.EasyCriteriaQuery;
import com.itcs.helpdesk.util.Log;

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
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "recintoController")
@SessionScoped
public class RecintoController extends AbstractManagedBean<Recinto> implements Serializable {

//    private Recinto current;
//    private Recinto[] selectedItems;
    private int selectedItemIndex;

    public RecintoController() {
        super(Recinto.class);
    }

    @Override
    protected String getListPage() {
        return "/script/estado_caso/List";
    }
    
    

//    public Recinto getSelected() {
//        if (current == null) {
//            current = new Recinto();
//            selectedItemIndex = -1;
//        }
//        return current;
//    }
//    @Override
//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(getPaginationPageSize()) {
//                @Override
//                public int getItemsCount() {
//                    return getJpaController().count(Recinto.class).intValue();
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    return new ListDataModel(getJpaController().queryByRange(Recinto.class, getPageSize(), getPageFirstItem()));
//                }
//            };
//        }
//        return pagination;
//    }
//
//    public String prepareList() {
//        recreateModel();
//        return "/script/estado_caso/List";
//    }

    public String prepareView() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Es requerido que seleccione una fila para visualizar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/estado_caso/View";
    }

    public String prepareCreate() {
        current = new Recinto();
        selectedItemIndex = -1;
        return "/script/estado_caso/Create";
    }

    public String create() {
        try {
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RecintoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/estado_caso/Edit";
    }

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RecintoUpdated"));
            return "/script/estado_caso/View";
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
        return "/script/estado_caso/List";
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
        return "/script/estado_caso/List";
    }

//    public String destroyAndView() {
//        performDestroy();
//        recreateModel();
//        updateCurrentItem();
//        if (selectedItemIndex >= 0) {
//            return "/script/estado_caso/View";
//        } else {
//            // all items were removed - go back to list
//            recreateModel();
//            return "/script/estado_caso/List";
//        }
//    }

    private void performDestroy() {
        try {
            getJpaController().remove(Recinto.class, current.getIdRecinto());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RecintoDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    private void updateCurrentItem() {
//        int count = getJpaController().count(Recinto.class).intValue();
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = (Recinto) getJpaController().queryByRange(Recinto.class, 1, selectedItemIndex).get(0);
//        }
//    }
//
//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//        Iterator iter = items.iterator();
//        List<Recinto> listOfRecinto = new ArrayList<Recinto>();
//        while (iter.hasNext()) {
//            listOfRecinto.add((Recinto) iter.next());
//        }
//
//        return new RecintoDataModel(listOfRecinto);
//    }

    public List<Recinto> autocompleteValues(String queryValue) {
        EasyCriteriaQuery<Recinto> ecq = new EasyCriteriaQuery<Recinto>(getJpaController(), Recinto.class);
        ecq.addLikePredicate("nombre", "%" + queryValue.trim() + "%");
        ecq.setMaxResults(10);
        ecq.setFirstResult(0);
        ecq.setAll(false);
        List<Recinto> lista = ecq.next();

        if (lista != null && !lista.isEmpty() && !StringUtils.isEmpty(queryValue)) {
            lista = new LinkedList<Recinto>();
            Recinto loneRader = new Recinto();
            loneRader.setNombre(queryValue);
            lista.add(loneRader);
        }
        return lista;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    /**
//     * @return the selectedItems
//     */
//    public Recinto[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Recinto[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }
//
//
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getRecintoFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getRecintoFindAll(), true);
//    }
    @FacesConverter(forClass = Recinto.class)
    public static class RecintoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            try {
                if (value == null || value.length() == 0) {
                    return null;
                }
                RecintoController controller = (RecintoController) facesContext.getApplication().getELResolver().
                        getValue(facesContext.getELContext(), null, "recintoController");
                return controller.getJpaController().find(Recinto.class, getKey(value));
            } catch (Exception ex) {
                Logger.getLogger(RecintoController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Recinto) {
                Recinto o = (Recinto) object;
                return getStringKey(o.getIdRecinto());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + RecintoController.class.getName());
            }
        }
    }
}

class RecintoDataModel extends ListDataModel<Recinto> implements SelectableDataModel<Recinto> {

    public RecintoDataModel() {
        //nothing
    }

    public RecintoDataModel(List<Recinto> data) {
        super(data);
    }

    @Override
    public Recinto getRowData(String rowKey) {
        List<Recinto> listOfRecinto = (List<Recinto>) getWrappedData();

        for (Recinto obj : listOfRecinto) {
            if (obj.getIdRecinto().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Recinto classname) {
        return classname.getIdRecinto();
    }
}
