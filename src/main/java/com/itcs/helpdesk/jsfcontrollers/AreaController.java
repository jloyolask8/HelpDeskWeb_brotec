package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Categoria;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.quartz.DownloadEmailJob;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;
import org.quartz.JobKey;
import org.quartz.SchedulerException;


@ManagedBean(name = "areaController")
@SessionScoped
public class AreaController extends AbstractManagedBean<Area> implements Serializable {

 
//    private Area current;
//    private Area[] selectedItems;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    private long selectedItemIndex;
    private transient TreeNode root;
    private int editActiveIndex;
    private int editActiveIndex2;
    private boolean showStoreProtocol = true;
    private boolean sendMailExchange = false;
    private boolean sendMailExchangeInbound = false;
    private String sslOrTls = "SSL";

    public AreaController() {
        super(Area.class);
    }

    public void onTabChange(){
        
    }
//    public Area getSelected() {
//        if (current == null) {
//            current = new Area();
//            selectedItemIndex = -1;
//        }
//        return current;
//    }
//
//    public void setSelected(Area a) {
//        current = a;
//    }

    public void handleUseJndiChange() {
    }

    public void handleSimpleChange() {
    }

//    public void handleSmtpSslOrTlsChange() {
//        if (getSslOrTls().equalsIgnoreCase("SSL")) {
//
//            getSelected().setMailSmtpSslEnable(true);
//            getSelected().setMailTransportTls(false);
//
//        } else {
//            getSelected().setMailSmtpSslEnable(false);
//            getSelected().setMailTransportTls(true);
//        }
//    }
//
//    public void handleEmailServerTypeSalidaChange() {
//        if (getSelected().getMailServerTypeSalida().equalsIgnoreCase(ApplicationConfig.EnumMailServerType.SMTP.getValue())) {
//
//            this.setSendMailExchange(false);
//
//        } else if (getSelected().getMailServerTypeSalida().equalsIgnoreCase(ApplicationConfig.EnumMailServerType.EXCHANGE.getValue())) {
//            this.setSendMailExchange(true);
//        }
//    }
//
//    public void handleEmailServerTypeChange() {
//        if (getSelected().getMailServerType().equalsIgnoreCase(ApplicationConfig.EnumMailServerType.POPIMAP.getValue())) {
//            this.setSendMailExchangeInbound(false);
//            this.setShowStoreProtocol(true);
//
//        } else if (getSelected().getMailServerType().equalsIgnoreCase(ApplicationConfig.EnumMailServerType.EXCHANGE.getValue())) {
//            this.setSendMailExchangeInbound(true);
//            this.setShowStoreProtocol(false);
//        }
//    }

    public void handleMailStoreProtocolChange() {
    }

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(Area.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(Area.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "/script/area/List";
    }

    public String prepareView(String idArea) {
        current = getJpaController().getAreaFindByIdArea(idArea);
        editActiveIndex = 0;
        return "/script/area/View";
    }

    public String prepareCreate() {
        current = new Area();
        selectedItemIndex = -1;
        return "/script/area/Create";
    }

    public String create() {
        try {
            current.setEditable(true);
            getJpaController().persistArea(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AreaCreated"));
            return prepareEdit(current.getIdArea());
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(String idArea) {
        current = getJpaController().getAreaFindByIdArea(idArea);
        editActiveIndex = 0;
        return "/script/area/Edit";
    }

//    public String prepareEdit() {
//
//        if (getSelectedItems().length != 1) {
//            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
//            return "";
//        } else {
//            current = getSelectedItems()[0];
//        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        editActiveIndex = 0;
//        return "Edit";
//    }
    
     public String update() {
        try {
//            handleSmtpSslOrTlsChange();
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AreaUpdated"));

//            if (current.getEmailEnabled() != null && current.getEmailEnabled()) {
//
//                try {
//                    //Email Enabled is false by default, so if its not configured yet, we schedule nothing, this means when we change the config to true, we must re-schedule.
//                    MailClientFactory.createInstance(current);
//                    HelpDeskScheluder.scheduleRevisarCorreo(current.getIdArea(), current.getEmailFrecuencia());
//
//                } catch (SchedulerException ex) {
//                    JsfUtil.addErrorMessage(ex, "No se pudo inicializar La revision de correo del area " + current.getIdArea());
//                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se pudo inicializar La revision de correo del area " + current.getIdArea(), ex);
//                }
//            } else 
            {
                try {
                    //disable Job revisar correo!
                    final String downloadEmailJobId = DownloadEmailJob.formatJobId(current.getIdArea());
                    final JobKey jobKey = JobKey.jobKey(downloadEmailJobId, HelpDeskScheluder.GRUPO_CORREO);
                    HelpDeskScheluder.unschedule(jobKey);
                } catch (SchedulerException ex) {
                    JsfUtil.addWarningMessage("Error del sistema, No se pudo desabilitar La revision de correo del area " + current.getIdArea());
                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se pudo inicializar La revision de correo del area " + current.getIdArea(), ex);
                }
            }

            return "/script/area/List";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "update", e);
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }
     
//    public String update() {
//        try {
//            getJpaController().mergeArea(current);
//            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AreaUpdated"));
//
//            if (current.getEmailEnabled() != null && current.getEmailEnabled()) {
//                try {
//                    HelpDeskScheluder.unscheduleTask("revisarCorreoArea" + current.getIdArea(), HelpDeskScheluder.GRUPO_CORREO);
//                    //Email Enabled is false by default, so if its not configured yet, we schedule nothing, this means when we change the config to true, we must re-schedule.
//                    AutomaticMailExecutor mailExec = new AutomaticMailExecutor(current, utx, emf);
//                    mailExec.agendarRevisarCorreo();
//                } catch (FileNotFoundException ex) {
//                    JsfUtil.addErrorMessage(ex, "No se pudo inicializar La revision de correo del area " + current.getIdArea());
//                    Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "No se pudo inicializar La revision de correo del area " + current.getIdArea(), ex);
//                } catch (IOException ex) {
//                    JsfUtil.addErrorMessage(ex, "No se pudo inicializar La revision de correo del area " + current.getIdArea());
//                    Logger.getLogger(AppStarter.class.getName()).log(Level.SEVERE, "No se pudo inicializar La revision de correo del area " + current.getIdArea(), ex);
//                }
//            } else {
//                //disable Job revisar correo!
//                HelpDeskScheluder.unscheduleTask("revisarCorreoArea" + current.getIdArea(), HelpDeskScheluder.GRUPO_CORREO);
//            }
//
//            return "/script/area/View";
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.createLogger(this.getClass().getName()).logSevere(e);
//            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
//        }
//    }

    public String destroy() {
        if (current == null) {
            return "";
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "/script/area/List";
    }

    public void destroySelected(ActionEvent actionEvent) {
        destroySelected();
    }

    public String destroySelected() {
        performDestroy();
        recreateModel();
        return "/script/area/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/area/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/area/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeArea(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AreaDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        long count = getJpaController().count(Area.class);
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Area) getJpaController().queryByRange(Area.class, 1, (int) selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<Area> listOfArea = new ArrayList<Area>();
        while (iter.hasNext()) {
            listOfArea.add((Area) iter.next());
        }

        return new AreaDataModel(listOfArea);
    }

//    /**
//     * @return the selectedItems
//     */
//    public Area[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Area[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }

//    private void recreateModel() {
//        items = null;
//    }
//
//    public String next() {
//        getPagination().nextPage();
//        recreateModel();
//        return "/script/area/List";
//    }
//
//    public String previous() {
//        getPagination().previousPage();
//        recreateModel();
//        return "/script/area/List";
//    }
//
//    public String last() {
//        getPagination().lastPage();
//        recreateModel();
//        return "/script/area/List";
//    }
//
//    public String first() {
//        getPagination().firstPage();
//        recreateModel();
//        return "/script/area/List";
//    }

//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getAreaFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getAreaFindAll(), true);
//    }

    /**
     * @return the root
     */
    public TreeNode getRoot() {

        root = new DefaultTreeNode("Áreas", null);
//        TreeNode areasNode = new DefaultTreeNode("Areas", root); 



        List<Area> areas = getJpaController().getAreaFindAll();
        for (Area area : areas) {
            TreeNode areaNode = new DefaultTreeNode("areas", area, root);
            TreeNode gruposNode = new DefaultTreeNode("Grupos", areaNode);
            TreeNode catsNode = new DefaultTreeNode("Categorías", areaNode);
           
            
            for (Grupo grupo : area.getGrupoList()) {
                TreeNode grupoNode = new DefaultTreeNode("grupos", grupo, gruposNode);
//                 TreeNode agentsNode = new DefaultTreeNode("Agentes", grupoNode);
                for (Usuario usuario : grupo.getUsuarioList()) {
                    TreeNode agentNode = new DefaultTreeNode("agentes", usuario, grupoNode);
                }
            }
            
            for (Categoria cat : area.getCategoriaList()) {
                TreeNode catNode = new DefaultTreeNode("categorias", cat, catsNode);
            }
        }

        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    /**
     * @return the editActiveIndex
     */
    public int getEditActiveIndex() {
        return editActiveIndex;
    }

    /**
     * @param editActiveIndex the editActiveIndex to set
     */
    public void setEditActiveIndex(int editActiveIndex) {
        this.editActiveIndex = editActiveIndex;
    }

    /**
     * @return the showStoreProtocol
     */
    public boolean isShowStoreProtocol() {
        return showStoreProtocol;
    }

    /**
     * @param showStoreProtocol the showStoreProtocol to set
     */
    public void setShowStoreProtocol(boolean showStoreProtocol) {
        this.showStoreProtocol = showStoreProtocol;
    }

    /**
     * @return the editActiveIndex2
     */
    public int getEditActiveIndex2() {
        return editActiveIndex2;
    }

    /**
     * @param editActiveIndex2 the editActiveIndex2 to set
     */
    public void setEditActiveIndex2(int editActiveIndex2) {
        this.editActiveIndex2 = editActiveIndex2;
    }

    /**
     * @return the sendMailExchange
     */
    public boolean isSendMailExchange() {
        return sendMailExchange;
    }

    /**
     * @param sendMailExchange the sendMailExchange to set
     */
    public void setSendMailExchange(boolean sendMailExchange) {
        this.sendMailExchange = sendMailExchange;
    }

    /**
     * @return the sendMailExchangeInbound
     */
    public boolean isSendMailExchangeInbound() {
        return sendMailExchangeInbound;
    }

    /**
     * @param sendMailExchangeInbound the sendMailExchangeInbound to set
     */
    public void setSendMailExchangeInbound(boolean sendMailExchangeInbound) {
        this.sendMailExchangeInbound = sendMailExchangeInbound;
    }

    /**
     * @return the sslOrTls
     */
    public String getSslOrTls() {
        return sslOrTls;
    }

    /**
     * @param sslOrTls the sslOrTls to set
     */
    public void setSslOrTls(String sslOrTls) {
        this.sslOrTls = sslOrTls;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FacesConverter(forClass = Area.class)
    public static class AreaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
            return controller.getJpaController().getAreaFindByIdArea(getKey(value));
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getIdArea());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Area.class.getName());
            }
        }
    }
}
class AreaDataModel extends ListDataModel<Area> implements SelectableDataModel<Area> {

    public AreaDataModel() {
        //nothing
    }

    public AreaDataModel(List<Area> data) {
        super(data);
    }

    @Override
    public Area getRowData(String rowKey) {
        List<Area> listOfArea = (List<Area>) getWrappedData();

        for (Area obj : listOfArea) {
            if (obj.getIdArea().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Area classname) {
        return classname.getIdArea();
    }
}