package com.itcs.helpdesk.util;

import com.itcs.commons.email.EmailAutoconfigClient;
import com.itcs.commons.email.EmailClient;
import com.itcs.commons.email.EnumEmailSettingKeys;
import com.itcs.commons.email.impl.ExchangeEmailClientImpl;
import com.itcs.commons.email.impl.PopImapEmailClientImpl;
import com.itcs.helpdesk.persistence.entities.Canal;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.mail.Email;

/**
 *
 * @author Jonathan
 */
public class MailClientFactory {

//    private static EmailClient instance;
    private static HashMap<String, EmailClient> clients = new HashMap<>();

    /**
     * This method creates an instance of EmailClient based on the configuration
     * passed.
     *
     * @param tenant
     * @param canal canal de tipo email
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static EmailClient createInstance(String tenant, Canal canal) throws FileNotFoundException, IOException {
//        if (instance == null) {
        EmailClient instance = null;
//        String serverType = a.getMailServerType();//props.getProperty(ApplicationConfig.MAIL_SERVER_TYPE);
        if (canal.containsKey(EnumEmailSettingKeys.SERVER_TYPE.getKey()) && canal.getSetting(EnumEmailSettingKeys.SERVER_TYPE.getKey()).equals(Email.POP_IMAP)) {
            boolean useJNDI = Boolean.valueOf(canal.getSetting(EnumEmailSettingKeys.USE_JNDI.getKey()));
            if (useJNDI) {
                instance = new PopImapEmailClientImpl(canal.getSetting(EnumEmailSettingKeys.SESSION_JNDINAME.getKey()));//props.getProperty(ApplicationConfig.MAIL_SESSION_JNDINAME));
            } else {
//                instance = new PopImapEmailClientImpl(ApplicationConfig.generateEmailPropertiesFromCanal(canal));
                instance = new PopImapEmailClientImpl(EmailAutoconfigClient.generateEmailProperties(canal.getMapSetting()));
            }
        } else if (canal.containsKey(EnumEmailSettingKeys.SERVER_TYPE.getKey()) && canal.getSetting(EnumEmailSettingKeys.SERVER_TYPE.getKey()).equals(Email.EXCHANGE)) {
            instance = new ExchangeEmailClientImpl(ApplicationConfigs.generateEmailPropertiesFromCanal(canal));
        }
//        }
        clients.put(canal.getIdCanal(), instance);
        return instance;
    }
    
     /**
     * @param tenant
     * @param canal
     * @return the instance
     * @throws java.io.IOException
     */
    public static EmailClient getInstance(String tenant, Canal canal) throws IOException {
        if(clients != null && !clients.isEmpty() && clients.containsKey(canal.getIdCanal())){
            return clients.get(canal.getIdCanal());
        }else{
            EmailClient instance = createInstance(tenant, canal);
            return instance;
//            throw new MailNotConfiguredException("No se puede enviar correos, favor comunicarse con el administrador para que configure la cuenta de correo asociada al canal "+canal);
        }
        
    }

    /**
     * @param tenant
     * @param idCanal
     * @return the instance
     * @throws com.itcs.helpdesk.util.MailClientFactory.MailNotConfiguredException
     */
    public static EmailClient getInstance(String tenant, String idCanal) throws MailNotConfiguredException{
        if(clients != null && !clients.isEmpty() && clients.containsKey(idCanal)){
            return clients.get(idCanal);
        }else{
            throw new MailNotConfiguredException("No se puede enviar correos, favor comunicarse con el administrador para que configure la cuenta de correo asociada al canal "+idCanal);
        }
        
    }

    public static class MailNotConfiguredException extends Exception {

        public MailNotConfiguredException(String message, Throwable cause) {
            super(message, cause);
        }

        public MailNotConfiguredException(Throwable cause) {
            super(cause);
        }

        public MailNotConfiguredException(String message) {
            super(message);
        }
        
       
    }
}
