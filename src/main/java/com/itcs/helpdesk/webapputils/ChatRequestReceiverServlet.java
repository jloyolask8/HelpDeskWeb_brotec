/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.webapputils;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.RulesEngine;
import com.itcs.helpdesk.webservices.DatosCaso;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
public class ChatRequestReceiverServlet extends AbstractServlet
{

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
        
        final StringBuilder xml = new StringBuilder();
        String schema = request.getParameter(AbstractJPAController.TENANT_PROP_NAME);
        try
        {
            
            final Reader reader = request.getReader();

            char[] buffer = new char[2048];
            for (int len; (len = reader.read(buffer)) != -1;) {
                xml.append(buffer, 0, len);
            }

            //    //System.out.println("\n\n\n\n\n\n\n\nXML:" + xml.toString());
            XStream xstream = new XStream();
            xstream.ignoreUnknownElements();
            xstream.alias("case", Case.class);
            xstream.alias("javascript_variable", Variable.class);
            xstream.alias("operator_variable", Variable.class);
            xstream.alias("transcript", Transcript.class);
            xstream.omitField(Case.class, "languages");
            xstream.omitField(Case.class, "plugins");
            Case caso_xml = (Case) xstream.fromXML(xml.toString());

            ////System.out.println("\n\n\nCaso:" + caso);
            DatosCaso datos = new DatosCaso();
            datos.setEmail(caso_xml.getRequested_by());

            //source_id: The tag source_id has an integer value of 1 when the SnapEngage interaction was a simple message, and of 2 when it was a live chat session.
            if (caso_xml.getSource_id().equals("1")) {
                //Offline
                datos.setDescripcion(caso_xml.getDescription());

                for (Variable variable : caso_xml.getJavascript_variables()) {
                    if (variable.getName().equalsIgnoreCase("Name")) {
                        datos.parseNombre(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("Phone")) {
                        datos.setTelefono(variable.getValue());
                    } else {
                        // datos.setDescripcion(datos.getDescripcion() + "\n" + variable.getName() + "=" + variable.getValue());
                        //System.out.println("Variable Value not handled!!! " + variable.getName() + "=" + variable.getValue());
                    }
                }
            } else if (caso_xml.getSource_id().equals("2")) {
                //Live Chat

                for (Variable variable : caso_xml.getJavascript_variables()) {
                    if (variable.getName().equalsIgnoreCase("Name")) {
                        if (!StringUtils.isEmpty(variable.getValue())) {
                            datos.parseNombre(variable.getValue());
                        }
                    } else if (variable.getName().equalsIgnoreCase("Phone")) {
                        if (!StringUtils.isEmpty(variable.getValue())) {
                            datos.setTelefono(variable.getValue());
                        }
                    } else {
                        // datos.setDescripcion(datos.getDescripcion() + "\n" + variable.getName() + "=" + variable.getValue());
                        //System.out.println("Variable Value not handled!!! " + variable.getName() + "=" + variable.getValue());
                    }
                }

                for (Variable variable : caso_xml.getOperator_variables()) {
                    if (variable.getName().equalsIgnoreCase("email")) {
                        datos.setEmail(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("nombre")) {
                        datos.setNombre(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("apellidos") || variable.getName().equalsIgnoreCase("apellido")) {
                        datos.setApellidos(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("rut")) {
                        datos.setRut(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("telefono")) {
                        datos.setTelefono(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("telefono2")) {
                        datos.setTelefono2(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("comuna")) {
                        datos.setComuna(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("tipo_caso")) {
                        datos.setTipoCaso(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("area") || variable.getName().equalsIgnoreCase("idarea")) {
                        datos.setIdArea(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("producto")) {
                        datos.setProducto(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("proyecto")) {
                        datos.setProducto(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("modelo")) {
                        datos.setModelo(variable.getValue());
                    } else if (variable.getName().equalsIgnoreCase("fecha_compra")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            datos.setFechaEstimadaCompra(sdf.parse(variable.getValue()));
                        } catch (Exception e) {
                            //ignore
                        }

                    } else if (variable.getName().startsWith("credito")) {
                        datos.setCredito((variable.getValue().equalsIgnoreCase("si")) ? true : false);
                    } else if (variable.getName().equalsIgnoreCase("descripcion")) {
                        datos.setDescripcion(variable.getValue());
                    } else {
                        //System.out.println("Variable Value not handled!!! " + variable.getName() + "=" + variable.getValue());
//                      datos.setDescripcion(datos.getDescripcion() + "\n" + variable.getName() + "=" + variable.getValue());
                    }
                }
            } else {
                //Error
            }

            if (StringUtils.isEmpty(datos.getTipoCaso())) {
                datos.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso().getIdTipoCaso());
            }

            if (StringUtils.isEmpty(datos.getDescripcion())) {
                datos.setDescripcion("Caso creado desde el Chat, sin descripcion, favor revisar la conversación que se tuvo con el visitante del sitio.");
            }
            Date fechaCreacion = null;
            try {
                //2010-10-18T01:48:18.623Z
//                String fechaCreacionStr = caso_xml.getCreated_at().substring(0, caso_xml.getCreated_at().indexOf('T')+9);
                if (ApplicationConfig.isAppDebugEnabled()) {
                    Log.createLogger(this.getClass().getName()).logInfo("caso_xml.getCreated_at(): " + caso_xml.getCreated_at());

                }
                SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                fechaCreacion = parserSDF.parse(caso_xml.getCreated_at().replace("Z", "+0000"));
            } catch (ParseException ex) {
                Log.createLogger(this.getClass().getName()).log(Level.WARNING, "xml.toString():\n" + xml.toString(), ex);
                throw ex;
            }

            StringBuilder sb = new StringBuilder();
            sb.append((!StringUtils.isEmpty(datos.getDescripcion()) ? datos.getDescripcion() : ""));
            sb.append("<br/>");
            sb.append(createTranscriptView(caso_xml.getTranscripts()));
            
            datos.setDescripcion( sb.toString() );
            
            JPAServiceFacade aServiceFacade = new JPAServiceFacade(utx, emf, schema);
            ManagerCasos managerCasos = new ManagerCasos(aServiceFacade);
            
            Caso newCaso = managerCasos.crearCaso(datos, EnumCanal.CHAT.getCanal(), fechaCreacion);

            Log.createLogger(this.getClass().getName()).logInfo("CASO CREADO OK DESDE CHAT:" + newCaso.toString());

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL TRATAR DE CREAR CASO DESDE CHAT!", e);
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, xml.toString(), e);
            if (e instanceof ConstraintViolationException) {
                printWhatDaFuckIsGoingOn(e);
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR AL TRATAR DE CREAR CASO DESDE CHAT! Error msg:" + e.getMessage());
        } finally {
            out.close();
        }
    }

    private void printOutContraintViolation(ConstraintViolationException ex, String classname) {
        Set<ConstraintViolation<?>> set = (ex).getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : set) {
            Log.createLogger(classname).logInfo("leafBean class: " + constraintViolation.getLeafBean().getClass());
            Log.createLogger(classname).logInfo("anotacion: " + constraintViolation.getConstraintDescriptor().getAnnotation().toString() + " value:" + constraintViolation.getInvalidValue());
        }
    }

    public void printWhatDaFuckIsGoingOn(Exception ex) {
        if (ex instanceof ConstraintViolationException) {
            printOutContraintViolation((ConstraintViolationException) ex, this.getClass().getName());
        }
        if (ex.getCause() instanceof ConstraintViolationException) {
            printOutContraintViolation((ConstraintViolationException) (ex.getCause()), this.getClass().getName());
        }
        Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "exceptionThreatment", ex);
    }

    private String createTranscriptView(List<Transcript> transcripts) {
        StringBuilder sb = new StringBuilder();
        if (transcripts != null) {
            sb.append("<h2>Transcripción del chat:</h2><br/>");
            for (Transcript t : transcripts) {
                if (t.getAlias() != null) {
                    sb.append("<b>").append(t.getAlias()).append(":</b>").append(" ").append(t.getMessage()).append("<br/>");
                }
            }
        }
        return sb.toString();
    }

   

   
}

@XStreamAlias("transcript")
class Transcript {

    private String id = null;
    private String date = null;
    private String alias = null;
    private String message = null;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}

class Variable {

    private String name;
    private String value;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Variable [");
        builder.append("name=");
        builder.append(name);
        builder.append(", value=");
        builder.append(value);
        builder.append("]");
        return builder.toString();
    }
}

@XStreamAlias("case")
class Case {

    private String id = null;//<id><![CDATA[8b7c3b3b-f5d7-4e58-96d8]]></id>
    private String url = null;//<url>http://www.snapengage.com/viewcase?c=8b7c3b3b-f5d7-4e58-96d8</url>
    private String snapshot_image_url = null;//<snapshot_image_url>http://www.snapengage.com/snapabug/ServiceImage?c=8b7c3b3b-f5d7-4e58-96d8</snapshot_image_url>
    private String requested_by = null;//<requested_by><![CDATA[test@test.com]]></requested_by>
    private String description = null;//<description><![CDATA[Testing the Open API...]]></description>
    private String created_at = null;//<created_at type="datetime">2010-10-18T01:48:18.623Z</created_at>
    private String page_url = null;//<page_url>http://www.snapengage.com/demo/postapi.html></page_url>
    private String ip_address = null;//<ip_address>67.174.108.196</ip_address>
    private String browser = null;//<browser>Chrome (6.0.472.63)</browser>
    private String os = null;//<os>Microsoft Windows 7</os>
    private String country_code = null;//<country_code>US</country_code>
    private String country = null;//<country>United States</country>
    private String region = null;//<region>CO</region>
    private String city = null;//<city><![CDATA[Boulder]]></city>
    private String latitude = null;//<latitude>39.914700</latitude>
    private String longitude = null;//<longitude>-105.080902</longitude>
    private String source_id = null;//<source_id type="integer">2</source_id>
    @XStreamImplicit(itemFieldName = "transcript")
    private List<Transcript> transcripts;
    private Integer chat_waittime = null;
    private Integer chat_duration = null;
    @XStreamImplicit(itemFieldName = "javascript_variable")
    private List<Variable> javascript_variables;
    @XStreamImplicit(itemFieldName = "operator_variable")
    private List<Variable> operator_variables;
    @XStreamOmitField
    private Object languages;
    @XStreamOmitField
    private Object plugins;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Case [");
        builder.append("browser=");
        builder.append(browser);
        builder.append(", chat_duration=");
        builder.append(chat_duration);
        builder.append(", chat_waittime=");
        builder.append(chat_waittime);
        builder.append(", city=");
        builder.append(city);
        builder.append(", country=");
        builder.append(country);
        builder.append(", country_code=");
        builder.append(country_code);
        builder.append(", created_at=");
        builder.append(created_at);
        builder.append(", description=");
        builder.append(description);
        builder.append(", id=");
        builder.append(id);
        builder.append(", ip_address=");
        builder.append(ip_address);
        builder.append(", javascript_variables=");
        builder.append(javascript_variables);
        builder.append(", latitude=");
        builder.append(latitude);
        builder.append(", longitude=");
        builder.append(longitude);
        builder.append(", operator_variables=");
        builder.append(operator_variables);
        builder.append(", os=");
        builder.append(os);
        builder.append(", page_url=");
        builder.append(page_url);
        builder.append(", region=");
        builder.append(region);
        builder.append(", requested_by=");
        builder.append(requested_by);
        builder.append(", snapshot_image_url=");
        builder.append(snapshot_image_url);
        builder.append(", source_id=");
        builder.append(source_id);
        builder.append(", transcripts=");
        builder.append(transcripts);
        builder.append(", url=");
        builder.append(url);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the snapshot_image_url
     */
    public String getSnapshot_image_url() {
        return snapshot_image_url;
    }

    /**
     * @param snapshot_image_url the snapshot_image_url to set
     */
    public void setSnapshot_image_url(String snapshot_image_url) {
        this.snapshot_image_url = snapshot_image_url;
    }

    /**
     * @return the requested_by
     */
    public String getRequested_by() {
        return requested_by;
    }

    /**
     * @param requested_by the requested_by to set
     */
    public void setRequested_by(String requested_by) {
        this.requested_by = requested_by;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the created_at
     */
    public String getCreated_at() {
        return created_at;
    }

    /**
     * @param created_at the created_at to set
     */
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    /**
     * @return the page_url
     */
    public String getPage_url() {
        return page_url;
    }

    /**
     * @param page_url the page_url to set
     */
    public void setPage_url(String page_url) {
        this.page_url = page_url;
    }

    /**
     * @return the ip_address
     */
    public String getIp_address() {
        return ip_address;
    }

    /**
     * @param ip_address the ip_address to set
     */
    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    /**
     * @return the browser
     */
    public String getBrowser() {
        return browser;
    }

    /**
     * @param browser the browser to set
     */
    public void setBrowser(String browser) {
        this.browser = browser;
    }

    /**
     * @return the os
     */
    public String getOs() {
        return os;
    }

    /**
     * @param os the os to set
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * @return the country_code
     */
    public String getCountry_code() {
        return country_code;
    }

    /**
     * @param country_code the country_code to set
     */
    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the source_id
     */
    public String getSource_id() {
        return source_id;
    }

    /**
     * @param source_id the source_id to set
     */
    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

    /**
     * @return the transcripts
     */
    public List<Transcript> getTranscripts() {
        return transcripts;
    }

    /**
     * @param transcripts the transcripts to set
     */
    public void setTranscripts(List<Transcript> transcripts) {
        this.transcripts = transcripts;
    }

    /**
     * @return the chat_waittime
     */
    public Integer getChat_waittime() {
        return chat_waittime;
    }

    /**
     * @param chat_waittime the chat_waittime to set
     */
    public void setChat_waittime(Integer chat_waittime) {
        this.chat_waittime = chat_waittime;
    }

    /**
     * @return the chat_duration
     */
    public Integer getChat_duration() {
        return chat_duration;
    }

    /**
     * @param chat_duration the chat_duration to set
     */
    public void setChat_duration(Integer chat_duration) {
        this.chat_duration = chat_duration;
    }

    /**
     * @return the javascript_variables
     */
    public List<Variable> getJavascript_variables() {
        return javascript_variables;
    }

    /**
     * @param javascript_variables the javascript_variables to set
     */
    public void setJavascript_variables(List<Variable> javascript_variables) {
        this.javascript_variables = javascript_variables;
    }

    /**
     * @return the operator_variables
     */
    public List<Variable> getOperator_variables() {
        return operator_variables;
    }

    /**
     * @param operator_variables the operator_variables to set
     */
    public void setOperator_variables(List<Variable> operator_variables) {
        this.operator_variables = operator_variables;
    }

    /**
     * @return the languages
     */
    public Object getLanguages() {
        return languages;
    }

    /**
     * @param languages the languages to set
     */
    public void setLanguages(Object languages) {
        this.languages = languages;
    }

    /**
     * @return the plugins
     */
    public Object getPlugins() {
        return plugins;
    }

    /**
     * @param plugins the plugins to set
     */
    public void setPlugins(Object plugins) {
        this.plugins = plugins;
    }
}
