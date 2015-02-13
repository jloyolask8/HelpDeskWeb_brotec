/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.actionsimpl;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.rules.ActionInfo;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import com.itcs.helpdesk.util.MailNotifier;
import com.itcs.helpdesk.util.NoInstanceConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author jonathan
 */
@ActionInfo(name = "Envia caso por email",
        description = "Envia el caso completo por email", mustShow = true)
public class SendCaseByEmailAction extends Action {

//    public static final String EMAIL_TO_FIELD_KEY = "emailsToFieldKey";
//    public static final String ACTION_CLASS = "actionclass";
    public SendCaseByEmailAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }

    @Override
    public void execute(Caso caso) throws ActionExecutionException {

        caso = getJpaController().find(Caso.class, caso.getIdCaso());
        Log.createLogger(SendCaseByEmailAction.class.getName()).logSevere("SendCaseByEmailAction.execute on " + caso);
       
        String destinationEmails = getConfig();//Gets the parametros, setted 
        if (StringUtils.isEmpty(destinationEmails)) {
            throw new ActionExecutionException("Esta accion necesita parametros (destination Email addresses) para poder executarse.");
        } else {
            String[] emails = destinationEmails.split(",");
            for (String email : emails) {
                try {
                    MailNotifier.notifyCasoAsHtmlEmail(getJpaController().getSchema(), caso, email);
                } catch (MailClientFactory.MailNotConfiguredException ex) {
                    throw new ActionExecutionException("Error al tratar de enviar caso por email a " + destinationEmails + " favor verifique la configuración de correo.", ex);
                } catch (EmailException ex) {
                    throw new ActionExecutionException("Error al tratar de enviar caso por email a " + destinationEmails + " favor verifique los datos del caso.", ex);
                } catch (NoInstanceConfigurationException ex) {
                    throw new ActionExecutionException(ex.getMessage(), ex);
                }
            }
        }

    }
}
