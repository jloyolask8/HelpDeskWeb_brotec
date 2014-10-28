/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Resource;
import com.itcs.helpdesk.persistence.entities.ScheduleEventClient;
import com.itcs.helpdesk.persistence.entities.ScheduleEventClientPK;
import com.itcs.helpdesk.persistence.entities.ScheduleEventReminder;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.quartz.ActionClassExecutorJob;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.quartz.ScheduleEventReminderJob;
import com.itcs.helpdesk.util.Log;
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
import org.primefaces.event.TabCloseEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.quartz.SchedulerException;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "casoScheduleController")
@SessionScoped
public class CasoScheduleController extends AbstractManagedBean<com.itcs.helpdesk.persistence.entities.ScheduleEvent> implements Serializable {

    @ManagedProperty(value = "#{casoController}")
    private CasoController casoController;
    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;
    private ScheduleModel lazyScheduleEventsModel;
    private DefaultScheduleEvent event = null;

    private List<Usuario> filtrosUsuario = new LinkedList<Usuario>();
    private List<Resource> filtrosRecurso = new LinkedList<Resource>();
    private boolean insideCasoDisplay = true;

    //temp vars
    private Usuario selectedUserToAddInvited;
    private Resource selectedResourceToAddInvited;
    private Cliente selectedClientToAddInvited;

    //selected client
    private ScheduleEventClient selectedScheduleEventClient;

    public CasoScheduleController() {

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
                    getFilterHelper().getVista().setFiltrosVistaList(new ArrayList<FiltroVista>(2));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    //add date filters
                    FiltroVista f1 = new FiltroVista();
                    f1.setIdFiltro(1);//otherwise i dont know what to remove dude.
                    f1.setIdCampo("startDate");
                    f1.setIdComparador(EnumTipoComparacion.BW.getTipoComparacion());
                    f1.setValor(sdf.format(start));
                    f1.setValor2(sdf.format(end));

                    f1.setIdVista(getFilterHelper().getVista());

                    getFilterHelper().getVista().getFiltrosVistaList().add(f1);

                    if (insideCasoDisplay) {
                        Caso currentCaso = casoController.getSelected();
                        //add current caso filter
                        FiltroVista f3 = new FiltroVista();
                        f3.setIdFiltro(3);//otherwise i dont know what to remove dude.
                        f3.setIdCampo("idCaso");
                        f3.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                        f3.setValor(currentCaso.getIdCaso().toString());
                        f3.setIdVista(getFilterHelper().getVista());
                        getFilterHelper().getVista().getFiltrosVistaList().add(f3);
                    }

                    if (filtrosUsuario != null && !filtrosUsuario.isEmpty()) {
                        //add current caso filter
                        FiltroVista f4_0 = new FiltroVista();
                        f4_0.setIdFiltro(4);//otherwise i dont know what to remove dude.
                        f4_0.setIdCampo("idUsuario");
                        f4_0.setIdComparador(EnumTipoComparacion.SC.getTipoComparacion());
                        String commaSeparatedIdOfUsuariosFilter = "";
                        boolean first = true;
                        for (Usuario usuario : filtrosUsuario) {
                            if (first) {
                                commaSeparatedIdOfUsuariosFilter += usuario.getIdUsuario();
                                first = false;
                            } else {
                                commaSeparatedIdOfUsuariosFilter += ("," + usuario.getIdUsuario());
                            }
                        }

                        f4_0.setValor(commaSeparatedIdOfUsuariosFilter);
                        f4_0.setIdVista(getFilterHelper().getVista());
                        getFilterHelper().getVista().getFiltrosVistaList().add(f4_0);
                    }

                    if (filtrosUsuario != null && !filtrosUsuario.isEmpty()) {
                        //add current caso filter
                        FiltroVista f4 = new FiltroVista();
                        f4.setIdFiltro(4);//otherwise i dont know what to remove dude.
                        f4.setIdCampo("usuariosInvitedList");
                        f4.setIdComparador(EnumTipoComparacion.IM.getTipoComparacion());
                        String commaSeparatedIdOfUsuariosFilter = "";
                        boolean first = true;
                        for (Usuario usuario : filtrosUsuario) {
                            if (first) {
                                commaSeparatedIdOfUsuariosFilter += usuario.getIdUsuario();
                                first = false;
                            } else {
                                commaSeparatedIdOfUsuariosFilter += ("," + usuario.getIdUsuario());
                            }
                        }

                        f4.setValor(commaSeparatedIdOfUsuariosFilter);
                        f4.setIdVista(getFilterHelper().getVista());
                        getFilterHelper().getVista().getFiltrosVistaList().add(f4);
                    }

                    if (filtrosRecurso != null && !filtrosRecurso.isEmpty()) {
                        //add current caso filter
                        FiltroVista f5 = new FiltroVista();
                        f5.setIdFiltro(4);//otherwise i dont know what to remove dude.
                        f5.setIdCampo("resourceList");
                        f5.setIdComparador(EnumTipoComparacion.IM.getTipoComparacion());
                        String commaSeparatedIdOfResourcesFilter = "";
                        boolean first = true;
                        for (Resource r : filtrosRecurso) {
                            if (first) {
                                commaSeparatedIdOfResourcesFilter += r.getIdResource();
                                first = false;
                            } else {
                                commaSeparatedIdOfResourcesFilter += ("," + r.getIdResource());
                            }
                        }

                        f5.setValor(commaSeparatedIdOfResourcesFilter);
                        f5.setIdVista(getFilterHelper().getVista());
                        getFilterHelper().getVista().getFiltrosVistaList().add(f5);
                    }

//                    System.out.println("VISTA=" + getFilterHelper().getVista());

                    final List<com.itcs.helpdesk.persistence.entities.ScheduleEvent> findEntities
                            = (List<com.itcs.helpdesk.persistence.entities.ScheduleEvent>) getJpaController().findAllEntities(getFilterHelper().getVista(), new OrderBy("startDate", OrderBy.OrderType.DESC), null);
//                    System.out.println("events:" + findEntities);
                    for (com.itcs.helpdesk.persistence.entities.ScheduleEvent scheduleEvent : findEntities) {
                        final DefaultScheduleEvent defaultScheduleEvent = new DefaultScheduleEvent(scheduleEvent.getTitle(), scheduleEvent.getStartDate(), scheduleEvent.getEndDate());
                        defaultScheduleEvent.setAllDay(scheduleEvent.getAllDay());
                        defaultScheduleEvent.setData(scheduleEvent);

                        addEvent(defaultScheduleEvent);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "loadEvents", ex);
                }

            }
        };

//        System.out.println("new CasoScheduleController()");

    }

    public String selectCasoByEvento(DefaultScheduleEvent scheduleEvent) {

        this.event = scheduleEvent;
        com.itcs.helpdesk.persistence.entities.ScheduleEvent scheduleEvent1 = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) scheduleEvent.getData();
        casoController.setActiveIndexCasoSections(CasoController.TAB_EVENTO_INDEX);//tabEditarEvento
        return casoController.filterByIdCaso(scheduleEvent1.getIdCaso().getIdCaso());

    }

    public void updateAsistencia() {

        executeInClient("PF('selectedScheduleEventClientDialog').hide()");

    }

    public DefaultScheduleEvent getEvent() {
        return event;
    }

    public void setEvent(DefaultScheduleEvent event) {
        this.event = event;
    }

    public void deleteSelectedEvent() {
        try {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getData();

            if (event.getId() == null) {
                addWarnMessage("El evento no se puede eliminar por que no existe.");
            } else {
                Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "deleteSelectedEvent entityEvent::{0}", entityEvent);

                try {
                    unscheduleQuartzEvent(entityEvent);
                    unscheduleQuartzReminders(entityEvent);
                } catch (Exception ex) {
                    Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "onEventMove unschedule error", ex);
                }

                lazyScheduleEventsModel.deleteEvent(event);
                getJpaController().remove(com.itcs.helpdesk.persistence.entities.ScheduleEvent.class, entityEvent);
                addInfoMessage("Evento eliminado exitósamente.");
                executeInClient("PF('myschedule').update();PF('viewEventDialog').hide();");
            }

        } catch (Exception ex) {
            addErrorMessage("No se pudo eliminar el evento:" + ex.getMessage());
            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "deleteSelectedEvent", ex);
        }

    }

    public void quickSaveEvent() {

//        System.out.println("void quickSaveEvent called");

        try {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getData();

            entityEvent.setTitle(event.getTitle());
            entityEvent.setStartDate(event.getStartDate());
            entityEvent.setEndDate(event.getEndDate());
            entityEvent.setAllDay(event.isAllDay());

            if (event.getId() == null) {
//                showMessageInDialog(FacesMessage.SEVERITY_WARN, "Esta opción no esta disponible.", "Esta opción no esta disponible.");
                Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "persisting entityEvent::{0}", entityEvent);
                persistAndScheduleEvent(entityEvent);

                lazyScheduleEventsModel.addEvent(event);

                addInfoMessage("Evento agendado exitósamente.");
                executeInClient("PF('myschedule').update();");
            } else {
                Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "merging entityEvent::{0}", entityEvent);
                moveEventQuartzTriggers(entityEvent);
                lazyScheduleEventsModel.updateEvent(event);
                addInfoMessage("Evento actualizado exitósamente.");
                executeInClient("PF('myschedule').update();PF('viewEventDialog').hide();");
            }

        } catch (Exception ex) {
            addErrorMessage("No se pudo editar el evento:" + ex.getMessage());
            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "quickSaveEvent", ex);
        }

    }

    public void saveEvent() {

//        System.out.println("void saveEvent called");

        try {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getData();

            entityEvent.setTitle(event.getTitle());
            entityEvent.setStartDate(event.getStartDate());
            entityEvent.setEndDate(event.getEndDate());
            entityEvent.setAllDay(event.isAllDay());

            if (event.getId() == null) {
                //                showMessageInDialog(FacesMessage.SEVERITY_WARN, "Esta opción no esta disponible.", "Esta opción no esta disponible.");
                Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "persisting entityEvent::{0}", entityEvent);
                persistAndScheduleEvent(entityEvent);

                lazyScheduleEventsModel.addEvent(event);

                addInfoMessage("Evento agendado exitósamente.");
                executeInClient("PF('myschedule').update();");
            } else {
                Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "merging entityEvent::{0}", entityEvent);

                moveEventQuartzTriggers(entityEvent);

                lazyScheduleEventsModel.updateEvent(event);
                casoController.setActiveIndexCasoSections(CasoController.TAB_AGENDA_INDEX);//tabAgendarEvento
                this.event = null;
                addInfoMessage("Evento actualizado exitósamente.");
                executeInClient("PF('myschedule').update();");
            }

        } catch (Exception ex) {
            addErrorMessage("No se pudo editar el evento:" + ex.getMessage());
            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "quickSaveEvent", ex);
        }

    }

    public void onTabClose(TabCloseEvent closeEvent) {
        //update=":inputPanel"
//        System.out.println("TabCloseEvent:" + closeEvent.getTab());
//        FacesMessage msg = new FacesMessage("Tab Closed", "Closed tab: " + closeEvent.getTab().getTitle());
//        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.event = null;
    }

    public void goEditEvent() {
        //casoController.onChangeActiveIndexdescOrComment(event);
        addEvent();
        casoController.setActiveIndexCasoSections(CasoController.TAB_EVENTO_INDEX);//tabEditarEvento
        executeInClient("PF('viewEventDialog').hide();PF('createEventDialog').hide();");
        updateComponentInClient("inputPanel");
    }

    public void addEvent() {

        System.out.println("void addEvent called");

        try {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getData();

            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "entityEvent:::{0}", entityEvent);

            entityEvent.setTitle(event.getTitle());
            entityEvent.setStartDate(event.getStartDate());
            entityEvent.setEndDate(event.getEndDate());
            entityEvent.setAllDay(event.isAllDay());
            entityEvent.setFechaCreacion(new Date());

            //----
//                entityEvent.setFechaCreacion(new Date());
//                entityEvent.setIdCaso(casoController.getSelected());
//                entityEvent.setIdUsuario(userSessionBean.getCurrent());
            if (event.getId() == null) {
                Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "persisting entityEvent::{0}", entityEvent);
                persistAndScheduleEvent(entityEvent);

                lazyScheduleEventsModel.addEvent(event);

                addInfoMessage("Evento agregado exitósamente.");
                executeInClient("PF('myschedule').update();PF('createEventDialog').hide();");

            } else {
                Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "merging entityEvent::{0}", entityEvent);
                getJpaController().merge(entityEvent);
                lazyScheduleEventsModel.updateEvent(event);
                executeInClient("PF('myschedule').update();PF('createEventDialog').hide();");
            }
//            event = new DefaultScheduleEvent();

        } catch (Exception ex) {
            addErrorMessage("No se pudo agendar el evento:" + ex.getMessage());
            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "addEvent", ex);
        }

    }

    public void persistAndScheduleEvent(com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent) throws Exception {
        for (ScheduleEventReminder scheduleEventReminder : entityEvent.getScheduleEventReminderList()) {
            if (scheduleEventReminder.getIdReminder() != null || scheduleEventReminder.getIdReminder() < 0) {
                //new added reminders
                scheduleEventReminder.setIdReminder(null);
            }
        }

        getJpaController().persist(entityEvent);

        scheduleQuartzEvent(entityEvent);

        scheduleQuartzReminders(entityEvent);
    }

    private void scheduleQuartzEvent(com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent) throws SchedulerException, Exception {
        if (entityEvent != null && entityEvent.getExecuteAction() != null && entityEvent.getExecuteAction().equals(Boolean.TRUE) && entityEvent.getIdTipoAccion() != null
                && !StringUtils.isEmpty(entityEvent.getIdTipoAccion().getImplementationClassName())) {
            //we must schedule the selected action
            String jobID = HelpDeskScheluder.scheduleActionClassExecutorJob(
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

                final String eventIdString = entityEvent.getEventId().toString();
                final String scheduleEventReminderIdString = scheduleEventReminder.getIdReminder().toString();

                String jobId = HelpDeskScheluder.scheduleEventReminderJob(entityEvent.getUsuariosInvitedList(), entityEvent.getIdCaso().getIdCaso(), eventIdString, scheduleEventReminderIdString, cal.getTime());

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
        System.out.println("removeScheduleEventReminder...");
        if (this.event != null) {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) this.event.getData();

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
                    Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "removeScheduleEventReminder merge entityEvent error", ex);
                }
            }
        }
    }

    public void onEventSelect(SelectEvent selectEvent) {
        this.event = (DefaultScheduleEvent) selectEvent.getObject();
    }

    public void onDateSelect(SelectEvent selectEvent) {
        List<Usuario> fUsuario = new LinkedList<Usuario>();
        fUsuario.add(userSessionBean.getCurrent());
        if (!userSessionBean.getCurrent().equals(casoController.getSelected().getOwner())) {
            fUsuario.add(casoController.getSelected().getOwner());
        }
        com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = new com.itcs.helpdesk.persistence.entities.ScheduleEvent();
        entityEvent.addNewScheduleEventReminder();
        entityEvent.setIdCaso(casoController.getSelected());
        entityEvent.setIdUsuario(userSessionBean.getCurrent());

        entityEvent.setUsuariosInvitedList(fUsuario);
        this.event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
        this.event.setData(entityEvent);
    }

    public void onEventMove(ScheduleEntryMoveEvent entryMoveEvent) {

        System.out.println("onEventMove");

        com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) entryMoveEvent.getScheduleEvent().getData();

        moveEventQuartzTriggers(entityEvent);

        addInfoMessage("Event moved, Day delta:" + entryMoveEvent.getDayDelta() + ", Minute delta:" + entryMoveEvent.getMinuteDelta());
    }

    private void moveEventQuartzTriggers(com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent) {
        try {

            unscheduleQuartzEvent(entityEvent);
            unscheduleQuartzReminders(entityEvent);

        } catch (Exception ex) {
            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "onEventMove unschedule error", ex);
        }

        try {

            scheduleQuartzEvent(entityEvent);
            scheduleQuartzReminders(entityEvent);

            getJpaController().merge(entityEvent);
        } catch (Exception ex) {
            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "onEventMove merge entityEvent error", ex);
        }
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {

        System.out.println("onEventResize");
        com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) event.getScheduleEvent().getData();
        try {

//            unscheduleQuartzEvent(entityEvent);
//            unscheduleQuartzReminders(entityEvent);
//
//            scheduleQuartzEvent(entityEvent);
//            scheduleQuartzReminders(entityEvent);
            getJpaController().merge(entityEvent);
        } catch (Exception ex) {
            Logger.getLogger(CasoScheduleController.class.getName()).log(Level.SEVERE, "onEventResize merge entityEvent error", ex);
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
        System.out.println(query);
        //List<EmailCliente> results = new ArrayList<EmailCliente>();
        List<Usuario> list = getJpaController().findUsuariosEntitiesLike(query, false, 10, 0);
//        System.out.println(emailClientes);
        if (list != null && !list.isEmpty()) {
            return list;
        } else {
//            emailCliente_wizard_existeEmail = false;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningun usuario con ese email o nombre...", "No existe ningun usuario con ese email o nombre...");
            FacesContext.getCurrentInstance().addMessage(null, message);
            list = new ArrayList<Usuario>();
            System.out.println("No existe el Cliente con email" + query);
            return list;
        }

    }

    public List<Cliente> autoCompleteCliente(String query) {
        System.out.println(query);
        //List<EmailCliente> results = new ArrayList<EmailCliente>();
        List<Cliente> list = getJpaController().findClientesEntitiesLike(query, false, 10, 0);
//        System.out.println(emailClientes);
        if (list != null && !list.isEmpty()) {
            return list;
        } else {
//            emailCliente_wizard_existeEmail = false;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningun cliente con ese nombre...", "No existe ningun cliente con ese nombre...");
            FacesContext.getCurrentInstance().addMessage(null, message);
            list = new ArrayList<Cliente>();
            System.out.println("No existe el Cliente " + query);
            return list;
        }

    }

    public void usuarioFilterItemSelectEvent(SelectEvent event) {
        Object item = event.getObject();
        try {
            if (this.filtrosUsuario == null) {
                filtrosUsuario = new LinkedList<Usuario>();
            }
            filtrosUsuario.add((Usuario) item);
            selectedUserToAddInvited = null;//reset selection
            addInfoMessage("Filtro usuario Agregado OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar " + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, "usuarioFilterItemSelectEvent", ex);
        }

    }

    public void clienteInvitedItemSelectEvent(SelectEvent selectEvent) {
        Object item = selectEvent.getObject();

        try {

            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) this.event.getData();
            if (entityEvent.getScheduleEventClientList() == null) {
                entityEvent.setScheduleEventClientList(new LinkedList<ScheduleEventClient>());
            }
            final Cliente cliente = (Cliente) item;
            final ScheduleEventClient scheduleEventClient = new ScheduleEventClient(new ScheduleEventClientPK(entityEvent.getEventId(), cliente.getIdCliente()));
            scheduleEventClient.setCliente(cliente);
            scheduleEventClient.setScheduleEvent(entityEvent);
            scheduleEventClient.setTimestampCreacion(new Date());
            //TODO add created_by column

            if (entityEvent.getScheduleEventClientList() == null) {
                entityEvent.setScheduleEventClientList(new LinkedList<ScheduleEventClient>());
            }

            if (!entityEvent.getScheduleEventClientList().contains(scheduleEventClient)) {
                entityEvent.getScheduleEventClientList().add(scheduleEventClient);
            }else{
                addInfoMessage("El cliente ya existe en la lista.");
            }

            selectedClientToAddInvited = null;//reset selection

        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar " + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, "clienteInvitedItemSelectEvent", ex);
        }

    }

    public void usuarioInvitedItemSelectEvent(SelectEvent selectEvent) {
        Object item = selectEvent.getObject();
        System.out.println("item:" + item);

        try {
            com.itcs.helpdesk.persistence.entities.ScheduleEvent entityEvent = (com.itcs.helpdesk.persistence.entities.ScheduleEvent) this.event.getData();
            if (entityEvent.getUsuariosInvitedList() == null) {
                entityEvent.setUsuariosInvitedList(new LinkedList<Usuario>());
            }
            final Usuario user = (Usuario) item;

            if (!entityEvent.getUsuariosInvitedList().contains(user)) {
                entityEvent.getUsuariosInvitedList().add(user);
            } else {
                addInfoMessage("El usuario ya existe.");
            }
            selectedUserToAddInvited = null;//reset selection
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar " + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public List<Resource> autoCompleteResource(String query) {
        System.out.println(query);
        //List<EmailCliente> results = new ArrayList<EmailCliente>();
        List<Resource> list = getJpaController().findResourcesEntitiesLike(query, false, 10, 0);
//        System.out.println(emailClientes);
        if (list != null && !list.isEmpty()) {
            return list;
        } else {
//            emailCliente_wizard_existeEmail = false;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningun Resource...", "No existe ningun Resource...");
            FacesContext.getCurrentInstance().addMessage(null, message);
            list = new ArrayList<Resource>();
            System.out.println("No existe el Resource: " + query);
            return list;
        }

    }

    public void resourceInvitedItemSelectEvent(SelectEvent selectEvent) {
        Object item = selectEvent.getObject();
        System.out.println("item:" + item);

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

    public void resourceFilterItemSelectEvent(SelectEvent selectEvent) {
        Object item = selectEvent.getObject();
        try {

            if (this.filtrosRecurso == null) {
                this.filtrosRecurso = (new LinkedList<Resource>());
            }
            this.filtrosRecurso.add((Resource) item);
            setSelectedResourceToAddInvited(null);//reset selection

            addInfoMessage("Resource filter added OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar Resource" + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, "resourceFilterItemSelectEvent", ex);
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
     * @return the selectedClientToAddInvited
     */
    public Cliente getSelectedClientToAddInvited() {
        return selectedClientToAddInvited;
    }

    /**
     * @param selectedClientToAddInvited the selectedClientToAddInvited to set
     */
    public void setSelectedClientToAddInvited(Cliente selectedClientToAddInvited) {
        this.selectedClientToAddInvited = selectedClientToAddInvited;
    }

    /**
     * @return the selectedScheduleEventClient
     */
    public ScheduleEventClient getSelectedScheduleEventClient() {
        return selectedScheduleEventClient;
    }

    /**
     * @param selectedScheduleEventClient the selectedScheduleEventClient to set
     */
    public void setSelectedScheduleEventClient(ScheduleEventClient selectedScheduleEventClient) {
        this.selectedScheduleEventClient = selectedScheduleEventClient;
    }
}
