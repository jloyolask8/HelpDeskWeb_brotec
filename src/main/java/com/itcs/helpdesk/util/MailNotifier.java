package com.itcs.helpdesk.util;

import com.itcs.commons.email.EmailClient;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.quartz.AbstractGoDeskJob;
import com.itcs.helpdesk.quartz.CaseResponseByMailJob;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.quartz.TicketNotifyMailToGroup;
import com.itcs.helpdesk.util.MailClientFactory.MailNotConfiguredException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

/**
 *
 * @author Jonathan
 */
public class MailNotifier {

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

    public static void notifyCasoAssigned(Caso caso, String motivo) throws MailNotConfiguredException, EmailException {
        if (caso != null) {
            String asunto = ApplicationConfig.getNotificationSubjectText(); //may contain place holders
            String subject_ = ClippingsPlaceHolders.buildFinalText(asunto, caso);

            String texto = ApplicationConfig.getNotificationBodyText();//may contain place holders 
            texto = texto + "<b>Motivo:<b/> " + (motivo != null ? motivo : "Pronta atención del caso");
            String mensaje_ = ClippingsPlaceHolders.buildFinalText(texto, caso);

            final String subject = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + subject_;
            final String mensaje = mensaje_;

//            if (caso.getIdCanal() != null && caso.getIdCanal().getEnabled() != null
//                    && caso.getIdCanal().getEnabled()) {
//
//                if (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null) {
//                    MailClientFactory.getInstance(caso.getIdArea().getIdCanal().getIdCanal())
//                            .sendHTML(caso.getOwner().getEmail(), subject,
//                                    mensaje, null);
//                } else {
//                    //no default??? TODO
////                    MailClientFactory.getInstance(EnumAreas.DEFAULT_AREA.getArea().getIdArea())
////                            .sendHTML(current.getOwner().getEmail(), newAsunto,
////                                    newTexto, null);
//                    throw new EmailException("No se puede enviar el correo de asignación del caso #" + caso.getIdCaso() + ". El caso aún no tiene Area asociada.");
//                }
//
//            } else {
//                throw new EmailException("No se puede enviar el correo. El canal #" + caso.getIdCanal().getIdCanal() + " asociado al caso #" + caso.toString() + " tiene el envío de correos desabilitado.");
//            }
            //choose canal, prioritize the area's default canal
            Canal canal = (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null)
                    ? caso.getIdArea().getIdCanal() : caso.getIdCanal();

            if (canal != null && canal.getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal())
                    && !StringUtils.isEmpty(canal.getIdCanal())) {
                try {
                    HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                            caso.getOwner().getEmail(),
                            subject);
                } catch (SchedulerException ex) {
                    Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "error at scheduleSendMailNow", ex);
                    MailClientFactory.getInstance(canal.getIdCanal())
                            .sendHTML(caso.getOwner().getEmail(), subject,
                                    mensaje, null);
                }
            } else {
                throw new EmailException("No se puede enviar el correo de asignación del caso " + caso.toString() + ".Error: El area no tiene canal tipo email, el caso no tiene Area ni Canal o el canal no es del tipo email.");

            }

        }

    }

    public static String emailClientCasoUpdatedByAgent(Caso current) throws MailNotConfiguredException, EmailException {
        //TODO: use configure texts.
        if (current != null && current.getEmailCliente() != null) {
            String asunto = ApplicationConfig.getNotificationClientSubjectText(); //may contain place holders
            String newAsunto = ManagerCasos.formatIdCaso(current.getIdCaso()) + " " + ClippingsPlaceHolders.buildFinalText(asunto, current);
            String texto = ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientBodyText(), current);//may contain place holders 

            EmailClient ec = MailClientFactory.getInstance(current.getIdArea().getIdCanal().getIdCanal());
            if (ec != null) {
                ec.sendHTML(current.getEmailCliente().getEmailCliente(), newAsunto,
                        texto, null);
                return texto;
            }
        }
        return null;
    }

    public static void notifyClientCasoReceived(Caso caso) throws EmailException, SchedulerException {
        //new: permit to have a global response in the config
        //new: if area is not enabled, send a receipt confirmation to client from same channel this should be the default.
        //TODO: record this notification as an activity (Nota) in the case.

        if (ApplicationConfig.isSendNotificationToClientOnNewTicket()) {

            if (caso != null && caso.getEmailCliente() != null) {
                try {
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

                    //choose canal, prioritize the project's default canal
                    Canal canal = (caso.getIdProducto() != null && caso.getIdProducto().getIdOutCanal() != null)
                            ? caso.getIdProducto().getIdOutCanal() : null;

                    //choose canal, prioritize the area's default canal
                    if (canal == null) {
                        canal = (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null)
                                ? caso.getIdArea().getIdCanal() : caso.getIdCanal();
                    }

                    if (canal != null && canal.getIdTipoCanal() != null && canal.getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal())
                            && !StringUtils.isEmpty(canal.getIdCanal())) {
                        HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                                caso.getEmailCliente().getEmailCliente(),
                                subject);
                    } else {
                        throw new EmailException("No se puede enviar el correo de recepcion de caso al cliente " + caso.toString() + ".Error: El area no tiene canal tipo email, el caso no tiene Area ni Canal o el canal no es del tipo email.");

                    }
                } catch (EmailException ex) {
                    throw ex;
                }
            }

        }

    }

    public static void notifyClientConfirmedEvent(Caso caso) throws EmailException, SchedulerException {
        //TODO: USE THIS AS EVENT CONFIRMATION NOT CASO CREATION. SOON

        if (ApplicationConfig.isSendNotificationOnSubscribedToEvent()) {

            if (caso != null && caso.getEmailCliente() != null) {
                try {

                    final String mensaje = ManagerCasos.formatIdCaso(caso.getIdCaso()) + " " + (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientBodySubscribedToEventText(), caso));
                    final String subject = (ClippingsPlaceHolders.buildFinalText(ApplicationConfig.getNotificationClientSubjectSubscribedToEventText(), caso));

                    //choose canal, prioritize the project's default canal
                    Canal canal = (caso.getIdProducto() != null && caso.getIdProducto().getIdOutCanal() != null)
                            ? caso.getIdProducto().getIdOutCanal() : null;

                    //choose canal, prioritize the area's default canal
                    if (canal == null) {
                        canal = (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null)
                                ? caso.getIdArea().getIdCanal() : caso.getIdCanal();
                    }

                    if (canal != null && canal.getIdTipoCanal() != null && canal.getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal())
                            && !StringUtils.isEmpty(canal.getIdCanal())) {
                        HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), mensaje,
                                caso.getEmailCliente().getEmailCliente(),
                                subject);
                    } else {
                        throw new EmailException("No se puede enviar el correo de subpscripcion al evento " + caso.toString() + ".Error: El area no tiene canal tipo email, el caso no tiene Area ni Canal o el canal no es del tipo email.");

                    }
                } catch (EmailException ex) {
                    throw ex;
                }
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
                final String subject = "HELPDESK " + (caso.getIdCanal() != null ? caso.getIdCanal().getNombre() : "")
                        + " " + (caso.getTipoCaso() != null ? caso.getTipoCaso().getNombre() : EnumTipoCaso.CONTACTO.getTipoCaso().getNombre())
                        + " #" + caso.getIdCaso();//ApplicationConfig.getNotificationSubjectText(); //may contain place holders

                if (grupo.getUsuarioList() != null && !grupo.getUsuarioList().isEmpty()) {

                    StringBuilder to = new StringBuilder();
                    boolean first = true;
                    for (Usuario usuario : grupo.getUsuarioList()) {
                        if (first) {
                            to.append(usuario.getEmail());
                            first = false;
                        } else {
                            to.append(",").append(usuario.getEmail());
                        }

                    }

                    String mensaje = "Estimado Agente,<br/><br/>"
                            + "Le notificamos que existe un caso en su grupo (" + grupo.getNombre() + ") para su pronta atención. "
                            + "Favor contactar al cliente lo antes posible.<br/>";

                    final String mensajeFinal = mensaje + CasoExporter.exportToHtmlText(caso);
                    final String idCanal = grupo.getIdArea() != null ? grupo.getIdArea().getIdCanal().getIdCanal() : null;

                    try {
                        scheduleNotifyAgentsCasoReceived(idCanal, mensajeFinal, to.toString(), subject, caso.getIdCaso());
                    } catch (Exception ex) {
                        //It may fail right away, we still continue
                        Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "notifyAgentsCasoReceived", ex);
            }
        }
    }

//    /**
//     * sendMailToAgents.
//     * La idea es que en en caso de que falle el envio del correo a los agentes, esto se agende para reintento 10 minutos mas tarde.
//     * @param idArea
//     * @param mensajeFinal
//     * @param emails
//     * @param subject
//     * @param idCaso
//     * @throws com.itcs.helpdesk.util.MailClientFactory.MailNotConfiguredException
//     * @throws SchedulerException 
//     */
//    public static void sendMailToAgents(final String idArea, final String mensajeFinal,
//            final String emails[], final String subject, final long idCaso) throws MailNotConfiguredException, SchedulerException
//    {
//        try
//        {
//            MailClientFactory.getInstance(idArea).sendHTML(emails, subject, mensajeFinal, null);
//        }
//        catch (EmailException ex)
//        {
//            scheduleNotifyAgentsCasoReceived(new UnimailTask()
//            {
//                @Override
//                public void execute()
//                {
//                    try
//                    {
//                        sendMailToAgents(idArea, mensajeFinal, emails, subject, idCaso);
//                    }
//                    catch (MailNotConfiguredException ex)
//                    {
//                        Logger.getLogger(AutomaticMailExecutor.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    catch (SchedulerException ex)
//                    {
//                        Logger.getLogger(AutomaticMailExecutor.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }, idCaso);
//        }
//    }
    public static void scheduleNotifyAgentsCasoReceived(final String idCanal, final String mensajeFinal,
            final String to, final String subject, final Long idCaso) throws SchedulerException {

        System.out.println("scheduleNotifyAgentsCasoReceived()");
        final String valueOfIdCaso = String.valueOf(idCaso);
        final String jobId = TicketNotifyMailToGroup.formatJobId(idCanal, valueOfIdCaso, to);
        final JobKey jobKey = JobKey.jobKey(jobId, HelpDeskScheluder.GRUPO_CORREO);
        HelpDeskScheluder.unschedule(jobKey);

        JobDetail job = JobBuilder.newJob(TicketNotifyMailToGroup.class).withIdentity(jobId, HelpDeskScheluder.GRUPO_CORREO).build();

        job.getJobDataMap().put(AbstractGoDeskJob.ID_CANAL, idCanal);
        job.getJobDataMap().put(TicketNotifyMailToGroup.ID_CASO, valueOfIdCaso);
        job.getJobDataMap().put(TicketNotifyMailToGroup.EMAILS_TO, to);
        job.getJobDataMap().put(TicketNotifyMailToGroup.EMAIL_SUBJECT, subject);
        job.getJobDataMap().put(TicketNotifyMailToGroup.EMAIL_TEXT, mensajeFinal);

        HelpDeskScheluder.scheduleToRunNowWithInterval(job, 600);
    }

    public static synchronized void scheduleSendMailNota(final String idCanal, final String mensajeFinal,
            final String to, final String subject, final Long idCaso, final Integer idNota, final String attachIds) throws SchedulerException {

        System.out.println("scheduleSendMail()");
        final String valueOfIdCaso = String.valueOf(idCaso);
        final String valueOfIdNota = String.valueOf(idNota);
        final String jobId = CaseResponseByMailJob.formatJobId(idCanal, valueOfIdCaso, to);
        final JobKey jobKey = JobKey.jobKey(jobId, HelpDeskScheluder.GRUPO_CORREO);
        HelpDeskScheluder.unschedule(jobKey);

        JobDetail job = JobBuilder.newJob(CaseResponseByMailJob.class).withIdentity(jobId, HelpDeskScheluder.GRUPO_CORREO).build();

        job.getJobDataMap().put(AbstractGoDeskJob.ID_CANAL, idCanal);
        job.getJobDataMap().put(CaseResponseByMailJob.ID_CASO, valueOfIdCaso);
        job.getJobDataMap().put(CaseResponseByMailJob.ID_NOTA, valueOfIdNota);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAILS_TO, to);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAIL_SUBJECT, subject);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAIL_TEXT, mensajeFinal);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAIL_ATTACHMENTS, attachIds);

        HelpDeskScheluder.scheduleToRunNowWithInterval(job, 600);
    }

//    public static void notifyAgentsCasoReceived(Grupo grupo, Caso caso) {
//        Logger.getLogger(MailNotifier.class.getName()).log(Level.INFO, "notifyAgentsCasoReceived:{0}", grupo);
//        //TODO: use configure texts.
//        if (caso != null) {
//            try {
//                final String subject = "HELPDESK " + (caso.getIdCanal() != null ? caso.getIdCanal().getNombre() : "")
//                        + " " + (caso.getTipoCaso() != null ? caso.getTipoCaso().getNombre() : EnumTipoCaso.CONTACTO.getTipoCaso().getNombre())
//                        + " #" + caso.getIdCaso();//ApplicationConfig.getNotificationSubjectText(); //may contain place holders
////                final String subject = "Ha llegado un nuevo caso";//TODO: configurable!
//
//                if (grupo.getUsuarioList() != null && !grupo.getUsuarioList().isEmpty()) {
//                    for (Usuario usuario : grupo.getUsuarioList()) {
//
//                        String mensaje = "Estimado Agente,<br/><br/>"
//                                + "Le notificamos que existe un caso en su grupo para su pronta atención. Favor contactar al cliente lo antes posible.<br/>";
//
//                        mensaje = mensaje + CasoExporter.exportToHtmlText(caso);
////                        final String mensaje = "Hola " + usuario.getNombres() + ",<br/><br/>Ha llegado un nuevo caso a su Grupo:" + grupo.getNombre()
////                                + ".<br/>Saludos,<br/>" + ApplicationConfig.getHelpdeskTitle();
//                        MailClientFactory.getInstance(grupo.getIdArea().getIdArea())
//                                .sendHTML(usuario.getEmail(), subject, mensaje, null);
//                    }
//                }
//            } catch (Exception ex) {
//                Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, "notifyAgentsCasoReceived", ex);
//            }
//        }
//    }
}
