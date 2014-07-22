/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.commons.email.EmailClient;
import com.itcs.helpdesk.persistence.entities.ScheduleEvent;
import com.itcs.helpdesk.persistence.entities.ScheduleEventReminder;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.MailClientFactory;
import com.itcs.helpdesk.util.PrettyDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

/**
 *
 * @author jonathan
 */
public class ScheduleEventReminderJob extends AbstractGoDeskJob implements Job {

//    public static final String ID_CASO = "idCaso";
    public static final String EVENT_ID = "EVENT_ID";
    public static final String REMINDER_ID = "REMINDER_ID";
    public static final String EMAILS_TO = "to";
//    public static final String EMAIL_SUBJECT = "subject";
//    public static final String EMAIL_TEXT = "email_text";
//    public static final String INTENT = "intent";
//    public static final int RETRY = 3;

    /**
     * {0} = idArea,
     */
    public static final String JOB_ID = "%s_RemindEvent_to_%s_e%sr_%s";

    public static String formatJobId(String idCanal, String to, String eventId, String reminderId) {
        return String.format(JOB_ID, new Object[]{idCanal, to, eventId, reminderId});
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "ScheduleEventReminderJob.execute()");

        JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
        if (map != null) {
            String idCanal = (String) map.get(ID_CANAL);
//            String idCaso = (String) map.get(ID_CASO);
            String emails_to = (String) map.get(EMAILS_TO);
            //---
//            String subject = (String) map.get(EMAIL_SUBJECT);
//            String email_text = (String) map.get(EMAIL_TEXT);
            String eventId = (String) map.get(EVENT_ID);
            String reminderId = (String) map.get(REMINDER_ID);
            emails_to = emails_to.trim().replace(" ", "");

            final String formatJobId = formatJobId( idCanal, emails_to, eventId, reminderId);
            if (!StringUtils.isEmpty(idCanal) && !StringUtils.isEmpty(emails_to)
                    && !StringUtils.isEmpty(eventId) && !StringUtils.isEmpty(reminderId)) {

                try {
                    JPAServiceFacade jpaController = getJpaController();

                    ScheduleEvent event = jpaController.find(ScheduleEvent.class, Integer.valueOf(eventId));
                    ScheduleEventReminder eventReminder = jpaController.find(ScheduleEventReminder.class, Integer.valueOf(reminderId));

                    if (event != null && eventReminder != null) {
                        //database represents the real data, we only send reminders in case the event or reminders have not been deleted
                        System.out.println("eventReminder" + eventReminder);
                        if (eventReminder.getReminderType().equalsIgnoreCase("EMAIL")) {
                            // send mail
                            final EmailClient instance = MailClientFactory.getInstance( idCanal);
                            if (instance != null) {
                                //SEND THE EMAIL!
                                String subject = "Recordatorio: " + event.getTitle() + " " + PrettyDate.format(event.getStartDate()) + " (" + event.getIdUsuario().getCapitalName() + ")";

                                String bodyText = "TO BE DEFINED AS A CONFIG OR TEMPLATE USING EVENT INFO";//TODO
                                instance.sendHTML(emails_to.split(","), subject, bodyText, null);
                                //if sent ok, then forget about it
                                unschedule(formatJobId);
                                eventReminder.setNotifiedOk(Boolean.TRUE);
                                try {
                                    jpaController.merge(eventReminder);
                                } catch (Exception ex) {
                                    Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "merge error", ex);
                                }
                                
                                System.out.println("DONE!");

                            } else {
                                Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "Mail Not Configured for {0}", idCanal);
                                //if MailNotConfigured, then forget about it
                                unschedule(formatJobId);

                            }
                        } else if (eventReminder.getReminderType().equalsIgnoreCase("POPUP")) {
                            //not supported type YET
                            unschedule(formatJobId);
                        } else {
                            //not supported type
                            unschedule(formatJobId);
                        }

                    } else {
                        Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "event or reminder does not exist anymore, dismiss.");
                        unschedule(formatJobId);
                    }

                } catch (Exception ex) {
                    Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "ScheduleEventReminderJob error", ex);
                    throw new JobExecutionException(ex);
                }
            } else {
                Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "ScheduleEventReminderJob execution failed: Illegal parameters");
                unschedule(formatJobId);
            }

        } else {
            Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "JobDataMap is null");
        }

    }

    public static boolean unschedule(String formatJobId) {
        try {
            //        final String formatJobId = formatJobId( idCaso, idalerta);
            final JobKey jobKey = JobKey.jobKey(formatJobId, HelpDeskScheluder.GRUPO_CORREO);
            return HelpDeskScheluder.unschedule(jobKey);
        } catch (SchedulerException ex) {
            Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "unschedule " + formatJobId, ex);
            return false;
        }
    }

   

}
