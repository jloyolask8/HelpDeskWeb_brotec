/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.entities.Attachment;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.MailClientFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailAttachment;
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
public class CaseResponseByMailJob extends AbstractGoDeskJob implements Job {

    public static final String ID_NOTA = "idNota";
    public static final String EMAILS_TO = "to";
    public static final String EMAILS_CC = "cc";
    public static final String EMAILS_BCC = "bcc";
    public static final String EMAIL_SUBJECT = "subject";
    public static final String EMAIL_TEXT = "email_text";
    public static final String EMAIL_ATTACHMENTS = "email_atts";
//    public static final String INTENT = "intent";
//    public static final int RETRY = 3;

    /**
     * {0} = idArea,
     */
    public static final String JOB_ID = "%s_SendCaseResponseByEmail_#%s_%s";

    public static String formatJobId(String idCanal, String idCaso, String to) {
        return String.format(JOB_ID, new Object[]{idCanal, idCaso, to});
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        System.out.println("CaseResponseByMailJob.execute ");

        JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
        if (map != null) {
            String idCanal = (String) map.get(DownloadEmailJob.ID_CANAL);
            String idCaso = (String) map.get(ID_CASO);
            String idNota = (String) map.get(ID_NOTA);
            String emails_to = (String) map.get(EMAILS_TO);
            String emails_cc = (String) map.get(EMAILS_CC);
            String emails_bcc = (String) map.get(EMAILS_BCC);
            //---
            String subject = (String) map.get(EMAIL_SUBJECT);
            String email_text = (String) map.get(EMAIL_TEXT);
            emails_to = emails_to.trim().replace(" ", "");
            String attachIds = (String) map.get(EMAIL_ATTACHMENTS);

            List<Long> idAtts = new LinkedList<>();
            String ids[] = attachIds.split(";");
            for (String string : ids) {
                try {
                    Long idAtt = Long.parseLong(string);
                    idAtts.add(idAtt);
                } catch (NumberFormatException ex) {
                    /*probably a weird value, silently ignore it*/
                }
            }

            final String formatJobId = formatJobId(idCanal, idCaso, emails_to);
            if (!StringUtils.isEmpty(idCanal) && !StringUtils.isEmpty(idCaso) && !StringUtils.isEmpty(emails_to)) {

                EntityManagerFactory emf = null;
                UserTransaction utx = null;
                JPAServiceFacade jpaController;

                try {
                    emf = createEntityManagerFactory();
                    utx = UserTransactionHelper.lookupUserTransaction();
                    jpaController = new JPAServiceFacade(utx, emf);
                    //SEND THE EMAIL!
                    List<EmailAttachment> attachments = null;
                    if (!idAtts.isEmpty()) {
                        attachments = new LinkedList<>();
                        for (Long idAtt : idAtts) {

                            Attachment att = jpaController.getReference(Attachment.class, idAtt);
//                        Attachment att = getJpaController().getAttachmentFindByIdAttachment(idatt);}
                            Archivo archivo = jpaController.getArchivoFindByIdAttachment(idAtt);
//                        Archivo archivo = getJpaController().getArchivoFindByIdAttachment(att.getIdAttachment());
                            EmailAttachment emailAttachment = new EmailAttachment();
                            emailAttachment.setData(archivo.getArchivo());
                            emailAttachment.setMimeType(att.getMimeType());
                            emailAttachment.setName(att.getNombreArchivo());
                            emailAttachment.setPath(att.getNombreArchivo());
                            emailAttachment.setSize(archivo.getArchivo().length);
                            attachments.add(emailAttachment);
                        }
                    }
                    final String[] split = emails_to.split(",");
                    String[] splitcc = null;
                    String[] splitbcc = null;
                    if (!StringUtils.isEmpty(emails_cc)) {
                        splitcc = emails_cc.split(",");
                    }
                    if (!StringUtils.isEmpty(emails_bcc)) {
                        splitbcc = emails_bcc.split(",");
                    }
                    final String[] ccEmails = splitcc;
                    final String[] bccEmails = splitbcc;
                    MailClientFactory.getInstance(idCanal).sendHTML(split, ccEmails, bccEmails, subject, email_text, attachments);

                    try {
                        //if sent ok, then forget about it
                        Nota nota = jpaController.getReference(Nota.class, Integer.valueOf(idNota));
                        nota.setFechaEnvio(new Date());
                        nota.setEnviado(Boolean.TRUE);
                        nota.setEnviadoA("to:" + Arrays.toString(split)
                                + (ccEmails == null ? "" : " cc:" + Arrays.toString(ccEmails))
                                + (bccEmails == null ? "" : " bcc:" + Arrays.toString(bccEmails)));
                        jpaController.merge(nota);
                        unschedule(formatJobId);
                    } catch (SchedulerException ex) {
                        Logger.getLogger(CaseResponseByMailJob.class.getName()).log(Level.SEVERE, "no se pudo desagendar " + formatJobId, ex);
                        //ignore
                    } catch (Exception ex) {
                        Logger.getLogger(CaseResponseByMailJob.class.getName()).log(Level.SEVERE, "Excepcion en " + formatJobId, ex);
                    }

                } catch (MailClientFactory.MailNotConfiguredException ex) {
                    Logger.getLogger(CaseResponseByMailJob.class.getName()).log(Level.SEVERE, "Mail Not Configured for " + idCanal, ex);
                    try {
                        //if MailNotConfiguredException, then forget about it
                        unschedule(formatJobId);
                    } catch (SchedulerException exe) {
                        Logger.getLogger(CaseResponseByMailJob.class.getName()).log(Level.SEVERE, "no se pudo desagendar " + formatJobId, exe);
                        //ignore
                    }
                } catch (EmailException ex) {
                    Logger.getLogger(CaseResponseByMailJob.class.getName()).log(Level.SEVERE, "EmailException trying to send email ", ex);
                } catch (SchedulerException ex) {
                    Logger.getLogger(CaseResponseByMailJob.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    jpaController = null;
                    if (emf != null) {
                        emf.close();
                    }
                    UserTransactionHelper.returnUserTransaction(utx);
                }
            } else {
                throw new JobExecutionException("Illegal parameters to Job: CaseResponseByMailJob");
            }

        }

    }

    public static void unschedule(String formatJobId) throws SchedulerException {
//        final String formatJobId = formatJobId(schema, idCaso, idalerta);
        final JobKey jobKey = JobKey.jobKey(formatJobId, HelpDeskScheluder.GRUPO_CORREO);
        HelpDeskScheluder.unschedule(jobKey);
    }

}
