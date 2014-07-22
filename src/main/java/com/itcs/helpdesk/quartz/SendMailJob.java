/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.commons.email.EmailClient;
import com.itcs.helpdesk.util.MailClientFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
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
public class SendMailJob implements Job {

//    public static final String ID_CASO = "idCaso";
    public static final String EMAILS_TO = "to";
    public static final String EMAIL_SUBJECT = "subject";
    public static final String EMAIL_TEXT = "email_text";
//    public static final String INTENT = "intent";
//    public static final int RETRY = 3;

    /**
     * {0} = idArea,
     */
    public static final String JOB_ID = "%s_SendEmail_to_%s";

    public static String formatJobId(String idCanal, String to) {
        return String.format(JOB_ID, new Object[]{idCanal, to});
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
        if (map != null) {
            String idCanal = (String) map.get(AbstractGoDeskJob.ID_CANAL);
//            String schema = (String) map.get(ID_TENANT);
//            String idCaso = (String) map.get(ID_CASO);
            String emails_to = (String) map.get(EMAILS_TO);
            //---
            String subject = (String) map.get(EMAIL_SUBJECT);
            String email_text = (String) map.get(EMAIL_TEXT);
            emails_to = emails_to.trim().replace(" ", "");

            final String formatJobId = formatJobId(idCanal, emails_to);
            if (!StringUtils.isEmpty(idCanal) && !StringUtils.isEmpty(emails_to)) {
                try {
                    final EmailClient instance = MailClientFactory.getInstance(idCanal);
                    if (instance != null) {
                        //SEND THE EMAIL!
                        instance.sendHTML(emails_to.split(","), subject, email_text, null);
                        try {
                            //if sent ok, then forget about it
                            unschedule(formatJobId);
                        } catch (SchedulerException ex) {
                            Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, "no se pudo desagendar " + formatJobId, ex);
                            //ignore
                        }
                    } else {
                        Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, "Mail Not Configured for {0}", idCanal);
                        try {
                            //if MailNotConfigured, then forget about it
                            unschedule(formatJobId);
                        } catch (SchedulerException exe) {
                            Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, "no se pudo desagendar " + formatJobId, exe);
                            //ignore
                        }
                    }

                } catch (EmailException ex) {
                    Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, "EmailException trying to send email ", ex);
                } catch (MailClientFactory.MailNotConfiguredException ex) {
                    Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                throw new JobExecutionException("Illegal parameters to Job: SendMailJob");
            }

        }

    }

    public static void unschedule(String formatJobId) throws SchedulerException {
//        final String formatJobId = formatJobId(schema, idCaso, idalerta);
        final JobKey jobKey = JobKey.jobKey(formatJobId, HelpDeskScheluder.GRUPO_CORREO);
        HelpDeskScheluder.unschedule(jobKey);
    }

}
