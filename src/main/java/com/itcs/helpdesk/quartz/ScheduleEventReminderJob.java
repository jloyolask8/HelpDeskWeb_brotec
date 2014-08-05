/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.commons.email.impl.NoReplySystemMailSender;
import com.itcs.helpdesk.persistence.entities.Resource;
import com.itcs.helpdesk.persistence.entities.ScheduleEvent;
import com.itcs.helpdesk.persistence.entities.ScheduleEventReminder;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
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
    PrettyTime prettyTime = new PrettyTime(new Locale("es"));
//    public static final String EMAIL_SUBJECT = "subject";
//    public static final String EMAIL_TEXT = "email_text";
//    public static final String INTENT = "intent";
//    public static final int RETRY = 3;

    /**
     * {0} = idArea,
     */
    public static final String JOB_ID = "System_RemindEvent_to_%s_e%sr_%s";

    public static String formatJobId(String to, String eventId, String reminderId) {
        return String.format(JOB_ID, new Object[]{to, eventId, reminderId});
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "ScheduleEventReminderJob.execute()");

        JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
        if (map != null) {
//            String idCanal = (String) map.get(ID_CANAL);
//            String idCaso = (String) map.get(ID_CASO);
            String emails_to = (String) map.get(EMAILS_TO);
            //---
//            String subject = (String) map.get(EMAIL_SUBJECT);
//            String email_text = (String) map.get(EMAIL_TEXT);
            String eventId = (String) map.get(EVENT_ID);
            String reminderId = (String) map.get(REMINDER_ID);
            emails_to = emails_to.trim().replace(" ", "");

            final String formatJobId = formatJobId(emails_to, eventId, reminderId);
            if (!StringUtils.isEmpty(emails_to)
                    && !StringUtils.isEmpty(eventId) && !StringUtils.isEmpty(reminderId)) {

                try {
                    JPAServiceFacade jpaController = getJpaController();

                    ScheduleEvent event = jpaController.find(ScheduleEvent.class, Integer.valueOf(eventId));
                    ScheduleEventReminder eventReminder = jpaController.find(ScheduleEventReminder.class, Integer.valueOf(reminderId));

                    if (event != null && eventReminder != null) {
                        //database represents the real data, we only send reminders in case the event or reminders have not been deleted
                        System.out.println("eventReminder: " + eventReminder);
                        if (eventReminder.getReminderType().equalsIgnoreCase("EMAIL")) {

                            //SEND THE NOTIFICATION (NO-REPLY) EMAIL!
                            SimpleDateFormat sdf1 = new SimpleDateFormat("EEE dd 'de' MMM 'de' yyyy HH:mm", LOCALE_ES_CL);
                            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", LOCALE_ES_CL);

//                            Calendar startDateCal = Calendar.getInstance();
//                            startDateCal.setTime(event.getStartDate());
//                            
//                            Calendar endDateCal = Calendar.getInstance();
//                            endDateCal.setTime(event.getEndDate());
                            DateTime dtStart = new DateTime(event.getStartDate());
                            DateTime dtEnd = new DateTime(event.getEndDate());

                            String formattedDate = "fecha de evento desconocida";

                            if (event.getStartDate() != null) {
                                if (dtStart.isAfter(dtEnd) || event.getEndDate() == null) {
                                    //illegal!!
                                    formattedDate = sdf1.format(event.getStartDate());
                                } else {
                                    if (dtStart.withTimeAtStartOfDay().isEqual(dtEnd.withTimeAtStartOfDay())) {//same day?
                                        formattedDate = sdf1.format(event.getStartDate()) + " - " + sdf2.format(event.getEndDate());
                                    }
                                }
                            }

                            String subject = "Recordatorio: " + event.getTitle() + " " + formattedDate + " (" + event.getIdUsuario().getCapitalName() + ")";

                            StringBuilder bodyText = new StringBuilder();
                            bodyText.append("<table style=\"width:100%;font-family:Arial,Sans-serif;border:1px Solid #ccc;border-width:1px 2px 2px 1px;background-color:#fff\" cellpadding=\"4\" cellspacing=\"0\">")
                                    .append("	<tbody>")
                                    .append("		<tr>")
                                    .append("			<td>")
                                    .append("			<h2><strong>").append(event.getTitle()).append("</strong></h2>")
                                    .append("")
                                    .append("			<p>").append(event.getDescripcion()).append("</p>")
                                    .append("")
                                    .append("			<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" summary=\"Información del evento\">")
                                    .append("				<tbody>")
                                    .append("					<tr>")
                                    .append("						<td style=\"white-space:nowrap\">")
                                    .append("						<p style=\"color: rgb(170, 170, 170); font-style: italic;\">Cu&aacute;ndo:</p>")
                                    .append("						</td>")
                                    .append("						<td>&nbsp;").append(formattedDate).append(" (").append(prettyTime.format(event.getStartDate())).append(") ").append(LOCALE_ES_CL.getDisplayCountry()).append("</td>")
                                    .append("					</tr>");
                            if (!StringUtils.isEmpty(event.getLugar())) {
                                bodyText.append("					<tr>")
                                        .append("						<td style=\"white-space:nowrap\">")
                                        .append("						<p style=\"color: rgb(170, 170, 170); font-style: italic;\">D&oacute;nde:</p>")
                                        .append("						</td>")
                                        .append("						<td>&nbsp;").append(event.getLugar()).append("</td>")
                                        .append("					</tr>");
                            }

                            bodyText.append("					<tr>")
                                    .append("						<td style=\"white-space:nowrap\">")
                                    .append("						<p style=\"color: rgb(170, 170, 170); font-style: italic;\">Caso:</p>")
                                    .append("						</td>")
                                    .append("						<td>").append(event.getIdCaso().toString()).append("</td>")
                                    .append("					</tr>")
                                    .append("					<tr>")
                                    .append("						<td style=\"white-space:nowrap\">")
                                    .append("						<p style=\"color: rgb(170, 170, 170); font-style: italic;\">Invitados:</p>")
                                    .append("						</td>")
                                    .append("						<td>")
                                    .append("						<table cellpadding=\"0\" cellspacing=\"0\">")
                                    .append("							<tbody>");

                            for (Usuario usuario : event.getUsuariosInvitedList()) {
                                bodyText.append("<tr><td>&bull;&nbsp;").append(usuario.getCapitalName()).append("</td></tr>");
                            }

                            bodyText.append("</tbody>")
                                    .append("						</table>")
                                    .append("						</td>")
                                    .append("					</tr>");

                            if (event.getResourceList() != null && !event.getResourceList().isEmpty()) {
                                bodyText.append("					<tr>")
                                        .append("						<td style=\"white-space:nowrap\">")
                                        .append("						<p style=\"color: rgb(170, 170, 170); font-style: italic;\">Recursos:</p>")
                                        .append("						</td>")
                                        .append("						<td>")
                                        .append("						<table cellpadding=\"0\" cellspacing=\"0\">")
                                        .append("							<tbody>");

                                for (Resource resource : event.getResourceList()) {
                                    bodyText.append("<tr><td>&bull;&nbsp;").append(resource.getNombre()).append("</td></tr>");
                                }

                                bodyText.append("							</tbody>")
                                        .append("						</table>")
                                        .append("						</td>")
                                        .append("					</tr>");
                            }

                            bodyText.append("				</tbody>")
                                    .append("			</table>")
                                    .append("			</td>")
                                    .append("		</tr>")
                                    .append("		<tr>")
                                    .append("			<td style=\"background-color:#f6f6f6;color:#888;border-top:1px Solid #ccc;font-family:Arial,Sans-serif;font-size:11px\">")
                                    .append("			<p><small>Este es un recordatorio de&nbsp;<a href=\"http://www.godesk.cl\" target=\"_blank\"><em>GoDesk</em>&nbsp;Calendar</a>&nbsp;para <em>Brotec-Icafal</em>.</small></p>")
                                    .append("			<p><small>Si recibes este mensaje de correo electr&oacute;nico es debido a que est&aacute;s suscrito para recibir recordatorios del calendario del Sistema <em>GoDesk</em>.</small></p>")
                                    .append("			<p><small>Si deseas dejar de recibir estas notificaciones, accede a tu cuenta en <em>GoDesk</em> y modifica la configuraci&oacute;n de las notificaciones.</small></p>")
                                    .append("			<p><small>Esto es solo un aviso, por favor no responder este correo.</small></p>")
                                    .append("			</td>")
                                    .append("		</tr>")
                                    .append("	</tbody>")
                                    .append("</table>");

//                            String bodyText = "<table border=\"0\" cellpadding=\"8\" cellspacing=\"0\" style=\"width:100%\" summary=\"\">\n"
//                                    + "	<tbody>\n"
//                                    + "		<tr>\n"
//                                    + "			<td>\n"
//                                    + "			<p><font size=\"2\">" + event.getTitle() + "</font></p>\n"
//                                    + "\n"
//                                    + "			<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" summary=\"Información del evento\">\n"
//                                    + "				<tbody>\n"
//                                    + "					<tr>\n"
//                                    + "						<td style=\"white-space:nowrap\">\n"
//                                    + "						<p><span style=\"white-space:normal\">Cu&aacute;ndo</span></p>\n"
//                                    + "						</td>\n"
//                                    + "						<td>&nbsp;" + PrettyDate.format(event.getStartDate()) + "</td>\n"
//                                    + "					</tr>\n"
//                                    + "					<tr>\n"
//                                    + "						<td style=\"white-space:nowrap\">\n"
//                                    + "						<p><span style=\"white-space:normal\">D&oacute;nde</span></p>\n"
//                                    + "						</td>\n"
//                                    + "						<td>&nbsp;</td>\n"
//                                    + "					</tr>\n"
//                                    + "					<tr>\n"
//                                    + "						<td style=\"white-space:nowrap\">\n"
//                                    + "						<p><span style=\"white-space:normal\">Caso</span></p>\n"
//                                    + "						</td>\n"
//                                    + "						<td>" + event.getIdCaso().toString() + "</td>\n"
//                                    + "					</tr>\n"
//                                    + "					<tr>\n"
//                                    + "						<td style=\"white-space:nowrap\">\n"
//                                    + "						<p><span style=\"white-space:normal\">Invitados</span></p>\n"
//                                    + "						</td>\n"
//                                    + "						<td>\n"
//                                    + "						<table cellpadding=\"0\" cellspacing=\"0\">\n"
//                                    + "							<tbody>\n"
//                                    + "								<tr>\n";
//
//                            for (Usuario usuario : event.getUsuariosInvitedList()) {
//                                bodyText = bodyText + "<td>&bull;&nbsp;" + usuario.getCapitalName() + "</td>";
//                            }
//
//                            bodyText = bodyText + "								</tr>\n"
//                                    + "							</tbody>\n"
//                                    + "						</table>\n"
//                                    + "						</td>\n"
//                                    + "					</tr>\n"
//                                    + "				</tbody>\n"
//                                    + "			</table>\n"
//                                    + "\n"
//                                    + "			<p>&nbsp;</p>\n"
//                                    + "			</td>\n"
//                                    + "		</tr>\n"
//                                    + "		<tr>\n"
//                                    + "			<td style=\"background-color:#f6f6f6\">\n"
//                                    + "			<p>Este es un recordatorio de&nbsp;<a href=\"https://www.godesk.cl\" target=\"_blank\">GoDesk&nbsp;Calendar</a>&nbsp;para Brotec-Icafal.</p>\n"
//                                    + "\n"
//                                    + "			<p>Si recibes este mensaje de correo electr&oacute;nico en la direcci&oacute;n ${emailAgente}&nbsp;es&nbsp;porque est&aacute;s suscrito para recibir recordatorios del calendario del Sistema Godesk.</p>\n"
//                                    + "\n"
//                                    + "			<p>Si deseas dejar de recibir estas notificaciones, accede a tu cuenta en godesk&nbsp;y modifica la configuraci&oacute;n de las notificaciones.</p>\n"
//                                    + "			</td>\n"
//                                    + "		</tr>\n"
//                                    + "	</tbody>\n"
//                                    + "</table>";//TODO
                            NoReplySystemMailSender.sendHTML(emails_to.split(","), subject, bodyText.toString(), null);
                            //if sent ok, then forget about it
                            unschedule(formatJobId);
                            eventReminder.setNotifiedOk(Boolean.TRUE);
                            try {
                                jpaController.merge(eventReminder);
                            } catch (Exception ex) {
                                Logger.getLogger(ScheduleEventReminderJob.class.getName()).log(Level.SEVERE, "merge error", ex);
                            }

                            System.out.println("ScheduleEventReminderJob DONE!");

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
