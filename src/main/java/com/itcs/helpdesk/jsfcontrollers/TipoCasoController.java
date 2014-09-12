package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "tipoCasoController")
@SessionScoped
public class TipoCasoController extends AbstractManagedBean<TipoCaso> implements Serializable {

//    private TipoCaso current;
//    private DataModel items = null;
//    private PaginationHelper pagination;
    private int selectedItemIndex;

    public TipoCasoController() {
        super(TipoCaso.class);
    }

    public String prepareList() {
        recreateModel();
        return "/script/tipoCaso/List";
    }

    public String prepareView() {
        current = (TipoCaso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/tipoCaso/View";
    }

    public String prepareEdit(TipoCaso item) {
        current = item;
        return "/script/tipoCaso/Edit";
    }

    public String prepareCreate() {
        current = new TipoCaso();
        selectedItemIndex = -1;
        return "/script/tipoCaso/Create";
    }

    public String create() {
        try {
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("TipoCasoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (TipoCaso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/tipoCaso/Edit";
    }

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("TipoCasoUpdated"));
            return "/script/tipoCaso/List";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
        //  current = (TipoCaso) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (performDestroy()) {
            recreateModel();
        }

        return "/script/tipoCaso/List";
    }

    private boolean performDestroy() {
        try {
            getJpaController().remove(TipoCaso.class, current.getIdTipoCaso());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("TipoCasoDeleted"));
            return true;
        } catch (Exception e) {
            JsfUtil.addErrorMessage("No se pudo eliminar el Tipo de Caso, Favor verifique que no tiene datos asociados (Casos, Sub estados o Campos personalizados)");
//            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            JsfUtil.addErrorMessage(e, e.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public Class getDataModelImplementationClass() {
        return TipoCasoDataModel.class;
    }
}

class TipoCasoDataModel extends ListDataModel<TipoCaso> implements SelectableDataModel<TipoCaso> {

    public TipoCasoDataModel() {
        //nothing
    }

    public TipoCasoDataModel(List<TipoCaso> data) {
        super(data);
    }

    @Override
    public TipoCaso getRowData(String rowKey) {
        List<TipoCaso> listOfTipoCaso = (List<TipoCaso>) getWrappedData();

        for (TipoCaso obj : listOfTipoCaso) {
            if (obj.getIdTipoCaso().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(TipoCaso classname) {
        return classname.getIdTipoCaso();
    }
}
