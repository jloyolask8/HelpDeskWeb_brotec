/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JPAFilterHelper;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Recinto;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.webapputils.UAgentInfo;
import com.itcs.jpautils.EasyCriteriaQuery;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;
import org.primefaces.context.RequestContext;

/**
 *
 * @author jonathan
 */
public abstract class AbstractManagedBean<E> implements Serializable {

    private Class<E> entityClass;
    @Resource
    protected UserTransaction utx = null;
    @PersistenceUnit(unitName = "helpdeskPU")
    protected EntityManagerFactory emf = null;
    protected transient JPAServiceFacade jpaController = null;
    protected transient ManagerCasos managerCasos;
    protected transient DataModel items = null;
    protected transient PaginationHelper pagination;
    private int paginationPageSize = 10;
    protected E current;
    private ArrayList<E> selectedItems;
    private JPAFilterHelper filterHelper;

    public AbstractManagedBean(Class<E> entityClass) {
        this.entityClass = entityClass;

    }

    public abstract Class getDataModelImplementationClass();

    public String getDefaultOrderBy() {
        return null;
    }

    public Usuario getDefaultUserWho() {
        return null;
    }

//    public abstract PaginationHelper getPagination();
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                private Integer countCache = null;

                @Override
                public int getItemsCount() {
                    try {
                        if (countCache == null) {
                            countCache = getJpaController().countEntities(getFilterHelper().getVista(), getDefaultUserWho()).intValue();// getJpaController().count(Caso.class).intValue();
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
                        DataModel<E> dataModel = (DataModel) getDataModelImplementationClass().newInstance();
                        dataModel.setWrappedData(getJpaController().findEntities(entityClass, getFilterHelper().getVista(), getPageSize(), getPageFirstItem(), getDefaultOrderBy(), getDefaultUserWho()));
                        return dataModel;
                    } catch (Exception ex) {
                        JsfUtil.addErrorMessage(ex, "Lo sentimos, ocurrió un error inesperado. Favor intente más tarde.");
                        Logger.getLogger(AbstractManagedBean.class.getName()).log(Level.SEVERE, "createPageDataModel", ex);
                    }
                    return null;
//                    return new ListDataModel(getJpaController().queryByRange(entityClass, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//        return items;
//    }
//    
    protected void recreateModel() {
        items = null;
    }

    protected void recreatePagination() {
        pagination = null;
    }

    public JPAFilterHelper getFilterHelper() {
        if (filterHelper == null) {
            filterHelper = new JPAFilterHelper(new Vista(entityClass), emf) {
                @Override
                public JPAServiceFacade getJpaService() {
                    return getJpaController();
                }
            };
        }
        return filterHelper;
    }

    public void filter() {
        recreatePagination();
        recreateModel();
    }

    /**
     * ejemplo de llamada a este metdodo:
     * executeInClient("WidgetVarName.show();");
     *
     * @param command
     */
    protected void executeInClient(String command) {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute(command);
    }

    protected void refreshPage() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String refreshpage = fc.getViewRoot().getViewId();
        ViewHandler ViewH = fc.getApplication().getViewHandler();
        UIViewRoot UIV = ViewH.createView(fc, refreshpage);
        UIV.setViewId(refreshpage);
        fc.setViewRoot(UIV);
    }

    public void next() {
        getPagination().nextPage();
        recreateModel();
    }

    public void previous() {
        getPagination().previousPage();
        recreateModel();
    }

    public void last() {
        getPagination().lastPage();
        recreateModel();
    }

    public void first() {
        getPagination().firstPage();
        recreateModel();
    }

    public void resetPageSize() {
        recreatePagination();
        recreateModel();
    }

    public JPAServiceFacade getJpaController() {
        if (jpaController == null) {
            jpaController = new JPAServiceFacade(utx, emf);
        }
        return jpaController;
    }

    protected ManagerCasos getManagerCasos() {
        if (null == managerCasos) {
            managerCasos = new ManagerCasos();
            managerCasos.setJpaController(getJpaController());
        }
        return managerCasos;
    }

    public void addMessageTo(FacesMessage.Severity sev, String clientId, String sumary) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, sumary, null);
        FacesContext.getCurrentInstance().addMessage(clientId, facesMsg);
    }

    public void addMessage(FacesMessage.Severity sev, String sumary) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, sumary, null);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public void addErrorMessage(String sumary) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, sumary, null);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public void addInfoMessage(String sumary) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, sumary, null);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public void addWarnMessage(String sumary) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, sumary, null);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public void addMessage(FacesMessage.Severity sev, String sumary, String detail) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, sumary, detail);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    /**
     * @return the paginationPageSize
     */
    public int getPaginationPageSize() {
        return paginationPageSize;
    }

    /**
     * @param paginationPageSize the paginationPageSize to set
     */
    public void setPaginationPageSize(int paginationPageSize) {
        this.paginationPageSize = paginationPageSize;
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(getJpaController().findAll(entityClass), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(getJpaController().findAll(entityClass), true);
    }

    public List getItemsAvailableForSelect() {
        return getJpaController().findAll(entityClass);
    }

    /**
     * @return the selected
     */
    public E getSelected() {
        return current;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(E selected) {
        this.current = selected;
    }

    /**
     * @return the selectedItems
     */
    public ArrayList<E> getSelectedItems() {
        return selectedItems;
    }

    /**
     * @param selectedItems the selectedItems to set
     */
    public void setSelectedItems(ArrayList<E> selectedItems) {
        this.selectedItems = selectedItems;
    }

    /**
     * @return the entityClass
     */
    public Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * mothod to determine the browser agent of client!
     *
     * @param request
     * @return
     */
    protected boolean isThisRequestCommingFromAMobileDevice(HttpServletRequest request) {

        // http://www.hand-interactive.com/m/resources/detect-mobile-java.htm
        String userAgent = request.getHeader("User-Agent");
        String httpAccept = request.getHeader("Accept");

        UAgentInfo detector = new UAgentInfo(userAgent, httpAccept);

        if (detector.detectMobileQuick()) {
            return true;
        }

        if (detector.detectTierTablet()) {
            return true;
        }

        return false;
    }

    /**
     * ejemplo: redirectToView("customer/ticket");
     *
     * @param viewId
     */
    protected void redirectToView(String viewId) {
//        ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
//        nav.performNavigation(viewId);
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String path_ticket = request.getContextPath() + "/faces/" + viewId + ".xhtml";
            FacesContext.getCurrentInstance().getExternalContext().redirect(path_ticket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String redirect(String page) {

        FacesContext ctx = FacesContext.getCurrentInstance();

        ExternalContext extContext = ctx.getExternalContext();
        String url = extContext.encodeActionURL(ctx.getApplication().getViewHandler().getActionURL(ctx, page));
        try {

            extContext.redirect(url);
        } catch (IOException ioe) {
            throw new FacesException(ioe);

        }
        return null;

    }
}
