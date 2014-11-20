/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.FiltroAcceso;
import com.itcs.helpdesk.jsfcontrollers.util.JPAFilterHelper;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.ReportChart;
import com.itcs.helpdesk.persistence.entities.ReportChartFilter;
import com.itcs.helpdesk.persistence.entities.ReportChartSerie;
import com.itcs.helpdesk.persistence.jpa.exceptions.RollbackFailureException;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.util.Constants;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.ListDataModel;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.model.SelectableDataModel;
import javax.faces.application.Application;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.dashboard.Dashboard;
import org.primefaces.component.panel.Panel;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "reportChartController")
@SessionScoped
public class ReportChartController extends AbstractManagedBean<ReportChart> implements Serializable {

//    private DashboardModel model;
    public static final int DEFAULT_COLUMN_COUNT = 3;
    private FiltroVista filtroBaseSeries = new FiltroVista();
//    private int columnCount = DEFAULT_COLUMN_COUNT;

    private Dashboard dashboard;

    @ManagedProperty(value = "#{filtroAcceso}")
    private FiltroAcceso filtroAcceso;

    private Integer visibilityOption = 1;
    private int selectedItemIndex;
    private JPAFilterHelper filterHelperForSelectedEntityType;

    @Override
    public OrderBy getDefaultOrderBy() {
        return new OrderBy("columnIndex", OrderBy.OrderType.ASC);
    }

    public ReportChartController() {
        super(ReportChart.class);
    }

    @PostConstruct
    public void init() {

        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        dashboard = (Dashboard) application.createComponent(fc, "org.primefaces.component.Dashboard", "org.primefaces.component.DashboardRenderer");
        dashboard.setId("dashboard");

        DashboardModel model = new DefaultDashboardModel();
        for (int i = 0; i < DEFAULT_COLUMN_COUNT; i++) {
            DashboardColumn column = new DefaultDashboardColumn();
            model.addColumn(column);
        }
        dashboard.setModel(model);

        int i = 0;
        Iterator itr = getItems().iterator();
        while (itr.hasNext()) {
            ReportChart chart = (ReportChart) itr.next();

            Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel", "org.primefaces.component.PanelRenderer");
            panel.setId("id_" + chart.getIdChartReport().toString());
            panel.setHeader(chart.getNombre());
            panel.setClosable(true);
            panel.setToggleable(true);
            panel.setFooter("c:" + chart.getColumnIndex() + ", i:" + chart.getItemIndex());

            DashboardColumn column;
            getDashboard().getChildren().add(panel);
            if (chart.getColumnIndex() != null) {
                column = model.getColumn(chart.getColumnIndex());
            } else {
                column = model.getColumn(i % DEFAULT_COLUMN_COUNT);
            }
            if (chart.getItemIndex() != null) {
                column.addWidget(chart.getItemIndex(), panel.getId());
            } else {
                column.addWidget(panel.getId());
            }
            HtmlOutputText text = new HtmlOutputText();
            CommandButton commandButton = new CommandButton();
            commandButton.setAjax(false);
            commandButton.setIconPos("notext");
            commandButton.setIcon("ui-icon ui-icon-pencil");

            FacesContext context = FacesContext.getCurrentInstance();
            Application app = context.getApplication();
            ExpressionFactory f = app.getExpressionFactory();
            MethodExpression expr = f.createMethodExpression(context.getELContext(),
                    "#{reportChartController.prepareEditPK(" + chart.getIdChartReport() + ")}", String.class, new Class[]{Long.class});

            commandButton.setActionExpression(expr);
            text.setValue(chart.getNombre());
            panel.getChildren().add(text);
            panel.getChildren().add(commandButton);
            i++;

        }

    }

    public void handleReorder(DashboardReorderEvent event) {

        final DashboardModel model = dashboard.getModel();

        for (int c = 0; c < model.getColumnCount(); c++) {
            for (int i = 0; i < model.getColumn(c).getWidgetCount(); i++) {
                try {
                    System.out.println(model.getColumn(c).getWidget(i) + " column:" + c + ", index:" + i);
                    String id = model.getColumn(c).getWidget(i).replace("id_", "");
                    Long idChart = Long.parseLong(id);

                    //maybe this find is not necesary but easier.
                    ReportChart chart = getJpaController().find(ReportChart.class, idChart);
                    chart.setColumnIndex(c);
                    chart.setItemIndex(i);

                    getJpaController().merge(chart);

                } catch (Exception ex) {
                    addErrorMessage(ex.getMessage());
                    Logger.getLogger(ReportChartController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

//        recreateModel();
//        init();
//        addInfoMessage("Reordered: " + event.getWidgetId() + " Item index: " + event.getItemIndex() + ", Column index: " + event.getColumnIndex() + ", Sender index: " + event.getSenderColumnIndex());
    }

//    public String prepareEditString(String idLong) {
//        if (idLong == null) {
//            addWarnMessage("Se requiere que nos indique el reporte a editar.");
//            return null;
//        }
//        final long parseLong = Long.parseLong(idLong);
//        return prepareEditPK(parseLong);
//    }
    public void handleAnyChangeEvent() {
    }
    
    @Override
    protected void recreateModel() {
        super.recreateModel(); //To change body of generated methods, choose Tools | Templates.
        init();
    }
    
    

    public JPAFilterHelper getFilterHelperForSelectedEntityType() {
        
        if (filterHelperForSelectedEntityType == null) {
            if (getSelected().getBaseEntityType() != null) {
                filterHelperForSelectedEntityType = new JPAFilterHelper(getSelected().getBaseEntityType(), emf) {
                    @Override
                    public JPAServiceFacade getJpaService() {
                        return getJpaController();
                    }
                };
            }

        } else {
            if (getSelected().getBaseEntityType() != null && !filterHelperForSelectedEntityType.getBaseEntityClassName().equals(getSelected().getBaseEntityType())) {
                filterHelperForSelectedEntityType = new JPAFilterHelper(getSelected().getBaseEntityType(), emf) {
                    @Override
                    public JPAServiceFacade getJpaService() {
                        return getJpaController();
                    }
                };
            }
        }
        return filterHelperForSelectedEntityType;
    }

    @Override
    protected String getListPage() {
        return "/script/reportChart/dashboard";
    }

    @Override
    protected String getViewPage() {
        return "/script/reportChart/View";
    }

    @Override
    protected String getEditPage() {
        return "/script/reportChart/Edit";
    }

    @Override
    protected void afterSetSelected() {
//        this.filterHelper2 = null;//recreate filter helper
        setVisibilityOption(determineVisibility(current));

        if (this.current != null) {
            filtroBaseSeries.setIdCampo(current.getIdCampoBase());
            filtroBaseSeries.setIdComparador(current.getIdComparador());
            filtroBaseSeries.setValor(current.getValor());
            filtroBaseSeries.setValor2(current.getValor2());
            filtroBaseSeries.setValorLabel(current.getValorLabel());
            filtroBaseSeries.setValor2Label(current.getValor2Label());
        }
    }

    public Integer determineVisibility(ReportChart chart) {

        if (chart.getIdArea() != null) {
            return Constants.VISIBILITY_AREA;
        } else if (chart.getIdGrupo() != null) {
            return Constants.VISIBILITY_GRUPO;
        } else {
            return Constants.VISIBILITY_ALL;
        }

    }

    public void create(ReportChart chart) throws RollbackFailureException, Exception {
        setVisibilityOption(determineVisibility(chart));

        chart.setIdUsuarioCreatedby(getUserSessionBean().getCurrent());
        switch (getVisibilityOption()) {
            case Constants.VISIBILITY_ALL: //Visible to all option
                chart.setIdArea(null);
                chart.setIdGrupo(null);
                break;
            case Constants.VISIBILITY_GRUPO: // only Group option
                chart.setIdArea(null);
                chart.setVisibleToAll(false);
                break;
            case Constants.VISIBILITY_AREA: //Only Area Option
                chart.setIdGrupo(null);
                chart.setVisibleToAll(false);
                break;
            default: //Error

        }
        //reset ids
        if (chart.getReportChartSerieList() != null) {
            for (ReportChartSerie reportChartSerie : chart.getReportChartSerieList()) {
                reportChartSerie.setIdChartSerie(null);
            }
        }

        if (chart.getReportChartFilterList() != null) {
            for (ReportChartFilter reportChartFilter : chart.getReportChartFilterList()) {
                reportChartFilter.setIdReportChartFilter(null);
            }
        }

        Date now = new Date();
        chart.setFechaCreacion(now);
        chart.setFechaModif(now);
        chart.setIdUsuarioCreatedby(getUserSessionBean().getCurrent());

        getJpaController().persist(chart);
        resetVistas();

        JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ReportChartCreated"));

    }

    public String create() {
        try {
            if (filtroAcceso.verificarAccesoAFuncionEditarAjustes()) {
                this.create(current);
                resetVistas();
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

    public String prepareCreate() {
        current = new ReportChart();
        selectedItemIndex = -1;
        return "/script/reportChart/Create";
    }

    public String prepareView() {
        current = (ReportChart) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/reportChart/View";
    }

    public String update() {
        try {

            if (filtroAcceso.verificarAccesoAFuncionEditarAjustes()) {
                switch (getVisibilityOption()) {
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

                if (this.filtroBaseSeries != null) {
                    current.setIdCampoBase(filtroBaseSeries.getIdCampo());
                    current.setIdComparador(filtroBaseSeries.getIdComparador());
                    current.setValor(filtroBaseSeries.getValor());
                    current.setValor2(filtroBaseSeries.getValor2());
                    current.setValorLabel(filtroBaseSeries.getValorLabel());
                    current.setValor2Label(filtroBaseSeries.getValor2Label());
                }

                if (current.getReportChartSerieList() != null) {
                    for (ReportChartSerie reportChartSerie : current.getReportChartSerieList()) {
                        if (reportChartSerie.getIdChartSerie() < 0) {
                            reportChartSerie.setIdChartSerie(null);
                        }
                    }
                }

                if (current.getReportChartFilterList() != null) {
                    for (ReportChartFilter reportChartFilter : current.getReportChartFilterList()) {
                        if (reportChartFilter.getIdReportChartFilter() < 0) {
                            reportChartFilter.setIdReportChartFilter(null);
                        }
                    }
                }

                Date now = new Date();
                current.setFechaModif(now);
                getJpaController().merge(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ReportChartUpdated"));
                return prepareList();
            } else {
                addErrorMessage("No tiene privilegios para realizar esta operación!");
            }

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return null;
    }

    @Override
    protected Class getDataModelImplementationClass() {
        return ReportChartDataModel.class;
    }

    /**
     * @param filtroAcceso the filtroAcceso to set
     */
    public void setFiltroAcceso(FiltroAcceso filtroAcceso) {
        this.filtroAcceso = filtroAcceso;
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

//    /**
//     * @return the columnCount
//     */
//    public int getColumnCount() {
//        return columnCount;
//    }
//
//    /**
//     * @param columnCount the columnCount to set
//     */
//    public void setColumnCount(int columnCount) {
//        this.columnCount = columnCount;
//    }
    /**
     * @return the dashboard
     */
    public Dashboard getDashboard() {
        return dashboard;
    }

    /**
     * @param dashboard the dashboard to set
     */
    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    /**
     * @return the filtroBaseSeries
     */
    public FiltroVista getFiltroBaseSeries() {
        return filtroBaseSeries;
    }

    /**
     * @param filtroBaseSeries the filtroBaseSeries to set
     */
    public void setFiltroBaseSeries(FiltroVista filtroBaseSeries) {
        this.filtroBaseSeries = filtroBaseSeries;
    }

    @FacesConverter(forClass = ReportChart.class)
    public static class ReportChartControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ReportChartController controller = (ReportChartController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "reportChartController");
            return controller.getJpaController().find(ReportChart.class, getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ReportChart) {
                ReportChart o = (ReportChart) object;
                return getStringKey(o.getIdChartReport());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ReportChart.class.getName()});
                return null;
            }
        }

    }
}

class ReportChartDataModel extends ListDataModel<ReportChart> implements SelectableDataModel<ReportChart>, Serializable {

    public ReportChartDataModel() {
        //nothing
    }

    public ReportChartDataModel(List<ReportChart> data) {
        super(data);
    }

    @Override
    public ReportChart getRowData(String rowKey) {
        List<ReportChart> list = (List<ReportChart>) getWrappedData();

        if (list != null) {
            for (ReportChart obj : list) {
                if (obj.getIdChartReport().toString().equals(rowKey)) {
                    return obj;
                }
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(ReportChart classname) {
        return classname.getIdChartReport();
    }
}
