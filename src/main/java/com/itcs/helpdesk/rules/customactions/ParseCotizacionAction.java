/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.rules.customactions;

import com.itcs.helpdesk.jsfcontrollers.InputValidationBean;
import com.itcs.helpdesk.persistence.entities.Canal;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.ModeloProducto;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumSubEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCanal;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.rules.Action;
import com.itcs.helpdesk.rules.ActionExecutionException;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.UtilesRut;
import com.itcs.helpdesk.webservices.DatosCaso;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.NoResultException;
import javax.resource.NotSupportedException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonathan
 */
public abstract class ParseCotizacionAction extends Action {

    private Producto proyecto;

    public ParseCotizacionAction(JPAServiceFacade jpaController) {
        super(jpaController);
    }

    protected abstract String getCanalId();

    protected abstract String getCanalName();

    protected abstract DatosCaso collectData(String txt) throws ActionExecutionException;

    protected void executeBeforeMerge(Caso caso) throws ActionExecutionException {
    }

    @Override
    public void execute(Caso caso) throws ActionExecutionException {
        try {

            DatosCaso datos = collectData(caso.getDescripcion());

            if (ApplicationConfig.isAppDebugEnabled()) {
                System.out.println("debug datos collected:" + datos);
            }

            handleClientData(datos, caso);

            handleModeloData(datos, caso);

            //check canal
            Canal portalInmob = getJpaController().find(Canal.class, getCanalId());
            if (portalInmob == null) {
                portalInmob = new Canal(getCanalId());
                portalInmob.setNombre(getCanalName());
                portalInmob.setIdTipoCanal(EnumTipoCanal.FORMULARIO.getTipoCanal());
                getJpaController().persist(portalInmob);
            }
            caso.setIdCanal(portalInmob);
            caso.setTipoCaso(EnumTipoCaso.COTIZACION.getTipoCaso());
            caso.setIdSubEstado(EnumSubEstadoCaso.COTIZACION_NUEVO.getSubEstado());
//            caso.setIdPrioridad(EnumPrioridad.MEDIA.getPrioridad());

            executeBeforeMerge(caso);

            getManagerCasos().mergeCaso(caso, getManagerCasos().createLogReg(caso, "ParseCotizacionAction", "se parsea email marcado como cotizacion.", ""));

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ParseCotizacionAction.execute", ex);
        }
    }

    protected boolean complyWithMinimalDataRequirements(DatosCaso datos) {
        boolean valid = false;
        if (datos.getEmail() != null && !StringUtils.isEmpty(datos.getEmail())) {
            if (InputValidationBean.isValidEmail(datos.getEmail().toLowerCase().trim())) {
                valid = true;
            }
        }
        return valid;
    }

    private void printOutContraintViolation(ConstraintViolationException ex, String classname) {
        Set<ConstraintViolation<?>> set = (ex).getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : set) {
            Log.createLogger(classname).logInfo("leafBean class: " + constraintViolation.getLeafBean().getClass());
            Log.createLogger(classname).logInfo("anotacion: " + constraintViolation.getConstraintDescriptor().getAnnotation().toString() + " value:" + constraintViolation.getInvalidValue());
        }
    }

    public void exceptionThreatment(Exception ex, String classname) {
        if (ex instanceof ConstraintViolationException) {
            printOutContraintViolation((ConstraintViolationException) ex, classname);
        }
        if (ex.getCause() instanceof ConstraintViolationException) {
            printOutContraintViolation((ConstraintViolationException) (ex.getCause()), classname);
        }
        Log.createLogger(classname).log(Level.SEVERE, "exceptionThreatment", ex);
    }

    protected void tryToCollectUnknownData(Cliente persistentClient, DatosCaso datos) {
        if (persistentClient.getRut() == null || StringUtils.isEmpty(persistentClient.getRut())) {
            if (InputValidationBean.esRutValidoRegex(datos.getRut())) {
                persistentClient.setRut(datos.getRut());
            }
        }

        if (persistentClient.getNombres() == null || StringUtils.isEmpty(persistentClient.getNombres()) || persistentClient.getApellidos() == null || StringUtils.isEmpty(persistentClient.getApellidos())) {
            persistentClient.setNombres(datos.getNombre());
        }

        if (persistentClient.getApellidos() == null || StringUtils.isEmpty(persistentClient.getApellidos())) {
            persistentClient.setApellidos(datos.getApellidos());
        }

        if (persistentClient.getFono1() == null || StringUtils.isEmpty(persistentClient.getFono1())) {
            persistentClient.setFono1(datos.getTelefono());
        }

        if (persistentClient.getDirParticular() == null || StringUtils.isEmpty(persistentClient.getDirParticular())) {
            persistentClient.setDirParticular(datos.getComuna());
        }
    }

    protected void setClienteData(Cliente cliente, DatosCaso datos) {
        cliente.setNombres(datos.getNombre());
        cliente.setApellidos(datos.getApellidos());
        cliente.setDirParticular(datos.getComuna());
        cliente.setFono1(datos.getTelefono());
        cliente.setFono2(datos.getTelefono2());
        cliente.setRut(datos.getRut());
    }

    protected void handleClientData(DatosCaso datos, Caso caso) throws Exception {
        if (complyWithMinimalDataRequirements(datos)) {
            datos.setRut(UtilesRut.formatear(datos.getRut()));
            //is a good email lets check if cliente exists
            EmailCliente persistentEmailCliente = null;
            if (!StringUtils.isEmpty(datos.getEmail())) {
                persistentEmailCliente = getJpaController().find(EmailCliente.class, datos.getEmail());
            }
            //Existe email cliente
            if (persistentEmailCliente != null) {
                //exists, so what should i do with the data in the email?? i will just keep it in the description so agent can update if needed.
                //no reason to update the data in the DB if client also exists. but it could be helpful if we dont have the data.
                Cliente persistentClient = persistentEmailCliente.getCliente();

                if (persistentClient != null) {
                    //client exist as well
                    //check if data needed.

                    tryToCollectUnknownData(persistentClient, datos);

                    getJpaController().merge(persistentClient);
                    caso.setIdCliente(persistentClient);
                    getJpaController().merge(caso);

                } else {
                    //email exists but client may not exists
                    //check if rut exists in client table first
                    if (datos.getRut() != null && !StringUtils.isEmpty(datos.getRut())) {
                        persistentClient = getJpaController().getClienteJpaController().findByRut(datos.getRut());
                    }
                    if (persistentClient != null) {

                        persistentEmailCliente.setCliente(persistentClient);
                        persistentClient.getEmailClienteList().add(persistentEmailCliente);

                    } else {
                        //rut not present, must create the client.
                        Cliente cliente = new Cliente();
                        setClienteData(cliente, datos);
                        getJpaController().persist(cliente);
                        persistentEmailCliente.setCliente(cliente);
                    }
                }

                getJpaController().merge(persistentEmailCliente);
                caso.setEmailCliente(persistentEmailCliente);

            }
            //No existe email cliente
            else {
                //Si no viene con correo del cliente
                if (StringUtils.isEmpty(datos.getEmail())) 
                {
                    Cliente cliente = obtenerCliente(datos);
                    caso.setIdCliente(cliente);
                }
                //Si es que viene correo de cliente
                else {
                    //email not exist, will create email.
                    EmailCliente newEmailCliente = new EmailCliente(datos.getEmail());

                    Cliente persistentClient = obtenerCliente(datos);
                    caso.setIdCliente(persistentClient);
                    newEmailCliente.setCliente(persistentClient);
                    persistentClient.getEmailClienteList().add(newEmailCliente);
                    getJpaController().merge(newEmailCliente);
                    caso.setEmailCliente(newEmailCliente);
                }
            }

        } else {
            //check if only client rut is util to identify the user
            //check if rut exists in client table first
            Cliente persistentClient = getJpaController().getClienteJpaController().findByRut(datos.getRut());
            if (persistentClient != null) {
                final List<EmailCliente> emailClienteList = persistentClient.getEmailClienteList();
                if (emailClienteList != null && !emailClienteList.isEmpty()) {
                    final EmailCliente firstEmail = emailClienteList.get(0);
                    caso.setEmailCliente(firstEmail);//just use the first available
                } else {
                    //ERROR HORROR
                    //Will use the suposed bad email.
                    if (!StringUtils.isEmpty(datos.getEmail())) {
                    caso.setEmailCliente(new EmailCliente(datos.getEmail()));//just use the first available
                }
                }
            } else {
                //rut not present, must create the client. but wait not possible to associate this man with the case since there is no email
                //will save it just in case
                Cliente cliente = new Cliente();
                setClienteData(cliente, datos);
                getJpaController().persist(cliente);
            }
        }
    }

    private Cliente obtenerCliente(DatosCaso datos) throws Exception {
        //check if rut exists in client table first
        Cliente persistentClient = null;
        //Datos trae el rut
        if (datos.getRut() != null && !StringUtils.isEmpty(datos.getRut())) {
            persistentClient = getJpaController().getClienteJpaController().findByRut(datos.getRut());
        }
        //Si el cliente no existe se debe crear
        if (persistentClient == null) {
            persistentClient = new Cliente();
            setClienteData(persistentClient, datos);
            getJpaController().persist(persistentClient);
        }
        return persistentClient;
    }
    
    protected void handleModeloData(DatosCaso datos, Caso caso) throws NotSupportedException, ClassNotFoundException {

        try {

            if (datos.getProducto() != null && !StringUtils.isEmpty(datos.getProducto())) {
                // some product is passed
                Producto p = null;
                try {
                    p = getJpaController().getProductoFindByNombre(datos.getProducto());
                } catch (NoResultException ex) {
                    p = null;
                }
                //Producto p = getJpaController().find(Producto.class, datos.getProducto());//id
                if (p != null) {
                    caso.setIdProducto(p);
                    if (datos.getModelo() != null && !StringUtils.isEmpty(datos.getModelo())) {
                        List<ModeloProducto> listaModeloProducto = p.getModeloProductoList();
                        for (ModeloProducto modeloProducto : listaModeloProducto) {
                            if (modeloProducto.getNombre().equalsIgnoreCase(datos.getModelo().trim())) {
                                caso.setIdModelo(modeloProducto);
                                caso.setIdComponente(modeloProducto.getIdComponente());
                                break;
                            }
                        }
                    }
                }
            }
            if ((caso.getIdProducto() == null) && !StringUtils.isEmpty(datos.getModelo())) {
                handleProductByModel(datos, caso);
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ParseCotizacionAction.handleModeloData", ex);
        }
    }

    private void handleProductByModel(DatosCaso datos, Caso caso) throws IllegalStateException, NotSupportedException, ClassNotFoundException {
        Vista vista = new Vista(ModeloProducto.class);
        FiltroVista filter1 = new FiltroVista(1);
        filter1.setIdVista(vista);
        filter1.setIdCampo("nombre");
        filter1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filter1.setValor(datos.getModelo());
        
        if (vista.getFiltrosVistaList() == null) {
            vista.setFiltrosVistaList(new ArrayList<FiltroVista>());
        }
        vista.getFiltrosVistaList().add(filter1);
        
        List<ModeloProducto> modelos = (List<ModeloProducto>) getJpaController().findAllEntities(vista, new OrderBy("nombre"), null);
        
//                System.out.println("modelos:" + modelos);
        if (modelos != null) {
            for (ModeloProducto modelObject : modelos) {
                final Producto idProducto = modelObject.getProducto();
                if (idProducto != null) {
//                            System.out.println("Posible Proyecto:" + idProducto.getNombre());
                    
                    if ((datos.getProducto() != null && datos.getProducto().equalsIgnoreCase(idProducto.getIdProducto()))
                            || caso.getDescripcion().contains(idProducto.getNombre())) {
                        
//                                System.out.println("Match!!! Proyecto:" + idProducto.getNombre());
                        caso.setIdComponente(modelObject.getIdComponente());
                        caso.setIdModelo(modelObject);
                        caso.setIdProducto(idProducto);
                        break;
                        
                    }
                }
            }

        }
    }
    
    protected void extractEmail(String txt, DatosCaso datos) throws ActionExecutionException {
        
        String email = findPattern(txt, "(Email:\\s*?)([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)", 2);
        if (StringUtils.isEmpty(email)) {
            email = findPattern(txt, "(E-mail:\\s*?)([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)", 2);
        }
        if (!StringUtils.isEmpty(email)) {
            datos.setEmail(email);
        }
    }

    protected void extractRut(DatosCaso datos, String txt) {
        datos.setRut(UtilesRut.formatear(findPattern(txt, "(RUT:)(.*?)(\\n)", 2)));
    }

    protected void extractModelo(String txt, DatosCaso datos) {
        String modelo = findPattern(txt, "(Modelo de casa, Dormitorios:)(.*?)(,\\s\\d)", 2);
        if (StringUtils.isEmpty(modelo)) {
            modelo = findPattern(txt, "(Propiedad:)(.*?)(\\n)", 2);
        }
        if (StringUtils.isEmpty(modelo)) {
            modelo = findPattern(txt, "(Unidad:)(.*?)(\\n)", 2);
        }
        if (!StringUtils.isEmpty(modelo)) {
            modelo = modelo.replace("Casa", "").replace("Departamento", "")
                    .replace("Depto", "").replace("Dpto", "").replace("Oficina", "").trim();
            datos.setModelo(modelo);
        } else {
            if (proyecto != null) {
                for (ModeloProducto modeloProducto : proyecto.getModeloProductoList()) {
                    if (findPattern(txt, "(" + modeloProducto.getNombre() + ")(\\W*?)(\\n)", 1) != null) {
                        datos.setModelo(modeloProducto.getNombre());
                        break;
                    }
                }
            }
        }
    }

    protected void extractProyecto(DatosCaso datos, String txt) {
        proyecto = null;
        String nombreProyecto = findPattern(txt, "(Nombre proyecto:)(.*?)(\\n)", 2);
        if (!StringUtils.isEmpty(nombreProyecto)) {
            try {
                proyecto = getJpaController().getProductoFindByNombre(nombreProyecto);
            } catch (NoResultException ex) {
            }
        }

        if (proyecto == null) {
            nombreProyecto = findPattern(txt, "(Proyecto:)(.*?)(\\n)", 2);
            if (!StringUtils.isEmpty(nombreProyecto)) {
                try {
                    proyecto = getJpaController().getProductoFindByNombre(nombreProyecto);
                } catch (NoResultException ex) {
                }
            }
        }

        if (proyecto == null) {
            List<Producto> lista = getJpaController().getProductoFindAll();
            for (Producto producto : lista) {
                if (findPattern(txt, "(" + producto.getNombre() + ")(\\W*?)(\\n)", 1) != null) {
                    proyecto = producto;
                    break;
                }
            }
        }
        datos.setProducto(proyecto.getNombre());
    }

    protected void extractNombre(DatosCaso datos, String txt) {
        String nombre = findPattern(txt, "(Cotizante:)(.*?)(\\n)", 2);
        if (StringUtils.isEmpty(nombre)) {
            nombre = findPattern(txt, "(Nombre de la persona:)(.*?)(\\n)", 2);
        }
        if (StringUtils.isEmpty(nombre)) {
            nombre = findPattern(txt, "(Nombre:)(.*?)(\\n)", 2);
        }
        if (!StringUtils.isEmpty(nombre)) {
            datos.parseNombre(nombre);
        }
        
    }

    protected void extractTelefono(DatosCaso datos, String txt) {
        datos.setTelefono(findPattern(txt, "(Teléfono celular:)(.*?)(\\n)", 2));
        if (datos.getTelefono() == null) {
            datos.setTelefono(findPattern(txt, "(Teléfono Movil:)(.*?)(\\n)", 2));
        }
        if (datos.getTelefono() == null) {
            datos.setTelefono(findPattern(txt, "(Teléfono:)(.*?)(\\n)", 2));
        }
        if (datos.getTelefono() == null) {
            datos.setTelefono(findPattern(txt, "(Teléfono particular:)(.*?)(\\n)", 2));
            if (datos.getTelefono() == null) {
                datos.setTelefono(findPattern(txt, "(Teléfono comercial:)(.*?)(\\n)", 2));
            } else {
                datos.setTelefono2(findPattern(txt, "(Teléfono comercial:)(.*?)(\\n)", 2));
            }
        } else {
            datos.setTelefono2(findPattern(txt, "(Teléfono particular:)(.*?)(\\n)", 2));
            if (datos.getTelefono2() == null) {
                datos.setTelefono2(findPattern(txt, "(Teléfono comercial:)(.*?)(\\n)", 2));
            }
        }
    }

    protected String findPattern(String src, String strPattern, int group) {
        try {
            Pattern p = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(src);
            if (m.find()) {
                String string1 = m.group(group);
                return string1.trim();
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "error tratar de extraer patron:" + strPattern, e);
        }
        return null;
    }
}
