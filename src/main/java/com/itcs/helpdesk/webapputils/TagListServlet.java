/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.itcs.helpdesk.persistence.entities.Etiqueta;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
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
public class TagListServlet extends AbstractServlet {

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
                String user = request.getParameter("u");
                if (q != null && !StringUtils.isEmpty(q)) {
                    out.println(getTagJSonList(schema, q, user));
                } else {
                    out.println(getTagJSonList(schema));
                }
                System.out.println("getTagJSonList(" + q + ")");
            }
        } finally {
            out.close();
        }

    }

    public String getTagJSonList(String schema, String pattern, String idUsuario) {
        JSONArray list = new JSONArray((List<Etiqueta>) getJpaController(schema).findEtiquetasLike(pattern, idUsuario));
        return list.toString();
    }

    public String getTagJSonList(String schema) {
        JSONArray list = new JSONArray((List<Etiqueta>) getJpaController(schema).findAll(Etiqueta.class));
        return list.toString();
    }

  

}
