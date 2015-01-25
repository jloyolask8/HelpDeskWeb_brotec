/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.jsfcontrollers.CasoController;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Etiqueta;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.primefaces.event.SelectEvent;
import org.primefaces.json.JSONArray;
import org.primefaces.model.tagcloud.TagCloudItem;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "tagCloudBean")
@SessionScoped
public class TagCloudBean extends AbstractManagedBean<Etiqueta> implements Serializable {

    /**
     * Creates a new instance of TagCloudBean
     */
    public TagCloudBean() {
        super(Etiqueta.class);
    }

    @Override
    public OrderBy getDefaultOrderBy() {
        return new OrderBy("tagId");
    }

    @Override
    public Usuario getDefaultUserWho() {
        return getUserSessionBean().getCurrent();
    }

    @Override
    public Vista getVista() {
        Vista vista1 = new Vista(Etiqueta.class);

        FiltroVista f1 = new FiltroVista();
        f1.setIdCampo("owner");
        f1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        f1.setValor(getUserSessionBean().getCurrent().getIdUsuario());
        f1.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(f1);

        FiltroVista f2 = new FiltroVista();
        f2.setIdCampo("casoList");
        f2.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        f2.setValor(AbstractJPAController.PLACE_HOLDER_ANY);//not empty
        f2.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(f2);
        return vista1;
    }

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                private Integer countCache = null;

                @Override
                public int getItemsCount() {
                    try {
                        if (countCache == null) {
                            countCache = getJpaController().countEntities(getVista(), getDefaultUserWho(), getQuery()).intValue();// getJpaController().count(Caso.class).intValue();
                        }

                        return countCache;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "ClassNotFoundException at getItemsCount", ex);
                    }
                    return 0;
                }

                @Override
                public DataModel createPageDataModel() {
                    try {
                        ListDataModel<Etiqueta> dataModel = (ListDataModel) getDataModelImplementationClass().newInstance();

                        final List<?> data = getJpaController().findEntities(getVista(), getPageSize(), getPageFirstItem(), getDefaultOrderBy(), getDefaultUserWho(), getQuery());

                        if (Comparable.class.isAssignableFrom(getEntityClass())) {
                            Collections.sort((List<Comparable>) data);
                        }

                        dataModel.setWrappedData(data);
                        return dataModel;
                    } catch (IllegalStateException ex) {//error en el filtro
                        JsfUtil.addErrorMessage(ex, "Existe un problema con el filtro. Favor corregir e intentar nuevamente.");
                    } catch (ClassNotFoundException ex) {
                        JsfUtil.addErrorMessage(ex, "Lo sentimos, ocurrió un error inesperado. Favor contactar a soporte.");
                        Logger.getLogger(AbstractManagedBean.class.getName()).log(Level.SEVERE, "ClassNotFoundException createPageDataModel", ex);
                    } catch (IllegalAccessException ex) {
                        JsfUtil.addErrorMessage(ex, "Lo sentimos, ocurrió un error inesperado. Favor contactar a soporte.");
                        Logger.getLogger(AbstractManagedBean.class.getName()).log(Level.SEVERE, "IllegalAccessException createPageDataModel", ex);
                    } catch (InstantiationException ex) {
                        JsfUtil.addErrorMessage(ex, "Lo sentimos, ocurrió un error inesperado. Favor contactar a soporte.");
                        Logger.getLogger(AbstractManagedBean.class.getName()).log(Level.SEVERE, "InstantiationException createPageDataModel", ex);
                    }
                    return null;
                }
            };
        }
        return pagination;
    }

    public void onEtiquetaSelected(String tagId) {
        Vista copy = new Vista(Caso.class);
        copy.setNombre(tagId);

        FiltroVista fCopy = new FiltroVista();
        fCopy.setIdFiltro(1);
        fCopy.setIdCampo("etiquetaList");
        fCopy.setIdComparador(EnumTipoComparacion.IM.getTipoComparacion());
        fCopy.setValor(tagId);
        fCopy.setIdVista(copy);

        copy.getFiltrosVistaList().add(fCopy);
        final CasoController casoController = (CasoController) JsfUtil.getManagedBean("casoController");

        casoController.setVista(copy);

        casoController.recreateModel();
        
        recreateModel();
        recreatePagination();

//        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Etiqueta Seleccionada:" + tagId, "");
//        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onSelect(SelectEvent event) {
        TagCloudItem item = (TagCloudItem) event.getObject();
        System.out.println("item:" + item.getLabel());
        System.out.println("item_url:" + item.getUrl());
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Selected", item.getLabel());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * @return the model
     *
     */
    public List<Etiqueta> getEtiquetasByUsuario() {
        try {
            
//            Vista vista1 = new Vista(Etiqueta.class);
//
//            FiltroVista f1 = new FiltroVista();
//            f1.setIdCampo("owner");
//            f1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
//            f1.setValor(getUserSessionBean().getCurrent().getIdUsuario());
//            f1.setIdVista(vista1);
//
//            vista1.getFiltrosVistaList().add(f1);
//
//            FiltroVista f2 = new FiltroVista();
//            f2.setIdCampo("casoList");
//            f2.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
//            f2.setValor(AbstractJPAController.PLACE_HOLDER_ANY);//not empty
//            f2.setIdVista(vista1);
//
//            vista1.getFiltrosVistaList().add(f2);
//
//            List<Etiqueta> etiquetas = (List<Etiqueta>) getJpaController().findEntities(vista1, 20, 0, new OrderBy("tagId"), null);
//        model = new DefaultTagCloudModel();
//        for (Etiqueta etiqueta : etiquetas) {
//            final int count = getJpaController().countCasosByEtiqueta(etiqueta).intValue();
//            if(count > 0){
//                model.addTag(new DefaultTagCloudItem(etiqueta.getTagId(), count));
//            }
//            
//        }

            return (List<Etiqueta>)getItems().getWrappedData();
        } catch (Exception ex) {
            addErrorMessage("Error: " + ex.getMessage());
            Logger.getLogger(TagCloudBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.EMPTY_LIST;
    }

    public List<Etiqueta> getEtiquetasListByUsuario() {
        return getEtiquetasByUsuario();
    }

    public String getTagJSonList() {
        JSONArray list = new JSONArray((List<Etiqueta>) getJpaController().findAll(Etiqueta.class));
        return list.toString();
    }

    public List<Etiqueta> getTagList() {
        return (List<Etiqueta>) getJpaController().findAll(Etiqueta.class);
    }

//    /**
//     * @param model the model to set
//     */
//    public void setModel(TagCloudModel model) {
//        this.model = model;
//    }
    @Override
    public Class getDataModelImplementationClass() {
        return ListDataModel.class;
    }

}
