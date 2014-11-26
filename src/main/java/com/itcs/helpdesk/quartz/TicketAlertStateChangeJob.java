/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.helpdesk.persistence.entities.AuditLog;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.TipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.jpa.exceptions.NonexistentEntityException;
import com.itcs.helpdesk.util.MailNotifier;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.commons.lang3.StringUtils;
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
//@ExecuteInJTATransaction
public class TicketAlertStateChangeJob extends AbstractGoDeskJob implements Job {

//    EventNotifier eventNotifier = lookupEventNotifierBean();
    public static final String ID_ESTADO_ALERTA = "idalerta";

    /**
     * {0} = schema {1} = caso# {2} = id alerta
     */
    private static final String JOB_ID = "TicketAlertStateChange_%s_%s";

    public static String formatJobId(Long idCaso, Integer idalerta) {
        return String.format(JOB_ID, new Object[]{idCaso.toString(), idalerta.toString()});
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
//        throw new UnsupportedOperationException("Not supported yet."); 

        try {
            JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
            if (map != null) {
                String idCaso = (String) map.get(ID_CASO);
                String idAlerta = (String) map.get(ID_ESTADO_ALERTA);
                if (!StringUtils.isEmpty(idCaso) && !StringUtils.isEmpty(idAlerta)) {
                    EntityManagerFactory emf = createEntityManagerFactory();
                    UserTransaction utx = UserTransactionHelper.lookupUserTransaction();
//                    JPAServiceFacade jpaController = new JPAServiceFacade(utx, emf);
                    EntityManager em = null;

                    final Long valueOfIdCaso = Long.valueOf(idCaso);
                    final Integer valueOfIdAlerta = Integer.valueOf(idAlerta);

                    try {

                        if (valueOfIdCaso != null && valueOfIdAlerta != null) {
                            utx.begin();
                            em = emf.createEntityManager();
                            Caso caso = em.find(Caso.class, valueOfIdCaso);
                            if (caso != null) {

                                final TipoAlerta tipoAlerta = em.find(TipoAlerta.class, valueOfIdAlerta);
                                if (tipoAlerta != null) {

                                    caso.setEstadoAlerta(tipoAlerta);
//                                    caso.setFechaModif(new Date());//Do not change this!!

                                    em.merge(caso);

                                    //Audit Log
                                    String idUser = context.getScheduler().getSchedulerName();
                                    AuditLog audit = new AuditLog();
                                    audit.setIdUser(idUser);
                                    audit.setFecha(Calendar.getInstance().getTime());
                                    audit.setTabla(Caso.class.getSimpleName());
                                    audit.setNewValue("Estado de alerta del caso cambia a: " + caso.getEstadoAlerta().getNombre());
                                    audit.setIdCaso(caso.getIdCaso());
                                    if (caso.getOwner() == null) {
                                        audit.setOwner(null);
                                    } else {
                                        audit.setOwner(caso.getOwner().getIdUsuario());
                                    }

                                    em.persist(audit);
                                    //End Audit Log                                    

                                    if (valueOfIdAlerta.equals(EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta().getIdalerta())) {
                                        HelpDeskScheluder.scheduleAlertaVencido(valueOfIdCaso, caso.getNextResponseDue());
                                    }
                                    
                                    if(caso.getOwner() != null){
                                        if(caso.getOwner().getEmailNotificationsEnabled() ){
                                            if(caso.getOwner().getNotifyWhenTicketAlert()){
                                                MailNotifier.notifyCasoOwnerAlertChanged(caso);
                                            }
                                        }
                                    }

//                                    eventNotifier.fire(new NotificationData("Estado de Alerta Caso", 
//                                            "Estado de alerta del caso " + caso.getIdCaso() + 
//                                                    " pasa a " + caso.getEstadoAlerta().getNombre(), audit.getOwner()));
                                    unschedule(formatJobId(valueOfIdCaso, valueOfIdAlerta));
                                    utx.commit();

                                } else {
                                    throw new NonexistentEntityException("El tipo de Alerta " + idAlerta + " no existe!!!");
                                }

                            } else {
                                throw new NonexistentEntityException("El caso " + idCaso + " ya no existe!!!");
                            }
                        }

                    } catch (NonexistentEntityException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "NonexistentEntityException at TicketAlertStateChangeJob.execute:{0}", ex.getMessage());
                        unschedule(formatJobId(valueOfIdCaso, valueOfIdAlerta));

                    } catch (Exception ex) {
                        if (utx != null) {
                            try {
                                utx.rollback();
                            } catch (IllegalStateException ex1) {
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "rollback error: IllegalStateException", ex1);
                            } catch (SecurityException ex1) {
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "rollback error: SecurityException", ex1);
                            } catch (SystemException ex1) {
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "rollback error: SystemException", ex1);
                            }
                        }
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "TicketAlertStateChangeJob.execute exception", ex);
                    } finally {
                        UserTransactionHelper.returnUserTransaction(utx);
                        if (em != null) {
                            em.close();
                        }
                        if (emf != null) {
                            emf.close();
                        }
                    }

                } else {
                    throw new IllegalStateException("los parametros proporcionados al Job CambiarEstadoAlertaCasoJob(schema, idCaso, idalerta) son illegales!");
                }
            }
        } catch (NumberFormatException nfe) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "NumberFormatException at TicketAlertStateChangeJob.execute", nfe);
        } catch (IllegalStateException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "TicketAlertStateChangeJob.execute", ex);
        } catch (SchedulerException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "TicketAlertStateChangeJob.execute", ex);
        }
    }

    public static void unschedule(String formatJobId) throws SchedulerException {
//        final String formatJobId = formatJobId(schema, idCaso, idalerta);
        final JobKey jobKey = JobKey.jobKey(formatJobId, HelpDeskScheluder.GRUPO_CASOS);
        HelpDeskScheluder.unschedule(jobKey);
    }

//    private EventNotifier lookupEventNotifierBean() {
//        try {
//            Context c = new InitialContext();
////            INFO: EJB5181:Portable JNDI names for EJB EventNotifier: [java:global/HelpDeskWeb_brotec/EventNotifier!com.itcs.helpdesk.ejb.notifications.EventNotifier, java:global/HelpDeskWeb_brotec/EventNotifier]
//            return (EventNotifier) c.lookup("java:global/HelpDeskWeb_brotec/EventNotifier");
//        } catch (NamingException ne) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
//            throw new RuntimeException(ne);
//        }
//    }
}
