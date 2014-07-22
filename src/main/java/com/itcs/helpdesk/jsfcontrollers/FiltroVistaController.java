package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

@ManagedBean(name = "filtroVistaController")
@SessionScoped
public class FiltroVistaController extends AbstractManagedBean<FiltroVista> implements Serializable {

//    private FiltroVista current;   
    private int selectedItemIndex;

    public FiltroVistaController() {
        super(FiltroVista.class);
    }

//    public FiltroVista getSelected() {
//        if (current == null) {
//            current = new FiltroVista();
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
                    return getJpaController().count(FiltroVista.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(FiltroVista.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "/script/filtroVista/List";
    }

    public String prepareView() {
        current = (FiltroVista) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/filtroVista/View";
    }

    public String prepareCreate() {
        current = new FiltroVista();
        selectedItemIndex = -1;
        return "/script/filtroVista/Create";
    }

    public String create() {
        try {
            getJpaController().getFiltroVistaJpaController().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FiltroVistaCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (FiltroVista) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/filtroVista/Edit";
    }

    public String update() {
        try {
            getJpaController().getFiltroVistaJpaController().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FiltroVistaUpdated"));
            return "/script/filtroVista/View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (FiltroVista) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "/script/filtroVista/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/filtroVista/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/filtroVista/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().getFiltroVistaJpaController().destroy(current.getIdFiltro());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FiltroVistaDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(FiltroVista.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (FiltroVista) getJpaController().queryByRange(FiltroVista.class, selectedItemIndex + 1, selectedItemIndex).get(0);
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


//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getFiltroVistaJpaController().findFiltroVistaEntities(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getFiltroVistaJpaController().findFiltroVistaEntities(), true);
//    }

    @FacesConverter(forClass = FiltroVista.class)
    public static class FiltroVistaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FiltroVistaController controller = (FiltroVistaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "filtroVistaController");
            return controller.getJpaController().getFiltroVistaJpaController().findFiltroVista(getKey(value));
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
            if (object instanceof FiltroVista) {
                FiltroVista o = (FiltroVista) object;
                return getStringKey(o.getIdFiltro());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + FiltroVista.class.getName());
            }
        }
    }
}