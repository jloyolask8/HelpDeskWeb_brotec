/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jorge
 */
public class RemoveDuplicatedCasoPreventaAction extends Action {

    public RemoveDuplicatedCasoPreventaAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }

    @Override
    public void execute(Caso caso) throws ActionExecutionException {
        if (caso.getTipoCaso().equals(EnumTipoCaso.PREVENTA.getTipoCaso())) {
            Cliente cliente = caso.getEmailCliente().getCliente();
            List<Caso> lista = getJpaController().getCasoJpa().findDuplicatedCasosPreventaByClient(cliente);
            if (lista == null) {
                return;
            }
            Caso casoPreventaUnico = lista.get(0);
            lista.remove(0);
            if(casoPreventaUnico.getCasosHijosList() == null)
            {
                casoPreventaUnico.setCasosHijosList(new LinkedList<Caso>());
            }
            while(!lista.isEmpty())
            {
                Caso casoPreventa = lista.get(0);
                lista.remove(0);
                if((casoPreventa.getCasosHijosList() != null) && (!casoPreventa.getCasosHijosList().isEmpty()))
                {
                    for (Caso casoHijo : casoPreventa.getCasosHijosList()) {
                        casoHijo.setIdCasoPadre(casoPreventaUnico);
                        if(!casoPreventaUnico.getCasosHijosList().contains(casoHijo))
                        {
                            casoPreventaUnico.getCasosHijosList().add(casoHijo);
                        }
                        try {
                            getJpaController().mergeCasoWithoutNotify(casoPreventaUnico);
                            getJpaController().mergeCasoWithoutNotify(casoHijo);
                        } catch (Exception ex) {
                            Logger.getLogger(RemoveDuplicatedCasoPreventaAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                casoPreventa.setCasosHijosList(null);
                try {
                    getJpaController().mergeCasoWithoutNotify(casoPreventa);
                    getJpaController().getCasoJpa().destroy(casoPreventa.getIdCaso());
                } catch (Exception ex) {
                    Logger.getLogger(RemoveDuplicatedCasoPreventaAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
