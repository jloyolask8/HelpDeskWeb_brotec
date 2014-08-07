/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util.delete;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "cuposHorarioBean")
@ApplicationScoped
public class CuposHorarioBean implements Serializable{

    public static final int CUPO_DEFAULT = 15;
    public static final String HORARIO_1 = "Primer grupo: 9:00 a 10:45";
    public static final String HORARIO_2 = "Segundo Grupo: 10:30 a 11:45";
    public static final String HORARIO_3 = "Tercer Grupo: 11:30 a 13:15";
    public static final String HORARIO_4 = "Cuarto Grupo: 14:45 a 16:00";
    public static final String HORARIO_5 = "Quinto Grupo: 15:15 a 17:00";

//    private HashMap<String, AtomicInteger> cuposMap;
    private List<Cupo> entries;
    private Map<String, String> inscritos = new HashMap<String, String>();

    /**
     * Creates a new instance of CuposHorarioBean
     */
    public CuposHorarioBean() {
    }
    
  

    @PostConstruct
    private void init() {

        entries = new ArrayList<Cupo>();
        entries.add(new Cupo(1, HORARIO_1, new AtomicInteger(CUPO_DEFAULT)));
        entries.add(new Cupo(2, HORARIO_2, new AtomicInteger(CUPO_DEFAULT)));
        entries.add(new Cupo(3, HORARIO_3, new AtomicInteger(CUPO_DEFAULT)));
        entries.add(new Cupo(4, HORARIO_4, new AtomicInteger(CUPO_DEFAULT)));
        entries.add(new Cupo(4, HORARIO_5, new AtomicInteger(CUPO_DEFAULT)));
//        cuposMap.put(HORARIO_1530_HRS_A_1700_HRS, MAX_CUPOS5);
    }

    public int decrementCupo(String rut, String email, final String horarioKey) {
        inscritos.put(rut, email);
        for (Cupo cupo : entries) {
            if (cupo.getHorario().equals(horarioKey)) {
                return cupo.getCupos().decrementAndGet();
            }
        }
        System.out.println(horarioKey + " NOT FOUND!");
        return -1;
    }

    public void updateCupos() {
        JsfUtil.addSuccessMessage("Actualizacion ok.");
    }

    public void addCupo() {
        entries.add(new Cupo(entries.size() + 1, "", new AtomicInteger(CUPO_DEFAULT)));
    }

    public void resetInscritos() {
        int inc = this.inscritos.size();
        this.inscritos = new HashMap<String, String>();
          FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, inc + " inscrito(s) removidos ok.",
                        "!"));
    }

    public void removeCupo(int id) {
        for (Cupo cupo : entries) {
            if (cupo.getId() == id) {
                entries.remove(cupo);
                JsfUtil.addSuccessMessage(cupo + " Removido ok.");
            }
        }
    }

    /**
     * @return the entries
     */
    public List<Cupo> getEntries() {
        return entries;
    }

    /**
     * @param entries the entries to set
     */
    public void setEntries(List<Cupo> entries) {
        this.entries = entries;
    }

    /**
     * @return the inscritos
     */
    public Map<String, String> getInscritos() {
        return inscritos;
    }

    /**
     * @param inscritos the inscritos to set
     */
    public void setInscritos(Map<String, String> inscritos) {
        this.inscritos = inscritos;
    }
}
