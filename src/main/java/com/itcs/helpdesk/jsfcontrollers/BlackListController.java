package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.BlackListEmail;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;

@ManagedBean
@SessionScoped
public class BlackListController extends AbstractManagedBean<BlackListEmail> implements Serializable {

    public BlackListController() {
        super(BlackListEmail.class);
    }

//    @Override
//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(getPaginationPageSize()) {
//                @Override
//                public int getItemsCount() {
//                    return getJpaController().count(BlackListEmail.class).intValue();
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    return new ListDataModel(getJpaController().queryByRange(BlackListEmail.class, getPageSize(), getPageFirstItem()));
//                }
//            };
//        }
//        return pagination;
//    }

//    public String prepareList() {
//        recreateModel();
//        return getListOutcomeNotAbstract();
//    }

    public void prepareCreate() {
        current = new BlackListEmail();
    }

//    public void prepareEdit(BlackListEmail o) {
//        current = o;
//    }

    public void create() {
        try {
            getJpaController().persist(current);
         JsfUtil.addSuccessMessage(getSelected().getClass().getSimpleName() + " guardado exitosamente.");
            recreateModel();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
        }
    }

    public void update() {
        try {
            getJpaController().merge(current);
           JsfUtil.addSuccessMessage(getSelected().getClass().getSimpleName() + " actualizado exitosamente.");
//            return "View";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
        }
    }

    public void destroy(BlackListEmail o) {
        if (o != null) {
            try {
                getJpaController().remove(BlackListEmail.class, o);
                JsfUtil.addSuccessMessage(getSelected().getClass().getSimpleName() + " eliminado exitosamente.");
            } catch (Exception e) {
                Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
            recreateModel();
        }
    }

    public void destroySelected() {
        if (current != null) {
            performDestroy();
            recreateModel();
        }
    }

//    public String destroyAndViewNext() {
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
            getJpaController().remove(BlackListEmail.class, current);
           JsfUtil.addSuccessMessage(getSelected().getClass().getSimpleName() + " eliminado exitosamente.");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    private void updateCurrentItem() {
//        int count = getJpaController().count(Canal.class).intValue();
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = (Canal) getJpaController().queryByRange(Canal.class, 1, selectedItemIndex).get(0);
//        }
//    }
//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//
//        return items;
//    }

    @Override
    protected String getListPage() {
        return "/script/blackList/List";
    }

    @Override
    public Class getDataModelImplementationClass() {
        return ListDataModel.class;
    }

    @FacesConverter(forClass = BlackListEmail.class)
    public static class BlackListEmailConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {

            if (value == null || value.length() == 0) {
                return null;
            }
            UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "UserSessionBean");
        return controller.getJpaController().find(BlackListEmail.class, getKey(value));
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
            if (object instanceof BlackListEmail) {
                BlackListEmail o = (BlackListEmail) object;
                return getStringKey(o.getEmailAddress());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + BlackListEmail.class.getName());
            }
        }
    }
}

