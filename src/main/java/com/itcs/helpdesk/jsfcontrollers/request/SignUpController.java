/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.request;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.commons.Tenant;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import static com.itcs.helpdesk.persistence.jpa.service.TenantDataPopulator.getMD5;
import com.itcs.helpdesk.util.MailNotifier;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
@ManagedBean
@SessionScoped
public class SignUpController implements Serializable {

    @Resource
    protected UserTransaction utx = null;
    @PersistenceUnit(unitName = "CommonGodeskPU")
    protected EntityManagerFactory CommonGodeskPUEmf = null;

//    private RegistrationVO registrationVO = new RegistrationVO();
//    private boolean agreedToTermsConditions = false;
    private Tenant tenant = new Tenant();
    private boolean availableDomain = false;
    private JPAServiceFacade commonController = null;

    /**
     * Creates a new instance of SignUpController
     */
    public SignUpController() {
    }

    public JPAServiceFacade getJpaController() {
        if (commonController == null) {
            commonController = new JPAServiceFacade(utx, CommonGodeskPUEmf, null);//default schema godesk_common db public schema.
        }
        return commonController;
    }

    public void checkDomainAvailabilty() {
        String subDomainName = !StringUtils.isEmpty(tenant.getSubDomainName()) ? tenant.getSubDomainName() : null;
        if (subDomainName != null) {

            List<Tenant> tenants = (List<Tenant>) getJpaController().getEntityManager().createNamedQuery("Tenant.findByDomain").setParameter("subDomainName", subDomainName).getResultList();

            System.out.println("tenants found with same domain:" + tenants);
            
            if(tenants == null || tenants.isEmpty()){
                //valid domain
                setAvailableDomain(true);
            }
        } else {
            setAvailableDomain(false);
        }
    }

    /**
     * First step of signing up an account, we send an email verification EMAIL to the customer in order to
     * make sure the email is valid.
     * @return 
     */
    public String signup0() {
        try {
            String uuid = UUID.randomUUID().toString();

            tenant.setTenantUUID(uuid);
            
            tenant.setSchemaName(tenant.getSubDomainName());
            
            tenant.setStatus(Tenant.STATUS_VALIDATION_EMAIL_SENT);
            
            tenant.setPassw(getMD5(tenant.getPassw()));

            getJpaController().persist(tenant);

            MailNotifier.sendEmailVerifyNewAccount(uuid, tenant.getUserEmail(), tenant.getUserFullName(), uuid);

            return "signupsuccess";
        } catch (Exception e) {
            tenant.setPassw(null);
            tenant.setTenantUUID(null);
            e.printStackTrace();
            JsfUtil.addErrorMessage("Lo sentimos ha ocurrido un error no esperado. favor intente más tarde. ErrorCode:" + e.getMessage());
        }

        return null;
    }

    /**
     * rapid code this needs to be refactored
     *
     * @return
     */
//    public String signup() {
//
////        if (!agreedToTermsConditions) {
////            addErrorMessage("Debe estar de acuerdo con los términos del servicio antes de crear su cuenta.");
////            return null;
////        }
//        if (!registrationVO.getPassword().equals(registrationVO.getPasswordConfirm())) {
//            JsfUtil.addErrorMessage("Verifique su contraseña, la contraseña de confirmación no coincide.");
//            return null;
//        }
//
//        if (!StringUtils.isEmpty(registrationVO.getCompanyName())) {
//            try {
//                final String schemaName = registrationVO.getCompanyName().trim().toLowerCase().replace("\u0020", "_");
//                registrationVO.setCompanyName(schemaName);
//                //run this in public schema
//                final JPAServiceFacade jpaController1 = new JPAServiceFacade(utx, emf, AbstractJPAController.PUBLIC_SCHEMA_NAME);
//                String schemaNameCreated = jpaController1.createTheSchema(registrationVO);
//
//                String verificationCode = UUID.randomUUID().toString();
//                //Insert the base data. run this in the schema just created
//                Usuario usuarioOwner = new Usuario(registrationVO.getUsername(), true,
//                        getMD5(registrationVO.getPasswordConfirm()), "", registrationVO.getEmail(), true,
//                        registrationVO.getFullName(), "");
//                usuarioOwner.setTelFijo(registrationVO.getPhoneNumber());
//                usuarioOwner.setVerificationCode(verificationCode);
//
//                final JPAServiceFacade tenantJPAController = new JPAServiceFacade(utx, emf, schemaNameCreated);
//                tenantJPAController.setupTheSchema(usuarioOwner, registrationVO.getDefaultAreas());
//
//                TenantDataPopulator dataPopulator = new TenantDataPopulator(new JPAServiceFacade(utx, emf, schemaNameCreated));
//                dataPopulator.populateBaseData();
//                //Insert the Admin user
//
////                Usuario admin = dataPopulator.insertAdminUser(verificationCode, registrationVO);
//                //for now we will trust the email is valid. and login the user inmediately.
//                JsfUtil.getUserSessionBean().setCurrent(usuarioOwner);
//                JsfUtil.getUserSessionBean().setTenantId(usuarioOwner.getTenantId());
//                AppStarter.initTenant(utx, emf, usuarioOwner.getTenantId());
//
//                HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
//
//                //Lots of duplicated code here!!! but anyways we need to save time
//                UsuarioSessionLog sessionLog = new UsuarioSessionLog();
//                sessionLog.setIdUsuario(usuarioOwner);
//                sessionLog.setIp(request.getRemoteAddr());
//                sessionLog.setTimestampLogin(new Date());
//                sessionLog.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
//                sessionLog.setLanguages(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
//
//                tenantJPAController.persist(sessionLog);
//                usuarioOwner.addUsuarioSessionLog(sessionLog);
//
//                JsfUtil.getUserSessionBean().setCurrentSessionLog(sessionLog);
//
//                String channel = "/" + UUID.randomUUID().toString();
//                JsfUtil.getUserSessionBean().setChannel(channel);
//                JsfUtil.getApplicationBean().addChannel(usuarioOwner.getIdUsuario(), channel);
//                final String sessionId = JsfUtil.getRequest().getSession().getId();
//                JsfUtil.getApplicationBean().getSessionIdMappings().put(sessionId, usuarioOwner.getIdUsuario());
//                RequestContext requestContext = RequestContext.getCurrentInstance();
//                requestContext.execute("PF('socketMessages').connect('/" + usuarioOwner.getIdUsuario() + "')");
//
//                //send verification email
//                MailNotifier.sendGreetingsToNewAccount(usuarioOwner.getTenantId(), usuarioOwner.getEmail(), usuarioOwner.getCapitalName(), verificationCode);
//
//                return "dashboard";
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                JsfUtil.addErrorMessage("Lo sentimos ha ocurrido un error no esperado. favor intente más tarde. ErrorCode:" + e.getMessage());
//            }
//        }
//
//        return null;
//    }
//    public void validateCompanyName(FacesContext context, UIComponent component,
//            Object value) throws ValidatorException {
//        System.out.println("validateCompanyName()");
//        String strValue = (String) value;
//
//        //Set the schema pattern string
//        Pattern p = Pattern.compile("[a-z][a-z0-9_]+$");
//
//        //Match the given string with the pattern
//        Matcher m = p.matcher(strValue);
//
//        //Check whether match is found
//        boolean matchFound = m.matches();
//
//        if (!matchFound) {
//            FacesMessage message = new FacesMessage();
//            message.setDetail("El nombre de empresa no es válido.");
//            message.setSummary("El nombre de empresa no es válido.");
//            message.setSeverity(FacesMessage.SEVERITY_ERROR);
//            throw new ValidatorException(message);
//        }
//
//        //Public schema
//        final JPAServiceFacade jpaController1 = new JPAServiceFacade(utx, emf, AbstractJPAController.PUBLIC_SCHEMA_NAME);
//        String result = jpaController1.findSchemaByName(strValue);
//
//        if (!StringUtils.isEmpty(result)) {
//            FacesMessage message = new FacesMessage();
//            message.setDetail("El nombre de empresa '" + result + "' ya está siendo utilizado.");
//            message.setSummary("El nombre de empresa '" + result + "' ya está siendo utilizado.");
//            message.setSeverity(FacesMessage.SEVERITY_ERROR);
//            throw new ValidatorException(message);
//        }
//    }
    /**
     * @return the tenant
     */
    public Tenant getTenant() {
        return tenant;
    }

    /**
     * @param tenant the tenant to set
     */
    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    /**
     * @return the availableDomain
     */
    public boolean isAvailableDomain() {
        return availableDomain;
    }

    /**
     * @param availableDomain the availableDomain to set
     */
    public void setAvailableDomain(boolean availableDomain) {
        this.availableDomain = availableDomain;
    }

    

}
