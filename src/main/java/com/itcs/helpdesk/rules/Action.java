/*
 * Abstract action to inherit from, An action is executed when a Rules applies to case.
 */
package com.itcs.helpdesk.rules;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.ManagerCasos;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

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

    /**
     * @return the properties
     */
    public Properties getConfigAsProperties() throws IOException {
        Properties props = new Properties();
        // load a properties file for reading  
        props.load(new StringReader(this.config));

        return props;
    }

    public static String getPropertyAsString(Properties prop) throws IOException {
        StringWriter writer = new StringWriter();
        prop.store(new PrintWriter(writer), "");
        return writer.getBuffer().toString();
    }

    /**
     * @param properties the properties to set
     */
    public void setConfigProperties(Properties properties) throws IOException {
        this.config = getPropertyAsString(properties);
    }
}
