/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

/**
 *
 * @author jonathan
 */
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.Cliente_;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.jpa.EasyCriteriaQuery;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.mail.internet.MimeUtility;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

@ManagedBean(name = "scheduler")
@SessionScoped
public class SchedulerBean extends AbstractManagedBean<Object> implements Serializable {

    private static final long serialVersionUID = 1L;
    private transient Scheduler scheduler;
    private transient List<QuartzJob> quartzJobList = new ArrayList<QuartzJob>();

    public SchedulerBean() throws SchedulerException {
        super(Object.class);
    }

    //trigger a job
    public void fireNow(String jobName, String jobGroup)
            throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);

    }

    public void agendarAlertasForAllCasos() {
        //System.out.println("agendarAlertasForAllCasos");

        List<Caso> casos_pendiente = getJpaController().getCasoFindByEstadoAndAlerta(EnumEstadoCaso.ABIERTO.getEstado(), EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
//            //System.out.println("encontrados "+casos.size()+" casos "+EnumTipoAlerta.TIPO_ALERTA_PENDIENTE+" que se debe agendar cambio de alerta");

        for (Caso caso : casos_pendiente) {
            if (caso.getNextResponseDue() != null) {
                try {
                    if ((caso.getEstadoAlerta().getIdalerta().equals(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta().getIdalerta()))
                            && (caso.getNextResponseDue().after(Calendar.getInstance().getTime()))) {
                        HelpDeskScheluder.scheduleAlertaPorVencer(getUserSessionBean().getTenantId(),caso.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(caso));
                    }

                        //Siempre se debe agendar el cambio a caso vencido para cuando se acabe el plazo para responder el caso
                    //                    HelpDeskScheluder.scheduleAlertaVencido(caso.getIdCaso(), caso.getNextResponseDue());
                    //                    agendarCambioEstadoAlerta(schema, caso);
                } catch (SchedulerException ex) {
                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Caso " + caso.getIdCaso() + " agendarAlertasForAllCasos -> pendientes", ex);
                } catch (NullPointerException ex) {
                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Caso " + caso.getIdCaso() + " error grave: ", ex);
                }
            }
        }

        List<Caso> casos_por_vencer = getJpaController().getCasoFindByEstadoAndAlerta(EnumEstadoCaso.ABIERTO.getEstado(),
                EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta());
//            //System.out.println("encontrados "+casos.size()+" casos "+EnumTipoAlerta.TIPO_ALERTA_POR_VENCER+" que se debe agendar cambio de alerta");
        for (Caso caso : casos_por_vencer) {
            try {
                HelpDeskScheluder.scheduleAlertaVencido(getUserSessionBean().getTenantId(),caso.getIdCaso(), caso.getNextResponseDue());
//                    agendarCambioEstadoAlerta(schema, caso);
            } catch (SchedulerException ex) {
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "agendarAlertasForAllCasos -> por vencer", ex);
            }
        }
    }

    public void deleteJob(String jobName, String jobGroup)
            throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);

    }

    public List<QuartzJob> getQuartzJobList() {
        try {
            scheduler = HelpDeskScheluder.getInstance();
            quartzJobList = new ArrayList<QuartzJob>();

            // loop jobs by group
            for (String groupName : scheduler.getJobGroupNames()) {

                // get jobkey
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher
                        .jobGroupEquals(groupName))) {

                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();

                    // get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler
                            .getTriggersOfJob(jobKey);
                    Date nextFireTime = triggers.get(0).getNextFireTime();

                    quartzJobList.add(new QuartzJob(jobName, jobGroup, nextFireTime));

                }

            }

            return quartzJobList;
        } catch (SchedulerException ex) {
            JsfUtil.addErrorMessage(ex, ex.getMessage());
            Logger.getLogger(SchedulerBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class QuartzJob {

        private static final long serialVersionUID = 1L;
        String jobName;
        String jobGroup;
        Date nextFireTime;

        public QuartzJob(String jobName, String jobGroup, Date nextFireTime) {

            this.jobName = jobName;
            this.jobGroup = jobGroup;
            this.nextFireTime = nextFireTime;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public String getJobGroup() {
            return jobGroup;
        }

        public void setJobGroup(String jobGroup) {
            this.jobGroup = jobGroup;
        }

        public Date getNextFireTime() {
            return nextFireTime;
        }

        public void setNextFireTime(Date nextFireTime) {
            this.nextFireTime = nextFireTime;
        }
    }
    
    /**
     * remove this duplicated code
     * @param jpaController 
     */
    
    //Nada que ver con el codigo
    public void verificaDatosBase() {
        JPAServiceFacade controller = getJpaController();
        System.out.println("verificaDatosBase()...");
        fixClientesCasos(controller);
        fixNombreCliente(controller);
    }

    /**
     * remove this duplicated code
     * @param jpaController 
     */
    
    private void fixClientesCasos(JPAServiceFacade jpaController) {
        EasyCriteriaQuery<Caso> ecq = new EasyCriteriaQuery<>(getJpaController(), Caso.class);
        ecq.addEqualPredicate("idCliente", null);
        ecq.addDistinctPredicate("emailCliente", null);
        List<Caso> casosToFix = ecq.getAllResultList();

        for (Caso caso : casosToFix) {
            caso.setIdCliente(caso.getEmailCliente().getCliente());
            try {
                jpaController.merge(caso);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * remove this duplicated code
     * @param jpaController 
     */
    private void fixNombreCliente(JPAServiceFacade jpaController) {
        EasyCriteriaQuery<Cliente> ecq = new EasyCriteriaQuery<>(getJpaController(), Cliente.class);
        ecq.addLikePredicate(Cliente_.nombres.getName(), "%=?ISO-8859-1%");
        List<Cliente> clientsToFix = ecq.getAllResultList();

        for (Cliente cliente : clientsToFix) {
            try {
                String from = MimeUtility.decodeText(cliente.getNombres().replace("\"", ""));
                String[] nombres = from.split(" ");
                if (nombres.length > 0) {
                    cliente.setNombres(nombres[0]);
                }
                if (nombres.length > 2) {
                    cliente.setApellidos(nombres[1] + " " + nombres[2]);
                } else if (nombres.length > 1) {
                    cliente.setApellidos(nombres[1]);
                }
                try {
                    jpaController.merge(cliente);
                } catch (Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }




}
