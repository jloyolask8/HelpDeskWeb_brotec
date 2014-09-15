/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util.delete;

import java.io.Serializable;

/**
 *
 * @author jonathan
 */
public class FormCaso implements Serializable{
    
    private String rut;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String descripcion;
    private String producto;
    private String tipoCaso;
    private String horario;
    private Integer scheduleEventId;//
//    private String tipoConsulta;

    /**
     * @return the rut
     */
    public String getRut() {
        return rut;
    }

    /**
     * @param rut the rut to set
     */
    public void setRut(String rut) {
        this.rut = rut;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the apellidos
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * @param apellidos the apellidos to set
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return the descripcion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @param descripcion the descripcion to set
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * @return the producto
     */
    public String getProducto() {
        return producto;
    }

    /**
     * @param producto the producto to set
     */
    public void setProducto(String producto) {
        this.producto = producto;
    }

    /**
     * @return the horario
     */
    public String getHorario() {
        return horario;
    }

    /**
     * @param horario the horario to set
     */
    public void setHorario(String horario) {
        this.horario = horario;
    }

    /**
     * @return the tipoCaso
     */
    public String getTipoCaso() {
        return tipoCaso;
    }

    /**
     * @param tipoCaso the tipoCaso to set
     */
    public void setTipoCaso(String tipoCaso) {
        this.tipoCaso = tipoCaso;
    }

    /**
     * @return the scheduleEventId
     */
    public Integer getScheduleEventId() {
        return scheduleEventId;
    }

    /**
     * @param scheduleEventId the scheduleEventId to set
     */
    public void setScheduleEventId(Integer scheduleEventId) {
        this.scheduleEventId = scheduleEventId;
    }
    
    
    
}
