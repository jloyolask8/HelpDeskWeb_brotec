package com.itcs.helpdesk.util;

import com.itcs.commons.email.EmailMessage;
import com.itcs.helpdesk.jsfcontrollers.CasoController;
import com.itcs.helpdesk.jsfcontrollers.InputValidationBean;
import com.itcs.helpdesk.jsfcontrollers.UsuarioController;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Attachment;
import com.itcs.helpdesk.persistence.entities.AuditLog;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Caso_;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.ModeloProducto;
import com.itcs.helpdesk.persistence.entities.ModeloProductoPK;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.entities.Prioridad;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.persistence.entities.Recinto;
import com.itcs.helpdesk.persistence.entities.SubEstadoCaso;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumPrioridad;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.quartz.TicketAlertStateChangeJob;
import com.itcs.helpdesk.webservices.DatosCaso;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.MimeUtility;
import javax.persistence.NoResultException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.quartz.SchedulerException;

/**
 *
 * @author jorge
 */
public class ManagerCasos implements Serializable {

    public static final String PATTERN_ID_CASO = "(\\[ref#)(\\d*)(\\])";
    public static final String PATTERN_ID_CASO_LEGACY = "(N°\\[)(\\d+)(\\])";
    public static final String FORMAT_ID_CASO = "[ref#%d]";
    public static final Pattern patternIdCaso = Pattern.compile(PATTERN_ID_CASO, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern patternIdCasoLegacy = Pattern.compile(PATTERN_ID_CASO_LEGACY, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final float PORCENTAJE_POR_VENCER = 0.9f;
    private JPAServiceFacade jpaController;
//    private final ResourceBundle resourceBundle;
//    private long currentTask = 0;
    private EmailCliente emailClient;

    public static String removeAccents(String text) {
        return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public ManagerCasos() {
//        this.resourceBundle = ResourceBundle.getBundle("Bundle");
    }

    public ManagerCasos(JPAServiceFacade jpaController) {
        this.jpaController = jpaController;
//        this.resourceBundle = ResourceBundle.getBundle("Bundle");
    }

    public void setJpaController(JPAServiceFacade jpaController) {
        this.jpaController = jpaController;
    }

    private JPAServiceFacade getJpaController() {
        return jpaController;
    }

    public long countCasosByUsuario(Usuario user) {

        try {
            Vista vista = new Vista(Caso.class);

            vista.setNombre("count");

            FiltroVista filtroOwner = new FiltroVista();
            filtroOwner.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
            filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroOwner.setValor(user.getIdUsuario());
            filtroOwner.setIdVista(vista);

            vista.getFiltrosVistaList().add(filtroOwner);

            FiltroVista filtroEstado = new FiltroVista();
            filtroEstado.setIdFiltro(2);//otherwise i dont know what to remove dude.
            filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
            filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
            filtroEstado.setIdVista(vista);

            vista.getFiltrosVistaList().add(filtroEstado);

            return getJpaController().countEntities(vista, null, null);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, null, ex);
            return 0L;
        }
    }

    public void asignarCasoAUsuarioConMenosCasos(Grupo grupo, Caso caso) throws Exception {

        String oldOwner = caso.getOwner().getCapitalName();
//        System.out.println("asignarCasoAUsuarioConMenosCasos..." + grupo.getUsuarioList());
        caso.setIdGrupo(grupo);
        Usuario usuarioConMenosCasos = null;
        if (grupo.getUsuarioList() != null) {

            for (Usuario usuario : grupo.getUsuarioList()) {
                if (usuario.getActivo()) {
                    if (usuarioConMenosCasos == null) {
                        usuarioConMenosCasos = usuario;
                    } else {
                        final long countCasosByUsuario = countCasosByUsuario(usuarioConMenosCasos);
                        final long countCasosByUsuario1 = countCasosByUsuario(usuario);
//                    System.out.println("usuario " + usuarioConMenosCasos + " tiene " + countCasosByUsuario);
//                    System.out.println("usuario " + usuario + " tiene " + countCasosByUsuario1);
                        if (countCasosByUsuario > countCasosByUsuario1) {
                            usuarioConMenosCasos = usuario;
                        }
                    }
                }
            }

        }

//        if (usuarioConMenosCasos != null) {
        caso.setOwner(usuarioConMenosCasos);
//            System.out.println("asignarCaso " + caso.getIdCaso() + " A UsuarioConMenosCasos:" + usuarioConMenosCasos);

        AuditLog auditLogAssignUser = new AuditLog();
        auditLogAssignUser.setIdUser(EnumUsuariosBase.SISTEMA.getUsuario().getIdUsuario());
        auditLogAssignUser.setFecha(Calendar.getInstance().getTime());
        auditLogAssignUser.setTabla("Caso");
        auditLogAssignUser.setCampo("Agente (con menos casos)");
        auditLogAssignUser.setNewValue(usuarioConMenosCasos != null ? usuarioConMenosCasos.getCapitalName() : "Sin agente asignado");
        auditLogAssignUser.setOldValue(oldOwner);
        auditLogAssignUser.setIdCaso(caso.getIdCaso());

        List<AuditLog> logs = new ArrayList<>(2);
        logs.add(ManagerCasos.createLogComment(caso, "Se asigna el caso al grupo " + grupo.getNombre()));
        logs.add(auditLogAssignUser);

        getJpaController().mergeCasoWithoutNotify(caso, logs);

        if (ApplicationConfig.isSendNotificationOnTransfer()) {
            try {
                MailNotifier.notifyCasoAssigned(caso, null);
            } catch (MailClientFactory.MailNotConfiguredException ex) {
                Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "failed at ManagerCasos.asignarCasoAUsuarioConMenosCasos MailNotConfiguredException", ex);
            } catch (EmailException ex) {
                Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "failed at ManagerCasos.asignarCasoAUsuarioConMenosCasos EmailException", ex);
            }
        }

//        }
    }

//    public void calcularSLA(Caso caso, Date fecha) throws SchedulerException {
//        caso.setNextResponseDue(fecha);
//    }
    public static void calcularSLA(Caso caso) throws SchedulerException {
        Prioridad prioridad = caso.getIdPrioridad();
        if (prioridad != null) {
            int horas = prioridad.getSlaHoras();
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(caso.getFechaCreacion());
            calendario.add(Calendar.HOUR, horas);
            caso.setNextResponseDue(calendario.getTime());

        }
    }

    public static String formatIdCaso(Long idCaso) {
        return String.format(FORMAT_ID_CASO, new Object[]{idCaso});
    }

    public static Long extractIdCaso(String subject) {
        Long idCaso = extractIdCaso(subject, patternIdCaso);
        if (idCaso == null) {
            idCaso = extractIdCaso(subject, patternIdCasoLegacy);//legacy
        }
        return idCaso;
    }

    private static Long extractIdCaso(String subject, Pattern pattern) {
        try {
            if (StringUtils.isNotEmpty(subject)) // subject can be null
            {
                Matcher m = pattern.matcher(subject);
                if (m.find()) {
                    String id = m.group(2);
                    Long idCaso = Long.parseLong(id);
                    return idCaso;
                }
            }
        } catch (NumberFormatException e) {
            Log.createLogger(ManagerCasos.class.getName()).log(Level.SEVERE, "NumberFormatException extractIdCaso failed on " + subject, e);
        }
        return null;//means email does not ref# to an existing ticket
    }

    /**
     * Crear un registro en la tabla de log
     *
     * @param valorCampo Nombre del campo
     * @param newValue Nuevo valor
     * @param oldValue Valor antiguo
     * @param casoActual
     */
    public void createLogReg(String valorCampo,
            String newValue, String oldValue, Caso casoActual) {
        try {
            AuditLog audit = new AuditLog();
            audit.setIdUser(EnumUsuariosBase.SISTEMA.getUsuario().getIdUsuario());
            audit.setFecha(Calendar.getInstance().getTime());
            audit.setTabla("Caso");
            audit.setCampo(valorCampo);
            audit.setNewValue(newValue);
            audit.setOldValue(oldValue);
            audit.setIdCaso(casoActual.getIdCaso());
            getJpaController().persistAuditLog(audit);
        } catch (Exception ex) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "createLogReg", ex);

        }
    }

    private Cliente createOrUpdateCliente(DatosCaso datos) throws Exception {
        Cliente cliente = null;
        if (!StringUtils.isEmpty(datos.getRut())) {
            cliente = getJpaController().getClienteJpaController().findByRut(UtilesRut.formatear(datos.getRut()));
        }

        if (cliente == null) {
            Cliente newClientRecord = new Cliente();
            newClientRecord.setNombres(datos.getNombre());
            newClientRecord.setApellidos(datos.getApellidos());

            if (!StringUtils.isEmpty(datos.getRut())) {
                newClientRecord.setRut(UtilesRut.formatear(datos.getRut()));
            } else {
                newClientRecord.setRut(null);
            }
            newClientRecord.setFono1(datos.getTelefono());
            newClientRecord.setFono2(datos.getTelefono2());
            newClientRecord.setDirParticular(datos.getComuna());
            getJpaController().persistCliente(newClientRecord);
            cliente = newClientRecord;
        }
        return cliente;
    }

    private boolean complyWithMinimalDataRequirements(DatosCaso datos) {
        boolean valid = false;
        if (datos.getEmail() != null && !StringUtils.isEmpty(datos.getEmail())) {
            if (InputValidationBean.isValidEmail(datos.getEmail().toLowerCase().trim())) {
                valid = true;
            }
        }
        return valid;
    }

    public Caso crearCaso(DatosCaso datos, Canal canal) throws Exception {
        return crearCaso(datos, canal, null);
    }

    public Caso crearCaso(DatosCaso datos, Canal canal, Date fechaCreacion) throws Exception {
        Caso createdCaso = null;
        try {
            //TODO remove this!!!
            createdCaso = getJpaController().getCasoFindByEmailCreationTimeAndType(datos.getEmail().trim(), fechaCreacion, EnumTipoCaso.PREVENTA.getTipoCaso());
        } catch (Exception ex) {
            //Do nothing if NoResultException (normal behaviour)
            if (!(ex instanceof NoResultException)) {
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error al buscar caso el Producto", ex);
            }
        }
        if (createdCaso != null) {
            Log.createLogger(this.getClass().getName()).logInfo("Se ha encontrado un caso " + createdCaso.getIdCaso() + " del mismo cliente generado a la misma hora");
            return createdCaso;
        }
        if (complyWithMinimalDataRequirements(datos)) {
            Caso caso = new Caso();

            if (!StringUtils.isEmpty(datos.getEmail())) {
                EmailCliente emailCliente = createOrUpdateEmailCliente(datos);
                caso.setEmailCliente(emailCliente);
                caso.setIdCliente(emailCliente.getCliente());
            }

            caso.setIdCanal(canal);

            //Tema/Asunto:
            if (StringUtils.isEmpty(datos.getAsunto())) {
                int endIndex = (datos.getDescripcion() != null && !StringUtils.isEmpty(datos.getDescripcion())) ? datos.getDescripcion().length() : 0;
                endIndex = (endIndex < 50) ? endIndex : 50;
                caso.setTema(datos.getDescripcion().substring(0, endIndex));
            } else {
                caso.setTema(datos.getAsunto());
            }

            caso.setDescripcion(datos.getDescripcion());
            caso.setDescripcionTxt(HtmlUtils.stripInvalidMarkup(datos.getDescripcion()));

            //Tipo de caso:
            if (!StringUtils.isEmpty(datos.getTipoCaso())) {
                try {
                    TipoCaso tipoCaso = getJpaController().find(TipoCaso.class, ManagerCasos.removeAccents(datos.getTipoCaso().toLowerCase()));
                    if (tipoCaso != null) {
                        caso.setTipoCaso(tipoCaso);
                    } else {
                        caso.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso());
                        Log.createLogger(this.getClass().getName()).logWarning("No se ha encontrado el tipo de caso: \"" + datos.getTipoCaso() + "\"");
                    }

                } catch (NoResultException ex) {
                    Log.createLogger(this.getClass().getName()).logInfo("No se ha encontrado el tipo de caso: " + datos.getTipoCaso());
                    caso.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso());
//                    caso.setIdSubEstado(EnumSubEstadoCaso.CONTACTO_NUEVO.getSubEstado());
                }
            } else {
                caso.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso());
//                caso.setIdSubEstado(EnumSubEstadoCaso.CONTACTO_NUEVO.getSubEstado());
            }

            //Prioridad:
            if (!StringUtils.isEmpty(datos.getIdPrioridad())) {
                final Prioridad findPrioridad = getJpaController().find(Prioridad.class, datos.getIdPrioridad());
                if (findPrioridad != null) {
                    caso.setIdPrioridad(findPrioridad);
                } else {
                    caso.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());
                }
            } else {
                caso.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());
            }

            caso.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());

            //Producto:
            if (!StringUtils.isEmpty(datos.getProducto())) {
                try {
//                    Producto prod = getJpaController().getProductoFindByIdProducto(datos.getProducto().trim());
                    Producto prod = getJpaController().find(Producto.class, datos.getProducto().trim());

                    if (prod != null) {
                        caso.setIdProducto(prod);

                        //Modelo:
                        if (datos.getModelo() != null) {
                            try {
                                //Primera opcion buscar el modelo directo en la BBDD
                                final ModeloProducto modeloP = getJpaController().find(ModeloProducto.class, new ModeloProductoPK(datos.getModelo(), prod.getIdProducto()));//findModeloProductoById(datos.getModelo(), prod.getIdProducto());
                                if (modeloP != null) {
                                    caso.setIdModelo(modeloP);
                                } else {
                                    //Sino se encuentra buscar por codigo en la lista
                                    if (prod.getModeloProductoList() != null) {
                                        for (ModeloProducto modeloProducto : prod.getModeloProductoList()) {
                                            if ((modeloProducto.getIdModelo() != null) && (datos.getModelo() != null)) {
                                                if (modeloProducto.getIdModelo().equalsIgnoreCase(datos.getModelo().trim())) {
                                                    caso.setIdModelo(modeloProducto);
                                                }
                                            }
                                        }
                                        //Si aun no lo encuentro entonces buscar por nombre en la lista
                                        if (caso.getIdModelo() == null) {
                                            for (ModeloProducto modeloProducto : prod.getModeloProductoList()) {
                                                if (modeloProducto.getNombre().equalsIgnoreCase(datos.getModelo().trim())) {
                                                    caso.setIdModelo(modeloProducto);
                                                }
                                            }
                                        }
                                    }

                                }

                            } catch (Exception ex) {
                                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error al identificar el Modelo " + datos.getModelo() + " Para el producto " + prod, ex);
                            }
                        } else {
                            //logWarning: MODELO NOT SPECIFIED
                            Log.createLogger(this.getClass().getName()).logWarning("No se ha especificado un modelo");
                        }
                    } else {
                        Log.createLogger(this.getClass().getName()).logWarning("Producto desconocido: " + datos.getProducto());
                    }

                } catch (Exception ex) {
                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error al identificar el Producto", ex);
                }
            } else {
                Log.createLogger(this.getClass().getName()).logWarning("No se ha especificado un producto");
            }

            // caso.setIdArea(area);//This is important to know what area/email will received the ticket.
            //I decided area must be calculated based on RULES.
            //but area can never be null!!
            //Area:
            if (!StringUtils.isEmpty(datos.getIdArea())) {
                Area a = getJpaController().find(Area.class, datos.getIdArea());
                caso.setIdArea(a);
            } else {
//                caso.setIdArea(EnumAreas.DEFAULT_AREA.getArea());
            }

            if (datos.getTags() != null) {
                caso.setEtiquetaStringList(datos.getTags());
            }

            caso.setFechaEstimadaCompra(datos.getFechaEstimadaCompra());
            caso.setCreditoPreAprobado(datos.isCredito());

            if (fechaCreacion == null) {
                persistCaso(caso, createLogReg(caso, "Crear", "se crea caso desde " + canal.getNombre(), ""));
            } else {
                persistCaso(caso, createLogReg(caso, "Crear", "se crea caso desde " + canal.getNombre(), ""), fechaCreacion);
            }
//            createLogReg("Crear", "se crea caso desde canal " + caso.getIdCanal().getNombre(), "", caso);
            createdCaso = caso;
//            setCustomFieldValuesIfAny(datos, createdCaso);
//            createCasoPreventaIfNeeded(createdCaso);

        } else {
            throw new Exception("No se puede crear el caso. El caso no cumple con los requisitos minimos!");
        }

        return createdCaso;
    }

    public EmailCliente createOrUpdateEmailCliente(DatosCaso datos) throws NoResultException, Exception {
        String address = datos.getEmail().toLowerCase().trim();
        //Se busca el email del cliente
        EmailCliente existentEmailClient = getJpaController().getEmailClienteFindByEmail(address);

        EmailCliente emailClient;

        //Si emailCliente no existe
        if (existentEmailClient == null) {
            EmailCliente newEmailCliente = new EmailCliente(address);
            Cliente cliente = createOrUpdateCliente(datos);
            newEmailCliente.setCliente(cliente);
            getJpaController().persistEmailCliente(newEmailCliente);
            emailClient = newEmailCliente;
        } else //Si emailCliente ya existe
        {
            if (existentEmailClient.getCliente() == null) //Email no está asociado a un cliente
            {
                Cliente cliente = createOrUpdateCliente(datos);
                existentEmailClient.setCliente(cliente);
//                caso.setIdCliente(cliente_record);
                getJpaController().mergeEmailCliente(existentEmailClient);
            }
            //Se setea nuevamente en el caso el emailClient
            emailClient = existentEmailClient;
        }

        if (!StringUtils.isEmpty(datos.getTelefono()))//Se actualiza el telefono
        {
            emailClient.getCliente().setFono1(datos.getTelefono().trim());
        }
        getJpaController().merge(emailClient.getCliente()); //Se realiza el merge del cliente

        return emailClient;
    }

    public boolean crearCasoDesdeEmail(Canal canal, EmailMessage item) {
        boolean retorno = false;
        try {
            DatosCaso datos = new DatosCaso();
            datos.setEmail(item.getFromEmail().toLowerCase().trim());
            if (item.getFromName() != null) {

                String from = MimeUtility.decodeText(item.getFromName().replace("\"", ""));
                String[] nombres = from.split(" ");
                if (nombres.length > 0) {
                    datos.setNombre(nombres[0]);
                }
                if (nombres.length > 2) {
                    datos.setApellidos(nombres[1] + " " + nombres[2]);
                } else if (nombres.length > 1) {
                    datos.setApellidos(nombres[1]);
                }
            }
            datos.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso().getIdTipoCaso());
            datos.setAsunto(item.getSubject());
            datos.setDescripcion(item.getText());
//          datos.setIdArea(canal.getIdArea());//This is not important anymore
            Caso caso = crearCaso(datos, canal);
            retorno = true;
            handleEmailAttachments(item, caso);

        } catch (Exception ex) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "crearCasoDesdeEmail fail", ex);
        }
        return retorno;
    }

    public void mergeCaso(Caso caso, AuditLog audit) throws Exception {
        List<AuditLog> changeLog = new ArrayList<AuditLog>(1);
        changeLog.add(audit);
        getJpaController().mergeCaso(caso, changeLog);
//        createCasoPreventaIfNeeded(caso);
    }

    public void mergeCaso(Caso caso, List<AuditLog> changeLog) throws Exception {
        getJpaController().mergeCaso(caso, changeLog);
//        createCasoPreventaIfNeeded(caso);
    }

    public void persistCaso(Caso caso, AuditLog audit) throws Exception {
        persistCaso(caso, audit, Calendar.getInstance().getTime());
    }

    public void persistCaso(Caso caso, AuditLog audit, Date fechaCreacion) throws Exception {
        List<AuditLog> changeLog = new ArrayList<>(1);
        changeLog.add(audit);

        caso.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado());
        caso.setFechaCreacion(fechaCreacion);
        caso.setFechaModif(caso.getFechaCreacion());
//        caso.setIdCategoria(null);
        caso.setRevisarActualizacion(true);

        if (caso.getTipoCaso() == null) {
            caso.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso());
        }

        if ((null == caso.getIdSubEstado()) && (caso.getTipoCaso() != null)) {
            final TipoCaso tipoCaso = getJpaController().find(TipoCaso.class, caso.getTipoCaso().getIdTipoCaso());

            if (tipoCaso.getSubEstadoCasoList() != null) {
                SubEstadoCaso suposedFirstSubEstadoCaso = null;
                for (SubEstadoCaso subEstadoCaso : tipoCaso.getSubEstadoCasoList()) {
                    if (subEstadoCaso.isFirst()) {
                        caso.setIdSubEstado(subEstadoCaso);
                    }
                    final String toLowerCase = subEstadoCaso.getIdSubEstado().toLowerCase();

                    if (toLowerCase.contains("nuevo") || toLowerCase.contains("new")) {
                        suposedFirstSubEstadoCaso = subEstadoCaso;
                        break;
                    }
                }
                //Aggggrrrrrrrrrrr sometimes shit happens
                if (suposedFirstSubEstadoCaso != null && caso.getIdSubEstado() == null) {
                    caso.setIdSubEstado(suposedFirstSubEstadoCaso);
                }
            }

        } else if (caso.getTipoCaso() == null) {
            throw new Exception("ERROR!! Tipo caso cannot be null");
        }

        //TODO set html text 
//        caso.setDescripcionTxt(HtmlUtils.removeScriptsAndStyles(caso.getDescripcion()));
        caso.setDescripcionTxt(HtmlUtils.stripInvalidMarkup(caso.getDescripcion()));

        calcularSLA(caso);

//        prepareCustomFields(caso);??
        List<Caso> casosHijos = caso.getCasosHijosList();
        caso.setCasosHijosList(null);

        getJpaController().persistCaso(caso, changeLog);

//        if (caso.getTipoCaso().getIdTipoCaso().equalsIgnoreCase("inscripcion")) {//TODO: CUERO PENE, cambiará pronto
//            MailNotifier.notifyClientConfirmedEvent(caso);
//        } else {
        if (caso.getIdArea() != null) {
            if (caso.getIdArea().getEmailAcusederecibo()) {
                if (!caso.getTipoCaso().equals(EnumTipoCaso.PREVENTA.getTipoCaso())) {
                    MailNotifier.notifyClientCasoReceived(caso);
                }
            }
        }

        HelpDeskScheluder.scheduleAlertaPorVencer(caso.getIdCaso(), calculaCuandoPasaAPorVencer(caso));

        try {

            if ((casosHijos != null) && (!casosHijos.isEmpty())) {

//                System.out.println("pppppersisting casos hijosss");
                EmailCliente emailCliente = null;
                String email = null;
                Usuario uCliente = null;

                if (caso.getOwner() != null) {
                    email = caso.getOwner().getEmail();
                    uCliente = caso.getOwner();
                    emailCliente = getJpaController().getEmailClienteFindByEmail(email);

                } else {
                    //creador del caso pasaria a ser el cliente de los subcasos
                    UserSessionBean userSessionBean = (UserSessionBean) JsfUtil.getManagedBean("UserSessionBean");
                    email = userSessionBean.getCurrent().getEmail();
                    uCliente = userSessionBean.getCurrent();
                    emailCliente = getJpaController().getEmailClienteFindByEmail(email);

                }

                if (emailCliente == null) {
                    emailCliente = new EmailCliente(email);

                    Cliente persistentClientByRut = getJpaController().getClienteJpaController().findByRut(uCliente.getRut());

                    if (persistentClientByRut == null) {
                        Cliente cliente_record = new Cliente();
                        cliente_record.setRut(uCliente.getRut());
                        cliente_record.setNombres(uCliente.getNombres());
                        cliente_record.setApellidos(uCliente.getApellidos());
                        cliente_record.setFono1(uCliente.getTelFijo());
                        emailCliente.setCliente(cliente_record);
                        getJpaController().persistEmailCliente(emailCliente);
                    } else {
                        emailCliente.setCliente(persistentClientByRut);
                        getJpaController().persistEmailCliente(emailCliente);
                    }

                }

                List<Caso> newCasosHijosSaved = new ArrayList<>(casosHijos.size());

                for (Caso casoHijo : casosHijos) {
                    casoHijo.setIdCaso(null);
                    casoHijo.setFechaCreacion(fechaCreacion);
                    casoHijo.setFechaModif(fechaCreacion);
                    casoHijo.setIdCasoPadre(caso);
                    casoHijo.setIdArea(caso.getIdArea());
                    casoHijo.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
                    casoHijo.setIdCanal(EnumCanal.MANUAL.getCanal());

                    casoHijo.setIdPrioridad(caso.getIdPrioridad());

                    casoHijo.setIdEstado(caso.getIdEstado());
                    casoHijo.setTema("Sub caso de pre-entrega, reparación de item.");
                    casoHijo.setIdProducto(caso.getIdProducto());
                    casoHijo.setIdComponente(caso.getIdComponente());
                    casoHijo.setIdSubComponente(caso.getIdSubComponente());
                    casoHijo.setIdModelo(caso.getIdModelo());
                    ManagerCasos.calcularSLA(casoHijo);//    casoHijo.setNextResponseDue(caso.getNextResponseDue());      
                    casoHijo.setEmailCliente(emailCliente);

                    //TODO brotec-specific
                    System.out.println("persisting recinto " + casoHijo.getIdRecinto());
                    if (null == getJpaController().find(Recinto.class, casoHijo.getIdRecinto())) {
                        Recinto r = new Recinto();
                        r.setIdRecinto(casoHijo.getIdRecinto());
                        r.setNombre(casoHijo.getIdRecinto());
                        getJpaController().persist(r);
                    }

                    getJpaController().persistCaso(casoHijo, null);
                    newCasosHijosSaved.add(casoHijo);
//                caso.getCasosHijosList().add(casoHijo);
                    HelpDeskScheluder.scheduleAlertaPorVencer(casoHijo.getIdCaso(), calculaCuandoPasaAPorVencer(casoHijo));
                }

                caso.setCasosHijosList(newCasosHijosSaved);
                getJpaController().mergeCaso(caso, ManagerCasos.createLogReg(caso, "Se agregan Sub Casos", newCasosHijosSaved.toString(), ""));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void setCustomFieldValuesIfAny(DatosCaso datos, Caso current_caso) {
//        if (datos == null || current_caso == null || datos.getCustomFields() == null || current_caso.getCasoCustomFieldList() == null) {
//            return;
//        }
//        try {
//
//            for (DatosCaso.CustomField customField : datos.getCustomFields()) {
//                for (CasoCustomField casoCustomField : current_caso.getCasoCustomFieldList()) {
//                    if (casoCustomField.getFieldKey().equalsIgnoreCase(customField.getFieldKey())) {
//                        casoCustomField.setValor(customField.getFieldValue());
//                    }
//                }
//            }
//
//        } catch (Exception ex) {
//            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "setCustomFieldValuesIfAny", ex);
//        }
//    }
//    private void prepareCustomFields(Caso selectedCaso) {
//        final List<CustomField> casoCustomFields = getJpaController().getCustomFieldsForCaso();
//
//        if (selectedCaso.getCasoCustomFieldList() == null || selectedCaso.getCasoCustomFieldList().isEmpty()) {
//            //values do not exists yet, we must create them empty.
//            List<CasoCustomField> ccfs = new ArrayList<CasoCustomField>();
//            if (casoCustomFields != null) {
//                for (CustomField customField : casoCustomFields) {
//                    CasoCustomField newCasoCustomField = new CasoCustomField(customField.getCustomFieldPK().getFieldKey(), customField.getCustomFieldPK().getEntity(), selectedCaso);
//                    ccfs.add(newCasoCustomField);
//                }
//                selectedCaso.setCasoCustomFieldList(ccfs);
//            }
//        }
//    }
    public boolean crearNotaDesdeEmail(Caso caso, Canal canal, EmailMessage item) throws Exception {
        boolean retorno = false;
        StringBuilder listIdAtt = new StringBuilder();

//        boolean sendRespuestaCliente = true;
        boolean isAgent = false;
        boolean isClient = false;
        boolean isOwner = false;

        if (caso != null) {
            //FIRST QUESTION IS WHO IS THE SENDER?
            //OPTIONS ARE: AGENT OR CLIENT. AND OWNER OR NOT OWNER.
            final String emailSender = item.getFromEmail().toLowerCase().trim();
            String senderName = null;

            Nota nota = new Nota();
            nota.setAttachmentList(new LinkedList<Attachment>());
            nota.setVisible(true);//nota publica
            nota.setIdCaso(caso);

            //we need to check if the sender is an agent or a client
            //doing a quick check on the caso.owner may improve response time
            if (caso.getOwner() != null && caso.getOwner().getEmail() != null && caso.getOwner().getEmail().equalsIgnoreCase(emailSender)) {
                //bingo its the owner
                isAgent = true;
                isOwner = true;
                isClient = false;
                nota.setCreadaPor(caso.getOwner());
                nota.setTipoNota(EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota());
                nota.setEnviado(false);
                senderName = caso.getOwner().getIdUsuario();
            } else if (caso.getEmailCliente() != null && caso.getEmailCliente().getEmailCliente().equalsIgnoreCase(emailSender)) {
                //its a client
                isAgent = false;
                isClient = true;
                isOwner = true;
                caso.setRevisarActualizacion(true);
                nota.setTipoNota(EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota());
                nota.setEnviadoPor(emailSender);

                senderName = (caso.getEmailCliente().getCliente() != null
                        && !StringUtils.isEmpty(caso.getEmailCliente().getCliente().getCapitalName())) ? caso.getEmailCliente().getCliente().getCapitalName() : emailSender;
            } else {
                isOwner = false;
                //is not the owner agent and is not the owner client
                final List<Usuario> usuarioFindByEmail = getJpaController().getUsuarioFindByEmail(emailSender);
                if (usuarioFindByEmail != null) {
                    //sender is a user, but not the owner
                    isClient = false;
                    isAgent = true;
                    senderName = usuarioFindByEmail.get(0).getCapitalName();
                } else {
                    //last try is to check if this guy is another client
                    final EmailCliente emailCliente = getJpaController().find(EmailCliente.class, emailSender);
                    if (emailCliente != null) {
                        //sender is a client, but not the owner
                        isClient = true;
                        isAgent = false;
                        senderName = emailCliente.getCliente().getCapitalName();
                    }
                }
            }

            String textoBody = item.getText();
//          textoBody = parseHtmlToText(textoBody);
            if (textoBody != null) {
                nota.setTexto(HtmlUtils.removeScriptsAndStyles(textoBody));
            }

            nota.setFechaCreacion(Calendar.getInstance().getTime());
            if (item.getSubject().startsWith("Undeliverable:")) {
                nota.setTipoNota(EnumTipoNota.RESPUESTA_SERVIDOR.getTipoNota());
                nota.setTexto("El servidor de correos comuníca que su respuesta "
                        + "anterior no pudo ser entregada al destinatario");
            } else {

                for (EmailAttachment attachment : item.getAttachments()) {

                    Attachment attachmentEntity = attemptSaveAttachment(attachment, caso);
                    if (attachmentEntity != null) {
                        listIdAtt.append(attachmentEntity.getIdAttachment()).append(';');
                        nota.getAttachmentList().add(attachmentEntity);
                    }
//                    if (attachment.getContentId() == null) {
//                        attachmentsNames.append(attachment.getName()).append("<br/>");
//                    }
                }
                if (nota.getAttachmentList() != null && !nota.getAttachmentList().isEmpty()) {
                    caso.setHasAttachments(true);
                    nota.setHasAttachments(true);
                }
            }

            getJpaController().persist(nota);
            caso.getNotaList().add(nota);

            List<AuditLog> changeLog = new ArrayList<>();
            if (isClient) {
                changeLog.add(ManagerCasos.createLogReg(caso, "respuestas", "cliente " + senderName + " respondió el caso vía email, nota#Id:" + nota.getIdNota(), ""));

                //notify the case owner
                if (caso.getOwner() != null) {
                    if (caso.getOwner().getEmailNotificationsEnabled()) {
                        if (caso.getOwner().getNotifyWhenTicketIsUpdated()) {
                            MailNotifier.notifyOwnerCasoUpdated(caso, nota.getTexto(), senderName, nota.getFechaCreacion());
                        }
                    }
                }

            } else if (isAgent) {
                changeLog.add(ManagerCasos.createLogReg(caso, "respuestas", "Agente " + senderName + " agrega nota vía email al cliente nota#Id:" + nota.getIdNota(), ""));

                //notify the owner when the agent is not the owner
                if (!isOwner) {
                    if (caso.getOwner() != null) {
                        if (caso.getOwner().getEmailNotificationsEnabled()) {
                            if (caso.getOwner().getNotifyWhenTicketIsUpdated()) {
                                MailNotifier.notifyOwnerCasoUpdated(caso, nota.getTexto(), senderName, nota.getFechaCreacion());
                            }
                        }
                    }
                } else {
                    //forward the email to the client when agent is the owner 
                    String emailCliente = caso.getEmailCliente() != null && !StringUtils.isEmpty(caso.getEmailCliente().getEmailCliente())
                            ? caso.getEmailCliente().getEmailCliente() : null;
                    if (emailCliente != null) {
                        String subject = formatIdCaso(caso.getIdCaso()) + " " + ClippingsPlaceHolders.buildFinalText("${TipoCaso} ${Asunto}", caso);

                        HelpDeskScheluder.scheduleSendMailNota(canal.getIdCanal(),
                                item.getText(), emailCliente, subject, caso.getIdCaso(), nota.getIdNota(), listIdAtt.toString());
                    }
                }

            }

            mergeCaso(caso, changeLog);

            retorno = true;
//            getJpaController().commitTransaction();
        }
        return retorno;
    }

    private Attachment attemptSaveAttachment(EmailAttachment attachment, Caso caso) {

        int maxAttemptsToSave = 3;
        int currentAttempt = 0;
        while (currentAttempt < maxAttemptsToSave) {
            Attachment attachmentEntity = agregarAdjunto(attachment, caso);
            //intentionally. bug fix for exchange api.
            currentAttempt++;
            System.out.println("attemptSaveAttachment(" + currentAttempt + ")");//TODO Remove
            if (attachmentEntity != null) {
                //Lo creo ok!
                return attachmentEntity;
            }
        }
        return null;
    }

    private Attachment agregarAdjunto(EmailAttachment attachment, Caso caso) {
        try {
            String nombre = attachment.getName();
            if ((nombre != null) && (!nombre.trim().isEmpty())) {
                nombre = nombre.substring(nombre.lastIndexOf(File.separator) + 1);
                nombre = nombre.substring(nombre.lastIndexOf('\\') + 1);
            } else {
                nombre = "att" + System.currentTimeMillis();
                attachment.setName(nombre);
            }
            Attachment a = crearAdjunto(attachment.getData(), attachment.getContentId(), caso, nombre, attachment.getMimeType(), attachment.getSize());
            return a;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(ManagerCasos.class.getName()).log(Level.SEVERE, "\n\n--- EmailAttachment agregarAdjunto failed: {0}\n\n", e.getMessage());
        }
        return null;
    }

    /**
     * Crear un registro en la tabla de log
     *
     * @param valorCampo Nombre del campo
     * @param newValue Nuevo valor
     * @param oldValue Valor antiguo
     */
    public static AuditLog createLogReg(Caso caso, String valorCampo, String newValue, String oldValue) {
        if ((!valorCampo.toLowerCase().equals(Caso_.fechaModif.getName().toLowerCase()))) {
//            if (valorCampo.toLowerCase().equals(Caso_.nextResponseDue.getName().toLowerCase())) {
//                if (caso.getNextResponseDue().after(Calendar.getInstance().getTime())) {
//                    caso.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
//                }
//                try {
//                    agendarAlertas(caso);
//                } catch (SchedulerException ex) {
//                    Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
            try {
                AuditLog audit = new AuditLog();
                UserSessionBean userSessionBean = (UserSessionBean) JsfUtil.getManagedBean("UserSessionBean");
//                Usuario idUser = EnumUsuariosBase.SISTEMA.getUsuario();
                String idUser = EnumUsuariosBase.SISTEMA.getUsuario().getIdUsuario();
                if (userSessionBean != null) {
                    Usuario idUserAux = userSessionBean.getCurrent();
                    if (idUserAux != null) {
                        idUser = idUserAux.getIdUsuario();
                    } else if (userSessionBean.getEmailCliente() != null) {
                        idUser = userSessionBean.getEmailCliente().getEmailCliente();
                    }
                }

                audit.setIdUser(idUser);
                audit.setFecha(Calendar.getInstance().getTime());
                audit.setTabla(Caso.class.getSimpleName());
                audit.setCampo(valorCampo);
                audit.setNewValue(newValue);
                audit.setOldValue(oldValue);
                audit.setIdCaso(caso.getIdCaso());
                if (caso.getOwner() == null) {
                    audit.setOwner(UsuarioController.SIN_PROPIETARIO);
                } else {
                    audit.setOwner(caso.getOwner().getIdUsuario());
                }
                return audit;
            } catch (Exception e) {
                Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    public static AuditLog createLogComment(Caso caso, String comment) {
        return createLogReg(caso, "", comment, "");
    }

    public synchronized List<AuditLog> verificaCambios(Caso caso) {
        List<AuditLog> changeList = new LinkedList<>();
        Caso casoOld = getJpaController().find(Caso.class, caso.getIdCaso());

        Method[] metodos = caso.getClass().getMethods();
        for (Method method : metodos) {
            if (method.getName().startsWith("get")) {
                try {
                    Object oldValue = method.invoke(casoOld);
                    Object newValue = method.invoke(caso);
                    String campo = method.getName().substring(3);
                    if ((oldValue != null) || (newValue != null)) {
                        AuditLog audit = null;
                        if (((oldValue == null) && (newValue != null)) || ((oldValue != null) && (newValue == null))) {
                            if (method.getName().contains("List")) {
                               
                                int oldListSize = oldValue != null ? ((Collection) oldValue).size() : 0;
                                int newListSize = newValue != null ? ((Collection) newValue).size() : 0;

                                if (newListSize != 0 || oldListSize != 0) {
                                    audit = createLogReg(caso, campo, (newValue == null) ? ""
                                            : ((Collection) newValue).size() + " elementos",
                                            (oldValue == null) ? "" : ((Collection) oldValue).size() + " elementos");
                                }

                            } else {
                                audit = createLogReg(caso, campo,
                                        (newValue == null) ? "" : newValue.toString(),
                                        (oldValue == null) ? "" : oldValue.toString());
                            }
                        } else if (!oldValue.equals(newValue)) {
                            if (method.getName().contains("List")) {
                                audit = createLogReg(caso, campo,
                                        ((Collection) newValue).size() + " elemento(s)",
                                        ((Collection) oldValue).size() + " elemento(s)");
                            } else {
                                audit = createLogReg(caso, campo, newValue.toString(), oldValue.toString());
                            }
                        }
                        if (audit != null) {
                            changeList.add(audit);
                        }
                    }
                } catch (IllegalAccessException ex) {
                    Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return changeList;
    }

    public static Date calculaCuandoPasaAPorVencer(Caso casoFinal) {
        //Calcula tiempo para pasar de pendiente a por vencer
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(casoFinal.getFechaCreacion());
        long resta = casoFinal.getNextResponseDue().getTime() - casoFinal.getFechaCreacion().getTime();
        resta = resta / 60000L;
        int tiempoEnMin = (int) (((float) resta) * PORCENTAJE_POR_VENCER);
        calendario.add(Calendar.MINUTE, tiempoEnMin);
        return calendario.getTime();
    }

//    private void desagendarCambioAlerta(Caso caso)
//    {
//        try
//        {
////            Log.createLogger(ManagerCasos.class.getName()).logInfo("desagendarCambioAlerta para caso " + caso.getIdCaso());
//            HelpDeskScheluder.unscheduleTask(caso.getIdCaso() + "a", HelpDeskScheluder.GRUPO_CASOS);
//            HelpDeskScheluder.unscheduleTask(caso.getIdCaso() + "b", HelpDeskScheluder.GRUPO_CASOS);
//
//        }
//        catch (Exception ex)
//        {
//            Log.createLogger(CasoController.class.getName()).log(Level.INFO, null, ex);
//        }
//    }
    private String getReadableFileSize(Long fileSize) {
        if (fileSize == null) {
            return "0";
        }
        if (fileSize <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(fileSize / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public Attachment crearAdjunto(byte[] bytearray, String contentId, Caso caso, final String nombre, String mimeType, Long size) throws Exception {

        System.out.println("crearAdjunto()");

        String fileName = nombre.trim().replace(" ", "_");
        Archivo archivo = null;
        if (bytearray != null) {
            archivo = new Archivo();
            archivo.setArchivo(bytearray);
            archivo.setContentType(mimeType);
            archivo.setFileSize(size);
            archivo.setFileName(fileName);
            try {
                archivo.setFormat(fileName.substring(fileName.lastIndexOf(".") + 1));
            } catch (Exception e) {
            }
        }

        List col = caso.getAttachmentList();

        Attachment attach = new Attachment();
        attach.setIdCaso(caso);
        attach.setFileExtension(fileName.substring(fileName.lastIndexOf(".") + 1));
        attach.setNombreArchivoOriginal(nombre);
        attach.setNombreArchivo(fileName);
        attach.setFileSizeHuman(getReadableFileSize(size));
        attach.setMimeType(mimeType);
        attach.setContentId(contentId);

        getJpaController().persistAttachment(attach);
        col.add(attach);
        caso.setAttachmentList(col);
        if (archivo != null) {
            archivo.setIdAttachment(attach.getIdAttachment());
            getJpaController().persistArchivo(archivo);
            getJpaController().persistAuditLog(createLogReg(caso, "Archivo subido", "archivo atachado: " + fileName, ""));
        }
        return attach;
    }

//    private String parseHtmlToText(String textoTxt) {
//        textoTxt = HtmlUtils.stripInvalidMarkup(textoTxt);
//        return textoTxt;
//    }
    private void handleEmailAttachments(EmailMessage item, Caso caso) throws Exception {
        StringBuilder attachmentsNames = new StringBuilder();
//        System.out.println("El correo viene con " + item.getAttachments().size() + " Archivos adjuntos");
        for (EmailAttachment attachment : item.getAttachments()) {
            Attachment attachmentEntity = attemptSaveAttachment(attachment, caso);
            if (attachmentEntity != null) {
                if (attachment.getContentId() == null) {
                    attachmentsNames.append(attachment.getName()).append("<br/>");
                }
            }
        }

        if (!attachmentsNames.toString().isEmpty()) {
            StringBuilder textoNota = new StringBuilder((caso.getDescripcion() == null) ? "" : caso.getDescripcion());
            textoNota.append("<br/><div>Adjuntos incorporados:<br/>");
            textoNota.append(attachmentsNames);
            textoNota.append("</div>");
            caso.setDescripcion(textoNota.toString());
            caso.setDescripcionTxt(HtmlUtils.stripInvalidMarkup(textoNota.toString()));
            mergeCaso(caso, createLogReg(caso, "Adjunto", "se agregan adjuntos al caso", ""));
        }
    }

    public static Date getFechaVisitaPreventiva(Caso caso) {
        Date fechaEntrega = caso.getIdSubComponente().getFechaEntrega();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaEntrega);
        cal.add(Calendar.MONTH, 6);
        return cal.getTime();
    }
}
