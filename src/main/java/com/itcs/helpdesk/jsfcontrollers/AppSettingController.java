package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.AppSetting;
import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entityenums.EnumFieldType;
import com.itcs.helpdesk.persistence.entityenums.EnumSettingsBase;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.util.ApplicationConfig;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@ManagedBean(name = "appSettingController")
@SessionScoped
public class AppSettingController extends AbstractManagedBean<AppSetting> implements Serializable {

    private int selectedItemIndex;
    //------------------------------------
//    private List<AppSetting> settings;
    private transient UploadedFile logoUploadFile = null;
//    private StreamedContent currentLogoStreamedContent;
    private transient StreamedContent newLogoStreamedContent;
//    private CroppedImage croppedImage;

    public AppSettingController() {
        super(AppSetting.class);
    }

    @Override
    public OrderBy getDefaultOrderBy() {
        return new OrderBy("orderView");
    }
    
    

//    @PostConstruct
//    protected void initialize() {
//
//        try {
//            this.settings = (List<AppSetting>) getJpaController().findAll(AppSetting.class, new String[]{"grupo", "orderView"});
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public void handleFileUpload() {
        handleFileUpload(null);
    }

    public void handleFileUpload(FileUploadEvent event) {

//        String email_regexp = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";
//        Pattern p = Pattern.compile(email_regexp);
        if (event != null) {
            logoUploadFile = event.getFile();
        }

        if (logoUploadFile != null) {
            if (logoUploadFile.getContentType().startsWith("image")) {
                try {
                    final String contentType = "image/png";
                    final String format = "png";

                    BufferedImage originalImage = ImageIO.read(logoUploadFile.getInputstream());
                    BufferedImage resizedImage = Scalr.resize(originalImage, ApplicationConfig.getCompanyLogoSize());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resizedImage, format, baos);
                    baos.flush();
                    byte[] imageInByte = baos.toByteArray();
                    baos.close();

                    this.newLogoStreamedContent = new DefaultStreamedContent(new ByteArrayInputStream(imageInByte), contentType);

                    addMessage(FacesMessage.SEVERITY_INFO, "Archivo recibido exitosamente. tamaño: " + imageInByte.length);

                } catch (Exception ex) {
                    Logger.getLogger(AppSettingController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JsfUtil.addErrorMessage("El archivo enviado debe ser una imagen. Tipo " + logoUploadFile.getContentType() + " no esta soportado.");
            }

        }
        logoUploadFile = null;
//        JsfUtil.addSuccessMessage("upload finished.");
    }

    public String submitForm() {
        FacesMessage.Severity sev = FacesContext.getCurrentInstance().getMaximumSeverity();
        boolean hasErrors = (sev != null && (FacesMessage.SEVERITY_ERROR.compareTo(sev) >= 0));

        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.addCallbackParam("isValid", !hasErrors);

        for (AppSetting appSetting : (List<AppSetting>)items.getWrappedData()) {
            try {
                getJpaController().getAppSettingJpaController().edit(appSetting);
                ApplicationConfig.getInstance().getConfiguration().put(appSetting.getSettingKey(), appSetting.getSettingValue());
            } catch (Exception ex) {
                Logger.getLogger(AppSettingController.class.getName()).log(Level.SEVERE, null, ex);
                addMessage(FacesMessage.SEVERITY_FATAL, "No se pudo actualizar la propiedad del sistema." + appSetting);
                hasErrors = true;
            }
        }

        if (logoUploadFile != null) {
            if (logoUploadFile.getContentType().startsWith("image")) {
                try {
                    final String contentType = "image/png";
                    final String format = "png";

                    BufferedImage originalImage = ImageIO.read(logoUploadFile.getInputstream());
                    BufferedImage resizedImage = Scalr.resize(originalImage, ApplicationConfig.getCompanyLogoSize());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resizedImage, format, baos);
                    baos.flush();
                    byte[] imageInByte = baos.toByteArray();
                    baos.close();

                    this.newLogoStreamedContent = new DefaultStreamedContent(new ByteArrayInputStream(imageInByte), contentType);

                    try {
                        getJpaController().getArchivoJpaController().destroy(0L);
                        Archivo archivo = new Archivo();
                        archivo.setIdAttachment(0L);
                        archivo.setArchivo(imageInByte);
                        archivo.setContentType(contentType);
                        archivo.setFormat(format);
                        getJpaController().persistArchivo(archivo);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    logoUploadFile = null;

                    addMessage(FacesMessage.SEVERITY_INFO, "Archivo recibido exitosamente. tamaño: " + imageInByte.length);

                } catch (Exception ex) {
                    Logger.getLogger(AppSettingController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (logoUploadFile.getSize() > 0) {
                    JsfUtil.addErrorMessage("El archivo enviado debe ser una imagen. Tipo " + logoUploadFile.getContentType() + " no esta soportado. " + logoUploadFile.getSize());
                }
            }

        }

        if (!hasErrors) {
            addMessage(FacesMessage.SEVERITY_INFO, "Configuración guardada exitosamente.");

        }

        return null;
    }

    @Override
    protected String getEditPage() {
         return "/script/appSetting/Edit";
    }

    @Override
    protected String getViewPage() {
        return "/script/appSetting/View";
    }



    public String goToConfigPage() {
        return "/script/appSetting/config";
    }

    @Override
    protected String getListPage() {
        return "/script/appSetting/config";
    }

    public String prepareView() {
        current = (AppSetting) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return getViewPage();
        
    }

    public String prepareCreate() {
        current = new AppSetting();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().getAppSettingJpaController().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AppSettingCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (AppSetting) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return getEditPage();
        
    }

    public String update() {
        try {
            getJpaController().getAppSettingJpaController().edit(current);
            ApplicationConfig.getInstance().getConfiguration().put(current.getSettingKey(), current.getSettingValue());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AppSettingUpdated"));
            return getViewPage();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (AppSetting) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return getListPage();
        
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return getViewPage();
        } else {
            // all items were removed - go back to list
            recreateModel();
            return getListPage();
        }
    }

    private void performDestroy() {
        try {
            getJpaController().getAppSettingJpaController().destroy(current.getSettingKey());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AppSettingDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(AppSetting.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (AppSetting) getJpaController().queryByRange(AppSetting.class, selectedItemIndex + 1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getAppSettingJpaController().findAppSettingEntities(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getAppSettingJpaController().findAppSettingEntities(), true);
//    }
    public SelectItem[] getSettingsItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(EnumSettingsBase.values(), true);
    }

    public SelectItem[] getFieldTypeItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(EnumFieldType.values(), true);
    }

    /**
     * @return the currentConfiguration
     */
    public String getCurrentConfiguration() {
        return ApplicationConfig.getInstance().getConfiguration().toString();
    }

    /**
     * @return the logoUploadFile
     */
    public UploadedFile getLogoUploadFile() {
        return logoUploadFile;
    }

    /**
     * @param logoUploadFile0 the logoUploadFile to set
     */
    public void setLogoUploadFile(UploadedFile logoUploadFile0) {
        this.logoUploadFile = logoUploadFile0;
    }

//    /**
//     * @return the currentLogoStreamedContent
//     */
//    public StreamedContent getCurrentLogoStreamedContent() {
//        return currentLogoStreamedContent;
//    }
//
//    /**
//     * @param currentLogoStreamedContent the currentLogoStreamedContent to set
//     */
//    public void setCurrentLogoStreamedContent(StreamedContent currentLogoStreamedContent) {
//        this.currentLogoStreamedContent = currentLogoStreamedContent;
//    }
    /**
     * @return the newLogoStreamedContent
     */
    public StreamedContent getNewLogoStreamedContent() {
        return newLogoStreamedContent;
    }

    /**
     * @param newLogoStreamedContent the newLogoStreamedContent to set
     */
    public void setNewLogoStreamedContent(StreamedContent newLogoStreamedContent) {
        this.newLogoStreamedContent = newLogoStreamedContent;
    }

//    /**
//     * @return the settings
//     */
//    public List<AppSetting> getSettings() {
//        initialize();
//        return settings;
//    }
//
//    /**
//     * @param settings the settings to set
//     */
//    public void setSettings(List<AppSetting> settings) {
//        this.settings = settings;
//    }

    @Override
    public Class getDataModelImplementationClass() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return AppSettingDataModel.class;
    }

    @FacesConverter(forClass = AppSetting.class)
    public static class AppSettingControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AppSettingController controller = (AppSettingController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "appSettingController");
            return controller.getJpaController().getAppSettingJpaController().findAppSetting(getKey(value));
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
            if (object instanceof AppSetting) {
                AppSetting o = (AppSetting) object;
                return getStringKey(o.getSettingKey());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + AppSetting.class.getName());
            }
        }
    }
}

class AppSettingDataModel extends ListDataModel<AppSetting> implements SelectableDataModel<AppSetting>, Serializable {

    public AppSettingDataModel() {
        //nothing
    }

    public AppSettingDataModel(List<AppSetting> data) {
        super(data);
    }

    @Override
    public AppSetting getRowData(String rowKey) {
        List<AppSetting> list = (List<AppSetting>) getWrappedData();

        if (list != null) {
            for (AppSetting obj : list) {
                if (obj.getSettingKey()!= null) {
                    if (obj.getSettingKey().equals(rowKey)) {
                        return obj;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(AppSetting classname) {
        return classname.getSettingKey();
    }
}
