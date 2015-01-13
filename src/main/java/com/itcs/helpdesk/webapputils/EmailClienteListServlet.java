/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.google.gson.Gson;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.resource.NotSupportedException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.json.JSONArray;

/**
 *
 * @author jonathan
 */
public class EmailClienteListServlet extends AbstractServlet {


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String schema = request.getParameter(AbstractJPAController.TENANT_PROP_NAME);

            if (!StringUtils.isEmpty(schema)) {

                /* TODO output your page here. You may use following sample code. */
                String q = request.getParameter("q");
                if (!StringUtils.isEmpty(schema)) {
                    if (!StringUtils.isEmpty(q)) {
                        out.println(getGSonList(schema, q));
                    } else {
                        out.println(getGSonList(schema));
                    }
                }
            }
        } finally {
            out.close();
        }

        try {
            /* TODO output your page here. You may use following sample code. */

//            System.out.println("getTagJSonList(" + q + ")");
        } finally {
            out.close();
        }
    }

    private List<EmailCliente> getDataList(String schema, String q) throws NotSupportedException, ClassNotFoundException {
        if (q == null) {
            return (List<EmailCliente>) new LinkedList<EmailCliente>();//empty getJpaController(schema).findAll(EmailCliente.class);
        } else {
            Vista vista = new Vista(EmailCliente.class);
            FiltroVista filter1 = new FiltroVista(1);
            filter1.setIdCampo("emailCliente");
            filter1.setIdComparador(EnumTipoComparacion.CO.getTipoComparacion());
            filter1.setIdVista(vista);
            filter1.setValor(q);
            if (vista.getFiltrosVistaList() == null) {
                vista.setFiltrosVistaList(new ArrayList<FiltroVista>());
            }
            vista.getFiltrosVistaList().add(filter1);
            return (List<EmailCliente>) getJpaController(schema).findEntities(vista, 10, 0, new OrderBy("emailCliente"), null);

//            return (List<EmailCliente>) getJpaController(schema).findEntities(EmailCliente.class, vista, 10, 0, new OrderBy("emailCliente"), null);
        }

    }

    private String getGSonList(String schema) {
        return getGSonList(schema, null);
    }

    private String getGSonList(String schema, String pattern) {
        Gson gson = new Gson();
        try {

            //JSONArray list = new JSONArray();
            List<SimpleEmailCliente> lista = new ArrayList();
            for (EmailCliente emailCliente : getDataList(schema, pattern)) {

                lista.add(new SimpleEmailCliente(emailCliente.getEmailCliente(), emailCliente.getCliente() != null ? emailCliente.getCliente().getCapitalName() : ""));
            }
            return gson.toJson(lista);
        } catch (NotSupportedException ex) {
            Logger.getLogger(EmailClienteListServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EmailClienteListServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
//        return new JSONArray().toString();
        return gson.toJson(Collections.EMPTY_LIST);
    }

    private String getJSonList(String schema, String pattern) {
        try {
            JSONArray list = new JSONArray(getDataList(schema, pattern));
            return list.toString();
        } catch (NotSupportedException ex) {
            Logger.getLogger(EmailClienteListServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EmailClienteListServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray().toString();
    }

    private String getJSonList(String schema) {
        JSONArray list = new JSONArray((List<EmailCliente>) getJpaController(schema).findAll(EmailCliente.class));
        return list.toString();
    }

   
}

class SimpleEmailCliente {

    String email;
    String nombre;

    public SimpleEmailCliente(String email, String nombre) {
        this.email = email;
        this.nombre = nombre;
    }
}
