package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.UsuarioSessionLog;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailNotifier;
import com.itcs.helpdesk.util.UtilSecurity;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.RandomStringUtils;
import org.primefaces.context.RequestContext;

@ManagedBean(name = "loginController")
@RequestScoped
public class LoginController extends AbstractManagedBean<Usuario> implements Serializable {

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;

    //private LengthValidator passwordLengthValidator = new LengthValidator();
    private String username;
    private String password;
    private Usuario usuario;
//    private SesionesJpaController sesionesJpaController;
    //change pass
    private String passwordCurrent;
    private String passwordNew1;
    private String passwordNew2;

    /**
     * <p>
     * Construct a new request data bean instance.</p>
     */
    public LoginController() {
        super(Usuario.class);
    }

    public String changePass() {
        try {
            if (passwordCurrent != null) {
                Usuario user = getUserSessionBean().getCurrent();
                String passMD5 = UtilSecurity.getMD5(passwordCurrent);
                if (user.getPass().equals(passMD5)) {
                    //coincide con su contraseña actual! 
                    if (passwordNew1 != null && passwordNew1.equals(passwordNew2)) {
                        //So change it
                        user.setPass(UtilSecurity.getMD5(passwordNew1));
                        getJpaController().getUsuarioJpaController().edit(user);
                        JsfUtil.addSuccessMessage("La contraseña ha sido cambiada exitósamente.");
                        executeInClient("PF('panelChangePass').hide()");
                    } else {
                        JsfUtil.addErrorMessage("La nueva contraseña no coincide!");
                    }

                } else {
                    JsfUtil.addErrorMessage("La contraseña ingresada no coincide con su contraseña actual!");

                }
            } else {
                JsfUtil.addErrorMessage("Debe ingresar su contraseña actual.");

            }
        } catch (Exception ex) {
            JsfUtil.addErrorMessage("Ocurrió un problema con el servicio, Favor intente más tarde.");
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * RESET PASS
     *
     * @return
     */
    public String resetPassAction() {
        if (!getUserSessionBean().isValidatedSession()) {
            try {
                usuario = getJpaController().find(Usuario.class, username);
                if (usuario == null) {
                    throw new NoResultException();
                } else if (usuario.getPass() != null) {
                    String randomPass = RandomStringUtils.randomAlphanumeric(8);
                    usuario.setPass(UtilSecurity.getMD5(randomPass));//encript the password
                    getJpaController().merge(usuario);
                    MailNotifier.sendEmailRecoverPassword(usuario, randomPass);
                    JsfUtil.addSuccessMessage("Revise su casilla de correo. La contraseña será enviada a su dirección " + usuario.getEmail());
                    return null;
                } else {
                    JsfUtil.addErrorMessage("Ud. No tiene activada su contraseña, favor comuniquese con administración para solicitar una.");
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "loginAction", ex);
                JsfUtil.addErrorMessage("El nombre de usuario ingresado no está registrado en el sistema.");
            } catch (Exception ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage("Ocurrió un error inesperado, favor intentelo más tarde.");
            }
        } else {
            HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
            request.getSession().invalidate();
        }
        return null;
    }

    public String loginAction() {

        if (!getUserSessionBean().isValidatedSession()) {
            try {
                usuario = getJpaController().getUsuarioFindByIdUsuario(username);
                if (usuario == null) {
                    JsfUtil.addErrorMessage("El nombre de usuario ingresado no está registrado en el sistema.");
//                    return null;
                } else if (usuario.getPass() != null) {
                    String passMD5 = UtilSecurity.getMD5(password);
//                    System.out.println("pass: "+password+" en MD5:"+passMD5);
//                    System.out.println("usuario pass: "+usuario.getPass());
                    if (usuario.getPass().equals(passMD5)) {//compare encrypted pass

                        HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();

                        UsuarioSessionLog sessionLog = new UsuarioSessionLog();
                        sessionLog.setIdUsuario(usuario);
                        sessionLog.setIp(request.getRemoteAddr());
                        sessionLog.setTimestampLogin(new Date());
                        sessionLog.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
                        sessionLog.setLanguages(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
                        getJpaController().persist(sessionLog);
                        usuario.addUsuarioSessionLog(sessionLog);
                        getUserSessionBean().setCurrent(usuario);
                        getUserSessionBean().setCurrentSessionLog(sessionLog);

                        String channel = "/" + UUID.randomUUID().toString();
                        getUserSessionBean().setChannel(channel);
                        getApplicationBean().addChannel(usuario.getIdUsuario(), channel);
                        final String sessionId = JsfUtil.getRequest().getSession().getId();
                        System.out.println("sessionId:" + sessionId);
                        getApplicationBean().getSessionIdMappings().put(sessionId, usuario.getIdUsuario());
                        RequestContext requestContext = RequestContext.getCurrentInstance();
                        requestContext.execute("PF('socketMessages').connect('/" + usuario.getIdUsuario() + "')");

//                        ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
//                        JsfUtil.addSuccessMessage("Bienvenido, " + usuario.getCapitalName());
                        if (isThisRequestCommingFromAMobileDevice(JsfUtil.getRequest())) {
//                            nav.performNavigation("inboxMobile");//DeskTop Version
                            return "/mobile/index.xhtml?faces-redirect=true";
                        } else {
//                            nav.performNavigation("inbox");//DeskTop Version
                            return "/script/index.xhtml?faces-redirect=true";
                        }
                    } else {
                        JsfUtil.addErrorMessage("El nombre de usuario o la contraseña son incorrectos. Por favor intenténtelo nuevamente.");
//                        return null;
                    }
                } else {
                    JsfUtil.addErrorMessage("Ud. No tiene activada su contraseña, favor comuniquese con administración para solicitar una.");
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere(ex);
                JsfUtil.addErrorMessage("El nombre de usuario ingresado no está registrado en el sistema.");
//                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.createLogger(this.getClass().getName()).logSevere(ex);
                JsfUtil.addErrorMessage("No podemos atenderlo en este momento, por favor intentelo mas tarde.");
//              return null;
            }

        } else {
            HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
            request.getSession().invalidate();
//            loginAction();
            if (isThisRequestCommingFromAMobileDevice(JsfUtil.getRequest())) {
//                            nav.performNavigation("inboxMobile");//DeskTop Version
                return "/mobile/login.xhtml?faces-redirect=true";
            } else {
//                            nav.performNavigation("inbox");//DeskTop Version
                return "/script/login.xhtml?faces-redirect=true";

            }
        }

        return null;

    }

    public String logout_action() {
        HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
//        getApplicationBean().removeChannel(getUserSessionBean().getCurrent().getIdUsuario());
        final UsuarioSessionLog currentSessionLog = getUserSessionBean().getCurrentSessionLog();
        currentSessionLog.setTimestampLogout(new Date());
        try {
            getJpaController().merge(currentSessionLog);
        } catch (Exception ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
        request.getSession().invalidate();
        return "/script/login.xhtml?faces-redirect=true";
    }

//    protected UserSessionBean getUserSessionBean() {
//        return (UserSessionBean) JsfUtil.getManagedBean("UserSessionBean");
//    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the passwordCurrent
     */
    public String getPasswordCurrent() {
        return passwordCurrent;
    }

    /**
     * @param passwordCurrent the passwordCurrent to set
     */
    public void setPasswordCurrent(String passwordCurrent) {
        this.passwordCurrent = passwordCurrent;
    }

    /**
     * @return the passwordNew1
     */
    public String getPasswordNew1() {
        return passwordNew1;
    }

    /**
     * @param passwordNew1 the passwordNew1 to set
     */
    public void setPasswordNew1(String passwordNew1) {
        this.passwordNew1 = passwordNew1;
    }

    /**
     * @return the passwordNew2
     */
    public String getPasswordNew2() {
        return passwordNew2;
    }

    /**
     * @param passwordNew2 the passwordNew2 to set
     */
    public void setPasswordNew2(String passwordNew2) {
        this.passwordNew2 = passwordNew2;
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the applicationBean
     */
    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    /**
     * @param applicationBean the applicationBean to set
     */
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }
}
