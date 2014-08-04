/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.itcs.helpdesk.persistence.entities.AppSetting;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.jpa.AppSettingJpaController;
import com.itcs.helpdesk.persistence.jpa.CanalJpaController;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.AutomaticOpsExecutor;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.UserTransaction;
import org.quartz.SchedulerException;

/**
 *
 * @author jorge
 */
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
            HelpDeskScheluder.stop();
            Log.createLogger(this.getClass().getName()).logInfo("stopped HelpDesk Scheluder");
        } catch (SchedulerException ex) {
            Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void inicializar() throws SchedulerException {
        Log.createLogger(this.getClass().getName()).logInfo("Inicializando");

        AutomaticOpsExecutor autoOpsExec = new AutomaticOpsExecutor(utx, emf);
        autoOpsExec.verificaDatosBase();

        //1. Load Settings FROM APP_SETTING Table
        AppSettingJpaController appSettingJpaController = new AppSettingJpaController(utx, emf);
        List<AppSetting> appSettings = appSettingJpaController.findAppSettingEntities();
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
        CanalJpaController canalJpaController = new CanalJpaController(utx, emf);
        List<Canal> canales = canalJpaController.findCanalTipoEmail();
//        AreaJpaController areaJpaController = new AreaJpaController(utx, emf);
//        List<Area> areas = areaJpaController.findAreaEntities();
        if (canales!= null && !canales.isEmpty()) {
            for (Canal canal : canales) {
                if (canal.getEnabled() != null && canal.getEnabled()) {
                    try {
                        //Email Enabled is false by default, so if its not configured yet, we schedule nothing, this means when we change the config to true, we must re-schedule.
//                            AutomaticMailScheduler mailExec = new AutomaticMailScheduler(a, new JPAServiceFacade(utx, emf, schema));
//                            mailExec.agendarRevisarCorreo();
                        MailClientFactory.createInstance(canal);
                        HelpDeskScheluder.scheduleRevisarCorreo(canal.getIdCanal(), 300);//5 minutes fixed

                    } catch (SchedulerException ex) {
                        Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "No se pudo inicializar La revision del canal " + canal.getIdCanal(), ex);
                    } catch (IOException ex) {
                        Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "IOException " + canal.toString(), ex);
                    }
                }
            }
        }
        //Done!

        autoOpsExec.agendarAlertasForAllCasos();

//        autoOpsExec.agendarAgendarAlertas();
    }
}
