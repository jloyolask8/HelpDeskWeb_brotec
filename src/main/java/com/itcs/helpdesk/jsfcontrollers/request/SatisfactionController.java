/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.request;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.jsfcontrollers.customer.LoginCustomerController;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
//@Named(value = "satisfactionController")
@ManagedBean(name = "satisfactionController")
@RequestScoped
public class SatisfactionController extends AbstractManagedBean<Caso> implements Serializable {

    @ManagedProperty(value = "#{param.sat}")
    private String sat;

    @ManagedProperty(value = "#{param.idCaso}")
    private String idCaso;

    @ManagedProperty(value = "#{param.emailCliente}")
    private String emailCliente;

    private EmailCliente emailClienteEntity;

    private String msg;

    @PostConstruct
    public void doTheJob() {
        checkAccessToEvaluateTicket();
    }

    /**
     * Creates a new instance of SatisfactionController
     */
    public SatisfactionController() {
        super(Caso.class);
    }

    public void checkAccessToEvaluateTicket() {
        //System.out.println("checkAccessToEvaluateTicket()...");
//        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        if (!StringUtils.isEmpty(idCaso) && !StringUtils.isEmpty(emailCliente) && !StringUtils.isEmpty(sat)) {
            Caso caso = getJpaController().findCasoByIdEmailCliente(emailCliente, Long.valueOf(idCaso));
            EmailCliente email = getJpaController().find(EmailCliente.class, emailCliente);

            if (caso != null && email != null && caso.getEmailCliente().equals(email)) {
                setEmailClienteEntity(email);
                setSelected(caso);

                if (caso.isClosed()) {
                    if (caso.getCustomerSatisfied() == null) {
                        try {
                            caso.setCustomerSatisfied(Boolean.valueOf(sat));
                            caso.setFechaCustomerEvaluation(new Date());
                            getJpaController().merge(caso);

                            setMsg("Estimad@ " + (email.getCliente() != null ? email.getCliente().getCapitalName() : emailCliente) + ", "
                                    + "Gracias por responder nuestra encuesta de satisfacción!");

                        } catch (Exception ex) {
                            setMsg("Lo sentimos, en este momento no podemos atenderle. Favor inténtelo nuevamente más tarde.");
                            Logger.getLogger(LoginCustomerController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {

                        setMsg("No se puede evaluar el caso. Este caso ya ha sido evaluado como " + (caso.getCustomerSatisfied() ? "Satisfecho." : "Insatisfecho."));
                    }
                } else {
                    setMsg("Aún no se puede evaluar el caso, Su agente debe cerrar el caso primero.");
                }

            } else {
                setMsg("Lo sentimos, el número de caso no corresponde. Favor inténtelo nuevamente más tarde.");
            }
        }

    }

    /**
     * Get the value of sat
     *
     * @return the value of sat
     */
    public String getSat() {
        return sat;
    }

    /**
     * Set the value of sat
     *
     * @param sat new value of sat
     */
    public void setSat(String sat) {
        //System.out.println("setSat..." + sat);
        this.sat = sat;
    }

    /**
     * @return the idCaso
     */
    public String getIdCaso() {
        return idCaso;
    }

    /**
     * @param idCaso the idCaso to set
     */
    public void setIdCaso(String idCaso) {
        this.idCaso = idCaso;
    }

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

    @Override
    protected Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the emailClienteEntity
     */
    public EmailCliente getEmailClienteEntity() {
        return emailClienteEntity;
    }

    /**
     * @param emailClienteEntity the emailClienteEntity to set
     */
    public void setEmailClienteEntity(EmailCliente emailClienteEntity) {
        this.emailClienteEntity = emailClienteEntity;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

}
