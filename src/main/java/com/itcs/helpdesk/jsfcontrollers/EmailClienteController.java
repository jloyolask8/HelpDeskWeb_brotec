package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Cliente;
import com.itcs.helpdesk.persistence.entities.EmailCliente;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.ProductoContratado;
import com.itcs.helpdesk.persistence.entities.ProductoContratadoPK;
import com.itcs.helpdesk.persistence.entities.SubComponente;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entities.EmailCliente_;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.UtilesRut;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import jxl.CellReferenceHelper;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.UploadedFile;

@ManagedBean(name = "emailClienteController")
@SessionScoped
public class EmailClienteController extends AbstractManagedBean<EmailCliente> implements Serializable {

//    private EmailCliente current;
    private int selectedItemIndex;
    private boolean canCreate = false;
    private transient UploadedFile fileClients;
    private String cellPositionSubComponentId = "";
    private String cellPositionRut = "";
    private String cellPositionNombre = "";
    private String cellPositionApellidos = "";
    private String cellPositionCorreo = "";
    private String cellPositionSexo = "";
    private String cellPositionDireccion1 = "";
    private String cellPositionFono1 = "";
    private String cellPositionFono2 = "";
    private List<ProductoContratado> bulkLoadedProductoContratado;
    private String bulkLoadedProductoContratadoTipoAsoc;
    private List<Cliente> bulkLoadedClients;
    private List<Cliente> bulkLoadedClientsErrors;
    private String searchPattern;
    private ProductoContratado currentProductoContratado = new ProductoContratado();
    private boolean createClientIfNotExist = false;

    public EmailClienteController() {
        super(EmailCliente.class);
    }

    @Override
    public OrderBy getDefaultOrderBy() {
        return new OrderBy("emailCliente");
    }

    public List<EmailCliente> completeEmailCliente(String query) {
        System.out.println(query);
        //List<EmailCliente> results = new ArrayList<EmailCliente>();
//        List<EmailCliente> emailClientes = getJpaController().getEmailClienteFindByEmailLike(query, 10);
//        System.out.println(emailClientes);

        Vista vista1 = new Vista(EmailCliente.class);
//            
        FiltroVista f1 = new FiltroVista();
        f1.setIdCampo("emailCliente");
        f1.setIdComparador(EnumTipoComparacion.CO.getTipoComparacion());
        f1.setValor(query);
        f1.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(f1);
        List<EmailCliente> emailClientes;
        try {
            emailClientes = (List<EmailCliente>) getJpaController().findEntitiesFirstChunk(vista1, getDefaultOrderBy(), query);

            if (emailClientes != null && !emailClientes.isEmpty()) {
                return emailClientes;
            } else {
//            emailCliente_wizard_existeEmail = false;
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe el email:" + query, "No existe el Cliente con email:" + query + ". Se creará automáticamente.");
                FacesContext.getCurrentInstance().addMessage(null, message);
                emailClientes = new ArrayList<>();
                emailClientes.add(new EmailCliente(query));
                System.out.println("No existe el Cliente con email" + query);
                return emailClientes;
            }

        } catch (IllegalStateException | ClassNotFoundException ex) {
            Logger.getLogger(EmailClienteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * devuelve la lista de emails de clientes como lista<strings>
     *
     * @param query
     * @return
     */
    public List<String> completeEmailClienteString(String query) {
        try {
            System.out.println(query);
            List<String> results = new ArrayList<>();
            Vista vista1 = new Vista(EmailCliente.class);
//
            FiltroVista f1 = new FiltroVista();
            f1.setIdCampo("emailCliente");
            f1.setIdComparador(EnumTipoComparacion.CO.getTipoComparacion());
            f1.setValor(query);
            f1.setIdVista(vista1);
            vista1.getFiltrosVistaList().add(f1);
            List<EmailCliente> emailClientes;

            emailClientes = (List<EmailCliente>) getJpaController().findEntitiesFirstChunk(vista1, getDefaultOrderBy(), query);
//        List<EmailCliente> emailClientes = getJpaController().getEmailClienteFindByEmailLike(query, 10);
//        System.out.println(emailClientes);
            if (emailClientes != null && !emailClientes.isEmpty()) {
                for (EmailCliente emailCliente : emailClientes) {
                    results.add(emailCliente.getEmailCliente());
                }
            }
//        else {
////            emailCliente_wizard_existeEmail = false;
//            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe el email:" + query, "No existe el Cliente con email:" + query);
//            FacesContext.getCurrentInstance().addMessage(null, message);
//            System.out.println("No existe el Cliente con email" + query);
//        }

            if (InputValidationBean.isValidEmail(query) && !results.contains(query)) {
                results.add(query);
            }

            return results;
        } catch (IllegalStateException | ClassNotFoundException ex) {
            Logger.getLogger(EmailClienteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.EMPTY_LIST;
    }

    public String prepareCreateProductoContratado() {
        currentProductoContratado = new ProductoContratado();
        currentProductoContratado.setCliente(current.getCliente());
        return null;
    }

    public void prepareEditProductoContratado(ProductoContratado item) {
        currentProductoContratado = item;
    }

    public void handleEditProductoContratado() {
        executeInClient("PF('addPCDialog').hide()");
    }

    public void handleProductChange() {
    }

    public void addProductoContratado() {
        if (this.getSelected().getCliente().getProductoContratadoList() == null || this.getSelected().getCliente().getProductoContratadoList().isEmpty()) {
            this.getSelected().getCliente().setProductoContratadoList(new ArrayList<ProductoContratado>());
        }
        ProductoContratadoPK pk = new ProductoContratadoPK(current.getCliente().getIdCliente(), currentProductoContratado.getProducto().getIdProducto(), currentProductoContratado.getComponente().getIdComponente(), currentProductoContratado.getSubComponente().getIdSubComponente());
        currentProductoContratado.setProductoContratadoPK(pk);
        currentProductoContratado.setCliente(current.getCliente());
        if (!this.getSelected().getCliente().getProductoContratadoList().contains(currentProductoContratado)) {
            this.getSelected().getCliente().getProductoContratadoList().add(currentProductoContratado);
            this.currentProductoContratado = new ProductoContratado();
            executeInClient("PF('addPCDialog').hide()");

        } else {
            JsfUtil.addWarningMessage("No se puede asociar, ya existe en la lista.");

        }

    }

    public String saveBulkImportClienteProd() {
        try {
            int total = bulkLoadedProductoContratado != null ? bulkLoadedProductoContratado.size() : 0;
            int results = 0;
            for (ProductoContratado productoContratado : bulkLoadedProductoContratado) {
                try {
                    getJpaController().persist(productoContratado);
                    getJpaController().merge(productoContratado.getCliente());

                    for (EmailCliente emailCliente : productoContratado.getCliente().getEmailClienteList()) {
                        for (Caso caso : emailCliente.getCasoList()) {
                            getJpaController().merge(caso);
                        }
                    }

                    results++;
                } catch (Exception e) {
                    //ignore
                }

            }

            JsfUtil.addSuccessMessage(results + " registros fueron guardados de un total de " + total);
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
            return null;
        }
    }

    public void handleFileUploadClienteProd() {
        handleFileUploadClienteProd(null);
    }

    public void handleFileUploadClienteProd(FileUploadEvent event) {

        System.out.println("bulkLoadedProductoContratadoTipoAsoc:" + bulkLoadedProductoContratadoTipoAsoc);

//        String email_regexp = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";
        if (event != null) {
            fileClients = event.getFile();
        }

        if (fileClients != null) {

            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");

            Workbook w;
            try {
                w = Workbook.getWorkbook(fileClients.getInputstream(), ws);
                // Get the first sheet
                Sheet sheet = w.getSheet(0);
                // Loop over first 10 column and lines

                Map<String, ProductoContratado> map = new HashMap<>();
//                Map<String, ProductoContratado> mapWithError = new HashMap<String, ProductoContratado>();

//                Map<String, Cliente> bulkLoadedClientsNotExists = new HashMap<String, Cliente>();
                int bulkLoadedClientsNotExists = 0;
                int bulkLoadedSubComponentNotExists = 0;
//                Map<String, SubComponente> bulkLoadedSubComponentNotExists = new HashMap<String, SubComponente>();

//                Pattern p = Pattern.compile(email_regexp);
                for (int rowIndex = 1; rowIndex < sheet.getRows(); rowIndex++) {

                    String rut = sheet.getCell(CellReferenceHelper.getColumn(cellPositionRut), rowIndex).getContents();
                    String subComponentId = sheet.getCell(CellReferenceHelper.getColumn(cellPositionSubComponentId), rowIndex).getContents();
                    avoidLeak(rut, subComponentId, map, bulkLoadedSubComponentNotExists, bulkLoadedClientsNotExists);

                }

                bulkLoadedProductoContratado = new ArrayList<>(map.values());
                addInfoMessage("Archivo " + fileClients.getFileName() + "cargado exitósamente");
            } catch (Exception e) {
                addErrorMessage(e.getMessage());
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "handleFileUploadClienteProd", e);
//                e.printStackTrace();
            }

//            FacesMessage msg = new FacesMessage("Archivo cargado exitósamente", fileClients.getFileName());
//            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            FacesMessage msg = new FacesMessage("Error al subir el archivo, intente nuevamente");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

    }

    private void avoidLeak(String rut, String subComponentId, Map<String, ProductoContratado> map, int bulkLoadedSubComponentNotExists, int bulkLoadedClientsNotExists) {
        Cliente c = null;
        SubComponente subComponent = null;

        if (rut != null) {

            if (UtilesRut.validar(rut)) {
                rut = UtilesRut.formatear(rut);
                c = getJpaController().findClienteByRut(rut);
//                            if (createClientIfNotExist && c == null) {
//                                //must create client dude.
//
//                                String nombres = sheet.getCell(CellReferenceHelper.getColumn(cellPositionNombre), rowIndex).getContents();
//                                String apellidos = sheet.getCell(CellReferenceHelper.getColumn(cellPositionApellidos), rowIndex).getContents();
//                                String correo = sheet.getCell(CellReferenceHelper.getColumn(cellPositionCorreo), rowIndex).getContents();
//                                String sexo = sheet.getCell(CellReferenceHelper.getColumn(cellPositionSexo), rowIndex).getContents();
//                                String direccion1 = sheet.getCell(CellReferenceHelper.getColumn(cellPositionDireccion1), rowIndex).getContents();
//
//                                //Match the given string with the pattern
//                                Matcher emailMatcher = p.matcher(correo);
//
//                                EmailCliente ec = new EmailCliente(correo);                            
//
//                                c = new Cliente();                                
//                                c.setIdCliente(UtilesRut.getAsNumber(rut));
//                                c.setApellidos(apellidos);
//                                c.setDirParticular(direccion1);
//                                c.setNombres(nombres);
//                                c.setRut(rut);
//                                if (sexo != null && (sexo.equalsIgnoreCase("Hombre") || sexo.equalsIgnoreCase("Mujer"))) {
//                                    c.setSexo(sexo);
//                                } else {
//                                    c.setSexo("Desconocido");
//                                }
//                                ec.setCliente(c);
//                                List<EmailCliente> emails = new ArrayList<EmailCliente>();
//                                emails.add(ec);
//                                c.setEmailClienteList(emails);                                
//                                
//                                    //Check whether match is found
//
////                                if (!emailMatcher.matches()) {                                   
////                                   JsfUtil.addWarningMessage("Email " + correo + " es inválido.");
////                                }
//
//                            }
            }
        }

        if (subComponentId != null) {
            try {
                subComponent = getJpaController().find(SubComponente.class, subComponentId);
            } catch (Exception e) {
                subComponent = null;
                System.out.println("component not found!");
            }
        }

        if (c != null) {
            if (subComponent != null) {
                ProductoContratadoPK productoContratadoPK = new ProductoContratadoPK(c.getIdCliente(),
                        subComponent.getIdComponente().getIdProducto().getIdProducto(),
                        subComponent.getIdComponente().getIdComponente(), subComponent.getIdSubComponente());

                try {
                    ProductoContratado pc = getJpaController().find(ProductoContratado.class, productoContratadoPK);

//                System.out.println(pc);
                    if (pc == null) {
                        //do not exist, save it.

                        if (!map.containsKey(productoContratadoPK.toString())) {

//                        System.out.println("PUT");
                            pc = new ProductoContratado(productoContratadoPK);
                            pc.setCliente(c);
                            pc.setTipoAsociacion(bulkLoadedProductoContratadoTipoAsoc);
//                        pc.setProducto(subComponent.getIdComponente().getIdProducto());
//                        pc.setComponente(subComponent.getIdComponente());
//                        pc.setSubComponente(subComponent);
//                        List<ProductoContratado> contratados = new ArrayList<ProductoContratado>();
//                        contratados.add(pc);
//                        c.setProductoContratadoList(contratados);

                            for (EmailCliente emailCliente : c.getEmailClienteList()) {
                                for (Caso caso : emailCliente.getCasoList()) {
                                    caso.setIdProducto(subComponent.getIdComponente().getIdProducto());
                                    caso.setIdComponente(subComponent.getIdComponente());
                                    caso.setIdSubComponente(subComponent);
                                }
                            }

                            map.put(productoContratadoPK.toString(), pc);
                        }

                    }
                } catch (Exception e) {
                }

            } else {
//                            bulkLoadedSubComponentNotExists.put(subComponentId, subComponent);
                bulkLoadedSubComponentNotExists++;
            }
        } else {
//                        bulkLoadedClientsNotExists.put(rut, c);
            bulkLoadedClientsNotExists++;
        }
    }

    public void handleFileUpload() {
        handleFileUpload(null);
    }

    public void handleFileUpload(FileUploadEvent event) {

        String email_regexp = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";
        Pattern p = Pattern.compile(email_regexp);

//        fileClients = event.getFile();
        if (fileClients != null) {

            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");

            Workbook w;
            try {
                w = Workbook.getWorkbook(fileClients.getInputstream(), ws);
                // Get the first sheet
                Sheet sheet = w.getSheet(0);
                // Loop over first 10 column and lines

                Map<String, Cliente> bulkLoadedClientsMap = new HashMap<>();
                Map<String, Cliente> bulkLoadedClientsErrorMap = new HashMap<>();

                String rut = "";
                String nombres = "";
                String apellidos = "";
                String correo = "";
                String sexo = "";
                String direccion1 = "";
                String fono1 = "";
                String fono2 = "";

                for (int rowIndex = 1; rowIndex < sheet.getRows(); rowIndex++) {

                    try {
                        rut = sheet.getCell(CellReferenceHelper.getColumn(cellPositionRut), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    try {
                        nombres = sheet.getCell(CellReferenceHelper.getColumn(cellPositionNombre), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    try {
                        apellidos = sheet.getCell(CellReferenceHelper.getColumn(cellPositionApellidos), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    try {
                        correo = sheet.getCell(CellReferenceHelper.getColumn(cellPositionCorreo), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    try {
                        sexo = sheet.getCell(CellReferenceHelper.getColumn(cellPositionSexo), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    try {
                        direccion1 = sheet.getCell(CellReferenceHelper.getColumn(cellPositionDireccion1), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }

                    try {
                        fono1 = sheet.getCell(CellReferenceHelper.getColumn(cellPositionFono1), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    try {
                        fono2 = sheet.getCell(CellReferenceHelper.getColumn(cellPositionFono2), rowIndex).getContents();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }

                    String moreValidEmail = StringUtils.isEmpty(correo) ? "" : correo.toLowerCase().trim();
                    String formattedRut = StringUtils.isEmpty(rut) ? "" : UtilesRut.formatear(rut);
                    EmailCliente ec = new EmailCliente(moreValidEmail);
                    //Match the given string with the pattern
                    Matcher m = p.matcher(moreValidEmail);

                    //Check whether match is found
                    boolean matchFound = m.matches();
                    if (!matchFound) {
                        ec = null;
                    }
                    if (UtilesRut.validar(rut)) {
                        addClientTo(formattedRut, ec, apellidos, direccion1, nombres, sexo, bulkLoadedClientsMap, fono1, fono2);
                    } else {
                        addClientTo(rut, ec, apellidos, direccion1, nombres, sexo, bulkLoadedClientsErrorMap, fono1, fono2);
                    }
                }

                bulkLoadedClients = new ArrayList<>(bulkLoadedClientsMap.values());
                bulkLoadedClientsErrors = new ArrayList<>(bulkLoadedClientsErrorMap.values());
                addInfoMessage("Archivo " + fileClients.getFileName() + "cargado exitósamente");
            } catch (Exception e) {
                addErrorMessage(e.getMessage());
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "handleFileUploadClienteProd", e);
//                e.printStackTrace();
            }

//            FacesMessage msg = new FacesMessage("Archivo cargado exitósamente", fileClients.getFileName());
//            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            FacesMessage msg = new FacesMessage("Error al subir el archivo, intente nuevamente");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

    }

    private void addClientTo(String rut, EmailCliente ec, String apellidos, String direccion1, String nombres, String sexo, Map<String, Cliente> map, String fono1, String fono2) {
        if (ec == null) {
            Cliente cl = new Cliente();
            cl.setApellidos(apellidos);
            cl.setFono1(fono1);
            cl.setFono2(fono2);
            cl.setDirParticular(direccion1);
            cl.setNombres(nombres);
            cl.setRut(rut);
            if (sexo != null && (sexo.equalsIgnoreCase("Hombre") || sexo.equalsIgnoreCase("Mujer"))) {
                cl.setSexo(sexo);
            } else if (sexo != null && (sexo.equalsIgnoreCase("M") || sexo.equalsIgnoreCase("F"))) {
                cl.setSexo(sexo.equalsIgnoreCase("M") ? "Hombre" : "Mujer");
            } else if (sexo != null && (sexo.equalsIgnoreCase("MASCULINO") || sexo.equalsIgnoreCase("FEMENINO"))) {
                cl.setSexo(sexo.equalsIgnoreCase("MASCULINO") ? "Hombre" : "Mujer");
            } else {
                cl.setSexo("Desconocido");
            }

            map.put(cl.getRut(), cl);

        } else {
            if ((!StringUtils.isEmpty(rut)) && map.containsKey(rut)) {

                List<EmailCliente> emails = map.get(rut).getEmailClienteList();
                if (!emails.contains(ec)) {
                    emails.add(ec);
                }

            } else {
                Cliente cl = new Cliente();
                cl.setApellidos(apellidos);
                cl.setFono1(fono1);
                cl.setFono2(fono2);
                cl.setDirParticular(direccion1);
                cl.setNombres(nombres);
                cl.setRut(rut);
                if (sexo != null && (sexo.equalsIgnoreCase("Hombre") || sexo.equalsIgnoreCase("Mujer"))) {
                    cl.setSexo(sexo);
                } else if (sexo != null && (sexo.equalsIgnoreCase("M") || sexo.equalsIgnoreCase("F"))) {
                    cl.setSexo(sexo.equalsIgnoreCase("M") ? "Hombre" : "Mujer");
                } else if (sexo != null && (sexo.equalsIgnoreCase("MASCULINO") || sexo.equalsIgnoreCase("FEMENINO"))) {
                    cl.setSexo(sexo.equalsIgnoreCase("MASCULINO") ? "Hombre" : "Mujer");
                } else {
                    cl.setSexo("Desconocido");
                }
                ec.setCliente(cl);
                List<EmailCliente> emails = new ArrayList<>();
                emails.add(ec);
                cl.setEmailClienteList(emails);
                map.put(cl.getRut(), cl);
            }
        }

    }

    public void onBlurRutInput() {
//        System.out.println("formatea");
        String rutFormateado = UtilesRut.formatear(getSelected().getCliente().getRut());
        getSelected().getCliente().setRut(rutFormateado);

        Cliente c = getJpaController().findClienteByRut(rutFormateado);
        if (c != null) {//this client exists

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "El rut " + rutFormateado + " pertenece a un cliente existente, favor verificar que el cliente ya exista.", null);
            FacesContext.getCurrentInstance().addMessage("form:rut", message);

        }
//        if (!getClient().getRut().isEmpty()) {
//            if (!UtilesRut.validar(getClient().getRut())) {
//                JsfUtil.addErrorMessage("El Rut es inválido.");
//            }
//        }
    }

    public String actualizarEmailSeleccionado() {
        System.out.println("actualizarEmailSeleccionado pressed!!");
        String email = getSelected().getEmailCliente();
        EmailCliente emailCliente = getJpaController().find(EmailCliente.class, email);
        if (emailCliente != null) {
            current = emailCliente;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Email: " + emailCliente.getEmailCliente() + " ya existe en los registros de clientes.", "");
            FacesContext.getCurrentInstance().addMessage(null, message);
            if (emailCliente.getCliente() != null) {
                current.setCliente(emailCliente.getCliente());
                FacesMessage message3 = new FacesMessage(FacesMessage.SEVERITY_INFO, emailCliente.getCliente().getNombres() + " ya existe en los registros de clientes.", "");
                FacesContext.getCurrentInstance().addMessage(null, message3);
                setCanCreate(false);
            } else {
                FacesMessage message2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "No tenemos datos del cliente favor ingrese los datos.", "");
                FacesContext.getCurrentInstance().addMessage(null, message2);
                current.setCliente(new Cliente());
                setCanCreate(true);
            }
        } else {
            EmailCliente newEmailCliente = new EmailCliente();
            newEmailCliente.setCliente(new Cliente());
            newEmailCliente.setEmailCliente(email);
            current = newEmailCliente;
            setCanCreate(true);
            FacesMessage message4 = new FacesMessage(FacesMessage.SEVERITY_WARN, "No tenemos datos asociados al mail " + email + " favor ingrese los datos.", "");
            FacesContext.getCurrentInstance().addMessage(null, message4);
        }
        return null;
    }

    @Override
    public String prepareList() {
        recreateModel();
        recreatePagination();
        return "/script/emailCliente/List";
    }

    @Override
    public String prepareView(EmailCliente c) {
        current = c;
        return "/script/emailCliente/View";
    }

    public String prepareView() {
        if (current == null) {
            return "";
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/emailCliente/View";
    }

    public String prepareCreate() {
        current = new EmailCliente();
        current.setCliente(new Cliente());
        setCanCreate(false);
        selectedItemIndex = -1;
        return "/script/emailCliente/Create";
    }

    public String prepareCreateMasivo() {
        return "/script/emailCliente/cargarClientes";
    }

    public String saveBulkImport() {
        try {
            int total = bulkLoadedClients != null ? bulkLoadedClients.size() : 0;
            Integer[] results = getJpaController().persistManyClients(bulkLoadedClients);
            JsfUtil.addSuccessMessage(results[0] + " Clientes fueron guardados exitosamente, " + results[1] + " Clientes ya existian en la BBDD, " + results[2] + " Clientes con error al guardar. De un total de " + total);
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
            return null;
        }
    }

    public String create() {
        try {
            current.setEmailCliente(current.getEmailCliente().toLowerCase().trim());
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmailClienteCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public boolean puedeVer(EmailCliente item) {
        return item != null;
    }

    public boolean puedeEliminar(EmailCliente item) {
        return item.getCasoList().isEmpty();
    }

    @Override
    public String prepareEdit(EmailCliente item) {
        try {
            EmailCliente i = getJpaController().find(EmailCliente.class, item.getEmailCliente());
            setSelected(i);

        } catch (Exception ex) {
            Logger.getLogger(EmailClienteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "/script/emailCliente/Edit";
    }

//    public String prepareEdit() {
//        if (current == null) {
//            return "";
//        }
//        if (current.getCliente() == null) {
//            current.setCliente(new Cliente());
//        }
//
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        return "/script/emailCliente/Edit";
//    }
    public String update() {
        try {
            current.setEmailCliente(current.getEmailCliente().toLowerCase().trim());
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmailClienteUpdated"));
            return "/script/emailCliente/View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public void updateListener() {
        try {
//            getJpaController().merge(current.getCliente());
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmailClienteUpdated"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "/script/emailCliente/List";
    }

    public String destroySelected() {

        if (current == null) {
            return "";
        }

        performDestroy();

//        try {
//            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        } finally {
//            recreateModel();
//        }
        return "/script/emailCliente/List";
    }

//    public String destroyAndView() {
//        performDestroy();
//        recreateModel();
//        updateCurrentItem();
//        if (selectedItemIndex >= 0) {
//            return "/script/emailCliente/View";
//        } else {
//            // all items were removed - go back to list
//            recreateModel();
//            return "/script/emailCliente/List";
//        }
//    }
    private void performDestroy() {
        try {
            getJpaController().remove(EmailCliente.class, current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmailClienteDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    private void updateCurrentItem() {
//        int count = getJpaController().count(EmailCliente.class).intValue();
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = (EmailCliente) getJpaController().queryByRange(EmailCliente.class, 1, selectedItemIndex).get(0);
//        }
//    }
//
//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//        return items;
//    }
//    private void recreateModel() {
//        items = null;
//    }
//
//    public String next() {
//        getPagination().nextPage();
//        recreateModel();
//        return "/script/emailCliente/List";
//    }
//
//    public String previous() {
//        getPagination().previousPage();
//        recreateModel();
//        return "/script/emailCliente/List";
//    }
//
//    public String last() {
//        getPagination().lastPage();
//        recreateModel();
//        return "/script/emailClienteList";
//    }
//
//    public String first() {
//        getPagination().firstPage();
//        recreateModel();
//        return "/script/emailCliente/List";
//    }
//
//    public void resetPageSize() {
//        pagination = null;
//        recreateModel();
//    }
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getEmailClienteFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getEmailClienteFindAll(), true);
//    }
    /**
     * @return the canCreate
     */
    public boolean isCanCreate() {
        return canCreate;
    }

    /**
     * @param canCreate the canCreate to set
     */
    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    /**
     * @return the fileClients
     */
    public UploadedFile getFileClients() {
        return fileClients;
    }

    /**
     * @param fileClients the fileClients to set
     */
    public void setFileClients(UploadedFile fileClients) {
        this.fileClients = fileClients;
    }

    /**
     * @return the cellPositionNombre
     */
    public String getCellPositionNombre() {
        return cellPositionNombre;
    }

    /**
     * @param cellPositionNombre the cellPositionNombre to set
     */
    public void setCellPositionNombre(String cellPositionNombre) {
        this.cellPositionNombre = cellPositionNombre;
    }

    /**
     * @return the cellPositionApellidos
     */
    public String getCellPositionApellidos() {
        return cellPositionApellidos;
    }

    /**
     * @param cellPositionApellidos the cellPositionApellidos to set
     */
    public void setCellPositionApellidos(String cellPositionApellidos) {
        this.cellPositionApellidos = cellPositionApellidos;
    }

    /**
     * @return the cellPositionCorreo
     */
    public String getCellPositionCorreo() {
        return cellPositionCorreo;
    }

    /**
     * @param cellPositionCorreo the cellPositionCorreo to set
     */
    public void setCellPositionCorreo(String cellPositionCorreo) {
        this.cellPositionCorreo = cellPositionCorreo;
    }

    /**
     * @return the cellPositionSexo
     */
    public String getCellPositionSexo() {
        return cellPositionSexo;
    }

    /**
     * @param cellPositionSexo the cellPositionSexo to set
     */
    public void setCellPositionSexo(String cellPositionSexo) {
        this.cellPositionSexo = cellPositionSexo;
    }

    /**
     * @return the cellPositionDireccion1
     */
    public String getCellPositionDireccion1() {
        return cellPositionDireccion1;
    }

    /**
     * @param cellPositionDireccion1 the cellPositionDireccion1 to set
     */
    public void setCellPositionDireccion1(String cellPositionDireccion1) {
        this.cellPositionDireccion1 = cellPositionDireccion1;
    }

    /**
     * @return the cellPositionRut
     */
    public String getCellPositionRut() {
        return cellPositionRut;
    }

    /**
     * @param cellPositionRut the cellPositionRut to set
     */
    public void setCellPositionRut(String cellPositionRut) {
        this.cellPositionRut = cellPositionRut;
    }

    /**
     * @return the bulkLoadedClients
     */
    public List<Cliente> getBulkLoadedClients() {
        return bulkLoadedClients;
    }

    /**
     * @param bulkLoadedClients the bulkLoadedClients to set
     */
    public void setBulkLoadedClients(List<Cliente> bulkLoadedClients) {
        this.bulkLoadedClients = bulkLoadedClients;
    }

    /**
     * @return the bulkLoadedClientsErrors
     */
    public List<Cliente> getBulkLoadedClientsErrors() {
        return bulkLoadedClientsErrors;
    }

    /**
     * @param bulkLoadedClientsErrors the bulkLoadedClientsErrors to set
     */
    public void setBulkLoadedClientsErrors(List<Cliente> bulkLoadedClientsErrors) {
        this.bulkLoadedClientsErrors = bulkLoadedClientsErrors;
    }

    /**
     * @return the searchPattern
     */
    public String getSearchPattern() {
        return searchPattern;
    }

    /**
     * @param searchPattern the searchPattern to set
     */
    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    /**
     * @return the currentProductoContratado
     */
    public ProductoContratado getCurrentProductoContratado() {
        return currentProductoContratado;
    }

    /**
     * @param currentProductoContratado the currentProductoContratado to set
     */
    public void setCurrentProductoContratado(ProductoContratado currentProductoContratado) {
        this.currentProductoContratado = currentProductoContratado;
    }

    /**
     * @return the cellPositionSubComponentId
     */
    public String getCellPositionSubComponentId() {
        return cellPositionSubComponentId;
    }

    /**
     * @param cellPositionSubComponentId the cellPositionSubComponentId to set
     */
    public void setCellPositionSubComponentId(String cellPositionSubComponentId) {
        this.cellPositionSubComponentId = cellPositionSubComponentId;
    }

    /**
     * @return the bulkLoadedProductoContratado
     */
    public List<ProductoContratado> getBulkLoadedProductoContratado() {
        return bulkLoadedProductoContratado;
    }

    /**
     * @param bulkLoadedProductoContratado the bulkLoadedProductoContratado to
     * set
     */
    public void setBulkLoadedProductoContratado(List<ProductoContratado> bulkLoadedProductoContratado) {
        this.bulkLoadedProductoContratado = bulkLoadedProductoContratado;
    }

    /**
     * @return the createClientIfNotExist
     */
    public boolean isCreateClientIfNotExist() {
        return createClientIfNotExist;
    }

    /**
     * @param createClientIfNotExist the createClientIfNotExist to set
     */
    public void setCreateClientIfNotExist(boolean createClientIfNotExist) {
        this.createClientIfNotExist = createClientIfNotExist;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the bulkLoadedProductoContratadoTipoAsoc
     */
    public String getBulkLoadedProductoContratadoTipoAsoc() {
        return bulkLoadedProductoContratadoTipoAsoc;
    }

    /**
     * @param bulkLoadedProductoContratadoTipoAsoc the
     * bulkLoadedProductoContratadoTipoAsoc to set
     */
    public void setBulkLoadedProductoContratadoTipoAsoc(String bulkLoadedProductoContratadoTipoAsoc) {
        this.bulkLoadedProductoContratadoTipoAsoc = bulkLoadedProductoContratadoTipoAsoc;
    }

    /**
     * @return the cellPositionFono1
     */
    public String getCellPositionFono1() {
        return cellPositionFono1;
    }

    /**
     * @param cellPositionFono1 the cellPositionFono1 to set
     */
    public void setCellPositionFono1(String cellPositionFono1) {
        this.cellPositionFono1 = cellPositionFono1;
    }

    /**
     * @return the cellPositionFono2
     */
    public String getCellPositionFono2() {
        return cellPositionFono2;
    }

    /**
     * @param cellPositionFono2 the cellPositionFono2 to set
     */
    public void setCellPositionFono2(String cellPositionFono2) {
        this.cellPositionFono2 = cellPositionFono2;
    }

    @FacesConverter(forClass = EmailCliente.class)
    public static class EmailClienteControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "UserSessionBean");
            return controller.getJpaController().find(EmailCliente.class, getKey(value));
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(java.lang.String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof EmailCliente) {
                EmailCliente o = (EmailCliente) object;
                return getStringKey(o.getEmailCliente());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + EmailCliente.class.getName());
            }
        }
    }
}

class EmailClienteDataModel extends ListDataModel<EmailCliente> implements SelectableDataModel<EmailCliente> {

    public EmailClienteDataModel() {
        //nothing
    }

    public EmailClienteDataModel(List<EmailCliente> data) {
        super(data);
    }

    @Override
    public EmailCliente getRowData(String rowKey) {
        List<EmailCliente> listOfEmailCliente = (List<EmailCliente>) getWrappedData();

        for (EmailCliente obj : listOfEmailCliente) {
            if (obj.getEmailCliente().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(EmailCliente classname) {
        return classname.getEmailCliente();
    }
}
