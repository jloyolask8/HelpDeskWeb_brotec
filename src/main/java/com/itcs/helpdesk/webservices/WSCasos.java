package com.itcs.helpdesk.webservices;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.EstadoCaso;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.persistence.entities.SubEstadoCaso;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumPrioridad;
import com.itcs.helpdesk.persistence.entityenums.EnumSubEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.HtmlUtils;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.RulesEngine;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;

/**
 *
 * @author danilomoya
 */
@WebService(serviceName = "WSCasos")
public class WSCasos {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "helpdeskPU")
    private EntityManagerFactory emf = null;
    private JPAServiceFacade jpaController;
//    private Properties properties;
    private ManagerCasos managerCasos;
//    private ResourceBundle resourceBundle;

    private JPAServiceFacade getJpaController() {
        if (null == jpaController) {
            jpaController = new JPAServiceFacade(utx, emf);
            RulesEngine rulesEngine = new RulesEngine(emf, jpaController);
            jpaController.setCasoChangeListener(rulesEngine);
        }
        return jpaController;
    }

    private ManagerCasos getManagerCasos() {
        if (null == managerCasos) {
            managerCasos = new ManagerCasos(getJpaController());
        }
        return managerCasos;
    }

//    private ResourceBundle getResourceBundle()
//    {
//        if(null == resourceBundle)
//        {
//            this.resourceBundle = ResourceBundle.getBundle("Bundle");
//        }
//        return resourceBundle;
//    }
    /**
     * Web service operation
     */
    @WebMethod(operationName = "crearCasoDesdeFormulario")
    public WSResponse crearCasoDesdeFormulario(@WebParam(name = "datosCaso") DatosCaso datos) {
        WSResponse response = new WSResponse();
        response.setCodigo("0");
        response.setMensaje("Error al intentar crear el caso");
        try {
            Caso caso = getManagerCasos().crearCaso(datos, EnumCanal.WEBSERVICE.getCanal());
            response.setCodigo("1");
//            response.setMensaje("Caso creado con exito, ID CASO[" + caso.getIdCaso() + "]");
            response.setMensaje("Caso creado con éxito. ID caso " + caso.getIdCaso() + ". Revisaremos su consulta y lo contactaremos a la brevedad.");

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            response.setMensaje(response.getMensaje() + '\n' + getStackTrace(e));
        }
        return response;
    }

    private String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "crearCasoCliente")
    public WSResponse crearCasoCliente(@WebParam(name = "rut") final String rut,
            @WebParam(name = "tema") final String tema,
            @WebParam(name = "tipo") final String tipoCaso,
            @WebParam(name = "idProducto") final String idProducto,
            @WebParam(name = "descripcion") final String descripcion) {
        WSResponse response = new WSResponse();
        response.setCodigo("0");
        response.setMensaje("Error al intentar crear el caso");
        try {

            EstadoCaso ec = new EstadoCaso();
            ec.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());

            Caso caso = new Caso();
            caso.setIdCategoria(null);
            caso.setRevisarActualizacion(true);
            caso.setIdEstado(ec);

            Cliente c = getJpaController().getClienteJpaController().findByRut(rut);
            if (c != null) {
                for (EmailCliente emailCliente : c.getEmailClienteList()) {
                    caso.setEmailCliente(emailCliente);
                }
            }

            if (idProducto != null) {
                try {
                    Producto prod = getJpaController().getProductoFindByIdProducto(idProducto);
                    caso.setIdProducto(prod);
                    caso.setTema("[" + prod.getNombre() + "] ");
                } catch (NoResultException ex) {
                    Log.createLogger(WSCasos.class.getName()).logInfo("No se ha encontrador producto: " + idProducto);
                }
            }

            //Tipo de caso:
            if (tipoCaso != null && !tipoCaso.isEmpty()) {
                try {
                    TipoCaso tipo = getJpaController().find(TipoCaso.class, tipoCaso);
                    if (tipo != null) {
                        caso.setTipoCaso(tipo);
                        for (SubEstadoCaso subEstadoCaso : tipo.getSubEstadoCasoList()) {
                            if (subEstadoCaso.isFirst()) {
                                caso.setIdSubEstado(subEstadoCaso);
                            }
                        }
                          caso.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());
//                        if (tipo.getPrioridadList() != null && !tipo.getPrioridadList().isEmpty()) {
//                            caso.setIdPrioridad(tipo.getPrioridadList().get(0));//calculate prio based on some facts please
//                        }
                    }
                } catch (NoResultException ex) {
                    Log.createLogger(WSCasos.class.getName()).logInfo("No se ha encontrado el tipo de caso: " + tipoCaso);
                    caso.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso());
                    caso.setIdSubEstado(EnumSubEstadoCaso.CONTACTO_NUEVO.getSubEstado());
                    caso.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());
                }
            } else {
                caso.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso());
                caso.setIdSubEstado(EnumSubEstadoCaso.CONTACTO_NUEVO.getSubEstado());
                caso.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());
            }

            caso.setIdCanal(EnumCanal.WEBSERVICE.getCanal());

            caso.setDescripcion(descripcion);
            caso.setDescripcionTxt(HtmlUtils.stripInvalidMarkup(descripcion));
            int endIndex = descripcion.length();
            endIndex = (endIndex < 20) ? endIndex : 20;
            caso.setFechaCreacion(Calendar.getInstance().getTime());
            caso.setFechaModif(caso.getFechaCreacion());

            caso.setTema((caso.getTema() != null ? caso.getTema() : "") + "[" + tema + "] " + descripcion.substring(0, endIndex) + "...");

            getManagerCasos().calcularSLA(caso);
            caso.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());

//            caso.setIdArea(EnumAreas.DEFAULT_AREA.getArea());//This is important to know what email received the ticket, but in WS there is no specific email.

            getManagerCasos().persistCaso(caso, getManagerCasos().createLogReg(caso, "Crear", "se crea caso desde WEB SERVICE.", ""));
            getManagerCasos().createLogReg("Crear", "se crea caso desde canal " + caso.getIdCanal().getNombre(), "", caso);
            response.setCodigo("" + caso.getIdCaso());
            response.setMensaje("Caso #" + caso.getIdCaso() + " creado exitósamente.");

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            response.setCodigo("0");
            response.setMensaje(response.getMensaje() + '\n' + getStackTrace(e));
        }
        return response;
    }
}
