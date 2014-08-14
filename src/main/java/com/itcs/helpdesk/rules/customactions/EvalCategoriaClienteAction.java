/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Etiqueta;
import com.itcs.helpdesk.persistence.entityenums.EnumPrioridad;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.util.DateUtils;
import com.itcs.helpdesk.util.Log;
import static com.itcs.helpdesk.util.ManagerCasos.createLogReg;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 *
 * @author jorge
 */
public class EvalCategoriaClienteAction extends Action {

    public EvalCategoriaClienteAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }

    @Override
    public void execute(Caso current) throws ActionExecutionException {
        try {
            if (current.getTipoCaso().equals(EnumTipoCaso.PREVENTA.getTipoCaso()) || current.getTipoCaso().equals(EnumTipoCaso.COTIZACION.getTipoCaso())) {
                current.getEtiquetaList().remove(new Etiqueta("CLIENTE CATEGORIA B"));
                current.getEtiquetaList().remove(new Etiqueta("CLIENTE CATEGORIA C"));
                current.getEtiquetaList().remove(new Etiqueta("CLIENTE CATEGORIA A"));
                Etiqueta selected = null;
                if (current.getCreditoPreAprobado()) {
                    if (current.getFechaEstimadaCompra() != null) {
                        long days = DateUtils.daysBetween(new Date(), current.getFechaEstimadaCompra());

                        if (days <= 30) {
                            selected = new Etiqueta("CLIENTE CATEGORIA A");
//                            current.setIdCategoria(getJpaController().find(Categoria.class, 19));//A
                            current.setIdPrioridad(EnumPrioridad.ALTA.getPrioridad());
                        } else if (days > 30 && days < 90) {
                            selected = new Etiqueta("CLIENTE CATEGORIA B");
//                            current.setIdCategoria(getJpaController().find(Categoria.class, 20));//B
                            current.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());
                        } else {
                            selected = new Etiqueta("CLIENTE CATEGORIA C");
//                            current.setIdCategoria(getJpaController().find(Categoria.class, 21));//C
                            current.setIdPrioridad(EnumPrioridad.BAJA.getPrioridad());
                        }

                    } else {
                        selected = new Etiqueta("CLIENTE CATEGORIA C");
//                        current.setIdCategoria(getJpaController().find(Categoria.class, 21));//C
                        current.setIdPrioridad(EnumPrioridad.BAJA.getPrioridad());
                    }
                } else {
                    if (current.getFechaEstimadaCompra() != null) {
                        long days = DateUtils.daysBetween(new Date(), current.getFechaEstimadaCompra());

                        if (days < 90) {
                            selected = new Etiqueta("CLIENTE CATEGORIA B");
//                            current.setIdCategoria(getJpaController().find(Categoria.class, 20));//B
                            current.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());
                        } else {
                            selected = new Etiqueta("CLIENTE CATEGORIA C");
//                            current.setIdCategoria(getJpaController().find(Categoria.class, 21));//C
                            current.setIdPrioridad(EnumPrioridad.BAJA.getPrioridad());
                        }

                    } else {
                        selected = new Etiqueta("CLIENTE CATEGORIA C");
//                        current.setIdCategoria(getJpaController().find(Categoria.class, 21));//C
                        current.setIdPrioridad(EnumPrioridad.BAJA.getPrioridad());
                    }
                }
                if(selected.getCasoList() == null){
                    selected.setCasoList(new LinkedList<Caso>());
                }
                selected.getCasoList().add(current);
                Etiqueta etiquetaFound = getJpaController().find(Etiqueta.class, selected.getTagId());
                if(etiquetaFound != null){
                    etiquetaFound.getCasoList().add(current);
                    getJpaController().merge(etiquetaFound);
                }
                else{
                    current.getEtiquetaList().add(selected);
                    getJpaController().persist(selected);
                }
                getJpaController().mergeCaso(current, createLogReg(current, "se clasifica categoria cliente", selected.getTagId(), ""));
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "CasoController.update", e);
        }
    }

}
