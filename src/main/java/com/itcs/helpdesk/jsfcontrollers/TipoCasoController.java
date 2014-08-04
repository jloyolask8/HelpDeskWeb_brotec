package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.jpautils.EasyCriteriaQuery;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

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

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(TipoCaso.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(TipoCaso.class, getPageSize(), getPageFirstItem()));//.findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
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

  public Long countCasosByTipo(TipoCaso tipo) {
        EasyCriteriaQuery<Caso> c = new EasyCriteriaQuery<Caso>(emf, Caso.class);
        c.addEqualPredicate("tipoCaso", tipo);
        return c.count();
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
        performDestroy();
        recreatePagination();
        recreateModel();
        return "/script/tipoCaso/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/tipoCaso/List";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/tipoCaso/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().remove(TipoCaso.class, current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("TipoCasoDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            JsfUtil.addErrorMessage(e, e.getLocalizedMessage());
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(TipoCaso.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (TipoCaso) getJpaController().queryByRange(TipoCaso.class, pagination.getPageSize(), pagination.getPageFirstItem()).get(0);
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

  
}
