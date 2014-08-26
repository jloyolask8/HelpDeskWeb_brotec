/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Caso_;
import com.itcs.helpdesk.persistence.entities.Etiqueta;
import com.itcs.helpdesk.persistence.entities.FieldType;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Prioridad;
import com.itcs.helpdesk.persistence.entities.ReglaTrigger;
import com.itcs.helpdesk.persistence.entities.TipoAlerta;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.custom.CasoJPACustomController;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.actionsimpl.NotifyGroupCasoReceivedAction;
import com.itcs.helpdesk.rules.actionsimpl.SendCaseByEmailAction;
import com.itcs.helpdesk.util.ApplicationConfig;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;

/**
 *
 * @author jonathan
 */
@ManagedBean
@ApplicationScoped
public class ApplicationBean extends AbstractManagedBean<Object> implements Serializable {

//    private String defaultContactEmail = null;
    //  Map<String, String> loggedInUsers = new HashMap<String, String>();
    private Map<String, String> channels = new HashMap<String, String>();
    //--pre created Vistas
    private final transient Map<Integer, Vista> predefinedVistas = new HashMap<Integer, Vista>();
    private Vista vistaRevisarActualizacion;
    private transient final Map<String, Action> predefinedActions = new HashMap<String, Action>();

    private String ckEditorToolbar = "[{ name: 'document', items : ['Source','Preview', 'SpellChecker', 'Scayt', 'Link', 'Unlink', 'Iframe', 'Image','Table','HorizontalRule','NumberedList','BulletedList'] },"
            + "{ name: 'style', items : ['Bold','Italic','Underline','TextColor','BGColor', '-','RemoveFormat','Blockquote'] },"
            + "{ name: 'style', items : ['Styles','Format','Maximize']}]";

    /**
     * Creates a new instance of ApplicationBean
     */
    public ApplicationBean() {
        super(Object.class);
    }

//    @PostConstruct
    public void init() {
        for (EnumTipoAlerta enumTipoAlerta : EnumTipoAlerta.values()) {
            predefinedVistas.put(enumTipoAlerta.getTipoAlerta().getIdalerta(), createVistaPorAlerta(enumTipoAlerta.getTipoAlerta()));
        }
        vistaRevisarActualizacion = createVistaRevisarActualizacion();

        SendCaseByEmailAction caseByEmailAction = new SendCaseByEmailAction(getJpaController());
        predefinedActions.put("SendCaseByEmail", caseByEmailAction);

        NotifyGroupCasoReceivedAction casoReceivedAction = new NotifyGroupCasoReceivedAction(getJpaController());
        predefinedActions.put("NotifyGroupCasoReceived", casoReceivedAction);
    }

    public void onIdle() {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Inactividad detectada",
                "Su sesión expirará si no realiza ninguna actividad.");

        RequestContext.getCurrentInstance().showMessageInDialog(message);

//        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
//                                        "Se ha detectado inactividad.", "Su sesión expirará si no realiza ninguna actividad."));
    }

    public void onActive() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Que bueno que ya estás de vuelta", "Buen Coffee break!"));
        executeInClient("primefacesmessagedlg.hide()");
    }

    /**
     * @return the vistaRevisarActualizacion
     */
    public Vista getVistaRevisarActualizacion() {
        return vistaRevisarActualizacion;
    }

    public Vista getVistaPorAlerta(TipoAlerta alerta) {
        return predefinedVistas.get(alerta.getIdalerta());
    }

    private Vista createVistaRevisarActualizacion() {
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(((UserSessionBean) JsfUtil.getManagedBean("UserSessionBean")).getCurrent());
        vista1.setNombre("Casos recientemente Actualizados");

        FiltroVista filtroOwner = new FiltroVista();
        filtroOwner.setIdFiltro(-1);
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(CasoJPACustomController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroOwner);

        FiltroVista reviewUpdate = new FiltroVista();
        reviewUpdate.setIdFiltro(-2);
        reviewUpdate.setIdCampo(Caso_.REVISAR_ACTUALIZACION_FIELD_NAME);
        reviewUpdate.setVisibleToAgents(false);
        reviewUpdate.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        reviewUpdate.setValor(Boolean.TRUE.toString());
        reviewUpdate.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(reviewUpdate);
        return vista1;
    }

    private Vista createVistaPorAlerta(TipoAlerta alerta) {
//        //System.out.println("filtraPorAlerta");
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(((UserSessionBean) JsfUtil.getManagedBean("UserSessionBean")).getCurrent());
        vista1.setNombre("Casos con Alerta " + alerta.getNombre());

        FiltroVista filtroOwner = new FiltroVista();
        filtroOwner.setIdFiltro(-1);
        filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroOwner.setValor(CasoJPACustomController.PLACE_HOLDER_CURRENT_USER);
        filtroOwner.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroOwner);

        FiltroVista filtroEstado = new FiltroVista();
        filtroOwner.setIdFiltro(-2);
        filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
        filtroEstado.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroEstado);

        FiltroVista filtroAlerta = new FiltroVista();
        filtroOwner.setIdFiltro(-3);
        filtroAlerta.setIdCampo(Caso_.ESTADO_ALERTA_FIELD_NAME);
        filtroAlerta.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroAlerta.setValor(alerta.getIdalerta().toString());
        filtroAlerta.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroAlerta);

        return vista1;
    }

    public void addChannel(String user, String channel) {
        channels.put(user, channel);
    }

    public String getChannel(String user) {
        return channels.get(user);
    }

    public void sendFacesMessageNotification(String idUsuario, String m) {

        PushContext pushContext = PushContextFactory.getDefault().getPushContext();
        final String channel = getChannel(idUsuario);

//        System.out.println("SENDING NOTIFICATION TO " + channel);
        if (channel != null) {
//            System.out.println("SENT NOTIFICATION TO " + channel);
            pushContext.push(channel, new FacesMessage(m));
        } else {
            System.out.println("CHANNEL NOT OPPENED, COULD NOT SEND NOTIFICATION TO " + idUsuario);
        }

    }

    public Date getNow() {
        //please improve this to use calendar instance get time
        return new java.util.Date();
    }

    public SelectItem[] getEtiquetaItemsAvailableSelect() {
        return JsfUtil.getSelectItemsStrings(getJpaController().findAll(Etiqueta.class), false);
    }

    public List<Etiqueta> findEtiquetasByPattern(String pattern) {
        List<Etiqueta> results = new ArrayList<Etiqueta>();
        if (pattern != null) {
            results.add(new Etiqueta(pattern.trim()));
//          results.addAll(getJpaController().findEtiquetasLike(pattern.trim()));
            results.addAll(getJpaController().findEtiquetasLike(pattern.trim(), getUserSessionBean().getCurrent().getIdUsuario()));
        }
        //return ((List<Etiqueta>) getJpaController().findEtiquetasLike(pattern));
        return results;
    }

    public List<Etiqueta> getEtiquetasAll() {
        return (List<Etiqueta>) getJpaController().findAll(Etiqueta.class);
    }

    public List<ReglaTrigger> getReglasAll() {
        return (List<ReglaTrigger>) getJpaController().findAll(ReglaTrigger.class);
    }

    public List<FieldType> getAllFieldTypes() {
        return getJpaController().getCustomFieldTypes();
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("This operation is Not supported!.");
    }

    /**
     * @return the appPageTitle
     */
    public String getHelpdeskTitle() {
        return ApplicationConfig.getHelpdeskTitle();
    }

    public String getCompanyName() {
        return ApplicationConfig.getCompanyName();
    }

    public String getCompanyLogo() {
        return ApplicationConfig.getCompanyLogo();
    }

//    public String getCompanyDefaultContactEmail() {
////        if (defaultContactEmail == null) {
////            String idDefaultArea = EnumAreas.DEFAULT_AREA.getArea().getIdArea();
////            Area a = getJpaController().find(Area.class, idDefaultArea);
////            if (a != null && !StringUtils.isEmpty(a.getMailInboundUser())) {
////                defaultContactEmail = a.getMailInboundUser();
////            }
////        }
//
//        return defaultContactEmail;
//
//    }
    public String getProductDescription() {
        return ApplicationConfig.getProductDescription();
    }

    public String getProductComponentDescription() {
        return ApplicationConfig.getProductComponentDescription();
    }

    public String getProductSubComponentDescription() {
        return ApplicationConfig.getProductSubComponentDescription();
    }

    public List<TipoCaso> getTipoCasoAvailableList() {
        System.out.println("*** getTipoCasoAvailableList()");
        return (List<TipoCaso>) getJpaController().findAll(TipoCaso.class);
    }

    public List<Prioridad> getPrioridadItemsAvailableList() {
        return (List<Prioridad>) getJpaController().findAll(Prioridad.class);
    }

    public boolean isShowCompanyLogo() {
        return ApplicationConfig.isShowCompanyLogo();
    }

    public StreamedContent getLogo() {

        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(0L);
            if (archivo != null) {
                return new DefaultStreamedContent(
                        new ByteArrayInputStream(archivo.getArchivo()), archivo.getContentType(), "logo");
            } else {
                return new DefaultStreamedContent();
            }
        }
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the defaultContactEmail
     */
//    public String getDefaultContactEmail() {
//        return defaultContactEmail;
//    }
    /**
     * @return the predefinedActions
     */
    public Set<String> getPredefinedActionsAsString() {
        return predefinedActions.keySet();
    }

    public Map<String, Action> getPredefinedActions() {
        return predefinedActions;
    }

    /**
     * @return the ckEditorToolbar
     */
    public String getCkEditorToolbar() {
        return ckEditorToolbar;
    }

    /**
     * @param ckEditorToolbar the ckEditorToolbar to set
     */
    public void setCkEditorToolbar(String ckEditorToolbar) {
        this.ckEditorToolbar = ckEditorToolbar;
    }

    public String generateRandomColor() {
        Color baseColor = new Color(123, 209, 72);        
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        if (baseColor != null) {
            red = (red + baseColor.getRed()) / 2;
            green = (green + baseColor.getGreen()) / 2;
            blue = (blue + baseColor.getBlue()) / 2;
        }

        String color = "#" + Integer.toHexString(red)
                + Integer.toHexString(green)
                + Integer.toHexString(blue);

        return color;
    }
}
