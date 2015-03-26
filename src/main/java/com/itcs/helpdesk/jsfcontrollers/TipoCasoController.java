package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.EstadoCaso;
import com.itcs.helpdesk.persistence.entities.SubEstadoCaso;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.jpa.EasyCriteriaQuery;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.ListDataModel;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public String prepareList() {
        recreateModel();
        return "/script/tipoCaso/List";
    }

    public String prepareView() {
        current = (TipoCaso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/tipoCaso/View";
    }

    @Override
    public String prepareEdit(TipoCaso item) {
        current = item;
        return "/script/tipoCaso/Edit";
    }

    public String prepareCreate() {
        current = new TipoCaso();
        
        SubEstadoCaso initialSe = new SubEstadoCaso(current, "", "Nuevo", EnumEstadoCaso.ABIERTO.getEstado(), 
                "", true, "ffffff", "00aeed", true);
        
        SubEstadoCaso initialSe2 = new SubEstadoCaso(current, "", "Solucionado", EnumEstadoCaso.CERRADO.getEstado(), 
                "", true, "ffffff", "563d7c", false);
                        
        final LinkedList<SubEstadoCaso> linkedList = new LinkedList<>();
        linkedList.add(initialSe);
        linkedList.add(initialSe2);
                
        current.setSubEstadoCasoList(linkedList);
        selectedItemIndex = -1;
        return "/script/tipoCaso/Create";
    }
    
    public void addNewSubEstadoAbierto(){
        addNewSubEstado(EnumEstadoCaso.ABIERTO.getEstado());
    }
    
    public void addNewSubEstadoCerrado(){
        addNewSubEstado(EnumEstadoCaso.CERRADO.getEstado());
    }
    
    
    private void addNewSubEstado(EstadoCaso e){
        if(current.getSubEstadoCasoList() == null){
            current.setSubEstadoCasoList(new LinkedList<SubEstadoCaso>());
        }
        
        SubEstadoCaso se = new SubEstadoCaso(current, "", "Sub Estado " + (current.getSubEstadoCasoList().size()+1) , e, 
                "", true, "ffffff", "00aeed", false);
        
        current.getSubEstadoCasoList().add(se);
    }

    public String create() {
        try {
            current.setIdTipoCaso(current.getNombre().trim().toLowerCase().replace(" ", "_"));
            checkValidateSubEstados();
            if(!isOnlyOneSubEstadoFirst()){
                addErrorMessage("Solo un sub-estado puede ser el inicial.");
                return null;
            }
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("TipoCasoCreated"));
            return prepareList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    private void checkValidateSubEstados() {
        if(current.getSubEstadoCasoList() != null){
            for (SubEstadoCaso subEstadoCaso : current.getSubEstadoCasoList()) {
                if(StringUtils.isEmpty(subEstadoCaso.getIdSubEstado())){
                    subEstadoCaso.setIdSubEstado(current.getIdTipoCaso() + "_" + subEstadoCaso.getNombre().trim().toLowerCase().replace(" ", "_"));
                }
            }
        }
    }
    
    private boolean isOnlyOneSubEstadoFirst() {
        int count = 0;
        if(current.getSubEstadoCasoList() != null){
            for (SubEstadoCaso subEstadoCaso : current.getSubEstadoCasoList()) {
                if(subEstadoCaso.isFirst()){
                    count++;
                }
            }
        }
        return (count == 1);
    }

    public String prepareEdit() {
        current = (TipoCaso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/tipoCaso/Edit";
    }

    public String update() {
        try {
            
            checkValidateSubEstados();
            if(!isOnlyOneSubEstadoFirst()){
                addErrorMessage("Solo un sub-estado puede ser el inicial.");
                return null;
            }
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

    public Long countCasosByTipo(TipoCaso tipo) {
        EasyCriteriaQuery<Caso> c = new EasyCriteriaQuery<>(getJpaController(), Caso.class);
        c.addEqualPredicate("tipoCaso", tipo);
        return c.count();
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
