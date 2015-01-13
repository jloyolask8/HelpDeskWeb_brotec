/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

/**
 *
 * @author Jonathan
 */
public class DownloadFileServlet extends AbstractServlet {

   

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fileId = request.getParameter("id");
         String schema = request.getParameter(AbstractJPAController.TENANT_PROP_NAME);

        try {
            if (fileId == null || "".equals(fileId)) {
                response.sendError(404, "Debe especificar el id del archivo.");
            }
            Archivo existente = getJpaController(schema).find(Archivo.class, Long.parseLong(fileId));
            if (existente != null) {
                System.out.println("existe archivo con id " + fileId);
                System.out.println(existente.getContentType());
                ServletOutputStream sot = response.getOutputStream();
                response.setContentType(existente.getContentType());                
                BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(existente.getArchivo()));
                int i = 0;
                byte byteArray[] = new byte[4096];
                while ((i = bis.read(byteArray)) != -1) {
                    sot.write(byteArray, 0, i);
                }
                sot.flush();
                sot.close();
                bis.close();
            }else{
                System.out.println("NO existe archivo con id " + fileId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
