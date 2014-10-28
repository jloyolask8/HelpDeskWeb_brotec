package com.itcs.helpdesk.jsfcontrollers.customer;

import com.itcs.commons.email.impl.NoReplySystemMailSender;
import com.itcs.helpdesk.jsfcontrollers.*;
import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

@ManagedBean(name = "loginCustomerController")
@RequestScoped
public class LoginCustomerController extends AbstractManagedBean<Cliente> implements Serializable {

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;
    @ManagedProperty(value = "#{customerCasoController}")
    private CustomerCasoController customerCasoController;
    private String emailCliente;
    private Long numCaso;

    /**
     * <p>Construct a new request data bean instance.</p>
     */
    public LoginCustomerController() {
        super(Cliente.class);
    }

    public String login() {

        Caso caso = getJpaController().findCasoByIdEmailCliente(emailCliente, numCaso);
        if (caso != null) {
            try {
                //Se encontro un caso
                userSessionBean.setEmailCliente(caso.getEmailCliente());
                userSessionBean.setCurrent(null);
                customerCasoController.prepareCasoFilterForInbox();
//            customerCasoController.prepareCustomerViewCaso(caso);
                customerCasoController.openCase(caso);
//            customerCasoController.prepareDynamicCaseData(caso);
                
                String channel = "/" + UUID.randomUUID().toString();
                userSessionBean.setChannel(channel);
                applicationBean.addChannel(emailCliente, channel);
                return "ticket";
            } catch (Exception ex) {
                Logger.getLogger(LoginCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //No se encontro un caso
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "El Email y número de caso no coinciden con ningún caso en nuestros sistemas", "Favor revisar el email o número de caso");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            
        }
        
        return null;

    }

    public String logout_action() {
        HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
        request.getSession().invalidate();
        return "/faces/customer/login.xhtml?faces-redirect=true";
    }

    private void checkEmbeddedParam(HttpServletRequest request) {
        String embedded = request.getParameter("embedded");
        if (embedded != null) {
            userSessionBean.setRunningEmbedded(true);
        } else {
            userSessionBean.setRunningEmbedded(false);
        }
    }

    public void checkAccessToCasosList(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("checkSession()...");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkEmbeddedParam(request);

        if (!userSessionBean.isValidatedCustomerSession()) {
            //No hay caso seleccionado.
            redirectToView("customer/login");
        }
    }

    public void checkAccessToTicket(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("checkCustomerSession()...");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkEmbeddedParam(request);
        String idCaso = request.getParameter("idCaso");
        String emailCliente_ = request.getParameter("emailCliente");
        if (idCaso != null && !StringUtils.isEmpty(idCaso) && emailCliente_ != null && !StringUtils.isEmpty(emailCliente_)) {
            Caso caso = getJpaController().findCasoByIdEmailCliente(emailCliente_, Long.valueOf(idCaso));
            if (caso != null) {
                //Se encontro un caso      
//                System.out.println("Se encontro caso:" + idCaso);
                setNumCaso(Long.valueOf(idCaso));
                setEmailCliente(emailCliente_);
                customerCasoController.setSelected(caso);
                userSessionBean.setEmailCliente(caso.getEmailCliente());
                redirectToView("customer/ticket");
            }
        }

        if (customerCasoController.getSelected() == null) {
            //No hay caso seleccionado.
            redirectToView("customer/login");
        }

    }

    public void prepareCreateTicket(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("initializeData()...");
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkProductoParam(req);
    }

    private void checkProductoParam(HttpServletRequest req) {
        String proy = req.getParameter("prod");
        if (proy != null && !proy.isEmpty()) {
            System.out.println("prod=" + proy);
            try {
                Producto producto = getJpaController().find(Producto.class, proy);
//                setProyecto(producto);
//                if (getDatos() != null) {
//                    if (producto != null) {
//                        getDatos().setProducto(producto.getIdProducto());
//                    } else {
//                        JsfUtil.addErrorMessage("No se ha encontrado el proyecto especificado.");
//                    }
//                }
            } catch (NoResultException ex) {
                JsfUtil.addErrorMessage("No se ha encontrado el proyecto especificado.");
                Log.createLogger(CasoController.class.getName()).logSevere("No se ha encontrado producto: " + proy);
            }
        }
    }

    public String sendCasosByEmail() {

        String email = getEmailCliente();

//        List<Usuario> usuarios = getJpaController().getUsuarioFindByEmail(getEmailCliente());

        EmailCliente emailCliente_ = getJpaController().find(EmailCliente.class, email);

        if (emailCliente_ == null || (emailCliente_.getCasoList() == null || emailCliente_.getCasoList().isEmpty())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existen casos creados con su email.",
                    "Favor revisar si ud ha creado un caso con esta direccion de correo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

//      HashMap<Long, String> casosMap = new HashMap<Long, String>();
        StringBuilder sb = new StringBuilder();

        List<Caso> casos = emailCliente_.getCasoList();
        for (Caso caso : casos) {
            sb.append(caso.getIdCaso()).append(" - ").append(caso.getTema()).append("<br/>");
        }

        try {
            //TODO improve this email content look and feel
            String html = "<html>"
                    + "<h1>Resumen de sus Casos</h1><br/>" + sb.toString()
                    + "<br/></html>";
                    NoReplySystemMailSender.sendHTML(email, "Resumen de sus Casos", html, null);
        } catch (Exception ex) {
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Gracias!. Una lista de sus casos será enviada a su casilla de correo.",
                "");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        return null;

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
     * @param applicationBean the applicationBean to set
     */
//    public void setApplicationBean(ApplicationBean applicationBean) {
//        this.applicationBean = applicationBean;
//    }
    /**
     * @return the emailCliente
     */
    public String getEmailCliente() {
        return emailCliente;
    }

    /**
     * @param emailCliente the emailCliente to set
     */
    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    /**
     * @return the numCaso
     */
    public Long getNumCaso() {
        return numCaso;
    }

    /**
     * @param numCaso the numCaso to set
     */
    public void setNumCaso(Long numCaso) {
        this.numCaso = numCaso;
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    /**
     * @param customerCasoController the customerCasoController to set
     */
    public void setCustomerCasoController(CustomerCasoController customerCasoController) {
        this.customerCasoController = customerCasoController;
    }

    /**
     * @param applicationBean the applicationBean to set
     */
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }
}
