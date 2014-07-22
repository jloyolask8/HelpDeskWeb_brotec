/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.commons.email.EmailClient;
import com.itcs.commons.email.EmailMessage;
import com.itcs.commons.email.EnumEmailSettingKeys;
import com.itcs.helpdesk.persistence.entities.BlackListEmail;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.CanalSettingPK;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.RulesEngine;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.ee.jta.UserTransactionHelper;

/**
 *
 * @author jonathan
 */
public class DownloadEmailJob extends AbstractGoDeskJob implements Job {

    /**
     * {0} = schema {1} = idArea#
     */
    public static final String JOB_ID = "DownloadEmailCanal_%s";

    public static String formatJobId(String idCanal) {
        return String.format(JOB_ID, new Object[]{idCanal});
    }

//    public DownloadEmailJob(JPAServiceFacade jpaController, ManagerCasos managerCasos) {
//        super(jpaController, managerCasos);
//    }
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
        if (map != null) {
            String idCanal = (String) map.get(ID_CANAL);
            String interval = (String) map.get(INTERVAL_SECONDS);
            try {
                if (!StringUtils.isEmpty(idCanal) && !StringUtils.isEmpty(interval)) {
                    revisarCorreo(idCanal);
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "erron on execute DownloadEmailJob", ex);
            } finally {
                try {
                    //schedule to run again after that interval
                    HelpDeskScheluder.scheduleRevisarCorreo(idCanal, Integer.valueOf(interval));
                } catch (SchedulerException ex) {
                    Logger.getLogger(DownloadEmailJob.class.getName()).log(Level.SEVERE, "ERROR TRYING TO scheduleRevisarCorreo", ex);
                }
            }
        }
    }

    private String getValueOfCanalSetting(JPAServiceFacade jpaController, Canal canal, EnumEmailSettingKeys settingKey) {
        try {
            String value = jpaController.getCanalSettingJpaController().findCanalSetting(
                    new CanalSettingPK(canal.getIdCanal(), settingKey.getKey())).getCanalSettingValue();
            return value;
        } catch (Exception ex) {
            return null;
        }
    }

    private synchronized void revisarCorreo(String idCanal) throws Exception, MailClientFactory.MailNotConfiguredException {

//        EntityManager em = createEntityManager(schema);
        EntityManagerFactory emf = createEntityManagerFactory();
        UserTransaction utx = UserTransactionHelper.lookupUserTransaction();
//        System.out.println(UserTransactionHelper.getUserTxLocation() + ":" + utx);//DEBUG

        JPAServiceFacade jpaController = new JPAServiceFacade(utx, emf);
        RulesEngine rulesEngine = new RulesEngine(emf, jpaController);
        jpaController.setCasoChangeListener(rulesEngine);
        ManagerCasos managerCasos = new ManagerCasos(jpaController);

//        JPAServiceFacade jpaController = createJpaController(schema);
//        ManagerCasos managerCasos = new ManagerCasos();
//        managerCasos.setJpaController(jpaController);
        Canal canal = jpaController.find(Canal.class, idCanal);
        if (canal != null) {
            String emailSenderArea = getValueOfCanalSetting(jpaController, canal, EnumEmailSettingKeys.SMTP_USER);
                    
            String emailReceiverArea = getValueOfCanalSetting(jpaController, canal, EnumEmailSettingKeys.INBOUND_USER);

            EmailClient mailClient = MailClientFactory.getInstance(canal.getIdCanal());

            if (mailClient == null) {
                mailClient = MailClientFactory.createInstance(canal);
            }

            if (mailClient != null) {
                mailClient.connectStore();
                mailClient.openFolder("inbox");
                List<EmailMessage> messages = mailClient.getUnreadMessages();
                //List<EmailMessage> messages = mailClient.getAllMessages();
                List<BlackListEmail> blackList = (List<BlackListEmail>) jpaController.findAll(BlackListEmail.class);//findAll(BlackListEmail.class);
                HashMap<String, BlackListEmail> mapBlackList = new HashMap<String, BlackListEmail>();
                for (BlackListEmail blackListEmail : blackList) {
                    mapBlackList.put(blackListEmail.getEmailAddress(), blackListEmail);
                }

                if (ApplicationConfig.getInstance().isAppDebugEnabled()) {
                    Log.createLogger(this.getClass().getName()).logDebug("Debug Email BlackList:" + mapBlackList);
                }

                for (EmailMessage emailMessage : messages) {
                    try {
                        if (!mapBlackList.containsKey(emailMessage.getFromEmail()) && (!emailMessage.getFromEmail().equalsIgnoreCase(emailSenderArea))
                                && (!emailMessage.getFromEmail().equalsIgnoreCase(emailReceiverArea))) {
                            final boolean casoIsCreated = managerCasos.crearCasoDesdeEmail(canal, emailMessage);
                            if (casoIsCreated) {
                                mailClient.markReadMessage(emailMessage);
//                                mailClient.deleteMessage(emailMessage);//TODO add a config, deleteMessagesAfterSuccessRead?
                            }
                        } else {
                            if (ApplicationConfig.getInstance().isAppDebugEnabled()) {
                                Log.createLogger(this.getClass().getName()).logDebug("BLOCKED EMAIL FROM:" + emailMessage.getFromEmail());
                            }
                        }
                    } catch (MessagingException e) {
                        Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "MessagingException Error: No se pudo crear el caso. email subject: " + emailMessage.getSubject(), e);
                    } finally {
                        mailClient.markReadMessage(emailMessage);
                        //  mailClient.deleteMessage(emailMessage);
                    }

                }

                mailClient.closeFolder();
                mailClient.disconnectStore();

                if (ApplicationConfig.getInstance().isAppDebugEnabled()) {
                    Log.createLogger(this.getClass().getName()).logDebug("Revisión de correo " + canal + "exitósa: " + messages.size() + " mensajes leídos. Intancia: brotec-icafal");
                }
            } else {
                throw new MailClientFactory.MailNotConfiguredException("No se puede enviar correos, favor comunicarse con el administrador para que configure la cuenta de correo asociada al Área.");
            }
        }

    }

}
