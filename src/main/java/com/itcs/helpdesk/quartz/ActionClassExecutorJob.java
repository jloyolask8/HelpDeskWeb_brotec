/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.util.ManagerCasos;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
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
public class ActionClassExecutorJob extends AbstractGoDeskJob implements Job {

    public static final String ACTION_CLASSNAME = "actionClassName";
    public static final String ACTION_PARAMS = "actionParams";
    public static final String ID_CASO = "idCaso";
    public static final String ID_TIME = "idTime";
    /**
     * {0} = schema {1} = caso# {2} = actionClassName
     */
    public static final String JOB_ID = "ActionClassExecutor_%s_%s_%s";

    public static String formatJobId(String idCaso, String actionClassName, String idTime) {
        return String.format(JOB_ID, new Object[]{idCaso, actionClassName, idTime});
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
//        throw new UnsupportedOperationException("Not supported yet."); 
        try {
            JobDataMap map = context.getMergedJobDataMap();//.getJobDetail().getJobDataMap();
            if (map != null) {
                String idCaso = (String) map.get(ID_CASO);
                String params = (String) map.get(ACTION_PARAMS);
                String idTime = (String) map.get(ID_TIME);
                String actionClassName = (String) map.get(ACTION_CLASSNAME);
                if (!StringUtils.isEmpty(actionClassName) && !StringUtils.isEmpty(idCaso)) {
                    EntityManager em = createEntityManager();
                    try {
                        Caso caso = em.find(Caso.class, Long.valueOf(idCaso));
//                        final Action action = (Action) Class.forName(actionClassName.trim()).newInstance();
                        Constructor actionConstructor = Class.forName(actionClassName.trim()).getConstructor(JPAServiceFacade.class);
                        final Action action = (Action) actionConstructor.newInstance(getJpaController());
                        //Action being scheduled must not Receive any parametros!!! ???
                        action.setConfig(params);
                        action.execute(caso);
                        unschedule(formatJobId(idCaso, actionClassName, idTime));
                    } catch (Exception ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ActionClassExecutorJob: Error trying to execute Action class::" + actionClassName, ex);
                        throw new JobExecutionException(ex);
                    } finally {
                        em.close();
                    }
                } else {
                    throw new JobExecutionException("los parametros proporcionados al Job ActionClassExecutorJob(schema, idCaso, actionClassName) son illegales!");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "execute", ex);
            throw new JobExecutionException(ex);
        }
    }

    public static void unschedule(String formatJobId) throws SchedulerException {
//        final String formatJobId = formatJobId(schema, idCaso, idalerta);
        final JobKey jobKey = JobKey.jobKey(formatJobId, HelpDeskScheluder.GRUPO_CASOS);
        HelpDeskScheluder.unschedule(jobKey);
    }

}
