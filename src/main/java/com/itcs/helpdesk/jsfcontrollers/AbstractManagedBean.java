/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JPAFilterHelper;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.util.DateUtils;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.webapputils.UAgentInfo;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import javax.resource.NotSupportedException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.HttpHeaders;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.ocpsoft.prettytime.PrettyTime;
import org.primefaces.context.RequestContext;

/**
 *
 * @author jonathan
 * @param <E>
 */
public abstract class AbstractManagedBean<E> implements Serializable {

    @Resource
    protected UserTransaction utx = null;
    @PersistenceUnit(unitName = "helpdeskPU")
    protected EntityManagerFactory emf = null;

    //go back button
//    private String backOutcome;
    protected static final Locale LOCALE_ES_CL = new Locale("es", "CL");
    protected static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, dd 'de' MMMM 'de' yyyy HH:mm", LOCALE_ES_CL);
    protected static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("HH:mm");
    protected static final SimpleDateFormat monthDateFormat = new SimpleDateFormat("dd MMM", LOCALE_ES_CL);
    protected static final SimpleDateFormat yearDateFormat = new SimpleDateFormat("dd/MM/yy", LOCALE_ES_CL);

    protected static final SimpleDateFormat monthDateFormatWTime = new SimpleDateFormat("EEE, dd MMM HH:mm", LOCALE_ES_CL);
    protected static final SimpleDateFormat yearDateFormatWTime = new SimpleDateFormat("dd/MM/yy HH:mm", LOCALE_ES_CL);

    private final Class<E> entityClass;

    protected transient JPAServiceFacade jpaController = null;
    protected transient ManagerCasos managerCasos;
    protected transient DataModel items = null;
    protected transient PaginationHelper pagination;
    private int paginationPageSize = 20;
    protected E current;
    private List<E> selectedItems;
    private JPAFilterHelper filterHelper;
    private JPAFilterHelper casoFilterHelper;

    private String query = null;
    private boolean filterViewToggle = false;
    List<Vista> allMyVistas = null;

    private Vista vista;

    protected String backOutcome;

    public String goBack() {
        if (this.backOutcome == null) {
            recreateModel();
            return getListPage();
        } else {
            //TODO Callback to the mbean and refresh vistas
            return this.backOutcome;
        }
    }

    private static final Comparator<Vista> comparadorVistas = new Comparator<Vista>() {
        @Override
        public int compare(Vista o1, Vista o2) {
            return o1.getNombre().compareTo(o2.getNombre());
        }
    };

    public AbstractManagedBean(Class<E> entityClass) {
        this.entityClass = entityClass;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0} for {1} created", new Object[]{this.getClass().getSimpleName(), entityClass.getSimpleName()});
    }

    public void showMessageInDialog(FacesMessage.Severity severity, String msg, String detail) {
        FacesMessage message = new FacesMessage(severity, msg, detail);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public boolean isAjaxRequest() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    protected abstract Class getDataModelImplementationClass();

    public OrderBy getDefaultOrderBy() {
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
                        DataModel<E> dataModel = (DataModel) getDataModelImplementationClass().newInstance();

                        final List<?> data = getJpaController().findEntities(getVista(), getPageSize(), getPageFirstItem(), getDefaultOrderBy(), getDefaultUserWho(), getQuery());

                        if (Comparable.class.isAssignableFrom(entityClass)) {
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
                    } catch (NotSupportedException ex) {
                        addWarnMessage("Lo sentimos, ocurrió un error inesperado. La acción que desea realizar aún no esta soportada por el sistema.");
                    }
                    return null;
                }
            };
        }
        return pagination;
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    protected void recreateModel() {
        items = null;
    }

    protected void recreatePagination() {
        pagination = null;
    }

    public JPAFilterHelper getFilterHelper() {
        if (filterHelper == null) {
            filterHelper = new JPAFilterHelper((entityClass).getName()) {
                @Override
                public JPAServiceFacade getJpaService() {
                    return getJpaController();
                }
            };
        }
        return filterHelper;
    }

    public JPAFilterHelper getCasoFilterHelper() {
        if (casoFilterHelper == null) {
            casoFilterHelper = new JPAFilterHelper((Caso.class).getName()) {
                @Override
                public JPAServiceFacade getJpaService() {
                    return getJpaController();
                }
            };
        }
        return casoFilterHelper;
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

    protected void updateComponentInClient(String componentId) {
        RequestContext.getCurrentInstance().update(componentId);
    }

    /**
     * RequestContext is a helper with various utilities. Update component(s)
     * programmatically. Execute javascript from beans. Add ajax callback
     * parameters as JSON. ScrollTo a specific component after ajax update.
     * Invoke conditional javascript on page load.
     *
     * @return RequestContext
     */
    protected RequestContext getPrimefacesRequestContext() {
        return RequestContext.getCurrentInstance();
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

    public void addErrorMessage(String sumary, String detail) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, sumary, detail);
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

    protected UserSessionBean getUserSessionBean() {
        return (UserSessionBean) JsfUtil.getManagedBean("UserSessionBean");
    }

    protected CasoController getCasoControllerBean() {
        return (CasoController) JsfUtil.getManagedBean("casoController");
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
    public List<E> getSelectedItems() {
        return selectedItems;
    }

    /**
     * @param selectedItems the selectedItems to set
     */
    public void setSelectedItems(List<E> selectedItems) {
        this.selectedItems = selectedItems;
    }

    /**
     * @return the entityClass
     */
    public Class<E> getEntityClass() {
        return entityClass;
    }

    public String applyViewFilter(Vista vista) {

        setQuery(null);

        try {
            Vista copy = new Vista();
            copy.setIdVista(vista.getIdVista());
            copy.setBaseEntityType(vista.getBaseEntityType());
//            copy.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
            copy.setDescripcion(vista.getDescripcion());
            copy.setIdArea(vista.getIdArea());
            copy.setIdGrupo(vista.getIdGrupo());
            copy.setNombre(vista.getNombre());
            copy.setVisibleToAll(vista.getVisibleToAll());
            if (copy.getFiltrosVistaList() == null) {
                copy.setFiltrosVistaList(new ArrayList<FiltroVista>());
            }
            //Crearemos una copia para que al guardar no pisar la existente.
            int i = 1;
            if (vista.getFiltrosVistaList() != null) {
                for (FiltroVista f : vista.getFiltrosVistaList()) {
                    FiltroVista fCopy = new FiltroVista();
                    fCopy.setIdFiltro(i);
                    i++; //This is an ugly patch to solve issue when removing a filter from the view, if TODO: Warning - this method won't work in the case the id fields are not set
                    fCopy.setIdCampo(f.getIdCampo());
                    fCopy.setIdComparador(f.getIdComparador());
                    fCopy.setValor(f.getValor());
                    fCopy.setValorLabel(f.getValorLabel());
                    fCopy.setValor2(f.getValor2());
                    fCopy.setValor2Label(f.getValor2Label());
                    fCopy.setIdVista(copy);
                    copy.getFiltrosVistaList().add(fCopy);
                    //System.out.println("added filtro " + fCopy);
                }
            }

            if(copy.getFiltrosVistaList().isEmpty()){
                copy.addNewFiltroVista();
            }
            setVista(copy);
//        this.setVista(copy);
//        //System.out.println("Vista copy set:" + copy);
            recreatePagination();
//            recreateModel();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }

        return prepareList();//"inbox";

    }

    /**
     * mothod to determine the browser agent of client!
     *
     * @param request
     * @return
     */
    protected boolean isThisRequestCommingFromAMobileDevice(HttpServletRequest request) {

        // http://www.hand-interactive.com/m/resources/detect-mobile-java.htm
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String httpAccept = request.getHeader(HttpHeaders.ACCEPT);

        UAgentInfo detector = new UAgentInfo(userAgent, httpAccept);

        if (detector.detectMobileQuick()) {
            return true;
        }

        if (detector.detectTierTablet()) {
            return false;
        }

        return false;
    }

    protected UAgentInfo getUAgentInfo(HttpServletRequest request) {

        // http://www.hand-interactive.com/m/resources/detect-mobile-java.htm
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String httpAccept = request.getHeader(HttpHeaders.ACCEPT);

        UAgentInfo detector = new UAgentInfo(userAgent, httpAccept);
        return detector;

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
            String path_ticket = request.getContextPath() + request.getServletPath() + "/" + viewId + ".xhtml";
            FacesContext.getCurrentInstance().getExternalContext().redirect(path_ticket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * redirecciona un request ajax a una pagina especifica al parecer solo
     * funciona desde un filter no desde un managed bean =(
     *
     * @param request
     * @param page
     * @return
     */
    public String xmlPartialRedirectToPage(String page) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='UTF-8'?>");
        sb.append("<partial-response><redirect url=\"").append(request.getContextPath()).append(request.getServletPath()).append(page).append("\"/></partial-response>");
        Logger.getLogger(AbstractManagedBean.class.getName()).log(Level.SEVERE, "xmlPartialRedirectToPage:{0}", sb.toString());
        return sb.toString();
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

    public String prettyDate(Date date) {

        if (date != null) {
            PrettyTime p = new PrettyTime(new Locale("es"));
            return p.format(date);
//            return PrettyDate.format(date);
        } else {
            return "";
        }
    }

    public String formatShortDate(Date date) {
        if (date != null) {

            if (DateUtils.isToday(date)) {
                return dayDateFormat.format(date);
            } else if (DateUtils.isThisYear(date)) {
                return monthDateFormat.format(date);
            } else {
                return yearDateFormat.format(date);
            }
        } else {
            return "";
        }
    }

    public String formatDate(Date date) {
        if (date != null) {
            return fullDateFormat.format(date);
        } else {
            return "";
        }
    }

    public String formatShortDateTime(Date date) {
        if (date != null) {

            if (DateUtils.isToday(date)) {
                return dayDateFormat.format(date);
            } else if (DateUtils.isThisYear(date)) {
                return monthDateFormatWTime.format(date);
            } else {
                return yearDateFormatWTime.format(date);
            }
        } else {
            return "";
        }
    }

    public String formatDateRange(Date start, Date end) {

        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", LOCALE_ES_CL);

        DateTime dtStart = new DateTime(start);
        DateTime dtEnd = new DateTime(end);

        String formattedDate = "";

        if (start != null) {
            if (dtStart.isAfter(dtEnd) || end == null) {
                //illegal!!
                formattedDate = formatShortDateTime(start);
            } else {
                if (dtStart.withTimeAtStartOfDay().isEqual(dtEnd.withTimeAtStartOfDay())) {//same day?
                    formattedDate = formatShortDateTime(start) + " - " + sdf2.format(end);
                } else {
                    formattedDate = formatShortDateTime(start) + " - " + formatShortDateTime(end);
                }
            }
        }

        return formattedDate;
    }

    public String formatDurationRange(Date start, Date end) {
        if (start == null || end == null) {
            return "";
        }
        Interval interval = new Interval(new DateTime(start), new DateTime(end));
        return interval.toDuration().toString();
    }

    public String prepareView(E entity) {
        if (entity == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione un item para editar.");
            return null;
        }
        setSelected(entity);
        this.backOutcome = null;
        return getViewPage();
    }

    public String prepareView(E entity, String backOutcome) {
        if (entity == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione un item para editar.");
            return null;
        }
        setSelected(entity);
        this.backOutcome = backOutcome;
        return getViewPage();
    }

    protected void beforeSetSelected() {

    }

    protected void beforePrepareList(){

    }

    protected void afterSetSelected() {

    }

    public String prepareEdit(E entity) {
        if (entity == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione un item para editar.");
            return null;
        }

        beforeSetSelected();

        setSelected(entity);

        afterSetSelected();
        this.backOutcome = null;
        return getEditPage();
    }

    public String prepareEdit(E entity, String backOutcome) {
        prepareEdit(entity);
        this.backOutcome = backOutcome;
        return getEditPage();
    }

    public String prepareEditPK(Object entityPK) {
        try {
            if (entityPK == null) {
                return null;
            }
            E entity = getJpaController().find(entityClass, entityPK);

            beforeSetSelected();

            setSelected(entity);

            afterSetSelected();

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "prepareEdit (" + entityPK + ")", ex);
        } finally {
            this.backOutcome = null;
        }

        return getEditPage();
    }

    public String prepareEditPK(Object entityPK, String backOutcome) {

        prepareEditPK(entityPK);
        this.backOutcome = backOutcome;

        return getEditPage();
    }

    public String prepareList() {
        beforePrepareList();
        recreateModel();
        return getListPage();
    }

    /**
     * When done with experiment this method should be abstract
     *
     * @return the list page to outcome
     */
//    protected abstract String getListPage();
    protected String getEditPage() {
        return null;
    }

    /**
     * When done with experiment this method should be abstract
     *
     * @return the list page to outcome
     */
//    protected abstract String getListPage();
    protected String getViewPage() {
        return null;
    }

    /**
     * When done with experiment this method should be abstract
     *
     * @return the list page to outcome
     */
//    protected abstract String getListPage();
    protected String getListPage() {
        return null;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the filterViewToggle
     */
    public boolean isFilterViewToggle() {
        return filterViewToggle;
    }

    /**
     * @param filterViewToggle the filterViewToggle to set
     */
    public void setFilterViewToggle(boolean filterViewToggle) {
        this.filterViewToggle = filterViewToggle;
    }

    public void resetVistas() {
        allMyVistas = null;
    }

    public int countItemsVista(Vista vista) {
        try {
            UserSessionBean userSessionBean = getUserSessionBean();
            final Long countEntities = getJpaController().countEntities(vista, userSessionBean.getCurrent(), null);
            return countEntities.intValue();
        } catch (Exception ex) {
            addErrorMessage(ex.getMessage());
            Logger.getLogger(VistaController.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }

    }

    /**
     *
     * @param user
     * @return
     */
    public List<Vista> getAllAgentVistasItems() {

        if (allMyVistas == null) {
            List<Vista> lista = new ArrayList<>();
            final List<Vista> visibleForAllItems = getVisibleForAllItems(getEntityClass().getName());
            final UserSessionBean userSessionBean = getUserSessionBean();
            final List<Vista> visibleForMeOnlyItems = getVisibleForMeOnlyItems(userSessionBean.getCurrent(), getEntityClass().getName());
            final List<Vista> visibleForMyGroupsItems = getVisibleForMyGroupsItems(userSessionBean.getCurrent(), getEntityClass().getName());
            final List<Vista> visibleForMyAreasItems = getVisibleForMyAreasItems(userSessionBean.getCurrent(), getEntityClass().getName());

            if (visibleForAllItems != null) {
                for (Vista v : visibleForAllItems) {
                    if (!lista.contains(v)) {
                        lista.addAll(visibleForAllItems);
                    }
                }
            }

            if (visibleForMeOnlyItems != null) {
                for (Vista v : visibleForMeOnlyItems) {
                    if (!lista.contains(v)) {
                        lista.addAll(visibleForMeOnlyItems);
                    }
                }
            }

            if (visibleForMyGroupsItems != null) {
                for (Vista v : visibleForMyGroupsItems) {
                    if (!lista.contains(v)) {
                        lista.addAll(visibleForMyGroupsItems);
                    }
                }
            }

            if (visibleForMyAreasItems != null) {
                for (Vista v : visibleForMyAreasItems) {
                    if (!lista.contains(v)) {
                        lista.addAll(visibleForMyAreasItems);
                    }
                }
            }
            Collections.sort(lista, comparadorVistas);
            this.allMyVistas = lista;
//            System.out.println("allMyVistas:"+allMyVistas);
        }

        return this.allMyVistas;
    }

    /**
     * @param baseEntityType
     * @deprecated @return
     */
    public List<Vista> getVisibleForAllItems(String baseEntityType) {
        return getJpaController().findVistaEntitiesVisibleForAll(baseEntityType);
    }

    /**
     * @param user
     * @param baseEntityType
     * @deprecated @return
     */
    public List<Vista> getVisibleForMeOnlyItems(Usuario user, String baseEntityType) {
        return getJpaController().findVistaEntitiesCreatedByUser(user, baseEntityType);
    }

    /**
     * @param user
     * @param baseEntityType
     * @deprecated @return
     */
    public List<Vista> getVisibleForMyGroupsItems(Usuario user, String baseEntityType) {
        return getJpaController().findVistaEntitiesVisibleForGroupsOfUser(user, baseEntityType);
    }

    /**
     * @param user
     * @param baseEntityType
     * @deprecated @return
     */
    public List<Vista> getVisibleForMyAreasItems(Usuario user, String baseEntityType) {
        return getJpaController().findVistaEntitiesVisibleForAreasOfUser(user, baseEntityType);
    }

    /**
     * @return the vista
     */
    public Vista getVista() {

        if (vista == null) {
            vista = new Vista(entityClass);
            if (vista.getFiltrosVistaList() == null || vista.getFiltrosVistaList().isEmpty()) {
                vista.addNewFiltroVista();
            }
        }
        return vista;

    }

    /**
     * @param vista the vista to set
     */
    public void setVista(Vista vista) {
        this.vista = vista;
    }

}
