/*
 * Abstract action to inherit from, An action is executed when a Rules applies to case.
 */
package com.itcs.helpdesk.rules;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.ManagerCasos;

/**
 *
 * @author jonathan
 */
public abstract class Action {

    private JPAServiceFacade jpaController = null;
    private final ManagerCasos managerCasos;
    
     /**
     * config represents the parametros column in DB, it can be any String representation of data that Action needs to execute its purpose.
     */
    private String config;
    
     public Action(JPAServiceFacade jpaController) {
        this.jpaController = jpaController;
        this.managerCasos = new ManagerCasos(jpaController);
    }

//    public Action() {
//    }
//
//    public Action(JPAServiceFacade jpaController) {
//        this.jpaController = jpaController;
//    }
  
    /**
     *
     * @param caso the caso being created, so i have a reference to update the
     * caso in case the action updates the caso.
     */
  

    public abstract void execute(Caso caso) throws ActionExecutionException;

    public JPAServiceFacade getJpaController() {

        return jpaController;
    }

//    /**
//     * @param jpaController the jpaController to set
//     */
//    public void setJpaController(JPAServiceFacade jpaController) {
//        this.jpaController = jpaController;
//    }

    /**
     * @return the managerCasos
     */
    public ManagerCasos getManagerCasos() {
        return managerCasos;
    }

//    /**
//     * @param managerCasos the managerCasos to set
//     */
//    public void setManagerCasos(ManagerCasos managerCasos) {
//        this.managerCasos = managerCasos;
//    }
    
    /**
     * @return the config
     */
    public String getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(String config) {
        this.config = config;
    }
}
