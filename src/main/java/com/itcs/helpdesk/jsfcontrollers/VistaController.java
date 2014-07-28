package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.FiltroAcceso;
import com.itcs.helpdesk.jsfcontrollers.util.JPAFilterHelper;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Caso_;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;

@ManagedBean(name = "vistaController")
@SessionScoped
public class VistaController extends AbstractManagedBean<Vista> implements Serializable {

//    @ManagedProperty(value = "#{UserSessionBean}")
//    private UserSessionBean userSessionBean;
//    private Vista current;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    @ManagedProperty(value = "#{UserSessionBean}")
    protected UserSessionBean userSessionBean;
    @ManagedProperty(value = "#{filtroAcceso}")
    private FiltroAcceso filtroAcceso;
    private int selectedItemIndex;
    List<Vista> allMyVistas = null;
//    private Vista listFilter;
    //---
    private Integer visibilityOption = 1;
    //--
//    private int pageSize = 20;
//    private transient JPAFilterHelper filterHelper;
    private transient JPAFilterHelper filterHelper2;

    private static Comparator<Vista> comparadorVistas = new Comparator<Vista>() {
        @Override
        public int compare(Vista o1, Vista o2) {
            return o1.getNombre().compareTo(o2.getNombre());
        }
    };

    public VistaController() {
        super(Vista.class);
//        listFilter = new Vista(Vista.class);
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    @Override
    public String getDefaultOrderBy() {
        return ("nombre");
    }

    @Override
    public Usuario getDefaultUserWho() {
        return userSessionBean.getCurrent();
    }

//    public Vista getSelected() {
//        if (current == null) {
//            current = new Vista(Caso.class);
//            selectedItemIndex = -1;
//        }
//        return current;
//    }
//    public void setSelected(Vista v) {
//        this.current = v;
//    }
//    @Override
//    public JPAFilterHelper getFilterHelper() {
//        if (filterHelper == null) {
//            filterHelper = new JPAFilterHelper(listFilter, emf) {
//                @Override
//                public JPAServiceFacade getJpaService() {
//                    return getJpaController();
//                }
//            };
//        }
//        return filterHelper;
//    }
    /**
     * This one is not used to filter the data displayed, is used to create a
     * View with filters.
     *
     * @return
     */
    public JPAFilterHelper getFilterHelper2() {
        if (filterHelper2 == null) {
            filterHelper2 = new JPAFilterHelper(getSelected(), emf) {
                @Override
                public JPAServiceFacade getJpaService() {
                    return getJpaController();
                }
            };
        }
        return filterHelper2;
    }

//    @Override
//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(getPaginationPageSize()) {
//                @Override
//                public int getItemsCount() {
//                    try {
//                        return getJpaController().countEntities(getFilterHelper().getVista(), getDefaultUserWho()).intValue();// getJpaController().count(Caso.class).intValue();
//                    } catch (ClassNotFoundException ex) {
//                        Logger.getLogger(CasoController.class.getName()).log(Level.SEVERE, "ClassNotFoundException at getItemsCount", ex);
//                    }
//                    return 0;
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    if (listFilter.getFiltrosVistaList() != null && !listFilter.getFiltrosVistaList().isEmpty()) {
//                        try {
//                            return new ListDataModel(getJpaController().findEntities(Vista.class, listFilter, getPageSize(), getPageFirstItem(), new OrderBy("nombre"), userSessionBean.getCurrent()));
//                        } catch (NotSupportedException ex) {
//                            JsfUtil.addErrorMessage(ex, "Error on public List<?> findEntities(Class entityClass, Vista vista, int maxResults, int firstResult)");
//                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//                        } catch (ClassNotFoundException ex) {
//                            JsfUtil.addErrorMessage(ex, "Error on public List<?> findEntities(Class entityClass, Vista vista, int maxResults, int firstResult)");
//                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                    return new ListDataModel(getJpaController().queryByRange(Vista.class, getPageSize(), getPageFirstItem()));
//                }
////                @Override
////                public DataModel createPageDataModel() {
////                    return new ListDataModel(getJpaController().getVistaJpaController().findVistasOwnedBy(userSessionBean.getCurrent(), false, getPageSize(), getPageFirstItem()));//findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
////                }
//            };
//        }
//        return pagination;
//    }
    public String prepareList() {
        recreateModel();
        return "/script/vista/List";
    }

    public String prepareView() {
        current = (Vista) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/vista/View";
    }

    public String prepareCreate() {
        filterHelper2 = null;
        current = new Vista(Caso.class);
        selectedItemIndex = -1;
        return "/script/vista/Create";
    }

    public void handleAnyChangeEvent() {
    }

    public int countCasosForView(Vista vista) {

        try {
            return getJpaController().countEntities(vista, userSessionBean.getCurrent()).intValue();
        } catch (Exception ex) {
            addErrorMessage(ex.getMessage());
            Logger.getLogger(VistaController.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }

    }

    public void create(Vista view) throws RollbackFailureException, Exception {
        visibilityOption = determineVisibility(view);

        view.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        switch (visibilityOption) {
            case Constants.VISIBILITY_ALL: //Visible to all option
                view.setIdArea(null);
                view.setIdGrupo(null);
                break;
            case Constants.VISIBILITY_GRUPO: // only Group option
                view.setIdArea(null);
                view.setVisibleToAll(false);
                break;
            case Constants.VISIBILITY_AREA: //Only Area Option
                view.setIdGrupo(null);
                view.setVisibleToAll(false);
                break;
            default: //Error

        }

        for (FiltroVista filtroVista : view.getFiltrosVistaList()) {
            filtroVista.setIdFiltro(null);
        }

        getJpaController().getVistaJpaController().create(view);

        JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VistaCreated"));

    }

    public String create() {
        try {
            if (filtroAcceso.verificarAccesoAFuncionAdministrarVistas()) {
                this.create(current);
                return prepareList();
            } else {
                addErrorMessage("No tiene privilegios para realizar esta operación!");
            }

        } catch (RollbackFailureException ex) {
            Logger.getLogger(VistaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(VistaController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Integer determineVisibility(Vista vista) {

        if (vista.getIdArea() != null) {
            return Constants.VISIBILITY_AREA;
        } else if (vista.getIdGrupo() != null) {
            return Constants.VISIBILITY_GRUPO;
        } else {
            return Constants.VISIBILITY_ALL;
        }

    }

    public String prepareEdit(Vista item) {
        current = item;
        this.filterHelper2 = null;//recreate filter helper
        visibilityOption = determineVisibility(current);
        return "/script/vista/Edit";
    }

    public String update() {
        try {

            if (filtroAcceso.verificarAccesoAFuncionAdministrarVistas()) {
                switch (visibilityOption) {
                    case Constants.VISIBILITY_ALL: //Visible to all option
                        current.setIdArea(null);
                        current.setIdGrupo(null);
                        break;
                    case Constants.VISIBILITY_GRUPO: // only Group option
                        current.setIdArea(null);
                        current.setVisibleToAll(false);
                        break;
                    case Constants.VISIBILITY_AREA: //Only Area Option
                        current.setIdGrupo(null);
                        current.setVisibleToAll(false);
                        break;
                    default: //Error

                }

                for (FiltroVista filtroVista : current.getFiltrosVistaList()) {
                    if (filtroVista.getIdFiltro() < 0) {
                        filtroVista.setIdFiltro(null);
                    }
                }
                getJpaController().getVistaJpaController().edit(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VistaUpdated"));
                return "/script/vista/List";
            } else {
                addErrorMessage("No tiene privilegios para realizar esta operación!");
            }

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return null;
    }

    public String destroy() {
//        current = (Vista) getItems().getRowData();
        if (current == null) {
            JsfUtil.addErrorMessage("Debe seleccionar una vista!");
            return null;
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/vista/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().getVistaJpaController().destroy(current.getIdVista());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VistaDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().count(Vista.class).intValue();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Vista) getJpaController().queryByRange(Vista.class, selectedItemIndex, selectedItemIndex + 1).get(0);
        }
    }


    public List<Vista> getVistasCustomersItems() {
        List<Vista> lista = new ArrayList<Vista>();

        if (userSessionBean.getEmailCliente() != null) {
            Vista miscasosAbiertos = new Vista(Caso.class);
            miscasosAbiertos.setIdVista(1);//just a random id to know which is which
            miscasosAbiertos.setNombre("Solicitudes abiertas");

            //customer view!
//            vista1.setIdUsuarioCreadaPor(EnumUsuariosBase.SISTEMA.getUsuario());
            FiltroVista filtroEmailCliente = new FiltroVista();
            filtroEmailCliente.setIdFiltro(1);//otherwise i dont know what to remove dude.
            filtroEmailCliente.setIdCampo("emailCliente");
            filtroEmailCliente.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEmailCliente.setValor(userSessionBean.getEmailCliente().getEmailCliente());
            filtroEmailCliente.setIdVista(miscasosAbiertos);

            miscasosAbiertos.getFiltrosVistaList().add(filtroEmailCliente);

            FiltroVista filtroEstado = new FiltroVista();
            filtroEstado.setIdFiltro(2);//otherwise i dont know what to remove dude.
            filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
            filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
            filtroEstado.setIdVista(miscasosAbiertos);

            miscasosAbiertos.getFiltrosVistaList().add(filtroEstado);

            lista.add(miscasosAbiertos);

            Vista miscasosCerrados = new Vista(Caso.class);
            miscasosAbiertos.setIdVista(2);
            miscasosCerrados.setNombre("Solicitudes cerradas");

            //customer view!
//            vista1.setIdUsuarioCreadaPor(EnumUsuariosBase.SISTEMA.getUsuario());
            miscasosCerrados.getFiltrosVistaList().add(filtroEmailCliente);

            FiltroVista filtroEstadoCerrado = new FiltroVista();
            filtroEstadoCerrado.setIdFiltro(2);//otherwise i dont know what to remove dude.
            filtroEstadoCerrado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
            filtroEstadoCerrado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEstadoCerrado.setValor(EnumEstadoCaso.CERRADO.getEstado().getIdEstado());
            filtroEstadoCerrado.setIdVista(miscasosCerrados);

            miscasosCerrados.getFiltrosVistaList().add(filtroEstadoCerrado);

            lista.add(miscasosCerrados);
        }

        return lista;
    }

    public void resetVistas() {
        allMyVistas = null;
    }

    public List<Vista> getAllVistasItems() {

        if (allMyVistas == null) {
            List<Vista> lista = new ArrayList<Vista>();
            final List<Vista> visibleForAllItems = getVisibleForAllItems();
            final List<Vista> visibleForMeOnlyItems = getVisibleForMeOnlyItems();
            final List<Vista> visibleForMyGroupsItems = getVisibleForMyGroupsItems();
            final List<Vista> visibleForMyAreasItems = getVisibleForMyAreasItems();

            if (visibleForAllItems != null) {
                for (Vista vista : visibleForAllItems) {
                    if (!lista.contains(vista)) {
                        lista.addAll(visibleForAllItems);
                    }
                }
            }

            if (visibleForMeOnlyItems != null) {
                for (Vista vista : visibleForMeOnlyItems) {
                    if (!lista.contains(vista)) {
                        lista.addAll(visibleForMeOnlyItems);
                    }
                }
            }

            if (visibleForMyGroupsItems != null) {
                for (Vista vista : visibleForMyGroupsItems) {
                    if (!lista.contains(vista)) {
                        lista.addAll(visibleForMyGroupsItems);
                    }
                }
            }

            if (visibleForMyAreasItems != null) {
                for (Vista vista : visibleForMyAreasItems) {
                    if (!lista.contains(vista)) {
                        lista.addAll(visibleForMyAreasItems);
                    }
                }
            }
            Collections.sort(lista, comparadorVistas);
            this.allMyVistas = lista;
        }

        return this.allMyVistas;
    }

    /**
     * @deprecated @return
     */
    public List<Vista> getVisibleForAllItems() {
        return getJpaController().getVistaJpaController().findVistaEntitiesVisibleForAll();
    }

    /**
     * @deprecated @return
     */
    public List<Vista> getVisibleForMeOnlyItems() {
        return getJpaController().getVistaJpaController().findVistaEntitiesCreatedByUser(userSessionBean.getCurrent(), true, 0, 0);
    }

    /**
     * @deprecated @return
     */
    public List<Vista> getVisibleForMyGroupsItems() {
        return getJpaController().getVistaJpaController().findVistaEntitiesVisibleForGroupsOfUser(userSessionBean.getCurrent());
    }

    /**
     * @deprecated @return
     */
    public List<Vista> getVisibleForMyAreasItems() {
        return getJpaController().getVistaJpaController().findVistaEntitiesVisibleForAreasOfUser(userSessionBean.getCurrent());
    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    /**
     * @return the visibilityOption
     */
    public Integer getVisibilityOption() {
        return visibilityOption;
    }

    /**
     * @param visibilityOption the visibilityOption to set
     */
    public void setVisibilityOption(Integer visibilityOption) {
        this.visibilityOption = visibilityOption;
    }

//    public Object getVista() {
//        return this.listFilter;
//    }
    /**
     * @param filtroAcceso the filtroAcceso to set
     */
    public void setFiltroAcceso(FiltroAcceso filtroAcceso) {
        this.filtroAcceso = filtroAcceso;
    }

    @Override
    public Class getDataModelImplementationClass() {
        return VistaDataModel.class;
    }

    @FacesConverter(forClass = Vista.class)
    public static class VistaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            VistaController controller = (VistaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "vistaController");
            return controller.getJpaController().getVistaJpaController().findVista(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Vista) {
                Vista o = (Vista) object;
                return getStringKey(o.getIdVista());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Vista.class.getName());
            }
        }
    }
}

class VistaDataModel extends ListDataModel<Vista> implements SelectableDataModel<Vista>, Serializable {

    public VistaDataModel() {
        //nothing
    }

    public VistaDataModel(List<Vista> data) {
        super(data);
    }

    @Override
    public Vista getRowData(String rowKey) {
        List<Vista> list = (List<Vista>) getWrappedData();

        if (list != null) {
            for (Vista obj : list) {
                if (obj.getIdVista().toString().equals(rowKey)) {
                    return obj;
                }
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Vista classname) {
        return classname.getIdVista();
    }
}
