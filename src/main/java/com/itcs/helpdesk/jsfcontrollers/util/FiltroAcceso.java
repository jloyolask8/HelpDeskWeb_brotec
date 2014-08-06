/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.persistence.entities.Rol;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumFunciones;
import com.itcs.helpdesk.persistence.entityenums.EnumRoles;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author danilomoya TODO DELETE THIS FUKIN CLASS!
 */
@ManagedBean(name = "filtroAcceso")
@SessionScoped
public class FiltroAcceso implements Serializable {

    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;

    public FiltroAcceso() {
    }

    public boolean isAdministradorDelSistema() {
        for (Rol rol : userSessionBean.getCurrent().getRolList()) {
            if (rol.equals(EnumRoles.ADMINISTRADOR.getRol())) {
                return true;
            }
        }
        return false;
    }

    public boolean verificaAccesoAFuncion(EnumFunciones funcion) {
        Usuario user = userSessionBean.getCurrent();
        for (Rol rol : user.getRolList()) {
            if (rol.getFuncionList().contains(funcion.getFuncion())) {
                return true;
            }
        }
        return false;
    }

   

    public boolean esUsuarioSistema() {
        Usuario user = userSessionBean.getCurrent();
        if (user != null && user.equals(EnumUsuariosBase.SISTEMA.getUsuario())) {
            return true;
        }
        return false;
    }

    

    public boolean verificarAccesoAFuncionAgregarCaso() {
        return verificaAccesoAFuncion(EnumFunciones.AGREGAR_CASO);
    }

    public boolean verificarAccesoAFuncionAdministrarVistas() {
        return verificaAccesoAFuncion(EnumFunciones.ADMINISTRAR_VISTAS);
    }
    
    public boolean verificarAccesoAFuncionAdministrarItemsPreentrega()
    {
        return verificaAccesoAFuncion(EnumFunciones.ADMINISTRAR_ITEMS_PREENTREGA);
    }
    
    public boolean verificarAccesoACrearCasoPreentrega()
    {
        return verificaAccesoAFuncion(EnumFunciones.CREAR_CASO_PREENTREGA);
    }

    public boolean verificarAccesoAFuncionSupervision() {
        return verificaAccesoAFuncion(EnumFunciones.SUPERVISOR);
    }

    public boolean verificarAccesoAFuncionEditarCaso() {
        return verificaAccesoAFuncion(EnumFunciones.EDITAR_CASO);
    }

    public boolean verificarAccesoAFuncionEliminarCaso() {
        return verificaAccesoAFuncion(EnumFunciones.ELIMINAR_CASO);
    }

    public boolean verificarAccesoAFuncionCambiarCategoriaCaso() {
        return verificaAccesoAFuncion(EnumFunciones.CAMBIAR_CATEGORIA_CASO);
    }

    public boolean verificarAccesoAFuncionAsignarTransferirCaso() {
        return verificaAccesoAFuncion(EnumFunciones.ASIGNAR_TRANSFERIR_CASO);
    }

    public boolean verificarAccesoAFuncionFiltroOwner() {
        return verificaAccesoAFuncion(EnumFunciones.FILTRO_OWNER);
    }

    public boolean verificarAccesoAFuncionEditarAjustes() {
        return verificaAccesoAFuncion(EnumFunciones.EDITAR_AJUSTES);
    }

    public boolean verificarAccesoAFuncionResponderCualquierCaso() {
        return verificaAccesoAFuncion(EnumFunciones.RESPONDER_CUALQUIER_CASO);
    }

    public boolean verificarAccesoAFuncionEditarCualquierCaso() {
        return verificaAccesoAFuncion(EnumFunciones.EDITAR_CUALQUIER_CASO);
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }
}
