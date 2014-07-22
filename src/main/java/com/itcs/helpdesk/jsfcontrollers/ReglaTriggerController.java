package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JPAFilterHelper;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Accion;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Categoria;
import com.itcs.helpdesk.persistence.entities.Condicion;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Prioridad;
import com.itcs.helpdesk.persistence.entities.ReglaTrigger;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumNombreAccion;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.EmailStruct;
import com.thoughtworks.xstream.XStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "reglaTriggerController")
@SessionScoped
public class ReglaTriggerController extends AbstractManagedBean<ReglaTrigger> implements Serializable {

//    private ReglaTrigger current;
    private List<ReglaTrigger> reglaItems = null;
    private Integer rows = 15;
//    private transient PaginationHelper pagination;
    private int selectedItemIndex;
    //----logica de reglas
    private List<Accion> acciones = new ArrayList<Accion>();
    private HashMap<String, String> actionClassNames = new HashMap<String, String>();
    private Accion accionTemp = new Accion();
    //----fin logica de reglas
    private transient XStream xstream;
    private transient TreeNode catNodeSelected;
    private Grupo grupoTemp;
    private Area areaTemp;
    private Usuario usuarioTemp;
    private Prioridad prioridadTemp;
    private transient EmailStruct emailTemp;
    private transient JPAFilterHelper filterHelper;

    public ReglaTriggerController() {
        super(ReglaTrigger.class);
        xstream = new XStream();
        actionClassNames.put("SendCaseByEmail","com.itcs.helpdesk.rules.actionsimpl.SendCaseByEmailAction");
        actionClassNames.put("NotifyGroupCasoReceived","com.itcs.helpdesk.rules.actionsimpl.NotifyGroupCasoReceivedAction");
        actionClassNames.put("ParseCotizacionAddElInmobiliario","com.itcs.helpdesk.rules.customactions.ParseCotizacionAddElInmobiliarioAction");
        actionClassNames.put("ParseCotizacionEnlaceInmobiliario","com.itcs.helpdesk.rules.customactions.ParseCotizacionEnlaceInmobiliarioAction");
        actionClassNames.put("ParseCotizacionPortalInmobiliario","com.itcs.helpdesk.rules.customactions.ParseCotizacionPortalInmobiliarioAction");
        actionClassNames.put("ParseCotizacionPortalInmobiliarioAlt","com.itcs.helpdesk.rules.customactions.ParseCotizacionPortalInmobiliarioAltAction");
        actionClassNames.put("ParseCotizacionVendedorWeb","com.itcs.helpdesk.rules.customactions.ParseCotizacionVendedorWebAction");
        actionClassNames.put("ParseCotizacionZoomInmobiliario","com.itcs.helpdesk.rules.customactions.ParseCotizacionZoomInmobiliarioAction");
        actionClassNames.put("RemoveDuplicatedCasoPreventa","com.itcs.helpdesk.rules.customactions.RemoveDuplicatedCasoPreventaAction");
                
    }

    public void onReglaOrderEdit() {
//        Object oldValue = event.getOldValue();  
//        Object newValue = event.getNewValue();  
//          
//        if(newValue != null && !newValue.equals(oldValue)) {  
        Collections.sort(reglaItems);
        for (ReglaTrigger reglaTrigger : reglaItems) {
            try {
                getJpaController().merge(reglaTrigger);
            } catch (Exception ex) {
                addMessage(FacesMessage.SEVERITY_ERROR, "No se pudo persistir el cambio de orden de la regla " + reglaTrigger.getIdTrigger());
                Logger.getLogger(ReglaTriggerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        }  

    }

    /**
     * @deprecated @param node
     */
    public void setCatNodeSelected(TreeNode node) {
        Categoria cat = ((Categoria) node.getData());
        System.out.println("setCatNodeSelected: " + cat.getNombre());
        accionTemp.setParametros(cat.getNombre() + " ID[" + cat.getIdCategoria() + "]");
//        condicionTemp.setValor(  cat.getNombre() + " ID[" + cat.getIdCategoria() + "]");
        this.catNodeSelected = node;
    }

    /**
     * @deprecated @return
     */
    public TreeNode getCatNodeSelected() {
        return catNodeSelected;
    }

    public void valueChangeListener(ValueChangeEvent e) {
        System.out.println(" valueChangeListener() event:" + e);
        Map<String, String> reqParams = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();
        String idRegla = reqParams.get("idRegla");
        String valor = reqParams.get("valor");
        if ((idRegla == null) || (valor == null)) {
            return;
        }
//        System.out.println("idRegla:" + idRegla);
//        System.out.println("valor:" + valor);
        ReglaTrigger regla = getJpaController().getReglaTriggerFindByIdTrigger(idRegla);
        regla.setReglaActiva(Boolean.valueOf(valor));
        try {
            getJpaController().mergeReglaTrigger(regla);
            reglaItems = null;
            JsfUtil.addSuccessMessage("Regla " + idRegla + " actualizada");
            System.out.println("regla " + idRegla + " actualizada");
        } catch (Exception ex) {
            Logger.getLogger(ReglaTriggerController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, "Error actualizando regla " + idRegla);
        }
    }

    public void handleIdCampoChangeEvent() {
    }

    public void handleAnyChangeEvent() {
    }

    public void addNewFiltroVista() {
        Condicion filtro = new Condicion();
        if (current.getCondicionList() == null || current.getCondicionList().isEmpty()) {
            current.setCondicionList(new ArrayList<Condicion>());
        }
        Random randomGenerator = new Random();
        int n = randomGenerator.nextInt();
        if (n > 0) {
            n = n * (-1);
        }
        filtro.setIdCondicion(n);//Ugly patch to solve identifier unknown when new items are added to the datatable.
        filtro.setIdTrigger(current);
        System.out.println(filtro);
        current.getCondicionList().add(filtro);

    }

    public void removeFiltroFromVista(Condicion filtro) {
        if (current.getCondicionList() == null || current.getCondicionList().isEmpty()) {
            JsfUtil.addErrorMessage("No hay criterios en la regla!");
        }
        if (current.getCondicionList().contains(filtro)) {
            current.getCondicionList().remove(filtro);
        }
        filtro.setIdTrigger(null);
    }


    public void crearEmailTemp() {
        emailTemp = new EmailStruct();
    }

    public void setAccionParametroGrupo(Grupo grupo) {
        this.grupoTemp = grupo;
        accionTemp.setParametros(grupo.getIdGrupo());
    }

    public Grupo getAccionParametroGrupo() {
        return grupoTemp;
    }

    public void setAccionParametroArea(Area a) {
        this.areaTemp = a;
        accionTemp.setParametros(a.getIdArea());
    }

    public Area getAccionParametroArea() {
        return areaTemp;
    }

    public void setAccionParametroUsuario(Usuario usuario) {
        System.out.println("setAccionParametroUsuario " + usuario);
        this.usuarioTemp = usuario;
        accionTemp.setParametros(usuario.getIdUsuario());

    }

    public Usuario getAccionParametroUsuario() {
        return usuarioTemp;
    }

    public void setAccionParametroPrioridad(Prioridad prioridad) {
        this.prioridadTemp = prioridad;
        accionTemp.setParametros(prioridad.getIdPrioridad());

    }

    public Prioridad getAccionParametroPrioridad() {
        return prioridadTemp;
    }

    @Override
    public JPAFilterHelper getFilterHelper() {
        if (filterHelper == null) {
            filterHelper = new JPAFilterHelper(new Vista(Caso.class), emf) {
                @Override
                public JPAServiceFacade getJpaService() {
                    return getJpaController();
                }
            };
        }
        return filterHelper;
    }

//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(10) {
//                @Override
//                public int getItemsCount() {
//                    return getJpaController().count(ReglaTrigger.class).intValue();
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    System.out.println("createPageDataModel");
//                    return new ReglaTriggerDataModel(getJpaController().queryByRange(ReglaTrigger.class, getPageSize(), getPageFirstItem()));
//                }
//            };
//        }
//        return pagination;
//    }
    public boolean esAccionCambioCat() {
        return esAccion(EnumNombreAccion.CAMBIO_CAT);
    }

    public boolean esAccionAsignarArea() {
        return esAccion(EnumNombreAccion.ASIGNAR_A_AREA);
    }

    public boolean esAccionCustom() {
        return esAccion(EnumNombreAccion.CUSTOM);
    }

    public boolean esAccionAsignarAGrupo() {
        return esAccion(EnumNombreAccion.ASIGNAR_A_GRUPO);
    }
    
    public boolean esAccionRedefinirSLAFechaCompra()
    {
        return esAccion(EnumNombreAccion.DEFINIR_SLA_FECHA_COMPRA);
    }

    public boolean esAccionAsignarAUsuario() {
        return esAccion(EnumNombreAccion.ASIGNAR_A_USUARIO);
    }

    public boolean esAccionCambiarPrioridad() {
        return esAccion(EnumNombreAccion.CAMBIAR_PRIORIDAD);
    }

    public boolean esAccionEnviarEmail() {
        return esAccion(EnumNombreAccion.ENVIAR_EMAIL);
    }

    public boolean esAccionRecalcularSLA() {
        return esAccion(EnumNombreAccion.RECALCULAR_SLA);
    }

    public String prepareList() {
        recreateModel();
        return "/script/reglaTrigger/List";
    }

    public String prepareView() {
        if (current == null) {
            return "";
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/reglaTrigger/View";
    }

    public String prepareCreate() {
        current = new ReglaTrigger();
        current.setCondicionList(new ArrayList<Condicion>());
        current.setOrden(0);
        accionTemp = new Accion();
        acciones = new ArrayList<Accion>();
        selectedItemIndex = -1;
        return "/script/reglaTrigger/Create";
    }

    public String prepareCreateAccionEmail() {
        String xml = xstream.toXML(emailTemp);
        accionTemp.setParametros(xml);
        if (getAcciones() != null) {
            accionTemp.setIdTrigger(current);
            Random randomGenerator = new Random();
            int n = randomGenerator.nextInt();
            if (n > 0) {
                n = n * (-1);
            }
            accionTemp.setIdAccion(n);
            getAcciones().add(accionTemp);
        }
        System.out.println("prepareCreateAccion..." + getAcciones());
        accionTemp = new Accion();
        resetTempVars();
        JsfUtil.addSuccessMessage("Acción agregada");
        return null;
    }

    public String prepareCreateAccion() {
        if (getAcciones() != null) {
            accionTemp.setIdTrigger(current);
            Random randomGenerator = new Random();
            int n = randomGenerator.nextInt();
            if (n > 0) {
                n = n * (-1);
            }
            accionTemp.setIdAccion(n);
            getAcciones().add(accionTemp);
        }
        System.out.println("prepareCreateAccion..." + getAcciones());
        accionTemp = new Accion();
        resetTempVars();
        JsfUtil.addSuccessMessage("Acción agregada...");
        executeInClient("addAccionPopup.hide()");
        return null;
    }

    public String create() {
        try {
            for (Condicion c : current.getCondicionList()) {
                c.setIdCondicion(null);
            }
            for (Accion accion : acciones) {
                accion.setIdAccion(null);
            }

            current.setAccionList(acciones);
            getJpaController().persistReglaTrigger(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ReglaTriggerCreated"));
            reglaItems = null;
            return prepareList();
        } catch (Exception e) {
            e.printStackTrace();
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(ReglaTrigger item) {
        setSelected(item);
        return prepareEdit();
    }

    public String prepareEdit() {
        if (current == null) {
            return "";
        }
        setAcciones(current.getAccionList());
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/reglaTrigger/Edit";
    }

    public String update() {
        try {
            for (Condicion condicion : current.getCondicionList()) {
                if (condicion.getIdCondicion() < 0) {
                    condicion.setIdCondicion(null);
                }
            }

            for (Accion accion : acciones) {
                if (accion.getIdAccion() < 0) {
                    accion.setIdAccion(null);
                }
            }
            current.setAccionList(acciones);
            getJpaController().mergeReglaTrigger(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ReglaTriggerUpdated"));
            recreateModel();
            recreatePagination();
            return prepareList();
        } catch (Exception e) {
            e.printStackTrace();
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String update(ReglaTrigger regla) {
        try {
            System.out.println(regla + " regla estado:" + regla.getReglaActiva());
            getJpaController().mergeReglaTrigger(regla);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ReglaTriggerUpdated"));
            return prepareEdit();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        reglaItems = null;
        return "/script/reglaTrigger/List";
    }

    public String destroySelected() {
        if (current == null) {
            return "";
        }
        performDestroy();
//        try {
//            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        } finally {
//            recreateModel();
//        }
        recreateModel();
        reglaItems = null;
        return "/script/reglaTrigger/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/reglaTrigger/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/reglaTrigger/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeReglaTrigger(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ReglaTriggerDeleted"));
        } catch (Exception e) {
            e.printStackTrace();
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(ReglaTrigger.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (ReglaTrigger) getJpaController().queryByRange(ReglaTrigger.class, 1, selectedItemIndex).get(0);
        }
    }

    public List<ReglaTrigger> getItems() {
        if (reglaItems == null) {
            reglaItems = (List<ReglaTrigger>) getJpaController().findAll(ReglaTrigger.class, "orden");//getPagination().createPageDataModel();
        }
        return reglaItems;
    }

    /**
     * @return the acciones
     */
    public List<Accion> getAcciones() {
        return acciones;
    }

    /**
     * @param acciones the acciones to set
     */
    public void setAcciones(List<Accion> acciones) {
        this.acciones = acciones;
    }

    /**
     * @return the accionTemp
     */
    public Accion getAccionTemp() {
        return accionTemp;
    }

    /**
     * @param accionTemp the accionTemp to set
     */
    public void setAccionTemp(Accion accionTemp) {
        this.accionTemp = accionTemp;
    }

    public void resetTempVars() {
        System.out.println("resetTempVars");
        usuarioTemp = null;
        prioridadTemp = null;
        grupoTemp = null;
        emailTemp = null;
        accionTemp.setParametros(null);
        catNodeSelected = null;
    }

    /**
     * @return the emailTemp
     */
    public EmailStruct getEmailTemp() {
        return emailTemp;
    }

    /**
     * @param emailTemp the emailTemp to set
     */
    public void setEmailTemp(EmailStruct emailTemp) {
        this.emailTemp = emailTemp;
    }

    private boolean esAccion(EnumNombreAccion enumAccion) {
        if ((accionTemp == null) || (accionTemp.getIdNombreAccion() == null)) {
            return false;
        }
        return accionTemp.getIdNombreAccion().equals(enumAccion.getNombreAccion());
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
       return ReglaTriggerDataModel.class;
    }

    /**
     * @return the rows
     */
    public Integer getRows() {
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(Integer rows) {
        this.rows = rows;
    }

    /**
     * @return the actionClassNames
     */
    public Collection<String> getActionClassNames() {
        return actionClassNames.values();
    }

    

    @FacesConverter(forClass = ReglaTrigger.class, value="ReglaTriggerControllerConverter")
    public static class ReglaTriggerControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ReglaTriggerController controller = (ReglaTriggerController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "reglaTriggerController");
            return controller.getJpaController().getReglaTriggerFindByIdTrigger(getKey(value));
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(java.lang.String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ReglaTrigger) {
                ReglaTrigger o = (ReglaTrigger) object;
                return getStringKey(o.getIdTrigger());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ReglaTrigger.class.getName());
            }
        }
    }
}
class ReglaTriggerDataModel extends ListDataModel<ReglaTrigger> implements SelectableDataModel<ReglaTrigger> {

    public ReglaTriggerDataModel() {
        //nothing
    }

    public ReglaTriggerDataModel(List<ReglaTrigger> data) {
        super(data);
    }

    @Override
    public ReglaTrigger getRowData(String rowKey) {
        List<ReglaTrigger> listOfReglaTrigger = (List<ReglaTrigger>) getWrappedData();

        for (ReglaTrigger obj : listOfReglaTrigger) {
            if (obj.getIdTrigger().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(ReglaTrigger classname) {
        return classname.getIdTrigger();
    }
}
