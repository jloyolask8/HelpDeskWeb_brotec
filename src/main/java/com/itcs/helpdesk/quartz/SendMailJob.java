/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.commons.email.EmailClient;
import com.itcs.helpdesk.persistence.entities.AuditLog;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.MailClientFactory;
import com.itcs.helpdesk.util.ManagerCasos;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.ee.jta.UserTransactionHelper;

/**
 *
 * @author jonathan
 */
public class SendMailJob extends AbstractGoDeskJob implements Job {

//    public static final String ID_CASO = "idCaso";
    public static final String EMAILS_TO = "to";
    public static final String EMAIL_SUBJECT = "subject";
    public static final String EMAIL_TEXT = "email_text";
//    public static final String INTENT = "intent";
//    public static final int RETRY = 3;

    /**
     * {0} = idArea,
     */
    public static final String JOB_ID = "%s_SendEmail_#%s_to_%s";

    public static String formatJobId(String idCanal, String idCaso, String to) {
        return String.format(JOB_ID, new Object[]{idCanal, idCaso, to});
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        System.out.println("SendMailJob.execute ");

        JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
        if (map != null) {
            String idCanal = (String) map.get(AbstractGoDeskJob.ID_CANAL);
//            String schema = (String) map.get(ID_TENANT);
            String idCaso = (String) map.get(AbstractGoDeskJob.ID_CASO);
            String emails_to = (String) map.get(EMAILS_TO);
            //---
            String subject = (String) map.get(EMAIL_SUBJECT);
            String email_text = (String) map.get(EMAIL_TEXT);
            emails_to = emails_to.trim().replace(" ", "");

            final String formatJobId = formatJobId(idCanal, idCaso, emails_to);
            if (!StringUtils.isEmpty(idCanal) && !StringUtils.isEmpty(emails_to)) {
                try {
                    final EmailClient instance = MailClientFactory.getInstance(idCanal);
                    if (instance != null) {
                        final String[] split_emails = emails_to.split(",");
                        //SEND THE EMAIL!
                        instance.sendHTML(split_emails, subject, email_text, null);
                        //crear una nota de envio de email en el caso!
                        EntityManagerFactory emf = createEntityManagerFactory();
                        UserTransaction utx = UserTransactionHelper.lookupUserTransaction();
                        JPAServiceFacade jpaController = new JPAServiceFacade(utx, emf);

                        try {
                            List<AuditLog> changeLog = new ArrayList<AuditLog>();

                            final long idCasoLong = Long.parseLong(idCaso);
                            Caso caso = jpaController.getReference(Caso.class, idCasoLong);

                            Nota nota = new Nota();
                            nota.setEnviadoA(Arrays.toString(split_emails));
                            nota.setCreadaPor(EnumUsuariosBase.SISTEMA.getUsuario());
                            nota.setEnviado(Boolean.TRUE);
                            nota.setFechaCreacion(Calendar.getInstance().getTime());
                            nota.setFechaEnvio(Calendar.getInstance().getTime());
                            nota.setFechaModificacion(Calendar.getInstance().getTime());
                            nota.setIdCaso(caso);
                            nota.setTexto(email_text);
                            nota.setVisible(Boolean.FALSE);
                            nota.setTipoNota(EnumTipoNota.RESPUESTA_AUT_CLIENTE.getTipoNota());
                            jpaController.persist(nota);

                            changeLog.add(ManagerCasos.createLogReg(caso, "respuestas", "Se envía respuesta automática (acuse de recibo)", ""));
                            caso.setFechaModif(Calendar.getInstance().getTime());
                            getJpaController().mergeCaso(caso, changeLog);

                        } catch (Exception ex) {
                            Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, "SendMailJob no se pudo persistir la nota en el caso", ex);
                        }

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
                    Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, "Mail Not Configured for {0}", idCanal);
                } catch (SchedulerException ex) {
                    Logger.getLogger(SendMailJob.class.getName()).log(Level.SEVERE, "SchedulerException lookup TX", ex);
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
