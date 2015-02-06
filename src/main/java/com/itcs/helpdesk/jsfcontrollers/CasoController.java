package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.ApplicationBean;
import com.itcs.helpdesk.jsfcontrollers.util.FiltroAcceso;
import com.itcs.helpdesk.jsfcontrollers.util.JPAFilterHelper;
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
import com.itcs.helpdesk.persistence.entities.TipoAccion;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.entities.Prioridad;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import com.itcs.helpdesk.persistence.entities.Recinto;
import com.itcs.helpdesk.persistence.entities.ReglaTrigger;
import com.itcs.helpdesk.persistence.entities.ScheduleEvent;
import com.itcs.helpdesk.persistence.entities.SubComponente;
import com.itcs.helpdesk.persistence.entities.SubEstadoCaso;
import com.itcs.helpdesk.persistence.entities.TipoAlerta;
import com.itcs.helpdesk.persistence.entities.TipoNota;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumFunciones;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAccion;
import com.itcs.helpdesk.persistence.entityenums.EnumPrioridad;
import com.itcs.helpdesk.persistence.entityenums.EnumSubEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.exceptions.IllegalOrphanException;
import com.itcs.helpdesk.persistence.jpa.exceptions.NonexistentEntityException;
import com.itcs.helpdesk.persistence.jpa.exceptions.PreexistingEntityException;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.reports.ReportsManager;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.rules.actionsimpl.CrearCasoVisitaRepSellosAction;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.ClassUtils;
import com.itcs.helpdesk.util.ClippingsPlaceHolders;
import com.itcs.helpdesk.util.HtmlUtils;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailNotifier;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.helpdesk.util.NoOutChannelException;
import com.itcs.helpdesk.util.RulesEngine;
import com.itcs.helpdesk.util.UtilesRut;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
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
import org.primefaces.model.UploadedFile;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudItem;
import org.primefaces.model.tagcloud.TagCloudModel;
import org.quartz.SchedulerException;

@ManagedBean(name = "casoController")
@SessionScoped
public class CasoController extends AbstractManagedBean<Caso> implements Serializable {

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
    private List<Etiqueta> selectedEtiquetas = new ArrayList<>();
    private transient HashMap<String, BlackListEmail> blackListMap;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    protected int selectedItemIndex;
    private int activeIndexMenuAccordionPanel = 1;
    private String activeIndexWestPanel = "0";//TODO should be an int
    //Notas
    protected String textoNota = null;
    protected boolean textoNotaVisibilidadPublica = false;
    private boolean responderAOtroEmail = false;
    private boolean cc = false;
    private boolean cco = false;
    private List<String> otroEmail;
    private List<String> ccEmail;
    private List<String> ccoEmail;
//    private TipoNota tipoNota;
//    private float laborTime = 0;
    protected List<String> tipoNotas;
    //Clippings
    private Clipping selectedClipping;
    /*
     * Objetos para filtro
     */
//    private transient TreeNode categoria;
    private String idCasoStr;
    /*
     * Objetos para attachments
     */
    protected transient UploadedFile uploadFile;
    protected String idFileDelete = "";
    private transient List<Attachment> selectedAttachmensForMail;
    protected int cantidadDeNotas;
    protected int cantidadDeRespuestasACliente;
    protected int cantidadDeRespuestasDelCliente;
    //TODO send it to abstract manager 
    static transient ResourceBundle resourceBundle = ResourceBundle.getBundle("Bundle");
//    private List<Nota> listaActividadesOrdenada = null;
//    private boolean incluirHistoria;
//    private Integer progresoEnvioRespuesta;
    private ReglaTrigger reglaTriggerSelected;
    protected String emailCliente_wizard;
    private String tipoAsoc_wizard;
    private SubComponente subComponente_wizard;
    private Cliente cliente_wizard;
    private ProductoContratado pcWizard;
    protected String rutCliente_wizard;
    protected boolean emailCliente_wizard_existeEmail = false;
    protected boolean emailCliente_wizard_existeCliente = false;
    protected boolean emailCliente_wizard_updateCliente = false;
//    private transient ManagerCasos managerCasos;
    private String htmlToView = null;
//    private boolean filtrarPorCategorias;
//    private boolean filtrarPorVista = false;
//    private Vista vista;
//    private transient JPAFilterHelper filterHelper;
    //mobile
    private String swatch = "b";
    private Nota selectedNota;
    // transfer
    private Integer tipoTransferOption = 1;
    private Usuario usuarioSeleccionadoTransfer;
    private EmailCliente emailClienteSeleccionadoTransfer;
    private Long idFileRemove;
    protected Integer justCreadedNotaId;
    protected Integer selectedViewId;//Vista seleccionada
    private String accionToRunSelected;
    private String accionToRunParametros;
//    private int activeIndexdescOrComment;
    protected int activeIndexCasoSections;
    public static final int TAB_ACTIVIDADES_INDEX = 0;
    public static final int TAB_AGENDA_INDEX = 1;
    public static final int TAB_ADJUNTOS_INDEX = 2;
    public static final int TAB_CASOSREL_INDEX = 3;
    public static final int TAB_TIMELINE_INDEX = 4;
    public static final int TAB_EVENTO_INDEX = 5;
    //visitas preventivas, TODO brotec specific
    private static final int visitaPreventivaCrearCasoDiasAntes = 10;
    private static final int visitaPreventivaCrearCasoEnMeses = 6;
    private Long casosPendientes;
    private Long casosPorVencer;
    private Long casosVencidos;
    private Long casosRevisarActualizacion;
    private Long casosPrioritarios;
    private Long casosCerrados;
    //reply-mode
    protected boolean replyMode = false;
    private boolean replyByEmail = true;
    protected boolean searchBarVisible = true;
    //respuesta
    private boolean adjuntarArchivosARespuesta = false;
    private boolean mergeHabilitado;
    private transient LinkedList<Caso> mergeCandidatesList;
    private transient Map<Integer, Boolean> showReducedContentMap;

    public static final String HEADER_HISTORY = "<br/><hr/><b>HISTORIA DEL CASO</b><hr/><br/>";
    public static final String HEADER_HISTORY_NOTA_ALT = "HISTORIA DEL CASO";
    public static final String FOOTER_HISTORY_NOTA = "FIN MENSAJE ORIGINAL";
    public static final int MEGABYTE = (1024 * 1024);

    private static transient Comparator<Caso> comparadorCasosPorFechaCreacion = new Comparator<Caso>() {
        @Override
        public int compare(Caso o1, Caso o2) {
            return o1.getFechaCreacion().compareTo(o2.getFechaCreacion());
        }
    };

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

    public void enableMerge() {
        this.setMergeHabilitado(!isMergeHabilitado());
        if (isMergeHabilitado()) {
            mergeCandidatesList = null;
            setSelectedItems(new LinkedList<Caso>());
        }
    }

    public void mergeSelectedCasos() throws Exception {
        if (getSelectedItems().isEmpty()) {
            showMessageInDialog(FacesMessage.SEVERITY_WARN, "No se han seleccionado casos para combinar",
                    "Para poder combinar casos, se deben seleccionar los que se deseen combinar");
        } else {
            getSelectedItems().add(getSelected());
            Collections.sort(getSelectedItems(), comparadorCasosPorFechaCreacion);
            Caso casoBase = getSelectedItems().get(0);
            getSelectedItems().remove(0);
            StringBuilder sb = new StringBuilder("se ha combinado con el(los) caso(s) ");
            for (Caso casoToMerge : getSelectedItems()) {
                casoBase.getNotaList().add(getManagerCasos().crearNotaDesdeCaso(casoToMerge, casoBase));
                List<Nota> listaNotas = casoToMerge.getNotaList();
                for (Nota item : listaNotas) {
                    Nota nuevaNota = getManagerCasos().clonarNota(item);
                    nuevaNota.setIdCaso(casoBase);
                    getJpaControllerThatListenRules().persist(nuevaNota);
                    casoBase.getNotaList().add(nuevaNota);
                }
                casoBase.getCasosHijosList().addAll(casoToMerge.getCasosHijosList());
                casoBase.getAttachmentList().addAll(cloneAttachments(casoToMerge.getAttachmentList(), casoBase));
                mergeList(casoBase.getCasosRelacionadosList(), casoToMerge.getCasosRelacionadosList());
                casoBase.getCasosRelacionadosList().remove(casoBase);
                casoToMerge.setIdCasoCombinado(casoBase);
                casoToMerge.setIdEstado(EnumEstadoCaso.CERRADO.getEstado());
                casoToMerge.setFechaCierre(applicationBean.getNow());
                casoToMerge.setIdSubEstado((casoToMerge.getTipoCaso().equals(EnumTipoCaso.CONTACTO.getTipoCaso()) ? EnumSubEstadoCaso.CONTACTO_DUPLICADO.getSubEstado()
                        : (casoToMerge.getTipoCaso().equals(EnumTipoCaso.COTIZACION.getTipoCaso()) ? EnumSubEstadoCaso.COTIZACION_DUPLICADO.getSubEstado()
                                : (casoToMerge.getTipoCaso().equals(EnumTipoCaso.CONTACTO.getTipoCaso()) ? EnumSubEstadoCaso.CONTACTO_DUPLICADO.getSubEstado()
                                        : EnumSubEstadoCaso.CONTACTO_DUPLICADO.getSubEstado()))));
                getManagerCasos().mergeCaso(casoToMerge,
                        ManagerCasos.createLogComment(casoToMerge, "Se combina con el caso " + casoBase.getIdCaso()));
                sb.append(casoToMerge.getIdCaso());
                sb.append(',');
            }
            String comment = sb.toString();
            comment = comment.substring(0, comment.length() - 1);
            casoBase.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado());
            getManagerCasos().mergeCaso(casoBase, ManagerCasos.createLogComment(casoBase, comment));
            mergeCandidatesList = null;
            setMergeHabilitado(false);
            setSelectedItems(null);
            showMessageInDialog(FacesMessage.SEVERITY_INFO, "Combinación finalizada",
                    "El caso " + casoBase.getIdCaso() + " " + comment);
            redirect("/script/caso/Edit.xhtml");
        }
    }

    private List<Attachment> cloneAttachments(List<Attachment> attachments, Caso casoBase) {
//        //System.out.println("clone Attachments size list: " + attachments.size());
//        //System.out.println("Caso base attachment list size: " + casoBase.getAttachmentList().size());
//        //System.out.println("Caso base attachmentNotEmbedded list size: " + casoBase.getAttachmentsNotEmbedded().size());
        List<Attachment> clonedElements = new ArrayList<>(attachments.size());
        for (Attachment attachment : attachments) {
            Archivo archivo = getJpaController().find(Archivo.class, attachment.getIdAttachment());
            if (archivo != null) {
                try {
                    Attachment nuevoAttachment = (Attachment) ClassUtils.clone(attachment);
                    nuevoAttachment.setIdAttachment(null);
                    nuevoAttachment.setIdCaso(casoBase);
                    getJpaController().persist(nuevoAttachment);
                    Archivo nuevoArchivo = (Archivo) ClassUtils.clone(archivo);
                    nuevoArchivo.setIdAttachment(nuevoAttachment.getIdAttachment());
                    getJpaController().persist(nuevoArchivo);
                    clonedElements.add(nuevoAttachment);
                } catch (Exception ex) {
                    Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return clonedElements;
    }

    private void mergeList(List listaOriginal, List lista) {
        for (Object caso : lista) {
            if (!listaOriginal.contains(caso)) {
                listaOriginal.add(caso);
            }
        }
    }

    public List<Caso> getMergeCandidatesList() {
        if (mergeCandidatesList == null) {
            mergeCandidatesList = new LinkedList<>();
            List<Caso> tmpList;
            //comentado a peticion de betterlife
//          tmpList = getSelected().getIdCliente().getCasoList();
//            for (Caso tmpList1 : tmpList) {
//                addToMergeCandidatesList(tmpList1);
//            }
            tmpList = getSelected().getCasosRelacionadosList();
            for (Caso tmpList1 : tmpList) {
                addToMergeCandidatesList(tmpList1);
            }

            for (Caso casoHijo : getSelected().getCasosHijosList()) {
                mergeCandidatesList.remove(casoHijo);
                if (mergeCandidatesList.isEmpty()) {
                    break;
                }
            }

            if ((getSelected().getIdCasoPadre() != null) && (!mergeCandidatesList.isEmpty())) {
                mergeCandidatesList.remove(getSelected().getIdCasoPadre());
            }

        }
        return mergeCandidatesList;
    }

    private void addToMergeCandidatesList(Caso casoToMerge) {
        if ((!mergeCandidatesList.contains(casoToMerge))
                && (casoToMerge.getIdCasoCombinado() == null)
                && (!casoToMerge.equals(getSelected()))) {
            mergeCandidatesList.add(casoToMerge);
        }
    }

    @Override
    protected String getListPage() {
        return "inbox";
    }

    public void enableReplyMode(boolean all) {

        if (validateEdit()) {
            return;
        }

        this.setReplyMode(true);
        this.setReplyByEmail(true);
        this.ccEmail = new ArrayList<>();

        if (all) {

            if (current.getEmailClienteCCList() != null && !current.getEmailClienteCCList().isEmpty()) {
                this.setCc(true);
                for (EmailCliente e : current.getEmailClienteCCList()) {
                    ccEmail.add(e.getEmailCliente());
                }
            }

            if (current.getUsuarioCCList() != null && !current.getUsuarioCCList().isEmpty()) {
                this.setCc(true);
                for (Usuario u : current.getUsuarioCCList()) {
                    ccEmail.add(u.getEmail());
                }
            }
        } else {
            this.setCc(false);
        }

        //set borrador
        this.textoNota = current.getRespuesta();

        agregarHistoria();

        if (getOtroEmail() == null) {
            setOtroEmail(new LinkedList<String>());
        }

        if (current.getEmailCliente() != null
                && current.getEmailCliente().getEmailCliente() != null
                && !getOtroEmail().contains(current.getEmailCliente().getEmailCliente())) {
            getOtroEmail().add(current.getEmailCliente().getEmailCliente());
        }

    }

    public void enableCommentMode() {

        if (validateEdit()) {
            return;
        }

        this.setReplyMode(true);
        this.setReplyByEmail(false);

        this.textoNota = current.getRespuesta();

    }

    private boolean validateEdit() {
        if (applicationBean.isProductoRequired() && current.getIdProducto() == null) {
            showMessageInDialog(FacesMessage.SEVERITY_ERROR, "Acción requerida", "Antes de continuar es necesario que seleccione el " + applicationBean.getProductDescription() + " relacionado con el caso.");
            return true;
        }
        if (applicationBean.isAreaRequired() && current.getIdArea() == null) {
            showMessageInDialog(FacesMessage.SEVERITY_ERROR, "Acción requerida", "Antes de continuar es necesario que seleccione el área al cual pertenece el caso.");
            return true;
        }
        return false;
    }

    public void disableReplyMode() {
        this.resetNotaForm();
    }

    public void submitSelectedManyItems() {
        executeInClient("PF('selectItemsWidget').hide()");
    }

    public void onChangeActiveIndexCasoSections(TabChangeEvent event) {
        //THIS CUERO PIC PULGA ES A RAIZ DE UN BUG DE PRIMEFACES, ME ENVIA EL ACTIVE INDEX SIEMPRE EN CER0.
        try {
            final String idtab = event.getTab().getId();
            if (idtab.contains("tab-actividades")) {
                setActiveIndexCasoSections(TAB_ACTIVIDADES_INDEX);//tab-actividades
            } else if (idtab.contains("tabAgendarEvento")) {
                setActiveIndexCasoSections(TAB_AGENDA_INDEX);//tabAgendarEvento
            } else if (idtab.contains("tabArchivos")) {
                setActiveIndexCasoSections(TAB_ADJUNTOS_INDEX);//tabArchivos
            } else if (idtab.contains("tabCasosRelacionados")) {
                setActiveIndexCasoSections(TAB_CASOSREL_INDEX);//tabCasosRelacionados
            } else if (idtab.contains("tab-timeline")) {
                setActiveIndexCasoSections(TAB_TIMELINE_INDEX);//tab-timeline
            } else if (idtab.contains("tabEditarEvento")) {
                setActiveIndexCasoSections(TAB_EVENTO_INDEX);//tabEditarEvento
            } else {
                setActiveIndexCasoSections(TAB_ACTIVIDADES_INDEX);//descripcion
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<AuditLog> getAuditLogsCurrentCase() {
        if (current == null || current.getIdCaso() == null) {
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
            return (List<AuditLog>) getJpaControllerThatListenRules()
                    .findAllEntities(vista1, new OrderBy("fecha", OrderBy.OrderType.DESC), userSessionBean.getCurrent());

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
//        //System.out.println(newSubCaso);
        current.getCasosHijosList().add(newSubCaso);
    }

    /**
     *
     * @param subcaso
     */
    public void removeSubCasoFromCurrentCaso(Caso subcaso) {
        if (current.getCasosHijosList() == null || current.getCasosHijosList().isEmpty()) {
            JsfUtil.addErrorMessage("No hay subcasos en la lista");
        }
        if (current.getCasosHijosList().contains(subcaso)) {
            current.getCasosHijosList().remove(subcaso);
        }
        subcaso.setIdCasoPadre(null);
    }

    public void runActionOnSelectedCasos() {

//        //System.out.println("accionToRunSelected:" + accionToRunSelected);
        Action a = applicationBean.getPredefinedActions().get(accionToRunSelected);
        a.setConfig(accionToRunParametros);

        List<Caso> casosToSend = Collections.EMPTY_LIST;
        if (getSelectedItemsCount() > 0) {
            casosToSend = getSelectedItems();

        } else {
            //run on all items in Vista
            try {
                casosToSend = (List<Caso>) getJpaControllerThatListenRules().findAllEntities(getVista(), getDefaultOrderBy(), userSessionBean.getCurrent());
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
        recreateModel();
        recreatePagination();
        executeInClient("PF('runActionDialog').hide()");
        if (getSelectedItemsCount() > count) {
            addWarnMessage("Error: " + (getSelectedItemsCount() - count) + " casos no han sido enviados, favor revisar la configuración de correo del area correspondiente.");
        }

    }

    public void applyReglaToSelectedCasos() {
        RulesEngine rulesEngine = new RulesEngine(getJpaController());
        List<Caso> casosToSend = Collections.EMPTY_LIST;
        if (getSelectedItemsCount() > 0) {
            casosToSend = getSelectedItems();

        } else {
            //run on all items in Vista
            try {
                casosToSend = (List<Caso>) getJpaControllerThatListenRules().findAllEntities(getVista(), getDefaultOrderBy(), userSessionBean.getCurrent());
            } catch (Exception ex) {
                Logger.getLogger(CasoController.class
                        .getName()).log(Level.SEVERE, "findEntities", ex);
            }
        }

        if (casosToSend != null && !casosToSend.isEmpty()) {
            rulesEngine.applyRuleOnThisCasos(reglaTriggerSelected, casosToSend);
            addInfoMessage("Regla ejecutada en " + casosToSend.size() + " casos.");
            recreateModel();
            recreatePagination();
            executeInClient("PF('applyRuleDialog').hide()");
            //TODO update table data
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
        System.out.println("tagItemSelectEvent");
        String item = (String)event.getObject();
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
            getJpaControllerThatListenRules().mergeCaso(current, ManagerCasos.createLogReg(current, "Etiquetas", "Se agrega Etiqueta :" + item.toString(), ""));
//            addInfoMessage("Etiqueta Agregada OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Agregar la etiqueta" + item);
            Log.createLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void tagItemUnselectEvent(UnselectEvent event) {
        Object item = event.getObject();
        current.setFechaModif(Calendar.getInstance().getTime());
        try {
            getJpaControllerThatListenRules().mergeCaso(current, ManagerCasos.createLogReg(current, "Etiquetas", "Etiqueta " + item.toString() + " removida.", ""));
//            addInfoMessage("Etiqueta Removida OK!");
        } catch (Exception ex) {
            addInfoMessage("No se pudo Remover la etiqueta" + item);
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @deprecated @return
     */
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

    /**
     * @deprecated @return
     */
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
                blackListMap = new HashMap<>();
                for (Caso caso : getSelectedItems()) {
                    if (caso.getEmailCliente() != null && caso.getEmailCliente().getEmailCliente() != null) {
                        blackListMap.put(caso.getEmailCliente().getEmailCliente(), new BlackListEmail(caso.getEmailCliente().getEmailCliente()));
                    }
                }
                return new ArrayList<>(blackListMap.keySet());
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }

        return null;
    }

    public void saveBlackList() {

        for (BlackListEmail blackListEmail : blackListMap.values()) {
            BlackListEmail persistentBlackListEmail = getJpaControllerThatListenRules().find(BlackListEmail.class, blackListEmail.getEmailAddress());
            if (persistentBlackListEmail == null) {
                try {
                    getJpaControllerThatListenRules().persist(blackListEmail);
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
            getJpaControllerThatListenRules().merge(caso);
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

    public void handleSubComponentSelect(SelectEvent event) {
        SubComponente s = (SubComponente) getJpaController().find(SubComponente.class, getSubComponente_wizard().getIdSubComponente(), true);
        if (s.getProductoContratadoList() != null && !s.getProductoContratadoList().isEmpty()) {
            final ProductoContratado pc1 = s.getProductoContratadoList().get(0);

            if (pc1.getCliente() != null) {
                rutCliente_wizard = pc1.getCliente().getRut();
                setEmailCliente_wizard_existeCliente(true);
                current.setIdCliente(pc1.getCliente());
                current.setIdProducto(pc1.getProducto());
                current.setIdComponente(pc1.getComponente());
                current.setIdSubComponente(pc1.getSubComponente());
            } else {
                addWarnMessage("ERRORCODE 2");
            }

        } else {

            setEmailCliente_wizard_existeCliente(false);
            addWarnMessage("El " + applicationBean.getProductSubComponentDescription() + " seleccionado no esta asociado a ningún cliente.");
        }

    }

    public void handleEmailChange() {
        handleEmailSelect(null);
    }

    public void handleEmailSelect(SelectEvent event) {

        if (!StringUtils.isEmpty(getEmailCliente_wizard())) {
            //        EmailCliente emailCliente = getJpaController().getEmailClienteFindByEmail(event.getObject().toString());
            EmailCliente emailCliente = getJpaController().find(EmailCliente.class, getEmailCliente_wizard());

//        ////System.out.println("emailCliente_wizard:" + emailCliente_wizard);
//        ////System.out.println("event.getObject().toString():" + event.getObject().toString());
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
                    Cliente existentClient = getJpaController().findClienteByRut(rutCliente_wizard);
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
        } else {
            //selected null (other email blank!!)
            current.setEmailCliente(null);
//            current.setIdCliente(new Cliente());
            setEmailCliente_wizard(null);
            setEmailCliente_wizard_existeCliente(false);
            setEmailCliente_wizard_existeEmail(false);
        }

    }

    public void formateaRutFiltro2() {
        ////System.out.println("formateaRutFiltro2()");
        try {
//            final String rutInput = getSelected().getEmailCliente().getCliente().getRut();

            if (rutCliente_wizard != null && !StringUtils.isEmpty(rutCliente_wizard)) {
                rutCliente_wizard = UtilesRut.formatear(rutCliente_wizard);

                if (!emailCliente_wizard_existeCliente) {
                    Cliente c = getJpaControllerThatListenRules().findClienteByRut(rutCliente_wizard);
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

    public void handleNewTicketClientSelectionChange() {
        final ProductoContratado c = getPcWizard();
        if (c != null) {
            this.setTipoAsoc_wizard(c.getTipoAsociacion());
            setEmailCliente_wizard_existeCliente(true);
            rutCliente_wizard = c.getCliente().getRut();
            current.setIdCliente(c.getCliente());
            if (c.getCliente().getEmailClienteList() != null && !c.getCliente().getEmailClienteList().isEmpty()) {
                EmailCliente emailCliente = c.getCliente().getEmailClienteList().get(0);
                current.setEmailCliente(emailCliente);
                emailCliente_wizard = emailCliente.getEmailCliente();
                setEmailCliente_wizard_existeEmail(true);
            } else {
                emailCliente_wizard = null;
                setEmailCliente_wizard_existeEmail(false);
            }
        } else {
            current.setIdCliente(new Cliente());
            current.setEmailCliente(null);
            rutCliente_wizard = null;
            emailCliente_wizard = null;
            setEmailCliente_wizard_existeCliente(false);
            setEmailCliente_wizard_existeEmail(false);
        }
    }

    public void handleSubCompChange() {
        //System.out.println("handleSubCompChange...");
        if (getSelected().getIdSubComponente() != null) {
            getSelected().setIdSubComponente(getJpaController().find(SubComponente.class, getSelected().getIdSubComponente().getIdSubComponente(), true));
        }
    }

    protected void persist(Caso newCaso) throws PreexistingEntityException, RollbackFailureException, Exception {

        List<Attachment> attachmentList = current.getAttachmentList();
        current.setAttachmentList(null);

        if (newCaso.getFechaCreacion() == null) {
            newCaso.setFechaCreacion(new Date());
        }
        if (newCaso.getIdPrioridad() == null) {
            newCaso.setIdPrioridad(EnumPrioridad.BAJA.getPrioridad());
        }

        if (newCaso.getIdPrioridad() != null && newCaso.getIdPrioridad().getSlaHoras() != null) {
            int horas = newCaso.getIdPrioridad().getSlaHoras();
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(newCaso.getFechaCreacion());
            calendario.add(Calendar.HOUR, horas);
            newCaso.setNextResponseDue(calendario.getTime());
        }

        newCaso.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());

        if (emailCliente_wizard_updateCliente && emailCliente_wizard_existeCliente) {
            addProdContratadoToClient(newCaso.getEmailCliente().getCliente());
            getJpaControllerThatListenRules().merge(newCaso.getEmailCliente().getCliente());
        } else if (!emailCliente_wizard_existeCliente) {
            addProdContratadoToClient(newCaso.getIdCliente());
            getJpaControllerThatListenRules().persist(newCaso.getIdCliente());
        }

        if (!emailCliente_wizard_existeEmail && !StringUtils.isEmpty(emailCliente_wizard)) {
            getJpaControllerThatListenRules().persist(newCaso.getEmailCliente());
        }

        getManagerCasos().persistCaso(newCaso, ManagerCasos.createLogReg(newCaso, "Caso", "Se crea caso manual", ""));
        if (newCaso.getIdCasoPadre() != null) {
            Caso casoPadre = newCaso.getIdCasoPadre();
            casoPadre.getCasosHijosList().add(newCaso);
            getManagerCasos().mergeCaso(casoPadre, ManagerCasos.createLogReg(casoPadre, "Nuevo subCaso", newCaso.getIdCaso().toString(), ""));
        }

        if (attachmentList != null) {
            for (Attachment attachment : attachmentList) {
                getManagerCasos().crearAdjunto(
                        attachment.getArchivo().getArchivo(), null,
                        this.current, attachment.getNombreArchivo(),
                        attachment.getMimeType(),
                        attachment.getArchivo().getFileSize());
                JsfUtil.addSuccessMessage("Archivo " + attachment.getNombreArchivo() + " subido con exito");
            }

            getJpaController().merge(this.current);
        }

//        getManagerCasos().agendarAlertas(newCaso);
        HelpDeskScheluder.scheduleAlertaPorVencer(getUserSessionBean().getTenantId(), newCaso.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(newCaso));
        JsfUtil.addSuccessMessage("El Caso " + newCaso.getIdCaso() + " ha sido creado con éxito.");

    }

    private void addProdContratadoToClient(Cliente c) {
        if (pcWizard == null && !StringUtils.isEmpty(tipoAsoc_wizard) && current.getIdSubComponente() != null) {
            ProductoContratado pc = new ProductoContratado(c.getIdCliente(),
                    current.getIdProducto().getIdProducto(), current.getIdComponente().getIdComponente(), current.getIdSubComponente().getIdSubComponente());
            pc.setCliente(c);
            pc.setComponente(current.getIdComponente());
            pc.setProducto(current.getIdProducto());
            pc.setSubComponente(current.getIdSubComponente());
            pc.setTipoAsociacion(tipoAsoc_wizard);

            if (c.getProductoContratadoList() == null) {
                c.setProductoContratadoList(new ArrayList<ProductoContratado>());
            }
            c.getProductoContratadoList().add(pc);
        } else {
            if (pcWizard != null) {
                if (c.getProductoContratadoList().contains(pcWizard)) {
                    c.getProductoContratadoList().get(c.getProductoContratadoList().indexOf(pcWizard)).setTipoAsociacion(tipoAsoc_wizard);
                }
            }
        }
    }

    public void verifyGoToCaso() {
        HttpServletRequest request = (HttpServletRequest) JsfUtil.getRequest();
        String id = request.getParameter("id");
        if (id != null) {
            try {
                Caso casoRequested = getJpaControllerThatListenRules().find(Caso.class, Long.parseLong(id));
                if (casoRequested == null) {
                    JsfUtil.addErrorMessage("Caso ID " + id + " no existe en el sistema");
                    FacesContext.getCurrentInstance().getExternalContext().redirect("../index.xhtml");
                } else {
                    setSelected(casoRequested);
                    redirect("/script/caso/Edit.xhtml");
                }

            } catch (Exception ex) {
                JsfUtil.addErrorMessage("Sesion invalida");
                Log.createLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //return null;
    }

    public void onNodeItemSelect(NodeSelectEvent event) {
        Item item = (Item) event.getTreeNode().getData();
        current.getCasosHijosList().get(current.getCasosHijosList().size() - 1).setIdItem(item);
        Object res = JsfUtil.getManagedBean("itemController");
        if (res != null) {
            ItemController itemController = ((ItemController) res);
            Item itemSelected = (Item) itemController.getItem().getData();
//            ////System.out.println("categoria " + catSelected + " seleccionada");
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
//            ////System.out.println("categoria " + catSelected + " seleccionada");
//            current.setIdItem(itemSelected);
            current.setIdItem(itemSelected);
        }
    }

    public void agregarHistoria() {
        StringBuilder textoMensaje = new StringBuilder(textoNota != null ? textoNota : "");
        textoMensaje.append(obtenerHistorial());
        textoNota = textoMensaje.toString();
    }

    public String getIdFileDelete() {
        return idFileDelete;
    }

    public Long getIdFileRemove() {
        return idFileRemove;
    }

    public void setIdFileDelete(String idFileDelete) {
        this.idFileDelete = idFileDelete;
        ////System.out.println("idFileDelete:" + idFileDelete);
    }
    /**
     * Id Caso relacionado
     */
    private Caso idCaserel;

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
        if (getVista() == null) {
            recreatePagination();
            recreateModel();
            prepareCasoFilterForInbox();
        }
        return "inbox";
    }

    public Caso getIdCaserel() {
        return idCaserel;
    }

    public void setIdCaserel(Caso idCaserel) {
        this.idCaserel = idCaserel;
    }

    public String getIdCaso() {
        return idCasoStr;
    }

    public void setIdCaso(String idCaso) {
        this.idCasoStr = idCaso;
    }

    public String creaTituloDeNota(Nota nota) {
        //TODO locale should be the Browser or user configured locale.!
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm", LOCALE_ES_CL);
        if (nota != null && nota.getTipoNota() != null) {
            if (EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota().equals(nota.getTipoNota())) {
                if (nota.getEnviadoPor() == null) {
                    return nota.getTipoNota().getNombre() + " - " + format.format(nota.getFechaCreacion());
                } else {
                    return nota.getTipoNota().getNombre() + " - enviado por: " + nota.getEnviadoPor() + " - " + format.format(nota.getFechaCreacion());
                }
            } else if (EnumTipoNota.RESPUESTA_A_CLIENTE.getTipoNota().equals(nota.getTipoNota())
                    || EnumTipoNota.RESPUESTA_AUT_CLIENTE.getTipoNota().equals(nota.getTipoNota())
                    || EnumTipoNota.REG_ENVIO_CORREO.getTipoNota().equals(nota.getTipoNota())) {
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
            } else if (EnumTipoNota.COMENTARIO_AGENTE.getTipoNota().equals(nota.getTipoNota())) {
                StringBuilder sbuilder = new StringBuilder(nota.getTipoNota().getNombre());
                sbuilder.append(" - creada por ");
                sbuilder.append(nota.getCreadaPor().getIdUsuario());
                if (nota.getEnviado() != null && nota.getEnviado()) {
                    sbuilder.append(" - enviada el ");
                    sbuilder.append(format.format(nota.getFechaEnvio()));
                } else {
                    sbuilder.append(" - creada el ");
                    sbuilder.append(format.format(nota.getFechaCreacion()));
                }
                return sbuilder.toString();
            }

            String text = nota.getTipoNota().getNombre();
            if (nota.getCreadaPor() != null) {
                text = text + " - creado por " + nota.getCreadaPor().getIdUsuario();
            }

            if (nota.getFechaCreacion() != null) {
                text = text + " - " + format.format(nota.getFechaCreacion());
            }

            return text;

        }

        return "Nota";

    }

    public int cantidadAttachment() {
        try {
            return getJpaControllerThatListenRules().countAttachmentWOContentId(current).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

//    public int cantidadAttachmentEmbedded() {
//        try {
//            return getJpaControllerThatListenRules().countAttachmentWContentId(current).intValue();
//        } catch (Exception e) {
//            return 0;
//        }
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

    public void formateaRut() {
//        //System.out.println("formateaRut");
        if (getSelected().getIdCliente() == null) {
            return;
        }
        if (getSelected().getIdCliente() != null && StringUtils.isEmpty(getSelected().getIdCliente().getRut())) {
            return;
        } else {
            String rutFormateado = UtilesRut.formatear(getSelected().getIdCliente().getRut());
            getSelected().getIdCliente().setRut(rutFormateado);
            validaRut();
        }

    }

    public void validaRut() {
        if (!UtilesRut.validar(getSelected().getIdCliente().getRut())) {
            JsfUtil.addErrorMessage("Rut invalido");
        }
    }

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
        Map<String, Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 500);
        options.put("contentWidth", 700);
        RequestContext.getCurrentInstance().openDialog("/script/caso/ViewHtml", options, null);
    }

    public void changeCCO(boolean cc) {
        if (cc) {
            setCc(!isCc());
//            ccEmail = null;
        } else {
            setCco(!isCco());
//            ccoEmail = null;
        }
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

        try {
            Vista vistaCasosPendientes = createVistaCurrentUserByAlert(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta(), EnumEstadoCaso.ABIERTO.getEstado());
            casosPendientes = getJpaControllerThatListenRules().countEntities(vistaCasosPendientes, userSessionBean.getCurrent(), null);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Vista vistaCasosPorVencer = createVistaCurrentUserByAlert(EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta(), EnumEstadoCaso.ABIERTO.getEstado());
            casosPorVencer = getJpaControllerThatListenRules().countEntities(vistaCasosPorVencer, userSessionBean.getCurrent(), null);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Vista vistaCasosVencidos = createVistaCurrentUserByAlert(EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta(), EnumEstadoCaso.ABIERTO.getEstado());
            casosVencidos = getJpaControllerThatListenRules().countEntities(vistaCasosVencidos, userSessionBean.getCurrent(), null);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Vista vistaCasosCerrados = createVistaMisCasosCerrados();
            casosCerrados = getJpaControllerThatListenRules().countEntities(vistaCasosCerrados, userSessionBean.getCurrent(), null);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Vista vistaMyReviewUpdate = createVistaMyReviewUpdate();
            casosRevisarActualizacion = getJpaControllerThatListenRules().countEntities(vistaMyReviewUpdate, userSessionBean.getCurrent(), null);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Vista vistacasosPrioritarios = createVistaOpenPrio();
            casosPrioritarios = getJpaControllerThatListenRules().countEntities(vistacasosPrioritarios, userSessionBean.getCurrent(), null);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        }

//                    casosPendientes = getJpaControllerThatListenRules().getCasoCount(userSessionBean.getCurrent(), EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
//            casosPorVencer = getJpaControllerThatListenRules().getCasoCount(userSessionBean.getCurrent(), EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta());
//            casosVencidos = getJpaControllerThatListenRules().getCasoCount(userSessionBean.getCurrent(), EnumTipoAlerta.TIPO_ALERTA_VENCIDO.getTipoAlerta());
//            casosPrioritarios = getJpaControllerThatListenRules().getCasoCountPrioritarieOpen(userSessionBean.getCurrent());
//            casosCerrados = getJpaControllerThatListenRules().getCasoCountClosed(userSessionBean.getCurrent());
//            casosRevisarActualizacion = getJpaControllerThatListenRules().getCasoCountActualizados(userSessionBean.getCurrent());
    }

    public Long getCasosPrioritarios() {
        return casosPrioritarios;
    }

    public Long getCasosCerrados() {
        return casosCerrados;
    }

    public Long getCasosPendientes() {
        return casosPendientes;
    }

    public Long getCasosPorVencer() {
        return casosPorVencer;
    }

    public Long getCasosVencidos() {
        return casosVencidos;
    }

    public Long getCountCasosRevisarActualizacion() {
        return casosRevisarActualizacion;
    }

    public int getCantidadDeNotas() {
        cantidadDeNotas = 0;
        cantidadDeRespuestasACliente = 0;
        cantidadDeRespuestasDelCliente = 0;

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
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
            EmailCliente cliente = getJpaControllerThatListenRules().find(EmailCliente.class, usr.getEmail());
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
//                getJpaControllerThatListenRules().persistEmailCliente(newCliente);
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
            current.setIdPrioridad(getJpaControllerThatListenRules().find(Prioridad.class, EnumPrioridad.MEDIA.getPrioridad().getIdPrioridad()));

            current.setIdEstado(ec);
            selectedItemIndex = -1;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido generar un caso interno", e);
        }
        return "/script/caso/Create";
    }

    public String prepareCreateCasoReparacion() {
        try {
            current = new Caso();
            current.setRevisarActualizacion(true);
            current.setIdPrioridad(null);
            current.setFechaCreacion(Calendar.getInstance().getTime());
            current.setFechaModif(current.getFechaCreacion());
            current.setOwner(userSessionBean.getCurrent());
            current.setTipoCaso(EnumTipoCaso.POSTVENTA.getTipoCaso());
            current.setIdSubEstado(EnumSubEstadoCaso.POSTVENTA_NUEVO.getSubEstado());
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
            //current.setTema("Postventa para " + current.getIdCliente().getCapitalName());

            emailCliente_wizard_existeEmail = false;
            emailCliente_wizard_existeCliente = false;
            emailCliente_wizard_updateCliente = false;
            emailCliente_wizard = null;
            tipoAsoc_wizard = null;
            rutCliente_wizard = null;
            selectedItemIndex = -1;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo("Error al preparar la creación del caso de postventa");
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
        }
        return "/script/caso/Create_preentrega";
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
            tipoAsoc_wizard = null;
            rutCliente_wizard = null;
            selectedItemIndex = -1;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logInfo("Error al preparar la creación de un caso de entrega");
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
        }
        return "/script/caso/Create_preentrega";
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
            current.setIdCliente(email.getCliente());
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
            tipoAsoc_wizard = null;
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
    public String createAndView() {
        try {
            //System.out.println("CURRENT TEST ATACHMENT BEFORE:" + this.current.getAttachmentList());
            persist(current);
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

    public void refreshCurrentCaso() throws Exception {
        current = getJpaControllerThatListenRules().find(Caso.class, current.getIdCaso());
//        if (current != null && current.getNotaList() != null) {
//            Collections.sort(current.getNotaList());
//        }
        resetNotaForm();
//        recreateModel();
    }

    public void prepareEditNota(Nota nota) throws Exception {
        selectedNota = nota;
    }

    public String extractNotaNewPart(Nota nota) {
        String ret = nota.getTexto();

        int headerStartIndex = ret.indexOf(HEADER_HISTORY_NOTA_ALT);
        int historyEndIndex = ret.lastIndexOf(FOOTER_HISTORY_NOTA);
        headerStartIndex = (headerStartIndex > 0) ? headerStartIndex : ret.indexOf(HEADER_HISTORY_NOTA_ALT);
        historyEndIndex = (historyEndIndex > 0) ? historyEndIndex : ret.indexOf(FOOTER_HISTORY_NOTA);
        if (headerStartIndex > 0) {
            ret = ret.substring(0, headerStartIndex);
            if ((!debeMostrarContenidoReducido(nota)) && (historyEndIndex > headerStartIndex)) {
                ret += nota.getTexto().substring(historyEndIndex + FOOTER_HISTORY_NOTA.length());
            }
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(ret);
        Elements strongTag = doc.getElementsByTag("strong");
        for (org.jsoup.nodes.Element style : strongTag) {
            style.remove();
        }
        ret = doc.toString();
        ////System.out.println("html ret:\n"+ret);
        return ret;
    }

    public boolean existsNotaHistoryPart(Nota nota) {
        return (nota.getTexto().indexOf(HEADER_HISTORY_NOTA_ALT) > 0) || (nota.getTexto().indexOf(HEADER_HISTORY_NOTA_ALT) > 0);
    }

    public String extractNotaHistoryPart(Nota nota) {
        int headerStartIndex = nota.getTexto().indexOf(HEADER_HISTORY_NOTA_ALT);
        headerStartIndex = (headerStartIndex > 0) ? headerStartIndex : nota.getTexto().indexOf(HEADER_HISTORY_NOTA_ALT);
        if (headerStartIndex > 0) {
            return nota.getTexto().substring(headerStartIndex);
        }
        return "";
    }

    public void toogleMostrarContenidoReducido(Nota nota) {
        if (showReducedContentMap == null) {
            showReducedContentMap = new LinkedHashMap<>();
        }
        if (showReducedContentMap.get(nota.getIdNota()) != null) {
            showReducedContentMap.put(nota.getIdNota(), showReducedContentMap.get(nota.getIdNota()) ? Boolean.FALSE : Boolean.TRUE);
        } else {
            showReducedContentMap.put(nota.getIdNota(), Boolean.TRUE);
        }
    }

    public boolean debeMostrarContenidoReducido(Nota nota) {
        if (showReducedContentMap == null) {
            showReducedContentMap = new LinkedHashMap<>();
        }
        if (showReducedContentMap.get(nota.getIdNota()) != null) {
            return showReducedContentMap.get(nota.getIdNota());
        }
        return false;
    }

    /**
     * Opens ticket selected row
     *
     * @param event
     */
    public void onRowSelect(SelectEvent event) {
        try {
            Caso caso = (Caso) event.getObject();
            selectedItemIndex = ((List<Caso>) getItems().getWrappedData()).indexOf(caso);

            if (caso != null) {
                openCase(caso);
                redirect("/script/caso/Edit.xhtml");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error onRowSelect", e);
            addErrorMessage("Lo sentimos, no se pudo recuperar la información del caso.");
        }

    }

    /**
     * Opens ticket, reset the tab, set selected ticket, refresh ticket data
     * updates RevisarActualizacion state if opener is owner user.
     *
     * This method must be used in all ways to open a case.
     *
     * @param caso
     * @throws Exception
     */
    public void openCase(Caso caso) throws Exception {
        current = caso;
        setActiveIndexCasoSections(TAB_ACTIVIDADES_INDEX);
        if (showReducedContentMap == null) {
            showReducedContentMap = new LinkedHashMap<>();
        } else {
            showReducedContentMap.clear();
        }
        if (current != null) {
            refreshCurrentCaso();
        }
        if (current != null && (current.getOwner() != null)
                && (current.getOwner().equals(userSessionBean.getCurrent()))) {
            if (current.getRevisarActualizacion()) {
                current.setRevisarActualizacion(false);
                List<AuditLog> changeLog = new ArrayList<>();
                changeLog.add(ManagerCasos.createLogComment(current, "Agente propietario del caso revisa caso pendiente de revisión."));
                getJpaControllerThatListenRules().mergeCaso(current, changeLog);
            }
        }

        //ugly patch to reset the selected event
        final CasoScheduleController bean = (CasoScheduleController) JsfUtil.getManagedBean("casoScheduleController");
        bean.setEvent(null);
        //reset the merge option
        setMergeHabilitado(false);
    }

    /**
     * opens a case from an index in the data table
     *
     * @param index
     * @throws Exception
     */
    private void prepareEditIndex(Integer index) throws Exception {
        Caso caso = null;
        if (index != null) {
            selectedItemIndex = index;
            getItems().setRowIndex(index);
            caso = (Caso) getItems().getRowData();
        } else {
            caso = (Caso) getItems().getRowData();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        }

        if (caso != null) {
            openCase(caso);
        } else {
            selectedItemIndex = -1;
            addWarnMessage("Error: No se puede abrir el caso");
        }

    }

    /**
     * Goes to the previus case in the datatable
     *
     * @return
     * @throws Exception
     */
    public String prepareEditPreviusItem() {
        try {
            if (selectedItemIndex > pagination.getPageFirstItem()) {
                prepareEditIndex(--selectedItemIndex);
            } else {
                prepareEditIndex(pagination.getPageFirstItem());
            }
        } catch (Exception e) {
            addErrorMessage("No existe el elemento.");
        }
        return null;
    }

    /**
     * Goes to the next case in the datatable
     *
     * @return
     * @throws Exception
     */
    public String prepareEditNextItem() {

        try {
            if (selectedItemIndex < pagination.getPageLastItem()) {
                prepareEditIndex(++selectedItemIndex);
            } else {
                prepareEditIndex(pagination.getPageFirstItem());
            }

        } catch (Exception e) {
            addErrorMessage("No existe el elemento.");
        }
        return null;

    }

    public String prepareEdit() throws Exception {
        prepareEditIndex(null);
        return getEditPage();
    }

    public String prepareEditCaso() {
        return prepareEditCaso(idCasoStr);
    }

    public String prepareEditCaso(String idCasoString) {
        if (idCasoString == null) {
            JsfUtil.addErrorMessage("Debe especificar el numero de caso.");
        } else {
            try {
                Long idCaso = Long.parseLong(idCasoString);
                return filterByIdCaso(idCaso);
            } catch (NumberFormatException ex) {
                JsfUtil.addErrorMessage("Debe especificar el numero de caso.");
            }
        }
        return null;
    }

    public String filterByIdCaso(Long idCaso) {

        try {
            Caso caso = getJpaControllerThatListenRules().find(Caso.class, idCaso);

            if (caso != null) {
                openCase(caso);
                return getEditPage();
            } else {
                addErrorMessage("Caso " + idCaso + " no encontrado! Favor verifique que este exista.");
            }

        } catch (Exception ex) {
            JsfUtil.addErrorMessage("Caso " + idCaso + " no encontrado");
        }

        return null;

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

        setVista(copy);

        recreateModel();
        recreatePagination();

//        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Etiqueta Seleccionada:" + tagId, "");
//        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String saveCurrentView() {

        try {

            if (getVista() != null && getVista().getFiltrosVistaList() != null) {
                //SE hizo una copia de esta vista, y los filtros tienen ids falsos
                for (FiltroVista f : getVista().getFiltrosVistaList()) {
                    f.setIdFiltro(null); //This is an ugly patch to solve issue when removing a filter from the view, if TODO: Warning - this method won't work in the case the id fields are not set
                }
            }

            vistaController.create(getVista());
            JsfUtil.addSuccessMessage("La Vista guardada exitosamente. Revisar el panel de Vistas.");
            executeInClient("PF('saveViewDialog').hide()");
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

    public void prepareCasoFilterForInbox() {

        if (userSessionBean.isValidatedCustomerSession()) {
            //customer view!

            List<Vista> vistas = vistaController.getVistasCustomersItems();

            if (vistas != null && !vistas.isEmpty()) {
                final Vista vista0 = vistas.get(0);
                //there are vistas, lets select the first one as the user inbox
                setVista(vista0);
                setSelectedViewId(vista0.getIdVista());
            }

        } else if (userSessionBean.isValidatedSession()) {

            List<Vista> vistas = getAllAgentVistasItems();

            if (vistas != null && !vistas.isEmpty()) {
                final Vista vista0 = vistas.get(0);
                //there are vistas, lets select the first one as the user inbox
                setVista(vista0);
                setSelectedViewId(vista0.getIdVista());
            } else {
                //damn, there are no vistas for this madafaka, lets give him some default shit
                Vista vista1 = new Vista(Caso.class);
                vista1.setIdVista(1);
                vista1.setNombre("Inbox");

                vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());

                FiltroVista filtroOwner = new FiltroVista();
                filtroOwner.setIdFiltro(1);//otherwise i dont know what to remove dude.
                filtroOwner.setIdCampo("owner");
                filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroOwner.setValor(AbstractJPAController.PLACE_HOLDER_CURRENT_USER);
                filtroOwner.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroOwner);

                FiltroVista filtroEstado = new FiltroVista();
                filtroEstado.setIdFiltro(2);//otherwise i dont know what to remove dude.
                filtroEstado.setIdCampo("idEstado");
                filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
                filtroEstado.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroEstado);

                setVista(vista1);
                setSelectedViewId(vista1.getIdVista());
            }

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
        Vista vista1 = createVistaMyReviewUpdate();
        setVista(vista1);
        recreatePagination();
        recreateModel();
        return "inbox";
    }

    private Vista createVistaMyReviewUpdate() {
        //        ////System.out.println("filtraRevisarActualizaciones");
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Casos Actualizados por el cliente");

        FiltroVista filtroOwner = new FiltroVista(-1);
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(AbstractJPAController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setValorLabel(JPAFilterHelper.PLACE_HOLDER_CURRENT_USER_LABEL);
        filtroOwner.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroOwner);

        FiltroVista reviewUpdate = new FiltroVista(-2);
        reviewUpdate.setIdCampo(Caso_.REVISAR_ACTUALIZACION_FIELD_NAME);
        reviewUpdate.setVisibleToAgents(false);
        reviewUpdate.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        reviewUpdate.setValor(Boolean.TRUE.toString());
        reviewUpdate.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(reviewUpdate);

        return vista1;
    }

    /**
     * Porfavor refactorizar este codigo. HAcer un factory de vistas, donde la
     * vista se cree una vez y se use para contar o listar de tal manera que se
     * llame al metodo applyView siempre que se quiera filtrar los casos.
     *
     * @return
     */
    public String filtraCasosCerrados() {
        Vista vista1 = createVistaMisCasosCerrados();

        setVista(vista1);
        recreatePagination();
        recreateModel();
        return "inbox";
    }

    private Vista createVistaMisCasosCerrados() {
//        ////System.out.println("filtraRevisarActualizaciones");
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Casos cerrados");
        FiltroVista filtroOwner = new FiltroVista(-1);
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(AbstractJPAController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setValorLabel(JPAFilterHelper.PLACE_HOLDER_CURRENT_USER_LABEL);
        filtroOwner.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroOwner);
        FiltroVista filtroEstado = new FiltroVista(-2);
        filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroEstado.setValor(EnumEstadoCaso.CERRADO.getEstado().getIdEstado());
        filtroEstado.setValorLabel(EnumEstadoCaso.CERRADO.getEstado().getNombre());
        filtroEstado.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroEstado);
        return vista1;
    }

    public String filtrarPrioritarios() {
        Vista vista1 = createVistaOpenPrio();

        setVista(vista1);
        recreatePagination();
        recreateModel();
        return "inbox";
    }

    private Vista createVistaOpenPrio() {
//        ////System.out.println("filtraPorAlerta");
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Mis Casos prioritarios");
        FiltroVista filtroOwner = new FiltroVista(-1);
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(AbstractJPAController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setValorLabel(JPAFilterHelper.PLACE_HOLDER_CURRENT_USER_LABEL);
        filtroOwner.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroOwner);
        FiltroVista filtroEstado = new FiltroVista(-2);
        filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
        filtroEstado.setValorLabel(EnumEstadoCaso.ABIERTO.getEstado().getNombre());
        filtroEstado.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroEstado);
        FiltroVista fprio = new FiltroVista(-3);
        fprio.setIdCampo(Caso_.ES_PRIORITARIO_FIELD_NAME);
        fprio.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        fprio.setValor(Boolean.TRUE.toString());
        fprio.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(fprio);
        return vista1;
    }

    public String filtraPorAlerta(TipoAlerta alerta) {
        Vista vista1 = createVistaCurrentUserByAlert(alerta, EnumEstadoCaso.ABIERTO.getEstado());

        setVista(vista1);
        recreatePagination();
        recreateModel();
        return "inbox";
    }

    private Vista createVistaCurrentUserByAlert(TipoAlerta alerta, EstadoCaso estadoCaso) {
//        ////System.out.println("filtraPorAlerta");
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Casos con Alerta " + alerta.getNombre());
        FiltroVista filtroOwner = new FiltroVista(-1);
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(AbstractJPAController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setValorLabel(JPAFilterHelper.PLACE_HOLDER_CURRENT_USER_LABEL);
        filtroOwner.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroOwner);
        FiltroVista filtroEstado = new FiltroVista(-2);
        filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroEstado.setValor(estadoCaso.getIdEstado());
        filtroEstado.setValorLabel(estadoCaso.getNombre());
        filtroEstado.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroEstado);
        FiltroVista filtroAlerta = new FiltroVista(-3);
        filtroAlerta.setIdCampo(Caso_.ESTADO_ALERTA_FIELD_NAME);
        filtroAlerta.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroAlerta.setValor(alerta.getIdalerta().toString());
        filtroAlerta.setValorLabel(alerta.getNombre());
        filtroAlerta.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroAlerta);
        return vista1;
    }

    public void refresh() {
        recreateModel();
        recreatePagination();
    }

    public void changeSearchBarVisibility() {
        setSearchBarVisible(!isSearchBarVisible());
//        //System.out.println("changeSearchBarVisibility: "+isSearchBarVisible());
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
                    getJpaControllerThatListenRules().mergeCaso(caso, changeLog);
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
        return (puedeModificar() && current.isOpen());
    }

    public boolean verificarCrearActividad() {
        return (puedeModificar() && current.isOpen());
    }

    public boolean verificarResponderCaso() {
        return (current.isOpen() && puedeResponder() && current.getEmailCliente() != null);
    }

    public boolean verificarGrabarCaso() {
        return (puedeModificar());
    }

    public boolean verificarRelacionarCaso() {
        return current.isOpen() && puedeModificar();
    }

    public boolean verificarCerrarCaso() {
        if (current == null) {
            return false;
        }
        return current.isOpen() && puedeModificar();
    }

    public String descartarBorrador() {

        //List<AuditLog> changeLog = new ArrayList<AuditLog>();
        try {
            //  current.setFechaModif(Calendar.getInstance().getTime());
            current.setRespuesta(null);
            //changeLog.add(ManagerCasos.createLogReg(current, "Borrador", "Se descarta borrador de respuesta", ""));
            //getJpaControllerThatListenRules().mergeCaso(current, changeLog);
            addInfoMessage("Borrador descartado");
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

        List<AuditLog> changeLog = new ArrayList<>();

        try {
            current.setIdEstado(EnumEstadoCaso.CERRADO.getEstado());
            current.setFechaCierre(Calendar.getInstance().getTime());

            Nota nota = createNota(current, false, textoNota.trim(),
                    EnumTipoNota.NOTA_CIERRE.getTipoNota(), false, null);
            addNotaToCaso(current, nota);

            getJpaControllerThatListenRules().mergeCaso(current, changeLog);

            JsfUtil.addSuccessMessage(resourceBundle.getString("caso.cerrar.ok"));
            changeLog.add(ManagerCasos.createLogReg(current, "Estado", userSessionBean.getCurrent().getCapitalName() + " cerró el caso.", ""));

            HelpDeskScheluder.unscheduleCambioAlertas(getUserSessionBean().getTenantId(), current.getIdCaso());

            if (current.getCasosHijosList() != null) {
                for (Caso casoHijo : current.getCasosHijosList()) {
                    if (casoHijo.isOpen()) {
                        changeLog = new ArrayList<>();

                        casoHijo.setIdEstado(EnumEstadoCaso.CERRADO.getEstado());
                        casoHijo.setFechaCierre(Calendar.getInstance().getTime());

                        Nota nota2 = createNota(casoHijo, false, textoNota.trim(),
                                EnumTipoNota.NOTA_CIERRE.getTipoNota(), false, null);
                        addNotaToCaso(casoHijo, nota2);

                        JsfUtil.addSuccessMessage(resourceBundle.getString("caso.cerrar.ok") + "(" + casoHijo.getIdCaso() + ")");
                        changeLog.add(ManagerCasos.createLogReg(casoHijo, "Estado", userSessionBean.getCurrent().getCapitalName() + " cerró el caso.", ""));
                        getJpaControllerThatListenRules().mergeCaso(casoHijo, changeLog);

                        HelpDeskScheluder.unscheduleCambioAlertas(getUserSessionBean().getTenantId(), casoHijo.getIdCaso());
                    }
                }
            }

            refreshCurrentCaso();

            if (ApplicationConfig.isCustomerSurveyEnabled()) {
                MailNotifier.emailClientCustomerSurvey(getUserSessionBean().getTenantId(), current);
            }

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

        saveVisitaInspectivaEvent();

    }

//    public void prepareProgramarVisitaPreventiva(ActionEvent actionEvent) {
////        ////System.out.println("prepareCreateRespuesta");
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
    /**
     * brotec icafal specifics!!
     */
    private void saveVisitaInspectivaEvent() {
        String visitaPreventivaSubject = "Coordinar Visita preventiva con el cliente";
        String visitaPreventivaTextoDesc = "Caso creado a partir de una visita preventiva otorgada por la inmobiliaria, favor coordinar la fecha de visita definitiva con el cliente.";

        try {

            Date futureDate = ManagerCasos.getFechaVisitaPreventiva(current);

            Calendar cal = Calendar.getInstance();
            cal.setTime(futureDate);
            cal.add(Calendar.DAY_OF_MONTH, (visitaPreventivaCrearCasoDiasAntes * (-1)));
            Date eventDate = cal.getTime();

            ScheduleEvent entityEvent = new ScheduleEvent(null, visitaPreventivaSubject, eventDate, applicationBean.getNow());
            entityEvent.setIdCaso(current);
            entityEvent.setAllDay(true);
            entityEvent.setDescripcion(visitaPreventivaTextoDesc);
            entityEvent.setEndDate(eventDate);
            entityEvent.setIdUsuario(userSessionBean.getCurrent());
            entityEvent.setLugar("dentro del sistema");
            entityEvent.setPublicEvent(true);
//            entityEvent.setUsuariosInvitedList(visitaPreventivaAsignarAGrupo.getUsuarioList());
            entityEvent.setExecuteAction(true);
            final TipoAccion nombreAccion = EnumTipoAccion.CREAR_CASO_VISITA_REP_SELLOS.getNombreAccion();
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

            getJpaControllerThatListenRules().persist(entityEvent);

            if (entityEvent.getExecuteAction() && entityEvent.getIdTipoAccion() != null
                    && !StringUtils.isEmpty(entityEvent.getIdTipoAccion().getImplementationClassName())) {
                //we must schedule the selected action
                String jobID = HelpDeskScheluder.scheduleActionClassExecutorJob(
                        getUserSessionBean().getTenantId(),
                        getSelected().getIdCaso(),
                        entityEvent.getIdTipoAccion().getImplementationClassName(),
                        entityEvent.getParametrosAccion(),
                        entityEvent.getStartDate());
                entityEvent.setQuartzJobId(jobID);
                getJpaControllerThatListenRules().merge(entityEvent);
            }

            addInfoMessage("Evento agendado exitósamente.");
//            executeInClient("PF('ProgramarVisitaPreventiva').hide();");

        } catch (Exception ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "persistProgramarVisitaPreventivaEvent", ex);
        }
    }

    public void generarPDFVisitaPreventivaPostventa() {
        try {
//            Archivo a = getJpaControllerThatListenRules().find(Archivo.class,  getSelected().getIdProducto().getIdLogo());  
            List<Item> itemsAReparar = new LinkedList<>();
            for (Caso caso : getSelected().getCasosHijosList()) {
                if (caso.isOpen() && caso.getIdItem() != null) {
                    itemsAReparar.add(caso.getIdItem());
                }
            }

            if (!itemsAReparar.isEmpty()) {
                byte[] bytearray = ReportsManager.createVisitaPreventivaPostventa(getSelected(), ManagerCasos.getFechaVisitaPreventiva(current), ManagerCasos.getFechaVisitaPreventiva(current), itemsAReparar);

                final String nombre = "VisitaPreventiva_Postventa_" + getSelected().getIdCaso() + ".pdf";
                Attachment attach = getManagerCasos().crearAdjunto(bytearray, null, this.current, nombre, "application/pdf", (long) bytearray.length);
                List<Attachment> attachmentList = new ArrayList<>(1);
                attachmentList.add(attach);
                final String msg = "Documento " + attach.getNombreArchivo() + " creado con exito";
                textoNota = textoNota + "<br/>" + msg;
                Nota nota = createNota(getSelected(), false, textoNota.trim(),
                        EnumTipoNota.NOTA.getTipoNota(), false, attachmentList);
                addNotaToCaso(current, nota);
//                armarNota(current, false, textoNota, EnumTipoNota.NOTA.getTipoNota(), attachmentList);
                getJpaControllerThatListenRules().mergeCaso(this.current, ManagerCasos.createLogReg(current, "Attachment", "genera PDF Visita Preventiva Postventa", ""));
                addInfoMessage(msg);
                executeInClient("PF('genDocVisitaPreventiva').hide()");
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

        List<AuditLog> changeLog = new ArrayList<>();

        try {
            //TODO implementar tipo-> SubEstado selection in reabrir dialog
            current.setIdEstado(EnumEstadoCaso.ABIERTO.getEstado());
            current.setFechaModif(Calendar.getInstance().getTime());
            current.setFechaCierre(null);
            changeLog.add(ManagerCasos.createLogReg(current, "Estado", "reabre el caso", ""));
            getJpaControllerThatListenRules().mergeCaso(current, changeLog);
            JsfUtil.addSuccessMessage(resourceBundle.getString("caso.abrir.ok"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(resourceBundle.getString("caso.abrir.nook"));
        }

        return getEditPage();
    }

    public String guardarBorrador() {
        //List<AuditLog> changeLog = new ArrayList<AuditLog>();
        try {
            current.setRespuesta(textoNota);
            getJpaControllerThatListenRules().mergeCaso(current, ManagerCasos.createLogReg(current, "Borrador", "Se guarda borrador de respuesta", ""));
            JsfUtil.addSuccessMessage("Borrador guardado");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, "No se ha guardado el borrador");
        }

        return getEditPage();
    }

    @Override
    protected String getEditPage() {
        return "/script/caso/Edit";
    }

    public String agregarRelacionado() {

        try {

//            Caso casorel = getJpaController().find(Caso.class, new Long(idCaserel));
            if (idCaserel != null) {

                if (idCaserel.getIdCaso().equals(current.getIdCaso())) {
                    addErrorMessage("No se puede relacionar un caso consigo mismo.");
                    return null;
                } else {
                    current = getJpaController().find(Caso.class, current.getIdCaso());
//                casorel.getCasosRelacionadosList().add(current);
//                getJpaController().mergeCaso(casorel, getManagerCasos().verificaCambios(casorel));
                    if (current.getCasosRelacionadosList() == null) {
                        current.setCasosRelacionadosList(new CopyOnWriteArrayList<Caso>());
                    }
                    current.getCasosRelacionadosList().add(idCaserel);
                    getJpaController().mergeCaso(current, getManagerCasos().verificaCambios(current));

                    idCaserel = null;
//                current = getJpaController().find(Caso.class, current.getIdCaso());
                    JsfUtil.addSuccessMessage(resourceBundle.getString("casorel.ok"));
                    executeInClient("PF('casosRela').hide()");

                    //ManagerCasos.createLogReg(current, "Casos Relacionados", idCaserel, "");
                }

            } else {
                idCaserel = null;
                JsfUtil.addErrorMessage(resourceBundle.getString("casorel.notfound"));
            }
        } catch (Exception e) {
            idCaserel = null;
            current = getJpaController().find(Caso.class, current.getIdCaso());
            JsfUtil.addErrorMessage(resourceBundle.getString("casorel.nook"));
        }
//        current = getJpaController().find(Caso.class, current.getIdCaso());
        return "/script/caso/Edit";
    }

    public void onCasoChosenForEliminarRelacionado(SelectEvent event) {
        idCaserel = (Caso) event.getObject();
        addInfoMessage("Caso Selected Id:" + idCaserel.getIdCaso());
        try {

            if (idCaserel == null) {
                JsfUtil.addErrorMessage(resourceBundle.getString("DeletecasoRel.notfound"));
                return;
            }

            if (!current.getCasosRelacionadosList().contains(idCaserel)) {
                JsfUtil.addErrorMessage(resourceBundle.getString("DeletecasoRel.notfound"));
                return;
            }

            current.getCasosRelacionadosList().remove(idCaserel);
            getJpaController().mergeCaso(current, getManagerCasos().verificaCambios(current));

            idCaserel = null;
//            current = getJpaController().find(Caso.class, current.getIdCaso());
            JsfUtil.addSuccessMessage(resourceBundle.getString("DeletecasoRel.ok"));

        } catch (Exception e) {
            idCaserel = null;
            JsfUtil.addErrorMessage(resourceBundle.getString("DeletecasoRel.nook"));
        }

    }

    public void chooseCasoFromDialog() {
        RequestContext.getCurrentInstance().openDialog("selectOneCasoDialog");
    }

//    public String eliminarRelacionado() {
//
//        try {
//
////            Caso casorel = getJpaController().find(Caso.class, new Long(idCaserel));
//            if (idCaserel == null) {
//                JsfUtil.addErrorMessage(resourceBundle.getString("DeletecasoRel.notfound"));
//                return null;
//            }
//
//            if (!idCaserel.getCasosRelacionadosList().contains(current)) {
//                JsfUtil.addErrorMessage(resourceBundle.getString("DeletecasoRel.notfound"));
//                return null;
//            }
//
//            if (idCaserel.getIdCaso().equals(current.getIdCaso())) {
//                addErrorMessage("Error!");
//            }
////            if (casorel != null) {
////            current = getJpaController().find(Caso.class, current.getIdCaso());
////            idCaserel.getCasosRelacionadosList().remove(current);
////            getJpaController().mergeCaso(idCaserel, getManagerCasos().verificaCambios(idCaserel));
//
//            current.getCasosRelacionadosList().remove(idCaserel);
//            getJpaController().mergeCaso(current, getManagerCasos().verificaCambios(current));
//
//            idCaserel = null;
////            current = getJpaController().find(Caso.class, current.getIdCaso());
//            JsfUtil.addSuccessMessage(resourceBundle.getString("DeletecasoRel.ok"));
//            executeInClient("PF('DeleteCasosRela').hide()");
//            //ManagerCasos.createLogReg(current, "Casos Relacionados", idCaserel, "");
////            } else {
////                idCaserel = null;
////                JsfUtil.addErrorMessage(resourceBundle.getString("DeletecasoRel.notfound"));
////            }
//        } catch (Exception e) {
//            idCaserel = null;
//            current = getJpaController().find(Caso.class, current.getIdCaso());
//            JsfUtil.addErrorMessage(resourceBundle.getString("DeletecasoRel.nook"));
//        }
////        current = getJpaController().find(Caso.class, current.getIdCaso());
//        return "/script/caso/Edit";
//    }
    public void asignarCaso() {

        List<AuditLog> changeLog = new ArrayList<>();

        try {
            String motivo = textoNota;

            if (tipoTransferOption == 1) {
                //change agent
                if (usuarioSeleccionadoTransfer != null && !usuarioSeleccionadoTransfer.equals(current.getOwner())) {
                    String idUsuarioNuevo = usuarioSeleccionadoTransfer.getIdUsuario();
                    String idUsuarioAnterior = current.getOwner() == null ? "No asignado" : current.getOwner().getIdUsuario();

                    current.setOwner(usuarioSeleccionadoTransfer);
                    changeLog.add(ManagerCasos.createLogReg(current, "Asignar caso", idUsuarioNuevo, idUsuarioAnterior));

                    Nota nota = buildNewNota(getSelected(), false, motivo.trim(), EnumTipoNota.TRANSFERENCIA_CASO.getTipoNota(), false);
                    addNotaToCaso(current, nota);
//                    Nota nota = this.armarNota(current, false, motivo, EnumTipoNota.TRANSFERENCIA_CASO.getTipoNota(), null);
                    changeLog.add(ManagerCasos.createLogReg(current, "Agente agrega nota", "Agente agrega una actividad tipo " + nota.getTipoNota().getNombre(), ""));

                    getJpaControllerThatListenRules().mergeCaso(current, changeLog);
                    addInfoMessage("El caso se ha tranferido con éxito al agente " + usuarioSeleccionadoTransfer.getIdUsuario());

                    if (ApplicationConfig.isSendNotificationOnTransfer()) {
//                        if (current.getIdArea() != null && current.getIdArea().getIdArea() != null) {
                        try {
                            MailNotifier.notifyCasoAssigned(getUserSessionBean().getTenantId(), current, motivo);
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
                    current.setIdCliente(emailClienteSeleccionadoTransfer.getCliente());

                    changeLog.add(ManagerCasos.createLogReg(current, "Cambio de Cliente", idUsuarioNuevo, idUsuarioAnterior));
                    getJpaControllerThatListenRules().mergeCaso(current, changeLog);

                    JsfUtil.addSuccessMessage("El caso se ha tranferido con éxito al cliente " + emailClienteSeleccionadoTransfer.getCliente().getCapitalName());
                }
            } else {
                //error
                JsfUtil.addErrorMessage("Error!!!");
            }

            recreateModel();

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("transferir.hide()");

//            current = getJpaControllerThatListenRules().getCasoFindByIdCaso(current.getIdCaso());
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            addErrorMessage("Error al tratar de transferir el caso");
        } finally {
            resetNotaForm();
        }

//        return "/script/caso/Edit";
    }

    public String tomarCaso() {

        List<AuditLog> changeLog = new ArrayList<>();

        try {
            current = getJpaControllerThatListenRules().find(Caso.class, current.getIdCaso());
            if (current.getOwner() == null) {
                current.setOwner(userSessionBean.getCurrent());
                current.setRevisarActualizacion(false);
                changeLog.add(ManagerCasos.createLogReg(current, "Asignarmelo", current.getOwner().getIdUsuario(), ""));
                getJpaControllerThatListenRules().mergeCaso(current, changeLog);
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
                        getJpaControllerThatListenRules().mergeCaso(casoHijo, changeLog);
                    } else {
                        JsfUtil.addSuccessMessage("No se puede asignar el caso N°" + casoHijo.getIdCaso() + ", ya ha sido asignado a " + casoHijo.getOwner().toString());
                    }
                }
            }
        } catch (Exception e) {
//            ////System.out.println("tomar caso, exception: " + e.getClass().getName());
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, resourceBundle.getString("tomarcasoNOOK"));
        }

        return "/script/caso/Edit";

    }

    /**
     * persist the Object Nota to DB
     *
     * @param caso
     * @param publica
     * @param texto
     * @param tipo
     * @param customer
     * @param attachmentList
     * @return
     */
    //TODO add from to Nota type email
    private Nota createNota(Caso caso, boolean publica, String texto,
            TipoNota tipo, boolean customer, List<Attachment> attachmentList) {
//        boolean guardarNota = true;
        try {
            Nota nota = new Nota();
            nota.setIdNota(null);
            nota.setIdCaso(caso);
            nota.setFechaCreacion(Calendar.getInstance().getTime());
            nota.setTexto(HtmlUtils.stripInvalidMarkup(texto));
//            nota.setIdCanal(EnumCanal.SISTEMA.getCanal());
            nota.setVisible(publica);

            if (customer) {
                nota.setEnviadoPor(userSessionBean.getEmailCliente().getEmailCliente());
                nota.setCreadaPor(null);
                nota.setTipoNota(EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota());

            } else {

                nota.setEnviadoPor(null);
                nota.setCreadaPor(userSessionBean.getCurrent());
                nota.setTipoNota(tipo);

            }

            getJpaControllerThatListenRules().persist(nota);

            if (attachmentList != null) {
                for (Attachment attachment : attachmentList) {
                    if (attachment.getNotaList() == null) {
                        attachment.setNotaList(new LinkedList<Nota>());
                    }
                    attachment.getNotaList().add(nota);
                }

                nota.setAttachmentList(attachmentList);
                getJpaControllerThatListenRules().merge(nota);
            }

            return nota;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "createNota", e);
            addErrorMessage("ha ocurrido un error en construyendo el objeto.");
            return null;
        }
    }

    /**
     * sólo Arma una nota no la persiste (there should be a Nota factory
     * facility)
     *
     * @return
     */
    protected Nota buildNewNota(Caso caso, boolean publica, String texto, TipoNota tipo, boolean customer/*, List<Attachment> attachmentList*/) {
//        boolean guardarNota = true;
        try {
            Nota nota = new Nota();
            nota.setIdNota(null);
            nota.setIdCaso(caso);
            nota.setFechaCreacion(Calendar.getInstance().getTime());
            nota.setTexto(HtmlUtils.stripInvalidMarkup(texto));
//            nota.setIdCanal(EnumCanal.SISTEMA.getCanal());
            nota.setVisible(publica);
//            nota.setAttachmentList(attachmentList);
//
//            if (nota.getAttachmentList() != null) {
//                for (Attachment attachment : nota.getAttachmentList()) {
//                    if (attachment.getNotaList() == null) {
//                        attachment.setNotaList(new LinkedList<Nota>());
//                    }
//                    attachment.getNotaList().add(nota);
//                }
//            }

            if (customer) {
                nota.setEnviadoPor(userSessionBean.getEmailCliente().getEmailCliente());
                nota.setCreadaPor(null);
                nota.setTipoNota(EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota());

            } else {

                nota.setEnviadoPor(null);
                nota.setCreadaPor(userSessionBean.getCurrent());
                nota.setTipoNota(tipo);

            }

            return nota;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "buildNewNota", e);
            addErrorMessage("ha ocurrido un error en construyendo el objeto.");
            return null;
        }
    }

    protected void resetNotaForm() {
        this.textoNota = null;
        this.replyMode = false;
        this.replyByEmail = false;//default option
        this.selectedClipping = null;//reset clipping
        this.adjuntarArchivosARespuesta = false;//reset attach files checkbox

        if (tipoNotas != null && tipoNotas.isEmpty()) {
            tipoNotas.clear();
        }

        if (current.getNotaList() != null) {
            Collections.sort(current.getNotaList());
        }//fix when you add a nota and shows in last position

        setOtroEmail(null);
        setCcEmail(null);
        setCcoEmail(null);

        setCc(false);
        setCco(false);

    }

    protected void addNotaToCaso(Caso caso, Nota nota) {

        if (caso.getNotaList() == null) {
            caso.setNotaList(new ArrayList<Nota>());
        }
        caso.getNotaList().add(0, nota);

        if (caso.getOwner() != null) {
            if (nota.getCreadaPor() != null && nota.getCreadaPor().getIdUsuario() != null && !nota.getCreadaPor().getIdUsuario().equals(caso.getOwner().getIdUsuario())) {
                caso.setRevisarActualizacion(true);
            } else {
                if (!StringUtils.isEmpty(nota.getEnviadoPor())) {
                    caso.setRevisarActualizacion(true);
                }
            }
        }
    }

//     /**
//     * Arma una nota y la agrega al caso.
//     *
//     * @return
//     */
//    private Nota armarNota(Caso caso, boolean publica, String texto, TipoNota tipo) {
////        boolean guardarNota = true;
//        try {
//            Nota nota = new Nota();
//            nota.setFechaCreacion(Calendar.getInstance().getTime());
//            nota.setCreadaPor(userSessionBean.getCurrent());
//            nota.setIdCaso(caso);
//            nota.setTexto(HtmlUtils.removeScriptsAndStyles(texto));
//            nota.setTipoNota(tipo);
//            nota.setVisible(publica);
//           
//
//            if (caso.getOwner() != null) {
//                if (!nota.getCreadaPor().getIdUsuario().equals(caso.getOwner().getIdUsuario())) {
//                    caso.setRevisarActualizacion(true);
//                }
//            }
//
//            agregarNotaACaso(nota);
//            return nota;
//        } catch (Exception e) {
//            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
//            JsfUtil.addErrorMessage("ha ocurrido un error, nota no agregada");
//            return null;
//        }
//
//    }
    /**
     * TODO deprecate this code
     *
     * @deprecated
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

    public String crearNota(boolean notifyClient) {

        if (StringUtils.isEmpty(textoNota)) {
            addErrorMessage("Su comentario no tiene texto, verifíque e intente nuevamente");
            return null;
        }

        try {
            Nota nota = buildNewNota(current, textoNotaVisibilidadPublica, textoNota,
                    EnumTipoNota.NOTA.getTipoNota(), false);
            addNotaToCaso(current, nota);

            //evaluar estado de alerta del caso
            if (current.getNextResponseDue().after(Calendar.getInstance().getTime())) {
                current.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
                //re-schedule alert
                HelpDeskScheluder.scheduleAlertaPorVencer(getUserSessionBean().getTenantId(), current.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(current));
            }
            getJpaControllerThatListenRules().mergeCaso(current, ManagerCasos.createLogReg(current, "Agente agrega nota", "Agente agrega una actividad tipo " + nota.getTipoNota().getNombre(), ""));
            if (notifyClient) {
                notifyClient();
            }

        } catch (NonexistentEntityException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RollbackFailureException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            resetNotaForm();
        }

        return getEditPage();
    }

    private void notifyClient() {
        String correoEnviado = null;
        try {
            List<AuditLog> changeLog = new ArrayList<>();

            correoEnviado = MailNotifier.emailClientCasoUpdatedByAgent(getUserSessionBean().getTenantId(), current);

            if (correoEnviado != null) {
                final String message = "Cliente ha sido notificado por email sobre la actualización del caso. "
                        + "<br/>Email: <br/>" + correoEnviado;
                changeLog.add(ManagerCasos.createLogReg(current, "notificacion de actualización (por email)", correoEnviado, ""));

                Nota nota = buildNewNota(current, true, message,
                        EnumTipoNota.NOTIFICACION_UPDATE_CASO.getTipoNota(), false);
                addNotaToCaso(current, nota);
                changeLog.add(ManagerCasos.createLogReg(current, "Se registra envio email", correoEnviado, ""));

                JsfUtil.addSuccessMessage("Notificación enviada al cliente exitósamente.");

            } else {
                changeLog.add(ManagerCasos.createLogReg(current, "Envio Correo de Respuesta falló", "", ""));
            }

            getJpaControllerThatListenRules().mergeCaso(current, changeLog);

        } catch (Exception ex) {
            Logger.getLogger(CasoController.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            resetNotaForm();
        }
    }

    public String editNota(boolean notifyClient) {
        try {
//            listaActividadesOrdenada = null;
            //        tipoNota = EnumTipoNota.NOTA.getTipoNota();
            getJpaControllerThatListenRules().merge(selectedNota);
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
        return getEditPage();
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
    }

    private boolean createEmailComment() {

        if (StringUtils.isEmpty(textoNota)) {
            addErrorMessage("La respuesta no tiene texto, verifíque e intente nuevamente");
            return false;
        }

        if (otroEmail == null || (otroEmail != null && otroEmail.isEmpty())) {
            addErrorMessage("No se puede envíar la respuesta.", "No se han incluido destinatarios");
        } else if (current.getEmailCliente() == null || StringUtils.isEmpty(current.getEmailCliente().getEmailCliente())) {
            addErrorMessage("No se puede envíar la respuesta.", "El caso no tiene email del cliente!");
            return false;
        }

        List<AuditLog> changeLog = new ArrayList<>();
        Canal canal;

        try {
            canal = MailNotifier.chooseDefaultCanalToSendMail(current);
        } catch (NoOutChannelException no) {
            addErrorMessage("No se puede enviar la respuesta dado que el sistema no sabe por que canal responder este caso. Favor asignar un " + ApplicationConfig.getProductDescription() + " o Área al caso para poder determinar el canal de salida para enviar el correo.");
            return false;
        }
        try {

            //evaluar estado de alerta del caso
            if (current.getNextResponseDue().after(Calendar.getInstance().getTime())) {
                current.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
                //re-schedule alert
                HelpDeskScheluder.scheduleAlertaPorVencer(getUserSessionBean().getTenantId(), current.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(current));
            }

            String mensaje = textoNota;

            List<Attachment> attachmentList = null;

            if (adjuntarArchivosARespuesta && selectedAttachmensForMail != null) {
                attachmentList = selectedAttachmensForMail;
            }

            //TODO: create an Object Builder factory
            Nota nota = createNota(getSelected(), true, textoNota.trim(),
                    EnumTipoNota.REG_ENVIO_CORREO.getTipoNota(), false, attachmentList);

//            Nota nuevaNota = this.armarNota(current, true, textoNota.trim(),
//                    EnumTipoNota.REG_ENVIO_CORREO.getTipoNota(), attachmentList);
//            getJpaControllerThatListenRules().persist(nota);
//            getJpaControllerThatListenRules().merge(nota);
            addNotaToCaso(current, nota);

            this.justCreadedNotaId = nota.getIdNota();

//            if (nuevaNota != null) {
            String subject = "Re: " + ManagerCasos.formatIdCaso(current.getIdCaso()) + " "
                    + current.getTema();
//                correoAgendado = enviarCorreo(current.getEmailCliente().getEmailCliente(), 
//                        subject, mensaje, nuevaNota);

            StringBuilder listIdAtt = new StringBuilder();

            if (adjuntarArchivosARespuesta && selectedAttachmensForMail != null) {

                Iterator<Attachment> iteradorAttachments = selectedAttachmensForMail.iterator();
                while (iteradorAttachments.hasNext()) {
                    final Attachment next = iteradorAttachments.next();
                    listIdAtt.append(next.getIdAttachment());
                    listIdAtt.append(';');
                }
            }

            StringBuilder sbuilder = new StringBuilder();
            String destinatario;
            if (otroEmail != null && !otroEmail.isEmpty()) {
                for (String string : otroEmail) {
                    if (sbuilder.length() > 0) {
                        sbuilder.append(',');
                    }
                    sbuilder.append(string);
                }
                destinatario = sbuilder.toString();
            } else {
                destinatario = current.getEmailCliente().getEmailCliente();
            }
            String ccEmails = null;
            if (isCc() && ccEmail != null && !ccEmail.isEmpty()) {
                sbuilder = new StringBuilder();
                for (String string : ccEmail) {
                    if (sbuilder.length() > 0) {
                        sbuilder.append(',');
                    }
                    sbuilder.append(string);
                }
                ccEmails = sbuilder.toString();
            }
            String ccoEmails = null;
            if (isCco() && ccoEmail != null && !ccoEmail.isEmpty()) {
                sbuilder = new StringBuilder();
                for (String string : ccoEmail) {
                    if (sbuilder.length() > 0) {
                        sbuilder.append(',');
                    }
                    sbuilder.append(string);
                }
                ccoEmails = sbuilder.toString();
            }
            HelpDeskScheluder.scheduleSendMailNota(
                    getUserSessionBean().getTenantId(),
                    canal.getIdCanal(), mensaje,
                    destinatario, ccEmails, ccoEmails, subject,
                    current.getIdCaso(), nota.getIdNota(), listIdAtt.toString());
            changeLog.add(ManagerCasos.createLogReg(current, "Envío de Correo de Respuesta agendado ok", userSessionBean.getCurrent().getIdUsuario() + " envía correo de respuesta.", ""));

            getJpaControllerThatListenRules().mergeCaso(current, changeLog);//todo: is this needed?

            return true;

//            }
//                else {
//                changeLog.add(ManagerCasos.createLogReg(current, "Creación de Respuesta fallida", "Creación de la nota de respuesta falló.", ""));
//            }
        } catch (SchedulerException e) {
            changeLog.add(ManagerCasos.createLogReg(current, "Envio de Correo de Respuesta falló", "Agendar Envío de Correo de Respuesta falló", ""));
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "mail send exception in createEmailComment", e);
            JsfUtil.addErrorMessage(e, "Agendar Envío de Correo de Respuesta falló");

        } catch (NonexistentEntityException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "NonexistentEntityException", ex);
        } catch (RollbackFailureException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "RollbackFailureException", ex);
        } catch (Exception ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "Exception", ex);
        } finally {
            resetNotaForm();
            getPrimefacesRequestContext().scrollTo("inputPanel:formact:panelActividades");
        }

        return false;
    }

//    private boolean enviarCorreo(final String emailCliente, final String subject, String mensaje, Nota nota) throws SchedulerException, NoOutChannelException {
//
//        
//    }
    public String upload() {
//        ////System.out.println("upload()");
        if (uploadFile != null) {
//            ////System.out.println("Succesful " + uploadFile.getFileName() + " is uploaded.");
            FacesMessage msg = new FacesMessage("Succesful", uploadFile.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return null;
    }

    private String obtenerHistorial() {
        StringBuilder sbuilder = new StringBuilder(HEADER_HISTORY);
        if (current != null && current.getNotaList() != null) {
            for (Nota nota : current.getNotaList()) {
                if (nota.getVisible() != null && nota.getVisible()) {
                    sbuilder.append(creaMensajeOriginal(nota));
                }
            }
        }

        sbuilder.append(creaMensajeOriginal(current));
//        if (sbuilder.length() > MEGABYTE) {
//            String onlyText = HtmlUtils.extractText(sbuilder.toString());
//            if (onlyText.length() > MEGABYTE) {
//                return onlyText.substring(0, MEGABYTE);
//            } else {
//                return onlyText;
//            }
//        }
        return sbuilder.toString();
    }

    private String creaMensajeOriginal(Caso caso) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("<br/>");
        sbuilder.append("MENSAJE ORIGINAL");
        sbuilder.append("<br/>");
        sbuilder.append("Email cliente: ");
        sbuilder.append(caso.getEmailCliente() != null ? caso.getEmailCliente() : caso.getIdCliente() != null ? caso.getIdCliente().getEmailClienteList() : "No tiene");
        sbuilder.append("<br/>");
        sbuilder.append("Asunto: ");
        sbuilder.append(caso.getTema());
        sbuilder.append("<br/>");
        sbuilder.append("Fecha: ");
        sbuilder.append(caso.getFechaCreacion() != null ? sdf.format(caso.getFechaCreacion()) : "<unknown>");
        sbuilder.append("<br/>");
        sbuilder.append("Mensaje: ");
        sbuilder.append("<br/>");
        sbuilder.append(replaceEmbeddedImages(caso.getDescripcion()));
        sbuilder.append("<br/>");
        sbuilder.append(FOOTER_HISTORY_NOTA);
        sbuilder.append("<hr/>");
        return sbuilder.toString();
    }

    private String creaMensajeOriginal(Nota nota) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");

        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("INICIO ");
        sbuilder.append(nota.getTipoNota() != null ? nota.getTipoNota().getNombre() : "Comentario");
        sbuilder.append("<br/>");
        sbuilder.append("De: ");
        sbuilder.append(nota.getEnviadoPor() != null ? nota.getEnviadoPor() : nota.getCreadaPor() != null ? nota.getCreadaPor().getCapitalName() : "?");
        sbuilder.append("<br/>");
        sbuilder.append("Para: ");
        sbuilder.append(nota.getEnviadoA() != null ? nota.getEnviadoA() : "A nadie");
        sbuilder.append("<br/>");
        sbuilder.append("Fecha: ");
        sbuilder.append(nota.getFechaCreacion() != null ? sdf.format(nota.getFechaCreacion()) : "<unknown>");
        sbuilder.append("<br/>");
        sbuilder.append("<b>Mensaje: </b>");
        sbuilder.append("<br/>");
        sbuilder.append(replaceEmbeddedImages(extractNotaNewPart(nota)));
        sbuilder.append("<hr/>");
        return sbuilder.toString();
    }

    public void deleteAttachment(ActionEvent actionEvent) {
        try {
            Attachment att = getJpaControllerThatListenRules().find(Attachment.class, new Long(idFileDelete), true);
            String nombre = att.getNombreArchivo();
            Archivo archivo = getJpaControllerThatListenRules().find(Archivo.class, att.getIdAttachment(), true);
            getJpaControllerThatListenRules().remove(Archivo.class, archivo);
            getJpaControllerThatListenRules().remove(Attachment.class, att);

            current = getJpaControllerThatListenRules().find(Caso.class, current.getIdCaso());
            JsfUtil.addSuccessMessage("Archivo " + nombre + " borrado");
            getJpaControllerThatListenRules().persist(ManagerCasos.createLogReg(current, "Archivo borrado", "Archivo borrado: " + nombre, ""));

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
            Attachment att = getJpaControllerThatListenRules().find(Attachment.class, new Long(id));

            Archivo archivo = getJpaControllerThatListenRules().find(Archivo.class, att.getIdAttachment());
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
                    archivoLogo = getJpaController().find(Archivo.class, idLogo);
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
            List<Caso> casos = (List<Caso>) getJpaControllerThatListenRules().findAllEntities(getVista(), getDefaultOrderBy(), userSessionBean.getCurrent());//all
            OutputStream output = new ByteArrayOutputStream();
            //Se crea el libro Excel
            WritableWorkbook workbook = Workbook.createWorkbook(output);

            //Se crea una nueva hoja dentro del libro
            WritableSheet sheet = workbook.createSheet("Casos " + getVista().toString(), 0);
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
//                    //System.out.print(sheet.getCell(i, row).getContents()+";");                    
//                }
//                ////System.out.println("");
                    row++;
                }
            }

            workbook.write();
            workbook.close();
            InputStream decodedInput = new ByteArrayInputStream(((ByteArrayOutputStream) output).toByteArray());
            //////System.out.println("Ejemplo finalizado.");
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

            ////System.out.println("CURRENT:"+this.current);
            if (this.current != null) {
                getManagerCasos().crearAdjunto(bytearray, null, this.current, nombre, event.getFile().getContentType(), event.getFile().getSize());
                JsfUtil.addSuccessMessage("Archivo " + nombre + " subido con exito");
                getJpaControllerThatListenRules().merge(this.current);
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
            archivo.setFileSize(size);

            if (current.getAttachmentList() == null) {
                current.setAttachmentList(new ArrayList<Attachment>());
                ////System.out.println("CURRENT:"+this.current);
            }

            Attachment attach = new Attachment(n);
            attach.setIdCaso(current);

            attach.setNombreArchivo(nombre);
            attach.setNombreArchivoOriginal(nombre);
            attach.setFileExtension(nombre.substring(nombre.lastIndexOf('.')));
            attach.setMimeType(event.getFile().getContentType());
            attach.setArchivo(archivo);
            current.getAttachmentList().add(attach);
            ////System.out.println("CURRENT:"+this.current);

            //getJpaControllerThatListenRules().persistAttachment(attach);//later
            //archivo.setIdAttachment(attach.getIdAttachment());run later in create
            //getJpaControllerThatListenRules().persistArchivo(archivo); // run later in create
            JsfUtil.addSuccessMessage("Archivo " + nombre + " subido éxitosamente.");
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Error al subir archivo", e);
            JsfUtil.addErrorMessage(e, e.getLocalizedMessage());
        }
    }

    public String mergeCliente() {
        try {
            getJpaControllerThatListenRules().merge(current.getIdCliente());
            executeInClient("PF('dialogClient').hide()");
            JsfUtil.addSuccessMessage("Cliente actualizado exitosamente.");

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
        return null;
    }

    public String update() {
        try {

            if (validateEdit()) {
                return null;
            }

            update(current);
            return getEditPage();
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

        boolean cambiaArea = false;

        //Recintos brotec-icafal specifics
        if (casoToUpdate.getIdRecinto() != null && null == getJpaControllerThatListenRules().find(Recinto.class, casoToUpdate.getIdRecinto())) {
            Recinto r = new Recinto();

            r.setIdRecinto(casoToUpdate.getIdRecinto());
            r.setNombre(casoToUpdate.getIdRecinto());
            getJpaControllerThatListenRules().persist(r);
        }

        List<AuditLog> lista = getManagerCasos().verificaCambios(casoToUpdate);

        if (lista != null) {
            for (AuditLog auditLog : lista) {
                if (auditLog.getCampo().equalsIgnoreCase(Caso_.nextResponseDue.getName())) {
                    //evaluar estado de alerta del caso
                    if (casoToUpdate.getNextResponseDue().after(Calendar.getInstance().getTime())) {
                        casoToUpdate.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
                        //re-schedule alert
                        HelpDeskScheluder.scheduleAlertaPorVencer(getUserSessionBean().getTenantId(), casoToUpdate.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(casoToUpdate));
                    }
                } else if (auditLog.getCampo().equalsIgnoreCase(Caso_.AREA_FIELD_NAME)) {
                    cambiaArea = true;
                }
            }
        }

        if (cambiaArea) {
            getJpaControllerThatListenRules().mergeCasoWithoutNotify(casoToUpdate, lista);
            getJpaControllerThatListenRules().notifyCasoEventListeners(casoToUpdate, true, lista);
        } else {
            getJpaControllerThatListenRules().mergeCaso(casoToUpdate, lista);
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
                HelpDeskScheluder.unscheduleCambioAlertas(getUserSessionBean().getTenantId(), current.getIdCaso());

            } catch (SchedulerException ex) {
                Logger.getLogger(CasoController.class
                        .getName()).log(Level.SEVERE, "unscheduleTask", ex);
            }
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
                ArrayList<Caso> notDeleted = new ArrayList<>();
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

            executeInClient("PF('deleteSelectedCasos').hide()");

        } catch (Exception e) {
            addErrorMessage(e.getLocalizedMessage());
        }
    }

    private boolean performDestroy(Caso caso) {
        try {
            getJpaControllerThatListenRules().persist(ManagerCasos.createLogReg(caso, "Eliminar", userSessionBean.getCurrent().getIdUsuario() + " elimina el caso manualmente", ""));
            getJpaControllerThatListenRules().remove(Caso.class, caso);
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

    public List<Attachment> getSelectedAttachmensForMail() {
        return selectedAttachmensForMail;
    }

    public void setSelectedAttachmensForMail(List<Attachment> selectedAttachmensForMail) {
        this.selectedAttachmensForMail = selectedAttachmensForMail;
    }

    @Override
    public void recreateModel() {
        this.items = null;
        this.recreatePagination();
        this.emailClienteSeleccionadoTransfer = null;
        this.usuarioSeleccionadoTransfer = null;
    }

    public SelectItem[] getClippingsItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(getJpaControllerThatListenRules().findAll(Clipping.class), true);
    }

    public void handleClippingSelectChangeEvent() {
        //textoNota = null;
        if (selectedClipping != null && selectedClipping.getTexto() != null) {
            StringBuilder sb = new StringBuilder(ClippingsPlaceHolders.buildFinalText(selectedClipping.getTexto(), current));
            sb.append("<br/>");
            sb.append(textoNota);
            textoNota = sb.toString();
        } else {
            addErrorMessage("No se ha seleccionado ningun clipping.");
        }

    }

    public SelectItem[] getItemsSubEstadoCasoAvailableSelectOneCasoCerrado() {
        List<SubEstadoCaso> lista = new ArrayList<>();
        try {
            if (current.getTipoCaso() != null) {
                final List<SubEstadoCaso> subEstadoCasofindByIdEstadoAndTipoCaso = getJpaControllerThatListenRules().getSubEstadoCasofindByIdEstadoAndTipoCaso(EnumEstadoCaso.CERRADO.getEstado(), current.getTipoCaso());
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
        List<SubEstadoCaso> lista = new ArrayList<>();

        try {
            if (current.getTipoCaso() != null) {
                final List<SubEstadoCaso> subEstadoCasofindByIdEstadoAndTipoCaso = getJpaControllerThatListenRules().getSubEstadoCasofindByIdEstadoAndTipoCaso(EnumEstadoCaso.ABIERTO.getEstado(), current.getTipoCaso());
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
        //////System.out.println("comparando "+header+" vs "+value+" resp="+resp);
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
            Attachment att = getJpaControllerThatListenRules().findAttachmentByContentId('<' + contentId + '>', current);

            Archivo archivo = getJpaControllerThatListenRules().find(Archivo.class, att.getIdAttachment());
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

//    /**
//     * @return the activeIndexdescOrComment
//     */
//    public int getActiveIndexdescOrComment() {
//        return activeIndexdescOrComment;
//    }
//
//    /**
//     * @param activeIndexdescOrComment the activeIndexdescOrComment to set
//     */
//    public void setActiveIndexdescOrComment(int activeIndexdescOrComment) {
//        this.activeIndexdescOrComment = activeIndexdescOrComment;
//
//    }
    /**
     * @return the selectedItemIndex
     */
    public int getSelectedItemIndex() {
        return selectedItemIndex;
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

    /**
     * @return the responderAOtroEmail
     */
    public boolean isResponderAOtroEmail() {
        return responderAOtroEmail;
    }

    /**
     * @param responderAOtroEmail the responderAOtroEmail to set
     */
    public void setResponderAOtroEmail(boolean responderAOtroEmail) {
        this.responderAOtroEmail = responderAOtroEmail;
    }

    /**
     * @return the otroEmail
     */
    public List<String> getOtroEmail() {
        return otroEmail;
    }

    /**
     * @param otroEmail the otroEmail to set
     */
    public void setOtroEmail(List<String> otroEmail) {
        this.otroEmail = otroEmail;
    }

    /**
     * @return the cc
     */
    public boolean isCc() {
        return cc;
    }

    /**
     * @param cc the cc to set
     */
    public void setCc(boolean cc) {
        this.cc = cc;
    }

    /**
     * @return the cco
     */
    public boolean isCco() {
        return cco;
    }

    /**
     * @param cco the cco to set
     */
    public void setCco(boolean cco) {
        this.cco = cco;
    }

    /**
     * @return the ccEmail
     */
    public List<String> getCcEmail() {
        return ccEmail;
    }

    /**
     * @param ccEmail the ccEmail to set
     */
    public void setCcEmail(List<String> ccEmail) {
        this.ccEmail = ccEmail;
    }

    /**
     * @return the ccoEmail
     */
    public List<String> getCcoEmail() {
        return ccoEmail;
    }

    /**
     * @param ccoEmail the ccoEmail to set
     */
    public void setCcoEmail(List<String> ccoEmail) {
        this.ccoEmail = ccoEmail;
    }

    /**
     * @return the replyByEmail
     */
    public boolean isReplyByEmail() {
        return replyByEmail;
    }

    /**
     * @param replyByEmail the replyByEmail to set
     */
    public void setReplyByEmail(boolean replyByEmail) {
        this.replyByEmail = replyByEmail;
    }

    /**
     * @return the selectedViewId
     */
    public Integer getSelectedViewId() {
        return selectedViewId;
    }

    /**
     * @param selectedViewId the selectedViewId to set
     */
    public void setSelectedViewId(Integer selectedViewId) {
        this.selectedViewId = selectedViewId;
    }

    /**
     * @return the replyMode
     */
    public boolean isReplyMode() {
        return replyMode;
    }

    /**
     * @param replyMode the replyMode to set
     */
    public void setReplyMode(boolean replyMode) {
        this.replyMode = replyMode;
    }

    /**
     * @return the searchBarVisible
     */
    public boolean isSearchBarVisible() {
        return searchBarVisible;
    }

    /**
     * @param searchBarVisible the searchBarVisible to set
     */
    public void setSearchBarVisible(boolean searchBarVisible) {
        this.searchBarVisible = searchBarVisible;
    }

    /**
     * @return the habilitarMerge
     */
    public boolean isMergeHabilitado() {
        return mergeHabilitado;
    }

    /**
     * @param habilitarMerge the habilitarMerge to set
     */
    public void setMergeHabilitado(boolean habilitarMerge) {
//        //System.out.println("habilitarMerge: " + habilitarMerge);
        this.mergeHabilitado = habilitarMerge;
//        //System.out.println("seted habilitarMerge: " + this.mergeHabilitado);
    }
    
     public String getCanalStyleClass(Canal canal) {
        if (canal != null) {
            if (canal.getIdTipoCanal().equals(EnumTipoCanal.CHAT.getTipoCanal())) {
                return "fa-comments";
            } else if (canal.getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal())) {
                return "fa-envelope";
            } else if (canal.getIdTipoCanal().equals(EnumTipoCanal.PHONE.getTipoCanal())) {
                return "fa-phone";
            } else if (canal.getIdTipoCanal().equals(EnumTipoCanal.MANUAL.getTipoCanal())) {
                return "fa-ticket";
            } else if (canal.getIdTipoCanal().equals(EnumTipoCanal.APPLICATION.getTipoCanal())) {
                return "fa-comment";
            } else if (canal.getIdTipoCanal().equals(EnumTipoCanal.FORMULARIO.getTipoCanal())) {
                return "fa-cloud";
            }
        }

        return "fa-exclamation";
    }

    /**
     * @return the subComponente_wizard
     */
    public SubComponente getSubComponente_wizard() {
        return subComponente_wizard;
    }

    /**
     * @param subComponente_wizard the subComponente_wizard to set
     */
    public void setSubComponente_wizard(SubComponente subComponente_wizard) {
        this.subComponente_wizard = subComponente_wizard;
    }

    /**
     * @return the cliente_wizard
     */
    public Cliente getCliente_wizard() {
        return cliente_wizard;
    }

    /**
     * @param cliente_wizard the cliente_wizard to set
     */
    public void setCliente_wizard(Cliente cliente_wizard) {
        this.cliente_wizard = cliente_wizard;
    }

    /**
     * @return the tipoAsoc_wizard
     */
    public String getTipoAsoc_wizard() {
        return tipoAsoc_wizard;
    }

    /**
     * @param tipoAsoc_wizard the tipoAsoc_wizard to set
     */
    public void setTipoAsoc_wizard(String tipoAsoc_wizard) {
        this.tipoAsoc_wizard = tipoAsoc_wizard;
    }

    /**
     * @return the pcWizard
     */
    public ProductoContratado getPcWizard() {
        return pcWizard;
    }

    /**
     * @param pcWizard the pcWizard to set
     */
    public void setPcWizard(ProductoContratado pcWizard) {
        this.pcWizard = pcWizard;
    }

    @FacesConverter(forClass = Caso.class)
    public static class CasoControllerConverter implements Converter, Serializable {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "UserSessionBean");
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
                if (obj.getIdCaso() != null) {
                    if (obj.getIdCaso().toString().equals(rowKey)) {
                        return obj;
                    }
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
