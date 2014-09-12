package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

public class JsfUtil {

    public static SelectItem[] getSelectItemsStrings(List<?> entities, boolean selectOne) {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            items[i++] = new SelectItem(x.toString(), x.toString());
        }
        return items;
    }

    public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }

    public static SelectItem[] getSelectItems(Object[] list, boolean selectOne) {
        int size = selectOne ? list.length + 1 : list.length;
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : list) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }

    public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne, String getLabelMethodName) throws NoSuchMethodException, InvocationTargetException {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            try {
                Object o = x.getClass().getMethod(getLabelMethodName).invoke(x);
                if (o != null) {
                    if (o instanceof java.lang.String) {
                        items[i] = new SelectItem(x, (String) o);
                    } else {
                        items[i] = new SelectItem(x, o.toString());
                    }
                } else {
                    items[i] = new SelectItem(x, x.toString());
                }
                i++;
            } catch (Exception ex) {
                Logger.getLogger(JsfUtil.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return items;
    }

    public static SelectItem[] getSelectItems(Object[] list, boolean selectOne, String getLabelMethodName) {
        int size = selectOne ? list.length + 1 : list.length;
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : list) {
            try {
                Object o = x.getClass().getMethod(getLabelMethodName).invoke(x);
                if (o != null) {
                    if (o instanceof java.lang.String) {
                        items[i] = new SelectItem(x, (String) o);
                    } else {
                        items[i] = new SelectItem(x, o.toString());
                    }
                } else {
                    items[i] = new SelectItem(x, x.toString());
                }
                i++;
            } catch (Exception ex) {
                Logger.getLogger(JsfUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return items;
    }

    public static void addErrorMessage(Exception ex, String defaultMsg) {
        String msg = ex.getLocalizedMessage();
        if (msg != null && msg.length() > 0) {
            addErrorMessage(defaultMsg, msg);
        } else {
            addErrorMessage(defaultMsg);
        }
    }

    public static void addErrorMessages(List<String> messages) {
        for (String message : messages) {
            addErrorMessage(message);
        }
    }

    public static void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
    
    public static void addErrorMessage(String msg, String detail) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, detail);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static void addWarningMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static String getRequestParameter(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
    }

    public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
        String theId = JsfUtil.getRequestParameter(requestParameterName);
        return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    public static Object getManagedBean(String name) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context == null) {
                return null;
            }
            Application app = context.getApplication();
            ELResolver resolver = app.getELResolver();
            if (resolver == null) {
                return null;
            }
            ELContext elcontext = context.getELContext();
            if (elcontext == null) {
                return null;
            }
            Object obj = resolver.getValue(elcontext, null, name);
            return obj;
        } catch (Exception ex) {
            Log.createLogger(JsfUtil.class.getName()).log(Level.INFO, null, ex);
            return null;
        }
    }

    public static UserSessionBean getUserSessionBean() {
        return (UserSessionBean) FacesContext.getCurrentInstance().getApplication().getELResolver().getValue(FacesContext.getCurrentInstance().getELContext(), null, "UserSessionBean");
    }
    
    public static boolean isValidationFailed() {
        return FacesContext.getCurrentInstance().isValidationFailed();
    }
}