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
import com.itcs.helpdesk.persistence.entities.CanalSetting;
import com.itcs.helpdesk.persistence.entities.CanalSettingPK;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.RulesEngine;
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
    public static final String JOB_ID = "DownloadEmail_Canal_%s";
    public static final int N_EMAILS_FETCH = 10;

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
            int secondsToNextSync = Integer.valueOf(interval);
            try {
                if (!StringUtils.isEmpty(idCanal) && !StringUtils.isEmpty(interval)) {
                    secondsToNextSync = revisarCorreo(idCanal);
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "error on execute DownloadEmailJob. Canal:" + idCanal, ex);
            } finally {
                try {
                    //schedule to run again after that interval
                    HelpDeskScheluder.scheduleRevisarCorreo(idCanal, secondsToNextSync);
                } catch (SchedulerException ex) {
                    Logger.getLogger(DownloadEmailJob.class.getName()).log(Level.SEVERE, "ERROR TRYING TO schedule next RevisarCorreo Canal:" + idCanal, ex);
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

    /**
     *
     * @param idCanal idCanal for canal settings
     * @return interval for next mailSync
     * @throws Exception
     * @throws
     * com.itcs.helpdesk.util.MailClientFactory.MailNotConfiguredException
     */
    private synchronized int revisarCorreo(String idCanal) throws Exception, MailClientFactory.MailNotConfiguredException {

        int freq = HelpDeskScheluder.DEFAULT_CHECK_EMAIL_INTERVAL;

//        EntityManager em = createEntityManager(schema);
        EntityManagerFactory emf = createEntityManagerFactory();
        UserTransaction utx = UserTransactionHelper.lookupUserTransaction();
//        System.out.println(UserTransactionHelper.getUserTxLocation() + ":" + utx);//DEBUG

        JPAServiceFacade jpaController = new JPAServiceFacade(utx, emf);
        RulesEngine rulesEngine = new RulesEngine(emf, jpaController);
        jpaController.setCasoChangeListener(rulesEngine);
        ManagerCasos managerCasos = new ManagerCasos(jpaController);

        try {

//        JPAServiceFacade jpaController = createJpaController(schema);
//        ManagerCasos managerCasos = new ManagerCasos();
//        managerCasos.setJpaController(jpaController);
            Canal canal = jpaController.find(Canal.class, idCanal);

            if (canal != null) {
                String freqStr = canal.getSetting(EnumEmailSettingKeys.CHECK_FREQUENCY.getKey());
                try {
                    if (freqStr != null) {
                        freq = Integer.parseInt(freqStr);
                    }
                } catch (NumberFormatException ex) {
                    /*probably a weird value, silently ignore it*/
                }

                if (canal.getEnabled()) {

                    String emailSender = getValueOfCanalSetting(jpaController, canal, EnumEmailSettingKeys.SMTP_USER);

                    String emailReceiver = getValueOfCanalSetting(jpaController, canal, EnumEmailSettingKeys.INBOUND_USER);
                    EmailClient mailClient;
                    try {
                        mailClient = MailClientFactory.getInstance(canal.getIdCanal());
                    } catch (MailClientFactory.MailNotConfiguredException ex) {
                        mailClient = MailClientFactory.createInstance(canal);
                    }

                    if (mailClient != null) {

                        long highestUID = 0;
                        try {
                            mailClient.connectStore();
                            mailClient.openFolder("inbox");
                            List<EmailMessage> messages;
                            if (canal.containsKey(EnumEmailSettingKeys.HIGHEST_UID.getKey())) {
                                long nextUID = Long.parseLong(canal.getSetting(EnumEmailSettingKeys.HIGHEST_UID.getKey())) + 1;
                                //Trae los mensajes de a 10
                                messages = mailClient.getMessagesOnlyHeaders(nextUID, nextUID + N_EMAILS_FETCH);
                            } else {
                                if (canal.containsKey(EnumEmailSettingKeys.UNREAD_DOWNLOAD_LIMIT.getKey())) {
                                    int limit = Integer.parseInt(canal.getSetting(EnumEmailSettingKeys.UNREAD_DOWNLOAD_LIMIT.getKey()));
                                    messages = mailClient.getUnreadMessagesOnlyHeaders(limit);
                                }else
                                {
                                    int limit = Integer.parseInt(ApplicationConfig.DEFAULT_UNREAD_DOWNLOAD_LIMIT);
                                    messages = mailClient.getUnreadMessagesOnlyHeaders(limit);
                                }
                            }
    //                    List<EmailMessage> messages = mailClient.getUnreadMessages();

                            //Waste of resources, what if we have a million of blacklisted mails??
                            //                    List<BlackListEmail> blackList = (List<BlackListEmail>) jpaController.findAll(BlackListEmail.class);//findAll(BlackListEmail.class);
                            //                    HashMap<String, BlackListEmail> mapBlackList = new HashMap<>();
                            //                    for (BlackListEmail blackListEmail : blackList) {
                            //                        mapBlackList.put(blackListEmail.getEmailAddress(), blackListEmail);
                            //                    }
                            //                    if (ApplicationConfig.isAppDebugEnabled()) {
                            //                        Log.createLogger(this.getClass().getName()).logDebug("Debug Email BlackList:" + mapBlackList);
                            //                    }
                            for (EmailMessage emailMessage : messages) {
                                try {
                                    if (emailMessage.getIdMessage() > highestUID) {
                                        highestUID = emailMessage.getIdMessage();
//                                        System.out.println("highestUID: " + highestUID);
                                    }
                                    //if isn't a blacklisted address
                                    if ((jpaController.find(BlackListEmail.class, emailMessage.getFromEmail()) == null)
                                            && (!emailMessage.getFromEmail().equalsIgnoreCase(emailSender))
                                            && (!emailMessage.getFromEmail().equalsIgnoreCase(emailReceiver))) {

                                        String subject = emailMessage.getSubject();
                                        Long idCaso = ManagerCasos.extractIdCaso(subject);

                                        if (idCaso != null) {
                                            Caso caso = jpaController.find(Caso.class, idCaso);
                                            if (caso != null) {
                                                //Si el caso encontrado tiene un id de caso combinado, hay que crear la nota en el caso que quedo de la combinacion
                                                if (caso.getIdCasoCombinado() != null) {
                                                    caso = caso.getIdCasoCombinado();
                                                }
                                                //download message
                                                //Si está configurado bajar attachments y el correo tiene attachments se vuelve a descargar con attachments
                                                if ((canal.getSetting(EnumEmailSettingKeys.DOWNLOAD_ATTACHMENTS.getKey()) != null)
                                                        && canal.getSetting(EnumEmailSettingKeys.DOWNLOAD_ATTACHMENTS.getKey()).equals("true")
                                                        && emailMessage.isHasAttachment()) {
                                                    emailMessage = mailClient.getMessage(emailMessage.getIdMessage());
                                                }
                                                managerCasos.crearNotaDesdeEmail(caso, canal, emailMessage);
                                            }

                                        } else {
                                            //block messages that do not ref# to a case comming from an FromEmail configured as a agent's email addresss.
                                            //NEW TICKET?
                                            List<Usuario> users = jpaController.getUsuarioFindByEmail(emailMessage.getFromEmail());

                                            boolean download;
                                            if ((users != null) && (!users.isEmpty())) {
                                                Usuario user = users.get(0);
                                                //Esto debe hacerse configurable
                                                /*if ((user.getUsuarioList() != null) && (!user.getUsuarioList().isEmpty())) {
                                                 //this guy is a supervisor, he can create tickets
                                                 download = true;
                                                 } else */
                                                {
                                                    //ignore emails from users of the system !!
                                                    //let them know that this is now allowed!!
                                                    download = false;
                                                    if (ApplicationConfig.isAppDebugEnabled()) {
                                                        Log.createLogger(this.getClass().getName()).logDebug("IGNORING MESSAGE " + emailMessage.getIdMessage() + " from user  of the system :" + users);
                                                    }
                                                }

                                            } else {
                                                download = true;
                                            }
                                            if (download) {
                                                //download message
                                                //Si está configurado bajar attachments y el correo tiene attachments se vuelve a descargar con attachments
                                                if ((canal.getSetting(EnumEmailSettingKeys.DOWNLOAD_ATTACHMENTS.getKey()) != null)
                                                        && canal.getSetting(EnumEmailSettingKeys.DOWNLOAD_ATTACHMENTS.getKey()).equals("true")
                                                        && emailMessage.isHasAttachment()) {
                                                    emailMessage = mailClient.getMessage(emailMessage.getIdMessage());
                                                }
                                                final boolean casoIsCreated = managerCasos.crearCasoDesdeEmail(canal, emailMessage);
                                                if (casoIsCreated) {
                                                    mailClient.markReadMessage(emailMessage);
                                                    //mailClient.deleteMessage(emailMessage);//TODO add a config, deleteMessagesAfterSuccessRead?
                                                }
                                            }

                                        }
                                    } else {
                                        if (ApplicationConfig.isAppDebugEnabled()) {
                                            Log.createLogger(this.getClass().getName()).logDebug("BLOCKED MESSAGE " + emailMessage.getIdMessage() + " FROM:" + emailMessage.getFromEmail());
                                        }
                                    }
                                } catch (MessagingException e) {
                                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "MessagingException Error: No se pudo crear el caso. email subject: " + emailMessage.getSubject(), e);
                                } finally {
                                    mailClient.markReadMessage(emailMessage);
                                    //  mailClient.deleteMessage(emailMessage);
                                }
                            }

                            if (ApplicationConfig.isAppDebugEnabled()) {
                                Log.createLogger(this.getClass().getName()).logDebug("Revisión de correo " + canal + "exitosa: " + messages.size() + " mensajes leídos. Intancia: brotec-icafal");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            try {
                                if (highestUID > 0) {
                                    System.out.println("saving highestUID: " + highestUID);
                                    canal.getCanalSettingList().remove(new CanalSetting(canal.getIdCanal(), EnumEmailSettingKeys.HIGHEST_UID.getKey()));
                                    canal.getCanalSettingList().add(new CanalSetting(canal, EnumEmailSettingKeys.HIGHEST_UID.getKey(), String.valueOf(highestUID), ""));
                                    jpaController.merge(canal);
                                }

                                mailClient.closeFolder();
                                mailClient.disconnectStore();

                            } catch (Exception ex2) {
                                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error al cerrar folder/store o returnUserTransaction", ex2);
                            }
                        }
                    } else {
                        throw new MailClientFactory.MailNotConfiguredException("No se puede descargar correos del canal " + idCanal + ", favor comunicarse con el administrador para que configure la cuenta de correo.");
                    }
                }
            }
        } finally {
            jpaController = null;
            UserTransactionHelper.returnUserTransaction(utx);
            emf.close();
        }

        return freq;
    }

}
