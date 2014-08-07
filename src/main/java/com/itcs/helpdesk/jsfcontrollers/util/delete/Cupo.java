/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util.delete;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;


public class Cupo  implements Serializable{

    private int id;
    
    private String horario;
    private AtomicInteger cupos;

    public Cupo(int id, String horario, AtomicInteger cupos) {
        this.id = id;
        this.horario = horario;
        this.cupos = cupos;
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
     * @return the cupos
     */
    public AtomicInteger getCupos() {
        return cupos;
    }

    /**
     * @param cupos the cupos to set
     */
    public void setCupos(AtomicInteger cupos) {
        this.cupos = cupos;
    }
    
       /**
     * @return the cupos
     */
    public Integer getCuposs() {
        return cupos.get();
    }

    /**
     * @param cupos the cupos to set
     */
    public void setCuposs(Integer cupos) {
        this.cupos.set(cupos);
    }

    @Override
    public String toString() {
        return horario + " (" + cupos + " cupo(s) disponible(s))";
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cupo other = (Cupo) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
    
    
}
