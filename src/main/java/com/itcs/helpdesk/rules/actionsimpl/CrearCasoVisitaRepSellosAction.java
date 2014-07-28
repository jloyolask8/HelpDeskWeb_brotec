/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.actionsimpl;

import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Item;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonathan
 */
public class CrearCasoVisitaRepSellosAction extends Action {

    //seria bkn crear un XML representando un caso, para asi poder guardar un caso como string
    //similar a como nos envia el caso snap engage (xml) 
    //asi podriamos guardar el caso en un parametro (property) y no tener que crear cada dato del caso como un param.
    public static final String TEMA_KEY = "tema";
    public static final String DESC_KEY = "desc";
    public static final String ITEMS_KEY = "idItems";//comma separated id of Items values
    public static final String AREA_KEY = "idArea";
    public static final String GRUPO_KEY = "idGrupo";
    private static final String FECHA_VISITA_KEY = "FECHA_VISITA";
    private static final String FECHA_REP_KEY = "FECHA_REP";

    public CrearCasoVisitaRepSellosAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }

    @Override
    public void execute(Caso caso) throws ActionExecutionException {
        caso = getJpaController().find(Caso.class, caso.getIdCaso());
        Log.createLogger(CrearCasoVisitaRepSellosAction.class.getName()).logSevere("CrearCasoVisitaRepSellosAction.execute on " + caso);
        try {
            createSubCasos(caso);
        } catch (IOException ex) {
            Logger.getLogger(CrearCasoVisitaRepSellosAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createSubCasos(Caso casoAbuelo) throws IOException {

        if (casoAbuelo.getEmailCliente() == null)// || casoOrigen.getIdProducto() == null)
        {
            return;//dont do a shit.
        }

        Properties props = getConfigAsProperties();
        if (props != null && !props.isEmpty()) {

            Caso casoPadre = crearSubCaso(props, casoAbuelo, EnumTipoCaso.POSTVENTA.getTipoCaso(), null);
            Grupo grupo = getJpaController().getGrupoFindByIdGrupo(props.getProperty(GRUPO_KEY));
            ManagerCasos manager = new ManagerCasos(getJpaController());
            try {
                manager.asignarCasoAUsuarioConMenosCasos(grupo, casoPadre);
            } catch (Exception ex) {
                Logger.getLogger(CrearCasoVisitaRepSellosAction.class.getName()).log(Level.SEVERE, null, ex);
            }

            //crear nietos
            String idItems = props.getProperty(ITEMS_KEY);//comma separated id of Items values
            String itemsIds[] = idItems.split(",");
            for (String idItem : itemsIds) {
                Item i = getJpaController().find(Item.class, Integer.valueOf(idItem));
                if (i != null) {
                    crearSubCaso(props, casoPadre, EnumTipoCaso.REPARACION_ITEM.getTipoCaso(), i);
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "createSubCasos item error:{0}", idItem);
                }
            }

        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Properties error:{0}", "null properties");
//            return;//dont do a shit. cause i need properties to execute 
        }

    }

    private Caso crearSubCaso(Properties props, Caso casoPadre, TipoCaso tipo, Item item) {
        try {
            Caso casoNuevo = new Caso();
            casoNuevo.setIdItem(item);
            casoNuevo.setOwner(casoPadre.getOwner());
            casoNuevo.setTema(props.getProperty(TEMA_KEY));
            casoNuevo.setDescripcion(props.getProperty(DESC_KEY));
            casoNuevo.setIdArea(getJpaController().find(Area.class, props.getProperty(AREA_KEY)));// casoPadre.getIdArea());

            casoNuevo.setEmailCliente(casoPadre.getEmailCliente());

            casoNuevo.setIdProducto(casoPadre.getIdProducto());
            casoNuevo.setIdModelo(casoPadre.getIdModelo());
            casoNuevo.setIdComponente(casoPadre.getIdComponente());
            casoNuevo.setIdSubComponente(casoPadre.getIdSubComponente());
            casoNuevo.setEstadoAlerta(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());

            casoNuevo.setTipoCaso(tipo);//TO BE A GENERIC ACTION THIS COULD BE A PARAM

            casoNuevo.setIdPrioridad(casoPadre.getIdPrioridad());
//            newTicket.setEtiquetaList(casoPadre.getEtiquetaList());
            casoNuevo.setIdCanal(EnumCanal.MANUAL.getCanal());

            casoNuevo.setIdCasoPadre(casoPadre);
            //Brotec-Icafal specifics

            getManagerCasos().persistCaso(casoNuevo, ManagerCasos.createLogReg(casoNuevo, tipo.getNombre(),
                    "Se crea caso de forma automatizada desde caso padre: " + casoPadre, ""));

            List<Caso> casosHijosList = casoPadre.getCasosHijosList();
            if (casosHijosList == null) {
                casosHijosList = new ArrayList<Caso>();
            }

            casosHijosList.add(casoNuevo);
            casoPadre.setCasosHijosList(casosHijosList);
            getJpaController().merge(casoPadre);

            return casoNuevo;

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "method createSubCaso", ex);
            return null;
        }
    }
}
