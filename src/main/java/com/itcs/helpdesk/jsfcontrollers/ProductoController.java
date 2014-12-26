package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Archivo;
import com.itcs.helpdesk.persistence.entities.Attachment;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Componente;
import com.itcs.helpdesk.persistence.entities.SubComponente;
import com.itcs.helpdesk.persistence.jpa.ComponenteJpaController;
import com.itcs.helpdesk.persistence.jpa.ProductoJpaController;
import com.itcs.helpdesk.persistence.jpa.SubComponenteJpaController;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import com.itcs.helpdesk.util.ApplicationConfig;
import com.itcs.helpdesk.util.Log;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.persistence.NoResultException;
import jxl.CellReferenceHelper;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@ManagedBean(name = "productoController")
@SessionScoped
public class ProductoController extends AbstractManagedBean<Producto> implements Serializable {

    @ManagedProperty(value = "#{componenteController}")
    private transient ComponenteController componenteController;
//    private Producto current;
    private Componente currentComponente = new Componente();
    private SubComponente currentSubComponente = new SubComponente();
//    private Producto[] selectedItems;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    private int selectedItemIndex;
    //cell postion, for buld load.
    private String cellPositionProduct = "";
    private String cellPositionComponent = "";
    private String cellPositionSubComponentId = "";
    private String cellPositionSubComponentName = "";
    private String cellPositionSubComponentDesc = "";
    private transient UploadedFile uploadFile;
    private List<Producto> bulkLoadedProductos;
    private List<Producto> bulkLoadedProductosWithErrors;
    private List<Componente> bulkLoadedComponentes;
    private List<Componente> bulkLoadedComponentesWithErrors;
    private List<SubComponente> bulkLoadedSubComponentes;
    private List<SubComponente> bulkLoadedSubComponenteWithErrors;
    transient Map<String, Producto> persistentProductsMap = new HashMap<String, Producto>();
    transient Map<String, Producto> bulkLoadedProductoMap = new HashMap<String, Producto>();
    transient Map<String, Componente> bulkLoadedComponenteMap = new HashMap<String, Componente>();
    transient Map<String, SubComponente> bulkLoadedSubComponenteMap = new HashMap<String, SubComponente>();

    private Long idFileDelete = null;
    private String currentSubComponenteBackOutcome;

    public ProductoController() {
        super(Producto.class);
    }

//    public Producto getSelected() {
//        if (current == null) {
//            current = new Producto();
//            selectedItemIndex = -1;
//        }
//        return current;
//    }
//
//    public void setSelected(Producto item) {
//        current = item;
//    }
    public String prepareCreateMasivo() {
        bulkLoadedProductosWithErrors = null;
        bulkLoadedSubComponenteWithErrors = null;
        bulkLoadedComponentesWithErrors = null;

        bulkLoadedProductoMap = new HashMap<>();
        bulkLoadedComponenteMap = new HashMap<>();
        bulkLoadedSubComponenteMap = new HashMap<>();
        return "/script/producto/bulkLoad";
    }

    public void handleFileUpload() {
        handleFileUpload(null);
    }

    public String getFileName(Long id) {
        try {
            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(id);
            return archivo.getFileName();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage("Ocurrió un error con el archivo, favor intente nuevamente.");
            return "";
        }
    }

    public StreamedContent findLogoStreamedContent(Long idLogo) {

        System.out.println("idLogo:" + idLogo);
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(idLogo);
            if (archivo != null) {
                return new DefaultStreamedContent(
                        new ByteArrayInputStream(archivo.getArchivo()), archivo.getContentType(), archivo.getFileName() + "/" + archivo.getContentType());
            } else {
                return new DefaultStreamedContent();
            }
        }
    }

    public StreamedContent getLogo() {
        return findLogoStreamedContent(getSelected().getIdLogo());
    }

    public void deleteLogoProducto(ActionEvent actionEvent) {
        try {

            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(idFileDelete);
            if (archivo != null) {
                getJpaController().remove(Archivo.class, archivo);
                getSelected().setIdLogo(null);
                JsfUtil.addSuccessMessage("Archivo " + archivo.getFileName() + " borrado correctamente.");
                executeInClient("PF('borrarLogo').hide()");
            } else {
                getSelected().setIdLogo(null);
                addErrorMessage("El archivo ya no existe");
                executeInClient("PF('borrarLogo').hide()");
            }

        } catch (Exception e) {
            addErrorMessage("Error, No se ha podido borrar el archivo", e.getMessage());
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido borrar el archivo", e);
        }
    }

    public void deleteArchivoActaEntrega(ActionEvent actionEvent) {
        try {

            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(idFileDelete);
            getJpaController().removeArchivo(archivo);
            currentSubComponente.setIdArchivoActaEntrega(null);

            JsfUtil.addSuccessMessage("Archivo " + archivo.getFileName() + " borrado correctamente.");

            executeInClient("PF('borrarArchivoActaEntrega').hide()");

        } catch (Exception e) {
            JsfUtil.addSuccessMessage("Error, No se ha podido borrar el archivo");
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido borrar el archivo", e);
        }
    }

    public void deleteArchivoActaPreEntrega(ActionEvent actionEvent) {
        try {

            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(idFileDelete);
            getJpaController().removeArchivo(archivo);
            currentSubComponente.setIdArchivoActaPreEntrega(null);

            JsfUtil.addSuccessMessage("Archivo " + archivo.getFileName() + " borrado correctamente.");

            executeInClient("PF('borrarArchivoActaPreEntrega').hide()");

        } catch (Exception e) {
            JsfUtil.addSuccessMessage("Error, No se ha podido borrar el archivo");
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido borrar el archivo", e);
        }
    }

    public void deleteArchivoCartaEntrega(ActionEvent actionEvent) {
        try {

            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(idFileDelete);
            getJpaController().removeArchivo(archivo);
            currentSubComponente.setIdArchivoCartaEntrega(null);

            JsfUtil.addSuccessMessage("Archivo " + archivo.getFileName() + " borrado correctamente.");

            executeInClient("PF('borrarArchivoCartaEntrega').hide()");

        } catch (Exception e) {
            JsfUtil.addSuccessMessage("Error, No se ha podido borrar el archivo");
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se ha podido borrar el archivo", e);
        }
    }

    public StreamedContent downloadArchivo(Long id) {
        try {
            Archivo archivo = getJpaController().getArchivoFindByIdAttachment(id);
            return new DefaultStreamedContent(
                    new ByteArrayInputStream(archivo.getArchivo()), archivo.getContentType(), archivo.getFileName());
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage("Ocurrio un error al bajar el archivo, favor intente nuevamente.");
            return new DefaultStreamedContent();
        }
    }

    private Archivo persistArchivo(FileUploadEvent event) throws IOException, RollbackFailureException, Exception {

        String nombre = event.getFile().getFileName();
        nombre = nombre.substring(nombre.lastIndexOf(File.separator) + 1);
        if (nombre.lastIndexOf('\\') > 0) {
            nombre = nombre.substring(nombre.lastIndexOf('\\') + 1);
        }
        InputStream is = event.getFile().getInputstream();
        long size = event.getFile().getSize();
        byte[] bytearray = new byte[(int) size];
        is.read(bytearray);

//            //System.out.println("CURRENT:"+this.current);
        String fileName = nombre.trim().replace(" ", "_");
        Archivo archivo = new Archivo();
        archivo.setIdAttachment(getJpaController().nextVal("attachment_id_attachment_seq"));
        archivo.setArchivo(bytearray);
        archivo.setContentType(event.getFile().getContentType());
        archivo.setFileName(fileName);
        archivo.setFormat(fileName.substring(fileName.lastIndexOf(".") + 1));
        getJpaController().persistArchivo(archivo);
        return archivo;
    }

    public void uploadLogoProducto(FileUploadEvent event) {
        try {
            Archivo archivo = persistArchivo(event);
            if (archivo != null && getSelected() != null) {
                getSelected().setIdLogo(archivo.getIdAttachment());
                getJpaController().merge(getSelected());
                JsfUtil.addSuccessMessage("Logo " + archivo.getFileName() + " subido con exito");
                executeInClient("PF('uploadLogoProyectoDialog').hide()");
            } else {
                JsfUtil.addErrorMessage("Ocurrio un error al guardar el archivo, favor intente nuevamente.");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage("Ocurrio un error al subir el archivo, favor intente nuevamente.");
        }
    }

    public void uploadActaEntrega(FileUploadEvent event) {
        try {
            Archivo archivo = persistArchivo(event);
            if (archivo != null && this.currentSubComponente != null) {
                currentSubComponente.setIdArchivoActaEntrega(archivo.getIdAttachment());
                getJpaController().getSubComponenteJpaController().edit(currentSubComponente);
                JsfUtil.addSuccessMessage("Archivo " + archivo.getFileName() + " subido con exito");
                executeInClient("PF('uploadDialogidArchivoActaEntrega').hide()");
            } else {
                JsfUtil.addErrorMessage("Ocurrio un error al guardar el archivo, favor intente nuevamente.");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage("Ocurrio un error al subir el archivo, favor intente nuevamente.");
        }
    }

    public void uploadActaPreEntrega(FileUploadEvent event) {
        try {
            Archivo archivo = persistArchivo(event);
            if (archivo != null && this.currentSubComponente != null) {
                currentSubComponente.setIdArchivoActaPreEntrega(archivo.getIdAttachment());
                getJpaController().getSubComponenteJpaController().edit(currentSubComponente);
                JsfUtil.addSuccessMessage("Archivo " + archivo.getFileName() + " subido con exito");
                executeInClient("PF('uploadDialogidArchivoActaPreEntrega').hide()");
            } else {
                JsfUtil.addErrorMessage("Ocurrio un error al guardar el archivo, favor intente nuevamente.");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage("Ocurrio un error al subir el archivo, favor intente nuevamente.");
        }
    }

    public void uploadCartaEntrega(FileUploadEvent event) {
        try {
            Archivo archivo = persistArchivo(event);
            if (archivo != null && this.currentSubComponente != null) {
                currentSubComponente.setIdArchivoCartaEntrega(archivo.getIdAttachment());
                getJpaController().getSubComponenteJpaController().edit(currentSubComponente);
                JsfUtil.addSuccessMessage("Archivo " + archivo.getFileName() + " subido con exito");
                executeInClient("PF('uploadDialogidArchivoCartaEntrega').hide()");
            } else {
                JsfUtil.addErrorMessage("Ocurrio un error al guardar el archivo, favor intente nuevamente.");
            }
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage("Ocurrio un error al subir el archivo, favor intente nuevamente.");
        }
    }

    public void handleFileUpload(FileUploadEvent event) {

        persistentProductsMap = new HashMap<>();
        int countSubComp = 0;
//        uploadFile = event.getFile();

        if (uploadFile != null) {

            FacesMessage msg = new FacesMessage("Archivo subido exitósamente", uploadFile.getFileName());
            FacesContext.getCurrentInstance().addMessage(null, msg);

            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");

            Workbook w;
            try {

                w = Workbook.getWorkbook(uploadFile.getInputstream(), ws);
                // Get the first sheet
                Sheet sheet = w.getSheet(0);
                // Loop over first 10 column and lines

                bulkLoadedProductoMap = new HashMap<>();

                bulkLoadedComponenteMap = new HashMap<>();

                bulkLoadedSubComponenteMap = new HashMap<>();

                for (int rowIndex = 1; rowIndex < sheet.getRows(); rowIndex++) {

                    String nombreProducto = "";
                    String idComponente = "";
                    String subComponentId = "";
                    String subComponentName = "";
                    String subComponentDesc = "";

                    try {
                        nombreProducto = sheet.getCell(CellReferenceHelper.getColumn(cellPositionProduct), rowIndex).getContents().trim();
                    } catch (Exception e) {
                        System.out.println("Error on cell " + cellPositionSubComponentDesc);
                    }
                    try {
                        idComponente = sheet.getCell(CellReferenceHelper.getColumn(cellPositionComponent), rowIndex).getContents().trim();
                    } catch (Exception e) {
                        System.out.println("Error on cell " + cellPositionSubComponentDesc);
                    }
                    try {
                        subComponentId = sheet.getCell(CellReferenceHelper.getColumn(cellPositionSubComponentId), rowIndex).getContents().trim();
                    } catch (Exception e) {
                        System.out.println("Error on cell " + cellPositionSubComponentDesc);
                    }
                    try {
                        subComponentName = sheet.getCell(CellReferenceHelper.getColumn(cellPositionSubComponentName), rowIndex).getContents().trim();
                    } catch (Exception e) {
                        System.out.println("Error on cell " + cellPositionSubComponentDesc);
                    }
                    try {
                        subComponentDesc = sheet.getCell(CellReferenceHelper.getColumn(cellPositionSubComponentDesc), rowIndex).getContents().trim();
                    } catch (Exception e) {
                        System.out.println("Error on cell " + cellPositionSubComponentDesc);
                    }

                    Producto producto;
                    if ((StringUtils.isEmpty(nombreProducto)) || (StringUtils.isEmpty(idComponente))){
                        continue;
                    }
                    try {
                        producto = getJpaController().getProductoFindByNombre(nombreProducto);
                        if (producto != null) {
                            if (!persistentProductsMap.containsKey(producto.getIdProducto())) {
                                persistentProductsMap.put(producto.getIdProducto(), producto);
                            }
                        } else {
                            producto = new Producto(nombreProducto);
                            producto.setNombre(nombreProducto);
                        }
                    } catch (NoResultException nr) {
                        producto = new Producto(nombreProducto);
                        producto.setNombre(nombreProducto);
                    }

                    //Check whether is found
                    if (!bulkLoadedProductoMap.containsKey(producto.getIdProducto())) {
                        bulkLoadedProductoMap.put(producto.getIdProducto(), producto);
                    } else {
                        producto = bulkLoadedProductoMap.get(producto.getIdProducto());
                    }

                    Componente componente = new Componente(idComponente);
                    componente.setIdProducto(producto);
                    componente.setNombre(idComponente);

                    //Check whether is found
                    if (!bulkLoadedComponenteMap.containsKey(idComponente)) {
                        bulkLoadedComponenteMap.put(componente.getIdComponente(), componente);
                    } else {
                        componente = bulkLoadedComponenteMap.get(idComponente);
                    }

                    if (producto.getComponenteList() == null) {
                        producto.setComponenteList(new ArrayList<Componente>());
                    }
                    if (!producto.getComponenteList().contains(componente)) {
                        producto.getComponenteList().add(componente);
                    }

                    SubComponente subComponente = new SubComponente(subComponentId);
                    subComponente.setNombre(subComponentName);
                    subComponente.setDescripcion(subComponentDesc);
                    subComponente.setIdComponente(componente);

                    //Check whether is found
                    if (!bulkLoadedSubComponenteMap.containsKey(subComponentId)) {
                        bulkLoadedSubComponenteMap.put(subComponentId, subComponente);
                    } else {
                        subComponente = bulkLoadedSubComponenteMap.get(subComponentId);
                    }

                    if (componente.getSubComponenteList() == null) {
                        componente.setSubComponenteList(new ArrayList<SubComponente>());
                    }
                    if (!componente.getSubComponenteList().contains(subComponente)) {
                        componente.getSubComponenteList().add(subComponente);
                        countSubComp++;
                    }

                }

                bulkLoadedProductos = new ArrayList<>(bulkLoadedProductoMap.values());

                bulkLoadedComponentes = new ArrayList<>(bulkLoadedComponenteMap.values());

                bulkLoadedSubComponentes = new ArrayList<>(bulkLoadedSubComponenteMap.values());

                System.out.println("countSubComp:" + countSubComp);

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Extracción de datos del Archivo finalizada.", uploadFile.getFileName()));

            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al tratar de cargar la informacion desde el archivo, intente nuevamente."));
                e.printStackTrace();
            }

        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al subir el archivo, intente nuevamente"));
        }

    }

    public boolean canEditProductId(Producto p) {
        if (persistentProductsMap.containsKey(p.getIdProducto())) {
            return false;
        } else {
            return true;
        }
    }

    public void handleChangeProductsName(Producto producto) {

        try {
            Producto persistent = getJpaController().getProductoFindByNombre(producto.getNombre());
            if (persistent != null) {//nombre exists
                if (!bulkLoadedProductoMap.containsKey(persistent.getIdProducto())) {//existent p is not in the list
                    //then modify the current product with persistent data.
                    producto.setIdProducto(persistent.getIdProducto());
                    producto.setNombre(persistent.getNombre());
                    producto.setDescripcion(persistent.getDescripcion());
                    producto.setComponenteList(persistent.getComponenteList());
                } else {
                    producto.setNombre("");
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("El nombre ingresado ya está siendo utilizado."));
                }
            }
        } catch (NoResultException nr) {//nombre do not exist
            //ignore
        }

    }

    public void handleChangeProductsId(Producto producto) {

        try {
            Producto persistent = getJpaController().find(Producto.class, producto.getIdProducto());
            if (persistent != null) {
                if (!bulkLoadedProductoMap.containsKey(persistent.getIdProducto())) {
                    producto.setIdProducto(persistent.getIdProducto());
                    producto.setNombre(persistent.getNombre());
                    producto.setComponenteList(persistent.getComponenteList());
                    producto.setDescripcion(persistent.getDescripcion());
                } else {
                    producto.setIdProducto("");
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("El Código ingresado ya está siendo utilizado."));
                }
            }
        } catch (NoResultException nr) {
            //ignore
        }
    }

    public void handleChangeProductsData() {
    }

    public String saveBulkImport() {
        System.out.println("saveBulkImport");
//        Map<String, Producto> bulkLoadedProductoErrorMap = new HashMap<String, Producto>();
//        Map<String, Componente> bulkLoadedComponenteErrorMap = new HashMap<String, Componente>();
//        Map<String, SubComponente> bulkLoadedSubComponenteErrorMap = new HashMap<String, SubComponente>();

        //--------
        bulkLoadedProductosWithErrors = new ArrayList<>();

        int counterproducto = 0;

        final ProductoJpaController pJpaController = getJpaController().getProductoJpaController();

        for (Producto producto : bulkLoadedProductos) {

            try {
                pJpaController.createOrMerge(producto);
                counterproducto++;
            } catch (Exception e) {
                bulkLoadedProductosWithErrors.add(producto);
                System.out.println("****** Error creating product:");
                e.printStackTrace();
            }
        }

        JsfUtil.addSuccessMessage(counterproducto + " " + ApplicationConfig.getProductDescription() + "(s) fueron guardados de un total de " + bulkLoadedProductos.size());
        if (bulkLoadedProductosWithErrors.size() > 0) {
            JsfUtil.addSuccessMessage(bulkLoadedProductosWithErrors.size() + " " + ApplicationConfig.getProductDescription() + "(s) tienen error" + bulkLoadedProductos.size());
        }

//        --------
        bulkLoadedComponentesWithErrors = new ArrayList<>();

        int counterComponente = 0;

//        final ComponenteJpaController cJpaController = getJpaController().getComponenteJpaController();
        for (Componente componente : bulkLoadedComponentes) {
            try {
                if (!StringUtils.isEmpty(componente.getIdComponente())) {
                    final Componente find = getJpaController().find(Componente.class, componente.getIdComponente());
                    if (find == null) {
                        getJpaController().persist(componente);
                        counterComponente++;
                    }
                }

            } catch (Exception e) {
                bulkLoadedComponentesWithErrors.add(componente);
                System.out.println("****** Error creating component:");
                e.printStackTrace();
            }
        }

        JsfUtil.addSuccessMessage(counterComponente + " " + ApplicationConfig.getProductComponentDescription() + "(s) fueron guardados de un total de " + bulkLoadedComponentes.size());
//        --------
        bulkLoadedSubComponenteWithErrors = new ArrayList<>();

        int counterSubComponente = 0;

//        final SubComponenteJpaController scJpaController = getJpaController().getSubComponenteJpaController();
        for (SubComponente subComponente : bulkLoadedSubComponentes) {
            try {
                if (!StringUtils.isEmpty(subComponente.getIdSubComponente())) {
                    if (StringUtils.isEmpty(subComponente.getNombre())) {
                        subComponente.setNombre(subComponente.getIdComponente().getNombre());
                    }
                    final SubComponente find = getJpaController().find(SubComponente.class, subComponente.getIdSubComponente());
                    //scJpaController.findSubComponente(subComponente.getIdSubComponente());

                    if (find == null) {
                        getJpaController().persist(subComponente);
                        counterSubComponente++;
                    }
                }

            } catch (Exception e) {
                bulkLoadedSubComponenteWithErrors.add(subComponente);
                System.out.println("****** Error creating subComponent:");
                e.printStackTrace();
            }
        }

        JsfUtil.addSuccessMessage(counterSubComponente + " " + ApplicationConfig.getProductSubComponentDescription() + "(s) fueron guardados de un total de " + bulkLoadedSubComponentes.size());
//        --------
        try {
            prepareList();
            FacesContext.getCurrentInstance().getExternalContext().redirect("List.xhtml");
            //        return prepareList();
        } catch (IOException ex) {
            Logger.getLogger(ProductoController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

//    @Override
//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(getPaginationPageSize()) {
//                @Override
//                public int getItemsCount() {
//                    return getJpaController().count(Producto.class).intValue();
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    return new ProductoDataModel(getJpaController().queryByRange(Producto.class, getPageSize(), getPageFirstItem()));
//                }
//            };
//        }
//        return pagination;
//    }
    public int getTotalItemsCount() {
        return getJpaController().count(Producto.class).intValue();
    }

    public int getTotalComponentsItemsCount() {
        return getJpaController().count(Componente.class).intValue();
    }

    public int getTotalSubComponentsItemsCount() {
        return getJpaController().count(SubComponente.class).intValue();
    }

    public String prepareView() {
        if (current == null) {
            JsfUtil.addSuccessMessage("Es requerido que seleccione una fila para visualizar.");
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/producto/View";
    }

    public String prepareCreate() {
        current = new Producto();
        selectedItemIndex = -1;
        return "/script/producto/Create";
    }

    public String create() {
        try {
            getJpaController().persistProducto(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProductoCreated"));
            return prepareEdit();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(Producto item) {
        setSelected(item);
        return prepareEdit();
    }

    public String prepareCreateComponente() {
        currentComponente = new Componente();
        currentComponente.setIdProducto(current);
        return null;
    }

    public String prepareCreateSubComponente() {
        currentSubComponente = new SubComponente();
        currentSubComponente.setIdComponente(currentComponente);
        return null;
    }

    public void prepareEditComponente(Componente item) {
        currentComponente = item;
    }

    public String prepareViewSubComponente(SubComponente item, String backOutcome) {
        if (item == null) {
            addWarnMessage("debe especificar un Producto");
            return null;
        }
        this.setCurrentSubComponenteBackOutcome(backOutcome);
        currentSubComponente = item;
        return "/script/producto/ViewSubComponente";
    }

    public void prepareEditSubComponente(SubComponente item) {
        currentSubComponente = item;
    }

    public void handleEditComponent() {
    }

    public void addComponentToProduct() {
        if (this.getSelected().getComponenteList() == null || this.getSelected().getComponenteList().isEmpty()) {
            this.getSelected().setComponenteList(new ArrayList<Componente>());
        }

        this.getSelected().getComponenteList().add(currentComponente);
        this.currentComponente = null;

    }

    public void addSubComponentToComponent() {
        if (this.getCurrentComponente().getSubComponenteList() == null || this.getCurrentComponente().getSubComponenteList().isEmpty()) {
            this.getCurrentComponente().setSubComponenteList(new ArrayList<SubComponente>());
        }

        this.getCurrentComponente().getSubComponenteList().add(currentSubComponente);
        this.currentSubComponente = null;

    }

    public String prepareList() {
        recreateModel();
        return "/script/producto/List";
    }

    public String prepareEdit() {
        if (current == null) {
            return prepareList();
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/producto/Edit";
    }

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProductoUpdated"));
            return prepareList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String updateCurrentSubComponente() {
        try {
            getJpaController().merge(currentSubComponente);
            JsfUtil.addSuccessMessage("Datos actualizados exitósamente.");
            return getCurrentSubComponenteBackOutcome();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return prepareList();
    }

    public String destroySelected() {

        if (current == null) {
            return "";
        }
        performDestroy();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        recreateModel();
        return "/script/producto/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/producto/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/producto/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeProducto(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProductoDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Producto.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Producto) getJpaController().queryByRange(Producto.class, 1, selectedItemIndex).get(0);
        }
    }

    /**
     * @param componenteController the componenteController to set
     */
    public void setComponenteController(ComponenteController componenteController) {
        this.componenteController = componenteController;
    }

    /**
     * @return the currentComponente
     */
    public Componente getCurrentComponente() {
        return currentComponente;
    }

    /**
     * @param currentComponente the currentComponente to set
     */
    public void setCurrentComponente(Componente currentComponente) {
        this.currentComponente = currentComponente;
    }

    /**
     * @return the currentSubComponente
     */
    public SubComponente getCurrentSubComponente() {
        return currentSubComponente;
    }

    /**
     * @param currentSubComponente the currentSubComponente to set
     */
    public void setCurrentSubComponente(SubComponente currentSubComponente) {
        this.currentSubComponente = currentSubComponente;
    }

    /**
     * @return the cellPositionProduct
     */
    public String getCellPositionProduct() {
        return cellPositionProduct;
    }

    /**
     * @param cellPositionProduct the cellPositionProduct to set
     */
    public void setCellPositionProduct(String cellPositionProduct) {
        this.cellPositionProduct = cellPositionProduct;
    }

    /**
     * @return the cellPositionComponent
     */
    public String getCellPositionComponent() {
        return cellPositionComponent;
    }

    /**
     * @param cellPositionComponent the cellPositionComponent to set
     */
    public void setCellPositionComponent(String cellPositionComponent) {
        this.cellPositionComponent = cellPositionComponent;
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
     * @return the cellPositionSubComponentName
     */
    public String getCellPositionSubComponentName() {
        return cellPositionSubComponentName;
    }

    /**
     * @param cellPositionSubComponentName the cellPositionSubComponentName to
     * set
     */
    public void setCellPositionSubComponentName(String cellPositionSubComponentName) {
        this.cellPositionSubComponentName = cellPositionSubComponentName;
    }

    /**
     * @return the cellPositionSubComponentDesc
     */
    public String getCellPositionSubComponentDesc() {
        return cellPositionSubComponentDesc;
    }

    /**
     * @param cellPositionSubComponentDesc the cellPositionSubComponentDesc to
     * set
     */
    public void setCellPositionSubComponentDesc(String cellPositionSubComponentDesc) {
        this.cellPositionSubComponentDesc = cellPositionSubComponentDesc;
    }

    /**
     * @return the bulkLoadedProductos
     */
    public List<Producto> getBulkLoadedProductos() {
        return bulkLoadedProductos;
    }

    /**
     * @param bulkLoadedProductos the bulkLoadedProductos to set
     */
    public void setBulkLoadedProductos(List<Producto> bulkLoadedProductos) {
        this.bulkLoadedProductos = bulkLoadedProductos;
    }

    /**
     * @return the bulkLoadedProductosWithErrors
     */
    public List<Producto> getBulkLoadedProductosWithErrors() {
        return bulkLoadedProductosWithErrors;
    }

    /**
     * @param bulkLoadedProductosWithErrors the bulkLoadedProductosWithErrors to
     * set
     */
    public void setBulkLoadedProductosWithErrors(List<Producto> bulkLoadedProductosWithErrors) {
        this.bulkLoadedProductosWithErrors = bulkLoadedProductosWithErrors;
    }

    /**
     * @return the bulkLoadedComponentes
     */
    public List<Componente> getBulkLoadedComponentes() {
        return bulkLoadedComponentes;
    }

    /**
     * @param bulkLoadedComponentes the bulkLoadedComponentes to set
     */
    public void setBulkLoadedComponentes(List<Componente> bulkLoadedComponentes) {
        this.bulkLoadedComponentes = bulkLoadedComponentes;
    }

    /**
     * @return the bulkLoadedComponentesWithErrors
     */
    public List<Componente> getBulkLoadedComponentesWithErrors() {
        return bulkLoadedComponentesWithErrors;
    }

    /**
     * @param bulkLoadedComponentesWithErrors the
     * bulkLoadedComponentesWithErrors to set
     */
    public void setBulkLoadedComponentesWithErrors(List<Componente> bulkLoadedComponentesWithErrors) {
        this.bulkLoadedComponentesWithErrors = bulkLoadedComponentesWithErrors;
    }

    /**
     * @return the bulkLoadedSubComponentes
     */
    public List<SubComponente> getBulkLoadedSubComponentes() {
        return bulkLoadedSubComponentes;
    }

    /**
     * @param bulkLoadedSubComponentes the bulkLoadedSubComponentes to set
     */
    public void setBulkLoadedSubComponentes(List<SubComponente> bulkLoadedSubComponentes) {
        this.bulkLoadedSubComponentes = bulkLoadedSubComponentes;
    }

    /**
     * @return the bulkLoadedSubComponenteWithErrors
     */
    public List<SubComponente> getBulkLoadedSubComponenteWithErrors() {
        return bulkLoadedSubComponenteWithErrors;
    }

    /**
     * @param bulkLoadedSubComponenteWithErrors the
     * bulkLoadedSubComponenteWithErrors to set
     */
    public void setBulkLoadedSubComponenteWithErrors(List<SubComponente> bulkLoadedSubComponenteWithErrors) {
        this.bulkLoadedSubComponenteWithErrors = bulkLoadedSubComponenteWithErrors;
    }

    /**
     * @return the uploadFile
     */
    public UploadedFile getUploadFile() {
        return uploadFile;
    }

    /**
     * @param uploadFile the uploadFile to set
     */
    public void setUploadFile(UploadedFile uploadFile) {
        this.uploadFile = uploadFile;
    }

    @Override
    public Class getDataModelImplementationClass() {
        return ProductoDataModel.class;
    }

    /**
     * @return the idFileDelete
     */
    public Long getIdFileDelete() {
        return idFileDelete;
    }

    /**
     * @param idFileDelete the idFileDelete to set
     */
    public void setIdFileDelete(Long idFileDelete) {
        this.idFileDelete = idFileDelete;
    }

    /**
     * @return the currentSubComponenteBackOutcome
     */
    public String getCurrentSubComponenteBackOutcome() {
        return currentSubComponenteBackOutcome;
    }

    /**
     * @param currentSubComponenteBackOutcome the
     * currentSubComponenteBackOutcome to set
     */
    public void setCurrentSubComponenteBackOutcome(String currentSubComponenteBackOutcome) {
        this.currentSubComponenteBackOutcome = currentSubComponenteBackOutcome;
    }

    @FacesConverter(forClass = Producto.class)
    public static class ProductoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ProductoController controller = (ProductoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "productoController");
            return controller.getJpaController().getProductoFindByIdProducto(getKey(value));
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
            if (object instanceof Producto) {
                Producto o = (Producto) object;
                return getStringKey(o.getIdProducto());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Producto.class.getName());
            }
        }
    }
}

class ProductoDataModel extends ListDataModel<Producto> implements SelectableDataModel<Producto> {

    public ProductoDataModel() {
        //nothing
    }

    public ProductoDataModel(List<Producto> data) {
        super(data);
    }

    @Override
    public Producto getRowData(String rowKey) {
        List<Producto> listOfProducto = (List<Producto>) getWrappedData();

        for (Producto obj : listOfProducto) {
            if (obj.getIdProducto().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Producto classname) {
        return classname.getIdProducto();
    }
}
