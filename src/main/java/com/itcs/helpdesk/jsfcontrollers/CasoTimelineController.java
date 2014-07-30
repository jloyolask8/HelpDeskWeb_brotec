/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.AuditLog;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.ScheduleEvent;
import com.itcs.helpdesk.persistence.entities.ScheduleEventReminder;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.extensions.component.timeline.TimelineUpdater;
import org.primefaces.extensions.event.timeline.TimelineModificationEvent;
import org.primefaces.extensions.model.timeline.TimelineEvent;
import org.primefaces.extensions.model.timeline.TimelineModel;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "casoTimelineController")
@SessionScoped
public class CasoTimelineController extends AbstractManagedBean<ScheduleEvent> implements Serializable {

    @ManagedProperty(value = "#{casoController}")
    private CasoController casoController;
//    @ManagedProperty(value = "#{UserSessionBean}")
//    private UserSessionBean userSessionBean;
    private TimelineModel model;
    private TimelineModel modelScheduleEvents;

    private TimelineEvent event; // current event to be changed, edited, deleted or added  

    private long zoomMax;
    private Date start;
    private Date end;

    private TimeZone timeZone = TimeZone.getDefault();

    public CasoTimelineController() {

        super(ScheduleEvent.class);

        // initial zooming is ca. one month to avoid hiding of event details (due to wide time range of events)  
        zoomMax = 1000L * 60 * 60 * 24 * 30 * 12;

        // set initial start / end dates for the axis of the timeline (just for testing)  
//        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//        cal.set(2013, Calendar.JUNE, 9, 0, 0, 0);
//        start = cal.getTime();
//        cal.set(2013, Calendar.AUGUST, 10, 0, 0, 0);
//        end = cal.getTime();
        modelScheduleEvents = new TimelineModel() {

            @Override
            public List<TimelineEvent> getEvents() {
                Logger.getLogger(CasoTimelineController.class.getName()).log(Level.SEVERE, "getEvents");
                List<TimelineEvent> events = new LinkedList<TimelineEvent>();

                try {

                    Vista vista0 = new Vista(ScheduleEvent.class);

                    //add date filters
                    Caso currentCaso = casoController.getSelected();
                    //add current caso filter
                    FiltroVista filtro1 = new FiltroVista();
                    filtro1.setIdFiltro(3);//otherwise i dont know what to remove dude.
                    filtro1.setIdCampo("idCaso");
                    filtro1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                    filtro1.setValor(currentCaso.getIdCaso().toString());
                    filtro1.setIdVista(vista0);
                    vista0.getFiltrosVistaList().add(filtro1);

                    List<ScheduleEvent> findEntities = (List<ScheduleEvent>) getJpaController().findAllEntities(ScheduleEvent.class, vista0, ("startDate"), null);
                    for (ScheduleEvent log : findEntities) {
                        events.add(new TimelineEvent(log, log.getStartDate(), Boolean.TRUE));
                    }

                } catch (Exception ex) {
                    Logger.getLogger(CasoTimelineController.class.getName()).log(Level.SEVERE, "getEvents", ex);
                }

                return events;
            }
        };

        model = new TimelineModel() {

            @Override
            public List<TimelineEvent> getEvents() {
                List<TimelineEvent> events = new LinkedList<TimelineEvent>();

                try {

                    Vista vista = new Vista(AuditLog.class);

                    //add date filters
                    Caso currentCaso = casoController.getSelected();
                    //add current caso filter
                    FiltroVista f3 = new FiltroVista();
                    f3.setIdFiltro(3);//otherwise i dont know what to remove dude.
                    f3.setIdCampo("idCaso");
                    f3.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                    f3.setValor(currentCaso.getIdCaso().toString());
                    f3.setIdVista(vista);
                    vista.getFiltrosVistaList().add(f3);

                    List<AuditLog> findEntities = (List<AuditLog>) getJpaController().findAllEntities(AuditLog.class, vista, ("fecha"), null);
                    for (AuditLog log : findEntities) {
                        events.add(new TimelineEvent(log, log.getFecha(), Boolean.FALSE));
                    }

                } catch (Exception ex) {
                    Logger.getLogger(CasoTimelineController.class.getName()).log(Level.SEVERE, "getEvents", ex);
                }

                return events;
            }
        };

        System.out.println("new CasoTimelineController()");

    }

    public void onChange(TimelineModificationEvent e) {
        // get clone of the TimelineEvent to be changed with new start / end dates  
        event = e.getTimelineEvent();

        // update booking in DB...  
        // if everything was ok, no UI update is required. Only the model should be updated  
        getModel().update(event);

        FacesMessage msg
                = new FacesMessage(FacesMessage.SEVERITY_INFO, "The booking dates have been updated", null);
        FacesContext.getCurrentInstance().addMessage(null, msg);

        // otherwise (if DB operation failed) a rollback can be done with the same response as follows:  
        // TimelineEvent oldEvent = model.getEvent(model.getIndex(event));  
        // TimelineUpdater timelineUpdater = TimelineUpdater.getCurrentInstance(":mainForm:timeline");  
        // model.update(oldEvent, timelineUpdater);  
    }

    public void onEdit(TimelineModificationEvent e) {
        // get clone of the TimelineEvent to be edited  
        event = e.getTimelineEvent();
    }

//    public void onAdd(TimelineAddEvent e) {  
//        // get TimelineEvent to be added  
//        event = new TimelineEvent(new Booking(), e.getStartDate(), e.getEndDate(), true, e.getGroup());  
//  
//        // add the new event to the model in case if user will close or cancel the "Add dialog"  
//        // without to update details of the new event. Note: the event is already added in UI.  
//        model.add(event);  
//    }  
    public void onDelete(TimelineModificationEvent e) {
        // get clone of the TimelineEvent to be deleted  
        event = e.getTimelineEvent();
    }

    public void delete() {
        // delete booking in DB...  

        // if everything was ok, delete the TimelineEvent in the model and update UI with the same response.  
        // otherwise no server-side delete is necessary (see timelineWdgt.cancelDelete() in the p:ajax onstart).  
        // we assume, delete in DB was successful  
        TimelineUpdater timelineUpdater = TimelineUpdater.getCurrentInstance(":mainForm:timeline");
        getModel().delete(event, timelineUpdater);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "The booking has been deleted", null);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void saveDetails() {
        // save the updated booking in DB...  

        // if everything was ok, update the TimelineEvent in the model and update UI with the same response.  
        // otherwise no server-side update is necessary because UI is already up-to-date.  
        // we assume, save in DB was successful  
        TimelineUpdater timelineUpdater = TimelineUpdater.getCurrentInstance(":mainForm:timeline");
        getModel().update(event, timelineUpdater);

        FacesMessage msg
                = new FacesMessage(FacesMessage.SEVERITY_INFO, "The booking details  have been saved", null);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

//    public void addEvent() {
//
//        System.out.println("void addEvent called");
//
//        try {
//            ScheduleEvent entityEvent = (ScheduleEvent) event.getData();
//
//            Logger.getLogger(CasoTimelineController.class.getName()).log(Level.SEVERE, "entityEvent:::{0}", entityEvent);
//
//            entityEvent.setTitle(event.getTitle());
//            entityEvent.setStartDate(event.getStartDate());
//            entityEvent.setEndDate(event.getEndDate());
//            entityEvent.setAllDay(event.isAllDay());
//
//            entityEvent.setFechaCreacion(new Date());
//            //----
////                entityEvent.setFechaCreacion(new Date());
////                entityEvent.setIdCaso(casoController.getSelected());
////                entityEvent.setIdUsuario(userSessionBean.getCurrent());
//
//            if (event.getId() == null) {
//                persistAndScheduleEvent(entityEvent);
//
//                lazyScheduleEventsModel.addEvent(event);
//
//                addInfoMessage("Evento agregado exitósamente.");
//                executeInClient("PF('myschedule').update();PF('createEventDialog').hide();");
//
//            } else {
//                getJpaController().merge(entityEvent);
//                lazyScheduleEventsModel.updateEvent(event);
//                executeInClient("PF('myschedule').update();PF('createEventDialog').hide();");
//            }
//            event = new DefaultScheduleEvent();
//
//        } catch (Exception ex) {
//            addErrorMessage("No se pudo agendar el evento:" + ex.getMessage());
//            Logger.getLogger(CasoTimelineController.class.getName()).log(Level.SEVERE, "addEvent", ex);
//        }
//
//    }
    public void persistAndScheduleEvent(ScheduleEvent entityEvent) throws Exception {
        for (ScheduleEventReminder scheduleEventReminder : entityEvent.getScheduleEventReminderList()) {
            if (scheduleEventReminder.getIdReminder() != null || scheduleEventReminder.getIdReminder() < 0) {
                //new added reminders
                scheduleEventReminder.setIdReminder(null);
            }
        }

        getJpaController().persist(entityEvent);

//        scheduleQuartzEvent(entityEvent);
//        scheduleQuartzReminders(entityEvent);
    }

//    public void onEventSelect(SelectEvent selectEvent) {
//        event = (DefaultScheduleEvent) selectEvent.getObject();
//    }
//
//    public void onDateSelect(SelectEvent selectEvent) {
//        List<Usuario> fUsuario = new LinkedList<Usuario>();
//        fUsuario.add(userSessionBean.getCurrent());
//        if (!userSessionBean.getCurrent().equals(casoController.getSelected().getOwner())) {
//            fUsuario.add(casoController.getSelected().getOwner());
//        }
//        ScheduleEvent entityEvent = new ScheduleEvent();
//        entityEvent.addNewScheduleEventReminder();
//        entityEvent.setIdCaso(casoController.getSelected());
//        entityEvent.setIdUsuario(userSessionBean.getCurrent());
//
//        entityEvent.setUsuariosInvitedList(fUsuario);
//        event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
//        event.setData(entityEvent);
//    }
    public void onEventMove(ScheduleEntryMoveEvent event) {

        System.out.println("onEventMove");

        ScheduleEvent entityEvent = (ScheduleEvent) event.getScheduleEvent().getData();

        addInfoMessage("Event moved, Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {

        System.out.println("onEventResize");
        ScheduleEvent entityEvent = (ScheduleEvent) event.getScheduleEvent().getData();
        try {
            getJpaController().merge(entityEvent);
        } catch (Exception ex) {
            Logger.getLogger(CasoTimelineController.class.getName()).log(Level.SEVERE, "onEventResize merge entityEvent error", ex);
        }

        addInfoMessage("Event resized, Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());
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
//    public void setUserSessionBean(UserSessionBean userSessionBean) {
//        this.userSessionBean = userSessionBean;
//    }
    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the model
     */
    public TimelineModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(TimelineModel model) {
        this.model = model;
    }

    /**
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        System.out.println("timeZone:" + timeZone.toString());
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the zoomMax
     */
    public long getZoomMax() {
        return zoomMax;
    }

    /**
     * @param zoomMax the zoomMax to set
     */
    public void setZoomMax(long zoomMax) {
        this.zoomMax = zoomMax;
    }

    /**
     * @return the start
     */
    public Date getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * @return the modelScheduleEvents
     */
    public TimelineModel getModelScheduleEvents() {
        return modelScheduleEvents;
    }

    /**
     * @param modelScheduleEvents the modelScheduleEvents to set
     */
    public void setModelScheduleEvents(TimelineModel modelScheduleEvents) {
        this.modelScheduleEvents = modelScheduleEvents;
    }

}
