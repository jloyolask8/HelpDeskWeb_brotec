package com.itcs.helpdesk.util;

import com.itcs.commons.email.EmailClient;
import com.itcs.commons.email.impl.NoReplySystemMailSender;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
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

    public static void notifyCasoOwnerAlertChanged(Caso caso) throws MailNotConfiguredException, EmailException, NoOutChannelException {
        if (caso != null) {
            String asunto = ApplicationConfig.getNotificationTicketAlertChangeSubjectText(); //may contain place holders
            String subject_ = ClippingsPlaceHolders.buildFinalText(asunto, caso);

            String texto = ApplicationConfig.getNotificationTicketAlertChangeBodyText();//may contain place holders 

            String mensaje_ = ClippingsPlaceHolders.buildFinalText(texto, caso);

            final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
            final String mensaje = mensaje_;

            //choose canal, prioritize the area's default canal
            Canal canal = chooseDefaultCanalToSendMail(caso);

            try {
                if (ApplicationConfig.isAppDebugEnabled()) {
                    Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "notifyCasoOwnerAlertChanged");
                }
                if (caso.getEstadoAlerta().equals(EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta())) {

                    HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                            caso.getOwner().getEmail(),
                            subject);
                    
                } else if (caso.getEstadoAlerta().equals(EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta())) {
                    HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                            caso.getOwner().getEmail() + (caso.getOwner().getSupervisor() != null ? (","+caso.getOwner().getSupervisor().getEmail()) :""),
                            subject);
                }

            } catch (SchedulerException ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "error at scheduleSendMailNow", ex);
                MailClientFactory.getInstance(canal.getIdCanal())
                        .sendHTML(caso.getOwner().getEmail(), subject,
                                mensaje, null);
            }

        }
    }

    public static void notifyCasoAsHtmlEmail(Caso caso, String emailTo) throws MailNotConfiguredException, EmailException {
        if (caso != null) {
            String asunto = "HELPDESK " + (caso.getIdCanal() != null ? caso.getIdCanal().getNombre() : "")
                    + " " + (caso.getTipoCaso() != null ? caso.getTipoCaso().getNombre() : EnumTipoCaso.CONTACTO.getTipoCaso().getNombre())
                    + " #" + caso.getIdCaso();//ApplicationConfig.getNotificationSubjectText(); //may contain place holders

            String texto = ClippingsPlaceHolders.buildFinalText(CasoExporter.exportToHtmlText(caso), caso);//ApplicationConfig.getNotificationBodyText();//may contain place holders 

            if (caso.getIdCanal() != null && caso.getIdCanal().getEnabled() != null
                    && caso.getIdCanal().getEnabled()) {

//                if (caso.getIdCanal() != null) {
                if (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null) {
                    MailClientFactory.getInstance(caso.getIdArea().getIdCanal().getIdCanal())
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

    public static void notifyCasoAssigned(Caso caso, String motivo) throws MailNotConfiguredException, EmailException, NoOutChannelException {
        if (caso != null) {
            String asunto = ApplicationConfig.getNotificationSubjectText(); //may contain place holders
            String subject_ = ClippingsPlaceHolders.buildFinalText(asunto, caso);

            String texto = ApplicationConfig.getNotificationBodyText();//may contain place holders 
            texto = texto + "<b>Motivo:<b/> " + (motivo != null ? motivo : "Pronta atención del caso");
            String mensaje_ = ClippingsPlaceHolders.buildFinalText(texto, caso);

            final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
            final String mensaje = mensaje_;

            //choose canal, prioritize the area's default canal
            Canal canal = chooseDefaultCanalToSendMail(caso);

            try {

                if (caso.getOwner() != null) {
                    if (caso.getOwner().getEmailNotificationsEnabled()) {
                        if (caso.getOwner().getNotifyWhenTicketAssigned()) {
                            HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                                    caso.getOwner().getEmail(),
                                    subject);
                        }
                    }
                }

            } catch (SchedulerException ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "error at scheduleSendMailNow", ex);
                MailClientFactory.getInstance(canal.getIdCanal())
                        .sendHTML(caso.getOwner().getEmail(), subject,
                                mensaje, null);
            }
        }
    }

    public static void notifyOwnerCasoUpdated(Caso caso, String comments, String sentBy, Date fecha) throws MailNotConfiguredException, EmailException, NoOutChannelException {
        if (caso != null) {
            String asunto = ApplicationConfig.getNotificationTicketUpdatedSubjectText(); //may contain place holders
            String subject_ = ClippingsPlaceHolders.buildFinalText(asunto, caso);

            String texto = ApplicationConfig.getNotificationTicketUpdatedBodyText();//may contain place holders 
            texto = texto + "<hr/><b>Comentario:<b/><br/> " + (comments != null ? comments : "no comments");
            texto = texto + "<br/><b>Enviado el:<b/><br/> " + (fecha != null ? fecha.toString() : "unknown");
            texto = texto + "<br/><b>Enviado por:<b/> " + (sentBy != null ? sentBy : "unknown") + "<hr/>";
            String mensaje_ = ClippingsPlaceHolders.buildFinalText(texto, caso);

            final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
            final String mensaje = mensaje_;

            //choose canal, prioritize the area's default canal
            Canal canal = chooseDefaultCanalToSendMail(caso);

            try {

                if (caso.getOwner() != null) {
                    if (caso.getOwner().getEmailNotificationsEnabled()) {
                        if (caso.getOwner().getNotifyWhenTicketIsUpdated()) {
                            HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                                    caso.getOwner().getEmail(),
                                    subject);
                        }
                    }
                }

            } catch (SchedulerException ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "error at scheduleSendMailNow", ex);
                MailClientFactory.getInstance(canal.getIdCanal())
                        .sendHTML(caso.getOwner().getEmail(), subject,
                                mensaje, null);
            }
        }
    }

    public static String emailClientCasoUpdatedByAgent(Caso current) throws MailNotConfiguredException, EmailException, NoOutChannelException {
        //TODO: use configure texts.
        if (current != null && current.getEmailCliente() != null) {
            String asunto = ApplicationConfig.getNotificationClientSubjectText(); //may contain place holders
            String newAsunto = ManagerCasos.formatIdCaso(current.getIdCaso()) + " " + ClippingsPlaceHolders.buildFinalText(asunto, current);
            String texto = ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientBodyText(), current);//may contain place holders 

            Canal canal = chooseDefaultCanalToSendMail(current);

            EmailClient ec = MailClientFactory.getInstance(canal.getIdCanal());

            if (ec != null) {
                ec.sendHTML(current.getEmailCliente().getEmailCliente(), newAsunto,
                        texto, null);
                return texto;
            }
        }
        return null;
    }

    public static String emailClientCustomerSurvey(Caso current) throws MailNotConfiguredException, EmailException, NoOutChannelException {
        //TODO: use configure texts.
        if (current != null && current.getEmailCliente() != null && current.getEmailCliente().getEmailCliente() != null) {
            String asunto = ApplicationConfig.getCustomerSurveySubjectText(); //may contain place holders
            String newAsunto = ManagerCasos.formatIdCaso(current.getIdCaso()) + " " + ClippingsPlaceHolders.buildFinalText(asunto, current);
            String texto = ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getCustomerSurveyBodyText(), current);//may contain place holders 

            Canal canal = chooseDefaultCanalToSendMail(current);

            EmailClient ec = MailClientFactory.getInstance(canal.getIdCanal());

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
            String mensaje_ = sb.append("Estimad@ ").append(usuario.getCapitalName()).append(",").append("<br/>")
                    .append("Se ha solicido reestablecer su contraseña de acceso a GoDesk, Su contraseña temporal es:")
                    .append(newPassword)
                    .append("<br/>Se recomienda cambiar esta contraseña lo antes posible.").toString();
            NoReplySystemMailSender.sendHTML(usuario.getEmail(), subject_, mensaje_, null);
            Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "sendEmailRecoverPassword succeeded:{0}", usuario.getEmail());
        } catch (EmailException ex) {
            Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "sendEmailRecoverPassword", ex);
        }
    }

    public static void notifyClientCasoReceived(Caso caso) throws NoOutChannelException, SchedulerException {
        //new: permit to have a global response in the config
        //new: if area is not enabled, send a receipt confirmation to client from same channel this should be the default.
        //TODO: record this notification as an activity (Nota) in the case.

        if (ApplicationConfig.isSendNotificationToClientOnNewTicket()) {

            if (caso != null && caso.getEmailCliente() != null) {

                String mensaje_ = "";
                String subject_ = "";

                if (caso.getIdArea() != null) {
                    if (caso.getIdArea().getEmailAcusederecibo()) {
                        if (!StringUtils.isEmpty(caso.getIdArea().getTextoRespAutomatica())
                                && !StringUtils.isEmpty(caso.getIdArea().getSubjectRespAutomatica())) {
                            //use area config texts
                            mensaje_ = (ClippingsPlaceHolders.buildFinalText(caso.getIdArea().getTextoRespAutomatica(), caso));
                            subject_ = (ClippingsPlaceHolders.buildFinalText(caso.getIdArea().getSubjectRespAutomatica(), caso));
                        } else {
                            //no tiene textos, use global config 
                            mensaje_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientBodyNewTicketText(), caso));
                            subject_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientSubjectNewTicketText(), caso));
                        }
                    } else {
                        //area manda! si dice que no enviar entonces no enviar acuse
                        Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "area {0} manda! si dice no enviar acuse entonces no enviar acuse", caso.getIdArea() + ":" + caso.getIdArea().getEmailAcusederecibo());
                        return;
                    }
                } else {
                    //no tiene area, use global config 
                    mensaje_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientBodyNewTicketText(), caso));
                    subject_ = (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientSubjectNewTicketText(), caso));
                }

                final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
                final String mensaje = mensaje_;

                Canal canal = chooseDefaultCanalToSendMail(caso);

                HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                        caso.getEmailCliente().getEmailCliente(),
                        subject);

            }

        }

    }

    public static void notifyClientConfirmedEvent(Caso caso) throws NoOutChannelException, SchedulerException {
        //TODO: USE THIS AS EVENT CONFIRMATION NOT CASO CREATION. SOON

        if (ApplicationConfig.isSendNotificationOnSubscribedToEvent()) {

            if (caso != null && caso.getEmailCliente() != null) {

                final String mensaje = (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientBodySubscribedToEventText(), caso));
                final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientSubjectSubscribedToEventText(), caso));

                Canal canal = chooseDefaultCanalToSendMail(caso);

                HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                        caso.getEmailCliente().getEmailCliente(),
                        subject);

            }

        }

    }

    /**
     *
     * Use Same for all (config)
     *
     * @param grupo
     * @param caso
     */
    public static void notifyGroupCasoReceived(final Grupo grupo, Caso caso) {
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
                            HelpDeskScheluder.scheduleNotifyAgentsCasoReceived(canal.getIdCanal(), mensajeFinal, to.toString(), subject, caso.getIdCaso());
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
