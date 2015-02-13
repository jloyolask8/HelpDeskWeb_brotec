/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.itcs.commons.email.EnumEmailSettingKeys;
import com.itcs.commons.email.impl.PopImapEmailClientImpl;
import com.itcs.helpdesk.persistence.entities.AppSetting;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.CanalSetting;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.ApplicationConfigs;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    @PersistenceUnit(unitName = "helpdeskPU")
    private EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Log.createLogger(this.getClass().getName()).logInfo("*** contextInitialized - Initializing GoDesk Application... *** ");
        try {
            inicializar();
        } catch (Exception ex) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log.createLogger(this.getClass().getName()).logInfo("*** contextDestroyed - Stopping GoDesk-Quartz Scheluder ***");
        try {
            HelpDeskScheluder.stop();
            PopImapEmailClientImpl.getExecutorService().shutdown();
        } catch (SchedulerException ex) {
            Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * we need to initialize many contexts now. One Context for each tenant.
     *
     * @throws SchedulerException
     */
    private void inicializar() throws SchedulerException {

        try {
            try (Connection connection = lookupConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();

                try (ResultSet res = metaData.getSchemas()) {
                    System.out.println("*** Loading tenant configurations & Email Channels... ");
                    while (res.next()) {
                        String schema = res.getString(1);

                        if (schema != null && !schema.equalsIgnoreCase("public")
                                && !schema.equalsIgnoreCase("information_schema")
                                && !schema.equalsIgnoreCase("pg_catalog")
                                && !schema.equalsIgnoreCase("base_schema")) {//,
                            initTenant(schema);
                        } else {
                            System.out.println("schema not allowed to load:" + schema);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void initTenant(String schema) {
        //loop over schemas in db. and do this
        Logger.getLogger(AppStarter.class.getName()).log(Level.INFO, "Loading tenant {0}...", schema);

        try {

            JPAServiceFacade jpaController = new JPAServiceFacade(utx, emf, schema);

            //1. Load Settings FROM APP_SETTING Table
            List<AppSetting> appSettings = (List<AppSetting>) jpaController.findAll(AppSetting.class);
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

            ApplicationConfigs.loadTenantInstance(schema, props);

            //done
            //2. Load email Channels.
            //Each Channel will have its own email configuration to send and read emails.
            List<Canal> canales = (List<Canal>) jpaController.findAll(Canal.class);
            if (canales
                    != null && !canales.isEmpty()) {
                for (Canal canal : canales) {
                    if (EnumTipoCanal.EMAIL.getTipoCanal().equals(canal.getIdTipoCanal()) && canal.getEnabled()) {
                        try {
                            System.out.println("Loading Channel " + canal);
//                            canal.getCanalSettingList().remove(new CanalSetting(canal.getIdCanal(), EnumEmailSettingKeys.HIGHEST_UID.getKey()));

                            //Email Enabled is true by default, this means when we change the config to true/false, we must re-schedule.
                            MailClientFactory.createInstance(jpaController.getSchema(), canal);

                            String freqStr = canal.getSetting(EnumEmailSettingKeys.CHECK_FREQUENCY.getKey());
                            int freq = HelpDeskScheluder.DEFAULT_CHECK_EMAIL_INTERVAL;
                            try {
                                if (freqStr != null) {
                                    freq = Integer.parseInt(freqStr);
                                }
                            } catch (NumberFormatException ex) {/*probably a weird value*/

                            }

                            HelpDeskScheluder.scheduleRevisarCorreo(schema, canal.getIdCanal(), freq);

                        } catch (SchedulerException ex) {
                            Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "ERROR TRYING TO schedule next RevisarCorreo Canal:" + canal, ex);
                        }
                    }
                }

            }
            //Done!
            //now is not necessary as we persisted the triggers in the DB.
//            autoOpsExec.agendarAlertasForAllCasos(schema);

            Logger.getLogger(AppStarter.class.getName()).log(Level.INFO, "Tenant {0} loaded OK.", schema);

        } catch (Exception ex) {
            Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "Tenant " + schema + " loading ERROR.", ex);
        }

    }

    /**
     * TODO take the datasource name from the quartz properties file please!
     *
     * @return
     */
    static Connection lookupConnection() {
        String DATASOURCE_CONTEXT = "jdbc/godesk";

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
