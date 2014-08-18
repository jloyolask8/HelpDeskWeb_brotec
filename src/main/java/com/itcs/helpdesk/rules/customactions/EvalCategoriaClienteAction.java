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
import com.itcs.helpdesk.rules.ActionInfo;
import com.itcs.helpdesk.util.DateUtils;
import com.itcs.helpdesk.util.Log;
import static com.itcs.helpdesk.util.ManagerCasos.createLogReg;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author jorge
 */
@ActionInfo(name = "Evaluar categoría del cliente",
        description = "Evalua según atributo crédito pre-aprobado y fecha estimada de"
                + "compra la categoría del cliente y la prioridad del caso", mustShow = true)
public class EvalCategoriaClienteAction extends Action {

//    @ManagedProperty(value = "#{UserSessionBean}")
//    protected transient UserSessionBean userSessionBean;

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
                List<Etiqueta> etiquetaList = current.getEtiquetaList();
                if (etiquetaList == null) {
                    etiquetaList = new LinkedList<Etiqueta>();
                }
                etiquetaList.add(selected);

                try {
                        for (Etiqueta etiqueta : etiquetaList) {
//                            etiqueta.setOwner(userSessionBean.getCurrent());
                        if (etiqueta.getCasoList() == null) {
                            etiqueta.setCasoList(new LinkedList<Caso>());
                        }
                        etiqueta.getCasoList().add(current);
                    }
                    getJpaController().mergeCaso(current, createLogReg(current, "Etiquetas", "Se agrega Etiqueta :" + selected.toString(), ""));
                } catch (Exception ex) {
                    System.out.println("No se pudo Agregar la etiqueta" + selected);
                    Log.createLogger(EvalCategoriaClienteAction.class.getName()).log(Level.SEVERE, null, ex);
                }
//                selected.getCasoList().add(current);
//                Etiqueta etiquetaFound = getJpaController().find(Etiqueta.class, selected.getTagId());
//                if(etiquetaFound != null){
//                    current.getEtiquetaList().add(etiquetaFound);
//                    etiquetaFound.getCasoList().add(current);
////                    getJpaController().mergeInTx(etiquetaFound);
//                }
//                else{
//                    current.getEtiquetaList().add(selected);
////                    getJpaController().persist(selected);
//                }
//                getJpaController().mergeCaso(current, createLogReg(current, "se clasifica categoria cliente", selected.getTagId(), ""));
            }

        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "CasoController.update", e);
        }
    }

}
