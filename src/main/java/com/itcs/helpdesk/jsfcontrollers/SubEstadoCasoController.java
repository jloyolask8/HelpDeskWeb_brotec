package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.SubEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.SelectableDataModel;


@ManagedBean(name = "subEstadoCasoController")
@SessionScoped
public class SubEstadoCasoController extends AbstractManagedBean<SubEstadoCaso> implements Serializable {

//    private SubEstadoCaso current;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
   
    private int selectedItemIndex;

    public SubEstadoCasoController() {
        super(SubEstadoCaso.class);
    }

    public void handleChange() {
    }

    public String prepareList() {
        recreateModel();
        return "/script/sub_estado_caso/List";
        //return "List";
    }

    public void onRowSelect(SelectEvent event) {
//        FacesMessage msg = new FacesMessage("Subestado seleccionado "+((SubEstadoCaso) event.getObject()).getNombre(), ((SubEstadoCaso) event.getObject()).getNombre());  
//  
//        FacesContext.getCurrentInstance().addMessage(null, msg);  
    }

    public String prepareView() {
        if (getSelected() == null) {
            JsfUtil.addSuccessMessage("Es requerido que seleccione una fila para visualizar.");
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/sub_estado_caso/View";
    }

    public boolean puedeVer(SubEstadoCaso item) {
        return item != null;
    }

    public boolean puedeEditar(SubEstadoCaso item) {
        return (puedeVer(item) && item.isEditable());
    }

    public String prepareCreate() {
        current = new SubEstadoCaso();
        selectedItemIndex = -1;
        return "/script/sub_estado_caso/Create";
    }

    public String create() {
        try {
            current.setEditable(true);
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SubEstadoCasoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(SubEstadoCaso item) {
        setSelected(item);
        return prepareEdit();
    }

    public String prepareEdit() {
        if (getSelected() == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/sub_estado_caso/Edit";
    }

    public String prepareDeleteSelected() {

        return null;
    }

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SubEstadoCasoUpdated"));
            return "/script/sub_estado_caso/View";
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
        return "/script/sub_estado_caso/List";
    }

    public String destroySelected() {
        performDestroy();
        recreateModel();
        return "/script/sub_estado_caso/List";
    }

    public void destroySelected(ActionEvent actionEvent) {
        destroySelected();
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/sub_estado_caso/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/sub_estado_caso/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().remove(SubEstadoCaso.class, current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SubEstadoCasoDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(SubEstadoCaso.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (SubEstadoCaso) getJpaController().queryByRange(SubEstadoCaso.class, 1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }
//
//    private void recreateModel() {
//        items = null;
//    }
//
//    public String next() {
//        getPagination().nextPage();
//        recreateModel();
//        return "/script/sub_estado_caso/List";
//    }
//
//    public String previous() {
//        getPagination().previousPage();
//        recreateModel();
//        return "/script/sub_estado_caso/List";
//    }
//
//    public String last() {
//        getPagination().lastPage();
//        recreateModel();
//        return "/script/sub_estado_caso/List";
//    }
//
//    public String first() {
//        getPagination().firstPage();
//        recreateModel();
//        return "/script/sub_estado_caso/List";
//    }

//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getSubEstadoCasoFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getSubEstadoCasoFindAll(), true);
//    }

    public SelectItem[] getItemsAvailableSelectOneCerrarCaso() {
        return JsfUtil.getSelectItems(getJpaController().getSubEstadoCasofindByIdEstado(EnumEstadoCaso.CERRADO.getEstado().getIdEstado()), false);
    }

    public SelectItem[] getItemsAvailableSelectOneAbierto() {
        return JsfUtil.getSelectItems(getJpaController().getSubEstadoCasofindByIdEstado(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado()), false);
    }

    @Override
    public Class getDataModelImplementationClass() {
        return SubEstadoCasoDataModel.class;
    }


    @FacesConverter(forClass = SubEstadoCaso.class)
    public static class SubEstadoCasoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            SubEstadoCasoController controller = (SubEstadoCasoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "subEstadoCasoController");
            return controller.getJpaController().find(SubEstadoCaso.class, getKey(value));
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
            if (object instanceof SubEstadoCaso) {
                SubEstadoCaso o = (SubEstadoCaso) object;
                return getStringKey(o.getIdSubEstado());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + SubEstadoCaso.class.getName());
            }
        }
    }
}
class SubEstadoCasoDataModel extends ListDataModel<SubEstadoCaso> implements SelectableDataModel<SubEstadoCaso> {

    public SubEstadoCasoDataModel() {
        //nothing
    }

    public SubEstadoCasoDataModel(List<SubEstadoCaso> data) {
        super(data);
    }

    @Override
    public SubEstadoCaso getRowData(String rowKey) {
        List<SubEstadoCaso> listOfSubEstadoCaso = (List<SubEstadoCaso>) getWrappedData();

        for (SubEstadoCaso obj : listOfSubEstadoCaso) {
            if (obj.getIdSubEstado().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(SubEstadoCaso classname) {
        return classname.getIdSubEstado();
    }
}
