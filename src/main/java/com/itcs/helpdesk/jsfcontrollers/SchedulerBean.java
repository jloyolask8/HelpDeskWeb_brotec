/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

/**
 *
 * @author jonathan
 */
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.Cliente_;
import com.itcs.helpdesk.persistence.entities.FieldType;
import com.itcs.helpdesk.persistence.entities.Prioridad;
import com.itcs.helpdesk.persistence.entities.Responsable;
import com.itcs.helpdesk.persistence.entities.TipoCanal;
import com.itcs.helpdesk.persistence.entities.TipoCaso;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumAreas;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumCategorias;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumFieldType;
import com.itcs.helpdesk.persistence.entityenums.EnumFunciones;
import com.itcs.helpdesk.persistence.entityenums.EnumGrupos;
import com.itcs.helpdesk.persistence.entityenums.EnumNombreAccion;
import com.itcs.helpdesk.persistence.entityenums.EnumPrioridad;
import com.itcs.helpdesk.persistence.entityenums.EnumResponsables;
import com.itcs.helpdesk.persistence.entityenums.EnumRoles;
import com.itcs.helpdesk.persistence.entityenums.EnumSettingsBase;
import com.itcs.helpdesk.persistence.entityenums.EnumSubEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoAlerta;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.persistence.jpa.exceptions.PreexistingEntityException;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.AutomaticOpsExecutor;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.ManagerCasos;
import com.itcs.jpautils.EasyCriteriaQuery;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.mail.internet.MimeUtility;
import javax.persistence.NoResultException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

@ManagedBean(name = "scheduler")
@SessionScoped
public class SchedulerBean extends AbstractManagedBean<Object> implements Serializable {

    private static final long serialVersionUID = 1L;
    private transient Scheduler scheduler;
    private transient List<QuartzJob> quartzJobList = new ArrayList<QuartzJob>();

    public SchedulerBean() throws SchedulerException {
        super(Object.class);
    }

    //trigger a job
    public void fireNow(String jobName, String jobGroup)
            throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);

    }

    public void agendarAlertasForAllCasos() {
        System.out.println("agendarAlertasForAllCasos");

        List<Caso> casos_pendiente = getJpaController().getCasoFindByEstadoAndAlerta(EnumEstadoCaso.ABIERTO.getEstado(), EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta());
//            System.out.println("encontrados "+casos.size()+" casos "+EnumTipoAlerta.TIPO_ALERTA_PENDIENTE+" que se debe agendar cambio de alerta");

        for (Caso caso : casos_pendiente) {
            if (caso.getNextResponseDue() != null) {
                try {
                    if ((caso.getEstadoAlerta().getIdalerta().equals(EnumTipoAlerta.TIPO_ALERTA_PENDIENTE.getTipoAlerta().getIdalerta()))
                            && (caso.getNextResponseDue().after(Calendar.getInstance().getTime()))) {
                        HelpDeskScheluder.scheduleAlertaPorVencer(caso.getIdCaso(), ManagerCasos.calculaCuandoPasaAPorVencer(caso));
                    }

                        //Siempre se debe agendar el cambio a caso vencido para cuando se acabe el plazo para responder el caso
                    //                    HelpDeskScheluder.scheduleAlertaVencido(caso.getIdCaso(), caso.getNextResponseDue());
                    //                    agendarCambioEstadoAlerta(schema, caso);
                } catch (SchedulerException ex) {
                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Caso " + caso.getIdCaso() + " agendarAlertasForAllCasos -> pendientes", ex);
                } catch (NullPointerException ex) {
                    Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "Caso " + caso.getIdCaso() + " error grave: ", ex);
                }
            }
        }

        List<Caso> casos_por_vencer = getJpaController().getCasoFindByEstadoAndAlerta(EnumEstadoCaso.ABIERTO.getEstado(),
                EnumTipoAlerta.TIPO_ALERTA_POR_VENCER.getTipoAlerta());
//            System.out.println("encontrados "+casos.size()+" casos "+EnumTipoAlerta.TIPO_ALERTA_POR_VENCER+" que se debe agendar cambio de alerta");
        for (Caso caso : casos_por_vencer) {
            try {
                HelpDeskScheluder.scheduleAlertaVencido(caso.getIdCaso(), caso.getNextResponseDue());
//                    agendarCambioEstadoAlerta(schema, caso);
            } catch (SchedulerException ex) {
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "agendarAlertasForAllCasos -> por vencer", ex);
            }
        }
    }

    public void deleteJob(String jobName, String jobGroup)
            throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);

    }

    public List<QuartzJob> getQuartzJobList() {
        try {
            scheduler = HelpDeskScheluder.getInstance();
            quartzJobList = new ArrayList<QuartzJob>();

            // loop jobs by group
            for (String groupName : scheduler.getJobGroupNames()) {

                // get jobkey
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher
                        .jobGroupEquals(groupName))) {

                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();

                    // get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler
                            .getTriggersOfJob(jobKey);
                    Date nextFireTime = triggers.get(0).getNextFireTime();

                    quartzJobList.add(new QuartzJob(jobName, jobGroup, nextFireTime));

                }

            }

            return quartzJobList;
        } catch (SchedulerException ex) {
            JsfUtil.addErrorMessage(ex, ex.getMessage());
            Logger.getLogger(SchedulerBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class QuartzJob {

        private static final long serialVersionUID = 1L;
        String jobName;
        String jobGroup;
        Date nextFireTime;

        public QuartzJob(String jobName, String jobGroup, Date nextFireTime) {

            this.jobName = jobName;
            this.jobGroup = jobGroup;
            this.nextFireTime = nextFireTime;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public String getJobGroup() {
            return jobGroup;
        }

        public void setJobGroup(String jobGroup) {
            this.jobGroup = jobGroup;
        }

        public Date getNextFireTime() {
            return nextFireTime;
        }

        public void setNextFireTime(Date nextFireTime) {
            this.nextFireTime = nextFireTime;
        }
    }
    
    //Nada que ver con el codigo
    public void verificaDatosBase() {
        JPAServiceFacade controller = getJpaController();
        System.out.println("verificaDatosBase()...");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
//        }
        verificarAreas(controller);
        verificarGrupos(controller);
        verificarUsuarios(controller);
        verificarFunciones(controller);
        verificarCategorias(controller);
        verificarTipoCaso(controller);
        verificarTipoCanal(controller);
        verificarTiposAlerta(controller);
        verificarTiposNota(controller);
        verificarTipoComparacion(controller);
        
        verificarRoles(controller);
        verificarEstadosCaso(controller);
        verificarSubEstadosCaso(controller);

        verificarPrioridades(controller);
        
        verificarFieldTypes(controller);
        
        verificarNombreAcciones(controller);
        verificarSettingsBase(controller);
        
        verificarCanales(controller);
        fixClientesCasos(controller);
        fixNombreCliente(controller);
        verificarResponsables(controller);
    }

    private void fixClientesCasos(JPAServiceFacade jpaController) {
        EasyCriteriaQuery<Caso> ecq = new EasyCriteriaQuery<Caso>(emf, Caso.class);
        ecq.addEqualPredicate("idCliente", null);
        ecq.addDistinctPredicate("emailCliente", null);
        List<Caso> casosToFix = ecq.getAllResultList();

        for (Caso caso : casosToFix) {
            caso.setIdCliente(caso.getEmailCliente().getCliente());
            try {
                jpaController.merge(caso);
            } catch (Exception ex) {
                Logger.getLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void fixNombreCliente(JPAServiceFacade jpaController) {
        EasyCriteriaQuery<Cliente> ecq = new EasyCriteriaQuery<Cliente>(emf, Cliente.class);
        ecq.addLikePredicate(Cliente_.nombres.getName(), "%=?ISO-8859-1%");
        List<Cliente> clientsToFix = ecq.getAllResultList();

        for (Cliente cliente : clientsToFix) {
            try {
                String from = MimeUtility.decodeText(cliente.getNombres().replace("\"", ""));
                String[] nombres = from.split(" ");
                if (nombres.length > 0) {
                    cliente.setNombres(nombres[0]);
                }
                if (nombres.length > 2) {
                    cliente.setApellidos(nombres[1] + " " + nombres[2]);
                } else if (nombres.length > 1) {
                    cliente.setApellidos(nombres[1]);
                }
                try {
                    jpaController.merge(cliente);
                } catch (Exception ex) {
                    Logger.getLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    private void printOutContraintViolation(ConstraintViolationException ex, String classname) {
//        Set<ConstraintViolation<?>> set = (ex).getConstraintViolations();
//        for (ConstraintViolation<?> constraintViolation : set) {
//            Log.createLogger(classname).logInfo("leafBean class: " + constraintViolation.getLeafBean().getClass());
//            Log.createLogger(classname).logInfo("anotacion: " + constraintViolation.getConstraintDescriptor().getAnnotation().toString() + " value:" + constraintViolation.getInvalidValue());
//        }
//    }
//
//    public void exceptionThreatment(Exception ex, String classname) {
//        if (ex instanceof ConstraintViolationException) {
//            printOutContraintViolation((ConstraintViolationException) ex, classname);
//        }
//        if (ex.getCause() instanceof ConstraintViolationException) {
//            printOutContraintViolation((ConstraintViolationException) (ex.getCause()), classname);
//        }
//        Log.createLogger(classname).log(Level.SEVERE, "exceptionThreatment", ex);
//    }
    private void verificarSettingsBase(JPAServiceFacade jpaController) {
        for (EnumSettingsBase enumSettingsBase : EnumSettingsBase.values()) {
            try {
                if (null == jpaController.getAppSettingJpaController().findAppSetting(enumSettingsBase.getAppSetting().getSettingKey())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existen settings " + enumSettingsBase.getAppSetting().getSettingKey() + ", se creara ahora");
                try {
                    jpaController.persistSetting(enumSettingsBase.getAppSetting());
                } catch (PreexistingEntityException pre) {
                    //ignore if already exists
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarTipoCanal(JPAServiceFacade jpaController) {
        for (EnumTipoCanal enumTipoCanal : EnumTipoCanal.values()) {
            System.out.println("verificando tipo de canal");
            try {
                TipoCanal tipoCanal = jpaController.find(TipoCanal.class, enumTipoCanal.getTipoCanal().getIdTipo());
                if (null == tipoCanal) {
                    throw new NoResultException();
                }
            } catch (Exception ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe tipo canal " + enumTipoCanal.getTipoCanal().getIdTipo() + ", se creara ahora");
                try {
                    jpaController.persist(enumTipoCanal.getTipoCanal());
                } catch (PreexistingEntityException pre) {
                    //ignore if already exists
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarUsuarios(JPAServiceFacade jpaController) {
        try {
            Usuario usuarioSistema = jpaController.getUsuarioFindByIdUsuario(EnumUsuariosBase.SISTEMA.getUsuario().getIdUsuario());
            if (null == usuarioSistema) {
                throw new NoResultException("No existe usuario SISTEMA");
            }
        } catch (NoResultException ex) {
            try {
                Log.createLogger(this.getClass().getName()).logSevere("No existe usuario SISTEMA, se creara ahora");
                jpaController.persistUsuario(EnumUsuariosBase.SISTEMA.getUsuario());
            } catch (Exception e) {
                Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    private void verificarTiposAlerta(JPAServiceFacade jpaController) {
        for (EnumTipoAlerta tipoAlerta : EnumTipoAlerta.values()) {
            try {
                if (null == jpaController.getTipoAlertaFindByIdTipoAlerta(tipoAlerta.getTipoAlerta().getIdalerta())) {
                    throw new NoResultException();
                }

            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe tipo alerta " + tipoAlerta.getTipoAlerta().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistTipoAlerta(tipoAlerta.getTipoAlerta());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarFunciones(JPAServiceFacade jpaController) {
        for (EnumFunciones funcion : EnumFunciones.values()) {

            if (null == jpaController.getFuncionFindByIdFuncion(funcion.getFuncion().getIdFuncion())) {
                try {
                    Log.createLogger(this.getClass().getName()).logSevere("No existe funcion " + funcion.getFuncion().getNombre() + ", se creara ahora");
                    jpaController.persistFuncion(funcion.getFuncion());
                } catch (PreexistingEntityException ex) {
                    Logger.getLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RollbackFailureException ex) {
                    Logger.getLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    private void verificarRoles(JPAServiceFacade jpaController) {
        for (EnumRoles enumRol : EnumRoles.values()) {
            try {
                if (null == jpaController.getRolFindByIdRol(enumRol.getRol().getIdRol())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe rol " + enumRol.getRol().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistRol(enumRol.getRol());
                } catch (Exception e) {
                    e.getCause().printStackTrace();
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarCategorias(JPAServiceFacade jpaController) {
        for (EnumCategorias enumCat : EnumCategorias.values()) {
            try {
                if (enumCat.isPersistente()) {
                    if (null == jpaController.getCategoriaFindByIdCategoria(enumCat.getCategoria().getIdCategoria())) {
                        throw new NoResultException();
                    }
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe categoria " + enumCat.getCategoria().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistCategoria(enumCat.getCategoria());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarResponsables(JPAServiceFacade jpaController) {
        for (EnumResponsables enumResponsables : EnumResponsables.values()) {
            try {
                if (null == jpaController.find(Responsable.class, enumResponsables.getResponsable().getIdResponsable())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe el responsable " + enumResponsables.getResponsable().getNombreResponsable() + ", se creara ahora");
                try {
                    jpaController.persist(enumResponsables.getResponsable());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, "verificarResponsables", e);
                }
            }
        }
    }

    private void verificarAreas(JPAServiceFacade jpaController) {
        for (EnumAreas enumArea : EnumAreas.values()) {
            try {
                if (null == jpaController.getAreaFindByIdArea(enumArea.getArea().getIdArea())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe area " + enumArea.getArea().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistArea(enumArea.getArea());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarGrupos(JPAServiceFacade jpaController) {
        for (EnumGrupos enumGrupos : EnumGrupos.values()) {
            try {
                if (null == jpaController.getGrupoFindByIdGrupo(enumGrupos.getGrupo().getIdGrupo())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe grupo " + enumGrupos.getGrupo().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistGrupo(enumGrupos.getGrupo());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarEstadosCaso(JPAServiceFacade jpaController) {
        for (EnumEstadoCaso enumEstado : EnumEstadoCaso.values()) {
            try {
                if (null == jpaController.getEstadoCasoFindByIdEstado(enumEstado.getEstado().getIdEstado())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe estado " + enumEstado.getEstado().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistEstadoCaso(enumEstado.getEstado());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarSubEstadosCaso(JPAServiceFacade jpaController) {
        for (EnumSubEstadoCaso enumSubEstado : EnumSubEstadoCaso.values()) {
            try {
                if (null == jpaController.getSubEstadoCasoFindByIdSubEstadoCaso(enumSubEstado.getSubEstado().getIdSubEstado())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe sub estado " + enumSubEstado.getSubEstado().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistSubEstadoCaso(enumSubEstado.getSubEstado());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarTipoCaso(JPAServiceFacade jpaController) {
        for (EnumTipoCaso enumTipoCaso : EnumTipoCaso.values()) {
            try {
                if (null == jpaController.find(TipoCaso.class, enumTipoCaso.getTipoCaso().getIdTipoCaso())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe el tipo caso " + enumTipoCaso.getTipoCaso().getIdTipoCaso() + ", se creara ahora");
                try {
                    jpaController.persist(enumTipoCaso.getTipoCaso());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, "verificarTipoCaso", e);
                }
            }
        }
    }

    private void verificarCanales(JPAServiceFacade jpaController) {
        for (EnumCanal enumCanal : EnumCanal.values()) {
            try {
                if (null == jpaController.getCanalFindByIdCanal(enumCanal.getCanal().getIdCanal())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe canal " + enumCanal.getCanal().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistCanal(enumCanal.getCanal());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarPrioridades(JPAServiceFacade jpaController) {
        for (EnumPrioridad enumPrioridad : EnumPrioridad.values()) {
            try {
                Prioridad prioridad = jpaController.getPrioridadFindByIdPrioridad(enumPrioridad.getPrioridad().getIdPrioridad());
                if (prioridad == null) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe prioridad " + enumPrioridad.getPrioridad().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistPrioridad(enumPrioridad.getPrioridad());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarTiposNota(JPAServiceFacade jpaController) {
        for (EnumTipoNota enumTipoNota : EnumTipoNota.values()) {
            try {
                if (null == jpaController.getTipoNotaFindById(enumTipoNota.getTipoNota().getIdTipoNota())) {
                    throw new NoResultException();
                } else {
                    jpaController.getTipoNotaJpaController().edit(enumTipoNota.getTipoNota());
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe tipo nota " + enumTipoNota.getTipoNota().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistTipoNota(enumTipoNota.getTipoNota());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            } catch (Exception ex) {
                Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void verificarNombreAcciones(JPAServiceFacade jpaController) {
        for (EnumNombreAccion enumNombreAccion : EnumNombreAccion.values()) {
            try {
                if (null == jpaController.getNombreAccionFindByIdNombreAccion(enumNombreAccion.getNombreAccion().getIdNombreAccion())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe nombre de accion " + enumNombreAccion.getNombreAccion().getNombre() + ", se creara ahora");
                try {
                    jpaController.persistNombreAccion(enumNombreAccion.getNombreAccion());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarFieldTypes(JPAServiceFacade jpaController) {
        for (EnumFieldType fieldType : EnumFieldType.values()) {
            try {
                if (null == jpaController.find(FieldType.class, fieldType.getFieldType().getFieldTypeId())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe nombre de fieldType " + fieldType.getFieldType().getFieldTypeId() + ", se creara ahora!!");
                try {
                    jpaController.persist(fieldType.getFieldType());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void verificarTipoComparacion(JPAServiceFacade jpaController) {
        for (EnumTipoComparacion enumTipoComparacion : EnumTipoComparacion.values()) {
            try {
                if (null == jpaController.getTipoComparacionFindByIdComparador(enumTipoComparacion.getTipoComparacion().getIdComparador())) {
                    throw new NoResultException();
                }
            } catch (NoResultException ex) {
                Log.createLogger(this.getClass().getName()).logSevere("No existe nombre de tipo comparacion " + enumTipoComparacion.getTipoComparacion().getIdComparador() + ", se creara ahora");
                try {
                    jpaController.persistTipoComparacion(enumTipoComparacion.getTipoComparacion());
                } catch (Exception e) {
                    Log.createLogger(AutomaticOpsExecutor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }
}
