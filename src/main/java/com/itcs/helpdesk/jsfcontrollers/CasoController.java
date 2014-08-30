package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import com.itcs.helpdesk.jsfcontrollers.util.FiltroAcceso;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.entities.Attachment;
import com.itcs.helpdesk.persistence.entities.AuditLog;
import com.itcs.helpdesk.persistence.entities.BlackListEmail;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Caso_;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.Clipping;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.EstadoCaso;
import com.itcs.helpdesk.persistence.entities.Etiqueta;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Item;
import com.itcs.helpdesk.persistence.entities.ModeloProducto;
import com.itcs.helpdesk.persistence.entities.NombreAccion;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.entities.Recinto;
import com.itcs.helpdesk.persistence.entities.ReglaTrigger;
import com.itcs.helpdesk.persistence.entities.ScheduleEvent;
import com.itcs.helpdesk.persistence.entities.SubEstadoCaso;
import com.itcs.helpdesk.persistence.entities.TipoAlerta;
import com.itcs.helpdesk.persistence.entities.TipoNota;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumFunciones;
import com.itcs.helpdesk.persistence.entityenums.EnumNombreAccion;
import com.itcs.helpdesk.persistence.entityenums.EnumPrioridad;
import com.itcs.helpdesk.persistence.entityenums.EnumSubEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.jpa.custom.CasoJPACustomController;
import com.itcs.helpdesk.persistence.jpa.exceptions.IllegalOrphanException;
import com.itcs.helpdesk.persistence.jpa.exceptions.NonexistentEntityException;
import com.itcs.helpdesk.persistence.jpa.exceptions.PreexistingEntityException;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.reports.ReportsManager;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.rules.actionsimpl.CrearCasoVisitaRepSellosAction;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.ClippingsPlaceHolders;
import com.itcs.helpdesk.util.DateUtils;
import com.itcs.helpdesk.util.HtmlUtils;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailNotifier;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.RulesEngine;
import com.itcs.helpdesk.util.UtilesRut;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.resource.NotSupportedException;
import javax.servlet.http.HttpServletRequest;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.ocpsoft.prettytime.PrettyTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudItem;
import org.primefaces.model.tagcloud.TagCloudModel;
import org.quartz.SchedulerException;

@ManagedBean(name = "casoController")
@SessionScoped
public class CasoController extends AbstractManagedBean<Caso> implements Serializable {

    private static final Locale LOCALE_ES_CL = new Locale("es", "CL");
    private static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, dd 'de' MMMM 'de' yyyy HH:mm", LOCALE_ES_CL);
    private static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat monthDateFormat = new SimpleDateFormat("dd MMM", LOCALE_ES_CL);
    private static final SimpleDateFormat yearDateFormat = new SimpleDateFormat("dd/MM/yy", LOCALE_ES_CL);
    //  other bean references
    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{filtroAcceso}")
    private transient FiltroAcceso filtroAcceso;
    @ManagedProperty(value = "#{vistaController}")
    private transient VistaController vistaController;
    @ManagedProperty(value = "#{UserSessionBean}")
    protected transient UserSessionBean userSessionBean;
    // end other bean references
    private List<Etiqueta> selectedEtiquetas = new ArrayList<Etiqueta>();
    private transient HashMap<String, BlackListEmail> blackListMap;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    private int selectedItemIndex;
    private int activeIndexMenuAccordionPanel;
    private String activeIndexWestPanel = "0";//TODO should be an int
    //Notas
    private String textoNota = null;
    private boolean textoNotaVisibilidadPublica = false;
//    private TipoNota tipoNota;
//    private float laborTime = 0;
    private List<String> tipoNotas;
    //Clippings
    private Clipping selectedClipping;
    /*
     * Objetos para filtro
     */
    private transient TreeNode categoria;
    private String idCasoStr;
    /*
     * Objetos para attachments
     */
    private transient UploadedFile uploadFile;
    private String idFileDelete = "";
    private transient List<Attachment> selectedAttachmensForMail;
    private int cantidadDeNotas;
    private int cantidadDeRespuestasACliente;
    private int cantidadDeRespuestasDelCliente;
    private static transient ResourceBundle resourceBundle = ResourceBundle.getBundle("Bundle");
//    private List<Nota> listaActividadesOrdenada = null;
    private boolean incluirHistoria;
    private Integer progresoEnvioRespuesta;
    private ReglaTrigger reglaTriggerSelected;
    private String emailCliente_wizard;
    private String rutCliente_wizard;
    private boolean emailCliente_wizard_existeEmail = false;
    private boolean emailCliente_wizard_existeCliente = false;
    private boolean emailCliente_wizard_updateCliente = false;
//    private transient ManagerCasos managerCasos;
    private String htmlToView = null;
//    private boolean filtrarPorCategorias;
//    private boolean filtrarPorVista = false;
//    private Vista vista;
    private Integer visibilityOption = 1;
//    private transient JPAFilterHelper filterHelper;
    //mobile
    private String swatch = "b";
    private Nota selectedNota;
    // transfer
    private Integer tipoTransferOption = 1;
    private Usuario usuarioSeleccionadoTransfer;
    private EmailCliente emailClienteSeleccionadoTransfer;
    private Long idFileRemove;
    private Integer justCreadedNotaId;

    private String accionToRunSelected;
    private String accionToRunParametros;
    private int activeIndexdescOrComment;
    private int activeIndexCasoSections;
    //visitas preventivas
    private static final int visitaPreventivaCrearCasoDiasAntes = 10;
    private static final int visitaPreventivaCrearCasoEnMeses = 6;
    private Integer casosPendientes;
    private Integer casosPorVencer;
    private Integer casosVencidos;
    private Integer casosRevisarActualizacion;
    //reply-mode
    private boolean replyMode = false;
    private boolean filterViewToggle = false;

    //respuesta
    private boolean adjuntarArchivosARespuesta = false;

    public CasoController() {
        super(Caso.class);
    }

    @Override
    public Class getDataModelImplementationClass() {
        return CasoDataModel.class;
    }

    @Override
    public OrderBy getDefaultOrderBy() {
        return new OrderBy("fechaModif", OrderBy.OrderType.DESC);
    }

    @Override
    public Usuario getDefaultUserWho() {
        return userSessionBean.getCurrent();
    }

    public void enableReplyMode() {
        this.replyMode = true;
    }

    public void disableReplyMode() {
        this.replyMode = false;
        this.selectedClipping = null;
        this.textoNota = null;
        this.adjuntarArchivosARespuesta = false;
    }

    public void submitSelectedManyItems() {
        executeInClient("selectItemsWidget.hide()");
    }

    public void onChangeActiveIndexdescOrComment(TabChangeEvent event) {
        //THIS CUERO PIC PULGA ES A RAIZ DE UN BUG DE PRIMEFACES, ME ENVIA EL ACTIVE INDEX SIEMPRE EN CER0.
        final String idtab = event.getTab().getId();
        if (idtab.contains("tabDesc")) {
            setActiveIndexdescOrComment(0);
        } else if (idtab.contains("tabComments")) {
            setActiveIndexdescOrComment(1);
        } else {
            setActiveIndexdescOrComment(0);
        }
    }

    public void onChangeActiveIndexCasoSections(TabChangeEvent event) {
        //THIS CUERO PIC PULGA ES A RAIZ DE UN BUG DE PRIMEFACES, ME ENVIA EL ACTIVE INDEX SIEMPRE EN CER0.
        final String idtab = event.getTab().getId();
        if (idtab.contains("tab-actividades")) {
            setActiveIndexCasoSections(0);
        } else if (idtab.contains("tab-timeline")) {
            setActiveIndexCasoSections(1);
        } else if (idtab.contains("tabAgendarEvento")) {
            setActiveIndexCasoSections(2);
        } else if (idtab.contains("tabArchivos")) {
            setActiveIndexCasoSections(3);
        } else if (idtab.contains("tabCasosRelacionados")) {
            setActiveIndexCasoSections(4);
        } else if (idtab.contains("tabSubCasos")) {
            setActiveIndexCasoSections(5);
        } else if (idtab.contains("tabEditarEvento")) {
            setActiveIndexCasoSections(6);
        } else {
            setActiveIndexCasoSections(0);
        }
    }

    public List<AuditLog> getAuditLogsCurrentCase() {
        if (current == null) {
            return null;
        }
        Vista vista1 = new Vista(AuditLog.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Audit Logs");

        FiltroVista f1 = new FiltroVista();
        f1.setIdCampo("idCaso");
        f1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        f1.setValor(current.getIdCaso().toString());
        f1.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(f1);

        try {
            return (List<AuditLog>) getJpaController()
                    .findAllEntities(AuditLog.class, vista1, new OrderBy("fecha", OrderBy.OrderType.DESC), userSessionBean.getCurrent());
        } catch (NotSupportedException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "NotSupportedException", ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "ClassNotFoundException", ex);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @param filtroAcceso the filtroAcceso to set
     */
    public void setFiltroAcceso(FiltroAcceso filtroAcceso) {
        this.filtroAcceso = filtroAcceso;
    }

    public void handleTagSelect() {
    }

    /**
     *
     * @return
     */
    @Override
    public JPAServiceFacade getJpaController() {
        if (jpaController == null) {
            jpaController = new JPAServiceFacade(utx, emf);
            RulesEngine rulesEngine = new RulesEngine(emf, jpaController);
            jpaController.setCasoChangeListener(rulesEngine);
        }
        return jpaController;
    }

    public void addNewSubCasoToPreentrega() {
        Caso newSubCaso = new Caso();
        if (current.getCasosHijosList() == null || current.getCasosHijosList().isEmpty()) {
            current.setCasosHijosList(new ArrayList<Caso>());
        }
        Random randomGenerator = new Random();
        long n = randomGenerator.nextLong();
        if (n > 0) {
            n = n * (-1);
        }
        newSubCaso.setIdCaso(n);//Ugly patch to solve identifier unknown when new items are added to the datatable.
        newSubCaso.setIdCasoPadre(current);
        newSubCaso.setTipoCaso(EnumTipoCaso.REPARACION_ITEM.getTipoCaso());
        newSubCaso.setIdSubEstado(EnumSubEstadoCaso.REPARACION_ITEM_NUEVO.getSubEstado());
        System.out.println(newSubCaso);
        current.getCasosHijosList().add(newSubCaso);
    }

    public void removeSubCasoFromPreentrega(Caso subcaso) {
        if (current.getCasosHijosList() == null || current.getCasosHijosList().isEmpty()) {
            JsfUtil.addErrorMessage("No hay subcasos en la lista");
        }
        if (current.getCasosHijosList().contains(subcaso)) {
            current.getCasosHijosList().remove(subcaso);
        }
        subcaso.setIdCasoPadre(null);
    }

    public void runActionOnSelectedCasos() {

//        System.out.println("accionToRunSelected:" + accionToRunSelected);
        Action a = applicationBean.getPredefinedActions().get(accionToRunSelected);
        a.setConfig(accionToRunParametros);

        List<Caso> casosToSend = Collections.EMPTY_LIST;
        if (getSelectedItemsCount() > 0) {
            casosToSend = getSelectedItems();

        } else {
            //run on all items in Vista
            try {
                casosToSend = (List<Caso>) getJpaController().findAllEntities(Caso.class, getFilterHelper().getVista(), getDefaultOrderBy(), userSessionBean.getCurrent());
            } catch (Exception ex) {
                Logger.getLogger(CasoController.class
                        .getName()).log(Level.SEVERE, "findEntities", ex);
            }
        }

        int count = 0;
        for (Caso caso : casosToSend) {
            try {
                a.execute(caso);
                count++;

            } catch (ActionExecutionException ex) {
                Logger.getLogger(CasoController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        addInfoMessage("Acción " + accionToRunSelected + " ejecutada exitosamente en " + count + " casos.");
        if (getSelectedItemsCount() > count) {
            addWarnMessage("Error: " + (getSelectedItemsCount() - count) + " casos no han sido enviados, favor revisar la configuración de correo del area correspondiente.");
        }

    }

    public void applyReglaToSelectedCasos() {
        RulesEngine rulesEngine = new RulesEngine(emf, jpaController);
        List<Caso> casosToSend = Collections.EMPTY_LIST;
        if (getSelectedItemsCount() > 0) {
            casosToSend = getSelectedItems();

        } else {
            //run on all items in Vista
            try {
                casosToSend = (List<Caso>) getJpaController().findAllEntities(Caso.class, getFilterHelper().getVista(), getDefaultOrderBy(), userSessionBean.getCurrent());
            } catch (Exception ex) {
                Logger.getLogger(CasoController.class
                        .getName()).log(Level.SEVERE, "findEntities", ex);
            }
        }

        if (casosToSend != null && !casosToSend.isEmpty()) {
            rulesEngine.applyRuleOnThisCasos(reglaTriggerSelected, casosToSend);
            addInfoMessage("Regla " + reglaTriggerSelected + " ejecutada en " + casosToSend.size() + " casos.");
            recreateModel();
        } else {
            addWarnMessage("No se ha Seleccionado ningun caso para ejecutar la regla de negocio.");
        }
    }

    public TagCloudModel getCasoTagModel() {
        TagCloudModel model = new DefaultTagCloudModel();
        if (current.getEtiquetaList() != null) {
            for (Etiqueta etiqueta : current.getEtiquetaList()) {
                model.addTag(new DefaultTagCloudItem(etiqueta.getTagId(), 1));
            }
        }
        return model;
    }

    public void tagItemSelectEvent(SelectEvent event) {
        Object item = event.getObject();
        current.setFechaModif(Calendar.getInstance().getTime());
        try {
            if (current.getEtiquetaList() != null) {
                for (Etiqueta etiqueta : current.getEtiquetaList()) {
                    etiqueta.setOwner(userSessionBean.getCurrent());
                    if (etiqueta.getCasoList() == null) {
                        etiqueta.setCasoList(new LinkedList<Caso>());
                    }
                    etiqueta.getCasoList().add(current);
                }
            }
            getJpaController().mergeCaso(current, ManagerCasos.createLogReg(current, "Etiquetas", "Se agrega Etiqueta :" + item.toString(), ""));
            addInfoMessage("Etiqueta Agregada OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar la etiqueta" + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void tagItemUnselectEvent(UnselectEvent event) {
        Object item = event.getObject();
        current.setFechaModif(Calendar.getInstance().getTime());
        try {
            getJpaController().mergeCaso(current, ManagerCasos.createLogReg(current, "Etiquetas", "Etiqueta " + item.toString() + " removida.", ""));
//            addInfoMessage("Etiqueta Removida OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Remover la etiqueta" + item);
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean isCasoTipoReparacion() {
        try {
            if (current.getTipoCaso().equals(EnumTipoCaso.REPARACION_ITEM.getTipoCaso())) {
                return true;
            }
        } catch (Exception e) {
            //ignore
        }

        return false;

    }

    public boolean isCasoTipoPreventa() {
        //
        try {
            if (current.getTipoCaso().equals(EnumTipoCaso.PREVENTA.getTipoCaso()) || current.getTipoCaso().equals(EnumTipoCaso.COTIZACION.getTipoCaso())) {
                return true;
            }
        } catch (Exception e) {
            //ignore
        }

        return false;

    }

    public List<String> getSelectedBlackList() {
        try {
            if (getSelectedItems() != null && getSelectedItems().size() > 0) {
                blackListMap = new HashMap<String, BlackListEmail>();
                for (Caso caso : getSelectedItems()) {
                    if (caso.getEmailCliente() != null && caso.getEmailCliente().getEmailCliente() != null) {
                        blackListMap.put(caso.getEmailCliente().getEmailCliente(), new BlackListEmail(caso.getEmailCliente().getEmailCliente()));
                    }
                }
                return new ArrayList<String>(blackListMap.keySet());
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }

        return null;
    }

    public void saveBlackList() {

        for (BlackListEmail blackListEmail : blackListMap.values()) {
            BlackListEmail persistentBlackListEmail = getJpaController().find(BlackListEmail.class, blackListEmail.getEmailAddress());
            if (persistentBlackListEmail == null) {
                try {
                    getJpaController().persist(blackListEmail);
                    addInfoMessage(blackListEmail.getEmailAddress() + " guardado en lista negra OK.");
                } catch (Exception ex) {
                    Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
                    addErrorMessage(blackListEmail.getEmailAddress() + " Error:" + ex.getLocalizedMessage());
                }
            }
        }

        recreateModel();
    }

    public void changePriority(Caso caso, boolean esPrioritario) {
        try {
            caso.setEsPrioritario(!esPrioritario);
            getJpaController().merge(caso);
        } catch (Exception ex) {
            caso.setEsPrioritario(esPrioritario);
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void chooseProductoModelo() {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Modelo Producto Selected", "Model:  Product:");
        RequestContext.getCurrentInstance().showMessageInDialog(message);
        RequestContext.getCurrentInstance().openDialog("selectProductoModelo");
    }

    public void onProductoModeloChosen() {
        //ModeloProducto modeloProducto = (ModeloProducto) event.getObject();
        if (current.getIdModelo() != null) {
            current.setIdComponente(current.getIdModelo().getIdComponente());
        }
//        String modelo = "Model:" + current.getIdModelo();
//        String producto = " Product:" + current.getIdProducto().getNombre();
//        String componente = " component:" + current.getIdComponente().getNombre();
//        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
//                "Modelo Producto Selected", modelo + producto
//                + componente);
//        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void selectProductoModeloFromDialog(ModeloProducto modeloProducto) {
        // RequestContext.getCurrentInstance().closeDialog(modeloProducto);
        current.setIdModelo(modeloProducto);
    }

    public void handleEmailSelect(SelectEvent event) {

//        EmailCliente emailCliente = getJpaController().getEmailClienteFindByEmail(event.getObject().toString());
        EmailCliente emailCliente = getJpaController().getEmailClienteFindByEmail(getEmailCliente_wizard());

//        //System.out.println("emailCliente_wizard:" + emailCliente_wizard);
//        //System.out.println("event.getObject().toString():" + event.getObject().toString());
        if (emailCliente != null) {

            current.setEmailCliente(emailCliente);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Email cliente registrado.", null);
            FacesContext.getCurrentInstance().addMessage("form:emailCliente", message);
            setEmailCliente_wizard_existeEmail(true);
            if (emailCliente.getCliente() != null) {
                getSelected().setIdCliente(emailCliente.getCliente());
                rutCliente_wizard = emailCliente.getCliente().getRut();
                setEmailCliente_wizard_existeCliente(true);
            } else {
                setEmailCliente_wizard_existeCliente(false);
                Cliente cliente = new Cliente();
                //cliente.setRut(rutCliente_wizard);
                emailCliente.setCliente(cliente);
            }
        } else {

            setEmailCliente_wizard_existeEmail(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Email " + emailCliente_wizard + " no registrado, favor ingresar los datos del cliente.", null);
            FacesContext.getCurrentInstance().addMessage("form:emailCliente", message);

            emailCliente = new EmailCliente(getEmailCliente_wizard());
            if (rutCliente_wizard != null) {
                //user already entered rut, so its posible there is a cliente selected.
                //if i change the email, to an email that is not in database, it means i want to add a new email to an existent client.
                Cliente existentClient = getJpaController().getClienteJpaController().findByRut(rutCliente_wizard);
                if (existentClient == null) {
                    //not exists
                    Cliente cliente = new Cliente();
                    cliente.setRut(rutCliente_wizard);
                    emailCliente.setCliente(cliente);
                    setEmailCliente_wizard_existeCliente(false);
                } else {
                    //yes, it exists...
                    emailCliente.setCliente(existentClient);
                    setEmailCliente_wizard_existeCliente(true);
                }
            } else {
                setEmailCliente_wizard_existeCliente(false);
                emailCliente.setCliente(new Cliente());
            }

            current.setEmailCliente(emailCliente);
        }
        // getSelected().setEmailCliente(emailCliente);
    }

    public void formateaRutFiltro2() {
        //System.out.println("formateaRutFiltro2()");
        try {
//            final String rutInput = getSelected().getEmailCliente().getCliente().getRut();

            if (rutCliente_wizard != null && !StringUtils.isEmpty(rutCliente_wizard)) {
                rutCliente_wizard = UtilesRut.formatear(rutCliente_wizard);

                if (!emailCliente_wizard_existeCliente) {
                    Cliente c = getJpaController().getClienteJpaController().findByRut(rutCliente_wizard);
                    if (c != null) {//this client exists
                        rutCliente_wizard = c.getRut();
                        setEmailCliente_wizard_existeCliente(true);

                        if (c.getEmailClienteList() != null && !c.getEmailClienteList().isEmpty()) {
                            EmailCliente emailCliente = c.getEmailClienteList().get(0);
                            current.setEmailCliente(emailCliente);
                            emailCliente_wizard = emailCliente.getEmailCliente();
                            setEmailCliente_wizard_existeEmail(true);
                        } else {
                            emailCliente_wizard = null;
                            setEmailCliente_wizard_existeEmail(false);
                        }
                        current.setIdCliente(c);
                    } else {

                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "El cliente con rut " + rutCliente_wizard + " no está registrado, favor ingresar.", null);
                        FacesContext.getCurrentInstance().addMessage("form:rut", message);
                        Cliente cliente = new Cliente();
                        cliente.setRut(rutCliente_wizard);

                        if (current.getEmailCliente() != null) {
                            current.getEmailCliente().setCliente(cliente);
                        } else {
                            if (getEmailCliente_wizard() != null) {
                                EmailCliente emailCliente = new EmailCliente(getEmailCliente_wizard());
                                emailCliente.setCliente(cliente);
                                current.setEmailCliente(emailCliente);
                            }
                        }
                        current.setIdCliente(cliente);

                        setEmailCliente_wizard_existeEmail(false);
                        setEmailCliente_wizard_existeCliente(false);

                    }

                } else {
                    getSelected().getEmailCliente().getCliente().setRut(rutCliente_wizard);
                }

            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    private void persist(Caso newCaso) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (newCaso.getIdPrioridad().getSlaHoras() != null) {
            int horas = newCaso.getIdPrioridad().getSlaHoras();
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(newCaso.getFechaCreacion());
            calendario.add(Calendar.HOUR, horas);
            newCaso.setNextResponseDue(calendario.getTime());
        }

        TipoAlerta alerta = EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta();

        if (alerta != null) {
            newCaso.setEstadoAlerta(alerta);
        }

        if (emailCliente_wizard_updateCliente && emailCliente_wizard_existeCliente) {
            getJpaController().merge(newCaso.getEmailCliente().getCliente());
        } else if (!emailCliente_wizard_existeCliente) {
            getJpaController().persist(newCaso.getIdCliente());
        }

        if (!emailCliente_wizard_existeEmail && !StringUtils.isEmpty(emailCliente_wizard)) {
            getJpaController().persistEmailCliente(newCaso.getEmailCliente());
        }

        getManagerCasos().persistCaso(newCaso, ManagerCasos.createLogReg(newCaso, "Caso", "Se crea caso manual", ""));
        if (newCaso.getIdCasoPadre() != null) {
            Caso casoPadre = newCaso.getIdCasoPadre();
            casoPadre.getCasosHijosList().add(newCaso);
            getManagerCasos().mergeCaso(casoPadre, ManagerCasos.createLogReg(casoPadre, "Nuevo subCaso", newCaso.getIdCaso().toString(), ""));
        }
//        getManagerCasos().agendarAlertas(newCaso);
        HelpDeskScheluder.scheduleAlertaPorVencer(newCaso.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(newCaso));
        JsfUtil.addSuccessMessage("El Caso " + newCaso.getIdCaso() + " ha sido creado con éxito.");

    }

    public void verifyGoToCaso() {
        HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
        String id = request.getParameter("id");
        if (id != null) {
            try {
                Caso casoRequested = getJpaController().getCasoFindByIdCaso(Long.parseLong(id));
                if (casoRequested == null) {
                    JsfUtil.addErrorMessage("Caso ID " + id + " no existe en el sistema");
                    FacesContext.getCurrentInstance().getExternalContext().redirect("../index.xhtml");
                } else {
                    setSelected(casoRequested);
                    FacesContext.getCurrentInstance().getExternalContext().redirect("Edit.xhtml");
                }

            } catch (Exception ex) {
                JsfUtil.addErrorMessage("Sesion invalida");
                Log.createLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //return null;
    }

    public void actualizaArbolDeCategoria() {
//        //System.out.println("actualizaArbolDeCategoria");
        Object res = JsfUtil.getManagedBean("categoriaController");
        if (res != null) {
            CategoriaController catController = ((CategoriaController) res);
            catController.filtrarCategorias();
        }
    }

    public void actualizaArbolDeCategoria(EventListener event) {
//        //System.out.println("actualizaArbolDeCategoria");
        Object res = JsfUtil.getManagedBean("categoriaController");
        if (res != null) {
            CategoriaController catController = ((CategoriaController) res);
            catController.filtrarCategorias();
        }
    }

    public void onNodeItemSelect(NodeSelectEvent event) {
        Item item = (Item) event.getTreeNode().getData();
        current.getCasosHijosList().get(current.getCasosHijosList().size() - 1).setIdItem(item);
        Object res = JsfUtil.getManagedBean("itemController");
        if (res != null) {
            ItemController itemController = ((ItemController) res);
            Item itemSelected = (Item) itemController.getItem().getData();
//            //System.out.println("categoria " + catSelected + " seleccionada");
//            current.setIdItem(itemSelected);
            current.getCasosHijosList().get(current.getCasosHijosList().size() - 1).setIdItem(itemSelected);
        }
    }

    public void onNodeItemSelectedInSelectedCaso(NodeSelectEvent event) {
        Item item = (Item) event.getTreeNode().getData();
        current.setIdItem(item);
        Object res = JsfUtil.getManagedBean("itemController");
        if (res != null) {
            ItemController itemController = ((ItemController) res);
            Item itemSelected = (Item) itemController.getItem().getData();
//            //System.out.println("categoria " + catSelected + " seleccionada");
//            current.setIdItem(itemSelected);
            current.setIdItem(itemSelected);
        }
    }

    public boolean getIncluirHistoria() {
        return incluirHistoria;
    }

    public void setIncluirHistoria(boolean incluirHistoria) {
        this.incluirHistoria = incluirHistoria;
    }

    public String getIdFileDelete() {
        return idFileDelete;
    }

    public Long getIdFileRemove() {
        return idFileRemove;
    }

    public void setIdFileDelete(String idFileDelete) {
        this.idFileDelete = idFileDelete;
        //System.out.println("idFileDelete:" + idFileDelete);
    }
    /**
     * Id Caso relacionado
     */
    private String idCaserel = "";

    @PostConstruct
    public void init() {

//        if (items == null) {
        idCasoStr = null;
        recreateModel();
        recreatePagination();
        prepareCasoFilterForInbox();
//        }
    }

    public String inbox() {
        idCasoStr = null;
        recreateModel();
        prepareCasoFilterForInbox();
        return "inbox";
    }

    public String getIdCaserel() {
        return idCaserel;
    }

    public void setIdCaserel(String idCaserel) {
        this.idCaserel = idCaserel;
    }

    public String getIdCaso() {
        return idCasoStr;
    }

    public void setIdCaso(String idCaso) {
        this.idCasoStr = idCaso;
    }

    public String creaTituloDeNota(Nota nota) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm", LOCALE_ES_CL);
        if (nota != null && nota.getTipoNota() != null) {
            if (EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota().equals(nota.getTipoNota())) {
                if (nota.getEnviadoPor() == null) {
                    return nota.getTipoNota().getNombre() + " - " + format.format(nota.getFechaCreacion());
                } else {
                    return nota.getTipoNota().getNombre() + " - enviado por: " + nota.getEnviadoPor() + " - " + format.format(nota.getFechaCreacion());
                }
            } else if (EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota().equals(nota.getTipoNota())
                    || EnumTipoNota.RESPUESTA_AUT_CLIENTE.getTipoNota().equals(nota.getTipoNota())) {
                StringBuilder sbuilder = new StringBuilder(nota.getTipoNota().getNombre());
                sbuilder.append(" - creada por ");
                sbuilder.append(nota.getCreadaPor().getIdUsuario());
                if (nota.getEnviado() != null && nota.getEnviado()) {
                    sbuilder.append(" - enviada el ");
                    sbuilder.append(format.format(nota.getFechaEnvio()));
                    sbuilder.append(" a: ");
                    sbuilder.append(nota.getEnviadoA());
                } else {
                    sbuilder.append(" - en proceso de envio - ");
                    sbuilder.append(format.format(nota.getFechaCreacion()));
                }
                return sbuilder.toString();
            }
            return nota.getTipoNota().getNombre() + " - creada por " + nota.getCreadaPor().getIdUsuario() + " - " + format.format(nota.getFechaCreacion());

        }

        return "Nota";

    }

    public int cantidadAttachment() {
        try {
            return getJpaController().countAttachmentWOContentId(current).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public int cantidadAttachmentEmbedded() {
        try {
            return getJpaController().countAttachmentWContentId(current).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public String nombreArchivoParaDesplegar(String nombreOriginal) {
        int max = 22;
        if (nombreOriginal.length() >= max) {
            StringBuilder sbuilder = new StringBuilder(nombreOriginal.substring(0, max - 3));
            sbuilder.append("...");
            return sbuilder.toString().toLowerCase();
        }
        return nombreOriginal.toLowerCase();
    }

//    public Collection datosAdjuntos() {
//        try {
//            return current.getAttachmentList();
//        } catch (Exception e) {
//            return new LinkedList();
//        }
//    }
//    public Collection datosAdjuntosNotEmbedded() {
//        try {
//            return getJpaController().getAttachmentWOContentId(current);
//        } catch (Exception e) {
//            return new LinkedList();
//        }
//    }
//    public void formateaRutFiltro() {
//        String rutFormateado = UtilesRut.formatear(getCaso().getRutCliente());
//        getCaso().setRutCliente(rutFormateado);
//        if (!getCaso().getRutCliente().isEmpty()) {
//            if (!UtilesRut.validar(getCaso().getRutCliente())) {
//                JsfUtil.addErrorMessage("Rut invalido");
//            }
//        }
//    }
    public void formateaRut() {
        System.out.println("formateaRut");
        String rutFormateado = UtilesRut.formatear(getSelected().getEmailCliente().getCliente().getRut());
        getSelected().getEmailCliente().getCliente().setRut(rutFormateado);
        validaRut();
    }

    public void validaRut() {
        if (!UtilesRut.validar(getSelected().getEmailCliente().getCliente().getRut())) {
            JsfUtil.addErrorMessage("Rut invalido");
        }
    }

//    public boolean esColaborativo() {
//        return current.getTipoCaso().equals(EnumTipoCaso.INTERNO.getTipoCaso());
//    }
//    public Caso getSelected() {
//        if (current == null) {
//            current = new Caso();
//            current.setIdCategoria(null);
//            current.setRevisarActualizacion(false);
//            selectedItemIndex = -1;
//        }
//        return current;
//    }
//
//    public void setSelected(Caso caso) {
//        this.current = caso;
//    }
    public void handleProductChange() {
    }

    public String stripInvalidMarkupLegacy(String textoTxt) {
        textoTxt = HtmlUtils.stripInvalidMarkupLegacy(textoTxt);
        return textoTxt;
    }

    public String parseHtmlToText(String textoTxt) {
        textoTxt = HtmlUtils.stripInvalidMarkup(textoTxt);
        return textoTxt;
    }

    public String parseHtmlToTextPreview(String textoTxt) {
        textoTxt = HtmlUtils.stripInvalidMarkup(textoTxt);
        int endIndex = textoTxt != null ? textoTxt.length() : 0;
        endIndex = (endIndex < 30) ? endIndex : 30;
        return endIndex != 0 ? textoTxt.substring(0, endIndex) : textoTxt;
    }

//    public String htmlToReducedSecureView(String textHtml) {
//        String textoTxt = HtmlUtils.stripInvalidMarkup(textHtml);
//        if (textoTxt.length() > 10000) {
//            return textoTxt.substring(0, 500);
//        }
//        return textoTxt;
//    }
    public void prepareViewHtml(String html) {
        htmlToView = replaceEmbeddedImages(html);
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 500);
        options.put("contentWidth", 700);
        RequestContext.getCurrentInstance().openDialog("/script/caso/ViewHtml", options, null);
    }

//    public String prepareViewHtml(String html) {
//        htmlToView = replaceEmbeddedImages(html);
//        return "/script/caso/ViewHtml";
//    }
    private String replaceEmbeddedImages(String html) {
        String pattern = "src=\"cid:";
        int index = html.indexOf(pattern);
        int endIndex = 0;
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

    public String getTextoNota() {
        return textoNota;
    }

    public void setTextoNota(String textoNota) {
        this.textoNota = textoNota;
    }

    public void initializeData(javax.faces.event.ComponentSystemEvent event) {

        casosPendientes = getJpaController().getCasoCount(userSessionBean.getCurrent(), EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
        casosPorVencer = getJpaController().getCasoCount(userSessionBean.getCurrent(), EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta());
        casosVencidos = getJpaController().getCasoCount(userSessionBean.getCurrent(), EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta());
        casosRevisarActualizacion = getJpaController().getCasoCountActualizados(userSessionBean.getCurrent());

    }

    public Integer getCasosPendientes() {
//        System.out.println("casoController.getCasosPendientes()" + JsfUtil.getRequest().getRequestURI() +"????"+ JsfUtil.getRequest().getQueryString());
        return casosPendientes;
    }

    public Integer getCasosPorVencer() {
        return casosPorVencer;
    }

    public Integer getCasosVencidos() {
//        System.out.println("casoController.getCasosVencido()"+ JsfUtil.getRequest().getRequestURI());
        return casosVencidos;
    }

    public Integer getCountCasosRevisarActualizacion() {
//        System.out.println("casoController.getCountCasosRevisarActualizacion()"+ JsfUtil.getRequest().getRequestURI());
        return casosRevisarActualizacion;
    }

    public Integer getProgresoEnvioRespuesta() {
        if (progresoEnvioRespuesta == null) {
            progresoEnvioRespuesta = 0;
        }
        int resp = progresoEnvioRespuesta;
        if (progresoEnvioRespuesta == 100) {
            progresoEnvioRespuesta = 0;
        }
        return resp;
    }

    public int getCantidadDeNotas() {
        cantidadDeNotas = 0;
        cantidadDeRespuestasACliente = 0;
        cantidadDeRespuestasDelCliente = 0;
        if (current != null) {
            final List<Nota> notaList = current.getNotaList();
            if (notaList != null) {
                for (Nota nota : notaList) {
                    if (nota.getTipoNota() != null && nota.getTipoNota().equals(EnumTipoNota.NOTA.getTipoNota())) {
                        cantidadDeNotas++;
                    }
                    if (nota.getTipoNota() != null && nota.getTipoNota().equals(EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota())) {
                        cantidadDeRespuestasACliente++;
                    }
                    if (nota.getTipoNota() != null && nota.getTipoNota().equals(EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota())) {
                        cantidadDeRespuestasDelCliente++;
                    }
                }
            }
        }

        return cantidadDeNotas;
    }

    public boolean notaEditable(Nota nota) {
        if (EnumTipoNota.NOTA.getTipoNota().equals(nota.getTipoNota()) && nota.getCreadaPor() != null && nota.getCreadaPor().equals(userSessionBean.getCurrent())) {
            return true;
        }
        return false;
    }

    public String getNotaStyle(TipoNota tipoNota) {
        for (EnumTipoNota enumTipoNota : EnumTipoNota.values()) {
            if (enumTipoNota.getTipoNota().equals(tipoNota)) {
                return enumTipoNota.getStyle();
            }
        }
        return "";
    }

//    @Override
//    public PaginationHelper getPagination() {
//        class PaginationHelperImpl extends PaginationHelper {
//
//            private Integer allCount = null;
//
//            public PaginationHelperImpl(int pageSize) {
//                super(pageSize);
//            }
//
//            @Override
//            public int getItemsCount() {
//                if (allCount == null) {
//                    try {
//                        allCount = getJpaController().countCasoEntities(getFilterHelper().getVista(), userSessionBean.getCurrent());
//                        return allCount;
//                    } catch (Exception ex) {
//                        Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "getItemsCount", ex);
//                        return 0;
//                    }
//                }
//
//                return allCount;
//            }
//
//            @Override
//            public DataModel createPageDataModel() {
//                try {
//
//                    CasoDataModel data = new CasoDataModel(getJpaController().findCasoEntities(getFilterHelper().getVista(), userSessionBean.getCurrent(), getPageSize(), getPageFirstItem(), getDefaultOrderBy()));
//                    setDatamodel(data);
//                    return data;
//                } catch (IllegalStateException ex) {//error en el filtro
//                    JsfUtil.addErrorMessage(ex, "Existe un problema con el filtro. Favor corregir e intentar nuevamente.");
//                } catch (ClassNotFoundException ex) {
//                    JsfUtil.addErrorMessage(ex, "Lo sentimos, ocurrió un error inesperado. Favor contactar a soporte.");
//                    Logger.getLogger(AbstractManagedBean.class.getName()).log(Level.SEVERE, "ClassNotFoundException createPageDataModel", ex);
//                } catch (NotSupportedException ex) {
//                    addWarnMessage("Lo sentimos, ocurrió un error inesperado. La acción que desea realizar aún no esta soportada por el sistema.");
//                }
//                return null;
//
//            }
//
//            /**
//             * @return the count
//             */
//            public Integer getAllCount() {
//                return allCount;
//            }
//
//            /**
//             * @param count the count to set
//             */
//            public void setAllCount(Integer count) {
//                this.allCount = count;
//            }
//        }
//        if (pagination == null) {
//            pagination = new PaginationHelperImpl(getPaginationPageSize());
//        }
//        return pagination;
//    }
    public String prepareList() {
        recreateModel();
//        recreatePagination();
        return "inbox";
    }

    public void handleFileUpload(FileUploadEvent event) {

        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String onFlowProcess(FlowEvent event) {
        return event.getNewStep();
    }

    public String crearCasoColab(Caso casoTmp) {
        try {
            current = new Caso();
            current.setRevisarActualizacion(false);
            current.setIdCasoPadre(casoTmp);
            Usuario usr = casoTmp.getOwner();
            EmailCliente cliente = getJpaController().getEmailClienteFindByEmail(usr.getEmail());
            current.setIdCliente(casoTmp.getIdCliente());
            rutCliente_wizard = casoTmp.getIdCliente().getRut();
            emailCliente_wizard_existeCliente = true;
            emailCliente_wizard_existeEmail = true;
            emailCliente_wizard_updateCliente = false;
            if (cliente != null) {
                emailCliente_wizard = cliente.getEmailCliente();
                current.setEmailCliente(cliente);
            } else {
                EmailCliente newCliente = new EmailCliente(usr.getEmail());
                Cliente cliente_record = new Cliente();
                cliente_record.setRut(usr.getRut());
                cliente_record.setNombres(usr.getNombres());
                cliente_record.setApellidos(usr.getApellidos());
                cliente_record.setFono1(usr.getTelFijo());
                newCliente.setCliente(cliente_record);
//                getJpaController().persistEmailCliente(newCliente);
                current.setEmailCliente(newCliente);
            }

            current.setTema("[Colab] " + casoTmp.getTema());
            current.setIdCanal(EnumCanal.SISTEMA.getCanal());
            current.setFechaCreacion(Calendar.getInstance().getTime());
            current.setFechaModif(current.getFechaCreacion());
            current.setOwner(null);
            EstadoCaso ec = new EstadoCaso();
            ec.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());

            current.setTipoCaso(EnumTipoCaso.INTERNO.getTipoCaso());
            current.setIdSubEstado(EnumSubEstadoCaso.INTERNO_NUEVO.getSubEstado());
            current.setIdPrioridad(getJpaController().getPrioridadFindByIdPrioridad(EnumPrioridad.MEDIA.getPrioridad().getIdPrioridad()));

            current.setIdEstado(ec);
            selectedItemIndex = -1;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido generar un caso interno", e);
        }
        return "/script/caso/Create";
    }

    public String prepareCreateCasoPreentrega() {
        try {
            current = new Caso();
            current.setRevisarActualizacion(true);
            current.setIdPrioridad(null);
            current.setFechaCreacion(Calendar.getInstance().getTime());
            current.setFechaModif(current.getFechaCreacion());
            current.setOwner(userSessionBean.getCurrent());
            current.setTema("Caso de pre-entrega");
            current.setTipoCaso(EnumTipoCaso.ENTREGA.getTipoCaso());
            current.setIdSubEstado(EnumSubEstadoCaso.ENTREGA_NUEVO.getSubEstado());
            current.setIdCanal(EnumCanal.MANUAL.getCanal());
            current.setIdCliente(new Cliente());
            EmailCliente email = new EmailCliente();
            email.setCliente(current.getIdCliente());
            current.setEmailCliente(email);
            EstadoCaso ec = new EstadoCaso();
            ec.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());

//            SubEstadoCaso sec = new SubEstadoCaso();
//            sec.setIdSubEstado(EnumSubEstadoCaso.NUEVO.getSubEstado().getIdSubEstado());
//            current.setIdSubEstado(sec);
            current.setIdEstado(ec);

            emailCliente_wizard_existeEmail = false;
            emailCliente_wizard_existeCliente = false;
            emailCliente_wizard_updateCliente = false;
            emailCliente_wizard = null;
            rutCliente_wizard = null;
            selectedItemIndex = -1;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo("Error al preparar la creación de un caso de entrega");
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
        }
        return "/script/caso/Create_preentrega";
    }

    public String prettyDate(Date date) {

        if (date != null) {
            PrettyTime p = new PrettyTime(new Locale("es"));
            return p.format(date);
//            return PrettyDate.format(date);
        } else {
            return "";
        }
    }

    public String formatShortDate(Date date) {
        if (date != null) {

            if (DateUtils.isToday(date)) {
                return dayDateFormat.format(date);
            } else if (DateUtils.isThisYear(date)) {
                return monthDateFormat.format(date);
            } else {
                return yearDateFormat.format(date);
            }
        } else {
            return "";
        }
    }

    public String formatDate(Date date) {
        if (date != null) {
            return fullDateFormat.format(date);
        } else {
            return "";
        }
    }

    public String prepareCreate() {
        try {
            current = new Caso();
            current.setRevisarActualizacion(true);
            current.setIdPrioridad(null);
            current.setFechaCreacion(Calendar.getInstance().getTime());
            current.setFechaModif(current.getFechaCreacion());
            current.setOwner(userSessionBean.getCurrent());
            current.setTipoCaso(EnumTipoCaso.CONTACTO.getTipoCaso());
            current.setIdSubEstado(EnumSubEstadoCaso.CONTACTO_NUEVO.getSubEstado());
            current.setIdCanal(EnumCanal.MANUAL.getCanal());
            EmailCliente email = new EmailCliente();
            email.setCliente(new Cliente());
            current.setEmailCliente(email);
            EstadoCaso ec = new EstadoCaso();
            ec.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());

//            SubEstadoCaso sec = new SubEstadoCaso();
//            sec.setIdSubEstado(EnumSubEstadoCaso.NUEVO.getSubEstado().getIdSubEstado());
//            current.setIdSubEstado(sec);
            current.setIdEstado(ec);

            emailCliente_wizard_existeEmail = false;
            emailCliente_wizard_existeCliente = false;
            emailCliente_wizard_updateCliente = false;
            emailCliente_wizard = null;
            rutCliente_wizard = null;
            selectedItemIndex = -1;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo("Error al preparar la creacion de un caso");
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
        }
        return "/script/caso/Create";
    }

    public String prepareCopy() {
        return prepareCopy(current, false);
    }

    public String prepareCopyAndAssignMe() {
        return prepareCopy(current, true);
    }

    private String prepareCopy(Caso origin, boolean assignme) {
        try {
            Caso copy = new Caso();
            copy.setDescripcion(origin.getDescripcion());
            copy.setDescripcionTxt(origin.getDescripcionTxt());
            copy.setEmailCliente(origin.getEmailCliente());
            copy.setEsPregConocida(origin.getEsPregConocida());
            copy.setEsPrioritario(origin.getEsPrioritario());
            copy.setEstadoAlerta(origin.getEstadoAlerta());
            copy.setEstadoEscalacion(origin.getEstadoEscalacion());
            copy.setIdCanal(origin.getIdCanal());
            copy.setIdComponente(origin.getIdComponente());
            copy.setIdPrioridad(origin.getIdPrioridad());
            copy.setIdProducto(origin.getIdProducto());
            copy.setIdSubComponente(origin.getIdSubComponente());
            copy.setIdCasoPadre(origin.getIdCasoPadre());

            copy.setTema(origin.getTema());
            copy.setTranferCount(0);

            copy.setRevisarActualizacion(true);
            copy.setFechaCreacion(Calendar.getInstance().getTime());
            copy.setFechaModif(copy.getFechaCreacion());
            if (assignme) {
                copy.setOwner(userSessionBean.getCurrent());
            } else {
                copy.setOwner(origin.getOwner());
            }

            copy.setTipoCaso(origin.getTipoCaso());
            copy.setIdSubEstado(origin.getIdSubEstado());
            copy.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado());

            if (origin.getEmailCliente() != null) {
                emailCliente_wizard_existeEmail = true;
                emailCliente_wizard = origin.getEmailCliente().getEmailCliente();
                try {
                    rutCliente_wizard = origin.getEmailCliente().getCliente().getRut();
                } catch (Exception e) {
                    //ignore.
                }
            }
            if (origin.getEmailCliente() != null && origin.getEmailCliente().getCliente() != null) {
                emailCliente_wizard_existeCliente = true;
            }

            copy.setIdCliente(origin.getIdCliente());

            current = copy;
            selectedItemIndex = -1;

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo("Error al preparar la creacion (copia) de un caso");
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
        }
        return "/script/caso/Create";
    }

    public void goToNextStepMobile() {
    }

    public void createCaso() {
        try {
            persist(current);
        } catch (PreexistingEntityException ex) {
            JsfUtil.addErrorMessage("El Caso no pudo ser creado, existe otro caso con el mismo id.");
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RollbackFailureException ex) {
            JsfUtil.addErrorMessage("El Caso no pudo ser creado, hay un problema con integridad de los datos..");
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            JsfUtil.addErrorMessage("El Caso no pudo ser creado:" + ex.getLocalizedMessage());
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * DUPLICATE: createAndView()
     *
     * @return
     */
    public String createPreentregaAndView() {
        try {
            persist(current);
//            listaActividadesOrdenada = null;
            return "/script/caso/Edit";
        } catch (PreexistingEntityException e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage("El Caso con id " + current.getIdCaso() + " ya existe!");
            return null;
        } catch (RollbackFailureException e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, "Se produjo una inconsistencia de datos, favor intente mas tarde.");
            return null;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo(resourceBundle.getString("PersistenceErrorOccured"));
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, resourceBundle.getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String createAndView() {
        try {
            persist(current);
//            listaActividadesOrdenada = null;
            return "/script/caso/Edit";
        } catch (PreexistingEntityException e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage("El Caso con id " + current.getIdCaso() + " ya existe!");
            return null;
        } catch (RollbackFailureException e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, "Se produjo una inconsistencia de datos, favor intente mas tarde.");
            return null;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo(resourceBundle.getString("PersistenceErrorOccured"));
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "createAndView", e);
            JsfUtil.addErrorMessage(e, resourceBundle.getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String goOneLevelUp() {
        if (current.getIdCasoPadre() != null) {
            //tiene padre 
            current = current.getIdCasoPadre();
//            recreateModel();
            return "/script/caso/Edit";
        } else {
            return prepareList();
        }
    }

    public void refreshCaso() throws Exception {
        current = getJpaController().find(Caso.class, current.getIdCaso());
        if (current != null && current.getNotaList() != null) {
            Collections.sort(current.getNotaList());
        }
//        recreateModel();
    }

    public String prepareEdit() throws Exception {
        if (current == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return null;
        } else {
            if (current != null) {
                current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
//                System.out.println("FOUND CASO:" + current);
                setActiveIndexdescOrComment(0);
                setActiveIndexCasoSections(0);
                if (current != null && (current.getOwner() != null) && (current.getOwner().equals(userSessionBean.getCurrent()))) {
                    if (current.getRevisarActualizacion()) {
                        current.setRevisarActualizacion(false);
                        List<AuditLog> changeList = getManagerCasos().verificaCambios(current);
                        getJpaController().mergeCaso(current, changeList);
                    }
                }
            }
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (current.getNotaList() != null) {
            Collections.sort(current.getNotaList());
        }
        return "/script/caso/Edit";
    }

    public Caso getCurrentCaso() {
        return current;
    }

    public void setCurrentCaso(Caso currentCaso) {
        this.current = currentCaso;
//        listaActividadesOrdenada = null;
        if (current != null) {
            current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
            if ((current.getOwner() != null) && (current.getOwner().equals(userSessionBean.getCurrent()))) {
                if (current.getRevisarActualizacion()) {
                    current.setRevisarActualizacion(false);
                    List<AuditLog> changeList = getManagerCasos().verificaCambios(current);
                    try {
                        getJpaController().mergeCaso(current, changeList);
                    } catch (Exception ex) {
                        Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public void prepareEditNota(Nota nota) throws Exception {
        selectedNota = nota;
    }

    public String prepareEdit(Caso caso) throws Exception {
        current = caso;
        return prepareEdit();
    }

    public String prepareEditCaso() {
        if (idCasoStr == null) {
            JsfUtil.addErrorMessage("Debe poner un numero de caso para ir");
            return "";
        } else {
            long idCaso = -1;
            try {
                idCaso = Long.parseLong(idCasoStr);
            } catch (NumberFormatException ex) {
                JsfUtil.addErrorMessage("Debe poner un numero de caso para ir");
                return "";
            }
            try {
                Caso casoSearch = getJpaController().getCasoFindByIdCaso(idCaso);
                if (casoSearch == null) {
                    JsfUtil.addErrorMessage("Caso " + idCaso + " no encontrado");
                    return "";
                } else {
                    current = casoSearch;
                    if ((current.getOwner() != null) && (current.getOwner().equals(userSessionBean.getCurrent()))) {
                        if (current.getRevisarActualizacion()) {
                            current.setRevisarActualizacion(false);
                            List<AuditLog> changeList = getManagerCasos().verificaCambios(current);
                            getJpaController().mergeCaso(current, changeList);
                        }
                    }
//                    listaActividadesOrdenada = null;
                    return "/script/caso/Edit";
                }
            } catch (Exception ex) {
                JsfUtil.addErrorMessage("Caso " + idCaso + " no encontrado");
                return "";
            }
        }
    }

    public String filterByIdCaso(Long idCaso) {

        Caso casoToGo = getJpaController().getCasoFindByIdCaso(idCaso);
//        listaActividadesOrdenada = null;

        if (casoToGo != null) {
            this.current = casoToGo;
            return "/script/caso/Edit";
        } else {
            addErrorMessage("El caso ya no existe.");
            return null;
        }
    }

//    public String redirect(String dir) {
//        return dir;
//    }
    public String filterList() {
        categoria = null;
        recreateModel();
        recreatePagination();
        return "inbox";
    }

    public void onTagIdSelectCaso(String idTag) {
        selectThisTag(idTag);
        redirect("/script/index.xhtml");
    }

    public void onTagSelectCaso(SelectEvent event) {
        onTagSelect(event);
        redirect("/script/index.xhtml");
    }

    public void onTagSelect(SelectEvent event) {
        TagCloudItem item = (TagCloudItem) event.getObject();
        selectThisTag(item.getLabel());

    }

    private void selectThisTag(String tagId) {
        Vista copy = new Vista(Caso.class);
        copy.setNombre(tagId);

        FiltroVista fCopy = new FiltroVista();
        fCopy.setIdFiltro(1);
        fCopy.setIdCampo("etiquetaList");
        fCopy.setIdComparador(EnumTipoComparacion.IM.getTipoComparacion());
        fCopy.setValor(tagId);
        fCopy.setIdVista(copy);

        copy.getFiltrosVistaList().add(fCopy);

        getFilterHelper().setVista(copy);

        recreateModel();
        recreatePagination();

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Etiqueta Seleccionada:" + tagId, "");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public Vista getVista() {
        return getFilterHelper().getVista();
    }

    public void setVista(Vista vista) {
        getFilterHelper().setVista(vista);
        recreatePagination();
        recreateModel();
    }

    public String applyViewFilter(Vista vista) {

        try {
            Vista copy = new Vista(Caso.class);
            copy.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
            copy.setDescripcion(vista.getDescripcion());
            copy.setIdArea(vista.getIdArea());
            copy.setIdGrupo(vista.getIdGrupo());
            copy.setNombre(vista.getNombre());
            copy.setVisibleToAll(vista.getVisibleToAll());
            //Crearemos una copia para que al guardar no pisar la existente.
            int i = 1;
            if (vista.getFiltrosVistaList() != null) {
                for (FiltroVista f : vista.getFiltrosVistaList()) {
                    FiltroVista fCopy = new FiltroVista();
                    fCopy.setIdFiltro(i);
                    i++; //This is an ugly patch to solve issue when removing a filter from the view, if TODO: Warning - this method won't work in the case the id fields are not set
                    fCopy.setIdCampo(f.getIdCampo());
                    fCopy.setIdComparador(f.getIdComparador());
                    fCopy.setValor(f.getValor());
                    fCopy.setValor2(f.getValor2());
                    fCopy.setIdVista(copy);
                    copy.getFiltrosVistaList().add(fCopy);
                    //System.out.println("added filtro " + fCopy);
                }
            }

            getFilterHelper().setVista(copy);
//        this.setVista(copy);
//        //System.out.println("Vista copy set:" + copy);
            recreatePagination();
            recreateModel();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }

        return "inbox";

    }

    public void prepareGuardarVista() {
        JsfUtil.addWarningMessage("Atención: Al Guardar se creará una vista Nueva.");
    }

    public String saveCurrentView() {
        //SE hizo una copia de esta vista, y los filtros tienen ids falsos
        for (FiltroVista f : getFilterHelper().getVista().getFiltrosVistaList()) {
            f.setIdFiltro(null); //This is an ugly patch to solve issue when removing a filter from the view, if TODO: Warning - this method won't work in the case the id fields are not set
        }
        try {
            vistaController.create(getFilterHelper().getVista());

            JsfUtil.addSuccessMessage("La Vista guardada exitosamente. Revisar el panel de Vistas.");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }

        return null;
    }

    public String filtraPorAlertaPendiente() {
        return filtraPorAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
    }

    public String filtraPorAlertaPorVencer() {
        return filtraPorAlerta(EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta());
    }

    public String filtraPorAlertaVencido() {
        return filtraPorAlerta(EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta());
    }

    private void prepareCasoFilterForInbox() {
//        //System.out.println("prepareCasoFilterForInbox");
        List<Vista> vistas = vistaController.getAllVistasItems();

        if (vistas != null && !vistas.isEmpty()) {
            //there are vistas, lets select the first one as the user inbox
            getFilterHelper().setVista(vistas.get(0));
        } else {
            //damn there are no vistas for this madafaka, lets give him some default shit
            Vista vista1 = new Vista(Caso.class);
            vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
            vista1.setNombre("Inbox");

            FiltroVista filtroOwner = new FiltroVista();
            filtroOwner.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
            filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroOwner.setValor(CasoJPACustomController.PLACE_HOLDER_CURRENT_USER);
            filtroOwner.setIdVista(vista1);

            vista1.getFiltrosVistaList().add(filtroOwner);

            FiltroVista filtroEstado = new FiltroVista();
            filtroEstado.setIdFiltro(2);//otherwise i dont know what to remove dude.
            filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
            filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
            filtroEstado.setIdVista(vista1);

            vista1.getFiltrosVistaList().add(filtroEstado);

            getFilterHelper().setVista(vista1);
        }

    }

    /**
     * Crea un filtro para mostrar los casos con el flag revisar actualizacion
     * activo.
     * <p>
     * Un caso cerrado puede tener actualización (Cuando un cliente responde,
     * despues de cerrado el caso).
     *
     * @return La página inbox con el filtro ya definido.
     */
    public String filtraRevisarActualizaciones() {
//        //System.out.println("filtraRevisarActualizaciones");
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Casos Actualizados por el cliente");

        FiltroVista filtroOwner = new FiltroVista();
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(CasoJPACustomController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroOwner);

        FiltroVista reviewUpdate = new FiltroVista();
        reviewUpdate.setIdCampo(Caso_.REVISAR_ACTUALIZACION_FIELD_NAME);
        reviewUpdate.setVisibleToAgents(false);
        reviewUpdate.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        reviewUpdate.setValor(Boolean.TRUE.toString());
        reviewUpdate.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(reviewUpdate);

        getFilterHelper().setVista(vista1);
        recreatePagination();
        recreateModel();
        return "inbox";
    }

    public String filtraPorAlerta(TipoAlerta alerta) {
//        //System.out.println("filtraPorAlerta");
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Casos con Alerta " + alerta.getNombre());

        FiltroVista filtroOwner = new FiltroVista();
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(CasoJPACustomController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroOwner);

        FiltroVista filtroEstado = new FiltroVista();
        filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
        filtroEstado.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroEstado);

        FiltroVista filtroAlerta = new FiltroVista();
        filtroAlerta.setIdCampo(Caso_.ESTADO_ALERTA_FIELD_NAME);
        filtroAlerta.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroAlerta.setValor(alerta.getIdalerta().toString());
        filtroAlerta.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroAlerta);

        getFilterHelper().setVista(vista1);
        recreatePagination();
        recreateModel();
        return "inbox";
    }

    public void refresh() {
        recreateModel();
        recreatePagination();
    }

//    public void refreshNotas() {
//        listaActividadesOrdenada = null;
//    }
    public void tagManyCasosManyTags() {

        int count = 0;
        if (selectedEtiquetas != null && !selectedEtiquetas.isEmpty()) {

            for (Caso caso : getSelectedItems()) {
                try {
                    if (caso.getEtiquetaList() == null) {
                        caso.setEtiquetaList(new LinkedList<Etiqueta>());
                    }
                    List<AuditLog> changeLog = new ArrayList<AuditLog>();
                    changeLog.add(ManagerCasos.createLogReg(caso, "Lista de etiquetas", selectedEtiquetas.toString(), caso.getEtiquetaList().toString()));
                    caso.getEtiquetaList().addAll(selectedEtiquetas);
                    getJpaController().mergeCaso(caso, changeLog);
                    count++;
                } catch (Exception ex) {
                    Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            JsfUtil.addSuccessMessage(count + " casos etiquetados exitosamente.");
        }
    }

    public boolean puedeResponder() {
        if (current == null) {
            return false;
        }
        if (filtroAcceso.verificaAccesoAFuncion(EnumFunciones.RESPONDER_CUALQUIER_CASO)) {
            return true;
        }
        if (current.getOwner() != null) {
            Usuario usuarioActual = userSessionBean.getCurrent();
            if (current.getOwner().equals(usuarioActual)) {
                return true;
            }
        }
        return false;
    }

    public boolean puedeModificar() {
        if (current == null) {
            return false;
        }
        if (filtroAcceso.verificaAccesoAFuncion(EnumFunciones.EDITAR_CUALQUIER_CASO)) {
            return true;
        }
        if (current.getOwner() != null) {
            Usuario usuarioActual = userSessionBean.getCurrent();
            if (current.getOwner().equals(usuarioActual)) {
                return true;
            }
        }
        return false;
    }

    public boolean verificarTomarCaso() {
        return verificarAsignarCaso();
    }

    public boolean verificarTransferirCaso() {
        if (current == null) {
            return false;
        }
        return (filtroAcceso.verificaAccesoAFuncion(EnumFunciones.ASIGNAR_TRANSFERIR_CASO) && current.isOpen() && current.hasAnOwner());
    }

    public boolean verificarAsignarCaso() {
        if (current == null) {
            return false;
        }
        return (filtroAcceso.verificaAccesoAFuncion(EnumFunciones.ASIGNAR_TRANSFERIR_CASO) && current.isOpen() && !current.hasAnOwner());
    }

    public boolean verificarEliminarCaso() {
        return (filtroAcceso.verificaAccesoAFuncion(EnumFunciones.ELIMINAR_CASO));
    }

    public boolean verificarReabrirCaso() {
        return (current.isClosed() && puedeModificar());
    }

    public boolean verificarCrearColaborativo() {
        return (current.hasAnOwner() && puedeModificar() && current.isOpen());
    }

    public boolean verificarCrearActividad() {
        return (current.hasAnOwner() && puedeModificar() && current.isOpen());
    }

    public boolean verificarResponderCaso() {
        return (current.hasAnOwner() && current.isOpen() && puedeResponder() && current.getEmailCliente() != null);
    }

    public boolean verificarGrabarCaso() {
        return (puedeModificar());
    }

    public boolean verificarRelacionarCaso() {
        return current.isOpen() && puedeModificar();
    }

    public boolean verificarCerrarCaso() {
        return current.isOpen() && puedeModificar();
    }

    public String descartarBorrador() {

        //List<AuditLog> changeLog = new ArrayList<AuditLog>();
        try {
            //  current.setFechaModif(Calendar.getInstance().getTime());
            current.setRespuesta(null);
            //changeLog.add(ManagerCasos.createLogReg(current, "Borrador", "Se descarta borrador de respuesta", ""));
            //getJpaController().mergeCaso(current, changeLog);
            JsfUtil.addSuccessMessage("Borrador descartado");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, "No se ha descartado el borrador");
        }

        return "/script/caso/Edit";
    }

    public void cerrarCaso() {
        if (cierraCaso()) {
            if (isAjaxRequest()) {
                redirect("/script/caso/Edit.xhtml");
            }
        }
    }

    private boolean cierraCaso() {

        List<AuditLog> changeLog = new ArrayList<AuditLog>();

        try {
            current.setIdEstado(EnumEstadoCaso.CERRADO.getEstado());
            current.setFechaCierre(Calendar.getInstance().getTime());

            JsfUtil.addSuccessMessage(resourceBundle.getString("caso.cerrar.ok"));
            changeLog.add(ManagerCasos.createLogReg(current, "Estado", userSessionBean.getCurrent().getIdUsuario() + " cerró el caso", ""));
            getJpaController().mergeCaso(current, changeLog);

            HelpDeskScheluder.unscheduleCambioAlertas(current.getIdCaso());

            if (current.getCasosHijosList() != null) {
                for (Caso casoHijo : current.getCasosHijosList()) {
                    if (casoHijo.isOpen()) {
                        casoHijo.setIdEstado(EnumEstadoCaso.CERRADO.getEstado());
                        casoHijo.setFechaCierre(Calendar.getInstance().getTime());

                        JsfUtil.addSuccessMessage(resourceBundle.getString("caso.cerrar.ok"));
                        changeLog.add(ManagerCasos.createLogReg(casoHijo, "Estado", "Cerrado", ""));
                        getJpaController().mergeCaso(casoHijo, changeLog);

                        HelpDeskScheluder.unscheduleCambioAlertas(casoHijo.getIdCaso());
                    }
                }
            }

            refreshCaso();

        } catch (Exception e) {
            JsfUtil.addErrorMessage(resourceBundle.getString("caso.cerrar.nook"));
            return false;
        }

        return true;
    }

    /**
     * cierra el caso y programa una visita preventiva en 6 meses a partir de la
     * fecha de entrega
     *
     * @return
     */
    public void cerrarCasoYProgramarVP() {

        //validation
        if (current.getIdSubComponente() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Para poder programar la visita preventiva, este caso debe estar asociado a un producto, favor seleccione el producto.");
            RequestContext.getCurrentInstance().showMessageInDialog(message);
            return;
        }
        if (current.getIdSubComponente().getFechaEntrega() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Para poder programar la visita preventiva, el producto " + current.getIdSubComponente()
                    + " debe tener fecha de entrega, favor modifique el producto e ingrese dicha fecha.");
            RequestContext.getCurrentInstance().showMessageInDialog(message);

            return;
        }
        if (current.hasOpenSubCasosDelTipo(EnumTipoCaso.REPARACION_ITEM.getTipoCaso())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Para poder programar la visita preventiva, este caso no debe tener sub casos abiertos pendientes de resolución.");
            RequestContext.getCurrentInstance().showMessageInDialog(message);
            return;
        }
        if (current.getEmailCliente() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Para poder programar la visita preventiva, este caso debe estar asociado a un cliente con el cúal se coordina la visita.");
            RequestContext.getCurrentInstance().showMessageInDialog(message);
            return;
        }

        if (cierraCaso()) {
            //visita preventiva

            persistProgramarVisitaPreventivaEvent();

            if (isAjaxRequest()) {
                redirect("/script/caso/Edit.xhtml");
            }

            //fin visita preventiva
            //cerrado ok
        }

    }

    //visita preventiva
    private void persistProgramarVisitaPreventivaEvent() {

        String visitaPreventivaSubject = "Coordinar Visita preventiva con el cliente";
        String visitaPreventivaTextoDesc = "Caso creado a partir de una visita preventiva otorgada por la inmobiliaria, favor coordinar la fecha de visita definitiva con el cliente.";

        try {

            Date futureDate = getFechaVisitaPreventiva(current);

            Calendar cal = Calendar.getInstance();
            cal.setTime(futureDate);
            cal.add(Calendar.DAY_OF_MONTH, (visitaPreventivaCrearCasoDiasAntes * (-1)));
            Date eventDate = cal.getTime();

            ScheduleEvent entityEvent = new ScheduleEvent(null, visitaPreventivaSubject, eventDate, applicationBean.getNow());
            entityEvent.setAllDay(true);
            entityEvent.setDescripcion(visitaPreventivaTextoDesc);
            entityEvent.setEndDate(eventDate);
            entityEvent.setIdCaso(current);
            entityEvent.setIdUsuario(userSessionBean.getCurrent());
            entityEvent.setLugar("dentro del sistema");
            entityEvent.setPublicEvent(true);
//            entityEvent.setUsuariosInvitedList(visitaPreventivaAsignarAGrupo.getUsuarioList());
            entityEvent.setExecuteAction(true);
            final NombreAccion nombreAccion = EnumNombreAccion.CREAR_CASO_VISITA_REP_SELLOS.getNombreAccion();
            entityEvent.setIdTipoAccion(nombreAccion);
            Properties props = new Properties();
            props.put(CrearCasoVisitaRepSellosAction.TEMA_KEY, visitaPreventivaSubject);
            props.put(CrearCasoVisitaRepSellosAction.DESC_KEY, visitaPreventivaTextoDesc);
//            props.put(CrearCasoVisitaRepSellosAction.AREA_KEY, visitaPreventivaAsignarAArea.getIdArea());
//            props.put(CrearCasoVisitaRepSellosAction.GRUPO_KEY, visitaPreventivaAsignarAGrupo.getIdGrupo());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            props.put(CrearCasoVisitaRepSellosAction.FECHA_VISITA_KEY, sdf.format(futureDate));
            props.put(CrearCasoVisitaRepSellosAction.FECHA_REP_KEY, sdf.format(futureDate));

//            String idsItems = "";
//            boolean first = true;
//            for (TreeNode treeNode : getVisitaPreventivaItemsAReparar()) {
//                if (first) {
//                    idsItems = ((Item) treeNode.getData()).getIdItem().toString();
//                    first = false;
//                } else {
//                    idsItems += "," + ((Item) treeNode.getData()).getIdItem().toString();
//                }
//            }
//            props.put(CrearCasoVisitaRepSellosAction.ITEMS_KEY, idsItems);
            entityEvent.setParametrosAccion(Action.getPropertyAsString(props));

            getJpaController().persist(entityEvent);

            if (entityEvent.getExecuteAction() && entityEvent.getIdTipoAccion() != null
                    && !StringUtils.isEmpty(entityEvent.getIdTipoAccion().getImplementationClassName())) {
                //we must schedule the selected action
                String jobID = HelpDeskScheluder.scheduleActionClassExecutorJob(
                        getSelected().getIdCaso(),
                        entityEvent.getIdTipoAccion().getImplementationClassName(),
                        entityEvent.getParametrosAccion(),
                        entityEvent.getStartDate());
                entityEvent.setQuartzJobId(jobID);
                getJpaController().merge(entityEvent);
            }

            addInfoMessage("Evento agendado exitósamente.");
//            executeInClient("PF('ProgramarVisitaPreventiva').hide();");

        } catch (Exception ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "persistProgramarVisitaPreventivaEvent", ex);
        }

    }

//    public void prepareProgramarVisitaPreventiva(ActionEvent actionEvent) {
////        //System.out.println("prepareCreateRespuesta");
//        try {
//
//            if (!validarSiPuedeProgramarVP(current)) {
//                return;
//            }
//
//            updateFechasVisitaPreventiva();
//
//            //reset items
//            setVisitaPreventivaItemsAReparar(null);
//            setVisitaPreventivaSubject("Generar Caso de postventa (programado) - reparación de sellos");
//            setVisitaPreventivaAsignarAGrupo(null);
//
//            executeInClient("ProgramarVisitaPreventiva.show()");
//
//            //can be created/scheduled
//            //show the creation dialog
//        } catch (Exception e) {
//            Log.createLogger(this.getClass().getName()).logSevere("Error al preparar la creacion de la actividad del caso.");
//        }
//    }
//    private boolean validarSiPuedeProgramarVP(Caso caso) {
//        //validation
//        if (caso.getIdSubComponente() == null) {
//            addWarnMessage("Para poder programar la visita preventiva, este caso debe estar asociado a un producto, favor seleccione el producto.");
//            return false;
//        }
//        if (caso.getIdSubComponente().getFechaEntrega() == null) {
//            addWarnMessage("Para poder programar la visita preventiva, el producto " + caso.getIdSubComponente()
//                    + " debe tener fecha de entrega, favor modifique el producto e ingrese dicha fecha.");
//            return false;
//        }
//        if (caso.hasOpenSubCasos()) {
//            addWarnMessage("Para poder programar la visita preventiva, este caso no debe tener sub casos abiertos pendientes de resolución.");
//            return false;
//        }
//        if (caso.getEmailCliente() == null) {
//            addWarnMessage("Para poder programar la visita preventiva, este caso debe estar asociado a un cliente con el cúal se coordina la visita.");
//            return false;
//        }
//        return true;
//    }
    private Date getFechaVisitaPreventiva(Caso caso) {
        Date fechaEntrega = caso.getIdSubComponente().getFechaEntrega();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaEntrega);
        cal.add(Calendar.MONTH, 6);
        return cal.getTime();
    }

    public void generarPDFVisitaPreventivaPostventa() {
        try {
//            Archivo a = getJpaController().find(Archivo.class,  getSelected().getIdProducto().getIdLogo());  
            List<Item> itemsAReparar = new LinkedList<Item>();
            for (Caso caso : getSelected().getCasosHijosList()) {
                if (caso.isOpen() && caso.getIdItem() != null) {
                    itemsAReparar.add(caso.getIdItem());
                }
            }

            if (!itemsAReparar.isEmpty()) {
                byte[] bytearray = ReportsManager.createVisitaPreventivaPostventa(getSelected(), getFechaVisitaPreventiva(current), getFechaVisitaPreventiva(current), itemsAReparar);

                final String nombre = "VisitaPreventiva_Postventa_" + getSelected().getIdCaso() + ".pdf";
                Attachment attach = getManagerCasos().crearAdjunto(bytearray, null, this.current, nombre, "application/pdf", (long) bytearray.length);
                final String msg = "Documento " + attach.getNombreArchivo() + " creado con exito";
                textoNota = textoNota + "<br/>" + msg;
                armarNota(current, false, textoNota, EnumTipoNota.NOTA.getTipoNota());
                getJpaController().merge(this.current);
                addInfoMessage(msg);
                executeInClient("genDocVisitaPreventiva.hide()");
            } else {
                addWarnMessage("No se puede generar el documento sin Items a Reparar.");
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "createVisitaPreventivaPostventa", e);
            JsfUtil.addErrorMessage("Ocurrió un error al generar el documento, favor intente más tarde.");
        }
    }

    //fin visita preventiva
    public String reabrirCaso() {

        List<AuditLog> changeLog = new ArrayList<AuditLog>();

        try {
            //TODO implementar tipo-> SubEstado selection in reabrir dialog
            current.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado());
            current.setFechaModif(Calendar.getInstance().getTime());
            current.setFechaCierre(null);
            changeLog.add(ManagerCasos.createLogReg(current, "Estado", "reabre el caso", ""));
            getJpaController().mergeCaso(current, changeLog);
            JsfUtil.addSuccessMessage(resourceBundle.getString("caso.abrir.ok"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(resourceBundle.getString("caso.abrir.nook"));
        }

        return "/script/caso/Edit";
    }

    public String guardarBorrador() {
        //List<AuditLog> changeLog = new ArrayList<AuditLog>();
        try {
            current.setRespuesta(textoNota);
            //current.setFechaModif(Calendar.getInstance().getTime());
            //changeLog.add(ManagerCasos.createLogReg(current, "Borrador", "Se crea borrador de respuesta", ""));
            //getJpaController().mergeCaso(current, changeLog);
            JsfUtil.addSuccessMessage("Borrador guardado");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, "No se ha guardado el borrador");
        }

        return "/script/caso/Edit";
    }

    public String agregarRelacionado() {

        try {

            Caso casorel = getJpaController().getCasoFindByIdCaso(new Long(idCaserel));
            if (new Long(idCaserel).equals(current.getIdCaso())) {
                throw new Exception();
            }
            if (casorel != null) {
                current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
                casorel.getCasosRelacionadosList().add(current);
                getJpaController().mergeCaso(casorel, getManagerCasos().verificaCambios(casorel));
                current.getCasosRelacionadosList().add(casorel);
                getJpaController().mergeCaso(current, getManagerCasos().verificaCambios(current));

                idCaserel = "";
                current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
                JsfUtil.addSuccessMessage(resourceBundle.getString("casorel.ok"));
                //ManagerCasos.createLogReg(current, "Casos Relacionados", idCaserel, "");
            } else {
                idCaserel = "";
                JsfUtil.addErrorMessage(resourceBundle.getString("casorel.notfound"));
            }
        } catch (Exception e) {
            idCaserel = "";
            current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
            JsfUtil.addErrorMessage(resourceBundle.getString("casorel.nook"));
        }
        current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
        return "/script/caso/Edit";
    }

    public void asignarCaso() {

        List<AuditLog> changeLog = new ArrayList<AuditLog>();

        try {
            String motivo = textoNota;

            if (tipoTransferOption == 1) {
                //change agent
                if (usuarioSeleccionadoTransfer != null && !usuarioSeleccionadoTransfer.equals(current.getOwner())) {
                    String idUsuarioNuevo = usuarioSeleccionadoTransfer.getIdUsuario();
                    String idUsuarioAnterior = current.getOwner() == null ? "No asignado" : current.getOwner().getIdUsuario();

                    current.setOwner(usuarioSeleccionadoTransfer);
                    changeLog.add(ManagerCasos.createLogReg(current, "Asignar caso", idUsuarioNuevo, idUsuarioAnterior));
                    Nota nota = this.armarNota(current, false, motivo, EnumTipoNota.TRANSFERENCIA_CASO.getTipoNota());
                    changeLog.add(ManagerCasos.createLogReg(current, "Agente agrega nota", "Agente agrega una actividad tipo " + nota.getTipoNota().getNombre(), ""));

                    getJpaController().mergeCaso(current, changeLog);
                    addInfoMessage("El caso se ha tranferido con éxito al agente " + usuarioSeleccionadoTransfer.getIdUsuario());

                    if (ApplicationConfig.isSendNotificationOnTransfer()) {
//                        if (current.getIdArea() != null && current.getIdArea().getIdArea() != null) {
                        try {
                            MailNotifier.notifyCasoAssigned(current, motivo);
                        } catch (Exception ex) {
                            addWarnMessage(ex.getMessage());
                        }
//                        } else {
//                            addWarnMessage("No se envia notificación por correo al agente asignado, dado que el area es null.");
//                        }
                    }

                } else {
                    addWarnMessage("El agente seleccionado no puede ser vacío ni igual al agente actual.");
                }
            } else if (tipoTransferOption == 2) {
                //change client
                if (emailClienteSeleccionadoTransfer != null) {
                    String idUsuarioNuevo = emailClienteSeleccionadoTransfer.getEmailCliente();
                    String idUsuarioAnterior = current.getEmailCliente() == null ? "Sin Cliente" : current.getEmailCliente().getEmailCliente();

                    current.setEmailCliente(emailClienteSeleccionadoTransfer);

                    changeLog.add(ManagerCasos.createLogReg(current, "Cambio de Cliente", idUsuarioNuevo, idUsuarioAnterior));
                    getJpaController().mergeCaso(current, changeLog);

                    JsfUtil.addSuccessMessage("El caso se ha tranferido con éxito al cliente " + emailClienteSeleccionadoTransfer.getCliente().getCapitalName());
                }
            } else {
                //error
                JsfUtil.addErrorMessage("Error!!!");
            }

            recreateModel();

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("transferir.hide()");

//            current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            addErrorMessage("Error al tratar de transferir el caso");
        }

//        return "/script/caso/Edit";
    }

    public String tomarCaso() {

        List<AuditLog> changeLog = new ArrayList<AuditLog>();

        try {
            current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
            if (current.getOwner() == null) {
                current.setOwner(userSessionBean.getCurrent());
                current.setRevisarActualizacion(false);
                changeLog.add(ManagerCasos.createLogReg(current, "Asignarmelo", current.getOwner().getIdUsuario(), ""));
                getJpaController().mergeCaso(current, changeLog);
                JsfUtil.addSuccessMessage(resourceBundle.getString("tomarcasoOK"));
            } else {
                JsfUtil.addSuccessMessage("El caso ya ha sido tomado por " + current.getOwner().toString());
            }

            if (current.getCasosHijosList() != null) {
                for (Caso casoHijo : current.getCasosHijosList()) {
                    if (casoHijo.getOwner() == null) {
                        casoHijo.setOwner(userSessionBean.getCurrent());
                        casoHijo.setRevisarActualizacion(false);
                        changeLog.add(ManagerCasos.createLogReg(casoHijo, "Asignarmelo", current.getOwner().getIdUsuario(), ""));
                        getJpaController().mergeCaso(casoHijo, changeLog);
                    } else {
                        JsfUtil.addSuccessMessage("No se puede asignar el caso N°" + casoHijo.getIdCaso() + ", ya ha sido asignado a " + casoHijo.getOwner().toString());
                    }
                }
            }
        } catch (Exception e) {
//            //System.out.println("tomar caso, exception: " + e.getClass().getName());
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, resourceBundle.getString("tomarcasoNOOK"));
        }

        return "/script/caso/Edit";

    }

    /**
     * TODO deprecate this code
     *
     * @param caso
     * @return
     */
    public String evaluarStyle(Caso caso) {
        if (caso != null) {
            if (caso.getIdSubEstado() == null) {
                return "";
            }

            if ((caso.getTipoCaso() != null) && (caso.getTipoCaso().equals(EnumTipoCaso.INTERNO.getTipoCaso()))) {
                if (caso.getRevisarActualizacion()) {
                    return "notaactualizado";
                } else {
                    return "nota";
                }
            } else {
                if (caso.getRevisarActualizacion()) {
                    return "actualizado";
                }
            }
        }
        return "";

    }

    /**
     * Arma una nota y la agrega al caso.
     *
     * @return
     */
    private Nota armarNota(Caso caso, boolean publica, String texto, TipoNota tipo) {
//        boolean guardarNota = true;
        try {
            Nota nota = new Nota();
            nota.setFechaCreacion(Calendar.getInstance().getTime());
            nota.setCreadaPor(userSessionBean.getCurrent());
            nota.setIdCaso(caso);
            nota.setTexto(HtmlUtils.removeScriptsAndStyles(texto));
            nota.setTipoNota(tipo);
            nota.setVisible(publica);

            if (caso.getOwner() != null) {
                if (!nota.getCreadaPor().getIdUsuario().equals(caso.getOwner().getIdUsuario())) {
                    caso.setRevisarActualizacion(true);
                }
            }

            agregarNotaACaso(nota);
            return nota;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage("ha ocurrido un error, nota no agregada");
            return null;
        }

    }

    /**
     * guarda la nota en base de datos y la agrega al caso
     *
     * @param nota
     * @throws Exception
     */
    private void agregarNotaACaso(Nota nota) throws Exception {

//        getJpaController().refresh(current);
        getJpaController().getNotaJpaController().create(nota);
//        getJpaController().persist(nota);
        this.justCreadedNotaId = nota.getIdNota();
        List<Nota> notas = current.getNotaList();
        if (notas == null) {
            notas = new LinkedList<Nota>();
            current.setNotaList(notas);
        }
        notas.add(nota);
        current.setFechaModif(Calendar.getInstance().getTime());

        JsfUtil.addSuccessMessage(resourceBundle.getString("NotaCreated"));

        textoNota = null;

        if (tipoNotas != null && tipoNotas.isEmpty()) {
            tipoNotas.clear();
        }
    }

    public String crearNota(boolean notifyClient) {
//        listaActividadesOrdenada = null;
//        tipoNota = EnumTipoNota.NOTA.getTipoNota();

        try {
            Nota nota = this.armarNota(current, textoNotaVisibilidadPublica, textoNota, EnumTipoNota.NOTA.getTipoNota());
            getJpaController().mergeCaso(current, ManagerCasos.createLogReg(current, "Agente agrega nota", "Agente agrega una actividad tipo " + nota.getTipoNota().getNombre(), ""));
            if (notifyClient) {
                notifyClient();
            }
            selectedClipping = null;//TODO: CHECK IF NOTA WAS CREATED FIRST
            adjuntarArchivosARespuesta = false;
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RollbackFailureException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "/script/caso/Edit";
    }

    private void notifyClient() {
        String correoEnviado = null;
        try {
            List<AuditLog> changeLog = new ArrayList<AuditLog>();

            correoEnviado = MailNotifier.emailClientCasoUpdatedByAgent(current);

            if (correoEnviado != null) {
                changeLog.add(ManagerCasos.createLogReg(current, "notificacion actualización (email)", correoEnviado, ""));

                Nota n = this.armarNota(current, true, "Cliente ha sido notificado por email sobre la actualización del caso. <br/>Email: <br/>" + correoEnviado,
                        EnumTipoNota.NOTIFICACION_UPDATE_CASO.getTipoNota());
                if (n != null) {
                    changeLog.add(ManagerCasos.createLogReg(current, "Se registra envio email", correoEnviado, ""));
                }

                JsfUtil.addSuccessMessage("Notificación enviada al cliente exitósamente.");

            } else {
                changeLog.add(ManagerCasos.createLogReg(current, "Envio Correo de Respuesta falló", "", ""));
            }

            getJpaController().mergeCaso(current, changeLog);

        } catch (Exception ex) {
            Logger.getLogger(CasoController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String editNota(boolean notifyClient) {
        try {
//            listaActividadesOrdenada = null;
            //        tipoNota = EnumTipoNota.NOTA.getTipoNota();
            getJpaController().merge(selectedNota);
            JsfUtil.addSuccessMessage("El comentario fue actualizado exitósamente.");

            if (notifyClient) {
                notifyClient();
            }

        } catch (Exception ex) {
            JsfUtil.addSuccessMessage("El comentario no pudo ser actualizado.");
            Logger
                    .getLogger(CasoController.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return "/script/caso/Edit";
    }

    /**
     * Mobile responder
     *
     * @return
     */
    public String responderMobile() {

        boolean result = createEmailComment();
        if (result) {
            return "pm:actividadesCaso";
        } else {
            return null;
        }

    }

    /**
     * responder actionListener desde web page.
     */
    public void responder() {

        createEmailComment();
        //return "/script/caso/Edit";
    }

    private boolean createEmailComment() {
        List<AuditLog> changeLog = new ArrayList<AuditLog>();
        boolean correoAgendado = false;
        try {
            progresoEnvioRespuesta = 0;

            String bodyTxt = textoNota.trim();

            if (bodyTxt.isEmpty()) {
                JsfUtil.addErrorMessage("La respuesta no tiene texto, verifíque e intente nuevamente");
                progresoEnvioRespuesta = 100;
                return false;
            }
            //TODO: create an Object Builder factory
            Nota nuevaNota = this.armarNota(current, true, textoNota.trim(), EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota());
            if (nuevaNota != null) {

//                changeLog.add(ManagerCasos.createLogReg(current, "Se crea Respuesta Caso", EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota().getNombre(), ""));
                //TODO Change it
                String subject = "Re: " + ManagerCasos.formatIdCaso(current.getIdCaso()) + " " + current.getTema() + " - " + resourceBundle.getString("email.tituloNotas");
                correoAgendado = enviarCorreo(current.getEmailCliente().getEmailCliente(), subject, bodyTxt, nuevaNota);

                if (correoAgendado) {
//                    current.setRespuesta(null);
//                    current.setFechaModif(Calendar.getInstance().getTime());
//                    current.setFechaRespuesta(Calendar.getInstance().getTime());
                    getPrimefacesRequestContext().scrollTo("inputPanel:formact:panelActividades");
                    changeLog.add(ManagerCasos.createLogReg(current, "Envio de Correo de Respuesta enviado ok", userSessionBean.getCurrent().getIdUsuario() + " envía correo de respuesta.", ""));

                } else {
                    changeLog.add(ManagerCasos.createLogReg(current, "Envio de Correo de Respuesta falló", "Envio de Correo de Respuesta falló", ""));
                }

            } else {
                changeLog.add(ManagerCasos.createLogReg(current, "Respuesta fallida", "Envío de correo de Respuesta falló.", ""));
            }

            getJpaController().mergeCaso(current, changeLog);

//            JsfUtil.addSuccessMessage("Respuesta enviada exitósamente.");
//            listaActividadesOrdenada = null;
            selectedClipping = null;//
            adjuntarArchivosARespuesta = false;

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "mail send exception in createEmailComment", e);
            JsfUtil.addErrorMessage(e, resourceBundle.getString("respuestaCasoNOOK"));
            return false;
        } finally {
            progresoEnvioRespuesta = 100;
        }

        return correoAgendado;
    }

    private boolean enviarCorreo(final String emailCliente, final String subject, String mensaje, Nota nota) throws NumberFormatException {
        boolean sended = false;

        StringBuilder listIdAtt = new StringBuilder();
//        String texto = (nota != null && nota.getTexto() != null) ? nota.getTexto() : "";

        if (selectedAttachmensForMail != null) {
//            StringBuilder attachmentsNames = new StringBuilder();
//            attachments = new ArrayList<EmailAttachment>(selectedAttachmensForMail.size());
            Iterator iteradorAttachments = selectedAttachmensForMail.iterator();
//            int indexAtt = 0;
            while (iteradorAttachments.hasNext()) {
                Long idatt = new Long((String) iteradorAttachments.next());
                listIdAtt.append(idatt);
                listIdAtt.append(';');
//                Attachment att = getJpaController().getAttachmentFindByIdAttachment(idatt);
//                attachmentsNames.append(att.getNombreArchivo()).append("<br/>");
//                indexAtt++;
//                progresoEnvioRespuesta = (int) (((50f / (float) selectedAttachmensForMail.size())) * indexAtt);
            }
        }

        if (incluirHistoria) {
//            StringBuilder nuevoTextoNota = new StringBuilder(texto);
//            nuevoTextoNota.append("<br/><b>Se ha agregado historia del caso</b><br/>");

            mensaje = mensaje != null ? mensaje : "";

            StringBuilder textoMensaje = new StringBuilder(mensaje);
            textoMensaje.append(obtenerHistorial());
            mensaje = textoMensaje.toString();
            incluirHistoria = false;
        }

        try {
            Canal canal = MailNotifier.chooseDefaultCanalToSendMail(current);
            HelpDeskScheluder.scheduleSendMailNota(canal.getIdCanal(), mensaje, emailCliente, subject, current.getIdCaso(), nota.getIdNota(), listIdAtt.toString());
            sended = true;
        } catch (Exception ex) {
            sended = false;
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, "enviarCorreo", ex);
        } finally {
            if (!sended) {
                JsfUtil.addErrorMessage(resourceBundle.getString("respuestaCasoNOOK"));
            }
        }
        return sended;
    }

    public String upload() {
//        //System.out.println("upload()");
        if (uploadFile != null) {
//            //System.out.println("Succesful " + uploadFile.getFileName() + " is uploaded.");
            FacesMessage msg = new FacesMessage("Succesful", uploadFile.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return null;
    }

    private String obtenerHistorial() {
        StringBuilder sbuilder = new StringBuilder("<br/><hr/><b>HISTORIA DEL CASO</b><br/>");
        if (current != null && current.getNotaList() != null) {
            for (Nota nota : current.getNotaList()) {
                if (nota.getTipoNota().equals(EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota()) || nota.getTipoNota().equals(EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota())) {
                    sbuilder.append(creaMensajeOriginal(current, nota));
                }
            }
        }

        sbuilder.append(creaMensajeOriginal(current));
        return sbuilder.toString();
    }

    private String creaMensajeOriginal(Caso caso) {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("<br/>");
        sbuilder.append("INICIO MENSAJE ORIGINAL");
        sbuilder.append("<br/>");
        sbuilder.append("Email cliente: ");
        sbuilder.append(caso.getEmailCliente());
        sbuilder.append("<br/>");
        sbuilder.append("Asunto: ");
        sbuilder.append(caso.getTema());
        sbuilder.append("<br/>");
        sbuilder.append("Fecha: ");
        sbuilder.append(caso.getFechaCreacion());
        sbuilder.append("<br/>");
        sbuilder.append("Mensaje: ");
        sbuilder.append("<br/>");
        sbuilder.append(replaceEmbeddedImages(caso.getDescripcion()));
        sbuilder.append("<br/>");
        sbuilder.append("FIN MENSAJE ORIGINAL");
        sbuilder.append("<hr/>");
        return sbuilder.toString();
    }

    private String creaMensajeOriginal(Caso caso, Nota nota) {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("INICIO ");
        sbuilder.append(nota.getTipoNota().getNombre().toUpperCase());
        sbuilder.append("<br/>");
        sbuilder.append("email cliente: ");
        sbuilder.append(caso.getEmailCliente());
        sbuilder.append("<br/>");
        sbuilder.append("Fecha: ");
        sbuilder.append(nota.getFechaCreacion());
        sbuilder.append("<br/>");
        sbuilder.append("Mensaje: ");
        sbuilder.append("<br/>");
        sbuilder.append(replaceEmbeddedImages(nota.getTexto()));
        sbuilder.append("<br/>");
        sbuilder.append("FIN ");
        sbuilder.append(nota.getTipoNota().getNombre().toUpperCase());
        sbuilder.append("<hr/>");
        return sbuilder.toString();
    }

    public void deleteAttachment(ActionEvent actionEvent) {
        try {
            Attachment att = getJpaController().getAttachmentFindByIdAttachment(new Long(idFileDelete));
            String nombre = att.getNombreArchivo();
            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(att.getIdAttachment());
            getJpaController().removeArchivo(archivo);
            getJpaController().removeAttachment(att);

            current = getJpaController().getCasoFindByIdCaso(current.getIdCaso());
            JsfUtil.addSuccessMessage("Archivo " + nombre + " borrado");
            getJpaController().persistAuditLog(ManagerCasos.createLogReg(current, "Archivo borrado", "Archivo borrado: " + nombre, ""));

        } catch (Exception e) {
            JsfUtil.addSuccessMessage("No se ha podido borrar el archivo");
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido borrar el archivo", e);
        }
    }

    public void removeAttachment(Long idAtt) {
        try {
            if (current.getAttachmentList() != null) {
                for (Attachment attachment : current.getAttachmentList()) {
                    if (attachment.getIdAttachment().equals(idAtt)) {
                        current.getAttachmentList().remove(attachment);
                        JsfUtil.addSuccessMessage("Archivo " + attachment.getNombreArchivo() + " removido de la lista.");
                    }
                }
            }

        } catch (Exception e) {
            JsfUtil.addErrorMessage("Ocurrio un error no esperado, No se ha podido remover el archivo.");
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido borrar el archivo", e);
            //e.printStackTrace();
        }
    }

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
            JsfUtil.addErrorMessage(resourceBundle.getString("attachment.error"));
            return new DefaultStreamedContent();
        }
    }

    /**
     * Brotec icafal specific
     *
     * @return
     */
    public StreamedContent generarActaPreEntrega() {
        try {
            Long idLogo = null;
            Archivo archivoLogo = null;
            if (getSelected().getIdProducto() != null) {
                if (getSelected().getIdProducto().getIdLogo() != null) {
                    idLogo = getSelected().getIdProducto().getIdLogo();
                    archivoLogo
                            = getJpaController().find(Archivo.class, idLogo);
                }
            }

            byte[] data = ReportsManager.createActaPreEntrega(getSelected(),
                    (archivoLogo == null) ? null : new ByteArrayInputStream(archivoLogo.getArchivo()));
            return new DefaultStreamedContent(
                    new ByteArrayInputStream(data), "application/pdf", "acta_preentrega_" + getSelected().getIdCaso() + ".pdf");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(resourceBundle.getString("attachment.error"));
            return new DefaultStreamedContent();
        }
    }

    public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {

        Document pdf = (Document) document;
        pdf.open();
        pdf.addAuthor("www.itcs.cl");
//        pdf.addHeader("Header1", "header2");
        pdf.addTitle("Itcs, Integrated Solutions.");
        pdf.setMargins(1, 1, 1, 1);
        pdf.setPageSize(PageSize.A4);

//        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
//        String logo = servletContext.getRealPath("") + File.separator + "resources" + File.separator + "images" + File.separator + "headerForm.png";
//
//        pdf.add(Image.getInstance(logo));
    }

    public void postProcessXLS(Object document) {
        HSSFWorkbook wb = (HSSFWorkbook) document;
        HSSFSheet sheet = wb.getSheetAt(0);

        HSSFRow header = sheet.getRow(0);

        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.BRIGHT_GREEN.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        for (int i = 0; i < header.getPhysicalNumberOfCells(); i++) {
            HSSFCell cell = header.getCell(i);
            cell.setCellStyle(cellStyle);
            sheet.autoSizeColumn((short) i);
        }
    }

    /**
     *
     * @return
     */
    public StreamedContent exportAllItems() {

        try {
            List<Caso> casos = getJpaController().findCasoEntities(getFilterHelper().getVista(), userSessionBean.getCurrent(), getDefaultOrderBy());//all
            OutputStream output = new ByteArrayOutputStream();
            //Se crea el libro Excel
            WritableWorkbook workbook
                    = Workbook.createWorkbook(output);

            //Se crea una nueva hoja dentro del libro
            WritableSheet sheet
                    = workbook.createSheet("Casos " + getFilterHelper().getVista().toString(), 0);
            WritableCellFormat timesBoldUnderline;
            WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.SINGLE);
            timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
            // Lets automatically wrap the cells
            timesBoldUnderline.setWrap(true);
            DateFormat customDateFormat = new DateFormat("d/m/yy h:mm");
            WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);

            String[] titulos = new String[]{"Fecha Creación", "ID Caso", "Rut", "Nombre", "Apellido", "Telefono", "Telefono adicional", "Ciudad",
                "Comentarios", "Tema", "Email", "Propietario", "Estado", "Subestado", "SLA", "Fecha de cierre", "Modelo", "Canal"};

            int col = 0;
            if (titulos != null) {
                for (String string : titulos) {
                    sheet.addCell(new Label(col, 0, string, timesBoldUnderline));
                    col++;
                }
            }

            int row = 1;
            if (casos != null) {
                for (Caso caso : casos) {
                    sheet.addCell(new DateTime(0, row, caso.getFechaCreacion(), dateFormat));
                    sheet.addCell(new Label(1, row, caso.getIdCaso() + ""));
                    if (caso.getEmailCliente() != null && caso.getEmailCliente().getCliente() != null) {
                        sheet.addCell(new Label(2, row, caso.getEmailCliente().getCliente().getRut()));
                        sheet.addCell(new Label(3, row, caso.getEmailCliente().getCliente().getNombres()));
                        sheet.addCell(new Label(4, row, caso.getEmailCliente().getCliente().getApellidos()));
                        sheet.addCell(new Label(5, row, caso.getEmailCliente().getCliente().getFono1()));
                        sheet.addCell(new Label(6, row, caso.getEmailCliente().getCliente().getFono2()));
                        sheet.addCell(new Label(7, row, caso.getEmailCliente().getCliente().getDirParticular()));
                        sheet.addCell(new Label(10, row, caso.getEmailCliente().getEmailCliente()));
                    }
                    sheet.addCell(new Label(8, row, caso.getDescripcionTxt()));
                    sheet.addCell(new Label(9, row, caso.getTema()));
                    if (caso.getOwner() != null) {
                        sheet.addCell(new Label(11, row, caso.getOwner().getCapitalName()));
                    }
                    if (caso.getIdEstado() != null) {
                        sheet.addCell(new Label(12, row, caso.getIdEstado().getNombre()));
                    }
                    if (caso.getIdSubEstado() != null) {
                        sheet.addCell(new Label(13, row, caso.getIdSubEstado().getNombre()));
                    }
                    if (caso.getNextResponseDue() != null) {
                        sheet.addCell(new DateTime(14, row, caso.getNextResponseDue(), dateFormat));
                    }
                    if (caso.getFechaCierre() != null) {
                        sheet.addCell(new DateTime(15, row, caso.getFechaCierre(), dateFormat));
                    }
                    if (caso.getIdModelo() != null) {
                        sheet.addCell(new Label(16, row, caso.getIdModelo().getNombre()));
                    }
                    if (caso.getIdCanal() != null) {
                        sheet.addCell(new Label(17, row, caso.getIdCanal().getNombre()));
                    }

//                for (int i = 0; i <= 15; i++) {
//                    System.out.print(sheet.getCell(i, row).getContents()+";");                    
//                }
//                //System.out.println("");
                    row++;
                }
            }

            workbook.write();
            workbook.close();
            InputStream decodedInput = new ByteArrayInputStream(((ByteArrayOutputStream) output).toByteArray());
            ////System.out.println("Ejemplo finalizado.");
            return new DefaultStreamedContent(
                    decodedInput, "application/vnd.ms-excel", "casos.xls");
        } catch (Exception ex) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage("Ocurrió un problema al generar el Documento Excel. Favor contacte a soporte de sistema.");
            return new DefaultStreamedContent();

        }

    }

    public void subir(FileUploadEvent event) {
        try {
            String nombre = event.getFile().getFileName();
            nombre = nombre.substring(nombre.lastIndexOf(File.separator) + 1);
            if (nombre.lastIndexOf('\\') > 0) {
                nombre = nombre.substring(nombre.lastIndexOf('\\') + 1);
            }
            InputStream is = event.getFile().getInputstream();
            long size = event.getFile().getSize();
            byte[] bytearray = new byte[(int) size];
            is.read(bytearray);

//            //System.out.println("CURRENT:"+this.current);
            if (this.current != null) {
                getManagerCasos().crearAdjunto(bytearray, null, this.current, nombre, event.getFile().getContentType(), event.getFile().getSize());
                JsfUtil.addSuccessMessage("Archivo " + nombre + " subido con exito");
                getJpaController().merge(this.current);
            } else {
                JsfUtil.addErrorMessage("Archivo " + nombre + " no se puede cargar. Favor intente nuevamente.");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage(e, resourceBundle.getString("attachment.error"));
        }
    }

    public void uploadToSaveLater(FileUploadEvent event) {
        try {
            String nombre = event.getFile().getFileName();
            nombre = nombre.substring(nombre.lastIndexOf(File.separator) + 1);
            if (nombre.lastIndexOf('\\') > 0) {
                nombre = nombre.substring(nombre.lastIndexOf('\\') + 1);
            }
            InputStream is = event.getFile().getInputstream();
            long size = event.getFile().getSize();
            byte[] bytearray = new byte[(int) size];
            is.read(bytearray);

            Random randomGenerator = new Random();
            long n = randomGenerator.nextLong();
            if (n > 0) {
                n = n * (-1);
            }

            Archivo archivo = new Archivo(n);
            archivo.setArchivo(bytearray);

            if (current.getAttachmentList() == null) {
                current.setAttachmentList(new ArrayList<Attachment>());
            }

            Attachment attach = new Attachment(n);
            attach.setIdCaso(current);
            attach.setNombreArchivo(nombre);
            attach.setMimeType(event.getFile().getContentType());
            attach.setArchivo(archivo);
            current.getAttachmentList().add(attach);

            //getJpaController().persistAttachment(attach);//later
            //archivo.setIdAttachment(attach.getIdAttachment());run later in create
            //getJpaController().persistArchivo(archivo); // run later in create
            JsfUtil.addSuccessMessage("Archivo " + nombre + " subido éxitosamente.");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error al subir archivo", e);
            JsfUtil.addErrorMessage(e, e.getLocalizedMessage());
        }
    }

    public String mergeCliente() {
        try {
            getJpaController().mergeCliente(current.getEmailCliente().getCliente());
            JsfUtil.addSuccessMessage("Cliente actualizado exitosamente.");
            return "/script/caso/Edit";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return null;
        }
    }

    public String update() {
        try {
            update(current);
            return "/script/caso/Edit";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, resourceBundle.getString("PersistenceErrorOccured"), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
            return null;
        }

    }

    public void updateListener() {
        update();
    }

    public void update(Caso casoToUpdate) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {

        if ((null != casoToUpdate.getIdCliente().getRut()) && (!casoToUpdate.getIdCliente().getRut().isEmpty())) {
            if (!UtilesRut.validar(casoToUpdate.getIdCliente().getRut())) {
                JsfUtil.addErrorMessage("Rut invalido");
                return;
            }
        }
//////            if (casoToUpdate.getOwner() != null) {
//////                Usuario actualUser = userSessionBean.getCurrent();
//////                if (!casoToUpdate.getOwner().equals(actualUser)) {
//////                    //RequestContext.getCurrentInstance().push(current.getOwner().getIdUsuario(), "Se ha actualizado el caso: "+current.getIdCaso());
//////                    ////System.out.println("\n\n\npushed!!\n\n\n");
//////                }
//////            }

        casoToUpdate.setFechaModif(Calendar.getInstance().getTime());
        List<AuditLog> lista = getManagerCasos().verificaCambios(casoToUpdate);

        if (casoToUpdate.getIdRecinto() != null && null == getJpaController().find(Recinto.class, casoToUpdate.getIdRecinto())) {
            Recinto r = new Recinto();

            r.setIdRecinto(casoToUpdate.getIdRecinto());
            r.setNombre(casoToUpdate.getIdRecinto());
            getJpaController()
                    .persist(r);
        }

        getJpaController().mergeCaso(casoToUpdate, lista);

        //re-schedule alerts
        if (lista != null) {
            for (AuditLog auditLog : lista) {
                if (auditLog.getCampo().equalsIgnoreCase(Caso_.nextResponseDue.getName())) {
                    getManagerCasos().evaluarEstadoDelCaso(casoToUpdate);
                }
            }
        }

        JsfUtil.addSuccessMessage(resourceBundle.getString("CasoUpdated"));

    }

    public String updateDescripcion() {
        String salida = update();
//        if (salida != null) {
//            ManagerCasos.createLogReg("Descripcion", "Descripcion actualizada", "");
//        }
        return salida;
    }

    public String destroy() {
        if (current == null) {
            return null;
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (performDestroy(current)) {
            JsfUtil.addSuccessMessage("El caso #" + current.getIdCaso() + " fue eliminado exitósamente.");
            try {
                HelpDeskScheluder.unscheduleCambioAlertas(current.getIdCaso());

            } catch (SchedulerException ex) {
                Logger.getLogger(CasoController.class
                        .getName()).log(Level.SEVERE, "unscheduleTask", ex);
            }
            actualizaArbolDeCategoria();
            current = null;
            recreateModel();
            recreatePagination();
            return "inbox";
        } else {
            addErrorMessage("El caso #" + current.getIdCaso() + " no se pudo eliminar.");
            return null;
        }
    }

    public void destroySelected() {
        try {
            if (getSelectedItems() != null) {
                int countDeleted = 0;
                ArrayList<Caso> notDeleted = new ArrayList<Caso>();
                if (getSelectedItems().size() <= 0) {
                    JsfUtil.addErrorMessage("Debe seleccionar al menos un caso.");
                } else {
                    if (getSelectedItems() != null) {
                        for (Caso cs : getSelectedItems()) {
                            if (performDestroy(cs)) {
                                countDeleted++;
                            } else {
                                addErrorMessage("El caso #" + cs.getIdCaso() + " no se pudo eliminar.");
                                notDeleted.add(cs);
                            }
                        }
                    }

                    JsfUtil.addSuccessMessage(countDeleted + " Casos fueron eliminados exitósamente.");
                }
                selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
                setSelectedItems(notDeleted);
                recreateModel();
                recreatePagination();
            }

        } catch (Exception e) {
            addErrorMessage(e.getLocalizedMessage());
        }
    }

    private boolean performDestroy(Caso caso) {
        try {
            getJpaController().persistAuditLog(ManagerCasos.createLogReg(caso, "Eliminar", userSessionBean.getCurrent().getIdUsuario() + " elimina el caso manualmente", ""));
            getJpaController().remove(Caso.class, caso);
            return true;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error al tratar de eliminar caso " + caso.toString() + ":" + e.getMessage(), e);
            return false;
        }
    }

    public int getSelectedItemsCount() {
        try {
            return getSelectedItems().size();
        } catch (Exception e) {
            //ignore.
        }
        return 0;
    }

    public void onRowSelect(SelectEvent event) {
        try {
            Caso caso = (Caso) event.getObject();
            if (caso != null) {
                current = caso;
                redirect("/script/caso/Edit.xhtml");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error onRowSelect", e);
            addErrorMessage("Lo sentimos, no se pudo recuperar la información del caso.");
        }

    }

    public List<Attachment> getSelectedAttachmensForMail() {
        return selectedAttachmensForMail;
    }

    public void setSelectedAttachmensForMail(List<Attachment> selectedAttachmensForMail) {
        this.selectedAttachmensForMail = selectedAttachmensForMail;
    }

    @Override
    protected void recreateModel() {
        this.items = null;
//        this.recreatePagination();
//        this.selectedItems = null; //not need to do this man
        this.emailClienteSeleccionadoTransfer = null;
        this.usuarioSeleccionadoTransfer = null;
    }

    public SelectItem[] getClippingsItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(getJpaController().getClippingJpaController().findClippingEntities(), true);
    }

    public void handleClippingSelectChangeEvent() {
        textoNota = null;
        if (selectedClipping != null && selectedClipping.getTexto() != null) {
            textoNota = ClippingsPlaceHolders.buildFinalText(selectedClipping.getTexto(), current);
        } else {
            addErrorMessage("No se ha seleccionado ningun clipping.");
        }

    }

    public SelectItem[] getItemsSubEstadoCasoAvailableSelectOneCasoCerrado() {
        List<SubEstadoCaso> lista = new ArrayList<SubEstadoCaso>();
        try {
            if (current.getTipoCaso() != null) {
                final List<SubEstadoCaso> subEstadoCasofindByIdEstadoAndTipoCaso = getJpaController().getSubEstadoCasofindByIdEstadoAndTipoCaso(EnumEstadoCaso.CERRADO.getEstado(), current.getTipoCaso());
                if (subEstadoCasofindByIdEstadoAndTipoCaso != null) {
                    for (SubEstadoCaso subEstadoCaso : subEstadoCasofindByIdEstadoAndTipoCaso) {
                        if (!lista.contains(subEstadoCaso)) {
                            lista.add(subEstadoCaso);
                        }
                    }
                }

            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        return JsfUtil.getSelectItems(lista, false);
    }

    public SelectItem[] getItemsSubEstadoCasoAvailableSelectOneCasoAbierto() {
        List<SubEstadoCaso> lista = new ArrayList<SubEstadoCaso>();

        try {
            if (current.getTipoCaso() != null) {
                final List<SubEstadoCaso> subEstadoCasofindByIdEstadoAndTipoCaso = getJpaController().getSubEstadoCasofindByIdEstadoAndTipoCaso(EnumEstadoCaso.ABIERTO.getEstado(), current.getTipoCaso());
                if (subEstadoCasofindByIdEstadoAndTipoCaso != null) {
                    for (SubEstadoCaso subEstadoCaso : subEstadoCasofindByIdEstadoAndTipoCaso) {
                        if (!lista.contains(subEstadoCaso)) {
                            lista.add(subEstadoCaso);
                        }
                    }
                }

            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }

        return JsfUtil.getSelectItems(lista, false);
    }

    /**
     * @return the uploadFile
     */
    public UploadedFile getUploadFile() {
        return uploadFile;
    }

    /**
     * @param uploadFile the uploadFile to set
     */
    public void setUploadFile(UploadedFile uploadFile) {
        this.uploadFile = uploadFile;
    }

    /**
     * @return the cantidadDeRespuestasACliente
     */
    public int getCantidadDeRespuestasACliente() {
        return cantidadDeRespuestasACliente;
    }

    /**
     * @return the cantidadDeRespuestasAlCliente
     */
    public int getCantidadDeRespuestasDelCliente() {
        return cantidadDeRespuestasDelCliente;
    }

    /**
     * @return the emailCliente_wizard
     */
    public String getEmailCliente_wizard() {
        return emailCliente_wizard;
    }

    /**
     * @param emailCliente_wizard the emailCliente_wizard to set
     */
    public void setEmailCliente_wizard(String emailCliente_wizard) {
        this.emailCliente_wizard = emailCliente_wizard;
    }

    private boolean compararStrings(String header, String value) {
        boolean resp = header.equals(value);
        ////System.out.println("comparando "+header+" vs "+value+" resp="+resp);
        return resp;
    }

    /**
     * @return the emailCliente_wizard_existeEmail
     */
    public boolean isEmailCliente_wizard_existeEmail() {
        return emailCliente_wizard_existeEmail;
    }

    /**
     * @param emailCliente_wizard_existeEmail the
     * emailCliente_wizard_existeEmail to set
     */
    public void setEmailCliente_wizard_existeEmail(boolean emailCliente_wizard_existeEmail) {
        this.emailCliente_wizard_existeEmail = emailCliente_wizard_existeEmail;
    }

    /**
     * @return the emailCliente_wizard_existeCliente
     */
    public boolean isEmailCliente_wizard_existeCliente() {
        return emailCliente_wizard_existeCliente;
    }

    /**
     * @param emailCliente_wizard_existeCliente the
     * emailCliente_wizard_existeCliente to set
     */
    public void setEmailCliente_wizard_existeCliente(boolean emailCliente_wizard_existeCliente) {
        this.emailCliente_wizard_existeCliente = emailCliente_wizard_existeCliente;
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

    private String createEmbeddedImage(String contentId) {
        try {
            Attachment att = getJpaController().getAttachmentFindByContentId('<' + contentId + '>', current);
            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(att.getIdAttachment());
            if (archivo != null) {
                String base64Image = Base64.encodeBase64String(archivo.getArchivo());
                return base64Image;
            }

        } catch (Exception ex) {
            Log.createLogger(this.getClass().getName()).log(Level.WARNING, "no se encuentra el archivo: " + contentId, ex);
        }
        return null;
    }

    /**
     * @return the activeIndexMenuAccordionPanel
     */
    public int getActiveIndexMenuAccordionPanel() {
        return activeIndexMenuAccordionPanel;
    }

    /**
     * @param activeIndexMenuAccordionPanel the activeIndexMenuAccordionPanel to
     * set
     */
    public void setActiveIndexMenuAccordionPanel(int activeIndexMenuAccordionPanel) {
        this.activeIndexMenuAccordionPanel = activeIndexMenuAccordionPanel;
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    /**
     * @return the visibilityOption
     */
    public Integer getVisibilityOption() {
        return visibilityOption;
    }

    /**
     * @param visibilityOption the visibilityOption to set
     */
    public void setVisibilityOption(Integer visibilityOption) {
        this.visibilityOption = visibilityOption;
    }

    /**
     * @return the selectedClipping
     */
    public Clipping getSelectedClipping() {
        return selectedClipping;
    }

    /**
     * @param selectedClipping the selectedClipping to set
     */
    public void setSelectedClipping(Clipping selectedClipping) {
        this.selectedClipping = selectedClipping;
    }

    /**
     * @return the emailCliente_wizard_updateCliente
     */
    public boolean isEmailCliente_wizard_updateCliente() {
        return emailCliente_wizard_updateCliente;
    }

    /**
     * @param emailCliente_wizard_updateCliente the
     * emailCliente_wizard_updateCliente to set
     */
    public void setEmailCliente_wizard_updateCliente(boolean emailCliente_wizard_updateCliente) {
        this.emailCliente_wizard_updateCliente = emailCliente_wizard_updateCliente;
    }

    /**
     * @return the rutCliente_wizard
     */
    public String getRutCliente_wizard() {
        return rutCliente_wizard;
    }

    /**
     * @param rutCliente_wizard the rutCliente_wizard to set
     */
    public void setRutCliente_wizard(String rutCliente_wizard) {
        this.rutCliente_wizard = rutCliente_wizard;
    }

    /**
     * @return the textoNotaVisibilidadPublica
     */
    public boolean isTextoNotaVisibilidadPublica() {
        return textoNotaVisibilidadPublica;
    }

    /**
     * @param textoNotaVisibilidadPublica the textoNotaVisibilidadPublica to set
     */
    public void setTextoNotaVisibilidadPublica(boolean textoNotaVisibilidadPublica) {
        this.textoNotaVisibilidadPublica = textoNotaVisibilidadPublica;
    }

    /**
     * @return the swatch
     */
    public String getSwatch() {
        return swatch;
    }

    /**
     * @param swatch the swatch to set
     */
    public void setSwatch(String swatch) {
        this.swatch = swatch;
    }

    /**
     * @return the selectedNota
     */
    public Nota getSelectedNota() {
        return selectedNota;
    }

    /**
     * @param selectedNota the selectedNota to set
     */
    public void setSelectedNota(Nota selectedNota) {
        this.selectedNota = selectedNota;
    }

    /**
     * @return the tipoTransferOption
     */
    public Integer getTipoTransferOption() {
        return tipoTransferOption;
    }

    /**
     * @param tipoTransferOption the tipoTransferOption to set
     */
    public void setTipoTransferOption(Integer tipoTransferOption) {
        this.tipoTransferOption = tipoTransferOption;
    }

    /**
     * @return the usuarioSeleccionadoTransfer
     */
    public Usuario getUsuarioSeleccionadoTransfer() {
        return usuarioSeleccionadoTransfer;
    }

    /**
     * @param usuarioSeleccionadoTransfer the usuarioSeleccionadoTransfer to set
     */
    public void setUsuarioSeleccionadoTransfer(Usuario usuarioSeleccionadoTransfer) {
        this.usuarioSeleccionadoTransfer = usuarioSeleccionadoTransfer;
    }

    /**
     * @return the emailClienteSeleccionadoTransfer
     */
    public EmailCliente getEmailClienteSeleccionadoTransfer() {
        return emailClienteSeleccionadoTransfer;
    }

    /**
     * @param emailClienteSeleccionadoTransfer the
     * emailClienteSeleccionadoTransfer to set
     */
    public void setEmailClienteSeleccionadoTransfer(EmailCliente emailClienteSeleccionadoTransfer) {
        this.emailClienteSeleccionadoTransfer = emailClienteSeleccionadoTransfer;
    }

    /**
     * @param idFileRemove the idFileRemove to set
     */
    public void setIdFileRemove(Long idFileRemove) {
        this.idFileRemove = idFileRemove;
    }

    /**
     * @return the selectedEtiquetas
     */
    public List<Etiqueta> getSelectedEtiquetas() {
        return selectedEtiquetas;
    }

    /**
     * @param selectedEtiquetas the selectedEtiquetas to set
     */
    public void setSelectedEtiquetas(List<Etiqueta> selectedEtiquetas) {
        this.selectedEtiquetas = selectedEtiquetas;
    }

    /**
     * @param vistaController the vistaController to set
     */
    public void setVistaController(VistaController vistaController) {
        this.vistaController = vistaController;
    }

    /**
     * @return the justCreadedNotaId
     */
    public Integer getJustCreadedNotaId() {
        return justCreadedNotaId;
    }

    /**
     * @param justCreadedNotaId the justCreadedNotaId to set
     */
    public void setJustCreadedNotaId(Integer justCreadedNotaId) {
        this.justCreadedNotaId = justCreadedNotaId;
    }

    /**
     * @return the activeIndexWestPanel
     */
    public String getActiveIndexWestPanel() {
        return activeIndexWestPanel;
    }

    /**
     * @param activeIndexWestPanel the activeIndexWestPanel to set
     */
    public void setActiveIndexWestPanel(String activeIndexWestPanel) {
        this.activeIndexWestPanel = activeIndexWestPanel;
    }

    /**
     * @return the reglaTriggerSelected
     */
    public ReglaTrigger getReglaTriggerSelected() {
        return reglaTriggerSelected;
    }

    /**
     * @param reglaTriggerSelected the reglaTriggerSelected to set
     */
    public void setReglaTriggerSelected(ReglaTrigger reglaTriggerSelected) {
        this.reglaTriggerSelected = reglaTriggerSelected;
    }

    /**
     * @return the accionToRunSelected
     */
    public String getAccionToRunSelected() {
        return accionToRunSelected;
    }

    /**
     * @param accionToRunSelected the accionToRunSelected to set
     */
    public void setAccionToRunSelected(String accionToRunSelected) {
        this.accionToRunSelected = accionToRunSelected;
    }

    /**
     * @return the accionToRunParametros
     */
    public String getAccionToRunParametros() {
        return accionToRunParametros;
    }

    /**
     * @param accionToRunParametros the accionToRunParametros to set
     */
    public void setAccionToRunParametros(String accionToRunParametros) {
        this.accionToRunParametros = accionToRunParametros;
    }

    /**
     * @param applicationBean the applicationBean to set
     */
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    /**
     * @return the activeIndexdescOrComment
     */
    public int getActiveIndexdescOrComment() {
        return activeIndexdescOrComment;
    }

    /**
     * @param activeIndexdescOrComment the activeIndexdescOrComment to set
     */
    public void setActiveIndexdescOrComment(int activeIndexdescOrComment) {
        this.activeIndexdescOrComment = activeIndexdescOrComment;

    }

    /**
     * @return the adjuntarArchivosARespuesta
     */
    public boolean isAdjuntarArchivosARespuesta() {
        return adjuntarArchivosARespuesta;
    }

    /**
     * @param adjuntarArchivosARespuesta the adjuntarArchivosARespuesta to set
     */
    public void setAdjuntarArchivosARespuesta(boolean adjuntarArchivosARespuesta) {
        this.adjuntarArchivosARespuesta = adjuntarArchivosARespuesta;
    }

    /**
     * @return the activeIndexCasoSections
     */
    public int getActiveIndexCasoSections() {
        return activeIndexCasoSections;
    }

    /**
     * @param activeIndexCasoSections the activeIndexCasoSections to set
     */
    public void setActiveIndexCasoSections(int activeIndexCasoSections) {
        this.activeIndexCasoSections = activeIndexCasoSections;
    }

    @FacesConverter(forClass = Caso.class)
    public static class CasoControllerConverter implements Converter, Serializable {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CasoController controller = (CasoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "casoController");
            return controller.getJpaController().find(Caso.class, getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Caso) {
                Caso o = (Caso) object;
                return getStringKey(o.getIdCaso());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CasoController.class.getName());
            }
        }
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
