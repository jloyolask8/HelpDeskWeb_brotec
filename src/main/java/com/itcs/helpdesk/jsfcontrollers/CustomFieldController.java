package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.CasoCustomField;
import com.itcs.helpdesk.persistence.entities.CustomField;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "customFieldController")
@SessionScoped
public class CustomFieldController extends AbstractManagedBean<CustomField> implements Serializable {

    private String currentOption;

    public CustomFieldController() {
        super(CustomField.class);
    }

    public void filterByCasoCustomFields() {
        recreateModel();
    }

    @Override
    public PaginationHelper getPagination() {
        return super.getPagination();
    }

    @Override
    protected String getListPage() {
        return "/script/customField/List";
    }

    @Override
    protected String getEditPage() {
        return null;
    }

    @Override
    protected String getViewPage() {
        return null;
    }
    
    

//    protected void initializeEmbeddableKey() {
//        current.setCustomFieldPK(new com.itcs.helpdesk.persistence.entities.CustomFieldPK());
//        current.getCustomFieldPK().setEntity(JPAServiceFacade.CASE_CUSTOM_FIELD);
//    }
    public void prepareCreate() {
        current = new CustomField();
//        initializeEmbeddableKey();
    }

    public void addOption() {
        final List<String> fieldOptionsList = current.getFieldOptionsList();
        fieldOptionsList.add(currentOption);
        current.setFieldOptionsList(fieldOptionsList);
        executeInClient("PF('addOptionFormDialog').hide()");
    }

    public void create() {

        if (current != null) {
//            setEmbeddableKeys();
            try {
                if (current.getEntity() != null && current.getEntity().equals("case") && current.getTipoCasoList() != null) {
                    for (TipoCaso tipoCaso : current.getTipoCasoList()) {
                        List<CustomField> customFieldList = tipoCaso.getCustomFieldList();
                        if (customFieldList == null) {
                            customFieldList = new LinkedList<>();
                        }
                        customFieldList.add(current);
                        tipoCaso.setCustomFieldList(customFieldList);
                    }
                }

                getJpaController().persist(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CustomFieldCreated"));
                if (!JsfUtil.isValidationFailed()) {
                    items = null;    // Invalidate list of items to trigger re-query.
                    executeInClient("PF('CustomFieldCreateDialog').hide()");
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }

//        persist(JsfUtil.PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("CustomFieldCreated"));
//        if (!JsfUtil.isValidationFailed()) {
//            items = null;    // Invalidate list of items to trigger re-query.
//            executeInClient("PF('CustomFieldCreateDialog').hide()");
//        }
    }

    public void update() {
        if (current != null) {
//            setEmbeddableKeys();
            try {
//                CustomField persistentCustomField = getJpaController().find(CustomField.class, current.getCustomFieldPK());
//                List<TipoCaso> tipoCasoListOld = persistentCustomField.getTipoCasoList();
//                List<TipoCaso> tipoCasoListNew = current.getTipoCasoList();

//                for (TipoCaso tipoCaso : tipoCasoListNew) {
//                    List<CustomField> customFieldList = tipoCaso.getCustomFieldList();
//                    if (customFieldList == null) {
//                        customFieldList = new LinkedList<CustomField>();
//                    }
//                    customFieldList.add(current);
//                    tipoCaso.setCustomFieldList(customFieldList);
//                }
//                List<TipoCaso> attachedTipoCasoListNew = new ArrayList<TipoCaso>();
//                for (TipoCaso tipoCasoListNewTipoCasoToAttach : tipoCasoListNew) {
//                    tipoCasoListNewTipoCasoToAttach = getJpaController().getReference(tipoCasoListNewTipoCasoToAttach.getClass(), tipoCasoListNewTipoCasoToAttach.getIdTipoCaso());
//                    attachedTipoCasoListNew.add(tipoCasoListNewTipoCasoToAttach);
//                }
//                tipoCasoListNew = attachedTipoCasoListNew;
//                current.setTipoCasoList(tipoCasoListNew);
                getJpaController().merge(current);

//                for (TipoCaso tipoCasoListOldTipoCaso : tipoCasoListOld) {
//                    if (!tipoCasoListNew.contains(tipoCasoListOldTipoCaso)) {
//                        tipoCasoListOldTipoCaso.getCustomFieldList().remove(current);
//                        getJpaController().merge(tipoCasoListOldTipoCaso);
//                    }
//                }
//                for (TipoCaso tipoCasoListNewTipoCaso : tipoCasoListNew) {
//                    if (!tipoCasoListOld.contains(tipoCasoListNewTipoCaso)) {
//                        tipoCasoListNewTipoCaso.getCustomFieldList().add(current);
//                        getJpaController().merge(tipoCasoListNewTipoCaso);
//                    }
//                }
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CustomFieldUpdated"));
                executeInClient("PF('CustomFieldEditDialog').hide()");
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public void destroy() {
        try {
            if (!JsfUtil.isValidationFailed()) {
                getJpaController().remove(CustomField.class, current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CustomFieldDeleted"));
                current = null; // Remove selection
                items = null;    // Invalidate list of items to trigger re-query.
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    public CustomField getCustomField(com.itcs.helpdesk.persistence.entities.CustomFieldPK id) {
//        return getJpaController().find(CustomField.class, id);
//    }
    @Override
    public Class getDataModelImplementationClass() {
        return CustomFieldDataModel.class;
    }

    /**
     * @return the currentOption
     */
    public String getCurrentOption() {
        return currentOption;
    }

    /**
     * @param currentOption the currentOption to set
     */
    public void setCurrentOption(String currentOption) {
        this.currentOption = currentOption;
    }

//    public List<CustomField> getItemsAvailableSelectMany() {
//        return getFacade().findAll();
//    }
//
//    public List<CustomField> getItemsAvailableSelectOne() {
//        return getFacade().findAll();
//    }
    public List<CasoCustomField> getCurrentCasoCustomFieldList(Caso caso) {

//        System.out.println("getCurrentCasoCustomFieldList...");
//        System.out.println("getTipoCaso:" + current.getTipoCaso());
//        System.out.println("CustomFieldList:" + current.getTipoCaso().getCustomFieldList());
        if (caso == null) {
            return null;
        }

        if (caso.getCasoCustomFieldList() == null) {
            caso.setCasoCustomFieldList(new ArrayList<CasoCustomField>());
        }

        try {
            List<CasoCustomField> removeCasoCustomFieldList = new LinkedList<>();
            if (caso.getTipoCaso() != null) {
                //remove all old

                TipoCaso tipo = getJpaController().find(TipoCaso.class, caso.getTipoCaso().getIdTipoCaso(), true);

//                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "CasoController.getCurrentCasoCustomFieldList:{0}", 
//                       tipo.getCustomFieldList());
                for (CasoCustomField casoCustomField : caso.getCasoCustomFieldList()) {
                    if (!casoCustomField.getCustomField().getTipoCasoList().contains(caso.getTipoCaso())) {
                        removeCasoCustomFieldList.add(casoCustomField);
                    }
                }
                caso.getCasoCustomFieldList().removeAll(removeCasoCustomFieldList);
                //add all new
                for (CustomField customField : tipo.getCustomFieldList()) {
                    CasoCustomField casoCustomField = new CasoCustomField(customField, caso);
                    if (!caso.getCasoCustomFieldList().contains(casoCustomField)) {
                        casoCustomField.setCustomField(customField);
                        caso.getCasoCustomFieldList().add(casoCustomField);
//                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "added:{0}", casoCustomField);
//                        merge = true;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "error at getItemsSubEstadoCasoAvailableSelectOneCasoAbierto", ex);
        }

//        System.out.println("return:" + caso.getCasoCustomFieldList());
        return caso.getCasoCustomFieldList();
    }

    @FacesConverter(forClass = CustomField.class)
    public static class CustomFieldControllerConverter implements Converter {

//        private static final String SEPARATOR = "#";
//        public static final String SEPARATOR_ESCAPED = "\\#";
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CustomFieldController controller = (CustomFieldController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "customFieldController");
            return controller.getJpaController().find(CustomField.class, getKey(value));
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
            if (object instanceof CustomField) {
                CustomField o = (CustomField) object;
                return getStringKey(o.getIdCustomField());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CasoController.class.getName());
            }
        }

//        com.itcs.helpdesk.persistence.entities.CustomFieldPK getKey(String value) {
//            com.itcs.helpdesk.persistence.entities.CustomFieldPK key;
//            String values[] = value.split(SEPARATOR_ESCAPED);
//            key = new com.itcs.helpdesk.persistence.entities.CustomFieldPK();
//            key.setFieldKey(values[0]);
//            key.setEntity(values[1]);
//            return key;
//        }
//
//        String getStringKey(com.itcs.helpdesk.persistence.entities.CustomFieldPK value) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(value.getFieldKey());
//            sb.append(SEPARATOR);
//            sb.append(value.getEntity());
//            return sb.toString();
//        }
//        @Override
//        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
//            if (object == null) {
//                return null;
//            }
//            if (object instanceof CustomField) {
//                CustomField o = (CustomField) object;
//                return getStringKey(o.getLabel());
//            } else {
//                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), CustomField.class.getName()});
//                return null;
//            }
//        }
    }
}

class CustomFieldDataModel extends ListDataModel<CustomField> implements SelectableDataModel<CustomField> {

    public CustomFieldDataModel() {
        //nothing
    }

    public CustomFieldDataModel(List<CustomField> data) {
        super(data);
    }

    @Override
    public CustomField getRowData(String rowKey) {
        List<CustomField> list = (List<CustomField>) getWrappedData();

        if (list != null) {
            for (CustomField obj : list) {
                if (obj.getIdCustomField().toString().equals(rowKey)) {
                    return obj;
                }
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(CustomField classname) {
        return classname.getIdCustomField();
    }
}
