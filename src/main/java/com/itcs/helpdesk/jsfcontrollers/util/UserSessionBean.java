/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webapputils.Theme;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Jonathan
 */
@ManagedBean(name = "UserSessionBean")
@SessionScoped
public class UserSessionBean extends AbstractManagedBean<Usuario> implements Serializable {

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    private String DEFAULT_THEME = "itcs-theme2";
    private Usuario sessionUser;
    private String channel;
    private int activeIndexOfMyAccount = 0;
    private Map<String, String> themes;
    private List<Theme> advancedThemes;
    //Customer
    private EmailCliente emailCliente = null;//session del cliente!
    private boolean runningEmbedded = false;

    public UserSessionBean() {
        super(Usuario.class);
    }

    public boolean isValidatedSession() {
        if (this.getCurrent() != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValidatedCustomerSession() {
        if (this.getEmailCliente() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("This operation is Not supported!.");
    }

    public Usuario getCurrent() {
        return sessionUser;
    }

//    public void nuevoCasoAssigned(@Observes(notifyObserver = Reception.IF_EXISTS) BackendEvent event) {
//        if (applicationBean != null) {
//            if (event.getEventType() == BackendEvent.TYPE_ASSIGNED_CASO) {
//                //its me!
//                applicationBean.sendFacesMessageNotification(sessionUser.getIdUsuario(), "El caso #" + event.getIdCaso() + " ha sido asignado a ud. por " + event.getAssigner());
//            }
//        }
//    }
//
//    public void existingCasoModified(@Observes(notifyObserver = Reception.IF_EXISTS) BackendEvent event) {
//        if (applicationBean != null) {
//            if (event.getEventType() == BackendEvent.TYPE_EDITED_CASO) {
//                //its mine or was!
//                applicationBean.sendFacesMessageNotification(sessionUser.getIdUsuario(), "Los datos del caso #" + event.getIdCaso() + " han sido modificado por " + event.getAssigner());
//            }
//        }
//    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String c) {
        this.channel = c;
    }

    public void setCurrent(Usuario current) {
        if((sessionUser == null) && (current != null))
        {
            applicationBean.init();
        }
        this.sessionUser = current;
    }

    public Map<String, String> getThemes() {
        return themes;
    }

    public String getTheme() {

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String path = request.getPathTranslated();

        if (!StringUtils.isEmpty(path) && path.contains("customer")) {
            return "bootstrap";
        }

        if (getCurrent() != null) {
            if (getCurrent().getTheme() != null && !getCurrent().getTheme().isEmpty()) {
                return getCurrent().getTheme();
            } else {
                return DEFAULT_THEME;
            }
        } else {
            return DEFAULT_THEME;
        }
    }

    public void setTheme(String theme) {
        this.getCurrent().setTheme(theme);
    }

    public void saveTheme() {
    }

    public String updateUsuario() {
        try {

            String rutFormateado = UtilesRut.formatear(sessionUser.getRut());
            sessionUser.setRut(rutFormateado);

            getJpaController().mergeUsuarioLight(sessionUser);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsuarioUpdated"));
            return null;
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public List<Theme> getAdvancedThemes() {
        return advancedThemes;
    }

    /**
     * @return the activeIndexOfMyAccount
     */
    public int getActiveIndexOfMyAccount() {
        return activeIndexOfMyAccount;
    }

    /**
     * @param activeIndexOfMyAccount the activeIndexOfMyAccount to set
     */
    public void setActiveIndexOfMyAccount(int activeIndexOfMyAccount) {
        this.activeIndexOfMyAccount = activeIndexOfMyAccount;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param applicationBean the applicationBean to set
     */
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    /**
     * @return the sessionClient
     */
    public EmailCliente getEmailCliente() {
        return emailCliente;
    }

    /**
     * @param emailCliente the emailCliente to set
     */
    public void setEmailCliente(EmailCliente emailCliente) {
        this.emailCliente = emailCliente;
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

    @PostConstruct
    public void init() {

        advancedThemes = new ArrayList<Theme>();
        advancedThemes.add(new Theme("itcs-theme", ""));
        advancedThemes.add(new Theme("itcs-theme2", ""));
        advancedThemes.add(new Theme("afterdark", "afterdark.png"));
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
        advancedThemes.add(new Theme("dark-hive", "dark-hive.png"));
        advancedThemes.add(new Theme("dot-luv", "dot-luv.png"));
        advancedThemes.add(new Theme("eggplant", "eggplant.png"));
        advancedThemes.add(new Theme("excite-bike", "excite-bike.png"));
        advancedThemes.add(new Theme("flick", "flick.png"));
        advancedThemes.add(new Theme("glass-x", "glass-x.png"));
        advancedThemes.add(new Theme("home", "home.png"));
        advancedThemes.add(new Theme("hot-sneaks", "hot-sneaks.png"));
        advancedThemes.add(new Theme("humanity", "humanity.png"));
        advancedThemes.add(new Theme("le-frog", "le-frog.png"));
        advancedThemes.add(new Theme("midnight", "midnight.png"));
        advancedThemes.add(new Theme("mint-choc", "mint-choc.png"));
        advancedThemes.add(new Theme("overcast", "overcast.png"));
        advancedThemes.add(new Theme("pepper-grinder", "pepper-grinder.png"));
        advancedThemes.add(new Theme("redmond", "redmond.png"));
        advancedThemes.add(new Theme("rocket", "rocket.png"));
        advancedThemes.add(new Theme("sam", "sam.png"));
        advancedThemes.add(new Theme("smoothness", "smoothness.png"));
        advancedThemes.add(new Theme("south-street", "south-street.png"));
        advancedThemes.add(new Theme("start", "start.png"));
        advancedThemes.add(new Theme("sunny", "sunny.png"));
        advancedThemes.add(new Theme("swanky-purse", "swanky-purse.png"));
        advancedThemes.add(new Theme("trontastic", "trontastic.png"));
        advancedThemes.add(new Theme("ui-darkness", "ui-darkness.png"));
        advancedThemes.add(new Theme("ui-lightness", "ui-lightness.png"));
        advancedThemes.add(new Theme("vader", "vader.png"));

        themes = new TreeMap<String, String>();
        themes.put("Afterdark", "afterdark");
        themes.put("Afternoon", "afternoon");
        themes.put("Afterwork", "afterwork");
        themes.put("Aristo", "aristo");
        themes.put("Black-Tie", "black-tie");
        themes.put("Blitzer", "blitzer");
        themes.put("Bluesky", "bluesky");
        themes.put("Bootstrap", "bootstrap");
        themes.put("Casablanca", "casablanca");
        themes.put("Cupertino", "cupertino");
        themes.put("Cruze", "cruze");
        themes.put("Dark-Hive", "dark-hive");
        themes.put("Dot-Luv", "dot-luv");
        themes.put("Eggplant", "eggplant");
        themes.put("Excite-Bike", "excite-bike");
        themes.put("Flick", "flick");
        themes.put("Glass-X", "glass-x");
        themes.put("Home", "home");
        themes.put("Hot-Sneaks", "hot-sneaks");
        themes.put("Humanity", "humanity");
        themes.put("Le-Frog", "le-frog");
        themes.put("Midnight", "midnight");
        themes.put("Mint-Choc", "mint-choc");
        themes.put("Overcast", "overcast");
        themes.put("Pepper-Grinder", "pepper-grinder");
        themes.put("Redmond", "redmond");
        themes.put("Rocket", "rocket");
        themes.put("Sam", "sam");
        themes.put("Smoothness", "smoothness");
        themes.put("South-Street", "south-street");
        themes.put("Start", "start");
        themes.put("Sunny", "sunny");
        themes.put("Swanky-Purse", "swanky-purse");
        themes.put("Trontastic", "trontastic");
        themes.put("UI-Darkness", "ui-darkness");
        themes.put("UI-Lightness", "ui-lightness");
        themes.put("Vader", "vader");
    }
}
