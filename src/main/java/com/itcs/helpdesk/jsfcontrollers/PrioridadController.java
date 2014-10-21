package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Prioridad;
import com.itcs.helpdesk.persistence.entityenums.EnumPrioridad;
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
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "prioridadController")
@SessionScoped
public class PrioridadController extends AbstractManagedBean<Prioridad> implements Serializable {

//    private Prioridad current;
    public PrioridadController() {
        super(Prioridad.class);
    }

    public String prepareList() {
        recreateModel();
        return "/script/prioridad/List";
    }

    public String prepareView(Prioridad item) {
        current = item;
        return "/script/prioridad/View";
    }

    public String prepareCreate() {
        current = new Prioridad();
        return "/script/prioridad/Create";
    }

    public boolean puedeEliminar(Prioridad p) {
        for (EnumPrioridad enumPrioridad : EnumPrioridad.values()) {
            enumPrioridad.getPrioridad().equals(p);
            return false;
        }

        return true;

    }

    public String create() {
        try {
            getJpaController().persistPrioridad(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PrioridadCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

//    @Override
//    public String prepareEdit(Prioridad item)  {
//        current = (item);
//        return "/script/prioridad/Edit";
//    }

    @Override
    protected String getEditPage() {
        return "/script/prioridad/Edit";
    }
    
    

    public String update() {
        try {
            getJpaController().mergePrioridad(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PrioridadUpdated"));
            return "/script/prioridad/View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
        performDestroy();
        recreateModel();
        return "/script/prioridad/List";
    }

    public void destroySelected() {
        recreateModel();
        recreatePagination();
        if (getSelectedItems() != null) {
            for (Prioridad prioridad : getSelectedItems()) {
                try {
                    getJpaController().remove(Prioridad.class, prioridad);
                } catch (Exception ex) {
                    addErrorMessage("No se pudo eliminar la prioridad " + prioridad.getIdPrioridad() + ". Tiene casos asociados.");
                    Logger.getLogger(PrioridadController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void performDestroy() {
        try {
            getJpaController().removePrioridad(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PrioridadDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }
    
    public SelectItem[] getStringItemsAvailableSelectOne() {
        List<Prioridad> lista = (List<Prioridad>) getJpaController().findAll(Prioridad.class);
        List<String> ids = new LinkedList<String>();
        for (Prioridad prioridad : lista) {
            ids.add(prioridad.getIdPrioridad());
        }
        return JsfUtil.getSelectItems(ids, true);
    }

    @Override
    public Class getDataModelImplementationClass() {
        return PrioridadDataModel.class;
    }

    @FacesConverter(forClass = Prioridad.class)
    public static class PrioridadControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PrioridadController controller = (PrioridadController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "prioridadController");
            return controller.getJpaController().getPrioridadFindByIdPrioridad(getKey(value));
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
            if (object instanceof Prioridad) {
                Prioridad o = (Prioridad) object;
                return getStringKey(o.getIdPrioridad());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Prioridad.class.getName());
            }
        }
    }
}

class PrioridadDataModel extends ListDataModel<Prioridad> implements SelectableDataModel<Prioridad> {

    public PrioridadDataModel() {
        //nothing
    }

    public PrioridadDataModel(List<Prioridad> data) {
        super(data);
    }

    @Override
    public Prioridad getRowData(String rowKey) {
        List<Prioridad> listOfPrioridad = (List<Prioridad>) getWrappedData();

        for (Prioridad obj : listOfPrioridad) {
            if (obj.getIdPrioridad().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Prioridad classname) {
        return classname.getIdPrioridad();
    }
}
