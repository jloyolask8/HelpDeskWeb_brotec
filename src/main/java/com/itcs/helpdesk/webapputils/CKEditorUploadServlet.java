package com.itcs.helpdesk.webapputils;

import com.itcs.helpdesk.persistence.entities.ArchivoNa;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import org.apache.commons.fileupload.FileItem;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class is responsible for uploading the images from UI to the DB It will
 * provide as response the callback method and URL for the image that has been
 * uploaded
 *
 * @author mcristea
 *
 */
@SuppressWarnings("unchecked")
@MultipartConfig(location = "/tmp", maxFileSize = 20848820, maxRequestSize = 418018841, fileSizeThreshold = 1048576)
public class CKEditorUploadServlet extends AbstractServlet {

    private static final long serialVersionUID = -7570633768412575697L;

    private static final Logger LOG = LoggerFactory.getLogger(CKEditorUploadServlet.class);
    private static final String ERROR_FILE_UPLOAD = "An error occurred to the file upload process.";
    private static final String ERROR_NO_FILE_UPLOAD = "No file is present for upload process.";
    private static final String ERROR_INVALID_CALLBACK = "Invalid callback.";
    private static final String CKEDITOR_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final String CKEDITOR_HEADER_NAME = "Cache-Control";
    private static final String CKEDITOR_HEADER_VALUE = "no-cache";

    private static final Pattern PATTERN = Pattern.compile("[\\w\\d]*");

    private String errorMessage = "";

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String schema = request.getParameter(AbstractJPAController.TENANT_PROP_NAME);
        ArchivoNa uploadedFile = new ArchivoNa();
        PrintWriter out = response.getWriter();

        response.setContentType(CKEDITOR_CONTENT_TYPE);
        response.setHeader(CKEDITOR_HEADER_NAME, CKEDITOR_HEADER_VALUE);

//        FileItemFactory factory = new DiskFileItemFactory();
//        ServletFileUpload upload = new ServletFileUpload();
        try {
            

            if (!StringUtils.isEmpty(schema)) {
                if (ServletFileUpload.isMultipartContent(request)) {
//                Map params = ((org.primefaces.webapp.MultipartRequest)request).getParameterMap();
                    FileItem file = ((org.primefaces.webapp.MultipartRequest) request).getFileItem("upload");

//                while (items.hasNext()) {
//                FileItemStream item = items.next();
                    if (file != null) {
                        uploadedFile.setContent(IOUtils.toByteArray(file.getInputStream()));
                        uploadedFile.setContentType(file.getContentType());
                        uploadedFile.setFileName(file.getName());
                        getJpaController(schema).persist(uploadedFile);
                    }
//                }
                }
            }
        } catch (Exception e) {
            errorMessage = ERROR_FILE_UPLOAD;
            LOG.error(errorMessage, e);
        }

//        try {
//            upload.getItemIterator(request);
//            List<FileItem> items = upload.parseRequest(request);
//            if (!items.isEmpty() && items.get(0) != null) {
//                uploadedFile.setContent(items.get(0).get());
//                uploadedFile.setContentType(items.get(0).getContentType());
//                uploadedFile.setFileName(((DiskFileItem) items.get(0)).getName());
//                getJpaController().saveUploadedFile(uploadedFile);
//            } else {
//                errorMessage = ERROR_NO_FILE_UPLOAD;
//            }
//
//        } catch (FileUploadException e) {
//            errorMessage = ERROR_FILE_UPLOAD;
//            LOG.error(errorMessage, e);
//        }
        // CKEditorFuncNum Is the location to display when the callback
        String callback = request.getParameter("CKEditorFuncNum");
        // verify if the callback contains only digits and letters in order to
        // avoid vulnerability on parsing parameter
        if (!PATTERN.matcher(callback).matches()) {
            callback = "";
            errorMessage = ERROR_INVALID_CALLBACK;
        }
        String pathToFile = request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getContextPath()
                + "/ckeditor/getimage?imageId=" + uploadedFile.getId()+"&"+AbstractJPAController.TENANT_PROP_NAME+"="+schema;
//        String pathToFile = request.getContextPath() + "/ckeditor/getimage?imageId=" + uploadedFile.getId();
//        String callbackStr = "<script type=\"text/javascript\"><![CDATA[window.parent.CKEDITOR.tools.callFunction('"
//                + callback + "','" + pathToFile + "','" + errorMessage + "')"+"]]></script>";
        out.println("<script type=\"text/javascript\">");
        out.println("//<![CDATA[");
        out.println("window.parent.CKEDITOR.tools.callFunction(" + callback + ",'" + pathToFile + "','" + errorMessage + "')");
        out.println("//]]>");
        out.println("</script>");
//        out.println(callbackStr);
        out.flush();
        out.close();
    }

}
