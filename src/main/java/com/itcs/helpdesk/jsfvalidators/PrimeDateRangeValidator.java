/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.itcs.helpdesk.jsfvalidators;

/**
 *
 * @author jonathan
 */
import java.util.Date;
 
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
 
 
@FacesValidator("primeDateRangeValidator")
public class PrimeDateRangeValidator implements Validator {
     
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
         
        //Leave the null handling of startDate to required="true"
        Object startDateValue = component.getAttributes().get("startDate");
        if (startDateValue==null) {
            return;
        }
         
        Date startDate = (Date)startDateValue;
        Date endDate = (Date)value; 
        if (endDate.before(startDate) && !endDate.equals(startDate)) {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "La fecha de fin no puede ser antes de la fecha de inicio.", "La fecha de fin no puede ser antes de la fecha de inicio.");
            throw new ValidatorException(facesMsg);
        }
    }
}