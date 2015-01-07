/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.itcs.commons.email.EnumEmailSettingKeys;
import com.itcs.helpdesk.persistence.entities.AppSetting;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.AutomaticOpsExecutor;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import com.itcs.helpdesk.util.RulesEngine;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import org.quartz.SchedulerException;

/**
 *
 * @author jorge
 */
//@WebListener
public class AppStarter implements ServletContextListener {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit
    private EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            //DOMConfigurator.configureAndWatch(filename, LOG_WATCH_PERIOD);
            inicializar();
        } catch (Exception ex) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            Log.createLogger(this.getClass().getName()).logInfo("*** Stopping GoDesk-Quartz Scheluder ***");
            HelpDeskScheluder.stop();
        } catch (SchedulerException ex) {
            Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void inicializar() throws SchedulerException {
        Log.createLogger(this.getClass().getName()).logInfo("Initializing GoDesk Application... ");

        AutomaticOpsExecutor autoOpsExec = new AutomaticOpsExecutor(utx, emf);
        autoOpsExec.verificaDatosBase();

        //1. Load Settings FROM APP_SETTING Table
        JPAServiceFacade appSettingJpaController = new JPAServiceFacade(utx, emf);
        List<AppSetting> appSettings = (List<AppSetting>) appSettingJpaController.findAll(AppSetting.class);
        Properties props = new Properties();
        for (AppSetting appSetting : appSettings) {
            if (appSetting != null) {
                if (appSetting.getSettingKey() != null) {
                    if (appSetting.getSettingValue() != null) {
                        props.put(appSetting.getSettingKey(), appSetting.getSettingValue());
                    } else {
                        props.put(appSetting.getSettingKey(), "");
                    }
                }
            }
        }

        ApplicationConfig.getInstance().loadConfiguration(props);

        //done
        //2. Load Areas, and configuration of each Area. Each Area will have its own email configuration to send and read emails.
        JPAServiceFacade canalJpaController = new JPAServiceFacade(utx, emf);
        List<Canal> canales = canalJpaController.findCanalTipoEmail();
//        AreaJpaController areaJpaController = new AreaJpaController(utx, emf);
//        List<Area> areas = areaJpaController.findAreaEntities();
        if (canales != null && !canales.isEmpty()) {
            for (Canal canal : canales) {
                if (canal.getEnabled() != null && canal.getEnabled()) {
                    try {
                        //Email Enabled is false by default, so if its not configured yet, we schedule nothing, this means when we change the config to true, we must re-schedule.
//                            AutomaticMailScheduler mailExec = new AutomaticMailScheduler(a, new JPAServiceFacade(utx, emf, schema));
//                            mailExec.agendarRevisarCorreo();
                        MailClientFactory.createInstance(canal);
                        String freqStr = canal.getSetting(EnumEmailSettingKeys.CHECK_FREQUENCY.getKey());
                        int freq = HelpDeskScheluder.DEFAULT_CHECK_EMAIL_INTERVAL;
                        try {
                            if (freqStr != null) {
                                freq = Integer.parseInt(freqStr);
                            }
                        } catch (NumberFormatException ex) {/*probably a weird value*/

                        }

                        HelpDeskScheluder.scheduleRevisarCorreo(canal.getIdCanal(), freq);//5 minutes fixed

                    } catch (SchedulerException ex) {
                        Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "No se pudo inicializar La revision del canal " + canal.getIdCanal(), ex);
                    } catch (IOException ex) {
                        Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "IOException " + canal.toString(), ex);
                    }
                }
            }
        }
        //Done!

//        autoOpsExec.agendarAlertasForAllCasos();
//        autoOpsExec.agendarAgendarAlertas();
    }

    static Connection lookupConnection() {
        String DATASOURCE_CONTEXT = "jdbc/godesk_db_ds";

        Connection result = null;
        try {
            Context initialContext = new InitialContext();
            DataSource datasource = (DataSource) initialContext.lookup(DATASOURCE_CONTEXT);
            if (datasource != null) {
                result = datasource.getConnection();
                System.out.println("DataSource Connection:" + result);
            } else {
                System.out.println("Failed to lookup datasource.");
            }
        } catch (Exception ex) {
            System.out.println("Cannot get connection: " + ex);
        }
        return result;
    }
}
