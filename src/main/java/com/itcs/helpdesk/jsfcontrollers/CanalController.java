package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.commons.email.EmailAutoconfigClient;
import com.itcs.commons.email.EnumEmailSettingKeys;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.CanalSetting;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.quartz.DownloadEmailJob;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.MailClientFactory;
import com.itcs.jpautils.EasyCriteriaQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import org.primefaces.context.RequestContext;
import org.primefaces.model.SelectableDataModel;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

@ManagedBean(name = "canalController")
@SessionScoped
public class CanalController extends AbstractManagedBean<Canal> implements Serializable {

//    private Canal current;
//    private Canal[] selectedItems;
    private String mode;
    private String tmpEmailSuNombre;
    private String tmpEmailCorreoElectronico;
    private String tmpEmailContrasena;
    private String tmpEmailIncommingType;
    private String tmpEmailIncommingHost;
    private String tmpEmailIncommingPort;
    private String tmpEmailIncommingSsl;
    private String tmpEmailOutgoingType;
    private String tmpEmailOutgoingHost;
    private String tmpEmailOutgoingPort;
    private String tmpEmailOutgoingSsl;
    private String tmpEmailUsuario;
    private String tmpEmailInfo;
    private boolean tmpEmailFinalizeReady;
    private boolean tmpEmailFirstStepReady;

    public CanalController() {
        super(Canal.class);
    }

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(Canal.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    EasyCriteriaQuery<Canal> ecq = new EasyCriteriaQuery<Canal>(emf, Canal.class);
                    ecq.setMaxResults(getPageSize());
                    ecq.setFirstResult(getPageFirstItem());
                    ecq.orderBy("idCanal", true);
                    return new ListDataModel(ecq.next());
//                    return new ListDataModel(getJpaController().queryByRange(Canal.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public void verifyEmailAccount() {
        tmpEmailInfo = "Verificando configuración";
        RequestContext.getCurrentInstance().update(":formAddEditEmail");
        boolean result = EmailAutoconfigClient.testServerSettings(createEmailSettingsMap());
        if (result) {
            tmpEmailInfo = "Configuración OK";
            tmpEmailFinalizeReady = true;
        } else {
            tmpEmailInfo = "Error en la configuración, por favor verifique e intente nuevamente";
            tmpEmailFinalizeReady = false;
        }
        RequestContext.getCurrentInstance().update(":formAddEditEmail");
    }

    private Map<String, String> createEmailSettingsMap() {
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(EnumEmailSettingKeys.INBOUND_SERVER.getKey(), tmpEmailIncommingHost);
        settings.put(EnumEmailSettingKeys.INBOUND_PORT.getKey(), tmpEmailIncommingPort);
        settings.put(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey(), tmpEmailIncommingSsl.equals("SSL/TLS") ? "true" : "false");
        settings.put(EnumEmailSettingKeys.INBOUND_USER.getKey(), tmpEmailUsuario);
        settings.put(EnumEmailSettingKeys.INBOUND_PASS.getKey(), tmpEmailContrasena);
        settings.put(EnumEmailSettingKeys.SERVER_TYPE.getKey(), "popimap");
        settings.put(EnumEmailSettingKeys.STORE_PROTOCOL.getKey(), tmpEmailIncommingType.equals("imap") ? "imaps" : "pop3s");

        settings.put(EnumEmailSettingKeys.MAIL_SERVER_TYPE_SALIDA.getKey(), "SMTP");
        settings.put(EnumEmailSettingKeys.SMTP_SERVER.getKey(), tmpEmailOutgoingHost);
        settings.put(EnumEmailSettingKeys.SMTP_PORT.getKey(), tmpEmailOutgoingPort);
        settings.put(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey(), tmpEmailOutgoingSsl.equals("SSL/TLS") ? "true" : "false");
        settings.put(EnumEmailSettingKeys.SMTP_USER.getKey(), tmpEmailUsuario);
        settings.put(EnumEmailSettingKeys.SMTP_FROM.getKey(), tmpEmailUsuario);
        settings.put(EnumEmailSettingKeys.SMTP_FROMNAME.getKey(), tmpEmailSuNombre);
        settings.put(EnumEmailSettingKeys.SMTP_PASS.getKey(), tmpEmailContrasena);
        return settings;
    }

    public void detectAutoConfig() {
        tmpEmailInfo = "intentando autodetectar configuración";
        RequestContext.getCurrentInstance().update(":formAddEditEmail");
        if (EmailAutoconfigClient.isValidEmail(tmpEmailCorreoElectronico)) {
            tmpEmailInfo = "buscando una configuración conocida";
            RequestContext.getCurrentInstance().update(":formAddEditEmail");
            if (EmailAutoconfigClient.existsAutoconfigSettings(tmpEmailCorreoElectronico)) {
                tmpEmailInfo = "se ha encontrado una configuración conocida";
                RequestContext.getCurrentInstance().update(":formAddEditEmail");
                Map<String, String> settings;
                if (EmailAutoconfigClient.isImapAvailable(tmpEmailCorreoElectronico)) {
                    settings = EmailAutoconfigClient.getIncommingServerSettings(tmpEmailCorreoElectronico, "imap");
                    System.out.println("settings: " + settings);
                    tmpEmailIncommingType = "IMAP";
                    tmpEmailIncommingHost = settings.get(EnumEmailSettingKeys.INBOUND_SERVER.getKey());
                    tmpEmailIncommingPort = settings.get(EnumEmailSettingKeys.INBOUND_PORT.getKey());
                    tmpEmailIncommingSsl = settings.get(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey()).equals("true") ? "SSL/TLS" : "NINGUNO";
                } else if (EmailAutoconfigClient.isPop3Available(tmpEmailCorreoElectronico)) {
                    settings = EmailAutoconfigClient.getIncommingServerSettings(tmpEmailCorreoElectronico, "pop3");
                    System.out.println("settings: " + settings);
                    tmpEmailIncommingType = "POP3";
                    tmpEmailIncommingHost = settings.get(EnumEmailSettingKeys.INBOUND_SERVER.getKey());
                    tmpEmailIncommingPort = settings.get(EnumEmailSettingKeys.INBOUND_PORT.getKey());
                    tmpEmailIncommingSsl = settings.get(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey()).equals("true") ? "SSL/TLS" : "NINGUNO";
                }
                settings = EmailAutoconfigClient.getOutgoingServerSettings(tmpEmailCorreoElectronico);
                if (settings != null) {
                    tmpEmailOutgoingHost = settings.get(EnumEmailSettingKeys.SMTP_SERVER.getKey());
                    tmpEmailOutgoingPort = settings.get(EnumEmailSettingKeys.SMTP_PORT.getKey());
                    tmpEmailOutgoingSsl = settings.get(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey()).equals("true") ? "SSL/TLS" : "NINGUNO";
                    tmpEmailUsuario = tmpEmailCorreoElectronico;
                }
            } else {
                tmpEmailInfo = "No se ha encontrado una configuración conocida";
                RequestContext.getCurrentInstance().update(":formAddEditEmail");
                tmpEmailUsuario = null;
                tmpEmailIncommingHost = null;
                tmpEmailIncommingPort = null;
                tmpEmailIncommingSsl = null;
                tmpEmailOutgoingHost = null;
                tmpEmailOutgoingPort = null;
                tmpEmailOutgoingSsl = null;
            }
        } else {
            tmpEmailInfo = "Correo no es válido";
            RequestContext.getCurrentInstance().update(":formAddEditEmail");
            System.out.println(tmpEmailCorreoElectronico + " mail not valid!!");
            return;
        }
        tmpEmailFirstStepReady = true;
        RequestContext.getCurrentInstance().update(":formAddEditEmail");
//        refreshPage();
    }
    
    public SelectItem[] getEmailsAvailableSelectOne() {
        EasyCriteriaQuery<Canal> ecq = new EasyCriteriaQuery<Canal>(emf, Canal.class);
        ecq.addEqualPredicate("idTipoCanal", EnumTipoCanal.EMAIL.getTipoCanal());
        return JsfUtil.getSelectItems(ecq.getAllResultList(), true);
    }

    public String prepareList() {
        recreateModel();
        return "/script/canal/List";
    }

    public void prepareView(Canal c) {
        current = c;
    }

    public void prepareCreateEmail() {
        tmpEmailSuNombre = null;
        tmpEmailCorreoElectronico = null;
        tmpEmailContrasena = null;
        tmpEmailIncommingType = null;
        tmpEmailIncommingHost = null;
        tmpEmailIncommingPort = null;
        tmpEmailIncommingSsl = null;
        tmpEmailOutgoingType = null;
        tmpEmailOutgoingHost = null;
        tmpEmailOutgoingPort = null;
        tmpEmailOutgoingSsl = null;
        tmpEmailUsuario = null;
        tmpEmailInfo = null;
        tmpEmailFinalizeReady = false;
        tmpEmailFirstStepReady = false;
        prepareCreate();
    }
    
    public void prepareCreateChat() {
        current = new Canal();
        current.setIdTipoCanal(EnumTipoCanal.CHAT.getTipoCanal());
        mode = "Create";
    }
    
    public void prepareCreateManual() {
        current = new Canal();
        current.setIdTipoCanal(EnumTipoCanal.MANUAL.getTipoCanal());
        mode = "Create";
    }
    
    public void prepareCreateFormulario() {
        current = new Canal();
        current.setIdTipoCanal(EnumTipoCanal.FORMULARIO.getTipoCanal());
        mode = "Create";
    }

    public void prepareCreate() {
        current = new Canal();
        mode = "Create";
    }

    public void prepareEdit(Canal c) {
        current = c;
        mode = "Edit";
    }

    public void prepareEditEmail(Canal c) {
        current = c;
        tmpEmailSuNombre = current.getSetting(EnumEmailSettingKeys.SMTP_FROMNAME.getKey());
        tmpEmailCorreoElectronico = current.getSetting(EnumEmailSettingKeys.SMTP_FROM.getKey());
        tmpEmailContrasena = current.getSetting(EnumEmailSettingKeys.SMTP_PASS.getKey());
        tmpEmailIncommingType = current.getSetting(EnumEmailSettingKeys.STORE_PROTOCOL.getKey()).equals("imaps") ? "imap" : "pop3";
        tmpEmailIncommingHost = current.getSetting(EnumEmailSettingKeys.INBOUND_SERVER.getKey());
        tmpEmailIncommingPort = current.getSetting(EnumEmailSettingKeys.INBOUND_PORT.getKey());
        tmpEmailIncommingSsl = current.getSetting(EnumEmailSettingKeys.INBOUND_SSL_ENABLED.getKey()).equals("true") ? "SSL/TLS" : "NINGUNO";
        //tmpEmailOutgoingType = current.getSetting(EnumEmailSettingKeys.SMTP_FROMNAME.getKey());
        tmpEmailOutgoingHost = current.getSetting(EnumEmailSettingKeys.SMTP_SERVER.getKey());
        tmpEmailOutgoingPort = current.getSetting(EnumEmailSettingKeys.SMTP_PORT.getKey());
        tmpEmailOutgoingSsl = current.getSetting(EnumEmailSettingKeys.SMTP_SSL_ENABLED.getKey()).equals("true") ? "SSL/TLS" : "NINGUNO";
        tmpEmailUsuario = current.getSetting(EnumEmailSettingKeys.SMTP_FROM.getKey());
        tmpEmailInfo = null;
        tmpEmailFinalizeReady = false;
        tmpEmailFirstStepReady = true;
        mode = "Edit";
    }

    public void createCanalEmail() {
        try {
            if (mode.equals("Edit")) {
                current.setNombre(tmpEmailCorreoElectronico);
                current.setDescripcion("Canal email, para la cuenta " + tmpEmailCorreoElectronico);
                getJpaController().mergeCanal(current);
                current.setCanalSettingList(new LinkedList<CanalSetting>());
                Map<String, String> settingsMap = createEmailSettingsMap();
                Iterator it = settingsMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    CanalSetting canalSetting = new CanalSetting(current, pairs.getKey().toString(), pairs.getValue().toString(), "");
                    getJpaController().merge(canalSetting);
                    current.getCanalSettingList().add(canalSetting);
                    it.remove(); // avoids a ConcurrentModificationException
                }
                getJpaController().mergeCanal(current);
            } else {
                current.setIdCanal(tmpEmailCorreoElectronico);
                current.setIdTipoCanal(EnumTipoCanal.EMAIL.getTipoCanal());
                current.setEnabled(true);
                current.setNombre(tmpEmailCorreoElectronico);
                current.setDescripcion("Canal email, para la cuenta " + tmpEmailCorreoElectronico);
                getJpaController().persistCanal(current);
                current.setCanalSettingList(new LinkedList<CanalSetting>());
                Map<String, String> settingsMap = createEmailSettingsMap();
                Iterator it = settingsMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    CanalSetting canalSetting = new CanalSetting(current, pairs.getKey().toString(), pairs.getValue().toString(), "");

                    getJpaController().persist(canalSetting);
                    current.getCanalSettingList().add(canalSetting);
                    it.remove(); // avoids a ConcurrentModificationException
                }
                getJpaController().mergeCanal(current);
                //hay que agregar al agendamiento este nuevo canal email
                MailClientFactory.createInstance(current);
                HelpDeskScheluder.scheduleRevisarCorreo(current.getIdCanal(), 300);
            }
            items = null;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void update(Canal canal) {
        try {
            System.out.println(canal + " canal estado:" + canal.getEnabled());
            getJpaController().mergeCanal(canal);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CanalUpdated"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void create() {
        try {
            current.setEnabled(true);
            getJpaController().persistCanal(current);
            executeInClient("addEditDialog.hide()");
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CanalCreated"));
            recreateModel();
//            return prepareCreate();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
        }
    }

    public void update() {
        try {
            getJpaController().mergeCanal(current);
            executeInClient("addEditDialog.hide()");
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CanalUpdated"));
//            return "View";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
//            return null;
        }
    }

    public void destroy(Canal c) {
        if (c != null) {
            current = c;
            performDestroy();
            recreateModel();
        }
    }

    public void destroySelected() {
        if (current != null) {
            try {
                final String downloadEmailJobId = DownloadEmailJob.formatJobId(current.getIdCanal());
                final JobKey jobKey = JobKey.jobKey(downloadEmailJobId, HelpDeskScheluder.GRUPO_CORREO);
                HelpDeskScheluder.unschedule(jobKey);
            } catch (SchedulerException ex) {
                Logger.getLogger(CanalController.class.getName()).log(Level.SEVERE, null, ex);
            }
            performDestroy();
            recreateModel();
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeCanal(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CanalDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<Canal> listOfCanal = new ArrayList<Canal>();
        while (iter.hasNext()) {
            listOfCanal.add((Canal) iter.next());
        }

        return new CanalDataModel(listOfCanal);
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the tmpEmailSuNombre
     */
    public String getTmpEmailSuNombre() {
        return tmpEmailSuNombre;
    }

    /**
     * @param tmpEmailSuNombre the tmpEmailSuNombre to set
     */
    public void setTmpEmailSuNombre(String tmpEmailSuNombre) {
        this.tmpEmailSuNombre = tmpEmailSuNombre;
    }

    /**
     * @return the tmpEmailCorreoElectronico
     */
    public String getTmpEmailCorreoElectronico() {
        return tmpEmailCorreoElectronico;
    }

    /**
     * @param tmpEmailCorreoElectronico the tmpEmailCorreoElectronico to set
     */
    public void setTmpEmailCorreoElectronico(String tmpEmailCorreoElectronico) {
        System.out.println("setTmpEmailCorreoElectronico:" + tmpEmailCorreoElectronico);
        this.tmpEmailCorreoElectronico = tmpEmailCorreoElectronico;
    }

    /**
     * @return the tmpEmailContrasena
     */
    public String getTmpEmailContrasena() {
        return tmpEmailContrasena;
    }

    /**
     * @param tmpEmailContrasena the tmpEmailContrasena to set
     */
    public void setTmpEmailContrasena(String tmpEmailContrasena) {
        System.out.println("setTmpEmailContrasena:" + tmpEmailContrasena);
        this.tmpEmailContrasena = tmpEmailContrasena;
    }

    /**
     * @return the tmpEmailIncommingType
     */
    public String getTmpEmailIncommingType() {
        return tmpEmailIncommingType;
    }

    /**
     * @param tmpEmailIncommingType the tmpEmailIncommingType to set
     */
    public void setTmpEmailIncommingType(String tmpEmailIncommingType) {
        this.tmpEmailIncommingType = tmpEmailIncommingType;
    }

    /**
     * @return the tmpEmailIncommingHost
     */
    public String getTmpEmailIncommingHost() {
        return tmpEmailIncommingHost;
    }

    /**
     * @param tmpEmailIncommingHost the tmpEmailIncommingHost to set
     */
    public void setTmpEmailIncommingHost(String tmpEmailIncommingHost) {
        this.tmpEmailIncommingHost = tmpEmailIncommingHost;
    }

    /**
     * @return the tmpEmailIncommingPort
     */
    public String getTmpEmailIncommingPort() {
        return tmpEmailIncommingPort;
    }

    /**
     * @param tmpEmailIncommingPort the tmpEmailIncommingPort to set
     */
    public void setTmpEmailIncommingPort(String tmpEmailIncommingPort) {
        this.tmpEmailIncommingPort = tmpEmailIncommingPort;
    }

    /**
     * @return the tmpEmailIncommingSsl
     */
    public String getTmpEmailIncommingSsl() {
        return tmpEmailIncommingSsl;
    }

    /**
     * @param tmpEmailIncommingSsl the tmpEmailIncommingSsl to set
     */
    public void setTmpEmailIncommingSsl(String tmpEmailIncommingSsl) {
        this.tmpEmailIncommingSsl = tmpEmailIncommingSsl;
    }

    /**
     * @return the tmpEmailOutgoingType
     */
    public String getTmpEmailOutgoingType() {
        return tmpEmailOutgoingType;
    }

    /**
     * @param tmpEmailOutgoingType the tmpEmailOutgoingType to set
     */
    public void setTmpEmailOutgoingType(String tmpEmailOutgoingType) {
        this.tmpEmailOutgoingType = tmpEmailOutgoingType;
    }

    /**
     * @return the tmpEmailOutgoingHost
     */
    public String getTmpEmailOutgoingHost() {
        return tmpEmailOutgoingHost;
    }

    /**
     * @param tmpEmailOutgoingHost the tmpEmailOutgoingHost to set
     */
    public void setTmpEmailOutgoingHost(String tmpEmailOutgoingHost) {
        this.tmpEmailOutgoingHost = tmpEmailOutgoingHost;
    }

    /**
     * @return the tmpEmailOutgoingPort
     */
    public String getTmpEmailOutgoingPort() {
        return tmpEmailOutgoingPort;
    }

    /**
     * @param tmpEmailOutgoingPort the tmpEmailOutgoingPort to set
     */
    public void setTmpEmailOutgoingPort(String tmpEmailOutgoingPort) {
        this.tmpEmailOutgoingPort = tmpEmailOutgoingPort;
    }

    /**
     * @return the tmpEmailOutgoingSsl
     */
    public String getTmpEmailOutgoingSsl() {
        return tmpEmailOutgoingSsl;
    }

    /**
     * @param tmpEmailOutgoingSsl the tmpEmailOutgoingSsl to set
     */
    public void setTmpEmailOutgoingSsl(String tmpEmailOutgoingSsl) {
        this.tmpEmailOutgoingSsl = tmpEmailOutgoingSsl;
    }

    /**
     * @return the tmpEmailUsuario
     */
    public String getTmpEmailUsuario() {
        return tmpEmailUsuario;
    }

    /**
     * @param tmpEmailUsuario the tmpEmailUsuario to set
     */
    public void setTmpEmailUsuario(String tmpEmailUsuario) {
        this.tmpEmailUsuario = tmpEmailUsuario;
    }

    /**
     * @return the tmpEmailInfo
     */
    public String getTmpEmailInfo() {
        return tmpEmailInfo;
    }

    /**
     * @param tmpEmailInfo the tmpEmailInfo to set
     */
    public void setTmpEmailInfo(String tmpEmailInfo) {
        this.tmpEmailInfo = tmpEmailInfo;
    }

    /**
     * @return the tmpEmailFinalizeReady
     */
    public boolean isTmpEmailFinalizeReady() {
        return tmpEmailFinalizeReady;
    }

    /**
     * @param tmpEmailFinalizeReady the tmpEmailFinalizeReady to set
     */
    public void setTmpEmailFinalizeReady(boolean tmpEmailFinalizeReady) {
        this.tmpEmailFinalizeReady = tmpEmailFinalizeReady;
    }

    /**
     * @return the tmpEmailFirstStepReady
     */
    public boolean isTmpEmailFirstStepReady() {
        return tmpEmailFirstStepReady;
    }

    /**
     * @param tmpEmailFirstStepReady the tmpEmailFirstStepReady to set
     */
    public void setTmpEmailFirstStepReady(boolean tmpEmailFirstStepReady) {
        this.tmpEmailFirstStepReady = tmpEmailFirstStepReady;
    }

    @FacesConverter(forClass = Canal.class)
    public static class CanalControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {

            if (value == null || value.length() == 0) {
                return null;
            }
            CanalController controller = (CanalController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "canalController");
            return controller.getJpaController().getCanalFindByIdCanal(getKey(value));
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
            if (object instanceof Canal) {
                Canal o = (Canal) object;
                return getStringKey(o.getIdCanal());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CanalController.class.getName());
            }
        }
    }
}

class CanalDataModel extends ListDataModel<Canal> implements SelectableDataModel<Canal> {

    public CanalDataModel() {
        //nothing
    }

    public CanalDataModel(List<Canal> data) {
        super(data);
    }

    @Override
    public Canal getRowData(String rowKey) {
        List<Canal> listOfCanal = (List<Canal>) getWrappedData();

        for (Canal obj : listOfCanal) {
            if (obj.getIdCanal().equals(rowKey)) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(Canal classname) {
        return classname.getIdCanal();
    }
}
