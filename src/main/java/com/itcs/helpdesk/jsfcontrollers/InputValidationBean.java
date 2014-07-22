/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.util.UtilesRut;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "inputValidationBean")
@RequestScoped
public class InputValidationBean {

    /**
     * Creates a new instance of InputValidationBean
     */
    public InputValidationBean() {
    }

    public void validarRut(FacesContext context, UIComponent component,
            Object value) throws ValidatorException {
        System.out.println("validarRut()");
//        boolean validFormat = false;
        boolean validValue = false;
        String strValue = (String) value;

        //Set the email pattern string

//        Pattern p = Pattern.compile("^([0-9]{1,2}+(.)[0-9]{3}+(.)[0-9]{3}+-[0-9Kk])$");
//
//        //Match the given string with the pattern
//        Matcher m = p.matcher(strValue);
//
//        //Check whether match is found
//        boolean matchFound = m.matches();
//
//        if (matchFound) {
//            validFormat = true;
//        }
        if (StringUtils.isEmpty(strValue)) {
            return;
        } else if (UtilesRut.validar(strValue.trim())) {
            validValue = true;
        }

        if (!(/**
                 * validFormat &&
                 */
                validValue)) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "El rut ingresado no es válido.",
                    "El rut ingresado no es válido."));
        }

    }

    public static boolean esRutValidoRegex(String value) {
        boolean validFormat = false;

        //Set the email pattern string

        Pattern p = Pattern.compile("^([0-9]{1,2}+(.)[0-9]{3}+(.)[0-9]{3}+-[0-9Kk])$");

        //Match the given string with the pattern
        Matcher m = p.matcher(value);

        //Check whether match is found
        boolean matchFound = m.matches();

        if (matchFound) {
            validFormat = true;
        }

        return validFormat;
    }

    public static boolean isValidEmail(String value) {
        boolean validFormat = false;
        //Set the email pattern string
        Pattern p = Pattern.compile("[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?");

        //Match the given string with the pattern
        Matcher m = p.matcher(value);

        //Check whether match is found
        boolean matchFound = m.matches();

        if (matchFound) {
            validFormat = true;
        }

        return validFormat;
    }

    public void validarEmail(FacesContext context, UIComponent component,
            Object value) throws ValidatorException {
        System.out.println("validarEmail()");
        String strValue = (String) value;
        //Set the email pattern string
        Pattern p = Pattern.compile("[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?");

        //Match the given string with the pattern
        Matcher m = p.matcher(strValue);

        //Check whether match is found
        boolean matchFound = m.matches();

        if (!matchFound) {
            FacesMessage message = new FacesMessage();
            message.setDetail("El Email ingresado no es válido.");
            message.setSummary("El Email ingresado no es válido.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }

    public void validateNumber(FacesContext context, UIComponent component,
            Object value) throws ValidatorException {
        String strValue = (String) value;
        Pattern p = Pattern.compile("^\\d*");
        Matcher m = p.matcher(strValue);
        if (!m.matches()) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Para el campo Caso: Se deben usar sólo números",
                    "Para el campo Caso: Se deben usar sólo números"));
        }

    }
}
