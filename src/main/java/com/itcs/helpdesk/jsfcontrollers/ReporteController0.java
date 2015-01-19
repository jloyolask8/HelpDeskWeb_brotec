/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JPAFilterHelper;
import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Caso_;
import com.itcs.helpdesk.persistence.entities.EstadoCaso;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Producto;
import com.itcs.helpdesk.persistence.entities.SubEstadoCaso;
import com.itcs.helpdesk.persistence.entities.TipoAlerta;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumFieldType;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.entityenums.EnumUsuariosBase;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.utils.ComparableField;
import com.itcs.helpdesk.util.DateUtils;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.resource.NotSupportedException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author jonathan
 */
@ManagedBean(name = "reporteController")
@SessionScoped
public class ReporteController0 extends AbstractManagedBean<Caso> implements Serializable {

    @ManagedProperty(value = "#{casoController}")
    transient private CasoController casoController;
    @ManagedProperty(value = "#{UserSessionBean}")
    transient protected UserSessionBean userSessionBean;
    @ManagedProperty(value = "#{vistaController}")
    transient protected VistaController vistaController;
    private boolean showFilter = false;

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    /**
     * @param vistaController the vistaController to set
     */
    public void setVistaController(VistaController vistaController) {
        this.vistaController = vistaController;
    }
    transient private PieChartModel pieModel = null;
    private String dataFormat = "value";
    private CartesianChartModel categoryModel = null;
    private String chartModelTitle = "";
    //================dynamic===
    private int activeLeftTab = 0;
    private boolean stacked = false;
    private boolean fill = false;
    private String tipoGraficoSelected = "pieChart"; //Other types are barChart
    private int variables = 1; // actualmente soporta solo graficos en una o dos variables.
    private Map<Integer, ArrayList<Vista>> viewMatrix;
    private String inboxUrl = "/script/index.xhtml";
//    private CampoCompCaso campoCompCasoEjeXSeriesEntity;
//    private CampoCompCaso campoCompCasoEjeYItemsEntity;
    private FiltroVista campoCompCasoEjeXSeriesEntity = new FiltroVista();
    private FiltroVista campoCompCasoEjeYItemsEntity = new FiltroVista();
    private Vista vistaBaseAxis;
    //================/dynamic===
    private String xaxisLabel = "";
    private String yaxisLabel = "";
    //comparable filds
//    public List<ComparableField> comparableFields;
//    public Map<String, ComparableField> comparableFieldsMap;
//    private JPAFilterHelper filterHelper;//Axis
//    private JPAFilterHelper filterHelper2;//Filters
    private Map<String, Map<String, Integer>> twoDimData = new HashMap<>();
    private Map<String, Integer> oneDimData = new HashMap<>();

    /**
     * Creates a new instance of ReporteController0
     */
    public ReporteController0() {
        super(Caso.class);
    }

    private void addOneDimTableValue(String label, Integer number) {
        if (label == null || oneDimData == null) {
            return;
        }
        if (oneDimData.containsKey(label)) {
            Integer oldNumber = oneDimData.get(label);
            Integer newNumber = oldNumber + number;
            oneDimData.put(label, newNumber);
        } else {
            oneDimData.put(label, number);
        }
    }

    public List<Map.Entry<String, Integer>> getOneDimList() {
        Set<Map.Entry<String, Integer>> set = oneDimData.entrySet();
        return new ArrayList<>(set);
    }

    public Integer calcularTotal(List<Map.Entry<String, Integer>> list) {
        Integer result = 0;
        for (Entry<String, Integer> entry : list) {
            result = result + entry.getValue();
        }
        return result;
    }

    public List<Map.Entry<String, List<Map.Entry<String, Integer>>>> getTwoDimList() {
        List<Map.Entry<String, List<Map.Entry<String, Integer>>>> lista = new ArrayList<>();

        Set<Map.Entry<String, Map<String, Integer>>> set = twoDimData.entrySet();
        List<Map.Entry<String, Map<String, Integer>>> xvaluesMap = new ArrayList<>(set);

        for (Map.Entry<String, Map<String, Integer>> xentry : xvaluesMap) {
            final Map<String, Integer> yvalueMap = xentry.getValue();
            Set<Map.Entry<String, Integer>> set2 = yvalueMap.entrySet();
            final ArrayList<Entry<String, Integer>> yvaluesList = new ArrayList<>(set2);

            lista.add(new AbstractMap.SimpleEntry<String, List<Map.Entry<String, Integer>>>(xentry.getKey(), yvaluesList));

        }

        return lista;
    }

    private void addTwoDimTableValue(String xlabel, String ylabel, Integer number) {
        if (xlabel == null || ylabel == null || twoDimData == null) {
            return;
        }

        if (twoDimData.containsKey(xlabel)) {
            final Map<String, Integer> yhashMap = twoDimData.get(xlabel);
            if (yhashMap.containsKey(ylabel)) {
                Integer oldNumber = yhashMap.get(ylabel);
                Integer newNumber = oldNumber + number;
                yhashMap.put(ylabel, newNumber);
            } else {
                yhashMap.put(ylabel, number);
            }
        } else {
            final HashMap<String, Integer> yhashMap = new HashMap<>();
            yhashMap.put(ylabel, number);
            twoDimData.put(xlabel, yhashMap);
        }

    }

    @PostConstruct
    private void doThisPostConstruct() {
        vistaBaseAxis = new Vista(Caso.class);
        vistaBaseAxis.getFiltrosVistaList().add(campoCompCasoEjeYItemsEntity);
        vistaBaseAxis.getFiltrosVistaList().add(campoCompCasoEjeXSeriesEntity);
//        comparableFields = getJpaController().getAnnotatedComparableFieldsByClass(Caso.class);
//        comparableFieldsMap = getJpaController().getAnnotatedComparableFieldsMap(comparableFields);
        preparePieModelEstadoCasos();
    }

    public void onTabChange(TabChangeEvent event) {
        //do not do a shit.
    }

    private void resetOptions() {
//        tipoGraficoSelected = "pieChart";//Reset
//        variables = 1; //One Dimention By Default
        pieModel = null;
        categoryModel = null;
        System.out.println("resetOptions()");
//        campoCompCasoEjeXSeriesEntity = new FiltroVista();
//        campoCompCasoEjeYItemsEntity = new FiltroVista();
    }

    public String prepareCreateReport() {
        resetOptions();

        return "reports";
    }

    public void handleIdCampoChangeEvent() {
        this.campoCompCasoEjeXSeriesEntity.setValor("");
        this.campoCompCasoEjeXSeriesEntity.setValor2("");
        this.campoCompCasoEjeXSeriesEntity.setValorLabel("");
        this.campoCompCasoEjeXSeriesEntity.setValor2Label("");
        this.campoCompCasoEjeXSeriesEntity.setIdComparador(null);
    }

    @Override
    public void filter() {
        generateModel();
    }

    public String generateModel() {
        try {
            if (campoCompCasoEjeXSeriesEntity != null) {

                final ComparableField campo = getFilterHelper().getComparableFieldsMap().get(campoCompCasoEjeXSeriesEntity.getIdCampo());
                setXaxisLabel(campo != null ? campo.getLabel() : "");

            }
            if (variables == 1) {
                createOneDimentionModel();
            } else if (variables == 2) {
                if (campoCompCasoEjeXSeriesEntity.getIdCampo().equals(getCampoCompCasoEjeYItemsEntity().getIdCampo())) {
                    JsfUtil.addErrorMessage("No Es Recomendable Usar la misma Dimensión en un gráfico de dos dimensiones.");
                    pieModel = null;
                    categoryModel = null;
                    return null;
                } else {
                    createTwoDimentionModel();
                }

            } else {
                JsfUtil.addErrorMessage("Numero de variables no soportado. solo podemos generar graficos en una y dos variables");
                return null;
            }

            this.showFilter = true;
            return "reports";
        } catch (Exception ex) {
            resetOptions();
            JsfUtil.addErrorMessage(ex.getMessage());
            Logger.getLogger(ReporteController0.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    private void createTwoDimentionModel() throws NotSupportedException, ClassNotFoundException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, Exception {
        //One Or Two Dimentions here
//        JPAServiceFacade jpa = getJpaController();
        twoDimData = new HashMap<>();
        variables = 2;

        EntityManager em = emf.createEntityManager();
        ComparableField comparableFieldY = getFilterHelper().getComparableFieldsMap().get(campoCompCasoEjeYItemsEntity.getIdCampo());
        ComparableField comparableFieldX = getFilterHelper().getComparableFieldsMap().get(campoCompCasoEjeXSeriesEntity.getIdCampo());
        setYaxisLabel("N° de Casos Por " + comparableFieldY.getLabel());

        if (tipoGraficoSelected.equalsIgnoreCase("barChart") || tipoGraficoSelected.equalsIgnoreCase("lineChart")) {
            pieModel = null;//Hide Pie chart
            categoryModel = new CartesianChartModel();
        } else {
            throw new NotSupportedException("Tipo de grafico no soportado para dos dimensiones!");
        }

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        try {

            for (SelectItem y : getFilterHelper().findPosibleOptionsSelectManyIncludeNullFor(comparableFieldY.getIdCampo(), userSessionBean.getCurrent())) {

                ChartSeries serie_y = new ChartSeries();
                serie_y.setLabel(y.getLabel());

                ArrayList<Vista> items_eje_x = new ArrayList<>();
                Class entity_x_type = comparableFieldX.getTipo();

                //Is the axis a Date|Calendar value? if yes then posible values are days of a period of days
                if (comparableFieldX.getFieldTypeId().getFieldTypeId().equalsIgnoreCase(EnumFieldType.CALENDAR.getFieldType().getFieldTypeId())) {

//                    int period = 1;
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", LOCALE_ES_CL);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", LOCALE_ES_CL);
//                    SimpleDateFormat dateFormatTZ = new SimpleDateFormat("dd/MM/yyyy Z", LOCALE_ES_CL);
                    Date from_date = format.parse(campoCompCasoEjeXSeriesEntity.getValor());
                    Date to_date = format.parse(campoCompCasoEjeXSeriesEntity.getValor2());

                    //less than one year
                    int daysBetween = Days.daysBetween(new DateTime(from_date), new DateTime(to_date)).getDays();

                    List<Interval> rangeStartDates = null;

                    if (daysBetween > 0) {
                        if (daysBetween <= 7) {
//                            period = 1;//Days
                            rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.DAY_OF_MONTH);
                            dateFormat = new SimpleDateFormat("dd MMM");
                        } else if (daysBetween <= 31) {
//                            period = 7;//Weeks
                            rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.WEEK_OF_MONTH);
                            dateFormat = new SimpleDateFormat("'w'ww MMM");
                        } else if (daysBetween <= 365) {
//                            period = 30;//Months, How Many? Calculate Range. which must be beetwen 1-12.
                            rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.MONTH);
                            dateFormat = new SimpleDateFormat("MMM yyyy");
                        } else if (daysBetween > 365) {
//                            period = 365;//Years, How Many? calculate range
                            rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.YEAR);
                            dateFormat = new SimpleDateFormat("yyyy");
                        }

                    } else {
                        throw new NotSupportedException("El rango de fechas no es valido!");
                    }

//                    Calendar calendar_from = Calendar.getInstance();
//                    calendar_from.setTime(from_date);
//                    Calendar calendar_to = Calendar.getInstance();
                    if (rangeStartDates != null) {
                        for (Interval interval : rangeStartDates) {

                            final Date startDate = interval.getStart().toDate();
                            final Date endDate = interval.getEnd().toDate();

                            String fecha_from = format.format(startDate);//ok to start any day i want for any period.
                            String fecha_to = format.format(endDate);//ok to start any day i want for any period.
                            String label = dateFormat.format(startDate);

                            //Create Vista to count items
                            Vista vista1 = new Vista(Caso.class);
                            if (vista1.getFiltrosVistaList() == null) {
                                vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());
                            }

                            //==================FECHA_CREACION/FECHA_X
                            FiltroVista filtroFecha1 = new FiltroVista();
                            filtroFecha1.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                            filtroFecha1.setIdComparador(EnumTipoComparacion.GE.getTipoComparacion());
                            filtroFecha1.setValor(fecha_from);
                            filtroFecha1.setValorLabel(fecha_from);
                            filtroFecha1.setIdVista(vista1);
                            vista1.getFiltrosVistaList().add(filtroFecha1);
                            //--
                            FiltroVista filtroFecha2 = new FiltroVista();
                            filtroFecha2.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                            filtroFecha2.setIdComparador(EnumTipoComparacion.LT.getTipoComparacion());
                            filtroFecha2.setValor(fecha_to);
                            filtroFecha2.setValorLabel(fecha_to);
                            filtroFecha2.setIdVista(vista1);
                            vista1.getFiltrosVistaList().add(filtroFecha2);

                            //============= Y AXIS
                            FiltroVista filtroEntityY = new FiltroVista();
                            filtroEntityY.setIdCampo(campoCompCasoEjeYItemsEntity.getIdCampo());
                            filtroEntityY.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                            filtroEntityY.setValor(y.getValue().toString());
                            filtroEntityY.setValorLabel(y.getLabel());
                            filtroEntityY.setIdVista(vista1);
                            vista1.getFiltrosVistaList().add(filtroEntityY);

                            //====== DATA FILTERS
                            if (showFilter) {
                                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                            }
                            final int countCasosForView = vistaController.countItemsVista(vista1);
                            if (countCasosForView > 0) {
                                serie_y.set(label, countCasosForView);
                                addTwoDimTableValue(label, serie_y.getLabel(), countCasosForView);
                                items_eje_x.add(vista1);
                                categoryModel.addSeries(serie_y);
                                seriesIndex++;
                            }

                        }
                    }

                } else if (comparableFieldX.getFieldTypeId().getFieldTypeId().equalsIgnoreCase(EnumFieldType.CHECKBOX.getFieldType().getFieldTypeId())) {

                    for (SelectItem x : getFilterHelper().findPosibleOptions(comparableFieldX.getIdCampo(), userSessionBean.getCurrent())) {
                        Vista vista1 = new Vista(Caso.class);
                        if (vista1.getFiltrosVistaList() == null) {
                            vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                        }
                        FiltroVista filtroEntityX = new FiltroVista();
                        filtroEntityX.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                        filtroEntityX.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                        filtroEntityX.setValor(x.getValue().toString());
                        filtroEntityX.setValorLabel(x.getLabel());
                        filtroEntityX.setIdVista(vista1);
                        vista1.getFiltrosVistaList().add(filtroEntityX);

                        FiltroVista filtroEntityY = new FiltroVista();
                        filtroEntityY.setIdCampo(campoCompCasoEjeYItemsEntity.getIdCampo());
                        filtroEntityY.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                        filtroEntityY.setValor(y.getValue().toString());
                        filtroEntityY.setValorLabel(y.getLabel());
                        filtroEntityY.setIdVista(vista1);
                        vista1.getFiltrosVistaList().add(filtroEntityY);

                        if (showFilter) {
                            vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                        }
                        final int countCasosForView = vistaController.countItemsVista(vista1);

                        if (countCasosForView > 0) {
                            serie_y.set(x.getLabel(), countCasosForView);
                            addTwoDimTableValue(x.getLabel(), serie_y.getLabel(), countCasosForView);
                            items_eje_x.add(vista1);
                            categoryModel.addSeries(serie_y);
                            seriesIndex++;
                        }

                    }

                } else if (comparableFieldX.getFieldTypeId().getFieldTypeId().equalsIgnoreCase(EnumFieldType.SELECTONE_ENTITY.getFieldType().getFieldTypeId())) {

                    //If not a Date or Calendar then see what the options are: options are those selected.
                    if (campoCompCasoEjeXSeriesEntity.getIdComparador().equals(EnumTipoComparacion.SC.getTipoComparacion())) {

                        //SUB-CONJUNTO:SC
                        //                for (SelectItem x : vistaController.findPosibleOptionsFor(campoCompCasoEjeXSeriesEntity.getIdCampo(), false, true, false)) {
                        for (String value : campoCompCasoEjeXSeriesEntity.getValoresList()) {

                            String label;
                            if (value.equalsIgnoreCase(AbstractJPAController.PLACE_HOLDER_NULL)) {
                                label = JPAFilterHelper.PLACE_HOLDER_NULL_LABEL;
                            } else {
                                try {
                                    label = em.find(entity_x_type, value).toString();

                                } catch (java.lang.IllegalArgumentException e) {
                                    label = em.find(entity_x_type, Integer.valueOf(value)).toString();
                                }
                            }

                            Vista vista1 = new Vista(Caso.class);
                            if (vista1.getFiltrosVistaList() == null) {
                                vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                            }
                            FiltroVista filtroEntity = new FiltroVista();
                            filtroEntity.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                            filtroEntity.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                            filtroEntity.setValor(value);
                            filtroEntity.setIdVista(vista1);
                            vista1.getFiltrosVistaList().add(filtroEntity);

                            FiltroVista filtroEntityY = new FiltroVista();
                            filtroEntityY.setIdCampo(getCampoCompCasoEjeYItemsEntity().getIdCampo());
                            filtroEntityY.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                            filtroEntityY.setValor(y.getValue().toString());
                            filtroEntityY.setValorLabel(y.getLabel());
                            filtroEntityY.setIdVista(vista1);
                            vista1.getFiltrosVistaList().add(filtroEntityY);

                            if (showFilter) {
                                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                            }
                            final int countCasosForView = vistaController.countItemsVista(vista1);
                            if (countCasosForView > 0) {
                                serie_y.set(label, countCasosForView);
                                addTwoDimTableValue(label, serie_y.getLabel(), countCasosForView);
                                items_eje_x.add(vista1);
                                categoryModel.addSeries(serie_y);
                                seriesIndex++;
                            }

                        }

                    } else {
                        //SELECTED VALUE

                        String label;
                        if (campoCompCasoEjeXSeriesEntity.getValor().equalsIgnoreCase(AbstractJPAController.PLACE_HOLDER_NULL)) {
                            label = JPAFilterHelper.PLACE_HOLDER_NULL_LABEL;
                        } else {
                            try {
                                label = em.find(entity_x_type, campoCompCasoEjeXSeriesEntity.getValor()).toString();

                            } catch (java.lang.IllegalArgumentException e) {
                                label = em.find(entity_x_type, Integer.valueOf(campoCompCasoEjeXSeriesEntity.getValor())).toString();
                            }
                        }

                        Vista vista1 = new Vista(Caso.class);
                        if (vista1.getFiltrosVistaList() == null) {
                            vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                        }
                        FiltroVista filtroEntity = new FiltroVista();
                        filtroEntity.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                        filtroEntity.setIdComparador(campoCompCasoEjeXSeriesEntity.getIdComparador());
                        filtroEntity.setValor(campoCompCasoEjeXSeriesEntity.getValor());
                        filtroEntity.setValorLabel(campoCompCasoEjeXSeriesEntity.getValorLabel());
                        filtroEntity.setIdVista(vista1);
                        vista1.getFiltrosVistaList().add(filtroEntity);

                        FiltroVista filtroEntityY = new FiltroVista();
                        filtroEntityY.setIdCampo(getCampoCompCasoEjeYItemsEntity().getIdCampo());
                        filtroEntityY.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                        filtroEntityY.setValor(y.getValue().toString());
                        filtroEntityY.setValorLabel(y.getLabel());
                        filtroEntityY.setIdVista(vista1);
                        vista1.getFiltrosVistaList().add(filtroEntityY);

                        if (showFilter) {
                            vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                        }
                        final int countCasosForView = vistaController.countItemsVista(vista1);

                        if (countCasosForView > 0) {
                            serie_y.set(label, countCasosForView);
                            addTwoDimTableValue(label, serie_y.getLabel(), countCasosForView);
                            items_eje_x.add(vista1);
                            categoryModel.addSeries(serie_y);
                            seriesIndex++;
                        }

                    }

                } else {
                    throw new NotSupportedException("Aun no se implementa esta funcionalidad, sorry!");
                }

                viewMatrix.put(seriesIndex, items_eje_x);

            }

            setChartModelTitle(comparableFieldX.getLabel() + " v/s N° de Casos por " + comparableFieldY.getLabel());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (categoryModel.getSeries() == null || categoryModel.getSeries().isEmpty()) {
                categoryModel = null;
            }

            em.close();
        }
    }

    public void createOneDimentionModel() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NotSupportedException, Exception {
        oneDimData = new HashMap<>();
        variables = 1;
        EntityManager em = emf.createEntityManager();
        ComparableField comparableFieldX = getFilterHelper().getComparableFieldsMap().get(campoCompCasoEjeXSeriesEntity.getIdCampo());
        try {

            //Pie Model can only be One Dimension
            ChartSeries serie = new ChartSeries();

            if (tipoGraficoSelected.equalsIgnoreCase("pieChart")) {
                categoryModel = null;
                pieModel = new PieChartModel();

            } else if (tipoGraficoSelected.equalsIgnoreCase("barChart") || tipoGraficoSelected.equalsIgnoreCase("lineChart")) {
                pieModel = null;//Hide Pie chart
                categoryModel = new CartesianChartModel();
                categoryModel.addSeries(serie);
            } else {
                throw new NotSupportedException("Tipo de grafico no soportado para una dimensión!");
            }

            viewMatrix = new HashMap<>();
            int seriesIndex = 0;
            serie.setLabel(comparableFieldX.getLabel());
            Class entity_type = comparableFieldX.getTipo();

            ArrayList<Vista> vistaItems = new ArrayList<>();

            //Is the axis a Date|Calendar value? if yes then posible values are days of a period of days
            if (comparableFieldX.getFieldTypeId().getFieldTypeId().equalsIgnoreCase(EnumFieldType.CALENDAR.getFieldType().getFieldTypeId())) {

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date from_date = format.parse(campoCompCasoEjeXSeriesEntity.getValor());
                Date to_date = format.parse(campoCompCasoEjeXSeriesEntity.getValor2());

                //less than one year
                int daysBetween = Days.daysBetween(new DateTime(from_date), new DateTime(to_date)).getDays();

                List<Interval> rangeStartDates = null;

                if (daysBetween > 0) {
                    if (daysBetween <= 7) {
//                            period = 1;//Days
                        rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.DAY_OF_MONTH);
                        dateFormat = new SimpleDateFormat("dd MMM");
                    } else if (daysBetween <= 31) {
//                            period = 7;//Weeks
                        rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.WEEK_OF_MONTH);
                        dateFormat = new SimpleDateFormat("'w'ww MMM");
                    } else if (daysBetween <= 365) {
//                            period = 30;//Months, How Many? Calculate Range. which must be beetwen 1-12.
                        rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.MONTH);
                        dateFormat = new SimpleDateFormat("MMM yyyy");
                    } else if (daysBetween > 365) {
//                            period = 365;//Years, How Many? calculate range
                        rangeStartDates = DateUtils.getRangeStartDates(from_date, to_date, Calendar.YEAR);
                        dateFormat = new SimpleDateFormat("yyyy");
                    }

                } else {
                    throw new NotSupportedException("El rango de fechas no es valido!");
                }

//                    Calendar calendar_from = Calendar.getInstance();
//                    calendar_from.setTime(from_date);
//                    Calendar calendar_to = Calendar.getInstance();
                if (rangeStartDates != null) {
                    for (Interval interval : rangeStartDates) {

                        String fecha_from = format.format(interval.getStart().toDate());//ok to start any day i want for any period.
                        String fecha_to = format.format(interval.getEnd().toDate());//ok to start any day i want for any period.
                        String label = dateFormat.format(interval.getStart().toDate());

                        //Create Vista to count items
                        Vista vista1 = generarFiltroVistaFechas(fecha_from, fecha_to);
                        if (showFilter) {
                            vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                        }
                        final int countCasosForView = vistaController.countItemsVista(vista1);
                        if (countCasosForView > 0) {
                            if (tipoGraficoSelected.equalsIgnoreCase("pieChart")) {
                                pieModel.set(label, vistaController.countItemsVista(vista1));
                                addOneDimTableValue(label, countCasosForView);
                            } else if (tipoGraficoSelected.equalsIgnoreCase("barChart") || tipoGraficoSelected.equalsIgnoreCase("lineChart")) {
                                serie.set(label, countCasosForView);
                                addOneDimTableValue(label, countCasosForView);
                            }
                            vistaItems.add(vista1);
                        }

                    }
                }

            } else if (comparableFieldX.getFieldTypeId().getFieldTypeId().equalsIgnoreCase(EnumFieldType.CHECKBOX.getFieldType().getFieldTypeId())) {

                for (SelectItem x : getFilterHelper().findPosibleOptions(comparableFieldX.getIdCampo(), userSessionBean.getCurrent())) {

                    Vista vista1 = new Vista(Caso.class);
                    if (vista1.getFiltrosVistaList() == null) {
                        vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                    }
                    FiltroVista filtroEntity = new FiltroVista();
                    filtroEntity.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                    filtroEntity.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                    filtroEntity.setValor(x.getValue().toString());
                    filtroEntity.setValorLabel(x.getLabel());
                    filtroEntity.setIdVista(vista1);
                    vista1.getFiltrosVistaList().add(filtroEntity);

                    if (showFilter) {
                        vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                    }
                    final int countCasosForView = vistaController.countItemsVista(vista1);
                    if (countCasosForView > 0) {
                        if (tipoGraficoSelected.equalsIgnoreCase("pieChart")) {
                            pieModel.set(x.getLabel(), countCasosForView);
                            addOneDimTableValue(x.getLabel(), countCasosForView);

                        } else if (tipoGraficoSelected.equalsIgnoreCase("barChart") || tipoGraficoSelected.equalsIgnoreCase("lineChart")) {
                            serie.set(x.getLabel(), countCasosForView);
                            addOneDimTableValue(x.getLabel(), countCasosForView);
                        }

                        vistaItems.add(vista1);
                    }

                }

            } else if (comparableFieldX.getFieldTypeId().getFieldTypeId().equalsIgnoreCase(EnumFieldType.SELECTONE_ENTITY.getFieldType().getFieldTypeId())) {

                //If not a Date or Calendar then see what the options are: options are those selected.
                if (campoCompCasoEjeXSeriesEntity.getIdComparador().equals(EnumTipoComparacion.SC.getTipoComparacion())) {
                    //SUB-CONJUNTO:SC
                    //                for (SelectItem x : vistaController.findPosibleOptionsFor(campoCompCasoEjeXSeriesEntity.getIdCampo(), false, true, false)) {
                    for (String value : campoCompCasoEjeXSeriesEntity.getValoresList()) {

                        String label;
                        if (value.equalsIgnoreCase(AbstractJPAController.PLACE_HOLDER_NULL)) {
                            label = JPAFilterHelper.PLACE_HOLDER_NULL_LABEL;
                        } else {
                            try {
                                label = em.find(entity_type, value).toString();

                            } catch (java.lang.IllegalArgumentException e) {
                                label = em.find(entity_type, Integer.valueOf(value)).toString();
                            }
                        }

                        Vista vista1 = new Vista(Caso.class);
                        if (vista1.getFiltrosVistaList() == null) {
                            vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                        }
                        FiltroVista filtroEntity = new FiltroVista();
                        filtroEntity.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                        filtroEntity.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                        filtroEntity.setValor(value);
                        filtroEntity.setValorLabel(label);
                        filtroEntity.setIdVista(vista1);
                        vista1.getFiltrosVistaList().add(filtroEntity);

                        if (showFilter) {
                            vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                        }
                        final int countCasosForView = vistaController.countItemsVista(vista1);
                        if (countCasosForView > 0) {
                            if (tipoGraficoSelected.equalsIgnoreCase("pieChart")) {
                                pieModel.set(label, countCasosForView);
                                addOneDimTableValue(label, countCasosForView);

                            } else if (tipoGraficoSelected.equalsIgnoreCase("barChart") || tipoGraficoSelected.equalsIgnoreCase("lineChart")) {
                                serie.set(label, countCasosForView);
                                addOneDimTableValue(label, countCasosForView);
                            }
                            vistaItems.add(vista1);
                        }

                    }

                } else {
                    //SELECTED VALUE

                    String label;
                    if (campoCompCasoEjeXSeriesEntity.getValor().equalsIgnoreCase(AbstractJPAController.PLACE_HOLDER_NULL)) {
                        label = JPAFilterHelper.PLACE_HOLDER_NULL_LABEL;
                    } else {
                        try {
                            label = em.find(entity_type, campoCompCasoEjeXSeriesEntity.getValor()).toString();

                        } catch (java.lang.IllegalArgumentException e) {
                            label = em.find(entity_type, Integer.valueOf(campoCompCasoEjeXSeriesEntity.getValor())).toString();
                        }
                    }

                    Vista vista1 = new Vista(Caso.class);
                    if (vista1.getFiltrosVistaList() == null) {
                        vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                    }
                    FiltroVista filtroEntity = new FiltroVista();
                    filtroEntity.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
                    filtroEntity.setIdComparador(campoCompCasoEjeXSeriesEntity.getIdComparador());
                    filtroEntity.setValor(campoCompCasoEjeXSeriesEntity.getValor());
                    filtroEntity.setValorLabel(campoCompCasoEjeXSeriesEntity.getValorLabel());
                    filtroEntity.setIdVista(vista1);
                    vista1.getFiltrosVistaList().add(filtroEntity);

                    if (showFilter) {
                        vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                    }

                    final int countCasosForView = vistaController.countItemsVista(vista1);
                    if (countCasosForView > 0) {
                        if (tipoGraficoSelected.equalsIgnoreCase("pieChart")) {
                            pieModel.set(label, countCasosForView);
                            addOneDimTableValue(label, countCasosForView);

                        } else if (tipoGraficoSelected.equalsIgnoreCase("barChart") || tipoGraficoSelected.equalsIgnoreCase("lineChart")) {
                            serie.set(label, countCasosForView);
                            addOneDimTableValue(label, countCasosForView);
                        }
                        vistaItems.add(vista1);
                    }

                }

            } else {
                throw new NotSupportedException("Aun no se implementa esta funcionalidad, sorry!");
            }

            viewMatrix.put(seriesIndex, vistaItems);
            setChartModelTitle("N° de Casos por " + comparableFieldX.getLabel());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (tipoGraficoSelected.equalsIgnoreCase("pieChart")) {
                if (pieModel.getData() == null || pieModel.getData().isEmpty()) {
                    pieModel = null;
                }

            } else if (tipoGraficoSelected.equalsIgnoreCase("barChart") || tipoGraficoSelected.equalsIgnoreCase("lineChart")) {
                if (categoryModel.getSeries() == null || categoryModel.getSeries().isEmpty()) {
                    categoryModel = null;
                }
            }

            em.close();
        }

        System.out.println("oneDimData:" + oneDimData);
    }

    /**
     * Estado actual de postventa
     *
     * @return
     */
    public String prepareCategoryModelCasosAbiertosPorSubEstado() {
        this.setShowFilter(false);
        variables = 2;
        twoDimData = new HashMap<>();
        pieModel = null;//Hide Pie chart
        categoryModel = new CartesianChartModel();
        setTipoGraficoSelected("barChart");
        setXaxisLabel("Ejecutivos");

//      List<SubEstadoCaso> subestados = (List<SubEstadoCaso>)getJpaController().findAll(SubEstadoCaso.class);//Y
        List<SubEstadoCaso> subestados = (List<SubEstadoCaso>) getJpaController().getSubEstadoCasofindByIdEstadoAndTipoCaso(EnumEstadoCaso.ABIERTO.getEstado(), EnumTipoCaso.POSTVENTA.getTipoCaso());

        List<Area> areas = (List<Area>) getJpaController().findAll(Area.class);//X

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        for (SubEstadoCaso subestado : subestados) {

            ChartSeries serieEstado = new ChartSeries();
            serieEstado.setLabel(subestado.getIdSubEstado());

            Vista vista0 = new Vista(Caso.class);
            FiltroVista filtroNotAssigned = new FiltroVista();
            filtroNotAssigned.setIdCampo(Caso_.AREA_FIELD_NAME);
            filtroNotAssigned.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroNotAssigned.setValor(AbstractJPAController.PLACE_HOLDER_NULL);
            filtroNotAssigned.setValorLabel("SIN AREA");
            filtroNotAssigned.setIdVista(vista0);
            if (vista0.getFiltrosVistaList() == null) {
                vista0.setFiltrosVistaList(new ArrayList<FiltroVista>());

            }
            vista0.getFiltrosVistaList().add(filtroNotAssigned);

            FiltroVista filtroEstado = new FiltroVista();
            filtroEstado.setIdCampo(Caso_.SUBESTADO_FIELD_NAME);
            filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEstado.setValor(subestado.getIdSubEstado());
            filtroEstado.setValorLabel(subestado.getNombre());
            filtroEstado.setIdVista(vista0);
            vista0.getFiltrosVistaList().add(filtroEstado);

            vista0.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
            serieEstado.set("SIN AREA", vistaController.countItemsVista(vista0));

            ArrayList<Vista> vistaItems = new ArrayList<>();
            vistaItems.add(vista0);

            for (Area area : areas) {

                Vista vista1 = new Vista(Caso.class);
                FiltroVista filtro = new FiltroVista();
                filtro.setIdCampo(Caso_.AREA_FIELD_NAME);
                filtro.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtro.setValor(area.getIdArea());
                filtro.setValorLabel(area.getNombre());
                filtro.setIdVista(vista1);
                if (vista1.getFiltrosVistaList() == null) {
                    vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                }
                vista1.getFiltrosVistaList().add(filtro);

                FiltroVista filtroEstado1 = new FiltroVista();
                filtroEstado1.setIdCampo(Caso_.SUBESTADO_FIELD_NAME);
                filtroEstado1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroEstado1.setValor(subestado.getIdSubEstado());
                filtroEstado1.setValorLabel(subestado.getNombre());
                filtroEstado1.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroEstado1);

                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                final int countCasosForView = vistaController.countItemsVista(vista1);
                serieEstado.set(area.getIdArea(), countCasosForView);
                addTwoDimTableValue(area.getIdArea(), subestado.getNombre(), countCasosForView);

                vistaItems.add(vista1);

            }
            categoryModel.addSeries(serieEstado);
            viewMatrix.put(seriesIndex, vistaItems);
            seriesIndex++;
        }
        setChartModelTitle("Sub Estados de Casos por Areas");
        return "reports";
    }

    /**
     * Casos Abiertos v/s Cerrados última semana
     *
     * @return
     */
    public String prepareCategoryModelCasosAbiertosVsCerradosLastWeek() {
        this.setShowFilter(true);
        variables = 2;
        twoDimData = new HashMap<>();
        pieModel = null;//Hide Pie chart
        categoryModel = new CartesianChartModel();
        setTipoGraficoSelected("barChart");
        setXaxisLabel("Fecha");
        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        final DateTime input = new DateTime();
        System.out.println(input);
        final DateTime startOfLastWeek
                = new DateTime(input.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay());
        System.out.println(startOfLastWeek);
        final DateTime endOfLastWeek = startOfLastWeek.plusDays(6).withTime(23, 59, 59, 0);
        System.out.println(startOfLastWeek + "---" + endOfLastWeek);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", LOCALE_ES_CL);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", LOCALE_ES_CL);

        List<Interval> rangeStartDates = DateUtils.getRangeStartDates(startOfLastWeek.toDate(), endOfLastWeek.toDate(), Calendar.DAY_OF_MONTH);

        List<EstadoCaso> estados = (List<EstadoCaso>) getJpaController().findAll(EstadoCaso.class);//Y-axis
        for (EstadoCaso estado : estados) {

            ChartSeries serieEstado = new ChartSeries();
            serieEstado.setLabel(estado.getIdEstado());

//            Vista vista0 = new Vista(Caso.class);
//            FiltroVista filtroNull = new FiltroVista();
//            filtroNull.setIdCampo(Caso_.ESTADO_FIELD_NAME);
//            filtroNull.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
//            filtroNull.setValor(AbstractJPAController.PLACE_HOLDER_NULL);
//            filtroNull.setValorLabel("SIN ESTADO!");
//            filtroNull.setIdVista(vista0);
//            if (vista0.getFiltrosVistaList() == null) {
//                vista0.setFiltrosVistaList(new ArrayList<FiltroVista>());
//
//            }
//            vista0.getFiltrosVistaList().add(filtroNull);
//
//            FiltroVista filtroEstado = new FiltroVista();
//            filtroEstado.setIdCampo(Caso_.SUBESTADO_FIELD_NAME);
//            filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
//            filtroEstado.setValor(subestado.getIdSubEstado());
//            filtroEstado.setValorLabel(subestado.getNombre());
//            filtroEstado.setIdVista(vista0);
//            vista0.getFiltrosVistaList().add(filtroEstado);
//
//            vista0.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
//            serieEstado.set("SIN AREA", vistaController.countItemsVista(vista0));
//
            ArrayList<Vista> vistaItems = new ArrayList<>();
//            vistaItems.add(vista0);

            if (rangeStartDates != null) {
                for (Interval interval : rangeStartDates) {

                    final Date startDate = interval.getStart().toDate();
                    final Date endDate = interval.getEnd().toDate();

                    String fecha_from = format.format(startDate);//ok to start any day i want for any period.
                    String fecha_to = format.format(endDate);//ok to start any day i want for any period.
                    String label = dateFormat.format(startDate);

                    //Create Vista to count items
                    Vista vista1 = new Vista(Caso.class);
                    if (vista1.getFiltrosVistaList() == null) {
                        vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());
                    }

                    //==================FECHA_CREACION/FECHA_X
                    FiltroVista filtroFecha1 = new FiltroVista();
                    filtroFecha1.setIdCampo(Caso_.FECHA_CREACION_FIELD_NAME);
                    filtroFecha1.setIdComparador(EnumTipoComparacion.BW.getTipoComparacion());
                    filtroFecha1.setValor(fecha_from);
                    filtroFecha1.setValorLabel(fecha_from);
                    filtroFecha1.setValor2(fecha_to);
                    filtroFecha1.setValor2Label(fecha_to);
                    filtroFecha1.setIdVista(vista1);
                    vista1.getFiltrosVistaList().add(filtroFecha1);

                    //============= Y AXIS
                    FiltroVista filtroEntityY = new FiltroVista();
                    filtroEntityY.setIdCampo(Caso_.ESTADO_FIELD_NAME);
                    filtroEntityY.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                    filtroEntityY.setValor(estado.getIdEstado());
                    filtroEntityY.setValorLabel(estado.getNombre());
                    filtroEntityY.setIdVista(vista1);
                    vista1.getFiltrosVistaList().add(filtroEntityY);

                    //====== DATA FILTERS
                    vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                    final int countCasosForView = vistaController.countItemsVista(vista1);
                    serieEstado.set(label, countCasosForView);
                    addTwoDimTableValue(label, serieEstado.getLabel(), countCasosForView);
                    vistaItems.add(vista1);
                }
            }

            categoryModel.addSeries(serieEstado);
            viewMatrix.put(seriesIndex, vistaItems);
            seriesIndex++;
        }
        setChartModelTitle("Casos Abiertos v/s Cerrados última semana");
        return "reports";
    }

    /**
     * Casos criticos
     *
     * @return
     */
    public String prepareCategoryModelCasosAbiertosPorEstadoAlerta() {
        this.setShowFilter(false);
        variables = 2;
        twoDimData = new HashMap<>();
        pieModel = null;//Hide Pie chart
        categoryModel = new CartesianChartModel();
        setTipoGraficoSelected("barChart");
        setXaxisLabel("Ejecutivos");

//      List<SubEstadoCaso> subestados = (List<SubEstadoCaso>)getJpaController().findAll(SubEstadoCaso.class);//Y
        List<TipoAlerta> alertas = (List<TipoAlerta>) getJpaController().findAll(TipoAlerta.class);

        List<Producto> productos = (List<Producto>) getJpaController().findAll(Producto.class);//X

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        for (TipoAlerta tipoAlerta : alertas) {

            ChartSeries seriey = new ChartSeries();
            seriey.setLabel(tipoAlerta.getNombre());

            ArrayList<Vista> vistaItems = new ArrayList<>();

            for (Producto prod : productos) {

                Vista vista1 = new Vista(Caso.class);
                FiltroVista filtro = new FiltroVista();
                filtro.setIdCampo(Caso_.PRODUCTO_FIELD_NAME);
                filtro.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtro.setValor(prod.getIdProducto());
                filtro.setValorLabel(prod.getNombre());
                filtro.setIdVista(vista1);
                if (vista1.getFiltrosVistaList() == null) {
                    vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                }
                vista1.getFiltrosVistaList().add(filtro);

                FiltroVista filtroEstado1 = new FiltroVista();
                filtroEstado1.setIdCampo(Caso_.ESTADO_ALERTA_FIELD_NAME);
                filtroEstado1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroEstado1.setValor(tipoAlerta.getIdalerta().toString());
                filtroEstado1.setValorLabel(tipoAlerta.getNombre());
                filtroEstado1.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroEstado1);

                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                final int countCasosForView = vistaController.countItemsVista(vista1);
                seriey.set(prod.getNombre(), countCasosForView);
                addTwoDimTableValue(prod.getNombre(), tipoAlerta.getNombre(), countCasosForView);

                vistaItems.add(vista1);

            }
            categoryModel.addSeries(seriey);
            viewMatrix.put(seriesIndex, vistaItems);
            seriesIndex++;
        }
        setChartModelTitle("Estado de Alerta de casos por Producto");
        return "reports";
    }

    /**
     * Casos criticos
     *
     * @return
     */
    public String prepareCategoryModelCasosPorProductos() {
        this.setShowFilter(false);
        variables = 2;
        twoDimData = new HashMap<>();
        pieModel = null;//Hide Pie chart
        categoryModel = new CartesianChartModel();
        setTipoGraficoSelected("barChart");
        setXaxisLabel("Ejecutivos");

        List<EstadoCaso> estados = (List<EstadoCaso>) getJpaController().findAll(EstadoCaso.class);//Y
        List<Producto> productos = (List<Producto>) getJpaController().findAll(Producto.class);//X

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        for (EstadoCaso estado : estados) {

            ChartSeries seriey = new ChartSeries();
            seriey.setLabel(estado.getNombre());

            ArrayList<Vista> vistaItems = new ArrayList<>();

            for (Producto prod : productos) {

                Vista vista1 = new Vista(Caso.class);
                FiltroVista filtro = new FiltroVista();
                filtro.setIdCampo(Caso_.PRODUCTO_FIELD_NAME);
                filtro.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtro.setValor(prod.getIdProducto());
                filtro.setValorLabel(prod.getNombre());
                filtro.setIdVista(vista1);
                if (vista1.getFiltrosVistaList() == null) {
                    vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                }
                vista1.getFiltrosVistaList().add(filtro);

                FiltroVista filtroEstado1 = new FiltroVista();
                filtroEstado1.setIdCampo(Caso_.ESTADO_FIELD_NAME);
                filtroEstado1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroEstado1.setValor(estado.getIdEstado());
                filtroEstado1.setValorLabel(estado.getNombre());
                filtroEstado1.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroEstado1);

                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                final int countCasosForView = vistaController.countItemsVista(vista1);
                seriey.set(prod.getNombre(), countCasosForView);
                addTwoDimTableValue(prod.getNombre(), estado.getNombre(), countCasosForView);

                vistaItems.add(vista1);

            }
            categoryModel.addSeries(seriey);
            viewMatrix.put(seriesIndex, vistaItems);
            seriesIndex++;
        }
        setChartModelTitle("Casos Abiertos v/s Cerrados por Producto");
        return "reports";
    }

//    private int daysBetween(Date d1, Date d2) {
//        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
//    }
    public String prepareCategoryModelCasosPorArea() {
        this.setShowFilter(false);
        variables = 2;
        twoDimData = new HashMap<>();
        pieModel = null;//Hide Pie chart
        categoryModel = new CartesianChartModel();
        setTipoGraficoSelected("barChart");
        setXaxisLabel("Areas");

        List<EstadoCaso> estados = (List<EstadoCaso>) getJpaController().findAll(EstadoCaso.class);//Y-axis
        List<Area> areas = (List<Area>) getJpaController().findAll(Area.class);//X-axix

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        for (EstadoCaso estado : estados) {//series index

            ChartSeries serieEstado = new ChartSeries();
            serieEstado.setLabel(estado.getIdEstado());

            ArrayList<Vista> vistaItems = new ArrayList<>();

            for (Area a : areas) {//item index

                Vista vista1 = new Vista(Caso.class);
                if (vista1.getFiltrosVistaList() == null) {
                    vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());
                }

                FiltroVista filtroArea = new FiltroVista();
                filtroArea.setIdCampo(Caso_.AREA_FIELD_NAME);
                filtroArea.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroArea.setValor(a.getIdArea());
                filtroArea.setValorLabel(a.getNombre());
                filtroArea.setIdVista(vista1);

                vista1.getFiltrosVistaList().add(filtroArea);

                FiltroVista filtroEstado1 = new FiltroVista();
                filtroEstado1.setIdCampo(Caso_.ESTADO_FIELD_NAME);
                filtroEstado1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroEstado1.setValor(estado.getIdEstado());
                filtroEstado1.setValorLabel(estado.getNombre());
                filtroEstado1.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroEstado1);

                vistaItems.add(vista1);

                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                final int countCasosForView = vistaController.countItemsVista(vista1);
                serieEstado.set(a.getIdArea(), countCasosForView);
                addTwoDimTableValue(a.getIdArea(), estado.getNombre(), countCasosForView);

            }

            categoryModel.addSeries(serieEstado);
            viewMatrix.put(seriesIndex, vistaItems);
            seriesIndex++;
        }
        setChartModelTitle("Estado de Casos por Area");
        return "reports";
    }

    public String prepareCategoryModelCasosPorGrupo() {
        this.setShowFilter(false);
        variables = 2;
        twoDimData = new HashMap<>();
        pieModel = null;//Hide Pie chart
        categoryModel = new CartesianChartModel();
        setTipoGraficoSelected("barChart");
        setXaxisLabel("Areas");

        List<EstadoCaso> estados = (List<EstadoCaso>) getJpaController().findAll(EstadoCaso.class);//Y-axis
        List<Grupo> grupos = (List<Grupo>) getJpaController().findAll(Grupo.class);//X-axix

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        for (EstadoCaso estado : estados) {//series index

            ChartSeries serieEstado = new ChartSeries();
            serieEstado.setLabel(estado.getIdEstado());

            ArrayList<Vista> vistaItems = new ArrayList<>();

            for (Grupo obj : grupos) {//item index

                Vista vista1 = new Vista(Caso.class);
                if (vista1.getFiltrosVistaList() == null) {
                    vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());
                }

                FiltroVista filtroArea = new FiltroVista();
                filtroArea.setIdCampo(Caso_.GRUPO_FIELD_NAME);
                filtroArea.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroArea.setValor(obj.getIdGrupo());
                filtroArea.setValorLabel(obj.getNombre());
                filtroArea.setIdVista(vista1);

                vista1.getFiltrosVistaList().add(filtroArea);

                FiltroVista filtroEstado1 = new FiltroVista();
                filtroEstado1.setIdCampo(Caso_.ESTADO_FIELD_NAME);
                filtroEstado1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroEstado1.setValor(estado.getIdEstado());
                filtroEstado1.setValorLabel(estado.getNombre());
                filtroEstado1.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroEstado1);

                vistaItems.add(vista1);

                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                final int countCasosForView = vistaController.countItemsVista(vista1);
                serieEstado.set(obj.getIdGrupo(), countCasosForView);
                addTwoDimTableValue(obj.getIdGrupo(), estado.getNombre(), countCasosForView);

            }

            categoryModel.addSeries(serieEstado);
            viewMatrix.put(seriesIndex, vistaItems);
            seriesIndex++;
        }
        setChartModelTitle("Estado de Casos por Grupos de Agentes");
        return "reports";
    }

    public String prepareCategoryModelCasosPorAgente() {
        this.setShowFilter(false);
        variables = 2;
        twoDimData = new HashMap<>();
        pieModel = null;//Hide Pie chart
        categoryModel = new CartesianChartModel();
        setTipoGraficoSelected("barChart");
        setXaxisLabel("Ejecutivos");

        List<EstadoCaso> estados = (List<EstadoCaso>) getJpaController().findAll(EstadoCaso.class);//Y-axis
        List<Usuario> agents = (List<Usuario>) getJpaController().findAll(Usuario.class);//X

        try {
            if (agents.contains(EnumUsuariosBase.SISTEMA.getUsuario())) {
                agents.remove(EnumUsuariosBase.SISTEMA.getUsuario());
            }

        } catch (Exception e) {
            //ignore.
        }

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        for (EstadoCaso estado : estados) {

            ChartSeries serieEstado = new ChartSeries();
            serieEstado.setLabel(estado.getIdEstado());

            Vista vista0 = new Vista(Caso.class);
            FiltroVista filtroNotAssigned = new FiltroVista();
            filtroNotAssigned.setIdCampo(Caso_.OWNER_FIELD_NAME);
            filtroNotAssigned.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroNotAssigned.setValor(AbstractJPAController.PLACE_HOLDER_NULL);
            filtroNotAssigned.setValorLabel("sin asignar");
            filtroNotAssigned.setIdVista(vista0);
            if (vista0.getFiltrosVistaList() == null) {
                vista0.setFiltrosVistaList(new ArrayList<FiltroVista>());

            }
            vista0.getFiltrosVistaList().add(filtroNotAssigned);

            FiltroVista filtroEstado = new FiltroVista();
            filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
            filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroEstado.setValor(estado.getIdEstado());
            filtroEstado.setValorLabel(estado.getNombre());
            filtroEstado.setIdVista(vista0);
            vista0.getFiltrosVistaList().add(filtroEstado);

            vista0.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
            serieEstado.set("Sin Agente", vistaController.countItemsVista(vista0));

            ArrayList<Vista> vistaItems = new ArrayList<>();
            vistaItems.add(vista0);

            for (Usuario agent : agents) {

                Vista vista1 = new Vista(Caso.class);
                FiltroVista filtro = new FiltroVista();
                filtro.setIdCampo(Caso_.OWNER_FIELD_NAME);
                filtro.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtro.setValor(agent.getIdUsuario());
                filtro.setValorLabel(agent.getCapitalName());
                filtro.setIdVista(vista1);
                if (vista1.getFiltrosVistaList() == null) {
                    vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                }
                vista1.getFiltrosVistaList().add(filtro);

                FiltroVista filtroEstado1 = new FiltroVista();
                filtroEstado1.setIdCampo(Caso_.ESTADO_FIELD_NAME);
                filtroEstado1.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtroEstado1.setValor(estado.getIdEstado());
                filtroEstado1.setValorLabel(estado.getNombre());
                filtroEstado1.setIdVista(vista1);
                vista1.getFiltrosVistaList().add(filtroEstado1);

                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                final int countCasosForView = vistaController.countItemsVista(vista1);
                serieEstado.set(agent.getIdUsuario(), countCasosForView);
                addTwoDimTableValue(agent.getIdUsuario(), estado.getNombre(), countCasosForView);

                vistaItems.add(vista1);

            }
            categoryModel.addSeries(serieEstado);
            viewMatrix.put(seriesIndex, vistaItems);
            seriesIndex++;
        }
        setChartModelTitle("Estado de Casos por Ejecutivo");
        return "reports";
    }

    public String preparePieModelEstadoCasos() {
        this.setShowFilter(false);
        variables = 1;
        oneDimData = new HashMap<>();
        System.out.println("preparePieModelEstadoCasos()");
        categoryModel = null;
        pieModel = new PieChartModel();
        setTipoGraficoSelected("pieChart");
        setXaxisLabel("Estados");

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;

        Vista vista1 = new Vista(Caso.class);
        FiltroVista filtroCasosAbiertos = new FiltroVista();
        filtroCasosAbiertos.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroCasosAbiertos.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroCasosAbiertos.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
        filtroCasosAbiertos.setValorLabel(EnumEstadoCaso.ABIERTO.getEstado().getNombre());
        filtroCasosAbiertos.setIdVista(vista1);
        if (vista1.getFiltrosVistaList() == null) {
            vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

        }
        vista1.getFiltrosVistaList().add(filtroCasosAbiertos);

        Vista vista2 = new Vista(Caso.class);
        FiltroVista filtroCasosCerrados = new FiltroVista();
        filtroCasosCerrados.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroCasosCerrados.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroCasosCerrados.setValor(EnumEstadoCaso.CERRADO.getEstado().getIdEstado());
        filtroCasosCerrados.setValorLabel(EnumEstadoCaso.CERRADO.getEstado().getNombre());
        filtroCasosCerrados.setIdVista(vista2);
        if (vista2.getFiltrosVistaList() == null) {
            vista2.setFiltrosVistaList(new ArrayList<FiltroVista>());

        }
        vista2.getFiltrosVistaList().add(filtroCasosCerrados);

//        vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
        final int countCasosForView = vistaController.countItemsVista(vista1);
        pieModel.set("Abiertos", countCasosForView);//TODO i18n
        addOneDimTableValue("Abiertos", countCasosForView);

//        vista2.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
        final int countCasosForView2 = vistaController.countItemsVista(vista2);
        pieModel.set("Cerrados", countCasosForView2);//TODO i18n
        addOneDimTableValue("Cerrados", countCasosForView2);

        ArrayList<Vista> vistaItems = new ArrayList<>();
        vistaItems.add(vista1);
        vistaItems.add(vista2);

        viewMatrix.put(seriesIndex, vistaItems);

        setChartModelTitle("Estado de Casos");

        System.out.println("oneDimData:" + oneDimData);

        return "reports";

    }

    public String preparePieModelCasosPorAgente() {
        this.setShowFilter(false);
        variables = 1;
        oneDimData = new HashMap<>();
        pieModel = new PieChartModel();
        categoryModel = null;
        setTipoGraficoSelected("pieChart");
        setXaxisLabel("Ejecutivos");

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;
        ArrayList<Vista> vistaItems = new ArrayList<>();

        Vista vista0 = new Vista(Caso.class);
        FiltroVista filtroNotAssigned = new FiltroVista();
        filtroNotAssigned.setIdCampo(Caso_.OWNER_FIELD_NAME);
        filtroNotAssigned.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroNotAssigned.setValor(AbstractJPAController.PLACE_HOLDER_NULL);
        filtroNotAssigned.setValorLabel("sin asignar");
        filtroNotAssigned.setIdVista(vista0);
        if (vista0.getFiltrosVistaList() == null) {
            vista0.setFiltrosVistaList(new ArrayList<FiltroVista>());

        }
        vista0.getFiltrosVistaList().add(filtroNotAssigned);
        vista0.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
        pieModel.set("No Asignados", vistaController.countItemsVista(vista0));
        vistaItems.add(vista0);

        List<Usuario> agents = (List<Usuario>) getJpaController().findAll(Usuario.class);
        for (Usuario agent : agents) {
            if (!agent.getIdUsuario().equalsIgnoreCase(EnumUsuariosBase.SISTEMA.getUsuario().getIdUsuario())) {
                Vista vista1 = new Vista(Caso.class);
                FiltroVista filtro = new FiltroVista();
                filtro.setIdCampo(Caso_.OWNER_FIELD_NAME);
                filtro.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
                filtro.setValor(agent.getIdUsuario());
                filtro.setValorLabel(agent.getCapitalName());
                filtro.setIdVista(vista1);
                if (vista1.getFiltrosVistaList() == null) {
                    vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

                }
                vista1.getFiltrosVistaList().add(filtro);
                vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
                final int countCasosForView = vistaController.countItemsVista(vista1);
                pieModel.set(agent.getIdUsuario(), countCasosForView);
                addOneDimTableValue(agent.getIdUsuario(), countCasosForView);
                vistaItems.add(vista1);

            }

        }

        viewMatrix.put(seriesIndex, vistaItems);
        setChartModelTitle("Resumen de Casos por Ejecutivo");
        return "reports";

    }

    public String preparePieModelCasosPorArea() {
        this.setShowFilter(false);
        variables = 1;
        oneDimData = new HashMap<>();
        pieModel = new PieChartModel();
        categoryModel = null;
        setTipoGraficoSelected("pieChart");

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;
        ArrayList<Vista> vistaItems = new ArrayList<>();

        //1.Que quiero mostrar en el eje x ???  = Areas.
        List<Area> areas = (List<Area>) getJpaController().findAll(Area.class);//X-axix
        //2. Para cada elemento del eje x, como calculo el numero?? = varios filtros. por defecto hay un filtro que siempre va = el exe x.
        for (Area area : areas) {
            Vista vista1 = new Vista(Caso.class);
            FiltroVista filtro = new FiltroVista();
            filtro.setIdCampo(Caso_.AREA_FIELD_NAME);
            filtro.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtro.setValor(area.getIdArea());
            filtro.setValorLabel(area.getNombre());
            filtro.setIdVista(vista1);
            if (vista1.getFiltrosVistaList() == null) {
                vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

            }
            vista1.getFiltrosVistaList().add(filtro);
            vista1.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());
            final int countCasosForView = vistaController.countItemsVista(vista1);
            pieModel.set(area.getIdArea(), countCasosForView);
            addOneDimTableValue(area.getIdArea(), countCasosForView);
            vistaItems.add(vista1);

        }

        viewMatrix.put(seriesIndex, vistaItems);

        setChartModelTitle("Resumen de Casos por Area");
        return "reports";

    }

    public String preparePieModelAntiguedadCasosAbiertosAsignados() {
        return preparePieModelAntiguedadCasosAbiertos(true);
    }

    public String preparePieModelAntiguedadCasosAbiertosNoAsignados() {
        return preparePieModelAntiguedadCasosAbiertos(false);
    }

    private String preparePieModelAntiguedadCasosAbiertos(boolean asignados) {
        this.setShowFilter(false);
        variables = 1;
        oneDimData = new HashMap<>();
        int howManyPeriodsToShow = 8;
        int periodDays = 1;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        pieModel = new PieChartModel();
        categoryModel = null;
        setTipoGraficoSelected("pieChart");

        viewMatrix = new HashMap<>();
        int seriesIndex = 0;
        ArrayList<Vista> vistaItems = new ArrayList<>();

        //1.Que quiero mostrar en el eje x ???  = dias desde la fecha de creacion.
        Date now = new Date();

        for (int i = 1; i <= howManyPeriodsToShow; i++) {

            String fecha2 = format.format(now);

            now = new Date(now.getTime() - (periodDays * 24 * 60 * 60 * 1000));

            String fecha1 = format.format(now);
//==================ESTADO
            Vista view = new Vista(Caso.class);
            FiltroVista filtroCasosCerrados = new FiltroVista();
            filtroCasosCerrados.setIdCampo(Caso_.ESTADO_FIELD_NAME);
            filtroCasosCerrados.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            filtroCasosCerrados.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
            filtroCasosCerrados.setValorLabel(EnumEstadoCaso.ABIERTO.getEstado().getNombre());
            filtroCasosCerrados.setIdVista(view);
            if (view.getFiltrosVistaList() == null) {
                view.setFiltrosVistaList(new ArrayList<FiltroVista>());

            }
            view.getFiltrosVistaList().add(filtroCasosCerrados);
//==================OWNER
            FiltroVista filtroAsignadoAgente = new FiltroVista();
            filtroAsignadoAgente.setIdCampo(Caso_.OWNER_FIELD_NAME);
            filtroAsignadoAgente.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
            if (asignados) {
                filtroAsignadoAgente.setValor(AbstractJPAController.PLACE_HOLDER_ANY);
                filtroAsignadoAgente.setValorLabel(JPAFilterHelper.PLACE_HOLDER_ANY_LABEL);
            } else {
                filtroAsignadoAgente.setValor(AbstractJPAController.PLACE_HOLDER_NULL);
                filtroAsignadoAgente.setValorLabel(JPAFilterHelper.PLACE_HOLDER_NULL_LABEL);
            }

            filtroAsignadoAgente.setIdVista(view);
            view.getFiltrosVistaList().add(filtroAsignadoAgente);
//==================FECHA_CREACION
            if (i != howManyPeriodsToShow) {
                FiltroVista filtroFecha1 = new FiltroVista();
                filtroFecha1.setIdCampo(Caso_.FECHA_CREACION_FIELD_NAME);
                filtroFecha1.setIdComparador(EnumTipoComparacion.GE.getTipoComparacion());
                filtroFecha1.setValor(fecha1);
                filtroFecha1.setValorLabel(fecha1);
                filtroFecha1.setIdVista(view);
                view.getFiltrosVistaList().add(filtroFecha1);
            }
//--
            FiltroVista filtroFecha2 = new FiltroVista();
            filtroFecha2.setIdCampo(Caso_.FECHA_CREACION_FIELD_NAME);
            filtroFecha2.setIdComparador(EnumTipoComparacion.LT.getTipoComparacion());
            filtroFecha2.setValor(fecha2);
            filtroFecha2.setValorLabel(fecha2);
            filtroFecha2.setIdVista(view);
            view.getFiltrosVistaList().add(filtroFecha2);
//==================
//            System.out.println(view);

            view.getFiltrosVistaList().addAll(getVista().getFiltrosVistaList());

            pieModel.set((i) + (i > 1 ? " días" : " día") + (i != howManyPeriodsToShow ? "" : " o más"), vistaController.countItemsVista(view));
            vistaItems.add(view);

        }

        viewMatrix.put(seriesIndex, vistaItems);

        //2. Para cada elemento del eje x, como calculo el numero de casos?? usando varios criterios/filtros. por defecto hay un filtro que siempre va = el exe x.
        setChartModelTitle("Antiguedad de Casos Abiertos (" + (asignados ? "Asignados" : "No Asignados") + ")");
        return "reports";

    }

    public void itemSelect(ItemSelectEvent event) {

        try {
            Vista selected = viewMatrix.get(event.getSeriesIndex()).get(event.getItemIndex());
            casoController.applyViewFilter(selected);
            JsfUtil.addSuccessMessage("Desplegando Vista de Casos desde Grafico/Reporte.");
            redirect(inboxUrl);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
        }

    }

    /**
     * @return the pieModel
     */
    public PieChartModel getPieModel() {
        return pieModel;
    }

    /**
     * @param pieModel the pieModel to set
     */
    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    /**
     * @return the dataFormat
     */
    public String getDataFormat() {
        return dataFormat;
    }

    /**
     * @param dataFormat the dataFormat to set
     */
    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    /**
     * @return the categoryModel
     */
    public CartesianChartModel getCategoryModel() {
        return categoryModel;
    }

    /**
     * @param categoryModel the categoryModel to set
     */
    public void setCategoryModel(CartesianChartModel categoryModel) {
        this.categoryModel = categoryModel;
    }

    /**
     * @param casoController the casoController to set
     */
    public void setCasoController(CasoController casoController) {
        this.casoController = casoController;
    }

    /**
     * @return the tipoGraficoSelected
     */
    public String getTipoGraficoSelected() {
        return tipoGraficoSelected;
    }

    /**
     * @param tipoGraficoSelected the tipoGraficoSelected to set
     */
    public void setTipoGraficoSelected(String tipoGraficoSelected) {
        this.tipoGraficoSelected = tipoGraficoSelected;
    }

    /**
     * @return the variables
     */
    public int getVariables() {
        return variables;
    }

    /**
     * @param variables the variables to set
     */
    public void setVariables(int variables) {
        this.variables = variables;
    }

    /**
     * @return the chartModelTitle
     */
    public String getChartModelTitle() {
        return chartModelTitle;
    }

    /**
     * @param chartModelTitle the chartModelTitle to set
     */
    public void setChartModelTitle(String chartModelTitle) {
        this.chartModelTitle = chartModelTitle;
    }

    /**
     * @return the stacked
     */
    public boolean isStacked() {
        return stacked;
    }

    /**
     * @param stacked the stacked to set
     */
    public void setStacked(boolean stacked) {
        this.stacked = stacked;
    }

    /**
     * @return the activeLeftTab
     */
    public int getActiveLeftTab() {
        return activeLeftTab;
    }

    /**
     * @param activeLeftTab the activeLeftTab to set
     */
    public void setActiveLeftTab(int activeLeftTab) {
        this.activeLeftTab = activeLeftTab;
    }

    /**
     * @return the xaxisLabel
     */
    public String getXaxisLabel() {
        return xaxisLabel;
    }

    /**
     * @param xaxisLabel the xaxisLabel to set
     */
    public void setXaxisLabel(String xaxisLabel) {
        this.xaxisLabel = xaxisLabel;
    }

    /**
     * @return the yaxisLabel
     */
    public String getYaxisLabel() {
        return yaxisLabel;
    }

    /**
     * @param yaxisLabel the yaxisLabel to set
     */
    public void setYaxisLabel(String yaxisLabel) {
        this.yaxisLabel = yaxisLabel;
    }

    /**
     * @return the campoCompCasoEjeXSeriesEntity
     */
    public FiltroVista getCampoCompCasoEjeXSeriesEntity() {
        return campoCompCasoEjeXSeriesEntity;
    }

    /**
     * @param campoCompCasoEjeXSeriesEntity the campoCompCasoEjeXSeriesEntity to
     * set
     */
    public void setCampoCompCasoEjeXSeriesEntity(FiltroVista campoCompCasoEjeXSeriesEntity) {
        this.campoCompCasoEjeXSeriesEntity = campoCompCasoEjeXSeriesEntity;
    }

    /**
     * @return the campoCompCasoEjeYItemsEntity
     */
    public FiltroVista getCampoCompCasoEjeYItemsEntity() {
        return campoCompCasoEjeYItemsEntity;
    }

    /**
     * @param campoCompCasoEjeYItemsEntity the campoCompCasoEjeYItemsEntity to
     * set
     */
    public void setCampoCompCasoEjeYItemsEntity(FiltroVista campoCompCasoEjeYItemsEntity) {
        this.campoCompCasoEjeYItemsEntity = campoCompCasoEjeYItemsEntity;
    }

    /**
     * @return the fill
     */
    public boolean isFill() {
        return fill;
    }

    /**
     * @param fill the fill to set
     */
    public void setFill(boolean fill) {
        this.fill = fill;
    }

    private Vista generarFiltroVistaFechas(String fecha_from, String fecha_to) {
        Vista vista1 = new Vista(Caso.class);
        if (vista1.getFiltrosVistaList() == null) {
            vista1.setFiltrosVistaList(new ArrayList<FiltroVista>());

        }
//        FiltroVista filtroEstado = new FiltroVista();
//        filtroEstado.setIdCampo(Caso_.ESTADO);
//        filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
//        if (campoCompCasoEjeXSeriesEntity.getIdCampo().equals(Caso_.FECHA_CREACION)) {
//            filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
//        } else {
//            filtroEstado.setValor(EnumEstadoCaso.CERRADO.getEstado().getIdEstado());
//        }
//        filtroEstado.setIdVista(vista1);
//        vista1.getFiltrosVistaList().add(filtroEstado);
        //==================FECHA_CREACION/FECHA_X
        FiltroVista filtroFecha1 = new FiltroVista();
        filtroFecha1.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
        filtroFecha1.setIdComparador(EnumTipoComparacion.GE.getTipoComparacion());
        filtroFecha1.setValor(fecha_from);
        filtroFecha1.setValorLabel(fecha_from);
        filtroFecha1.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroFecha1);
        //--
        FiltroVista filtroFecha2 = new FiltroVista();
        filtroFecha2.setIdCampo(campoCompCasoEjeXSeriesEntity.getIdCampo());
        filtroFecha2.setIdComparador(EnumTipoComparacion.LT.getTipoComparacion());
        filtroFecha2.setValor(fecha_to);
        filtroFecha2.setValorLabel(fecha_to);
        filtroFecha2.setIdVista(vista1);
        vista1.getFiltrosVistaList().add(filtroFecha2);
        //==================
        return vista1;
    }

    @Override
    public PaginationHelper getPagination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the showFilter
     */
    public boolean isShowFilter() {
        return showFilter;
    }

    /**
     * @param showFilter the showFilter to set
     */
    public void setShowFilter(boolean showFilter) {
        this.showFilter = showFilter;
    }
}
