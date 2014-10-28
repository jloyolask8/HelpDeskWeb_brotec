/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.util.Log;
import java.util.List;
import java.util.logging.Level;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "casoCustomerController")
@SessionScoped
public class CustomerCasoController extends CasoController {

    //customer
    private int stepNewCasoIndex;

    public String prepareCreateCasoFromCustomer() {
        try {
            current = new Caso();

            if (userSessionBean.isValidatedCustomerSession()) {
                emailCliente_wizard = userSessionBean.getEmailCliente().getEmailCliente();
                current.setEmailCliente(userSessionBean.getEmailCliente());
                emailCliente_wizard_existeEmail = true;
                if (userSessionBean.getEmailCliente().getCliente() != null) {
                    emailCliente_wizard_existeCliente = true;
                    if (StringUtils.isEmpty(userSessionBean.getEmailCliente().getCliente().getRut())) {
                        rutCliente_wizard = null;
                    } else {
                        rutCliente_wizard = userSessionBean.getEmailCliente().getCliente().getRut();
                    }
                } else {
                    emailCliente_wizard_existeCliente = false;
                }
            } else {
                EmailCliente email = new EmailCliente();
                email.setCliente(new Cliente());
                current.setEmailCliente(email);
                emailCliente_wizard_existeEmail = false;
                emailCliente_wizard = null;
            }

            emailCliente_wizard_updateCliente = false;
            selectedItemIndex = -1;
            setStepNewCasoIndex(1);//2,3 o 4.

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo("Error al preparar la creacion de un caso");
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        return "/customer/newSR";
    }

    public String createCasoCustomerStep() {

        switch (getStepNewCasoIndex()) {
            case 1:

                EmailCliente email = getJpaController().find(EmailCliente.class, emailCliente_wizard);
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

    public String createAndCustomerView() {
        try {
            current.setIdCanal(EnumCanal.SISTEMA.getCanal());
            persist(current);
            openCase(current);
            //Auto-Login customer
            userSessionBean.setEmailCliente(current.getEmailCliente());
//            prepareCasoFilterForInbox();
            return "/customer/ticket";
        } catch (RollbackFailureException ex) {
            addErrorMessage(resourceBundle.getString("PersistenceErrorOccured"), ex.getMessage());
            Log.createLogger(CustomerCasoController.class.getName()).log(Level.SEVERE, "persist " + current, ex);
        } catch (Exception ex) {
            Log.createLogger(CustomerCasoController.class.getName()).log(Level.SEVERE, "persist " + current, ex);

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
}
