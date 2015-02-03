/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class HelpDeskScheluder {

    public static final int DEFAULT_CHECK_EMAIL_INTERVAL = 300;// 5 MINUTES

    /**
     * Once you obtain a scheduler using
     * StdSchedulerFactory.getDefaultScheduler(), your application will not
     * terminate until you call scheduler.shutdown(), because there will be
     * active threads.
     *
     */
    public static final String GRUPO_CORREO = "correo";
    public static final String GRUPO_CASOS = "casos";
    public static final String GRUPO_ALERTAS = "alertas";
    public static final String TRIGGER_NAME = "trigger_";
//    private static int CORRELATIVO = 0;
    static private Scheduler scheduler = null;
    public static final int INTERVAL_10_MIN = 600;

    public static Scheduler getInstance() throws SchedulerException {
        if (scheduler == null) {
//            SchedulerFactory sf = new StdSchedulerFactory();
            scheduler = StdSchedulerFactory.getDefaultScheduler();// sf.getScheduler();
            scheduler.start();

        }
        return scheduler;
    }

    public static synchronized void stop() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown(true);
        }
    }

    /**
     * Delete the identified Job from the Scheduler - and any associated
     * Triggers. removed synchronized
     *
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public static boolean unschedule(final JobKey jobKey) throws SchedulerException {
        return getInstance().deleteJob(jobKey);
    }

    public static void scheduleToRunNowWithInterval(JobDetail job, Integer intervalInSeconds) throws SchedulerException {
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME + job.getKey().getName(), job.getKey().getGroup())
                .startNow().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalInSeconds)
                        .repeatForever()).build();
        getInstance().scheduleJob(job, trigger);
    }

    protected static void scheduleToRunNow(JobDetail job) throws SchedulerException {
//        JobDetail job = JobBuilder.newJob(UnimailJob.class).withIdentity(jobId, group).build();
//        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME + job.getKey().getName(), job.getKey().getGroup())
//                .startAt(startDate).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalInSeconds)
//                        .repeatForever()).build();
//        job.getJobDataMap().put(TASK_NAME, task);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_NAME + job.getKey().getName(), job.getKey().getGroup())
                .startNow().build();

        getInstance().scheduleJob(job, trigger);
    }

    public static void schedule(JobDetail job, Date startDate) throws SchedulerException {
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_NAME + job.getKey().getName(), job.getKey().getGroup())
                .startAt(startDate).build();

        getInstance().scheduleJob(job, trigger);
    }

    public static void scheduleSendMailNow(String tenant, final Long idCaso, final String idCanal, final String mensajeFinal,
            final String tos, final String subject) throws SchedulerException {
        scheduleSendMail(tenant, idCaso, idCanal, mensajeFinal, tos, subject, null);
    }

    private static String scheduleSendMail(String tenant, final Long idCaso, final String idCanal, final String mensajeFinal,
            final String to, final String subject, final Date whenToRun) throws SchedulerException {

//      System.out.println("scheduling SendMail job");
        final String jobId = SendMailJob.formatJobId(tenant, idCanal, subject, idCaso.toString(), to);
        final JobKey jobKey = JobKey.jobKey(jobId, HelpDeskScheluder.GRUPO_CORREO);
        if (getInstance().checkExists(jobKey)) {
            HelpDeskScheluder.unschedule(jobKey);
        }

        JobDetail job = JobBuilder.newJob(SendMailJob.class).withIdentity(jobId, HelpDeskScheluder.GRUPO_CORREO).build();

        job.getJobDataMap().put(AbstractGoDeskJob.ID_CANAL, idCanal);
        job.getJobDataMap().put(AbstractGoDeskJob.ID_CASO, idCaso.toString());
        job.getJobDataMap().put(SendMailJob.EMAILS_TO, to);
        job.getJobDataMap().put(SendMailJob.EMAIL_SUBJECT, subject);
        job.getJobDataMap().put(SendMailJob.EMAIL_TEXT, mensajeFinal);
        job.getJobDataMap().put(SendMailJob.TENANT_ID, tenant);

        if (whenToRun != null) {
            HelpDeskScheluder.schedule(job, whenToRun);
        } else {
            HelpDeskScheluder.scheduleToRunNowWithInterval(job, INTERVAL_10_MIN);//10 minutes
        }

        return jobId;
    }

    public static String scheduleEventReminderJob(String tenant,
            final List<Usuario> usuarios, final Long idCaso,
            final String eventId, String eventReminderId, final Date whenToRun) throws SchedulerException {

        //System.out.println("scheduling ScheduleEventReminderJob");

        String mailsTo = "";
        boolean first = true;
        for (Usuario usuario : usuarios) {
            if (first) {
                mailsTo = usuario.getEmail();
                first = false;
            } else {
                mailsTo += ("," + usuario.getEmail());
            }
        }

        final String jobId = ScheduleEventReminderJob.formatJobId(tenant, idCaso.toString(), eventId, eventReminderId);
        final JobKey jobKey = JobKey.jobKey(jobId, HelpDeskScheluder.GRUPO_CORREO);
        if (getInstance().checkExists(jobKey)) {
            HelpDeskScheluder.unschedule(jobKey);
        }

        JobDetail job = JobBuilder.newJob(ScheduleEventReminderJob.class).withIdentity(jobId, HelpDeskScheluder.GRUPO_CORREO).build();

//        job.getJobDataMap().put(AbstractGoDeskJob.ID_CANAL, idCanal);//not needed since will send the email from a non-reply email godesk session
        job.getJobDataMap().put(AbstractGoDeskJob.ID_CASO, idCaso.toString());
        job.getJobDataMap().put(ScheduleEventReminderJob.EMAILS_TO, mailsTo);
        job.getJobDataMap().put(ScheduleEventReminderJob.EVENT_ID, eventId);
        job.getJobDataMap().put(ScheduleEventReminderJob.REMINDER_ID, eventReminderId);
        job.getJobDataMap().put(AbstractGoDeskJob.TENANT_ID, tenant);

        HelpDeskScheluder.schedule(job, whenToRun);
        return jobId;
    }

    public static void scheduleRevisarCorreo(String tenant, String idCanal, Integer intervalInSeconds) throws SchedulerException {

        final String downloadEmailJobId = DownloadEmailJob.formatJobId(idCanal, tenant);
        final JobKey jobKey = JobKey.jobKey(downloadEmailJobId, HelpDeskScheluder.GRUPO_CORREO);
        if (getInstance().checkExists(jobKey)) {
            try{
                HelpDeskScheluder.unschedule(jobKey);
            }catch(SchedulerException ex){
                //System.out.println("ERROR CONTROLADO");
                ex.printStackTrace();
            }
        }

        JobDetail job = JobBuilder.newJob(DownloadEmailJob.class).withIdentity(downloadEmailJobId, HelpDeskScheluder.GRUPO_CORREO).build();

        job.getJobDataMap().put(DownloadEmailJob.ID_CANAL, idCanal);
        job.getJobDataMap().put(AbstractGoDeskJob.TENANT_ID, tenant);
        job.getJobDataMap().put(DownloadEmailJob.INTERVAL_SECONDS, intervalInSeconds.toString());

        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.SECOND, intervalInSeconds);

//        HelpDeskScheluder.schedule(job, calendario.getTime(), intervalInSeconds);
        HelpDeskScheluder.schedule(job, calendario.getTime());
    }

    public static void unscheduleCambioAlertas(String tenant, Long idCaso) throws SchedulerException {
        TicketAlertStateChangeJob.unschedule(TicketAlertStateChangeJob.formatJobId(tenant, idCaso, EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta().getIdalerta()));
        TicketAlertStateChangeJob.unschedule(TicketAlertStateChangeJob.formatJobId(tenant, idCaso, EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta().getIdalerta()));
    }

    public static String scheduleActionClassExecutorJob(String tenant, final Long idCaso, final String actionClassName, final String params, final Date whenToRun) throws SchedulerException {
        final String time = String.valueOf(whenToRun.getTime());
        final String jobId = ActionClassExecutorJob.formatJobId(tenant, idCaso.toString(), actionClassName, time);

        JobDetail job = JobBuilder.newJob(ActionClassExecutorJob.class)
                .withIdentity(jobId, HelpDeskScheluder.GRUPO_CASOS).build();
        job.getJobDataMap().put(ActionClassExecutorJob.ID_CASO, idCaso.toString());
        job.getJobDataMap().put(ActionClassExecutorJob.ACTION_CLASSNAME, actionClassName);
        job.getJobDataMap().put(ActionClassExecutorJob.ACTION_PARAMS, params);
        job.getJobDataMap().put(ActionClassExecutorJob.ID_TIME, time);
        job.getJobDataMap().put(AbstractGoDeskJob.TENANT_ID, tenant);

        HelpDeskScheluder.schedule(job, whenToRun);
        return jobId;

    }

    public static void scheduleAlertaVencido(String tenant, final Long idCaso, final Date whenToRun) throws SchedulerException {
        final String jobId = TicketAlertStateChangeJob.formatJobId(tenant, idCaso, EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta().getIdalerta());

        TicketAlertStateChangeJob.unschedule(jobId);

        JobDetail job = JobBuilder.newJob(TicketAlertStateChangeJob.class)
                .withIdentity(jobId, HelpDeskScheluder.GRUPO_CASOS).build();
        job.getJobDataMap().put(TicketAlertStateChangeJob.ID_CASO, idCaso.toString());
        job.getJobDataMap().put(AbstractGoDeskJob.TENANT_ID, tenant);
        job.getJobDataMap().put(TicketAlertStateChangeJob.ID_ESTADO_ALERTA, EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta().getIdalerta().toString());

        HelpDeskScheluder.schedule(job, whenToRun/*casoFinal.getNextResponseDue()*/);

    }

    public static void scheduleAlertaPorVencer(String tenant, final Long idCaso, final Date whenToRun) throws SchedulerException {

        unscheduleAlertasDelCaso(tenant, idCaso);

        final String jobIdPorVencer = TicketAlertStateChangeJob.formatJobId(tenant, idCaso, EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta().getIdalerta());

        JobDetail job = JobBuilder.newJob(TicketAlertStateChangeJob.class)
                .withIdentity(jobIdPorVencer, HelpDeskScheluder.GRUPO_CASOS).build();
        job.getJobDataMap().put(AbstractGoDeskJob.ID_CASO, idCaso.toString());
        job.getJobDataMap().put(AbstractGoDeskJob.TENANT_ID, tenant);
        job.getJobDataMap().put(TicketAlertStateChangeJob.ID_ESTADO_ALERTA, EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta().getIdalerta().toString());

        HelpDeskScheluder.schedule(job, whenToRun);

    }

    public static void unscheduleAlertasDelCaso(String tenant, final Long idCaso) throws SchedulerException {
        System.out.println("unscheduleAlertasDelCaso()");
        final String jobIdPorVencer = TicketAlertStateChangeJob.formatJobId(tenant, idCaso, EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta().getIdalerta());
        final String jobIdVencido = TicketAlertStateChangeJob.formatJobId(tenant, idCaso, EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta().getIdalerta());

        TicketAlertStateChangeJob.unschedule(jobIdPorVencer);
        TicketAlertStateChangeJob.unschedule(jobIdVencido);
    }

    public static void scheduleNotifyAgentsCasoReceived(String tenant, final String idCanal, final String mensajeFinal,
            final String to, final String subject, final Long idCaso) throws SchedulerException {

        //System.out.println("scheduleNotifyAgentsCasoReceived()");
        final String valueOfIdCaso = String.valueOf(idCaso);
        final String jobId = TicketNotifyMailToGroup.formatJobId(tenant, idCanal, valueOfIdCaso, to);
        final JobKey jobKey = JobKey.jobKey(jobId, HelpDeskScheluder.GRUPO_CORREO);

        if (getInstance().checkExists(jobKey)) {
            HelpDeskScheluder.unschedule(jobKey);
        }

        JobDetail job = JobBuilder.newJob(TicketNotifyMailToGroup.class).withIdentity(jobId, HelpDeskScheluder.GRUPO_CORREO).build();

        job.getJobDataMap().put(AbstractGoDeskJob.ID_CANAL, idCanal);
        job.getJobDataMap().put(AbstractGoDeskJob.ID_CASO, valueOfIdCaso);
        job.getJobDataMap().put(TicketNotifyMailToGroup.EMAILS_TO, to);
        job.getJobDataMap().put(TicketNotifyMailToGroup.EMAIL_SUBJECT, subject);
        job.getJobDataMap().put(TicketNotifyMailToGroup.EMAIL_TEXT, mensajeFinal);
        job.getJobDataMap().put(AbstractGoDeskJob.TENANT_ID, tenant);

        HelpDeskScheluder.scheduleToRunNowWithInterval(job, 600);
    }

    public static void scheduleSendMailNota(String tenant, final String idCanal, final String mensajeFinal,
            final String to, final String subject, final Long idCaso, final Integer idNota, final String attachIds) throws SchedulerException {
        scheduleSendMailNota(tenant, idCanal, mensajeFinal, to, null, null, subject, idCaso, idNota, attachIds);
    }

    /**
     * Agendar el envio del correo de respuesta al caso cuando a partir de una
     * nota existente!
     *
     * @param tenant
     * @param idCanal
     * @param mensajeFinal
     * @param to
     * @param cc
     * @param bcc
     * @param subject
     * @param idCaso
     * @param idNota
     * @param attachIds
     * @throws SchedulerException
     */
    public static void scheduleSendMailNota(String tenant, final String idCanal, final String mensajeFinal,
            final String to, final String cc, final String bcc, final String subject, final Long idCaso, final Integer idNota, final String attachIds) throws SchedulerException {

        //System.out.println("scheduleSendMail()");
        final String valueOfIdCaso = String.valueOf(idCaso);
        final String valueOfIdNota = String.valueOf(idNota);
        final String jobId = CaseResponseByMailJob.formatJobId(tenant, idCanal, valueOfIdCaso, to);
        final JobKey jobKey = JobKey.jobKey(jobId, HelpDeskScheluder.GRUPO_CORREO);
        if (getInstance().checkExists(jobKey)) {
            HelpDeskScheluder.unschedule(jobKey);
        }

        JobDetail job = JobBuilder.newJob(CaseResponseByMailJob.class).withIdentity(jobId, HelpDeskScheluder.GRUPO_CORREO).build();

        job.getJobDataMap().put(AbstractGoDeskJob.ID_CANAL, idCanal);
        job.getJobDataMap().put(CaseResponseByMailJob.ID_CASO, valueOfIdCaso);
        job.getJobDataMap().put(CaseResponseByMailJob.ID_NOTA, valueOfIdNota);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAILS_TO, to);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAILS_CC, cc);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAILS_BCC, bcc);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAIL_SUBJECT, subject);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAIL_TEXT, mensajeFinal);
        job.getJobDataMap().put(CaseResponseByMailJob.EMAIL_ATTACHMENTS, attachIds);
        job.getJobDataMap().put(AbstractGoDeskJob.TENANT_ID, tenant);

        HelpDeskScheluder.scheduleToRunNowWithInterval(job, INTERVAL_10_MIN);
    }

    private HelpDeskScheluder() throws Exception {
        //se crea el scheduler
        //getScheduler();
    }
}
