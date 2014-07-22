/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.actionsimpl;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonathan
 */
public class CrearCasoVisitaRepSellosAction extends Action {

    public CrearCasoVisitaRepSellosAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }

    @Override
    public void execute(Caso caso) throws ActionExecutionException {

        caso = getJpaController().find(Caso.class, caso.getIdCaso());
        Log.createLogger(CrearCasoVisitaRepSellosAction.class.getName()).logSevere("CrearCasoVisitaRepSellosAction.execute on " + caso);
        createSubCaso(caso);

    }
    
    private void createSubCaso(Caso casoPadre) {

        if (casoPadre.getEmailCliente() == null)// || casoOrigen.getIdProducto() == null)
        {
            return;//dont do a shit.
        }

        //TODO: This should be a RULE!
//        if (!(casoPadre.getTipoCaso().equals(EnumTipoCaso.COTIZACION.getTipoCaso()))) {
//            return;
//        }

        try {
            //Si un cliente esta cotizando, hay que crear implicitamente un caso de preVenta para que sea gestionado por el vendedor
            //La Cotizacion queda atachada al caso de preventa, si llegan nuevas cotizaciones se atachan al caso de preventa abierto para ese cliente.
            //select caso from caso where caso.idcliente = cliente and estado = abierto and tipo = preventa
            //se agrega filtro por producto!
          
//                DatosCaso preVentaDatos = new DatosCaso();
               Caso newTicket = new Caso();
                newTicket.setTema("Visita programada - reparación de sellos");
                newTicket.setDescripcion("Visita programada para hoy, motivo: la reparación de sellos.");
                newTicket.setEmailCliente(casoPadre.getEmailCliente());

                 newTicket.setIdProducto(casoPadre.getIdProducto());
                newTicket.setIdModelo(casoPadre.getIdModelo());
                newTicket.setIdComponente(casoPadre.getIdComponente());
                newTicket.setIdSubComponente(casoPadre.getIdSubComponente());
                newTicket.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
               
                newTicket.setTipoCaso(EnumTipoCaso.POSTVENTA.getTipoCaso());
                newTicket.setIdArea(casoPadre.getIdArea());
                newTicket.setIdPrioridad(casoPadre.getIdPrioridad());
                newTicket.setEtiquetaList(casoPadre.getEtiquetaList());
                newTicket.setIdCanal(EnumCanal.INTERNO.getCanal());
                
                   newTicket.setIdCasoPadre(casoPadre);
                //Brotec-Icafal specifics

               getManagerCasos().persistCaso(newTicket, ManagerCasos.createLogReg(newTicket, "Post VENTA", "Se crea caso de forma automatizada para la reparacion de sellos:" + casoPadre, ""));
           
             

             
                
                List<Caso> casosHijosList = casoPadre.getCasosHijosList();
                if (casosHijosList == null) {
                    casosHijosList = new ArrayList<Caso>();
                }
               
                casosHijosList.add(newTicket);
                
                casoPadre.setCasosHijosList(casosHijosList);

//                getJpaController().merge(newTicket);
                getJpaController().merge(casoPadre);

//            return padre;
            //si no existe se debe crear
            //caso preventa. asociar caso (this)
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "method createSubCaso", ex);
//            return null;
        }
    }
}
