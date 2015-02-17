package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Caso_;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Rol;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.util.Log;
import com.itcs.helpdesk.util.UtilSecurity;
import com.itcs.helpdesk.util.UtilesRut;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.MeterGaugeChartModel;

@ManagedBean(name = "usuarioController")
@SessionScoped
public class UsuarioController extends AbstractManagedBean<Usuario> implements Serializable {

    public static final String SIN_PROPIETARIO = "sin_propietario";
//    private Usuario current;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;
    private int selectedItemIndex;
    private Rol rol = new Rol();
    private List<Rol> roles = new ArrayList<>();
    private transient DualListModel<Grupo> gruposDualListModel = new DualListModel<>();
//    private MeterGaugeChartModel meterGaugeModel;
    private String idUsuarioDelete;
    private transient CartesianChartModel casosClosedVsOpenModel;
    private long maxValueForCasosClosedVsOpenModel = 0;
    private int casosClosedVsOpenYearfrom = Calendar.getInstance().get(Calendar.YEAR);
    private int casosClosedVsOpenYearTo = Calendar.getInstance().get(Calendar.YEAR);
    private String searchPattern;

    public UsuarioController() {
        super(Usuario.class);
    }

    /**
     * @return the gruposDualListModel
     */
    public DualListModel<Grupo> getGruposDualListModel() {
        return gruposDualListModel;
    }

    /**
     * @param gruposDualListModel the gruposDualListModel to set
     */
    public void setGruposDualListModel(DualListModel<Grupo> gruposDualListModel) {
        this.gruposDualListModel = gruposDualListModel;
    }

    public MeterGaugeChartModel getMeterGaugeModel() {

        Long count = 0L;
        List<Number> intervals = new ArrayList<Number>() {
            {
                add(15);
                add(30);
                add(45);
                add(60);
            }
        };

        try {

            Vista vista1 = new Vista(Caso.class);
            vista1.setNombre("Count Casos open by user");

            FiltroVista filtroOwner = new FiltroVista();
            filtroOwner.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroOwner.setIdCampo(Caso_.OWNER_FIELD_NAME);
            filtroOwner.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroOwner.setValor(userSessionBean.getCurrent().getIdUsuario());
            filtroOwner.setIdVista(vista1);
            vista1.getFiltrosVistaList().add(filtroOwner);

            FiltroVista filtroEstado = new FiltroVista();
            filtroEstado.setIdFiltro(2);//otherwise i dont know what to remove dude.
            filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
            filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
            filtroEstado.setIdVista(vista1);
            vista1.getFiltrosVistaList().add(filtroEstado);

            count = getJpaController().countEntities(vista1);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "ClassNotFoundException", ex);
        }

        return new MeterGaugeChartModel(count, intervals);
    }

//    public Usuario getSelected() {
////        //System.out.println("getSelected: " + current);
//        if (current == null) {
//            current = new Usuario();
//            selectedItemIndex = -1;
//        }
//        return current;
//    }
// public void setSelected(Usuario selected) {
//        //System.out.println("setSelected(" + selected + ")");
//        if (selected == null) {
//            return;
//        }
//        this.current = selected;
//    }
//    public List<Caso> getCasoAbiertoList() {
//        //System.out.println("getCasoAbiertoList");
//        Vista vista = new Vista(Caso.class);
//        if (vista.getFiltrosVistaList() == null) {
//            vista.setFiltrosVistaList(new ArrayList<FiltroVista>());
//
//        }
//
//        FiltroVista ownerFilter = new FiltroVista();
//        ownerFilter.setIdCampo(Caso_.OWNER_FIELD_NAME);
//        ownerFilter.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
//        ownerFilter.setValor(getSelected().getIdUsuario());
//        ownerFilter.setIdVista(vista);
//        vista.getFiltrosVistaList().add(ownerFilter);
//
//        FiltroVista estadoFilter = new FiltroVista();
//        estadoFilter.setIdCampo(Caso_.ESTADO_FIELD_NAME);
//        estadoFilter.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
//        estadoFilter.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
//        estadoFilter.setIdVista(vista);
//        vista.getFiltrosVistaList().add(estadoFilter);
//        try {
//            return (List<Caso>)jpaController.findAllEntities(Caso.class, vista, null, ((UserSessionBean) JsfUtil.getManagedBean("UserSessionBean")).getCurrent());
//        } catch (Exception ex) {
//            JsfUtil.addErrorMessage(ex.getMessage());
//            Logger.getLogger(UsuarioController.class.getName()).log(Level.SEVERE, null, ex);
//            return null;
//        } 
//    }
    public String getIdUsuarioDelete() {
        return idUsuarioDelete;
    }

    public void setIdUsuarioDelete(String idUsuarioDelete) {
        //System.out.println("idUsuario: " + idUsuarioDelete);
        this.idUsuarioDelete = idUsuarioDelete;
        Usuario usuario = getJpaController().find(Usuario.class, idUsuarioDelete);
        setSelected(usuario);
    }

    public String prepareView() {
        if (current == null) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila.");
            return null;
        }
        roles = (List<Rol>) current.getRolList();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Usuario();
        current.setSupervisor(userSessionBean.getCurrent());
        selectedItemIndex = -1;
        setGruposDualListModel(new DualListModel<>((List<Grupo>) getJpaController().findAll(Grupo.class), new ArrayList<Grupo>()));

        return "/script/usuario/Create";
    }

    public void formateaRut() {
        if (getSelected().getRut() != null && !StringUtils.isEmpty(getSelected().getRut())) {
            String rutFormateado = UtilesRut.formatear(getSelected().getRut());
            getSelected().setRut(rutFormateado);
        }

//        validaRut();
    }

    public void validaRut() {
        if (!UtilesRut.validar(getSelected().getRut())) {
            JsfUtil.addErrorMessage("Rut invalido");
//            //System.out.println("Rut invalido");
        }
//        else {
//            //System.out.println("Rut valido");
//        }
    }

    public String create() {
        try {
            if (!StringUtils.isEmpty(current.getRut())) {
                if (!UtilesRut.validar(current.getRut())) {
                    JsfUtil.addErrorMessage("Rut invalido");
                    return null;
                }

                List<Usuario> usuariosExistentesConMismoRut = getJpaController().getUsuarioFindByRut(current.getRut());
                if (usuariosExistentesConMismoRut != null && usuariosExistentesConMismoRut.size() > 0) {
                    for (Usuario u : usuariosExistentesConMismoRut) {
                        if (!u.getIdUsuario().equals(current.getIdUsuario())) {
                            JsfUtil.addErrorMessage("Ya existe un usuario con el rut que intenta registrar");
                            return null;
                        }
                    }

                }
            }

            current.setPass(UtilSecurity.getMD5(current.getPass()));
            current.setEditable(true);
            current.setGrupoList(getGruposDualListModel().getTarget());

            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsuarioCreated"));
            return prepareList();
        } catch (Exception e) {
            //Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            e.printStackTrace();
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public void prepareViewUserInDialog(Usuario u) {
//        //System.out.println("prepareViewUserInDialog(" + u + ")");
        current = u;
        Map<String, Object> options = new HashMap<>();
        options.put("height", "600");
//        options.put("width", "800");
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        RequestContext.getCurrentInstance().openDialog("/script/usuario/View_1", options, null);
    }

//    public String prepareView(Usuario u) {
//        current = u;
//        return "/script/usuario/View";
//    }
    @Override
    protected String getViewPage() {
        return "/script/usuario/View";
    }

    @Override
    protected String getListPage() {
        return "/script/usuario/List";
    }

    @Override
    protected String getEditPage() {
        return "/script/usuario/Edit";
    }

    @Override
    protected void afterSetSelected() {
        current.setPass("");
        roles = (List<Rol>) current.getRolList();

        setGruposDualListModel(new DualListModel<>((List<Grupo>) getJpaController().findAll(Grupo.class), current.getGrupoList()));
        for (Grupo grup : current.getGrupoList()) {
            if (getGruposDualListModel().getSource().contains(grup)) {
                getGruposDualListModel().getSource().remove(grup);
            }
        }
    }

//    @Override
//    public String prepareEdit(Usuario u) {
//        current = u;
//        //System.out.println("prepareEdit " + current.getIdUsuario());
//        if (current == null) {
//            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila.");
//            return null;
//        }
//        afterSetSelected();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        return "/script/usuario/Edit";
//    }
//
//    public String prepareEdit() {
//        if (current == null) {
//            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila.");
//            return null;
//        }
//       afterSetSelected();
//
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        return "/script/usuario/Edit";
//    }
    public String prepareDelete() {
        String idUsuario = JsfUtil.getRequestParameter("idUsuario");
        setIdUsuarioDelete(idUsuario);
        return null;
    }

    public String update() {
        return this.update(current, true);
    }

    public String updateLight() {
        return this.update(current, false);
    }

    private String update(Usuario updatedUser, boolean updateFull) {
        try {
            if (!StringUtils.isEmpty(updatedUser.getRut()) && !UtilesRut.validar(updatedUser.getRut())) {
                JsfUtil.addErrorMessage("Rut invalido");
                return null;
            }
            if (!StringUtils.isEmpty(current.getRut())) {
                List<Usuario> usuariosExistentesConMismoRut = getJpaController().getUsuarioFindByRut(updatedUser.getRut());
                if (usuariosExistentesConMismoRut != null && usuariosExistentesConMismoRut.size() > 0) {
                    for (Usuario u : usuariosExistentesConMismoRut) {
                        if (!u.getIdUsuario().equals(updatedUser.getIdUsuario())) {
                            JsfUtil.addErrorMessage("Ya existe un usuario con el rut que intenta registrar");
                            return null;
                        }
                    }

                }
            }

            Usuario usrOld = getJpaController().find(Usuario.class, updatedUser.getIdUsuario());
            //Si se cambio la password se actualiza si no, se deja la que ya está
            if (!updatedUser.getPass().trim().isEmpty()) {
                updatedUser.setPass(UtilSecurity.getMD5(updatedUser.getPass()));
            } else {
                updatedUser.setPass(usrOld.getPass());
            }

            updatedUser.setGrupoList(getGruposDualListModel().getTarget());
            if (updateFull) {
                getJpaController().mergeUsuarioFull(updatedUser);
            } else {
                getJpaController().merge(updatedUser);
            }

            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsuarioUpdated"));
            return prepareView();
        } catch (Exception e) {
            e.printStackTrace();
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public boolean puedeVer(Usuario item) {
        return item != null;
    }

    public boolean puedeEditar(Usuario item) {
        return (puedeVer(item) && item.isEditable());
    }

    public String destroy() {
        //System.out.println("destroy current:" + current);
        if (current == null) {
            return "";
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }

    public String destroySelected() {

        performDestroy();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        recreateModel();
        return "List";
    }

//    public String destroyAndView() {
//        performDestroy();
//        recreateModel();
//        updateCurrentItem();
//        if (selectedItemIndex >= 0) {
//            return "View";
//        } else {
//            // all items were removed - go back to list
//            recreateModel();
//            return "List";
//        }
//    }
    private void performDestroy() {
        try {
            getJpaController().remove(Usuario.class, current.getIdUsuario());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsuarioDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    private void updateCurrentItem() {
//        System.out.println("updateCurrentItem");
//        int count = getJpaController().count(Usuario.class).intValue();
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = (Usuario) getJpaController().queryByRange(Usuario.class, 1, selectedItemIndex).get(0);
//        }
//    }
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
//    public String last() {
//        getPagination().lastPage();
//        recreateModel();
//        return "List";
//    }
//
//    public String first() {
//        getPagination().firstPage();
//        recreateModel();
//        return "List";
//    }
//
//    public String next() {
//        getPagination().nextPage();
//        recreateModel();
//        return "List";
//    }
//
//    public String previous() {
//        getPagination().previousPage();
//        recreateModel();
//        return "List";
//    }
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getUsuarioFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getUsuarioFindAll(), true);
//    }
    public SelectItem[] getItemsAvailableSelectOneNoSystem() {
        List<Usuario> lista = (List<Usuario>) getJpaController().findAll(Usuario.class);
//        lista.remove(EnumUsuariosBase.SISTEMA.getUsuario());
        return JsfUtil.getSelectItems(lista, true);
    }

    public SelectItem[] getStringItemsAvailableSelectOneNoSystem() {
        List<Usuario> lista = (List<Usuario>) getJpaController().findAll(Usuario.class);
//        lista.remove(EnumUsuariosBase.SISTEMA.getUsuario());
        List<String> ids = new LinkedList<>();
        for (Usuario usuario : lista) {
            ids.add(usuario.getIdUsuario());
        }
        return JsfUtil.getSelectItems(ids, true);
    }

    public SelectItem[] getItemsAvailableSelectOneNoPropietario() {
        List<Usuario> lista = (List<Usuario>) getJpaController().findAll(Usuario.class);
//        lista.remove(EnumUsuariosBase.SISTEMA.getUsuario());
        lista.add(new Usuario(UsuarioController.SIN_PROPIETARIO, "Sin", "Propietario"));
        return JsfUtil.getSelectItems(lista, true);
    }

//    private void deleteThis() {
//        setMaxValueForCasosClosedVsOpenModel(0);
//        setCasosClosedVsOpenModel(new CartesianChartModel());
//
//        ChartSeries cerrados = new ChartSeries();
//        cerrados.setLabel("Cerrados");
//
//        ChartSeries creados = new ChartSeries();
//        creados.setLabel("Creados");
//        Calendar cal = Calendar.getInstance();
//
//        if (getCasosClosedVsOpenYearTo() > getCasosClosedVsOpenYearfrom()) {
//            //loop over each year, get casos created between start and end of the year.
//
//            for (int year = getCasosClosedVsOpenYearfrom(); year <= getCasosClosedVsOpenYearTo(); year++) {
//                cal.set(year, Calendar.JANUARY, 1);
//                cal.set(Calendar.HOUR_OF_DAY, 0);
//                cal.set(Calendar.MINUTE, 0);
//                cal.set(Calendar.SECOND, 0);
//                cal.set(Calendar.MILLISECOND, 0);
//
//                Date from = cal.getTime();
//
//                cal.set(year + 1, Calendar.JANUARY, 1);
//                cal.set(Calendar.HOUR_OF_DAY, 0);
//                cal.set(Calendar.MINUTE, 0);
//                cal.set(Calendar.SECOND, 0);
//                cal.set(Calendar.MILLISECOND, 0);
//
//                Date to = cal.getTime();
//
//                ////System.out.println("from:" + from);
//                ////System.out.println("To:" + to);
////                long count = getJpaController().countByCreatedBetween(Caso.class, from, to, idArea, idGrupo, idUsuario);
//                long count = getJpaController().countCasosByCreatedBetween(from, to, current);
//
//                creados.set(String.valueOf(year), count);
//                if (count >= getMaxValueForCasosClosedVsOpenModel()) {
//                    setMaxValueForCasosClosedVsOpenModel(count);
//                }
//                ////System.out.println("year:" + year + ", count:" + count);
//
//
////                long count2 = getJpaController().countByClosedBetween(Caso.class, from, to, idArea, idGrupo, idUsuario);
//                long count2 = getJpaController().countCasosByClosedBetween(from, to, current);
//
//                if (count2 >= getMaxValueForCasosClosedVsOpenModel()) {
//                    setMaxValueForCasosClosedVsOpenModel(count2);
//                }
//                cerrados.set(String.valueOf(year), count2);
//                ////System.out.println("year:" + year + ", count:" + count2);
//
//            }
//
//
//        } else if (getCasosClosedVsOpenYearTo() == getCasosClosedVsOpenYearfrom()) {
//            //loop over each MONTH of the year, get casos created between start and end of each month.
//            for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER; month++) {
//                cal.set(casosClosedVsOpenYearTo, month, 1);
//                cal.set(Calendar.HOUR_OF_DAY, 0);
//                cal.set(Calendar.MINUTE, 0);
//                cal.set(Calendar.SECOND, 0);
//                cal.set(Calendar.MILLISECOND, 0);
//
//                Date from = cal.getTime();
//
//                cal.set(casosClosedVsOpenYearfrom, month + 1, 1);
//                cal.set(Calendar.HOUR_OF_DAY, 0);
//                cal.set(Calendar.MINUTE, 0);
//                cal.set(Calendar.SECOND, 0);
//                cal.set(Calendar.MILLISECOND, 0);
//
//                Date to = cal.getTime();
//
//                ////System.out.println("from:" + from);
//                ////System.out.println("To:" + to);
//
////                long count = getJpaController().countByCreatedBetween(Caso.class, from, to, idArea, idGrupo, idUsuario);
//                long count = getJpaController().countCasosByCreatedBetween(from, to, current);
//
//                if (count >= getMaxValueForCasosClosedVsOpenModel()) {
//                    setMaxValueForCasosClosedVsOpenModel(count);
//                }
//                creados.set(GraphsManagedBean.traduceMonth(month), count);
//                ////System.out.println("month:" + month + ", count:" + count);
//
//
//                long count2 = getJpaController().countCasosByClosedBetween(from, to, current);
//
//                if (count2 >= getMaxValueForCasosClosedVsOpenModel()) {
//                    setMaxValueForCasosClosedVsOpenModel(count2);
//                }
//                cerrados.set(GraphsManagedBean.traduceMonth(month), count2);
//                ////System.out.println("month:" + month + ", count:" + count2);
//
//            }
//
//
//        } else {
//            //Error
//            JsfUtil.addErrorMessage("Hay un problema con el periodo seleccionado: " + getCasosClosedVsOpenYearfrom() + " - " + getCasosClosedVsOpenYearTo());
//        }
//
//        getCasosClosedVsOpenModel().addSeries(creados);
//        getCasosClosedVsOpenModel().addSeries(cerrados);
//
//    }
    /**
     * @return the casosClosedVsOpenYearfrom
     */
    public int getCasosClosedVsOpenYearfrom() {
        return casosClosedVsOpenYearfrom;
    }

    /**
     * @param casosClosedVsOpenYearfrom the casosClosedVsOpenYearfrom to set
     */
    public void setCasosClosedVsOpenYearfrom(int casosClosedVsOpenYearfrom) {
        this.casosClosedVsOpenYearfrom = casosClosedVsOpenYearfrom;
    }

    /**
     * @return the casosClosedVsOpenYearTo
     */
    public int getCasosClosedVsOpenYearTo() {
        return casosClosedVsOpenYearTo;
    }

    /**
     * @param casosClosedVsOpenYearTo the casosClosedVsOpenYearTo to set
     */
    public void setCasosClosedVsOpenYearTo(int casosClosedVsOpenYearTo) {
        this.casosClosedVsOpenYearTo = casosClosedVsOpenYearTo;
    }

    /**
     * @return the casosClosedVsOpenModel
     */
    public CartesianChartModel getCasosClosedVsOpenModel() {
        return casosClosedVsOpenModel;
    }

    /**
     * @param casosClosedVsOpenModel the casosClosedVsOpenModel to set
     */
    public void setCasosClosedVsOpenModel(CartesianChartModel casosClosedVsOpenModel) {
        this.casosClosedVsOpenModel = casosClosedVsOpenModel;
    }

    /**
     * @return the maxValueForCasosClosedVsOpenModel
     */
    public long getMaxValueForCasosClosedVsOpenModel() {
        return maxValueForCasosClosedVsOpenModel;
    }

    /**
     * @param maxValueForCasosClosedVsOpenModel the
     * maxValueForCasosClosedVsOpenModel to set
     */
    public void setMaxValueForCasosClosedVsOpenModel(long maxValueForCasosClosedVsOpenModel) {
        this.maxValueForCasosClosedVsOpenModel = maxValueForCasosClosedVsOpenModel;
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

    @Override
    public Class getDataModelImplementationClass() {
        return UsuarioDataModel.class;
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    @FacesConverter(forClass = Usuario.class)
    public static class UsuarioControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            if (UsuarioController.SIN_PROPIETARIO.equals(value)) {
                return new Usuario(UsuarioController.SIN_PROPIETARIO, "Sin", "Propietario");
            }
            UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "UserSessionBean");
            return controller.getJpaController().find(Usuario.class, getKey(value));
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
            if (object instanceof Usuario) {
                Usuario o = (Usuario) object;
                return getStringKey(o.getIdUsuario());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + UsuarioController.class.getName());
            }
        }
    }
}

class UsuarioDataModel extends ListDataModel<Usuario> implements SelectableDataModel<Usuario>, java.io.Serializable {

    public UsuarioDataModel() {
        //nothing
    }

    public UsuarioDataModel(List<Usuario> data) {
        super(data);
    }

    @Override
    public Usuario getRowData(String rowKey) {
        List<Usuario> listOfUsuario = (List<Usuario>) getWrappedData();

        for (Usuario obj : listOfUsuario) {
            if (obj.getIdUsuario().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Usuario classname) {
        return classname.getIdUsuario();
    }
}
