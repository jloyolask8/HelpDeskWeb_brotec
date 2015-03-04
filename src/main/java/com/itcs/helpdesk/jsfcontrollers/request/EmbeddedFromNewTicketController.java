/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.request;

import com.itcs.helpdesk.jsfcontrollers.*;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import java.util.List;
import java.util.logging.Level;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "embeddedFromNewTicketController")
@SessionScoped
public class EmbeddedFromNewTicketController extends CustomerCasoController {

    //customer
    private int stepNewCasoIndex;
    private boolean embeddedFlag = false;
    private boolean showAttachmentsFlag = false;

    private String currentTenantId;

    public void toggleShowAttachments() {
        showAttachmentsFlag = !showAttachmentsFlag;
    }
    
    @Override
    public String getCurrentTenantId(){
        return this.currentTenantId;
    }
    
    
    public List<TipoCaso> getTipoCasoAvailableList() {
        final List<TipoCaso> tipos = (List<TipoCaso>) getJpaController().findAll(TipoCaso.class);
        tipos.remove(EnumTipoCaso.INTERNO.getTipoCaso());
//        //System.out.println("*** getTipoCasoAvailableList()");
        return tipos;
    }

    @Override
    public void initializeEmbeddedForm(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("initializeEmbeddedForm()...");
        if (this.current == null) {
            prepareCreateCasoFromCustomer();
            setStepNewCasoIndex(1);//2,3 o 4.
        }
        embeddedFlag = true;

        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkTenantParam(req);
    }

    private void checkTenantParam(HttpServletRequest req) {
        String tenant = req.getParameter("tenant");
        if (tenant != null && !tenant.isEmpty()) {
            System.out.println("tenant=" + tenant);
            this.currentTenantId = tenant;
        }
    }

    public String createCasoCustomerStep() {

        switch (getStepNewCasoIndex()) {
            case 1:

                EmailCliente email = getJpaControllerLocal().find(EmailCliente.class, emailCliente_wizard);
                if (email != null) {
                    if (email.getCliente() == null) {
                        email.setCliente(new Cliente());
                        emailCliente_wizard_updateCliente = false;
                        emailCliente_wizard_existeCliente = false;
                    } else {
                        List<ProductoContratado> prods = email.getCliente().getProductoContratadoList();
                        if (prods != null && !prods.isEmpty()) {
                            ProductoContratado pc = prods.get(0);
                            current.setIdProducto(pc.getProducto());
                            current.setIdComponente(pc.getComponente());
                            current.setIdSubComponente(pc.getSubComponente());
                        }
                        emailCliente_wizard_updateCliente = true;
                        emailCliente_wizard_existeCliente = true;
                    }
                    emailCliente_wizard_existeEmail = true;
                } else {
                    emailCliente_wizard_existeEmail = false;
                    emailCliente_wizard_updateCliente = false;
                    emailCliente_wizard_existeCliente = false;
                    email = new EmailCliente(emailCliente_wizard);
                    email.setCliente(new Cliente());
                }

                current.setEmailCliente(email);

//                if(!emailCliente_wizard.equalsIgnoreCase(userSessionBean.getEmailCliente().getEmailCliente())){
//                    //email entered not customer email.
//                    
//                }
                setStepNewCasoIndex(getStepNewCasoIndex() + 1);
                break;
            case 2:
                setStepNewCasoIndex(getStepNewCasoIndex() + 1);
                break;
            case 3:
                setStepNewCasoIndex(getStepNewCasoIndex() + 1);
                break;
            case 4:
                return createAndCustomerView();

        }

        return null;

    }

    protected ManagerCasos getManagerCasosLocal() {
        if (!StringUtils.isEmpty(getCurrentTenantId())) {
            ManagerCasos managerCasos = new ManagerCasos();
            managerCasos.setJpaController(getJpaControllerLocal());
            return managerCasos;
        }
        throw new IllegalAccessError("Error al acceder al ManagerCasos");
    }

    public JPAServiceFacade getJpaControllerLocal() {
        if (!StringUtils.isEmpty(getCurrentTenantId())) {
            JPAServiceFacade jpaController = new JPAServiceFacade(utx, emf, getCurrentTenantId());
            return jpaController;
        }
        throw new IllegalAccessError("Error al acceder al jpa");
    }

    public String createAndCustomerView() {
        try {

            current.setIdCliente(current.getEmailCliente().getCliente());

            if (embeddedFlag) {
                current.setIdCanal(EnumCanal.GODESK_CUSTOMER_PORTAL_EMBEDDED_FORM.getCanal());
                getManagerCasosLocal().persistCaso(current, ManagerCasos.createLogReg(current, "Crear", "se crea caso desde fomulario embebido en sitio web.", ""));

            } else {
                current.setIdCanal(EnumCanal.GODESK_CUSTOMER_PORTAL.getCanal());
                getManagerCasosLocal().persistCaso(current, ManagerCasos.createLogReg(current, "Crear", "se crea caso desde portal del consumidor godesk web.", ""));
                openCase(current);
                //Auto-Login customer
                userSessionBean.setEmailCliente(current.getEmailCliente());
//            prepareCasoFilterForInbox();
                return "/customer/ticket";
            }

            setStepNewCasoIndex(getStepNewCasoIndex() + 1);
            addInfoMessage("Su solicitud ha sido ingresada exitósamente. Un ejecutivo se contactará con usted. Número de caso:[#" + current.getIdCaso() + "]");

        } catch (RollbackFailureException ex) {
            addErrorMessage("Error de base de datos", ex.getMessage());
            Log.createLogger(EmbeddedFromNewTicketController.class.getName()).log(Level.SEVERE, "persist " + current, ex);
        } catch (Exception ex) {
            Log.createLogger(EmbeddedFromNewTicketController.class.getName()).log(Level.SEVERE, "persist " + current, ex);

        }
        return null;
    }

    /**
     * @return the stepNewCasoIndex
     */
    public int getStepNewCasoIndex() {
        return stepNewCasoIndex;
    }

    /**
     * @param stepNewCasoIndex the stepNewCasoIndex to set
     */
    public void setStepNewCasoIndex(int stepNewCasoIndex) {
        this.stepNewCasoIndex = stepNewCasoIndex;
    }

    /**
     * @return the showAttachmentsFlag
     */
    public boolean isShowAttachmentsFlag() {
        return showAttachmentsFlag;
    }

    /**
     * @param showAttachmentsFlag the showAttachmentsFlag to set
     */
    public void setShowAttachmentsFlag(boolean showAttachmentsFlag) {
        this.showAttachmentsFlag = showAttachmentsFlag;
    }

 

    
}
