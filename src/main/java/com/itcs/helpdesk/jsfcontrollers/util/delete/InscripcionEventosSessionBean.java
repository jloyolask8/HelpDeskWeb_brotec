/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers.util.delete;

import com.itcs.helpdesk.jsfcontrollers.AbstractManagedBean;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.Componente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Nota;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import com.itcs.helpdesk.persistence.entities.ScheduleEvent;
import com.itcs.helpdesk.persistence.entities.ScheduleEventClient;
import com.itcs.helpdesk.persistence.entities.SubComponente;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoNota;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webservices.DatosCaso;
//import com.itcs.helpdesk.webservices.WSCasos_Service;
//import com.itcs.helpdesk.webservices.WsResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.resource.NotSupportedException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "inscripcionEventosSessionBean")
@SessionScoped
public class InscripcionEventosSessionBean extends AbstractManagedBean<Caso> implements Serializable {

    private FormCaso datos;

    /**
     * Creates a new instance of InscripcionSessionBean
     */
    public InscripcionEventosSessionBean() {
        super(Caso.class);
        datos = new FormCaso();
    }

    public void initializeData(javax.faces.event.ComponentSystemEvent event) {
        System.out.println("initializeData()...");
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String idCaso = req.getParameter("id");
        if (!StringUtils.isEmpty(idCaso)) {
            Caso caso = getJpaController().find(Caso.class, Long.valueOf(idCaso), true);
            if (caso != null) {
                setSelected(caso);
                //handle events
                Collections.sort(caso.getScheduleEventList());

            } else {
                //Error se necesita el id del caso/evento
            }
        } else {
            //Error se necesita el id del caso/evento
        }
    }

    public void formateaRutFiltro3() {
        try {
//            final String rutInput = getSelected().getEmailCliente().getCliente().getRut();
            String rutCliente_wizard = getDatos().getRut();
            if (rutCliente_wizard != null && !org.apache.commons.lang3.StringUtils.isEmpty(rutCliente_wizard)) {
                String formattedRut = UtilesRut.formatear(rutCliente_wizard);
                getDatos().setRut(formattedRut);
                Cliente c = getJpaController().getClienteJpaController().findByRut(formattedRut);
                if (c != null) {//this client exists
                    if (c.getEmailClienteList() != null && !c.getEmailClienteList().isEmpty()) {
                        EmailCliente emailCliente = c.getEmailClienteList().get(0);
                        getDatos().setEmail(emailCliente.getEmailCliente().toLowerCase());
                        getDatos().setTelefono(c.getFono1());

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ProductoContratado> getProductosContratados(Cliente idCliente, Producto producto, Componente componente, SubComponente subComponente) {
        Vista vista = new Vista(ProductoContratado.class);
        List<FiltroVista> filtrosVistaList = new ArrayList<FiltroVista>(2);

        if (idCliente != null) {
            //add filter
            FiltroVista filtroCliente = new FiltroVista();
            filtroCliente.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroCliente.setIdCampo("cliente");
            filtroCliente.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroCliente.setValor(idCliente.getIdCliente().toString());//desde
            filtroCliente.setIdVista(vista);
            filtrosVistaList.add(filtroCliente);
        }
        if (producto != null) {
            //add filter
            FiltroVista filtroProducto = new FiltroVista();
            filtroProducto.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroProducto.setIdCampo("producto");
            filtroProducto.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroProducto.setValor(producto.getIdProducto());//
            filtroProducto.setIdVista(vista);
            filtrosVistaList.add(filtroProducto);
        }
        if (componente != null) {
            //add filter
            FiltroVista filtroComponente = new FiltroVista();
            filtroComponente.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroComponente.setIdCampo("componente");
            filtroComponente.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroComponente.setValor(componente.getIdComponente());//
            filtroComponente.setIdVista(vista);
            filtrosVistaList.add(filtroComponente);
        }
        if (subComponente != null) {
            //add filter
            FiltroVista filtroSubComponente = new FiltroVista();
            filtroSubComponente.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroSubComponente.setIdCampo("subComponente");
            filtroSubComponente.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroSubComponente.setValor(subComponente.getIdSubComponente());//
            filtroSubComponente.setIdVista(vista);
            filtrosVistaList.add(filtroSubComponente);
        }
        vista.setFiltrosVistaList(filtrosVistaList);

        try {
            return (List<ProductoContratado>) getJpaController().findAllEntities(ProductoContratado.class, vista,
                    getDefaultOrderBy(), getDefaultUserWho());
        } catch (IllegalStateException ex) {//error en el filtro
            JsfUtil.addErrorMessage(ex, "Existe un problema con el filtro. Favor corregir e intentar nuevamente.");
        } catch (ClassNotFoundException ex) {
            JsfUtil.addErrorMessage(ex, "Lo sentimos, ocurrió un error inesperado. Favor contactar a soporte.");
            Logger.getLogger(AbstractManagedBean.class.getName()).log(Level.SEVERE, "ClassNotFoundException createPageDataModel", ex);
        } catch (NotSupportedException ex) {
            addWarnMessage("Lo sentimos, ocurrió un error inesperado. La acción que desea realizar aún no esta soportada por el sistema.");
        }

        return Collections.EMPTY_LIST;

    }

    public void enviarInscripcion() {

        if (datos.getScheduleEventId() == null) {
            addWarnMessage("Lo sentimos, no hay cupos disponibles en el evento seleccionado.");
            return;
        }
//        EmailCliente emailClienteEntity = getJpaController().getEmailClienteFindByEmail(datos.getEmail());
//        if (emailClienteEntity == null) {
        //no existe el email, puede ser que con el rut lo encontremos.
        Cliente clienteEntity = getJpaController().getClienteJpaController().findByRut(datos.getRut());
        if (clienteEntity == null) {
            //this dude is not a client!
            addWarnMessage("Lo sentimos este evento es sólo para clientes, Ud. no está registrado como cliente de la Inmobiliaria. Si ésta información es incorrecta favor notifíquenos enviando un mail a sac@brotec-icafal.cl.");
        } else {
            //Refresh state of caso
            Caso caso = getJpaController().find(Caso.class, getSelected().getIdCaso(), true);
            if (caso != null) {
                setSelected(caso);
            }
            //check si tiene producto contratado?
            List<ProductoContratado> pcs = getProductosContratados(clienteEntity, getSelected().getIdProducto(), getSelected().getIdComponente(), getSelected().getIdSubComponente());
            if (pcs != null && !pcs.isEmpty()) {
                //tiene al menos un prod contratado                
                //check si ya esta inscrito
                boolean inscrito = false;

                for (ScheduleEvent scheduleEvent : getSelected().getScheduleEventList()) {
                    for (ScheduleEventClient scheduleEventClientInscritos : scheduleEvent.getScheduleEventClientList()) {
                        if (scheduleEventClientInscritos.getCliente().equals(clienteEntity)) {
                            inscrito = true;
                            addWarnMessage("Ud. ya está inscrito al evento " + formatDateRange(scheduleEventClientInscritos.getScheduleEvent().getStartDate(), scheduleEventClientInscritos.getScheduleEvent().getEndDate()));
                            return;
                        }
                    }
                }

                if (!inscrito) {
                    //inscribir 

                    ScheduleEvent event = getJpaController().find(ScheduleEvent.class, datos.getScheduleEventId());

                    if (event.getMaxClientesInscritos() > event.getScheduleEventClientList().size()) {
                        ScheduleEventClient scheduleEventClient = new ScheduleEventClient(event.getEventId(), clienteEntity.getIdCliente());
                        scheduleEventClient.setCliente(clienteEntity);
                        scheduleEventClient.setScheduleEvent(event);
                        try {
                            if (event.getScheduleEventClientList() == null) {
                                event.setScheduleEventClientList(new ArrayList<ScheduleEventClient>());
                            }
                            event.getScheduleEventClientList().add(scheduleEventClient);
                            getJpaController().persist(scheduleEventClient);
                            getJpaController().merge(event);
                            if(getSelected().getNotaList() == null){
                                getSelected().setNotaList(new LinkedList<Nota>());
                            }
                            
                            Nota nota = new Nota();
//                            nota.setCreadaPor(EnumUsuariosBase.SISTEMA.getUsuario());
                            nota.setEnviadoPor(datos.getEmail());
                            nota.setFechaCreacion(new Date());
                            nota.setFechaModificacion(nota.getFechaCreacion());
                            nota.setIdCaso(caso);
                            nota.setTipoNota(EnumTipoNota.RESPUESTA_DE_CLIENTE.getTipoNota());
                            nota.setVisible(false);
                            nota.setTexto("[Inscripción] Bloque/Evento: " + formatDateRange(event.getStartDate(), event.getEndDate()) + "<br/>Email Cliente:" + datos.getEmail() + ", Rut: " + clienteEntity.getRut());
                                    
                            getSelected().getNotaList().add(nota);
                            getJpaController().merge(getSelected());
                            
                            addInfoMessage("Su inscripción fué enviada exitósamente. Revisaremos sus datos y le contactarémos luego!");
                            setDatos(new FormCaso());
                        } catch (Exception ex) {
                            Logger.getLogger(InscripcionEventosSessionBean.class.getName()).log(Level.SEVERE, null, ex);
                            addErrorMessage(ex.getMessage());
                        }
                    } else {
                        addErrorMessage("Lo sentimos, no hay cupos disponibles en el evento seleccionado. Favor seleccione otro bloque.");
                    }

                }

            } else {
                //Error no pass
                addWarnMessage("Lo sentimos Ud. no está en la lista de usuarios autorizados para inscribirse.");
            }
        }
    }
    
    

    /**
     * @return the datos
     */
    public FormCaso getDatos() {
        return datos;
    }

    /**
     * @param datos the datos to set
     */
    public void setDatos(FormCaso datos) {
        this.datos = datos;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}