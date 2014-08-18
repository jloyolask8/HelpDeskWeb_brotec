/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Accion;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.AuditLog;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Condicion;
import com.itcs.helpdesk.persistence.entities.FieldType;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Prioridad;
import com.itcs.helpdesk.persistence.entities.ReglaTrigger;
import com.itcs.helpdesk.persistence.entities.TipoComparacion;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumFieldType;
import com.itcs.helpdesk.persistence.entityenums.EnumNombreAccion;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.custom.CasoJPACustomController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.CasoChangeListener;
import com.itcs.helpdesk.persistence.utils.ComparableField;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.thoughtworks.xstream.XStream;
import java.beans.Expression;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.resource.NotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author jorge
 * @author jonathan
 */
public class RulesEngine implements CasoChangeListener {

    private final JPAServiceFacade jpaController;
    private EntityManagerFactory emf = null;
    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;

    public RulesEngine(EntityManagerFactory emf, JPAServiceFacade jpaController) {
        this.jpaController = jpaController;
//        this.managerCasos = managerCasos;
        this.emf = emf;
    }

    private JPAServiceFacade getJpaController() {
        return jpaController;
    }

    @Override
    public void casoCreated(Caso caso) {
        List<ReglaTrigger> listaSup = getJpaController().getReglaTriggerFindByEvento("%CREATE%");
        List<ReglaTrigger> lista;

        if (ApplicationConfig.isAppDebugEnabled()) {
            Log.createLogger(this.getClass().getName()).logInfo("executing casoCreated Reglas -> " + listaSup);
        }
        do {
            lista = new LinkedList<ReglaTrigger>(listaSup);
            for (ReglaTrigger reglaTrigger : lista) {
                if (reglaTrigger.getReglaActiva()) {
                    boolean aplica = evalConditions(reglaTrigger, caso);
                    if (aplica) {
                        if (ApplicationConfig.isAppDebugEnabled()) {
                            Log.createLogger(this.getClass().getName()).logInfo("regla " + reglaTrigger.getIdTrigger() + " APLICA_AL_CASO " + caso.toString());
                        }
                        listaSup.remove(reglaTrigger);
                        for (Accion accion : reglaTrigger.getAccionList()) {
                            executeAction(accion, caso);
                        }
                    } else {
                        if (ApplicationConfig.isAppDebugEnabled()) {
                            Log.createLogger(this.getClass().getName()).logInfo("regla " + reglaTrigger.getIdTrigger() + " NO_APLICA_AL_CASO " + caso.toString());
                        }
                    }
                } else {
                    if (ApplicationConfig.isAppDebugEnabled()) {
                        Log.createLogger(this.getClass().getName()).logInfo("regla " + reglaTrigger.getIdTrigger() + " NO_ESTA_ACTIVA");
                    }
                }
            }
        } while (lista.size() > listaSup.size());
    }

    @Override
    public void casoChanged(Caso caso, List<AuditLog> changeList) {
        List<ReglaTrigger> listaSup = getJpaController().getReglaTriggerFindByEvento("%UPDATE%");
        List<ReglaTrigger> lista;
        do {
            lista = new LinkedList<ReglaTrigger>(listaSup);
            for (ReglaTrigger reglaTrigger : lista) {
                if (reglaTrigger.getReglaActiva()) {
                    boolean aplica = evalConditions(reglaTrigger, caso, changeList);
                    if (aplica) {
                        Log.createLogger(this.getClass().getName()).logInfo("regla " + reglaTrigger.getIdTrigger() + " APLICA_AL_CASO " + caso.toString());
                        listaSup.remove(reglaTrigger);
                        for (Accion accion : reglaTrigger.getAccionList()) {
                            executeAction(accion, caso);
                        }
                    }
                }
            }
        } while (lista.size() > listaSup.size());
    }
    
    private boolean evalConditions(ReglaTrigger reglaTrigger, Caso caso) {
        return evalConditions(reglaTrigger, caso, null);
    }

    private boolean evalConditions(ReglaTrigger reglaTrigger, Caso caso, List<AuditLog> changeList) {
        Log.createLogger(this.getClass().getName()).logInfo("*** Verificando regla -> " + reglaTrigger);
        boolean any = false;
        if (reglaTrigger.getAnyOrAll() != null) {
            any = reglaTrigger.getAnyOrAll().equals("ANY");
        }
        boolean aplica = false;
        for (Condicion condicion : reglaTrigger.getCondicionList()) {
            try {
                boolean applyCondition = evalCondition(reglaTrigger, condicion, caso, (changeList == null) ? new ArrayList<AuditLog>() : changeList);//no changes =)
                if (any) {
                    if (applyCondition) {
                        aplica = true;
                        break;
                    }
                } else if (applyCondition) {
                    aplica = true;
                } else {
                    aplica = false;
                    break;
                }
            } catch (Exception e) {
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error on casoCreated Listener", e);
                break;
            }
        }
        return aplica;
    }

    public void applyRuleOnThisCasos(ReglaTrigger reglaTrigger, List<Caso> selectedCasos) {

        for (Caso caso : selectedCasos) {
            if (reglaTrigger.getReglaActiva()) {
                boolean aplica = evalConditions(reglaTrigger, caso);
                if (aplica) {
                    Log.createLogger(this.getClass().getName()).logInfo("regla " + reglaTrigger.getIdTrigger() + " APLICA_AL_CASO " + caso.toString());
                    for (Accion accion : reglaTrigger.getAccionList()) {
                        executeAction(accion, caso);
                    }
                }
            }
        }
    }

    /**
     * TODO implement changeList
     *
     * @param condicion
     * @param caso
     * @param changeList
     * @return
     * @throws NotSupportedException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    private boolean evalCondition(ReglaTrigger reglaTrigger, Condicion condicion, Caso caso, List<AuditLog> changeList) throws NotSupportedException, ClassNotFoundException, Exception {

        TipoComparacion operador = condicion.getIdComparador();

        Map<String, ComparableField> annotatedFields = getJpaController().getAnnotatedComparableFieldsMap(Caso.class);

        ComparableField comparableField = annotatedFields.get(condicion.getIdCampo());
        FieldType fieldType = comparableField.getFieldTypeId();

        String valorAttributo = condicion.getValor();

        if (operador == null || comparableField == null || valorAttributo == null || fieldType == null) {
            throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", La condicion no cumple con los requisitos minimos!");
        }

        Expression expresion;
        String methodName = "get" + WordUtils.capitalize(comparableField.getIdCampo());
        expresion = new Expression(caso, methodName, new Object[0]);
        expresion.execute();
        final Object value = expresion.getValue();

//        if (ApplicationConfig.isAppDebugEnabled()) {
//            System.out.println("caso." + methodName + " = " + value);
//        }
        if (fieldType.equals(EnumFieldType.TEXT.getFieldType()) || fieldType.equals(EnumFieldType.TEXTAREA.getFieldType())) {
            //El valor es de tipo String, usarlo tal como esta
            if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {

                if (value != null) {
                    return valorAttributo.equals((String) value);
                }

            } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                if (value != null) {
                    return !valorAttributo.equals((String) value);
                }
            } else if (operador.equals(EnumTipoComparacion.CO.getTipoComparacion())) {

                if (value != null) {
                    final String patternToSearch = "\\b" + valorAttributo + "\\b";
                    Pattern p = Pattern.compile(patternToSearch, Pattern.CASE_INSENSITIVE);
                    //Match the given string with the pattern
                    Matcher m = p.matcher((String) value);
                    if (ApplicationConfig.isAppDebugEnabled()) {
                        System.out.println("patternToSearch:" + patternToSearch);
                    }
                    return m.find();
//                    return ((String) expresion.getValue()).toLowerCase().contains(valorAttributo.toLowerCase());//removes case sensitive issue
                }

            } else if (operador.equals(EnumTipoComparacion.CT.getTipoComparacion())) {//Changed TO =)

                if ((value != null) && (changeList != null)) {
                    for (AuditLog auditLog : changeList) {
                        if (comparableField.getIdCampo().equalsIgnoreCase(auditLog.getCampo())) {
                            return valorAttributo.equals((String) value);
                        }
                    }
                }

            } else {
                throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Comparador " + operador.getIdComparador() + " is not supported!!");
            }

        } else if (fieldType.equals(EnumFieldType.CALENDAR.getFieldType())) {
            //El valor es de tipo Fecha, usar el String parseado a una fecha

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            try {
                if (!valorAttributo.isEmpty()) {
                    Date fecha1 = sdf.parse(valorAttributo);
                    Date beanDate = ((Date) value);

                    if (fecha1 != null && beanDate != null) {
                        if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {

                            return (fecha1.compareTo(beanDate) == 0);
                        } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                            return (fecha1.compareTo(beanDate) != 0);
                        } else if (operador.equals(EnumTipoComparacion.LE.getTipoComparacion())) {

                            return (beanDate.getTime() <= fecha1.getTime()); //lessThanOrEqualTo

                        } else if (operador.equals(EnumTipoComparacion.GE.getTipoComparacion())) {
                            return (beanDate.getTime() >= fecha1.getTime());
                        } else if (operador.equals(EnumTipoComparacion.LT.getTipoComparacion())) {
                            return (beanDate.getTime() < fecha1.getTime());
                        } else if (operador.equals(EnumTipoComparacion.GT.getTipoComparacion())) {
                            return (beanDate.getTime() > fecha1.getTime());
                        } else if (operador.equals(EnumTipoComparacion.BW.getTipoComparacion())) {
                            Date fecha2 = sdf.parse(condicion.getValor2());
                            return ((beanDate.getTime() >= fecha1.getTime()) && (beanDate.getTime() <= fecha2.getTime()));
                        } else {
                            throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Comparador " + operador.getIdComparador() + " is not supported!!");
                        }
                    }
                } else {
                    if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {

                        return (value == null);
                    } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                        return (value != null);
                    } else {
                        throw new IllegalStateException("Comparador " + operador.getIdComparador() + " is not supported!!");
                    }
                }
            } catch (ParseException ex) {
                //ignore and do not add this filter to the query
                Logger.getLogger(CasoJPACustomController.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (fieldType.equals(EnumFieldType.SELECTONE_ENTITY.getFieldType())) {

            EntityManager em = emf.createEntityManager();
            try {

                //El valor es el id de un entity, que tipo de Entity?= comparableField.tipo
                Class class_ = comparableField.getTipo();//Class.forName(comparableField.getTipo());
                Object oneEntity = value;

                //One or more values??
                if (operador.equals(EnumTipoComparacion.SC.getTipoComparacion())) {
                    //One or more values, as list select many.
                    List<String> valores = condicion.getValoresList();

                    if (oneEntity != null) {
                        return valores.contains(emf.getPersistenceUnitUtil().getIdentifier(oneEntity).toString());

                    } else {
                        return false;
                    }

                } else {

                    if (CasoJPACustomController.PLACE_HOLDER_ANY.equalsIgnoreCase(valorAttributo)) {
                        if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {
                            return (oneEntity != null);
                        } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                            return (oneEntity == null);
                        } else if (operador.equals(EnumTipoComparacion.CT.getTipoComparacion())) {//Changed TO =)

                            if ((value != null) && (changeList != null)) {
                                for (AuditLog auditLog : changeList) {
                                    if (comparableField.getIdCampo().equalsIgnoreCase(auditLog.getCampo())) {
                                        return (oneEntity != null);
                                    }
                                }
                            }

                        } else {
                            throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Tipo comparacion " + operador.getIdComparador() + " is not supported here!!");
                        }
                    } else if (CasoJPACustomController.PLACE_HOLDER_NULL.equalsIgnoreCase(valorAttributo)) {
                        if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {
                            return (oneEntity == null);
                        } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                            return (oneEntity != null);
                        } else if (operador.equals(EnumTipoComparacion.CT.getTipoComparacion())) {//Changed TO =)

                            if ((value != null) && (changeList != null)) {
                                for (AuditLog auditLog : changeList) {
                                    if (comparableField.getIdCampo().equalsIgnoreCase(auditLog.getCampo())) {
                                        return (oneEntity == null);
                                    }
                                }
                            }

                        } else {
                            throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Tipo comparacion " + operador.getIdComparador() + " is not supported here!!");
                        }
                    } else if (CasoJPACustomController.PLACE_HOLDER_CURRENT_USER.equalsIgnoreCase(valorAttributo)) {

                        if (userSessionBean != null && userSessionBean.getCurrent() != null) {
                            if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {
//                                return (oneEntity == null);
                                return (oneEntity != null ? userSessionBean.getCurrent().equals(oneEntity) : false);
                            } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
//                            return (oneEntity != null);
                                return (oneEntity != null ? !userSessionBean.getCurrent().equals(oneEntity) : false);
                            } else if (operador.equals(EnumTipoComparacion.CT.getTipoComparacion())) {//Changed TO =)

                                if ((value != null) && (changeList != null)) {
                                    for (AuditLog auditLog : changeList) {
                                        if (comparableField.getIdCampo().equalsIgnoreCase(auditLog.getCampo())) {
                                            return (oneEntity != null ? userSessionBean.getCurrent().equals(oneEntity) : false);
                                        }
                                    }
                                }

                            } else {
                                throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Tipo comparacion " + operador.getIdComparador() + " is not supported here!!");
                            }
                        } else {
                            return false;
                        }

                    } else {
                        if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {
                            try {
                                final Object find = em.find(class_, valorAttributo);
                                if (find != null) {
                                    return find.equals(oneEntity);
                                } else {
                                    return false;
                                }

                            } catch (java.lang.IllegalArgumentException e) {
                                return em.find(class_, Integer.valueOf(valorAttributo)).equals(oneEntity);
                            }
                        } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                            try {
                                return !em.find(class_, valorAttributo).equals(oneEntity);
                            } catch (java.lang.IllegalArgumentException e) {
                                return !em.find(class_, Integer.valueOf(valorAttributo)).equals(oneEntity);
                            }

                        } else if (operador.equals(EnumTipoComparacion.CT.getTipoComparacion())) {//Changed TO =)

                            if ((value != null) && (changeList != null)) {
                                for (AuditLog auditLog : changeList) {
                                    if (comparableField.getIdCampo().equalsIgnoreCase(auditLog.getCampo())) {
                                        try {
                                            return em.find(class_, valorAttributo).equals(oneEntity);
                                        } catch (java.lang.IllegalArgumentException e) {
                                            return em.find(class_, Integer.valueOf(valorAttributo)).equals(oneEntity);
                                        }
                                    }
                                }
                            }

                        } else {
                            throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Tipo comparacion " + operador.getIdComparador() + " is not supported here!!");
                        }
                    }
                }

            } finally {
                em.close();
            }

        } else if (fieldType.equals(EnumFieldType.SELECTONE_PLACE_HOLDER.getFieldType())) {

            Object oneEntity = value;

            if (CasoJPACustomController.PLACE_HOLDER_ANY.equalsIgnoreCase(valorAttributo)) {
                if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {
                    return (oneEntity != null);
                } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                    return (oneEntity == null);
                } else {
                    throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Tipo comparacion " + operador.getIdComparador() + " is not supported here!!");
                }
            } else if (CasoJPACustomController.PLACE_HOLDER_NULL.equalsIgnoreCase(valorAttributo)) {
                if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {
                    return (oneEntity == null);
                } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                    return (oneEntity != null);
                } else {
                    throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Tipo comparacion " + operador.getIdComparador() + " is not supported here!!");
                }
            }

        } else if (fieldType.equals(EnumFieldType.CHECKBOX.getFieldType())) {
            //Boolean comparation
            //El valor es de tipo boolean, usar el String parseado a un boolean

            try {
                Boolean valueBoolean = (Boolean) value;
                if (valueBoolean == null) {
                    valueBoolean = false;
                }

                boolean boolValue = Boolean.valueOf(valorAttributo);

                if (operador.equals(EnumTipoComparacion.EQ.getTipoComparacion())) {
                    return (valueBoolean == boolValue);
                } else if (operador.equals(EnumTipoComparacion.NE.getTipoComparacion())) {
                    return (valueBoolean != boolValue);
                } else if (operador.equals(EnumTipoComparacion.CT.getTipoComparacion())) {//Changed TO =)

                    if (changeList != null) {
                        for (AuditLog auditLog : changeList) {
                            if (comparableField.getIdCampo().equalsIgnoreCase(auditLog.getCampo())) {
                                return (valueBoolean == boolValue);
                            }
                        }
                    }

                } else {
                    throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", Comparador " + operador.getIdComparador() + " is not supported!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        } else {
            throw new NotSupportedException("Regla " + reglaTrigger.getIdTrigger() + ", fieldType " + fieldType.getFieldTypeId() + " is not supported yet!!");
        }

        return false;
    }

    private void executeAction(Accion accion, Caso caso) {
        try {
//            if (accion.getIdNombreAccion().equals(EnumNombreAccion.CAMBIO_CAT.getNombreAccion())) {
//                cambiarCategoria(accion, caso);
//            } else 
            if (accion.getIdNombreAccion().equals(EnumNombreAccion.ASIGNAR_A_GRUPO.getNombreAccion())) {
                asignarCasoAGrupo(accion, caso);
            } else if (accion.getIdNombreAccion().equals(EnumNombreAccion.ASIGNAR_A_AREA.getNombreAccion())) {
                asignarCasoArea(accion, caso);
            } else if (accion.getIdNombreAccion().equals(EnumNombreAccion.CUSTOM.getNombreAccion())) {
                executeCustomAction(accion, caso);
            } else if (accion.getIdNombreAccion().equals(EnumNombreAccion.ASIGNAR_A_USUARIO.getNombreAccion())) {
                asignarCasoAUsuario(accion, caso);
            } else if (accion.getIdNombreAccion().equals(EnumNombreAccion.CAMBIAR_PRIORIDAD.getNombreAccion())) {
                cambiarPrioridad(accion, caso);
            } else if (accion.getIdNombreAccion().equals(EnumNombreAccion.RECALCULAR_SLA.getNombreAccion())) {
                recalcularSLA(caso);
            } else if (accion.getIdNombreAccion().equals(EnumNombreAccion.DEFINIR_SLA_FECHA_COMPRA.getNombreAccion())) {
                definirSLAFechaCompra(caso);
            } else if (accion.getIdNombreAccion().equals(EnumNombreAccion.ENVIAR_EMAIL.getNombreAccion())) {
                enviarCorreo(accion, caso);
            }
        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "ejecutarAccion failed", ex);
        }
    }

    private void enviarCorreo(Accion accion, Caso caso) {
        try {
            XStream xstream = new XStream();
            EmailStruct emailStruct = (EmailStruct) xstream.fromXML(accion.getParametros());

            //choose canal, prioritize the project's default canal
            Canal canal = (caso.getIdProducto() != null && caso.getIdProducto().getIdOutCanal() != null)
                    ? caso.getIdProducto().getIdOutCanal() : null;

            //choose canal, prioritize the area's default canal
            if (canal == null) {
                canal = (caso.getIdArea() != null && caso.getIdArea().getIdCanal() != null)
                        ? caso.getIdArea().getIdCanal() : caso.getIdCanal();
            }

            if (canal != null && canal.getIdTipoCanal() != null && canal.getIdTipoCanal().equals(EnumTipoCanal.EMAIL.getTipoCanal())
                    && !StringUtils.isEmpty(canal.getIdCanal())) {
                HelpDeskScheluder.scheduleSendMailNow(caso.getIdCaso(), canal.getIdCanal(), emailStruct.getBody(),
                        emailStruct.getToAdress(),
                        emailStruct.getSubject());
            } else {
                throw new EmailException("No se puede enviar el correo de recepcion de caso al cliente " + caso.toString() + ".Error: El area no tiene canal tipo email, el caso no tiene Area ni Canal o el canal no es del tipo email.");

            }
        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "enviarCorreo", ex);
        }
    }

    private void asignarCasoAGrupo(Accion accion, Caso caso) {
        try {
            Grupo grupo = getJpaController().getGrupoFindByIdGrupo(accion.getParametros());
            ManagerCasos manager = new ManagerCasos(getJpaController());
            manager.asignarCasoAUsuarioConMenosCasos(grupo, caso);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "asignarCasoAGrupo", ex);
        }
    }

    private void asignarCasoAUsuario(Accion accion, Caso caso) {
        try {
            Usuario usuario = getJpaController().getUsuarioFindByIdUsuario(accion.getParametros());
            caso.setOwner(usuario);
            getJpaController().mergeCasoWithoutNotify(caso);

            if (ApplicationConfig.isSendNotificationOnTransfer()) {
                try {
                    MailNotifier.notifyCasoAssigned(caso, null);
                } catch (Exception ex) {
                    Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "failed at asignarCasoAUsuario", ex);
                }
            }//this should be removed since in rules we dont notify actions, there is an option of notity action in the rule.

        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void asignarCasoArea(Accion accion, Caso caso) {
        try {
            Area area = getJpaController().find(Area.class, accion.getParametros());
            caso.setIdArea(area);
            getJpaController().mergeCasoWithoutNotify(caso);
        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "asignarCasoArea", ex);
        }
    }

    private void executeCustomAction(Accion accion, Caso caso) {
        try {
            String clazzName = accion.getParametros();

            Constructor actionConstructor = Class.forName(clazzName).getConstructor(JPAServiceFacade.class);
            Action actionInstance = (Action) actionConstructor.newInstance(getJpaController());
//            actionInstance.setJpaController(getJpaController());
//            actionInstance.setManagerCasos(managerCasos);
            actionInstance.execute(caso);
        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "executeCustomAction", ex);
        }
    }

    private void cambiarPrioridad(Accion accion, Caso caso) {
        try {
            Prioridad prioridad = getJpaController().getPrioridadFindByIdPrioridad(accion.getParametros());
            caso.setIdPrioridad(prioridad);
            getJpaController().mergeCasoWithoutNotify(caso);
        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "cambiarPrioridad", ex);
        }
    }

    private void definirSLAFechaCompra(Caso caso) {
        try {
            if (caso.getFechaEstimadaCompra() != null) {
                caso.setNextResponseDue(caso.getFechaEstimadaCompra());
                getJpaController().mergeCasoWithoutNotify(caso);
                HelpDeskScheluder.scheduleAlertaPorVencer(caso.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(caso));
//                HelpDeskScheluder.scheduleAlertaVencido(caso.getIdCaso(), caso.getNextResponseDue());
//                managerCasos.agendarAlertas(caso);
            } else {
                throw new ActionExecutionException("fecha estimada de compra es null");
            }
        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "definirSLAFechaCompra", ex);
        }
    }

    private void recalcularSLA(Caso caso) {
        try {
            ManagerCasos.calcularSLA(caso);
            getJpaController().mergeCasoWithoutNotify(caso);
            HelpDeskScheluder.scheduleAlertaPorVencer(caso.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(caso));
//            HelpDeskScheluder.scheduleAlertaVencido(caso.getIdCaso(), caso.getNextResponseDue());
//            managerCasos.agendarAlertas(caso);
        } catch (Exception ex) {
            Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, "recalcularSLA", ex);
        }
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }
}
