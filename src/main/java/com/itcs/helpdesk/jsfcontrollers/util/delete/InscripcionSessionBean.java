/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util.delete;

import com.itcs.commons.email.impl.NoReplySystemMailSender;
import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.entities.Attachment;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.util.HtmlUtils;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webapputils.Theme;
import com.itcs.helpdesk.webservices.DatosCaso;
//import com.itcs.helpdesk.webservices.WSCasos_Service;
//import com.itcs.helpdesk.webservices.WsResponse;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "inscripcionSessionBean")
@SessionScoped
public class InscripcionSessionBean extends AbstractManagedBean<Caso> implements Serializable {

//    @WebServiceRef(wsdlLocation = "http://apps.itcs.cl/helpdesk/WSCasos?wsdl")
//    @WebServiceRef(wsdlLocation = "http://dev.itcs.cl/helpdesk_demo/WSCasos?wsdl")
//    @WebServiceRef(wsdlLocation = "http://apps.itcs.cl:28080/brotec-icafal/WSCasos?wsdl")//brotec prod!
//    private WSCasos_Service service;
//    private DatosCaso datos;
    private FormCaso datos;
  
    @ManagedProperty(value = "#{cuposHorarioBean}")
    private CuposHorarioBean cuposHorarioBean;
//    private List<AttachmentWrapper> attachmentWrapperList = new ArrayList<AttachmentWrapper>();
//    private String idFileDelete = "";
    private Producto proyecto = null;
    private String tipoConsulta = null;
    private String emailCliente;
    private EmailCliente cliente;
    private Long numCaso;
    private Caso selected = null;
    private List<Nota> listaActividadesOrdenada = null;
    private String textoNota = "";
    private String htmlToView = null;
    private List<Theme> advancedThemes;
    private final static String DEFAULT_THEME = "humanity";//or bootstrap
    private List<Caso> casosByRut = new ArrayList<Caso>();
    private boolean runningEmbedded = false;
    private String rut;
    private String tema;
    private String descripcion;
    private TipoCaso tipoCaso;

    @PostConstruct
    public void init() {

        advancedThemes = new ArrayList<Theme>();
        advancedThemes.add(new Theme("afternoon", "afternoon.png"));
        advancedThemes.add(new Theme("afterwork", "afterwork.png"));
        advancedThemes.add(new Theme("aristo", "aristo.png"));
        advancedThemes.add(new Theme("black-tie", "black-tie.png"));
        advancedThemes.add(new Theme("blitzer", "blitzer.png"));
        advancedThemes.add(new Theme("bluesky", "bluesky.png"));
        advancedThemes.add(new Theme("bootstrap", "bootstrap.png"));
        advancedThemes.add(new Theme("casablanca", "casablanca.png"));
        advancedThemes.add(new Theme("cruze", "cruze.png"));
        advancedThemes.add(new Theme("cupertino", "cupertino.png"));
//        advancedThemes.add(new Theme("dark-hive", "dark-hive.png"));
        advancedThemes.add(new Theme("dot-luv", "dot-luv.png"));
//        advancedThemes.add(new Theme("eggplant", "eggplant.png"));
        advancedThemes.add(new Theme("excite-bike", "excite-bike.png"));
        advancedThemes.add(new Theme("flick", "flick.png"));
        advancedThemes.add(new Theme("glass-x", "glass-x.png"));
        advancedThemes.add(new Theme("home", "home.png"));
        advancedThemes.add(new Theme("hot-sneaks", "hot-sneaks.png"));
        advancedThemes.add(new Theme("humanity", "humanity.png"));
//        advancedThemes.add(new Theme("midnight", "midnight.png"));
//        advancedThemes.add(new Theme("mint-choc", "mint-choc.png"));
//        advancedThemes.add(new Theme("overcast", "overcast.png"));
        advancedThemes.add(new Theme("pepper-grinder", "pepper-grinder.png"));
        advancedThemes.add(new Theme("redmond", "redmond.png"));
        advancedThemes.add(new Theme("rocket", "rocket.png"));
        advancedThemes.add(new Theme("sam", "sam.png"));
        advancedThemes.add(new Theme("smoothness", "smoothness.png"));
//        advancedThemes.add(new Theme("south-street", "south-street.png"));
        advancedThemes.add(new Theme("start", "start.png"));
        advancedThemes.add(new Theme("sunny", "sunny.png"));
//        advancedThemes.add(new Theme("swanky-purse", "swanky-purse.png"));
//        advancedThemes.add(new Theme("trontastic", "trontastic.png"));
        advancedThemes.add(new Theme("ui-darkness", "ui-darkness.png"));
//        advancedThemes.add(new Theme("ui-lightness", "ui-lightness.png"));
    }

    /**
     * Creates a new instance of InscripcionSessionBean
     */
    public InscripcionSessionBean() {
//        datos = new DatosCaso();
        super(Caso.class);
        datos = new FormCaso();
    }

   

    public StreamedContent getLogo() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
                // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
                return new DefaultStreamedContent();
            } else {
                Archivo archivo = getJpaController().getArchivoFindByIdAttachment(0L);
                System.out.println("archivo found:" + archivo.getContentType());
                return new DefaultStreamedContent(
                        new ByteArrayInputStream(archivo.getArchivo()), archivo.getContentType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultStreamedContent();
        }
    }

    public void initializeData(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("initializeData()...");
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkProyectoParam(req);
    }

    public void initializeDataProductTipo(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("initializeData()...");
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkProyectoParam(req);
        checkTipoCasoParam(req);
    }

    private void checkTipoCasoParam(HttpServletRequest req) {
        String tipoCasoId = req.getParameter("tipo");
        if (tipoCasoId != null && !StringUtils.isEmpty(tipoCasoId)) {
            //Buscar casos por rut
            TipoCaso tipo = getJpaController().find(TipoCaso.class, tipoCasoId);
            if (tipo != null) {
                //There is clients here
                this.tipoCaso = tipo;
                datos.setTipoCaso(tipo.getIdTipoCaso());

            } else {
                //show error
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "El Tipo de Caso " + tipoCasoId + " no esta registrado en nuestra base de datos.",
                        "Favor revisar si el dato esta correcto.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
//                navigateToView("error");
                datos.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso().getIdTipoCaso());
            }
        } else {
            this.tipoCaso = EnumTipoCaso.CONTACTO.getTipoCaso();
            datos.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso().getIdTipoCaso());
        }
    }

    public void initializeDataEmbeddedForm(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("initializeDataEmbeddedForm()...");
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkProyectoParam(req);

        String rut_ = req.getParameter("rut");
        if (rut_ != null && !StringUtils.isEmpty(rut_)) {
            //Buscar casos por rut
            Cliente clienteConEseRut = getJpaController().getClienteJpaController().findByRut(rut_);
            if (clienteConEseRut != null) {
                //There is clients here
                this.rut = clienteConEseRut.getRut();

            } else {
                //show error
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "El rut " + rut_ + " no esta registrado en nuestra base de datos.",
                        "Favor revisar si el rut esta correcto.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
//                navigateToView("error");
            }
        } else {
            if (!(this.rut != null && !this.rut.isEmpty())) {
                //show error
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "El rut es requerido.",
                        "");
                FacesContext.getCurrentInstance().addMessage(null, msg);
//                navigateToView("error");
            }

        }

        String tema_ = req.getParameter("tema");
        if (tema_ != null && !StringUtils.isEmpty(tema_)) {
            this.tema = tema_;
        } else {
            //show error
            if (!(this.tema != null && !this.tema.isEmpty())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "El tema es requerido.",
                        "");
                FacesContext.getCurrentInstance().addMessage(null, msg);
//                navigateToView("error");
            }

        }

    }

    private void navigateToView(String viewId) {
        ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
        nav.performNavigation(viewId);
    }

  

    public void updateNotas() {
        checkSession(null);
    }

    public void checkAccessToCasosList(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("checkAccessToCasosList()...");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkEmbeddedParam(request);
        String rut_req = request.getParameter("rut");
        if (rut_req != null && !StringUtils.isEmpty(rut_req)) {
            //Buscar casos por rut

            Cliente clienteConEseRut = getJpaController().getClienteJpaController().findByRut(rut_req);
            if (clienteConEseRut != null) {
                //There is clients here
                casosByRut = new ArrayList<Caso>();
                for (EmailCliente emailCliente1 : clienteConEseRut.getEmailClienteList()) {
                    if (emailCliente1 != null && emailCliente1.getEmailCliente() != null) {
                        this.setEmailCliente(emailCliente1.getEmailCliente());
                        casosByRut.addAll(getJpaController().getCasoFindByEmailCliente(emailCliente));
                        this.cliente = emailCliente1;
                    }
                }

            } else {
                //cliente no existe
                //show error
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "El rut " + rut_req + " no esta registrado en nuestra base de datos.",
                        "Favor revisar si el rut esta correcto.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                navigateToView("error");
            }
        } else {
            if (emailCliente != null && !emailCliente.isEmpty()) {
                setCasosByRut(getJpaController().getCasoFindByEmailCliente(emailCliente));
            }
        }
    }

    public void checkSession(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("checkSession()...");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        checkEmbeddedParam(request);
        String idCaso = request.getParameter("idCaso");
        String emailCliente_ = request.getParameter("emailCliente");
        if (idCaso != null && !StringUtils.isEmpty(idCaso) && emailCliente_ != null && !StringUtils.isEmpty(emailCliente_)) {
            Caso caso = getJpaController().findCasoByIdEmailCliente(emailCliente_, Long.valueOf(idCaso));
            if (caso != null) {
                //Se encontro un caso      
                System.out.println("Se encontro caso:" + idCaso);
                this.setSelected(caso);
                this.setEmailCliente(emailCliente_);
                this.cliente = caso.getEmailCliente();
                this.setNumCaso(Long.valueOf(idCaso));
                redirectToView("ticket");
            }
//           else {
//                //No se encontro un caso
//                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
//                        "El Email y número de caso no coinciden con ningún caso en nuestros sistemas",
//                        "Favor revisar el email o número de caso");
//                FacesContext.getCurrentInstance().addMessage(null, msg);
//            }
        }

        if (selected == null) {
            //No hay caso seleccionado.
            redirectToView("login");
        }
//        else {
//            selected = getJpaController().findCasoByIdEmailCliente(getEmailCliente(), getNumCaso());
//        }

    }

//    private WsResponse crearCasoDesdeFormulario(DatosCaso datosCaso) {
//        WSCasos port = service.getWSCasosPort();
//        return port.crearCasoDesdeFormulario(datosCaso);
//    }
    public void enviarInscripcion(ActionEvent e) {

//        EmailCliente emailClienteEntity = getJpaController().getEmailClienteFindByEmail(datos.getEmail());
//        if (emailClienteEntity == null) {
        //no existe ek email, puede ser que con el rut lo encontremos.
        Cliente clienteEntity = getJpaController().getClienteJpaController().findByRut(datos.getRut());
        if (clienteEntity == null) {
            //this dude is not a client!
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Lo sentimos este evento es solo para clientes, Ud. no está registrado como cliente de la Inmobiliaria. Si ésta información es incorrecta favor notifíquenos enviando un mail a sac@brotec-icafal.cl.",
                            ""));
            return;
        } else {
            //use this one
        }
//        } else {
        //use this one
//        }

        if (!cuposHorarioBean.getInscritos().containsKey(datos.getRut())) {
            datos.setDescripcion("[Inscripción] Horario: " + datos.getHorario() + "<br/>Email:" + datos.getEmail() + ", Rut: " + datos.getRut());

            if (getProyecto() != null) {
                getDatos().setProducto(getProyecto().getIdProducto());
            }
            String mensajeRetorno;

            DatosCaso datos_ = new DatosCaso();
            datos_.setAsunto("Horario: " + datos.getHorario());
            datos_.setDescripcion(datos.getDescripcion());
            datos_.setRut(datos.getRut());
            datos_.setEmail(datos.getEmail());
            datos_.setProducto(datos.getProducto());
            datos_.setTelefono(datos.getTelefono());
            datos_.setTipoCaso("inscripcion");
//            datos_.setIdArea("Venta Proyecto - SMP");
            try {
                Caso caso = getManagerCasos().crearCaso(datos_, EnumCanal.SISTEMA.getCanal());
                if (caso != null) {
//                    mensajeRetorno = "Caso creado con éxito. ID caso " + caso.getIdCaso() + ". Revisaremos su consulta y lo contactaremos a la brevedad.";

                    cuposHorarioBean.decrementCupo(datos.getRut(), datos.getEmail(), datos.getHorario());

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Su solicitud de inscripción fué enviada exitósamente. Revisaremos sus datos y le contactarémos luego!",
                                    ""));
                    setDatos(new FormCaso());

                } else {
                    mensajeRetorno = "En este momento no podemos atenderlo. Favor intente más tarde...";
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, mensajeRetorno,
                                    ""));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                mensajeRetorno = "En este momento no podemos atenderlo. Favor intente más tarde.";
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, mensajeRetorno,
                                ""));
            }

        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ud. ya está inscrito.",
                            ""));
        }

    }

//    public void enviar(ActionEvent e) {
//
//        System.out.println("Datos:" + getDatos());
//        if (getProyecto() != null) {
//            getDatos().setProducto(getProyecto().getIdProducto());
//        }
//        String mensajeRetorno;
//
//        DatosCaso datos_ = new DatosCaso();
//        datos_.setApellidos(datos.getApellidos());
//        datos_.setDescripcion(datos.getDescripcion());
//        datos_.setEmail(datos.getEmail());
//        datos_.setNombre(datos.getNombre());
//        datos_.setProducto(datos.getProducto());
//        datos_.setRut(datos.getRut());
//        datos_.setTelefono(datos.getTelefono());
//        datos_.setTipoCaso(datos.getTipoCaso());
//
//        WsResponse respuesta = crearCasoDesdeFormulario(datos_);
//        mensajeRetorno = respuesta.getMensaje();
//
//        System.out.println("Respuesta: " + mensajeRetorno);
//
//        if (respuesta.getCodigo().equalsIgnoreCase("0")) {
//            //Error
//            Log.createLogger(this.getClass().getName()).logSevere("Error al llamar al WSCasos  = " + respuesta.getMensaje());
//
//            if (respuesta.getMensaje().length() > 35) {
//                mensajeRetorno = mensajeRetorno.substring(0, 31);
//            }
//            FacesContext.getCurrentInstance().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, mensajeRetorno, mensajeRetorno));
//
//        } else {
//            FacesContext.getCurrentInstance().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_INFO, mensajeRetorno,
//                            "  Revisaremos tus datos y te llamaremos luego!"));
//            setDatos(new FormCaso());
//            navigateToView("success");
//        }
//    }
//
//    public void enviarSimple(ActionEvent e) {
//
//        if ((this.rut != null && !this.rut.isEmpty())) {
//            String mensajeRetorno;
//            WSCasos port = service.getWSCasosPort();
//            WsResponse respuesta = port.crearCasoCliente(rut, tema, null, getProyecto() != null ? getProyecto().getIdProducto() : null, descripcion);
//            mensajeRetorno = respuesta.getMensaje();
//
//            System.out.println("Respuesta: " + mensajeRetorno);
//
//            if (respuesta.getCodigo().equalsIgnoreCase("0")) {
//                //Error
//                if (respuesta.getMensaje().length() > 35) {
//                    mensajeRetorno = mensajeRetorno.substring(0, 31);
//                }
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR, mensajeRetorno, mensajeRetorno));
////            navigateToView("error");
//
//            } else {
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_INFO, mensajeRetorno,
//                                "  Revisaremos tus datos y te llamaremos luego!"));
////            setRut("");
////            setTema("");
//                setDescripcion("");
////            navigateToView("success");
//            }
//        }
//
//    }

    public void formateaRutFiltro() {
        System.out.println("------------AjaxBehaviorEvent...............................");
        if (null != getDatos().getRut()) {
            String rutFormateado = UtilesRut.formatear(getDatos().getRut().toString());
            getDatos().setRut(rutFormateado);
            if (!getDatos().getRut().isEmpty()) {
                if (!UtilesRut.validar(getDatos().getRut())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "El Rut ingresado no es válido.", ""));
                }
            }
        }
    }

//    private WsResponse crearCasoDesdeFormulario(DatosCaso datosCaso) {
//        WSCasos port = service.getWSCasosPort();
//        return port.crearCasoDesdeFormulario(datosCaso);
//    }
    public SelectItem[] getProductItemsAvailableSelectOne() {

//         return JsfUtil.getSelectItems(getJpaController().getProductoFindAll(), false);
        List<Producto> products = getJpaController().getProductoFindAll();
        SelectItem[] items = new SelectItem[products.size()];
        int i = 0;
        for (Producto producto : products) {
            items[i++] = new SelectItem(producto.getIdProducto(), producto.getNombre());
        }
        return items;
        /**
         * <option value="1">Santa Mar&iacute;a de Valle Grande</option> <option
         * value="2">Jardines de Santa Mar&iacute;a</option> <option
         * value="3">Santa Mar&iacute;a del Pe&ntilde;&oacute;n</option> <option
         * value="4">Ciudad del Sol</option> <option value="5">Palmas de
         * Tocornal</option> <option value="6">Palmas del Parque
         * Almagro</option> <option value="7">Piedra Roja</option>
         */
//        items[1] = new SelectItem("1", "Santa Maria de Valle Grande");
//        items[2] = new SelectItem("2", "Jardines de Santa María");
//        items[3] = new SelectItem("3", "Santa María del Peñón");
//        items[4] = new SelectItem("4", "Ciudad del Sol");
//        items[0] = new SelectItem("5", "Palmas de Tocornal");
//        items[5] = new SelectItem("6", "Palmas del Parque Almagro");
//        items[6] = new SelectItem("7", "Piedra Roja");
//
//        return items;
    }

//    public static SelectItem[] getProductItems(boolean selectOne) {
//
//        List<String> entities = new ArrayList<String>();
//        for (int i = 0; i < 15; i++) {
////            entities.add("PROYECTO TOCORNAL");
//        }
//
//        int size = selectOne ? entities.size() + 1 : entities.size();
//        SelectItem[] items = new SelectItem[size];
//        int i = 0;
//        if (selectOne) {
//            items[0] = new SelectItem("", "Seleccione Proyecto");
//            i++;
//        }
//        for (Object x : entities) {
//            items[i++] = new SelectItem(x, x.toString());
//        }
//        return items;
//    }
    public String nombreArchivoParaDesplegar(String nombreOriginal) {
        int max = 22;
        if (nombreOriginal.length() >= max) {
            StringBuilder sbuilder = new StringBuilder(nombreOriginal.substring(0, max - 3));
            sbuilder.append("...");
            return sbuilder.toString().toLowerCase();
        }
        return nombreOriginal.toLowerCase();
    }

//    public void handleFileUpload(FileUploadEvent event) {
//        try {
//            String nombre = event.getFile().getFileName();
//            nombre = nombre.substring(nombre.lastIndexOf(File.separator) + 1);
//            if (nombre.lastIndexOf('\\') > 0) {
//                nombre = nombre.substring(nombre.lastIndexOf('\\') + 1);
//            }
//            InputStream is = event.getFile().getInputstream();
//            long size = event.getFile().getSize();
//            byte[] bytearray = new byte[(int) size];
//            is.read(bytearray);
//
//            Archivo archivo = new Archivo();
//            archivo.setArchivo(bytearray);
//
////        List col = caso.getAttachmentList();
//
//            Attachment attach = new Attachment();
////        attach.setIdCaso(caso);
//            attach.setNombreArchivo(nombre);
//            attach.setMimeType(event.getFile().getContentType());
////            attach.setContentId(null);
//
////        getJpaController().persistAttachment(attach);
//            AttachmentWrapper adjunto = new AttachmentWrapper(archivo, attach);
//            getAttachmentWrapperList().add(adjunto);
////        caso.setAttachmentList(col);
////        archivo.setIdAttachment(attach.getIdAttachment());
////        getJpaController().persistArchivo(archivo);
////        getJpaController().persistAuditLog(createLogReg(caso, "Archivo subido", nombre, ""));
//
////            getManagerCasos().crearAdjunto(bytearray, null, current, nombre, event.getFile().getContentType());
//
//            JsfUtil.addSuccessMessage("Archivo " + nombre + " subido con exito");
//            System.out.println("Archivo " + nombre + " subido con exito");
//            System.out.println("list=" + getAttachmentWrapperList().size());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
//    /**
//     * @return the attachmentWrapperList
//     */
//    public List<AttachmentWrapper> getAttachmentWrapperList() {
//        return attachmentWrapperList;
//    }
//
//    /**
//     * @param attachmentWrapperList the attachmentWrapperList to set
//     */
//    public void setAttachmentWrapperList(List<AttachmentWrapper> attachmentWrapperList) {
//        this.attachmentWrapperList = attachmentWrapperList;
//    }
//    public void deleteAttachment(ActionEvent actionEvent) {
//        try {
//            for (AttachmentWrapper attachmentWrapper : attachmentWrapperList) {
//                if (attachmentWrapper.getAttachment().getNombreArchivo().equalsIgnoreCase(idFileDelete)) {
//                    attachmentWrapperList.remove(attachmentWrapper);
//                    break;
//                }
//            }
//
//        } catch (Exception e) {
//            JsfUtil.addSuccessMessage("No se ha podido borrar el archivo");
//            e.printStackTrace();
//        }
//    }
//    /**
//     * @return the idFileDelete
//     */
//    public String getIdFileDelete() {
//        return idFileDelete;
//    }
//
//    /**
//     * @param idFileDelete the idFileDelete to set
//     */
//    public void setIdFileDelete(String idFileDelete) {
//        this.idFileDelete = idFileDelete;
//    }
    /**
     * @return the proyecto
     */
    public Producto getProyecto() {
        return proyecto;
    }

    /**
     * @param proyecto the proyecto to set
     */
    public void setProyecto(Producto proyecto) {
        this.proyecto = proyecto;
    }

    //----------------------------------------------------------------------------
//    public List<Attachment> datosAdjuntosNotEmbedded() {
//        try {
//            return getJpaController().getAttachmentWOContentId(getSelected());
//        } catch (Exception e) {
//            return new ArrayList<Attachment>();
//        }
//    }

    /**
     * Envia el archivo al componente de PrimeFace filoDownload
     *
     * @param id Identificador de Attachment
     * @param modo Modo de download, 0=inmediato, 1=pregunta a usuario
     * @return
     */
    public StreamedContent bajarArchivo(String id) {
        try {
            Attachment att = getJpaController().getAttachmentFindByIdAttachment(new Long(id));

            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(att.getIdAttachment());
            return new DefaultStreamedContent(
                    new ByteArrayInputStream(archivo.getArchivo()), att.getMimeType(), att.getNombreArchivo());
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage("Ocurrió un error, favor intente nuevamente mas tarde.");
            return new DefaultStreamedContent();
        }
    }

    public String login() {

        Caso caso = getJpaController().findCasoByIdEmailCliente(emailCliente, numCaso);
        if (caso != null) {
            //Se encontro un caso          
            this.setSelected(caso);
            return "ticket";
        } else {
            //No se encontro un caso
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "El Email y número de caso no coinciden con ningún caso en nuestros sistemas", "Favor revisar el email o número de caso");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

    }

    public String prepareViewHtml(String html) {
        setHtmlToView(replaceEmbeddedImages(html));
        return "ViewHtml";
    }

    private String createEmbeddedImage(String contentId) {
        try {
            Attachment att = getJpaController().getAttachmentFindByContentId('<' + contentId + '>', this.selected);
            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(att.getIdAttachment());
            String base64Image = Base64.encodeBase64String(archivo.getArchivo());
            return base64Image;
        } catch (Exception ex) {
//            Log.createLogger(this.getClass().getName()).log(Level.WARNING, "no se encuentra el archivo: "+contentId, ex);
            ex.printStackTrace();
        }
        return null;
    }

    private String replaceEmbeddedImages(String html) {
        String pattern = "src=\"cid:";
        int index = html.indexOf(pattern);
        int endIndex;
        while (index > -1) {
            index += pattern.length();
            endIndex = html.indexOf("\"", index);
            String contentId = html.substring(index, endIndex);
            String base64Image = createEmbeddedImage(contentId);
            if (base64Image != null) {
                html = html.replace("cid:" + contentId, "data:image/png;base64," + base64Image);
            }
            index = html.indexOf("src=\"cid:", endIndex);
        }
        //src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIA..."
        return html;
    }

    public String parseHtmlToText(String textoTxt) {
        textoTxt = HtmlUtils.stripInvalidMarkup(textoTxt);
        return textoTxt;
    }

    public String crearNotaCliente() {
        try {
            Nota nota = new Nota();
            nota.setFechaCreacion(Calendar.getInstance().getTime());
//            int id = GenerateID.getCurrentStampInteger() + current.getIdCaso().intValue();
//            nota.setIdNota(id);
            nota.setEnviadoPor(emailCliente);
            nota.setCreadaPor(EnumUsuariosBase.SISTEMA.getUsuario());
            nota.setIdCaso(getSelected());
            nota.setTexto(getTextoNota());
            nota.setTipoNota(EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota());
            getSelected().setRevisarActualizacion(true);
//            agregarNotaACaso(nota);
            getJpaController().persistNota(nota);

            //return prepareCreate();
        } catch (Exception e) {
            e.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al crear Nota.",
                    "Favor revisar si ud es el cliente de este caso.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return "ticket";
    }

//    private void agregarNotaACaso(Nota nota) throws Exception {
//        getJpaController().persistNota(nota);
////        List<Nota> notas = getSelected().getNotaList();
////        notas.add(nota);
//        getSelected().setFechaModif(Calendar.getInstance().getTime());
////        getSelected().setNotaList(notas);
//        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Nota creada exitosamente.",
//                "Nota creada exitosamente.");
//        FacesContext.getCurrentInstance().addMessage(null, msg);
//        mergeCaso(getSelected(), null);
//        textoNota = "";
//    }
//    private void mergeCaso(Caso caso, AuditLog audit) throws Exception {
//        List<AuditLog> changeLog = new ArrayList<AuditLog>(1);
//        changeLog.add(audit);
//        getJpaController().mergeCaso(caso, changeLog);
//    }
    public String sendCasosByEmail() {

        System.out.println("sendCasosByEmail(" + getEmailCliente() + ")...");

//        List<Usuario> usuarios = getJpaController().getUsuarioFindByEmail(getEmailCliente());
        EmailCliente emailCliente_ = getJpaController().getEmailClienteFindByEmail(getEmailCliente());

        if (emailCliente_ == null || (emailCliente_.getCasoList() == null || emailCliente_.getCasoList().isEmpty())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existen casos creados con su email.",
                    "Favor revisar si ud ha creado un caso con esta direccion de correo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

//      HashMap<Long, String> casosMap = new HashMap<Long, String>();
        StringBuilder sb = new StringBuilder();

        List<Caso> casos = emailCliente_.getCasoList();
        for (Caso caso : casos) {
            sb.append(caso.getIdCaso()).append(" - ").append(caso.getTema()).append("<br/>");
        }

        try {
            String html = "<html>"
                    + "<h1>Resumen de sus Casos</h1><br/>" + sb.toString()
                    + "<br/></html>";
            NoReplySystemMailSender.sendHTML(getEmailCliente(), "Resumen de sus Casos", html, null);

        } catch (Exception ex) {
            Log.createLogger(InscripcionSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Gracias!. Una lista de sus casos será enviada a su casilla de correo.",
                "");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        return null;

    }

    public String logout() {
        setEmailCliente(null);
        cliente = null;
        casosByRut = null;
        setNumCaso(null);
        setSelected(null);
        return "login";
    }

    public List<Nota> getNotasList() {
        try {
//            if (listaActividadesOrdenada == null) {
//                System.out.println("se crea listaActividadesOrdenada");            
            listaActividadesOrdenada = getJpaController().getNotaFindByIdCaso(selected);
//              listaActividadesOrdenada = new ArrayList<Nota>(getSelected().getNotaList());   
            Collections.sort(listaActividadesOrdenada);
//            }
            return listaActividadesOrdenada;
        } catch (Exception e) {
            return new LinkedList();
        }
    }

    public String creaTituloDeNota(Nota nota) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota().equals(nota.getTipoNota())) {
            if (nota.getEnviadoPor() == null) {
                return nota.getTipoNota().getNombre() + " - " + format.format(nota.getFechaCreacion());
            } else {
                return nota.getTipoNota().getNombre() + " - enviado por: " + nota.getEnviadoPor() + " - " + format.format(nota.getFechaCreacion());
            }
        }
        return nota.getTipoNota().getNombre() + " - creada por " + nota.getCreadaPor().getIdUsuario() + " - " + format.format(nota.getFechaCreacion());
    }

    public String creaAutorDeNota(Nota nota) {
        if (EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota().equals(nota.getTipoNota())) {
            if (nota.getEnviadoPor() == null) {
                return "Cliente Anonimo";
            } else {
                return nota.getEnviadoPor();
            }
        }
        return nota.getCreadaPor().getCapitalName();
    }

    public boolean isNotaCliente(Nota nota) {
        if (EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota().equals(nota.getTipoNota())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isVisibleByCliente(Nota nota) {
        try {
            if (EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota().equals(nota.getTipoNota())
                    || EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota().equals(nota.getTipoNota())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Date getNow() {
        return new java.util.Date();
    }

    public boolean casoCerrado() {
        if (selected.getIdEstado() != null) {
            if (selected.getIdEstado().equals(EnumEstadoCaso.CERRADO.getEstado())) {
                return true;
            }
        }
        return false;
    }

    public String mergeCliente() {
        try {
            getJpaController().mergeCliente(cliente.getCliente());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cliente actualizado exitosamente", "Cliente actualizado exitosamente");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Ocurrio un error al tratar de guardar los datos.", "Cliente no fue actualizado.");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            return null;
        }
    }

    public String prepareEdit() throws Exception {
        if (getSelected() == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Se requiere que seleccione un caso para mostrar.", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        } //else {
//            setSelected(getJpaController().getCasoFindByIdCaso(getSelected().getIdCaso()));
        //}
        setListaActividadesOrdenada(null);
        return "ticket";
        //return "Edit";
    }

    public String prepareEdit(Caso caso) throws Exception {
        setSelected(caso);
        setNumCaso(caso.getIdCaso());
        return prepareEdit();
    }

    public void formateaRutFiltro2() {
        try {
            String rutFormateado = UtilesRut.formatear(getCliente().getCliente().getRut());
            getCliente().getCliente().setRut(rutFormateado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void formateaRutFiltro3() {
        try {
//            final String rutInput = getSelected().getEmailCliente().getCliente().getRut();
            String rutCliente_wizard = getDatos().getRut();
            if (rutCliente_wizard != null && !org.apache.commons.lang3.StringUtils.isEmpty(rutCliente_wizard)) {
                String formattedRut = UtilesRut.formatear(rutCliente_wizard);
                getDatos().setRut(formattedRut);
                Cliente c = getJpaController().getClienteJpaController().findByRut(formattedRut);
                if (c != null) {//this client exists
                    if (c.getEmailClienteList() != null && !c.getEmailClienteList().isEmpty()) {
                        EmailCliente emailCliente = c.getEmailClienteList().get(0);
                        getDatos().setEmail(emailCliente.getEmailCliente().toLowerCase());
                        getDatos().setTelefono(c.getFono1());

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Theme> getAdvancedThemes() {
        return advancedThemes;
    }

    public String getTheme() {
        if (getCliente() != null) {
            if (getCliente().getCliente().getTheme() != null && !StringUtils.isEmpty(getCliente().getCliente().getTheme())) {
                return getCliente().getCliente().getTheme();
            } else {
                return DEFAULT_THEME;
            }
        } else {
            return DEFAULT_THEME;
        }
    }

    public void saveTheme() {
    }

    /**
     * @return the numCaso
     */
    public Long getNumCaso() {
        return numCaso;
    }

    /**
     * @param numCaso the numCaso to set
     */
    public void setNumCaso(Long numCaso) {
        this.numCaso = numCaso;
    }

    /**
     * @return the selected
     */
    public Caso getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(Caso selected) {
        this.selected = selected;
    }

    /**
     * @return the listaActividadesOrdenada
     */
    public List<Nota> getListaActividadesOrdenada() {
        return listaActividadesOrdenada;
    }

    /**
     * @param listaActividadesOrdenada the listaActividadesOrdenada to set
     */
    public void setListaActividadesOrdenada(ArrayList<Nota> listaActividadesOrdenada) {
        this.listaActividadesOrdenada = listaActividadesOrdenada;
    }

    /**
     * @return the textoNota
     */
    public String getTextoNota() {
        return textoNota;
    }

    /**
     * @param textoNota the textoNota to set
     */
    public void setTextoNota(String textoNota) {
        this.textoNota = textoNota;
    }

    /**
     * @return the htmlToView
     */
    public String getHtmlToView() {
        return htmlToView;
    }

    /**
     * @param htmlToView the htmlToView to set
     */
    public void setHtmlToView(String htmlToView) {
        this.htmlToView = htmlToView;
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

    /**
     * @return the datos
     */
    public FormCaso getDatos() {
        return datos;
    }

    /**
     * @param datos the datos to set
     */
    public void setDatos(FormCaso datos) {
        this.datos = datos;
    }

    /**
     * @return the tipoConsulta
     */
    public String getTipoConsulta() {
        return tipoConsulta;
    }

    /**
     * @param tipoConsulta the tipoConsulta to set
     */
    public void setTipoConsulta(String tipoConsulta) {
        this.tipoConsulta = tipoConsulta;
    }

    /**
     * @return the casosByRut
     */
    public List<Caso> getCasosByRut() {
        return casosByRut;
    }

    /**
     * @param casosByRut the casosByRut to set
     */
    public void setCasosByRut(List<Caso> casosByRut) {
        this.casosByRut = casosByRut;
    }

    /**
     * @return the cliente
     */
    public EmailCliente getCliente() {
        return cliente;
    }

    /**
     * @return the runningEmbedded
     */
    public boolean isRunningEmbedded() {
        return runningEmbedded;
    }

    /**
     * @param runningEmbedded the runningEmbedded to set
     */
    public void setRunningEmbedded(boolean runningEmbedded) {
        this.runningEmbedded = runningEmbedded;
    }

    private void checkEmbeddedParam(HttpServletRequest request) {
        String embedded = request.getParameter("embedded");
        if (embedded != null) {
            if (embedded.equalsIgnoreCase("no")) {
                setRunningEmbedded(false);
            } else {
                setRunningEmbedded(true);
            }

        }
//        else{
//            setRunningEmbedded(false);
//        }
    }

    private void checkProyectoParam(HttpServletRequest req) {
        String proy = req.getParameter("proyecto");
        if (proy != null && !proy.isEmpty()) {
            System.out.println("proyecto=" + proy);
            try {
                Producto producto = getJpaController().getProductoFindByIdProducto(proy);
                setProyecto(producto);
                if (getDatos() != null) {
                    if (producto != null) {
                        getDatos().setProducto(producto.getIdProducto());
                    } else {
                        JsfUtil.addErrorMessage("No se ha encontrado el proyecto especificado.");
                    }
                }
            } catch (NoResultException ex) {
                JsfUtil.addErrorMessage("No se ha encontrado el proyecto especificado.");
                Log.createLogger(InscripcionSessionBean.class.getName()).logSevere("No se ha encontrado producto: " + getProyecto());
            }
        }
    }

    /**
     * @return the rut
     */
    public String getRut() {
        return rut;
    }

    /**
     * @param rut the rut to set
     */
    public void setRut(String rut) {
        this.rut = rut;
    }

    /**
     * @return the tema
     */
    public String getTema() {
        return tema;
    }

    /**
     * @param tema the tema to set
     */
    public void setTema(String tema) {
        this.tema = tema;
    }

    /**
     * @return the descripcion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @param descripcion the descripcion to set
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * @param cuposHorarioBean the cuposHorarioBean to set
     */
    public void setCuposHorarioBean(CuposHorarioBean cuposHorarioBean) {
        this.cuposHorarioBean = cuposHorarioBean;
    }

    /**
     * @return the tipoCaso
     */
    public TipoCaso getTipoCaso() {
        return tipoCaso;
    }

    /**
     * @param tipoCaso the tipoCaso to set
     */
    public void setTipoCaso(TipoCaso tipoCaso) {
        this.tipoCaso = tipoCaso;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class CasoDataModel extends ListDataModel<Caso> implements SelectableDataModel<Caso>, Serializable {

    public CasoDataModel() {
        //nothing
    }

    public CasoDataModel(List<Caso> data) {
        super(data);
    }

    @Override
    public Caso getRowData(String rowKey) {
        List<Caso> listOfCaso = (List<Caso>) getWrappedData();

        if (listOfCaso != null) {
            for (Caso obj : listOfCaso) {
                if (obj.getIdCaso().toString().equals(rowKey)) {
                    return obj;
                }
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Caso classname) {
        return classname.getIdCaso();
    }
}
