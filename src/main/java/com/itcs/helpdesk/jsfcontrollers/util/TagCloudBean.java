/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.persistence.entities.Etiqueta;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.json.JSONArray;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudItem;
import org.primefaces.model.tagcloud.TagCloudModel;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "tagCloudBean")
@SessionScoped
public class TagCloudBean extends AbstractManagedBean<Etiqueta> implements Serializable {

    
    private transient TagCloudModel model;

    /**
     * Creates a new instance of TagCloudBean
     */
    public TagCloudBean() {
        super(Etiqueta.class);
    }
    
     @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("This operation is Not supported!.");
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
    public TagCloudModel getEtiquetasByUsuario() {
        List<Etiqueta> etiquetas = (List<Etiqueta>) getJpaController().findEtiquetasByUsuario(getUserSessionBean().getCurrent().getIdUsuario());
        model = new DefaultTagCloudModel();
        for (Etiqueta etiqueta : etiquetas) {
            model.addTag(new DefaultTagCloudItem(etiqueta.getTagId(), getJpaController().countCasosByEtiqueta(etiqueta).intValue()));
        }
        return model;
    }
    
    public List<Etiqueta> getEtiquetasListByUsuario(){
        return (List<Etiqueta>) getJpaController().findEtiquetasByUsuario(getUserSessionBean().getCurrent().getIdUsuario());
    }

    public String getTagJSonList() {
        JSONArray list = new JSONArray((List<Etiqueta>) getJpaController().findAll(Etiqueta.class));
        return list.toString();
    }

    public List<Etiqueta> getTagList() {
        return (List<Etiqueta>) getJpaController().findAll(Etiqueta.class);
    }

    /**
     * @param model the model to set
     */
    public void setModel(TagCloudModel model) {
        this.model = model;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
