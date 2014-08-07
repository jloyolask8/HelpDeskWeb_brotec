/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import com.itcs.commons.email.EnumEmailSettingKeys;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entityenums.EnumSettingsBase;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;

/**
 *
 * @author jonathan
 */
public class ApplicationConfig {

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
    private static Log logger = Log.createLogger(ApplicationConfig.class.getName());
    private static final String MAIL_DEBUG = "mail.debug";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_USER = "mail.smtp.user";
    private static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    private static final String MAIL_SMTP_FROM = "mail.smtp.from";
    private static final String MAIL_SMTP_FROMNAME = "mail.smtp.fromname";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    private static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";
    private static final String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
    private static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
    private static final String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";
    //----------
    private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";
    //----------
    private static final String MAIL_POP_HOST = "mail.pop3s.host";
    private static final String MAIL_POP_PORT = "mail.pop3s.port";
    private static final String MAIL_POP_USER = "mail.pop3s.user";
    private static final String MAIL_POP_PASSWORD = "mail.pop3s.password";
    private static final String MAIL_POP_SSL_ENABLED = "mail.pop3s.ssl.enable";
    //----------
    private static final String MAIL_IMAPS_HOST = "mail.imaps.host";
    private static final String MAIL_IMAPS_PORT = "mail.imaps.port";
    private static final String MAIL_IMAPS_USER = "mail.imaps.user";
    private static final String MAIL_IMAPS_PASSWORD = "mail.imaps.password";
    private static final String MAIL_IMAPS_SSL_ENABLED = "mail.imaps.ssl.enable";
    //------
    //----------    
    private Properties configuration;
    private static ApplicationConfig instance = null;

    public static Properties generateEmailPropertiesFromCanal(Canal canal) {

        Properties props = new Properties();

        try {
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
                        if (canal.containsKey(EnumEmailSettingKeys.TRANSPORT_TLS.getKey())
                                && StringUtils.isNotEmpty(canal.getSetting(EnumEmailSettingKeys.TRANSPORT_TLS.getKey()))) {
                            if (Boolean.getBoolean(canal.getSetting(EnumEmailSettingKeys.TRANSPORT_TLS.getKey()))) {
                                props.put(MAIL_TRANSPORT_TLS, Boolean.TRUE);
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

        if (ApplicationConfig.isAppDebugEnabled()) {
            logger.logDebug("Called generateEmailPropertiesFromArea():" + props);
        }
        return props;

    }

    public static ApplicationConfig getInstance() {
        if (instance == null) {
            instance = new ApplicationConfig();
            instance.setConfiguration(new Properties());
//            instance.loadFromBundleConfiguration();//Here i can change to load properties from the Database.
        }

        return instance;
    }

    public void loadConfiguration(Properties p) {
//        configuration = new Properties();
        configuration.putAll(p);
//        configuration = new Properties();
//        configuration.putAll(EntityClassReflector.getPropertiesFromEntity(appConfig));
//        configuration.putAll(EntityClassReflector.getPropertiesFromEntity(emailConfig));
//        getInstance().setConfiguration(configuration);
        logger.logDebug(p);
    }

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
    private static String getProperty(String key) {
        return ApplicationConfig.getInstance().getConfiguration().getProperty(key);
    }

    private static String getProperty(String key, String defaultValue) {
        return ApplicationConfig.getInstance().getConfiguration().getProperty(key, defaultValue);
    }

    public static String getSaludoClienteHombre() {
        return ApplicationConfig.getProperty(EnumSettingsBase.SALUDO_CLIENTE_HOMBRE.getAppSetting().getSettingKey());
    }

    public static String getSaludoClienteMujer() {
        return ApplicationConfig.getProperty(EnumSettingsBase.SALUDO_CLIENTE_MUJER.getAppSetting().getSettingKey());
    }

    public static String getSaludoClienteUnknown() {
        return ApplicationConfig.getProperty(EnumSettingsBase.SALUDO_CLIENTE_UNKNOWN.getAppSetting().getSettingKey());
    }

    public static boolean isShowCompanyLogo() {
        boolean value = false;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.SHOW_COMPANY_LOGO.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

//    public static String getAttachmentRuta() {
//        return ApplicationConfig.getProperty(EnumSettingsBase.ATTACHMENT_RUTA.getAppSetting().getSettingKey());
//    }
    public static String getHelpdeskTitle() {
        return ApplicationConfig.getProperty(EnumSettingsBase.HELPDESK_TITLE.getAppSetting().getSettingKey());
    }

    public static String getCompanyLogo() {
        return ApplicationConfig.getProperty(EnumSettingsBase.COMPANY_LOGO_ID_ATTACHMENT.getAppSetting().getSettingKey());
    }

    public static int getCompanyLogoSize() {
        int value = 100;
        try {
            value = Integer.valueOf(getProperty(EnumSettingsBase.COMPANY_LOGO_SIZE.getAppSetting().getSettingKey(), "100"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getCompanyName() {
        return ApplicationConfig.getProperty(EnumSettingsBase.COMPANY_NAME.getAppSetting().getSettingKey());
    }

    public static String getNotificationSubjectText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIFICATION_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public static String getNotificationBodyText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIFICATION_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public static String getNotificationClientSubjectText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIFICATION_UPDATE_CLIENT_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public static String getNotificationClientBodyText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIFICATION_UPDATE_CLIENT_BODY_TEXT.getAppSetting().getSettingKey());
    }

    public static String getProductDescription() {
        return ApplicationConfig.getProperty(EnumSettingsBase.PRODUCT_DESCRIPTION.getAppSetting().getSettingKey());
    }

    public static String getProductComponentDescription() {
        return ApplicationConfig.getProperty(EnumSettingsBase.PRODUCT_COMP_DESCRIPTION.getAppSetting().getSettingKey());
    }

    public static String getProductSubComponentDescription() {
        return ApplicationConfig.getProperty(EnumSettingsBase.PRODUCT_SUBCOMP_DESCRIPTION.getAppSetting().getSettingKey());
    }

    public static boolean isSendNotificationOnTransfer() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_NOTIFICATION_ON_TRANSFER.getAppSetting().getSettingKey(), "true"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean isSendNotificationOnSubscribedToEvent() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_NOTIFICATION_TOCLIENT_ON_SUBSCRIBED_TO_EVENT.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean isSendNotificationToClientOnNewTicket() {
        boolean value = true;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_NOTIFICATION_TOCLIENT_ON_NEW_TICKET.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getNotificationClientSubjectNewTicketText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIFICATION_NEW_TICKET_CLIENT_SUBJECT_TEXT.getAppSetting().getSettingKey());
    }

    public static String getNotificationClientBodyNewTicketText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIFICATION_NEW_TICKET_CLIENT_BODY_TEXT.getAppSetting().getSettingKey());
    }
    
     public static String getNotificationClientSubjectSubscribedToEventText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIF_SUBSCRIBED_EVENT_CLIENT_SUBJECT.getAppSetting().getSettingKey());
    }

    public static String getNotificationClientBodySubscribedToEventText() {
        return ApplicationConfig.getProperty(EnumSettingsBase.NOTIFICATION_SUBSCRIBED_TO_EVENT_CLIENT_BODY_TEXT.getAppSetting().getSettingKey());
    }

//      public static boolean isSendGroupNotifOnNewCaseEnabled() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(getProperty(EnumSettingsBase.SEND_GROUP_NOTIFICATION_ON_NEW_CASE.getAppSetting().getSettingKey(), "false"));
//            if (isAppDebugEnabled()) {
//                System.out.println("SEND_GROUP_NOTIFICATION_ON_NEW_CASE:" + value);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return value;
//    }
    public static boolean isAppDebugEnabled() {
        boolean value = false;
        try {
            value = Boolean.valueOf(getProperty(EnumSettingsBase.DEBUG_ENABLED.getAppSetting().getSettingKey(), "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

//    private static String getMailServerType() {
//        return ApplicationConfig.getProperty(MAIL_SERVER_TYPE);
//    }
//
//    private static String getMailSessionJndiName() {
//        return ApplicationConfig.getProperty(MAIL_SESSION_JNDINAME);
//    }
//
//    private static String getTextoRespuestaCaso() {
//        return ApplicationConfig.getProperty(TEXTO_RESP_CASO);
//    }
//
//    private static String getTextoRespuestaAutomatica() {
//        return ApplicationConfig.getProperty(TEXTO_RESP_AUTOMATICA);
//    }
//
//    private static String getSubjectRespuestaAutomatica() {
//        return ApplicationConfig.getProperty(SUBJECT_RESP_AUTOMATICA);
//    }
//    private static boolean getEmailAcuseDeRecibo() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(ApplicationConfig.getProperty(ApplicationConfig.EMAIL_ACUSEDERECIBO, "false"));
//        } catch (Exception e) {
//            logger.logSevere(e.getMessage());
//        }
//        return value;
//    }
//
//    private static boolean isEmailEnabled() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(ApplicationConfig.getProperty(EMAIL_ENABLED, "false"));
//        } catch (Exception e) {
//            logger.logSevere(e.getMessage());
//        }
//        return value;
//    }
//
//    private static boolean isJndiEnabled() {
//        boolean value = false;
//        try {
//            value = Boolean.valueOf(ApplicationConfig.getProperty(MAIL_USE_JNDI, "false"));
//        } catch (Exception e) {
//            logger.logSevere(e.getMessage());
//        }
//        return value;
//    }
//    /**
//     * @return the EMAIL_FRECUENCIA
//     */
//    private static int getEmailFrecuencia() {
//        int value = 10;
//        try {
//            value = Integer.valueOf(ApplicationConfig.getProperty(EMAIL_FRECUENCIA));
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
