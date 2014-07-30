/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import static com.itcs.helpdesk.util.ManagerCasos.createLogReg;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jorge
 */
public class CreateCasoPadreAction extends Action{

    public CreateCasoPadreAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }

    @Override
    public void execute(Caso caso) throws ActionExecutionException {
        createCasoPreventaIfNeeded(caso);
    }
    
    private void createCasoPreventaIfNeeded(Caso casoOrigen) {

        if (casoOrigen.getEmailCliente() == null)// || casoOrigen.getIdProducto() == null)
        {
            return;//dont do a shit.
        }

        //TODO: This should be a RULE!
        if (!(casoOrigen.getTipoCaso().equals(EnumTipoCaso.COTIZACION.getTipoCaso()))) {
            return;
        }

        try {
            //Si un cliente esta cotizando, hay que crear implicitamente un caso de preVenta para que sea gestionado por el vendedor
            //La Cotizacion queda atachada al caso de preventa, si llegan nuevas cotizaciones se atachan al caso de preventa abierto para ese cliente.
            //select caso from caso where caso.idcliente = cliente and estado = abierto and tipo = preventa
            //se agrega filtro por producto!
            Caso padre = getJpaController().getCasoJpa().findCasoBy(casoOrigen.getEmailCliente()/*, casoOrigen.getIdProducto()*/,
                    EnumEstadoCaso.ABIERTO.getEstado(), EnumTipoCaso.PREVENTA.getTipoCaso());
            if (padre == null) {
                padre = new Caso();
                padre.setTema("Pre-Venta generada para gestión de cliente ");
                padre.setDescripcion("Pre-Venta única generada para gestion del cliente. Este caso contiene sub-casos.");
                padre.setEmailCliente(casoOrigen.getEmailCliente());

                padre.setIdProducto(casoOrigen.getIdProducto());
                padre.setIdModelo(casoOrigen.getIdModelo());
                padre.setEstadoAlerta(casoOrigen.getEstadoAlerta());
                padre.setIdCategoria(casoOrigen.getIdCategoria());
                padre.setTipoCaso(EnumTipoCaso.PREVENTA.getTipoCaso());
                padre.setIdArea(casoOrigen.getIdArea());
                padre.setIdPrioridad(casoOrigen.getIdPrioridad());
                padre.setEtiquetaList(casoOrigen.getEtiquetaList());
                padre.setIdCanal(casoOrigen.getIdCanal());
                //Brotec-Icafal specifics
                padre.setFechaEstimadaCompra(casoOrigen.getFechaEstimadaCompra());
                padre.setCreditoPreAprobado(casoOrigen.getCreditoPreAprobado());

                getManagerCasos().persistCaso(padre, createLogReg(padre, "PREVENTA", "Se crea caso para manejo de cliente, origen:" + casoOrigen, ""));
            } else {
                padre.setIdProducto(casoOrigen.getIdProducto());
                padre.setIdModelo(casoOrigen.getIdModelo());
                padre.setIdComponente(casoOrigen.getIdComponente());
                padre.setIdSubComponente(casoOrigen.getIdSubComponente());
                padre.setIdCategoria(casoOrigen.getIdCategoria());
                padre.setIdPrioridad(casoOrigen.getIdPrioridad());
            }

            if (padre != null) {
                casoOrigen.setIdCasoPadre(padre);
                List<Caso> casosHijosList = padre.getCasosHijosList();
                if (casosHijosList == null) {
                    casosHijosList = new ArrayList<Caso>();
                }
                if (!casosHijosList.contains(casoOrigen)) {
                    casosHijosList.add(casoOrigen);
                }
                padre.setCasosHijosList(casosHijosList);

                getJpaController().merge(padre);
                casoOrigen.setFechaModif(new Date());
                getJpaController().merge(casoOrigen);
            }

//            return padre;
            //si no existe se debe crear
            //caso preventa. asociar caso (this)
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "method createCasoPreventaIfNeeded", ex);
//            return null;
        }
    }
    
}
