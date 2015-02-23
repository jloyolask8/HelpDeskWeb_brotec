/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Resource;
import com.itcs.helpdesk.persistence.entities.ScheduleEventReminder;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.quartz.ActionClassExecutorJob;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.quartz.ScheduleEventReminderJob;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.view.Colors;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.quartz.SchedulerException;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "globalScheduleController")
@SessionScoped
public class GlobalScheduleController extends AbstractManagedBean<com.itcs.helpdesk.persistence.entities.ScheduleEvent> implements Serializable {

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{casoController}")
    private CasoController casoController;
    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;
    private ScheduleModel lazyScheduleEventsModel;
    private DefaultScheduleEvent event = new DefaultScheduleEvent();

    private List<Usuario> filtrosUsuario = new LinkedList<>();
    private List<Resource> filtrosRecurso = new LinkedList<>();
    private boolean insideCasoDisplay = false;//global

    //temp vars
    private Usuario selectedUserToAddInvited;
    private Resource selectedResourceToAddInvited;

    public GlobalScheduleController() {

        super(com.itcs.helpdesk.persistence.entities.ScheduleEvent.class);

        lazyScheduleEventsModel = new LazyScheduleModel() {
            @Override
            public void addEvent(ScheduleEvent event) {
                super.addEvent(event);
            }

            @Override
            public boolean deleteEvent(ScheduleEvent event) {
                return super.deleteEvent(event);
            }

            @Override
            public void updateEvent(ScheduleEvent event) {
                super.updateEvent(event);
            }

            @Override
            public ScheduleEvent getEvent(String id) {
                return super.getEvent(id);
            }

            @Override
            public void loadEvents(Date start, Date end) {
                try {
//                    getFilterHelper().getVista().setFiltrosVistaList(new ArrayList<FiltroVista>(2));

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    if (filtrosUsuario != null && !filtrosUsuario.isEmpty()) {

                        for (Usuario usuario : filtrosUsuario) {

                            List<FiltroVista> filtrosVistaList = new ArrayList<>(2);

                            //add date filter
                            FiltroVista filtroFechaDesdeHasta = new FiltroVista();
                            filtroFechaDesdeHasta.setIdFiltro(1);//otherwise i dont know what to remove dude.
                            filtroFechaDesdeHasta.setIdCampo("startDate");
                            filtroFechaDesdeHasta.setIdComparador(EnumTipoComparacion.BW.getTipoComparacion());
                            filtroFechaDesdeHasta.setValor(sdf.format(start));//desde
                            filtroFechaDesdeHasta.setValor2(sdf.format(end));//hasta
                            filtroFechaDesdeHasta.setIdVista(getVista());
                            filtrosVistaList.add(filtroFechaDesdeHasta);

                            //usuariosInvitedList filter
                            FiltroVista fu = new FiltroVista();
                            fu.setIdFiltro(2);//otherwise i dont know what to remove dude.
                            fu.setIdCampo("usuariosInvitedList");
                            fu.setIdComparador(EnumTipoComparacion.IM.getTipoComparacion());
                            fu.setValor(usuario.getIdUsuario());
                            fu.setIdVista(getVista());
                            filtrosVistaList.add(fu);

                            //set filters to vista
                            getVista().setFiltrosVistaList(filtrosVistaList);

                            final List<com.itcs.helpdesk.persistence.entities.ScheduleEvent> findEntities
                                    = (List<com.itcs.helpdesk.persistence.entities.ScheduleEvent>) getJpaController().findAllEntities(getVista(), new OrderBy("startDate", OrderBy.OrderType.DESC), null);
//                    //System.out.println("events:" + findEntities);
                            for (com.itcs.helpdesk.persistence.entities.ScheduleEvent scheduleEvent : findEntities) {
                                final DefaultScheduleEvent defaultScheduleEvent
                                        = new DefaultScheduleEvent(scheduleEvent.getTitle(),
                                                scheduleEvent.getStartDate(), scheduleEvent.getEndDate());
                                defaultScheduleEvent.setAllDay(scheduleEvent.getAllDay());
                                defaultScheduleEvent.setData(scheduleEvent);
                                defaultScheduleEvent.setStyleClass(usuario.getIdUsuario());

                                addEvent(defaultScheduleEvent);
                            }
                        }
                    }

                    if (filtrosRecurso != null && !filtrosRecurso.isEmpty()) {

                        for (Resource r : filtrosRecurso) {

                            List<FiltroVista> filtrosVistaList = new ArrayList<>(2);

                            //add date filter
                            FiltroVista filtroFechaDesdeHasta = new FiltroVista();
                            filtroFechaDesdeHasta.setIdFiltro(1);//otherwise i dont know what to remove dude.
                            filtroFechaDesdeHasta.setIdCampo("startDate");
                            filtroFechaDesdeHasta.setIdComparador(EnumTipoComparacion.BW.getTipoComparacion());
                            filtroFechaDesdeHasta.setValor(sdf.format(start));//desde
                            filtroFechaDesdeHasta.setValor2(sdf.format(end));//hasta
                            filtroFechaDesdeHasta.setIdVista(getVista());
                            filtrosVistaList.add(filtroFechaDesdeHasta);

                            //usuariosInvitedList filter
                            FiltroVista fu = new FiltroVista();
                            fu.setIdFiltro(2);//otherwise i dont know what to remove dude.
                            fu.setIdCampo("resourceList");
                            fu.setIdComparador(EnumTipoComparacion.IM.getTipoComparacion());
                            fu.setValor(r.getIdResource().toString());
                            fu.setIdVista(getVista());
                            filtrosVistaList.add(fu);

                            //set filters to vista
                            getVista().setFiltrosVistaList(filtrosVistaList);

                            final List<com.itcs.helpdesk.persistence.entities.ScheduleEvent> findEntities
                                    = (List<com.itcs.helpdesk.persistence.entities.ScheduleEvent>) getJpaController().findAllEntities(getVista(), new OrderBy("startDate", OrderBy.OrderType.DESC), null);
//                    //System.out.println("events:" + findEntities);
                            for (com.itcs.helpdesk.persistence.entities.ScheduleEvent scheduleEvent : findEntities) {
                                final DefaultScheduleEvent defaultScheduleEvent
                                        = new DefaultScheduleEvent(scheduleEvent.getTitle(),
                                                scheduleEvent.getStartDate(), scheduleEvent.getEndDate());
                                defaultScheduleEvent.setAllDay(scheduleEvent.getAllDay());
                                defaultScheduleEvent.setData(scheduleEvent);
                                defaultScheduleEvent.setStyleClass("res"+r.getIdResource().toString());

                                addEvent(defaultScheduleEvent);
                            }
                        }
                    }
                    
                   
//                    //System.out.println("VISTA=" + getFilterHelper().getVista());
                } catch (Exception ex) {
                    Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "loadEvents", ex);
                }

            }
        };

        //System.out.println("new CasoScheduleController()");

    }

    @Override
    protected String getListPage() {
        return "/script/agenda?faces-redirect=true";
    }

    
    @Override
    public String prepareList() {
        recreateModel();
        recreatePagination();
        if(this.filtrosUsuario == null || this.filtrosUsuario.isEmpty()){
            filtrosUsuario = new LinkedList<>();
            addUsuarioToTheList(userSessionBean.getCurrent());
        }
        return getListPage();
    }

    public DefaultScheduleEvent getEvent() {
        return event;
    }

    public void setEvent(DefaultScheduleEvent event) {
        this.event = event;
    }

    public void removeAllUsuariosFilters() {
        this.filtrosUsuario = new LinkedList<>();
    }
    
    public void removeAllResourcesFilters() {
        this.filtrosRecurso = new LinkedList<>();
    }

    public void addEvent() {

        //System.out.println("void addEvent called");

        try {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getData();

            Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "entityEvent:::{0}", entityEvent);

            entityEvent.setTitle(event.getTitle());
            entityEvent.setStartDate(event.getStartDate());
            entityEvent.setEndDate(event.getEndDate());
            entityEvent.setAllDay(event.isAllDay());
            //----
//                entityEvent.setFechaCreacion(new Date());
//                entityEvent.setIdCaso(casoController.getSelected());
//                entityEvent.setIdUsuario(userSessionBean.getCurrent());

            if (event.getId() == null) {
                for (ScheduleEventReminder scheduleEventReminder : entityEvent.getScheduleEventReminderList()) {
                    if (scheduleEventReminder.getIdReminder() != null || scheduleEventReminder.getIdReminder() < 0) {
                        //new added reminders
                        scheduleEventReminder.setIdReminder(null);
                    }
                }

                getJpaController().persist(entityEvent);

                lazyScheduleEventsModel.addEvent(event);

                try {
                    scheduleQuartzEvent(entityEvent);
                } catch (Exception ex) {
                    Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "scheduleQuartzEvent", ex);
                    addWarnMessage("hubo un problema al agendar la accion automática...");
                }

                try {
                    scheduleQuartzReminders(entityEvent);
                } catch (Exception ex) {
                    Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "scheduleQuartzReminders", ex);
                    addWarnMessage("hubo un problema al agendar recordatorios...");
                }
                addInfoMessage("Evento agregado exitósamente.");
                executeInClient("PF('myschedule').update();PF('createEventDialog').hide();");

            } else {
                getJpaController().merge(entityEvent);
                lazyScheduleEventsModel.updateEvent(event);
                executeInClient("PF('myschedule').update();PF('createEventDialog').hide();");
            }
            event = new DefaultScheduleEvent();

        } catch (Exception ex) {
            addErrorMessage("No se pudo agendar el evento:" + ex.getMessage());
            Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "addEvent", ex);
        }

    }

    private void scheduleQuartzEvent(com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent) throws SchedulerException, Exception {
        if (entityEvent.getExecuteAction() && entityEvent.getIdTipoAccion() != null
                && !StringUtils.isEmpty(entityEvent.getIdTipoAccion().getImplementationClassName())) {
            //we must schedule the selected action
            String jobID = HelpDeskScheluder.scheduleActionClassExecutorJob(
                    getUserSessionBean().getTenantId(),
                    entityEvent.getIdCaso().getIdCaso(),
                    entityEvent.getIdTipoAccion().getImplementationClassName(),
                    entityEvent.getParametrosAccion(),
                    entityEvent.getStartDate());
            entityEvent.setQuartzJobId(jobID);
            getJpaController().merge(entityEvent);
        }
    }

    private void scheduleQuartzReminders(com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent) throws Exception {
        if (entityEvent.getScheduleEventReminderList() != null && !entityEvent.getScheduleEventReminderList().isEmpty()) {
            //we need to schedule event reminders
            for (ScheduleEventReminder scheduleEventReminder : entityEvent.getScheduleEventReminderList()) {

                int minituesAmount = (-1) * scheduleEventReminder.getUnitOfTimeInMinutes() * scheduleEventReminder.getQuantityTime();
                //calculate when in function of  QuantityTime * UnitOfTimeInMinutes
                //ejemplo: 1 * 1 = 10 minutos antes del evento
                //ejemplo: 1 * 60 = 1 horas antes del evento
                //ejemplo: 1 * 1440 = 1440 minutos (1 dia) antes del evento
                //ejemplo: 1 * 10080 = 10080 minutos (1 semana) antes del evento

                Calendar cal = Calendar.getInstance();
                cal.setTime(entityEvent.getStartDate());
                cal.add(Calendar.MINUTE, minituesAmount);

                String jobId = HelpDeskScheluder.scheduleEventReminderJob(
                        getUserSessionBean().getTenantId(),
                        entityEvent.getUsuariosInvitedList(),
                        entityEvent.getIdCaso().getIdCaso(),
                        scheduleEventReminder.getEventId().getEventId().toString(),
                        scheduleEventReminder.getIdReminder().toString(), cal.getTime());
                scheduleEventReminder.setQuartzJobId(jobId);

                getJpaController().merge(scheduleEventReminder);

            }
        }
    }

    private void unscheduleQuartzReminders(com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent) {
        if (entityEvent.getScheduleEventReminderList() != null && !entityEvent.getScheduleEventReminderList().isEmpty()) {
            //we need to schedule event reminders

            for (ScheduleEventReminder r : entityEvent.getScheduleEventReminderList()) {
                if (r.getIdReminder() != null && r.getIdReminder() > 0
                        && !StringUtils.isEmpty(r.getQuartzJobId())) {
                    try {
                        //its persistent
                        ScheduleEventReminderJob.unschedule(r.getQuartzJobId());
                    } catch (SchedulerException ex) {
                        Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "unscheduleQuartzReminders:{0}", ex.getMessage());
                    }
                }
            }
        }
    }

    private void unscheduleQuartzEvent(com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent) throws Exception {
        if (entityEvent.getExecuteAction() && !StringUtils.isEmpty(entityEvent.getQuartzJobId())) {
            //we need to un schedule event for resschedule
            ActionClassExecutorJob.unschedule(entityEvent.getQuartzJobId());
        }
    }

    public void addNewScheduleEventReminder() {
        if (this.event != null) {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getData();
            entityEvent.addNewScheduleEventReminder();
        }
    }

    public void removeScheduleEventReminder(ScheduleEventReminder r) {
        //System.out.println("removeScheduleEventReminder...");
        if (this.event != null) {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getData();

            List<ScheduleEventReminder> scheduleEventReminderList = entityEvent.getScheduleEventReminderList();
            if (scheduleEventReminderList != null) {
                scheduleEventReminderList.remove(r);

            }

            if (r.getIdReminder() != null && r.getIdReminder() > 0) {
                //its persistent
                if (!StringUtils.isEmpty(r.getQuartzJobId())) {
                    try {
                        ScheduleEventReminderJob.unschedule(r.getQuartzJobId());
                    } catch (SchedulerException ex) {
                        Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "removeScheduleEventReminder:{0}", ex.getMessage());
                    }
                }

                try {
                    getJpaController().merge(entityEvent);
                } catch (Exception ex) {
                    Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "removeScheduleEventReminder merge entityEvent error", ex);
                }
            }
        }
    }

    public void onEventSelect(SelectEvent selectEvent) {
        event = (DefaultScheduleEvent) selectEvent.getObject();
    }

    public void onDateSelect(SelectEvent selectEvent) {
        List<Usuario> fUsuario = new LinkedList<Usuario>();
        fUsuario.add(userSessionBean.getCurrent());
//        if (!userSessionBean.getCurrent().equals(casoController.getSelected().getOwner())) {
//            fUsuario.add(casoController.getSelected().getOwner());
//        }
        com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = new com.itcs.helpdesk.persistence.entities.ScheduleEvent();
        entityEvent.addNewScheduleEventReminder();
//        entityEvent.setIdCaso(casoController.getSelected());
        entityEvent.setIdUsuario(userSessionBean.getCurrent());
        entityEvent.setFechaCreacion(new Date());
        entityEvent.setUsuariosInvitedList(fUsuario);
        event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
        event.setData(entityEvent);
    }

    public void onEventMove(ScheduleEntryMoveEvent event) {

        //System.out.println("onEventMove");

        com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getScheduleEvent().getData();

        try {

            unscheduleQuartzEvent(entityEvent);
            unscheduleQuartzReminders(entityEvent);

        } catch (Exception ex) {
            Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "onEventMove unschedule error", ex);
        }

        try {

            scheduleQuartzEvent(entityEvent);
            scheduleQuartzReminders(entityEvent);

            getJpaController().merge(entityEvent);
        } catch (Exception ex) {
            Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "onEventMove merge entityEvent error", ex);
        }

        addInfoMessage("Event moved, Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {

        //System.out.println("onEventResize");
        com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getScheduleEvent().getData();
        try {

//            unscheduleQuartzEvent(entityEvent);
//            unscheduleQuartzReminders(entityEvent);
//
//            scheduleQuartzEvent(entityEvent);
//            scheduleQuartzReminders(entityEvent);
            getJpaController().merge(entityEvent);
        } catch (Exception ex) {
            Logger.getLogger(GlobalScheduleController.class.getName()).log(Level.SEVERE, "onEventResize merge entityEvent error", ex);
        }

        addInfoMessage("Event resized, Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());
    }

//    @Override
//    public OrderBy getDefaultOrderBy() {
//        return new OrderBy("startDate", OrderBy.OrderType.DESC);
//    }
    @Override
    public Class getDataModelImplementationClass() {
        return LazyScheduleModel.class;
    }

    /**
     * @return the lazyScheduleEventsModel
     */
    public ScheduleModel getLazyScheduleEventsModel() {
        return lazyScheduleEventsModel;
    }

    /**
     * @param lazyScheduleEventsModel the lazyScheduleEventsModel to set
     */
    public void setLazyScheduleEventsModel(ScheduleModel lazyScheduleEventsModel) {
        this.lazyScheduleEventsModel = lazyScheduleEventsModel;
    }

    /**
     * @param casoController the casoController to set
     */
    public void setCasoController(CasoController casoController) {
        this.casoController = casoController;
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    public List<Usuario> autoCompleteUsuario(String query) {
        //System.out.println(query);
        //List<EmailCliente> results = new ArrayList<EmailCliente>();
        List<Usuario> list = getJpaController().findUsuariosEntitiesLike(query, false, 10, 0);
//        //System.out.println(emailClientes);
        if (list != null && !list.isEmpty()) {
            return list;
        } else {
//            emailCliente_wizard_existeEmail = false;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningun usuario con ese email o nombre...", "No existe ningun usuario con ese email o nombre...");
            FacesContext.getCurrentInstance().addMessage(null, message);
            list = new ArrayList<Usuario>();
            //System.out.println("No existe el Cliente con email" + query);
            return list;
        }

    }

     public void resourceFilterItemSelectEvent(SelectEvent event) {
        Object item = event.getObject();
        try {

            if (this.filtrosRecurso == null) {
                filtrosRecurso = new LinkedList<>();
            }

            final Resource res = (Resource) item;
            addResToTheList(res);
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar el recurso al filtro: " + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, "resourceFilterItemSelectEvent", ex);
        }

    }
     
     private void addResToTheList(final Resource r) {
        if (!filtrosRecurso.contains(r)) {
            if (StringUtils.isEmpty(r.getRandomColor())) {
                final String color = Colors.getNextColor();
                if (color != null) {
                    r.setRandomColor(color);
                    filtrosRecurso.add(r);
                } else {
                    addInfoMessage("No puede agregar más Resources, favor quitar algunos que no necesite ver, para agregar más!");
                }
                
            }
            
            selectedResourceToAddInvited = null;//reset selection
            
        } else {
            addInfoMessage("Este Resource ya existe en la lista!");
        }
    }
     
    public void usuarioFilterItemSelectEvent(SelectEvent event) {
        Object item = event.getObject();
        try {
            if (this.filtrosUsuario == null) {
                filtrosUsuario = new LinkedList<>();
            }

            final Usuario usuario = (Usuario) item;
            addUsuarioToTheList(usuario);

        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar " + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, "usuarioFilterItemSelectEvent", ex);
        }

    }

    private void addUsuarioToTheList(final Usuario usuario) {
        if (!filtrosUsuario.contains(usuario)) {
            if (StringUtils.isEmpty(usuario.getRandomColor())) {
                final String color = Colors.getNextColor();
                if (color != null) {
                    usuario.setRandomColor(color);
                    filtrosUsuario.add(usuario);
                } else {
                    addInfoMessage("No puede agregar más usuarios, favor quitar algunos que no necesite ver, para agregar más!");
                }
                
            }
            
            selectedUserToAddInvited = null;//reset selection
            
        } else {
            addInfoMessage("Este usuario ya existe en la lista!");
        }
    }

    public void usuarioInvitedItemSelectEvent(SelectEvent event) {
        Object item = event.getObject();
        //System.out.println("item:" + item);

        try {

            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) this.event.getData();
            if (entityEvent.getUsuariosInvitedList() == null) {
                entityEvent.setUsuariosInvitedList(new LinkedList<Usuario>());
            }
            entityEvent.getUsuariosInvitedList().add((Usuario) item);
            selectedUserToAddInvited = null;//reset selection

//            addInfoMessage("Agregada OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar " + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public List<Resource> autoCompleteResource(String query) {
        //System.out.println(query);
        //List<EmailCliente> results = new ArrayList<EmailCliente>();
        List<Resource> list = getJpaController().findResourcesEntitiesLike(query, false, 10, 0);
//        //System.out.println(emailClientes);
        if (list != null && !list.isEmpty()) {
            return list;
        } else {
//            emailCliente_wizard_existeEmail = false;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningun Resource...", "No existe ningun Resource...");
            FacesContext.getCurrentInstance().addMessage(null, message);
            list = new ArrayList<Resource>();
            //System.out.println("No existe el Resource: " + query);
            return list;
        }

    }

    public void resourceInvitedItemSelectEvent(SelectEvent event) {
        Object item = event.getObject();
        //System.out.println("item:" + item);

        try {

            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) this.event.getData();
            if (entityEvent.getResourceList() == null) {
                entityEvent.setResourceList(new LinkedList<Resource>());
            }
            entityEvent.getResourceList().add((Resource) item);
            setSelectedResourceToAddInvited(null);//reset selection

            addInfoMessage("Resource added OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar Resource" + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

   

    /**
     * @return the selectedUserToAddInvited
     */
    public Usuario getSelectedUserToAddInvited() {
        return selectedUserToAddInvited;
    }

    /**
     * @param selectedUserToAddInvited the selectedUserToAddInvited to set
     */
    public void setSelectedUserToAddInvited(Usuario selectedUserToAddInvited) {
        this.selectedUserToAddInvited = selectedUserToAddInvited;
    }

    /**
     * @return the selectedResourceToAddInvited
     */
    public Resource getSelectedResourceToAddInvited() {
        return selectedResourceToAddInvited;
    }

    /**
     * @param selectedResourceToAddInvited the selectedResourceToAddInvited to
     * set
     */
    public void setSelectedResourceToAddInvited(Resource selectedResourceToAddInvited) {
        this.selectedResourceToAddInvited = selectedResourceToAddInvited;
    }

    /**
     * @return the filtrosUsuario
     */
    public List<Usuario> getFiltrosUsuario() {
        return filtrosUsuario;
    }

    /**
     * @param filtrosUsuario the filtrosUsuario to set
     */
    public void setFiltrosUsuario(List<Usuario> filtrosUsuario) {
        this.filtrosUsuario = filtrosUsuario;
    }

    /**
     * @return the filtrosRecurso
     */
    public List<Resource> getFiltrosRecurso() {
        return filtrosRecurso;
    }

    /**
     * @param filtrosRecurso the filtrosRecurso to set
     */
    public void setFiltrosRecurso(List<Resource> filtrosRecurso) {
        this.filtrosRecurso = filtrosRecurso;
    }

    /**
     * @return the insideCasoDisplay
     */
    public boolean isInsideCasoDisplay() {
        return insideCasoDisplay;
    }

    /**
     * @param insideCasoDisplay the insideCasoDisplay to set
     */
    public void setInsideCasoDisplay(boolean insideCasoDisplay) {
        this.insideCasoDisplay = insideCasoDisplay;
    }

    /**
     * @param applicationBean the applicationBean to set
     */
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }
}
