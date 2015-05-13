/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import com.itcs.commons.email.EnumEmailSettingKeys;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entityenums.EnumSettingsBase;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;

/**
 *
 * @author jonathan
 */
public class ApplicationConfigs {

    private final String schema;
    private Properties configuration;
    private static final Map<String, ApplicationConfigs> instances = new HashMap<>();

    public ApplicationConfigs(String schema, Properties configuration) {
        this.schema = schema;
        this.configuration = configuration;
    }

    public static void loadTenantInstance(String schema, Properties configuration) {
        if (!StringUtils.isEmpty(schema)) {
//            if (!instances.containsKey(schema)) {
            ApplicationConfigs instance = new ApplicationConfigs(schema, configuration);
            instances.put(schema, instance);
//            }
        }
    }

    public static ApplicationConfigs getInstance(String schema) throws NoInstanceConfigurationException {
        if (!StringUtils.isEmpty(schema)) {
            if (instances.containsKey(schema)) {
                return instances.get(schema);
            }
//            instance.setConfiguration(new Properties());
//            instance.loadFromBundleConfiguration();//Here i can change to load properties from the Database.
        }

        throw new NoInstanceConfigurationException();
    }

    public enum EnumMailServerType {

        EXCHANGE("EXCHANGE"),
        POPIMAP("POPIMAP"),
        SMTP("SMTP");
        private String value;

        private EnumMailServerType(String value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }
    private static Log logger = Log.createLogger(ApplicationConfigs.class.getName());
    public final static String DEFAULT_CONN_TIMEOUT = "60000";
    public final static String DEFAULT_UNREAD_DOWNLOAD_LIMIT = "10";
    public final static String DEFAULT_IO_TIMEOUT = "600000";
    public static final String MAIL_DEBUG = "mail.debug";
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";
    public static final String MAIL_SMTP_USER = "mail.smtp.user";
    public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    public static final String MAIL_SMTP_FROM = "mail.smtp.from";
    public static final String MAIL_SMTP_FROMNAME = "mail.smtp.fromname";
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    public static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";
    public static final String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
    public static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
    public static final String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";
    //----------
    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    public static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";
    //----------
    public static final String MAIL_POP_HOST = "mail.pop3s.host";
    public static final String MAIL_POP_PORT = "mail.pop3s.port";
    public static final String MAIL_POP_USER = "mail.pop3s.user";
    public static final String MAIL_POP_PASSWORD = "mail.pop3s.password";
    public static final String MAIL_POP_SSL_ENABLED = "mail.pop3s.ssl.enable";
    //----------
    public static final String MAIL_IMAPS_HOST = "mail.imaps.host";
    public static final String MAIL_IMAPS_PORT = "mail.imaps.port";
    public static final String MAIL_IMAPS_USER = "mail.imaps.user";
    public static final String MAIL_IMAPS_PASSWORD = "mail.imaps.password";
    public static final String MAIL_IMAPS_SSL_ENABLED = "mail.imaps.ssl.enable";
    public static final String MAIL_IMAPS_STARTTLS_ENABLED = "mail.imap.starttls.enable";
    //------
    //----------    
    //Socket connection timeout value in milliseconds. Default is infinite timeout.
    public static final String MAIL_IMAPS_CONN_TIMEOUT = "mail.imaps.connectiontimeout";
    public static final String MAIL_IMAP_CONN_TIMEOUT = "mail.imap.connectiontimeout";
    //Socket I/O timeout value in milliseconds. Default is infinite timeout.
    public static final String MAIL_IMAPS_SOCKETIO_TIMEOUT = "mail.imaps.timeout";
    public static final String MAIL_IMAP_SOCKETIO_TIMEOUT = "mail.imap.timeout";

//    private Properties configuration;
//    private static ApplicationConfigs instance = null;
    /**
     * Genera properties de un canal especifico
     *
     * @deprecated use {@link #new()} instead.
     *
     * @param canal
     * @return
     */
    public static Properties generateEmailPropertiesFromCanal(Canal canal) {

        Properties props = new Properties();

        try {

            if (canal.containsKey(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey())) {
                props.put(MAIL_IMAPS_CONN_TIMEOUT, canal.getSetting(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey()));
                props.put(MAIL_IMAP_CONN_TIMEOUT, canal.getSetting(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey()));//Try imap even when using imaps
            } else {
                props.put(MAIL_IMAPS_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT);
                props.put(MAIL_IMAP_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT);//Try imap even when using imaps
            }

            props.put(MAIL_IMAPS_SOCKETIO_TIMEOUT, DEFAULT_IO_TIMEOUT);
            props.put(MAIL_IMAP_SOCKETIO_TIMEOUT, DEFAULT_IO_TIMEOUT);//Try imap even when using imaps

            // MAIL_DEBUG
//            Map<String, String> mailSettings = new HashMap<String, String>();
//            for (CanalSetting canalSetting : canal.getCanalSettingList()) {
//                mailSettings.put(canalSetting.getCanalSettingPK().getCanalSettingKey(), canalSetting.getCanalSettingValue());
//            }
            if (canal.containsKey(EnumEmailSettingKeys.MAIL_DEBUG.getKey())) {
                props.put(MAIL_DEBUG, canal.getSetting(EnumEmailSettingKeys.MAIL_DEBUG.getKey()));
            } else {
                props.put(MAIL_DEBUG, Boolean.FALSE);
            }

            if (canal.containsKey(EnumEmailSettingKeys.SERVER_TYPE.getKey())
                    && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SERVER_TYPE.getKey()))) {

                if (canal.getSetting(EnumEmailSettingKeys.SERVER_TYPE.getKey()).equalsIgnoreCase(EnumMailServerType.POPIMAP.getValue())) {

                    if (canal.getSetting(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()).equalsIgnoreCase("imaps")) {

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_SERVER.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()))) {
                            props.put(MAIL_IMAPS_HOST, canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_PORT.getKey())) {
                            props.put(MAIL_IMAPS_PORT, canal.getSetting(EnumEmailSettingKeys.INBOUND_PORT.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_USER.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()))) {
                            props.put(MAIL_IMAPS_USER, canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_PASS.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()))) {
                            props.put(MAIL_IMAPS_PASSWORD, canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey())) {
                            props.put(MAIL_IMAPS_SSL_ENABLED, canal.getSetting(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_STARTTLS.getKey())) {
                            props.put(MAIL_IMAPS_STARTTLS_ENABLED, canal.getSetting(EnumEmailSettingKeys.INBOUND_STARTTLS.getKey()));
                        }

                    } else if (canal.getSetting(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()).equalsIgnoreCase("pop3s")) {
                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_SERVER.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()))) {
                            props.put(MAIL_POP_HOST, canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_PORT.getKey())) {
                            props.put(MAIL_POP_PORT, canal.getSetting(EnumEmailSettingKeys.INBOUND_PORT.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_USER.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()))) {
                            props.put(MAIL_POP_USER, canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_PASS.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()))) {
                            props.put(MAIL_POP_PASSWORD, canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()));
                        }

                        if (canal.containsKey(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey())) {
                            props.put(MAIL_POP_SSL_ENABLED, canal.getSetting(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey()));
                        }
                    }
                } else if (canal.getSetting(EnumEmailSettingKeys.SERVER_TYPE.getKey()).equalsIgnoreCase(EnumMailServerType.EXCHANGE.getValue())) {

                    if (canal.containsKey(EnumEmailSettingKeys.INBOUND_USER.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()))) {
                        props.put(Email.RECEIVER_USERNAME, canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.INBOUND_PASS.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()))) {
                        props.put(Email.RECEIVER_PASSWORD, canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.INBOUND_SERVER.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()))) {
                        props.put(Email.MAIL_EXCHANGE_SERVER, canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.DOMINIO_EXCHANGE_ENTRADA.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.DOMINIO_EXCHANGE_ENTRADA.getKey()))) {
                        props.put(Email.MAIL_EXCHANGE_DOMAIN, canal.getSetting(EnumEmailSettingKeys.DOMINIO_EXCHANGE_ENTRADA.getKey()));
                    }
                }
            }

            if (canal.containsKey(EnumEmailSettingKeys.MAIL_SERVER_TYPE_SALIDA.getKey())
                    && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.MAIL_SERVER_TYPE_SALIDA.getKey()))) {
                if (canal.getSetting(EnumEmailSettingKeys.MAIL_SERVER_TYPE_SALIDA.getKey()).equalsIgnoreCase(EnumMailServerType.SMTP.getValue())) {

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_SERVER.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_SERVER.getKey()))) {
                        props.put(MAIL_SMTP_HOST, canal.getSetting(EnumEmailSettingKeys.SMTP_SERVER.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_PORT.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_PORT.getKey()))) {
                        props.put(MAIL_SMTP_PORT, canal.getSetting(EnumEmailSettingKeys.SMTP_PORT.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_USER.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_USER.getKey()))) {
                        props.put(MAIL_SMTP_USER, canal.getSetting(EnumEmailSettingKeys.SMTP_USER.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_PASS.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_PASS.getKey()))) {
                        props.put(MAIL_SMTP_PASSWORD, canal.getSetting(EnumEmailSettingKeys.SMTP_PASS.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_FROM.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_FROM.getKey()))) {
                        props.put(MAIL_SMTP_FROM, canal.getSetting(EnumEmailSettingKeys.SMTP_FROM.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_FROMNAME.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_FROMNAME.getKey()))) {
                        props.put(MAIL_SMTP_FROMNAME, canal.getSetting(EnumEmailSettingKeys.SMTP_FROMNAME.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey()))
                            && Boolean.parseBoolean(canal.getSetting(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey()))) {
                        props.put(MAIL_SMTP_SSL_ENABLE, Boolean.TRUE);
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        if (canal.containsKey(EnumEmailSettingKeys.SMTP_SOCKET_FACTORY_PORT.getKey())) {
                            props.put(MAIL_SMTP_SOCKET_FACTORY_PORT, canal.getSetting(EnumEmailSettingKeys.SMTP_SOCKET_FACTORY_PORT.getKey()));
                        } else {
                            if (canal.containsKey(EnumEmailSettingKeys.SMTP_PORT.getKey())
                                    && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_PORT.getKey()))) {
                                props.put(MAIL_SMTP_SOCKET_FACTORY_PORT, canal.getSetting(EnumEmailSettingKeys.SMTP_PORT.getKey()));
                            }
                        }
                    } else {
                        //check TLS
                        if (canal.containsKey(EnumEmailSettingKeys.SMTP_STARTTLS.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_STARTTLS.getKey()))) {
                            if (Boolean.parseBoolean(canal.getSetting(EnumEmailSettingKeys.SMTP_STARTTLS.getKey()))) {
                                props.put(MAIL_TRANSPORT_TLS, Boolean.TRUE);
                                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                                props.put("mail.smtp.auth", "true");
                            } else {
                                props.put(MAIL_TRANSPORT_TLS, Boolean.FALSE);
                            }
                        }
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey()))) {
                        props.put(MAIL_SMTP_CONNECTIONTIMEOUT, canal.getSetting(EnumEmailSettingKeys.SMTP_CONNECTIONTIMEOUT.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.SMTP_TIMEOUT.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.SMTP_TIMEOUT.getKey()))) {
                        props.put(MAIL_SMTP_TIMEOUT, canal.getSetting(EnumEmailSettingKeys.SMTP_TIMEOUT.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.TRANSPORT_PROTOCOL.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.TRANSPORT_PROTOCOL.getKey()))) {
                        props.put(MAIL_TRANSPORT_PROTOCOL, canal.getSetting(EnumEmailSettingKeys.TRANSPORT_PROTOCOL.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.STORE_PROTOCOL.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()))) {
                        props.put(MAIL_STORE_PROTOCOL, canal.getSetting(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()));
                    }

                } else if (canal.getSetting(EnumEmailSettingKeys.MAIL_SERVER_TYPE_SALIDA.getKey()).equalsIgnoreCase(EnumMailServerType.EXCHANGE.getValue())) {

                    if (canal.containsKey(EnumEmailSettingKeys.INBOUND_USER.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()))) {
                        props.put(Email.SENDER_USERNAME, canal.getSetting(EnumEmailSettingKeys.INBOUND_USER.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.INBOUND_PASS.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()))) {
                        props.put(Email.SENDER_PASSWORD, canal.getSetting(EnumEmailSettingKeys.INBOUND_PASS.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.INBOUND_SERVER.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()))) {
                        props.put(Email.MAIL_EXCHANGE_SERVER, canal.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey()));
                    }

                    if (canal.containsKey(EnumEmailSettingKeys.DOMINIO_EXCHANGE_SALIDA.getKey())
                            && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.DOMINIO_EXCHANGE_SALIDA.getKey()))) {
                        props.put(Email.MAIL_EXCHANGE_DOMAIN, canal.getSetting(EnumEmailSettingKeys.DOMINIO_EXCHANGE_SALIDA.getKey()));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.logDebug("Called generateEmailPropertiesFromCanal():" + props);
        return props;

    }

//    public static ApplicationConfigs getInstance() {
//        if (instance == null) {
//            instance = new ApplicationConfigs();
//            instance.setConfiguration(new Properties());
////            instance.loadFromBundleConfiguration();//Here i can change to load properties from the Database.
//        }
//
//        return instance;
//    }
//    public void loadConfiguration(Properties p) {
////        configuration = new Properties();
//        configuration.putAll(p);
////        configuration = new Properties();
////        configuration.putAll(EntityClassReflector.getPropertiesFromEntity(appConfig));
////        configuration.putAll(EntityClassReflector.getPropertiesFromEntity(emailConfig));
////        getInstance().setConfiguration(configuration);
//        logger.logDebug(p);
//    }
//    private void loadFromBundleConfiguration() {
//        ResourceBundle configBundle = ResourceBundle.getBundle("/Config");
//        configuration = new Properties();
//        Enumeration<String> keys = configBundle.getKeys();
//        while (keys.hasMoreElements()) {
//            String key = keys.nextElement();
//            configuration.put(key, configBundle.getString(key));
//        }
//
//    }
    private boolean getBooleanPropertyValue(String key) {
        boolean value = false;
        try {
            value = Boolean.valueOf(getProperty(key, "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

//    private static String getProperty(String key) {
//        return ApplicationConfigs.getInstance().getConfiguration().getProperty(key);
//    }
//
//    private static String getProperty(String key, String defaultValue) {
//        return ApplicationConfigs.getInstance().getConfiguration().getProperty(key, defaultValue);
//    }
    private String getProperty(String key) {
        return getConfiguration().getProperty(key);
    }

    private String getProperty(String key, String defaultValue) {
        return getConfiguration().getProperty(key, defaultValue);
    }

    public String getSaludoClienteHombre() {
        return getProperty(EnumSettingsBase.SALUDO_CLIENTE_HOMBRE.getAppSetting().getSettingKey());
    }

    public String getSaludoClienteMujer() {
        return getProperty(EnumSettingsBase.SALUDO_CLIENTE_MUJER.getAppSetting().getSettingKey());
    }

    public String getSaludoClienteUnknown() {
        return getProperty(EnumSettingsBase.SALUDO_CLIENTE_UNKNOWN.getAppSetting().getSettingKey());
    }

    public boolean isShowCompanyLogo() {
        return getBooleanPropertyValue(EnumSettingsBase.SHOW_COMPANY_LOGO.getAppSetting().getSettingKey());
    }

    public boolean isAreaRequired() {
        return getBooleanPropertyValue(EnumSettingsBase.AREA_IS_REQUIRED.getAppSetting().getSettingKey());
    }

    public boolean isCustomerSurveyEnabled() {
        return getBooleanPropertyValue(EnumSettingsBase.SURVEY_ENABLED.getAppSetting().getSettingKey());
    }

    public boolean isProductoRequired() {
        return getBooleanPropertyValue(EnumSettingsBase.PRODUCT_IS_REQUIRED.getAppSetting().getSettingKey());
    }

//    public static String getAttachmentRuta() {
//        return ApplicationConfigs.getProperty(EnumSettingsBase.ATTACHMENT_RUTA.getAppSetting().getSettingKey());
//    }
    public String getHelpdeskTitle() {
        return getProperty(EnumSettingsBase.HELPDESK_TITLE.getAppSetting().getSettingKey());
    }

    public String getCompanyLogo() {
        return getProperty(EnumSettingsBase.COMPANY_LOGO_ID_ATTACHMENT.getAppSetting().getSettingKey());
    }

    public String getCompanyLoginBackground() {
        return getProperty(EnumSettingsBase.COMPANY_LOGIN_BACKGROUND_URL.getAppSetting().getSettingKey());
    }

    public String getCompanyContextURL() {
        return getProperty(EnumSettingsBase.COMPANY_HELPDESK_SITE_URL.getAppSetting().getSettingKey());
    }

    public int getCompanyLogoSize() {
        int value = 100;
        try {
            value = Integer.valueOf(getProperty(EnumSettingsBase.COMPANY_LOGO_SIZE.getAppSetting().getSettingKey(), "100"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public String getCompanyName() {
        return getProperty(EnumSettingsBase.COMPANY_NAME.getAppSetting().getSettingKey());
    }

    public String getNotificationTicketAlertChangeSubjectText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_TAC_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationTicketAlertChangeBodyText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_TAC_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationTicketUpdatedSubjectText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_UPDATE_AGENT_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationTicketUpdatedBodyText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_UPDATE_AGENT_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationSubjectText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationBodyText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationClientSubjectText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_UPDATE_CLIENT_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationClientBodyText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_UPDATE_CLIENT_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public String getCustomerSurveySubjectText() {
        return getProperty(EnumSettingsBase.CUSTOMER_SURVEY_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public String getCustomerSurveyBodyText() {
        return getProperty(EnumSettingsBase.CUSTOMER_SURVEY_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public String getProductDescription() {
        return getProperty(EnumSettingsBase.PRODUCT_DESCRIPTION.getAppSetting().getSettingKey());
    }

    public String getProductComponentDescription() {
        return getProperty(EnumSettingsBase.PRODUCT_COMP_DESCRIPTION.getAppSetting().getSettingKey());
    }

    public String getProductSubComponentDescription() {
        return getProperty(EnumSettingsBase.PRODUCT_SUBCOMP_DESCRIPTION.getAppSetting().getSettingKey());
    }

    public String getDiagnosticScripts() {
        return getProperty(EnumSettingsBase.DIAGNOSTIC_SCRIPT.getAppSetting().getSettingKey(), "");
    }

    public boolean isSendNotificationOnTransfer() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_NOTIFICATION_ON_TRANSFER.getAppSetting().getSettingKey(), "true"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public boolean isSendNotificationOnSubscribedToEvent() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_NOTIFICATION_TOCLIENT_ON_SUBSCRIBED_TO_EVENT.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public boolean isSendNotificationToClientOnNewTicket() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_NOTIFICATION_TOCLIENT_ON_NEW_TICKET.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public boolean createSupervisorCases() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.CREATE_CASO_SUPERVISOR_ENABLED.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    
      /**
     * enable agents to create tickets by sending email to channel.
     * @return 
     */
    public boolean isAllUsersCanCreateEmailTickets() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.CREATE_CASO_AGENTS_ENABLED.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public String getNotificationClientSubjectNewTicketText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_NEW_TICKET_CLIENT_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationClientBodyNewTicketText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_NEW_TICKET_CLIENT_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public String getNotificationClientSubjectSubscribedToEventText() {
        return getProperty(EnumSettingsBase.NOTIF_SUBSCRIBED_EVENT_CLIENT_SUBJECT.getAppSetting().getSettingKey());
    }

    public String getNotificationClientBodySubscribedToEventText() {
        return getProperty(EnumSettingsBase.NOTIFICATION_SUBSCRIBED_TO_EVENT_CLIENT_BODY_TEXT.getAppSetting().getSettingKey());
    }

//      public boolean isSendGroupNotifOnNewCaseEnabled() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_GROUP_NOTIFICATION_ON_NEW_CASE.getAppSetting().getSettingKey(), "false"));
//            if (isAppDebugEnabled()) {
//                //System.out.println("SEND_GROUP_NOTIFICATION_ON_NEW_CASE:" + value);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return value;
//    }
    public boolean isAppDebugEnabled() {
        boolean value = false;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.DEBUG_ENABLED.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

//    private String getMailServerType() {
//        return getProperty(MAIL_SERVER_TYPE);
//    }
//
//    private String getMailSessionJndiName() {
//        return getProperty(MAIL_SESSION_JNDINAME);
//    }
//
//    private String getTextoRespuestaCaso() {
//        return getProperty(TEXTO_RESP_CASO);
//    }
//
//    private String getTextoRespuestaAutomatica() {
//        return getProperty(TEXTO_RESP_AUTOMATICA);
//    }
//
//    private String getSubjectRespuestaAutomatica() {
//        return getProperty(SUBJECT_RESP_AUTOMATICA);
//    }
//    private boolean getEmailAcuseDeRecibo() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(getProperty(EMAIL_ACUSEDERECIBO, "false"));
//        } catch (Exception e) {
//            logger.logSevere(e.getMessage());
//        }
//        return value;
//    }
//
//    private boolean isEmailEnabled() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(getProperty(EMAIL_ENABLED, "false"));
//        } catch (Exception e) {
//            logger.logSevere(e.getMessage());
//        }
//        return value;
//    }
//
//    private boolean isJndiEnabled() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(getProperty(MAIL_USE_JNDI, "false"));
//        } catch (Exception e) {
//            logger.logSevere(e.getMessage());
//        }
//        return value;
//    }
//    /**
//     * @return the EMAIL_FRECUENCIA
//     */
//    private int getEmailFrecuencia() {
//        int value = 10;
//        try {
//            value = Integer.valueOf(getProperty(EMAIL_FRECUENCIA));
//        } catch (Exception e) {
//            logger.logSevere(e.getMessage());
//        }
//        return value;
//    }
    /**
     * @return the configuration
     */
    public Properties getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    private void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }
}
