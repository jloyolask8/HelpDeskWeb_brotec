/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.jpa.exceptions.NonexistentEntityException;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private boolean embeddedFlag = false;
    private boolean showAttachmentsFlag = false;

    @Override
    protected String getListPage() {
        return "/customer/casos";
    }

    @Override
    protected String getEditPage() {
        return "/customer/ticket";
    }

    @Override
    protected String getViewPage() {
        return getEditPage();
    }

    public void toggleShowAttachments() {
        showAttachmentsFlag = !showAttachmentsFlag;
    }

    public void initializeEmbeddedForm(javax.faces.event.ComponentSystemEvent event) {
        //System.out.println("initializeEmbeddedForm()...");
        if (this.current == null) {
            prepareCreateCasoFromCustomer();
        }
        embeddedFlag = true;
    }

    public String customerCreateNota() {

        if (StringUtils.isEmpty(textoNota)) {
            addErrorMessage("Su comentario no tiene texto, verifíque e intente nuevamente");
            return null;
        }

        try {
            Nota nota = buildNewNota(current, textoNotaVisibilidadPublica, textoNota,
                    EnumTipoNota.NOTA.getTipoNota(), true);
            addNotaToCaso(current, nota);

            getJpaController().mergeCaso(current, ManagerCasos.createLogReg(current, "Cliente agrega comentarios", "Cliente agrega comentarios tipo " + nota.getTipoNota().getNombre(), ""));

        } catch (NonexistentEntityException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RollbackFailureException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            resetNotaForm();
        }

        return getEditPage();
    }

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
                current.setIdCliente(email.getCliente());
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

            current.setIdCliente(current.getEmailCliente().getCliente());

            if (embeddedFlag) {
                current.setIdCanal(EnumCanal.GODESK_CUSTOMER_PORTAL_EMBEDDED_FORM.getCanal());
                persist(current);
//                getManagerCasos().persistCaso(current, ManagerCasos.createLogReg(current, "Crear", "se crea caso desde fomulario embebido en sitio web.", ""));

            } else {
                current.setIdCanal(EnumCanal.GODESK_CUSTOMER_PORTAL.getCanal());
                persist(current);
//                getManagerCasos().persistCaso(current, ManagerCasos.createLogReg(current, "Crear", "se crea caso desde portal del consumidor godesk web.", ""));
                openCase(current);
                //Auto-Login customer
                userSessionBean.setEmailCliente(current.getEmailCliente());
//            prepareCasoFilterForInbox();
                return "/customer/ticket";
            }

            setStepNewCasoIndex(getStepNewCasoIndex() + 1);
            addInfoMessage("Su solicitud ha sido ingresada exitósamente. Un ejecutivo se contactará con usted. Número de caso:[#" + current.getIdCaso() + "]");

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
