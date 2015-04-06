package com.itcs.helpdesk.util;

import com.itcs.commons.email.EmailClient;
import com.itcs.commons.email.impl.NoReplySystemMailSender;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.MailClientFactory.MailNotConfiguredException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.quartz.SchedulerException;

/**
 *
 * @author Jonathan
 */
public class MailNotifier {

    public static void notifyCasoOwnerAlertChanged(String tenant, Caso caso) throws MailNotConfiguredException, EmailException, NoOutChannelException, NoInstanceConfigurationException {
        if (caso != null) {
            final ApplicationConfigs configInstance = ApplicationConfigs.getInstance(tenant);
            String asunto = configInstance.getNotificationTicketAlertChangeSubjectText(); //may contain place holders
            String subject_ = ClippingsPlaceHolders.buildFinalText(asunto, caso, tenant);

            String texto = configInstance.getNotificationTicketAlertChangeBodyText();//may contain place holders 

            String mensaje_ = ClippingsPlaceHolders.buildFinalText(texto, caso, tenant);

            final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
            final String mensaje = mensaje_;

            //choose canal, prioritize the area's default canal
            Canal canal = chooseDefaultCanalToSendMail(caso);

            try {
                if (configInstance.isAppDebugEnabled()) {
                    Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "notifyCasoOwnerAlertChanged");
                }
                HelpDeskScheluder.scheduleSendMailNow(tenant, caso.getIdCaso(), canal.getIdCanal(), mensaje,
                        caso.getOwner().getEmail(),
                        subject);
            } catch (SchedulerException ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "error at scheduleSendMailNow", ex);
                MailClientFactory.getInstance(tenant, canal.getIdCanal())
                        .sendHTML(caso.getOwner().getEmail(), subject,
                                mensaje, null);
            }

        }
    }

    public static void notifyCasoAsHtmlEmail(String tenant, Caso caso, String emailTo) throws MailNotConfiguredException, EmailException, NoInstanceConfigurationException {
        if (caso != null) {
            String asunto = "GODESK " + (caso.getIdCanal() != null ? caso.getIdCanal().getNombre() : "")
                    + " " + (caso.getTipoCaso() != null ? caso.getTipoCaso().getNombre() : EnumTipoCaso.CONTACTO.getTipoCaso().getNombre())
                    + " #" + caso.getIdCaso();//ApplicationConfig.getNotificationSubjectText(); //may contain place holders

            String texto = ClippingsPlaceHolders.buildFinalText(CasoExporter.exportToHtmlText(caso), caso, tenant);//ApplicationConfig.getNotificationBodyText();//may contain place holders 

            if (caso.getIdCanal() != null && caso.getIdCanal().getEnabled()) {

//                if (caso.getIdCanal() != null) {
                if (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null) {
                    MailClientFactory.getInstance(tenant, caso.getIdArea().getIdCanal().getIdCanal())
                            .sendHTML(emailTo, asunto,
                                    texto, null);
                } else {
                    //no default??? TODO
//                    MailClientFactory.getInstance(EnumCanal.DEFAULT_AREA.getArea().getIdArea())
//                            .sendHTML(emailTo, asunto,
//                                    texto, null);
                    throw new EmailException("No se puede enviar el correo de notificación del caso. El caso no tiene Area asociada.");
                }

            } else {
                throw new EmailException("No se puede enviar el correo. El Area Asociada al caso tiene el envío de correos desabilitado.");
            }

        }

    }

    public static void notifyCasoAssigned(String tenant, Caso caso, String motivo) throws MailNotConfiguredException, EmailException, NoOutChannelException, NoInstanceConfigurationException {
        if (caso != null) {
            String asunto = ApplicationConfigs.getInstance(tenant).getNotificationSubjectText(); //may contain place holders
            String subject_ = ClippingsPlaceHolders.buildFinalText(asunto, caso, tenant);

            String texto = ApplicationConfigs.getInstance(tenant).getNotificationBodyText();//may contain place holders 
            texto = texto + "<b>Motivo:<b/> " + (motivo != null ? motivo : "Pronta atención del caso");
            String mensaje_ = ClippingsPlaceHolders.buildFinalText(texto, caso, tenant);

            final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
            final String mensaje = mensaje_;

            //choose canal, prioritize the area's default canal
            Canal canal = chooseDefaultCanalToSendMail(caso);

            try {

                if (caso.getOwner() != null) {
                    if (caso.getOwner().getEmailNotificationsEnabled()) {
                        if (caso.getOwner().getNotifyWhenTicketAssigned()) {
                            HelpDeskScheluder.scheduleSendMailNow(tenant, caso.getIdCaso(), canal.getIdCanal(), mensaje,
                                    caso.getOwner().getEmail(),
                                    subject);
                        }
                    }
                }

            } catch (SchedulerException ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "error at scheduleSendMailNow", ex);
                MailClientFactory.getInstance(tenant, canal.getIdCanal())
                        .sendHTML(caso.getOwner().getEmail(), subject,
                                mensaje, null);
            }
        }
    }

    public static void notifyOwnerCasoUpdated(String tenant, Caso caso, String comments, String sentBy, Date fecha) throws MailNotConfiguredException, EmailException, NoOutChannelException, NoInstanceConfigurationException {
        if (caso != null) {
            String asunto = ApplicationConfigs.getInstance(tenant).getNotificationTicketUpdatedSubjectText(); //may contain place holders
            String subject_ = ClippingsPlaceHolders.buildFinalText(asunto, caso, tenant);

            String texto = ApplicationConfigs.getInstance(tenant).getNotificationTicketUpdatedBodyText();//may contain place holders 
            texto = texto + "<hr/><b>Comentario:<b/><br/> " + (comments != null ? comments : "no comments");
            texto = texto + "<br/><b>Enviado el:<b/><br/> " + (fecha != null ? fecha.toString() : "unknown");
            texto = texto + "<br/><b>Enviado por:<b/> " + (sentBy != null ? sentBy : "unknown") + "<hr/>";
            String mensaje_ = ClippingsPlaceHolders.buildFinalText(texto, caso, tenant);

            final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
            final String mensaje = mensaje_;

            //choose canal, prioritize the area's default canal
            Canal canal = chooseDefaultCanalToSendMail(caso);

            try {

                if (caso.getOwner() != null) {
                    if (caso.getOwner().getEmailNotificationsEnabled()) {
                        if (caso.getOwner().getNotifyWhenTicketIsUpdated()) {
                            HelpDeskScheluder.scheduleSendMailNow(tenant, caso.getIdCaso(), canal.getIdCanal(), mensaje,
                                    caso.getOwner().getEmail(),
                                    subject);
                        }
                    }
                }

            } catch (SchedulerException ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "error at scheduleSendMailNow", ex);
                MailClientFactory.getInstance(tenant, canal.getIdCanal())
                        .sendHTML(caso.getOwner().getEmail(), subject,
                                mensaje, null);
            }
        }
    }

    public static String emailClientCasoUpdatedByAgent(String tenant, Caso current) throws MailNotConfiguredException, EmailException, NoOutChannelException, NoInstanceConfigurationException {
        //TODO: use configure texts.
        if (current != null && current.getEmailCliente() != null) {
            String asunto = ApplicationConfigs.getInstance(tenant).getNotificationClientSubjectText(); //may contain place holders
            String newAsunto = ManagerCasos.formatIdCaso(current.getIdCaso()) + " " + ClippingsPlaceHolders.buildFinalText(asunto, current, tenant);
            String texto = ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getNotificationClientBodyText(), current, tenant);//may contain place holders 

            Canal canal = chooseDefaultCanalToSendMail(current);

            EmailClient ec = MailClientFactory.getInstance(tenant, canal.getIdCanal());

            if (ec != null) {
                ec.sendHTML(current.getEmailCliente().getEmailCliente(), newAsunto,
                        texto, null);
                return texto;
            }
        }
        return null;
    }

    public static String emailClientCustomerSurvey(String tenant, Caso current) throws MailNotConfiguredException, EmailException, NoOutChannelException, NoInstanceConfigurationException {
        //TODO: use configure texts.
        if (current != null && current.getEmailCliente() != null && current.getEmailCliente().getEmailCliente() != null) {
            String asunto = ApplicationConfigs.getInstance(tenant).getCustomerSurveySubjectText(); //may contain place holders
            String newAsunto = ManagerCasos.formatIdCaso(current.getIdCaso()) + " " + ClippingsPlaceHolders.buildFinalText(asunto, current, tenant);
            String texto = ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getCustomerSurveyBodyText(), current, tenant);//may contain place holders 

            Canal canal = chooseDefaultCanalToSendMail(current);

            EmailClient ec = MailClientFactory.getInstance(tenant, canal.getIdCanal());

            if (ec != null) {
                ec.sendHTML(current.getEmailCliente().getEmailCliente(), newAsunto,
                        texto, null);
                return texto;
            }
        }
        return null;
    }

    public static void sendEmailRecoverPassword(Usuario usuario, String newPassword) {
        if (usuario == null) {
            throw new IllegalStateException("usuario no puede ser null");
        }
        try {
//            String passMD5 = UtilSecurity.getMD5(usuario.getPass());//TODO fix, unencrypt the pass.
            String subject_ = "Recuperación de contraseña";
            StringBuilder sb = new StringBuilder();
            String mensaje_ = sb.append("Estimad@ ").append(usuario.getCapitalName()).append(",").append("<br/><br/>")
                    .append("Se ha solicido reestablecer su contraseña de acceso a GoDesk, Su contraseña temporal es: ").append("<span>")
                    .append(newPassword)
                    .append("</span>")
                    .append("<br/>Se recomienda cambiar esta contraseña lo antes posible.<br/><br/>")
                    .append("Equipo Godesk<br/>www.godesk.cl").toString();
            NoReplySystemMailSender.sendHTML(usuario.getEmail(), subject_, mensaje_, null);
            Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "sendEmailRecoverPassword succeeded:{0}", usuario.getEmail());
        } catch (EmailException ex) {
            Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "sendEmailRecoverPassword", ex);
        }
    }

    public static void sendEmailVerifyNewAccount(String tenantCompanyName, String email, String userName, String token) {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(userName)) {
            throw new IllegalStateException("email/userName no puede ser null");
        }
        try {
//            String passMD5 = UtilSecurity.getMD5(usuario.getPass());//TODO fix, unencrypt the pass.
            String subject_ = "Please verify this email address";
            StringBuilder sb = new StringBuilder();
            
            String mensaje_ = sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                    + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                    + "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                    + "        <title>Please verify this email address</title>\n"
                    + "    </head>\n"
                    + "    <body>\n"
                    + "        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#f5f5f5\">\n"
                    + "            <tr>\n"
                    + "                <td><table width=\"500\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\n"
                    + "                        <tr>\n"
                    + "                            <td>\n"
                    + "                                <p align=\"center\" style=\"font:9px Helvetica Neue, Helvetica, Arial, sans-serif;margin:0;padding:0;color:#777777;display:none !important\">Activate your account</p>\n"
                    + "                            </td>\n"
                    + "                        </tr>\n"
                    + "                        <tr>\n"
                    + "                            <td>\n"
                    + "                                <table width=\"500\" height=\"122\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n"
                    + "                                    <tr>\n"
                    + "                                        <td width=\"161\"><a href=\"http://www.godesk.cl\"><img src=\"http://www.godesk.cl/images/LogGoDesk2.png\" alt=\"Godesk\"  border=\"0\" /></a></td>\n"
                    + "                                        <td width=\"338\" align=\"right\"><p style=\"font:9px Helvetica Neue, Helvetica, Arial, sans-serif;letter-spacing:3px;margin:0;padding:0;\"><span style=\"color:#1a1a1a;\">EMAIL VERIFICATION</span></p></td>\n"
                    + "                                        <td width=\"1\"><img src=\"http://www.squarespace.com/storage/newsletter/images/dot.gif\" alt=\"\" width=\"1\" height=\"122\" /></td>\n"
                    + "                                    </tr>\n"
                    + "                                </table></td>\n"
                    + "                        </tr>\n"
                    + "                        <tr>\n"
                    + "                            <td height=\"50\"><table width=\"500\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#ffffff\" style=\"border:1px solid #e3e3e3;padding:0;margin:0;\">\n"
                    + "                                    <tr>\n"
                    + "                                        <td width=\"60\" height=\"50\"><img src=\"http://www.squarespace.com/storage/newsletter/images/dot.gif\" alt=\"\" width=\"60\" height=\"50\" border=\"0\" /></td>\n"
                    + "                                        <td width=\"380\"><img src=\"http://www.squarespace.com/storage/newsletter/images/dot.gif\" alt=\"\" width=\"380\" height=\"1\" align=\"top\" border=\"0\" /></td>\n"
                    + "                                        <td width=\"60\"><img src=\"http://www.squarespace.com/storage/newsletter/images/dot.gif\" alt=\"\" width=\"60\" height=\"1\" border=\"0\" /></td>\n"
                    + "                                    </tr>\n"
                    + "                                    <tr>\n"
                    + "                                        <td>&nbsp;</td>\n"
                    + "                                        <td>\n"
                    + "                                            <p style=\"font:22px Helvetica Neue, Helvetica, Arial, sans-serif;font-weight:bold;margin:0px 0 0 0;padding:0;color:#000;\">Welcome, and thanks for signing up for Godesk!</p>\n"
                    + "                                            <p style=\"font:14px Helvetica Neue, Helvetica, Arial, sans-serif;color:#777777;line-height:21px;margin:15px 0 0 0;padding:0;\">You are one step away from using godesk, please click on the link below to activate your account.</p><br/>\n"
                    + "<a style=\"font:14px Helvetica Neue, Helvetica, Arial, sans-serif;\" href=\"{{ACTIVATION_LINK}}\">Activate your account</a>\n"
                    + "                                        </td>\n"
                    + "                                        <td>&nbsp;</td>\n"
                    + "                                    </tr>\n"
                    + "                                    <tr>\n"
                    + "                                        <td width=\"60\"><img src=\"http://www.squarespace.com/storage/newsletter/images/dot.gif\" alt=\"\" width=\"60\" height=\"1\" /></td>\n"
                    + "                                        <td width=\"380\"><img src=\"http://www.squarespace.com/storage/newsletter/images/dot.gif\" alt=\"\" width=\"380\" height=\"1\" /></td>\n"
                    + "                                        <td width=\"60\"><img src=\"http://www.squarespace.com/storage/newsletter/images/dot.gif\" alt=\"\" width=\"60\" height=\"1\" /></td>\n"
                    + "                                    </tr>\n"
                    + "                                    <tr>\n"
                    + "                                        <td height=\"150\">&nbsp;</td>\n"
                    + "                                    </tr>\n"
                    + "                                    <tr>\n"
                    + "                                        <td height=\"150\">&nbsp;</td>\n"
                    + "                                        <td><p style=\"font:18px Helvetica Neue, Helvetica, Arial, sans-serif;font-weight:bold;margin:10px 0 10px 0;padding:10px 0 0 0;color:#000000;\">We are here to help.</p><p style=\"font:14px Helvetica Neue, Helvetica, Arial, sans-serif;color:#777777;line-height:21px;margin:0;padding:0;\">Our award-winning Customer Care team is available 24/7 at <a style=\"font:14px Helvetica Neue, Helvetica, Arial, sans-serif; color:#777; text-decoration:underline;\" href=\"http://www.godesk.cl\">www.godesk.cl</a> or contact us at <a href=\"mailto:contacto@godesk.cl\">contacto@godesk.cl</a>.</p></td>\n"
                    + "                                        <td>&nbsp;</td>\n"
                    + "                                    </tr>\n"
                    + "                                </table></td>\n"
                    + "                        </tr>\n"
                    + "                        <tr>\n"
                    + "                            <td height=\"10\" align=\"center\" valign=\"middle\"></td>\n"
                    + "                        </tr>\n"
                    + "                        <tr>\n"
                    + "                            <td height=\"30\" align=\"center\"><p style=\"font:12px Helvetica Neue, Helvetica, Arial, sans-serif;margin:0;padding:0;color:#777777;\">Godesk, Ltda. Sucre 2680, Ñuñoa, Santiago</p></td>\n"
                    + "                        </tr>\n"
                    + "                        <tr>\n"
                    + "                            <td height=\"50\" align=\"center\">&nbsp;</td>\n"
                    + "                        </tr>\n"
                    + "                    </table></td>\n"
                    + "            </tr>\n"
                    + "        </table>\n"
                    + "    </body>\n"
                    + "</html>").toString().replace("{{ACTIVATION_LINK}}", "http://www.godesk.cl/go/faces/public/VerifyAccount.xhtml?token="+token);
            
           
            NoReplySystemMailSender.sendHTML(email, subject_, mensaje_, null);
            Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "sendEmailVerifyNewAccount succeeded:{0}", email);
        } catch (EmailException ex) {
            Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "sendEmailVerifyNewAccount", ex);
        }
    }

   

    public static void sendGreetingsToNewAccount(String tenant, String email, String userName, String code) {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(userName)) {
            throw new IllegalStateException("email/userName no puede ser null");
        }
        try {
//            String passMD5 = UtilSecurity.getMD5(usuario.getPass());//TODO fix, unencrypt the pass.
            String subject_ = "Welcome, and thanks for signing up for Godesk!";
            StringBuilder sb = new StringBuilder();
            String mensaje_ = sb.append("Estimad@ ").append(userName).append(",").append("<br/><br/>")
                    .append("[Thanks email here]")
                    .append("<br/>")
                    .append("<br/><br/>")
                    .append("Equipo Godesk<br/>www.godesk.cl").toString();
            NoReplySystemMailSender.sendHTML(email, subject_, mensaje_, null);
            Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "sendGreetingsToNewAccount succeeded:{0}", email);
        } catch (EmailException ex) {
            Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "sendGreetingsToNewAccount", ex);
        }
    }

    public static void notifyClientCasoReceived(String tenant, Caso caso) throws NoOutChannelException, SchedulerException, NoInstanceConfigurationException {
        //new: permit to have a global response in the config
        //new: if area is not enabled, send a receipt confirmation to client from same channel this should be the default.
        //TODO: record this notification as an activity (Nota) in the case.

        if (ApplicationConfigs.getInstance(tenant).isSendNotificationToClientOnNewTicket()) {

            if (caso != null && caso.getEmailCliente() != null) {

                String mensaje_ = "";
                String subject_ = "";

                if (caso.getIdArea() != null) {
                    if (caso.getIdArea().getEmailAcusederecibo()) {
                        if (!StringUtils.isEmpty(caso.getIdArea().getTextoRespAutomatica())
                                && !StringUtils.isEmpty(caso.getIdArea().getSubjectRespAutomatica())) {
                            //use area config texts
                            mensaje_ = (ClippingsPlaceHolders.buildFinalText(caso.getIdArea().getTextoRespAutomatica(), caso, tenant));
                            subject_ = (ClippingsPlaceHolders.buildFinalText(caso.getIdArea().getSubjectRespAutomatica(), caso, tenant));
                        } else {
                            //no tiene textos, use global config 
                            mensaje_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getNotificationClientBodyNewTicketText(), caso, tenant));
                            subject_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getNotificationClientSubjectNewTicketText(), caso, tenant));
                        }
                    } else {
                        //area manda! si dice que no enviar entonces no enviar acuse
                        Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "area {0} manda! si dice no enviar acuse entonces no enviar acuse", caso.getIdArea() + ":" + caso.getIdArea().getEmailAcusederecibo());
                        return;
                    }
                } else {
                    //no tiene area, use global config 
                    mensaje_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getNotificationClientBodyNewTicketText(), caso, tenant));
                    subject_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getNotificationClientSubjectNewTicketText(), caso, tenant));
                }

                final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
                final String mensaje = mensaje_;

                Canal canal = chooseDefaultCanalToSendMail(caso);

                HelpDeskScheluder.scheduleSendMailNow(tenant, caso.getIdCaso(), canal.getIdCanal(), mensaje,
                        caso.getEmailCliente().getEmailCliente(),
                        subject);

            }

        }

    }

    public static void notifyClientConfirmedEvent(String tenant, Caso caso) throws NoOutChannelException, SchedulerException, NoInstanceConfigurationException {
        //TODO: USE THIS AS EVENT CONFIRMATION NOT CASO CREATION. SOON
        if (ApplicationConfigs.getInstance(tenant).isSendNotificationOnSubscribedToEvent()) {
            if (caso != null && caso.getEmailCliente() != null) {
                final String mensaje = (ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getNotificationClientBodySubscribedToEventText(), caso, tenant));
                final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + (ClippingsPlaceHolders.buildFinalText(ApplicationConfigs.getInstance(tenant).getNotificationClientSubjectSubscribedToEventText(), caso, tenant));
                Canal canal = chooseDefaultCanalToSendMail(caso);
                HelpDeskScheluder.scheduleSendMailNow(tenant, caso.getIdCaso(), canal.getIdCanal(), mensaje,
                        caso.getEmailCliente().getEmailCliente(),
                        subject);
            }
        }
    }

    /**
     *
     * Use Same for all (config) TODO Add configurable subject and body for this
     * message.
     *
     * @param tenant
     * @param grupo
     * @param caso
     */
    public static void notifyGroupCasoReceived(String tenant, final Grupo grupo, Caso caso) {
        Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "notifyAgentsCasoReceived:{0}", grupo);
        //TODO: use configure texts.
        if (caso != null) {
            try {

                if (grupo.getUsuarioList() != null && !grupo.getUsuarioList().isEmpty()) {

                    StringBuilder to = new StringBuilder();
                    boolean first = true;
                    for (Usuario usuario : grupo.getUsuarioList()) {
                        if (first) {
                            if (usuario.getEmailNotificationsEnabled()) {
                                if (usuario.getNotifyWhenNewTicketInGroup()) {
                                    to.append(usuario.getEmail());
                                }
                            }

                            first = false;
                        } else {
                            if (usuario.getEmailNotificationsEnabled()) {
                                if (usuario.getNotifyWhenNewTicketInGroup()) {
                                    to.append(",").append(usuario.getEmail());
                                }
                            }

                        }

                    }

                    if (!StringUtils.isEmpty(to)) {

                        final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " "
                                + "GoDesk " + (caso.getIdCanal() != null ? caso.getIdCanal().getNombre() : "")
                                + " Nuev@ " + (caso.getTipoCaso() != null ? caso.getTipoCaso().getNombre() : EnumTipoCaso.CONTACTO.getTipoCaso().getNombre());//ApplicationConfig.getNotificationSubjectText(); //may contain place holders

                        final String mensaje = "Estimad@ Agente, <br/><br/>"
                                + "Le notificamos que existe un caso en su grupo (" + grupo.getNombre() + ") para su pronta atención.<br/> "
                                + "Favor contactar al cliente lo antes posible.<br/>";

                        final String mensajeFinal = mensaje + CasoExporter.exportToHtmlText(caso);

                        Canal canal = chooseDefaultCanalToSendMail(caso);

                        try {
                            HelpDeskScheluder.scheduleNotifyAgentsCasoReceived(tenant, canal.getIdCanal(), mensajeFinal, to.toString(), subject, caso.getIdCaso());
                        } catch (SchedulerException ex) {
                            //It may fail right away, we still continue
                            Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "scheduleNotifyAgentsCasoReceived failed!", ex);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "notifyAgentsCasoReceived", ex);
            }
        }
    }

    /**
     * Algoritmo para determinar el canal de salida del caso.
     *
     * @param caso
     * @return
     * @throws com.itcs.helpdesk.util.NoOutChannelException
     */
    public static Canal chooseDefaultCanalToSendMail(Caso caso) throws NoOutChannelException {

        if (caso.getIdCanal() != null && caso.getIdCanal().getIdTipoCanal() != null
                && caso.getIdCanal().getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal())) {
            return caso.getIdCanal();
        } else {

            //choose canal, prioritize the area's default canal
            Canal chosenEmailChannel = (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null
                    && caso.getIdArea().getIdCanal().getIdTipoCanal() != null
                    && caso.getIdArea().getIdCanal().getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal()))
                            ? caso.getIdArea().getIdCanal() : null;

//            if (chosenEmailChannel == null) {
//            //We do not use this since product may have a ventas email, and when a postventa user gets here there will be problems.
//                //choose the project's default canal
//                chosenEmailChannel = (caso.getIdProducto() != null && caso.getIdProducto().getIdOutCanal() != null
//                        && caso.getIdProducto().getIdOutCanal().getIdTipoCanal() != null 
//                        && caso.getIdProducto().getIdOutCanal().getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal()))
//                        ? caso.getIdProducto().getIdOutCanal() : null;
//            }
            if (chosenEmailChannel != null
                    && chosenEmailChannel.getIdTipoCanal() != null
                    && chosenEmailChannel.getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal())
                    && !StringUtils.isEmpty(chosenEmailChannel.getIdCanal())) {

                return chosenEmailChannel;

            } else {
                throw new NoOutChannelException();
            }
        }

    }

}
