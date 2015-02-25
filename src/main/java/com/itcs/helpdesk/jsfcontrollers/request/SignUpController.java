/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.request;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.UsuarioSessionLog;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.jpa.service.TenantDataPopulator;
import static com.itcs.helpdesk.persistence.jpa.service.TenantDataPopulator.getMD5;
import com.itcs.helpdesk.persistence.utils.vo.RegistrationVO;
import com.itcs.helpdesk.util.MailNotifier;
import com.itcs.helpdesk.webapputils.AppStarter;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;

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

        if (!registrationVO.getPassword().equals(registrationVO.getPasswordConfirm())) {
            addErrorMessage("Verifique su contraseña, la contraseña de confirmación no coincide.");
            return null;
        }

        if (!StringUtils.isEmpty(registrationVO.getCompanyName())) {
            try {
                final String schemaName = registrationVO.getCompanyName().trim().toLowerCase().replace("\u0020", "_");
                registrationVO.setCompanyName(schemaName);
                //run this in public schema
                final JPAServiceFacade jpaController1 = new JPAServiceFacade(utx, emf, AbstractJPAController.PUBLIC_SCHEMA_NAME);
                String schemaNameCreated = jpaController1.createTheSchema(registrationVO);

                String verificationCode = UUID.randomUUID().toString();
                //Insert the base data. run this in the schema just created
                Usuario usuarioOwner = new Usuario(registrationVO.getUsername(), true,
                        getMD5(registrationVO.getPasswordConfirm()), "", registrationVO.getEmail(), true,
                        registrationVO.getFullName(), "");
                usuarioOwner.setTelFijo(registrationVO.getPhoneNumber());
                usuarioOwner.setVerificationCode(verificationCode);

                final JPAServiceFacade tenantJPAController = new JPAServiceFacade(utx, emf, schemaNameCreated);
                tenantJPAController.setupTheSchema(usuarioOwner, registrationVO.getDefaultAreas());

                TenantDataPopulator dataPopulator = new TenantDataPopulator(new JPAServiceFacade(utx, emf, schemaNameCreated));
                dataPopulator.populateBaseData();
                //Insert the Admin user

//                Usuario admin = dataPopulator.insertAdminUser(verificationCode, registrationVO);
                //for now we will trust the email is valid. and login the user inmediately.
                getUserSessionBean().setCurrent(usuarioOwner);
                getUserSessionBean().setTenantId(usuarioOwner.getTenantId());
                AppStarter.initTenant(utx, emf, usuarioOwner.getTenantId());

                HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
                
                //Lots of duplicated code here!!! but anyways we need to save time
                UsuarioSessionLog sessionLog = new UsuarioSessionLog();
                sessionLog.setIdUsuario(usuarioOwner);
                sessionLog.setIp(request.getRemoteAddr());
                sessionLog.setTimestampLogin(new Date());
                sessionLog.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
                sessionLog.setLanguages(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));

                tenantJPAController.persist(sessionLog);
                usuarioOwner.addUsuarioSessionLog(sessionLog);

                getUserSessionBean().setCurrentSessionLog(sessionLog);

                String channel = "/" + UUID.randomUUID().toString();
                getUserSessionBean().setChannel(channel);
                getApplicationBean().addChannel(usuarioOwner.getIdUsuario(), channel);
                final String sessionId = JsfUtil.getRequest().getSession().getId();
                getApplicationBean().getSessionIdMappings().put(sessionId, usuarioOwner.getIdUsuario());
                RequestContext requestContext = RequestContext.getCurrentInstance();
                requestContext.execute("PF('socketMessages').connect('/" + usuarioOwner.getIdUsuario() + "')");

                //send verification email
                MailNotifier.sendGreetingsToNewAccount(usuarioOwner.getTenantId(), usuarioOwner.getEmail(), usuarioOwner.getCapitalName(), verificationCode);

                return "dashboard";

            } catch (Exception e) {
                e.printStackTrace();
                addErrorMessage("Lo sentimos ha ocurrido un error no esperado. favor intente más tarde. ErrorCode:" + e.getMessage());
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
