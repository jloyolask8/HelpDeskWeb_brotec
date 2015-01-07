package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Funcion;
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

@ManagedBean(name = "funcionController")
@SessionScoped
public class FuncionController extends AbstractManagedBean<Funcion> implements Serializable {

//    private Funcion current;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    private int selectedItemIndex;

    public FuncionController() {
        super(Funcion.class);
    }

//    public Funcion getSelected() {
//        if (current == null) {
//            current = new Funcion();
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
                    return getJpaController().count(Funcion.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(Funcion.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    @Override
    public String prepareList() {
        recreateModel();
        return "/script/funcion/List";
    }

    public String prepareCreate() {
        current = new Funcion();
        selectedItemIndex = -1;
        return "/script/funcion/Create";
    }

    public String create() {
        try {
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FuncionCreated"));
            return prepareCreate();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FuncionUpdated"));
            return "/script/funcion/View";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Funcion.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Funcion) getJpaController().queryByRange(Funcion.class, 1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<Funcion> listOfFuncion = new ArrayList<Funcion>();
        while (iter.hasNext()) {
            listOfFuncion.add((Funcion) iter.next());
        }

        return new FuncionDataModel(listOfFuncion);
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getFuncionFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getFuncionFindAll(), true);
//    }
    @FacesConverter(forClass = Funcion.class)
    public static class FuncionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FuncionController controller = (FuncionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "funcionController");
            return controller.getJpaController().find(Funcion.class, getKey(value));
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
            if (object instanceof Funcion) {
                Funcion o = (Funcion) object;
                return getStringKey(o.getIdFuncion());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + FuncionController.class.getName());
            }
        }
    }
}

class FuncionDataModel extends ListDataModel<Funcion> implements SelectableDataModel<Funcion> {

    public FuncionDataModel() {
        //nothing
    }

    public FuncionDataModel(List<Funcion> data) {
        super(data);
    }

    @Override
    public Funcion getRowData(String rowKey) {
        List<Funcion> listOfFuncion = (List<Funcion>) getWrappedData();

        for (Funcion obj : listOfFuncion) {
            if (obj.getIdFuncion().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Funcion classname) {
        return classname.getIdFuncion();
    }
}
