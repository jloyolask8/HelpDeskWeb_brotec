/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.quartz;

import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.config.EntityManagerProperties;
//import org.eclipse.persistence.config.EntityManagerProperties;

/**
 *
 * @author jonathan
 */
public class AbstractGoDeskJob {

    protected static final Locale LOCALE_ES_CL = new Locale("es", "CL");

    public static final String ID_CANAL = "IdCanal";
    public static final String TENANT_ID = "tenant";
    public static final String ID_CASO = "idCaso";
    public static final String INTERVAL_SECONDS = "intervalInSeconds";

    protected EntityManager createEntityManager(String tenant) {
        EntityManagerFactory emf = createEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        if (!StringUtils.isEmpty(tenant)) {
            em.setProperty(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenant);
        } else {
            em.setProperty(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, AbstractJPAController.PUBLIC_SCHEMA_NAME);
        }
        return em;
    }

    protected EntityManagerFactory createEntityManagerFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("helpdeskPU");
        return emf;
    }

}
