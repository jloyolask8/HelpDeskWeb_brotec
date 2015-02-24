/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.request;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.jpa.service.TenantDataPopulator;
import com.itcs.helpdesk.persistence.utils.vo.RegistrationVO;
import com.itcs.helpdesk.util.MailNotifier;
import com.itcs.helpdesk.webapputils.AppStarter;
import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
@ManagedBean
@SessionScoped
public class SignUpController extends AbstractManagedBean<Usuario> implements Serializable {

    private RegistrationVO registrationVO = new RegistrationVO();
    private boolean agreedToTermsConditions = false;

    /**
     * Creates a new instance of SignUpController
     */
    public SignUpController() {
        super(Usuario.class);
    }

    /**
     * rapid code this needs to be refactored
     *
     * @return
     */
    public String signup() {

        if (!agreedToTermsConditions) {
            addErrorMessage("Debe estar de acuerdo con los términos del servicio antes de crear su cuenta.");
            return null;
        }

        if (!StringUtils.isEmpty(registrationVO.getCompanyName())) {
            try {
                final String schemaName = registrationVO.getCompanyName().trim().toLowerCase().replace("\u0020", "_");
                registrationVO.setCompanyName(schemaName);
                //run this in public schema
                final JPAServiceFacade jpaController1 = new JPAServiceFacade(utx, emf, AbstractJPAController.PUBLIC_SCHEMA_NAME);
                jpaController1.createTheSchema(registrationVO);

                //Insert the base data. run this in the schema just created
                TenantDataPopulator dataPopulator = new TenantDataPopulator(new JPAServiceFacade(utx, emf, schemaName));
                dataPopulator.populateBaseData();

                //Insert the Admin user
                String verificationCode = UUID.randomUUID().toString();
                Usuario admin = dataPopulator.insertAdminUser(verificationCode, registrationVO);

                //for now we will trust the email is valid. and login the user inmediately.
                getUserSessionBean().setCurrent(admin);
                getUserSessionBean().setTenantId(admin.getTenantId());
                AppStarter.initTenant(utx, emf, admin.getTenantId());

                //send verification email
                MailNotifier.sendGreetingsToNewAccount(admin.getTenantId(), admin.getEmail(), admin.getCapitalName(), verificationCode);

                return "inbox";

            } catch (Exception e) {
                e.printStackTrace();
                addErrorMessage("Lo sentimos ha ocurrido un error no esperado. favor intente más tarde.");
            }
        }

        return null;
    }

    public void validateCompanyName(FacesContext context, UIComponent component,
            Object value) throws ValidatorException {
        System.out.println("validateCompanyName()");
        String strValue = (String) value;

        //Set the schema pattern string
        Pattern p = Pattern.compile("[a-z][a-z0-9._-]+$");

        //Match the given string with the pattern
        Matcher m = p.matcher(strValue);

        //Check whether match is found
        boolean matchFound = m.matches();

        if (!matchFound) {
            FacesMessage message = new FacesMessage();
            message.setDetail("El nombre de empresa no es válido.");
            message.setSummary("El nombre de empresa no es válido.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }

        //Public schema
        final JPAServiceFacade jpaController1 = new JPAServiceFacade(utx, emf, AbstractJPAController.PUBLIC_SCHEMA_NAME);
        String result = jpaController1.findSchemaByName(strValue);

        if (!StringUtils.isEmpty(result)) {
            FacesMessage message = new FacesMessage();
            message.setDetail("El nombre de empresa '" + result + "' ya está siendo utilizado.");
            message.setSummary("El nombre de empresa '" + result + "' ya está siendo utilizado.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }

    /**
     * @return the registrationVO
     */
    public RegistrationVO getRegistrationVO() {
        return registrationVO;
    }

    /**
     * @param registrationVO the registrationVO to set
     */
    public void setRegistrationVO(RegistrationVO registrationVO) {
        this.registrationVO = registrationVO;
    }

    /**
     * @return the agreedToTermsConditions
     */
    public boolean isAgreedToTermsConditions() {
        return agreedToTermsConditions;
    }

    /**
     * @param agreedToTermsConditions the agreedToTermsConditions to set
     */
    public void setAgreedToTermsConditions(boolean agreedToTermsConditions) {
        this.agreedToTermsConditions = agreedToTermsConditions;
    }

    @Override
    protected Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
