package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.persistence.entities.FieldType;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.TipoComparacion;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entityenums.EnumFieldType;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.ComparableField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManagerFactory;
import javax.resource.NotSupportedException;
import org.apache.commons.lang3.StringUtils;

/**
 * Managed Beans that allow filtering of the data displayed should use/implement
 * helper class.
 *
 * @author jonathan
 */
public abstract class JPAFilterHelper implements Serializable {

    //TODO make a bundle message for i18n for this labels
    public static final String PLACE_HOLDER_CURRENT_USER_LABEL = "Usuario en Sesión";
    public static final String PLACE_HOLDER_NULL_LABEL = "Sin valor asignado (nulo)";
    public static final String PLACE_HOLDER_ANY_LABEL = "Cualquier valor distinto de nulo";
//    private Vista vista;
    private final String baseEntityClassName;
    public List<ComparableField> comparableFields;
    private Map<String, ComparableField> comparableFieldsMap;

//    private final transient EntityManagerFactory emf;
//    private final transient JPAServiceFacade jpaService;

    public JPAFilterHelper(String baseEntityClassName) {
        this.baseEntityClassName = baseEntityClassName;
//        this.jpaService = jpaService;
    }

//    public Long count(Usuario who) throws IllegalStateException, ClassNotFoundException {
//        return getJpaService().countEntities(vista, who);
//    }
    public abstract JPAServiceFacade getJpaService();

    public List<ComparableField> getComparableFields() {
        if (comparableFields == null) {
            try {
                comparableFields = getJpaService().getAnnotatedComparableFieldsByClass(Class.forName(getBaseEntityClassName()));
                Collections.sort(comparableFields);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(JPAFilterHelper.class.getName()).log(Level.SEVERE, null, ex);
                comparableFields = Collections.EMPTY_LIST;
            }
        }
        return comparableFields;
    }

    public List<ComparableField> getEntityOrCalendarComparableFields() throws ClassNotFoundException {
        List<ComparableField> comparableFieldsOfType = new ArrayList<>();
        if (comparableFields == null) {
            comparableFields = getJpaService().getAnnotatedComparableFieldsByClass(Class.forName(getBaseEntityClassName()));
        }
        for (ComparableField comparableField : comparableFields) {
            if (comparableField.getFieldTypeId().equals(EnumFieldType.SELECTONE_ENTITY.getFieldType())
                    || comparableField.getFieldTypeId().equals(EnumFieldType.CALENDAR.getFieldType())) {
                comparableFieldsOfType.add(comparableField);
            }
        }
        return comparableFieldsOfType;
//         return JsfUtil.getSelectItems(Collections.EMPTY_LIST, true);
    }

    public List<ComparableField> getEntityComparableFields() throws ClassNotFoundException {
        List<ComparableField> comparableFieldsOfType = new ArrayList<>();
        if (comparableFields == null) {
            comparableFields = getJpaService().getAnnotatedComparableFieldsByClass(Class.forName(getBaseEntityClassName()));
        }
        for (ComparableField comparableField : comparableFields) {
            if (comparableField.getFieldTypeId().equals(EnumFieldType.SELECTONE_ENTITY.getFieldType())) {
                comparableFieldsOfType.add(comparableField);
            }
        }
        return comparableFieldsOfType;
//         return JsfUtil.getSelectItems(Collections.EMPTY_LIST, true);
    }

    //TODO use the query param to do the search!
    public List<SelectItem> autoCompleteFindPosibleOptionsIncludingAllPlaceHolders(String query/*String idCampo, Usuario who*/) throws Exception {

        FacesContext context = FacesContext.getCurrentInstance();

        FiltroVista filterObject = (FiltroVista) UIComponent.getCurrentComponent(context).getAttributes().get("filtro");
        UserSessionBean userSessionBean = context.getApplication().evaluateExpressionGet(context, "#{UserSessionBean}", UserSessionBean.class);
//        FiltroVista filterObject = context.getApplication().evaluateExpressionGet(context, "#{filtro}", FiltroVista.class);

        return findEntitiesByAutocompleteQuery(query, filterObject.getIdCampo(), true, true, true, userSessionBean.getCurrent());
    }

//    public SelectItem[] autoCompleteFindPosibleOptionsFor(String query) {
//        System.out.println(query);
//        //List<EmailCliente> results = new ArrayList<EmailCliente>();
//        List<EmailCliente> emailClientes = getJpaController().getEmailClienteFindByEmailLike(query, 10);
////        System.out.println(emailClientes);
//        if (emailClientes != null && !emailClientes.isEmpty()) {
//            return emailClientes;
//        } else {
////            emailCliente_wizard_existeEmail = false;
//            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe el email:" + query, "No existe el Cliente con email:" + query + ". Se creará automáticamente.");
//            FacesContext.getCurrentInstance().addMessage(null, message);
//            emailClientes = new ArrayList<>();
//            emailClientes.add(new EmailCliente(query));
//            System.out.println("No existe el Cliente con email" + query);
//            return emailClientes;
//        }
//
//    }
    private List<SelectItem> findEntitiesByAutocompleteQuery(String query, String idCampo, boolean includeAny, boolean includeNull, boolean includeCurrentUser, Usuario who) {
        ComparableField comparableField = comparableFieldsMap.get(idCampo);

        if (comparableField == null) {
            return null;
        }

        if (comparableField.getFieldTypeId() == null) {
            return null;
        }

        if (comparableField.getFieldTypeId().equals(EnumFieldType.SELECTONE_ENTITY.getFieldType())) {
            //El valor es el id de un entity, que tipo de Entity?= comparableField.tipo

            List<?> entities = getJpaService().findEntitiesBySearchQuery(query, comparableFieldsMap.get(idCampo), who);

            List<SelectItem> selectItems = new LinkedList<>();

            if (StringUtils.isEmpty(query)) {
                selectItems.addAll(getPlaceHolderItems(includeAny, includeNull, includeCurrentUser, comparableField));
            }

            for (Object o : entities) {
                selectItems.add(new SelectItem(getJpaService().getIdentifier(o), o.toString()));
            }

            return (selectItems);

        } else if (comparableField.getFieldTypeId().equals(EnumFieldType.CHECKBOX.getFieldType())) {
            //Boolean comparation
            //El valor es de tipo boolean, usar el String parseado a un boolean
            //two values, true or false.
            List<SelectItem> selectItems = new ArrayList<>();
            selectItems.add(new SelectItem("true", "Si"));
            selectItems.add(new SelectItem("false", "No"));
            return (selectItems);

        } else {
            return null;
        }
    }

    private List<SelectItem> getPlaceHolderItems(boolean includeAny, boolean includeNull, boolean includeCurrentUser, ComparableField comparableField) {
        List<SelectItem> selectItems = new ArrayList<>();
        if (includeAny) {
            selectItems.add(new SelectItem(AbstractJPAController.PLACE_HOLDER_ANY, PLACE_HOLDER_ANY_LABEL));
        }
        if (includeNull) {
            selectItems.add(new SelectItem(AbstractJPAController.PLACE_HOLDER_NULL, PLACE_HOLDER_NULL_LABEL));
        }
        if (includeCurrentUser && comparableField.getTipo().equals(Usuario.class)) {
            selectItems.add(new SelectItem(AbstractJPAController.PLACE_HOLDER_CURRENT_USER, PLACE_HOLDER_CURRENT_USER_LABEL));
        }
        return selectItems;
    }

    private SelectItem[] findPosibleOptionsFor(String idCampo, boolean includeAny, boolean includeNull, boolean includeCurrentUser, Usuario who) {
        ComparableField comparableField = comparableFieldsMap.get(idCampo);

        if (comparableField == null) {
            return null;
        }

        if (comparableField.getFieldTypeId() == null) {
            return null;
        }

        if (comparableField.getFieldTypeId().equals(EnumFieldType.SELECTONE_ENTITY.getFieldType())) {
            //El valor es el id de un entity, que tipo de Entity?= comparableField.tipo

            List<?> entities = getJpaService().findPosibleDBOptionsFor(comparableFieldsMap.get(idCampo), who);
            List<SelectItem> selectItems = getPlaceHolderItems(includeAny, includeNull, includeCurrentUser, comparableField);

            for (Object o : entities) {
                selectItems.add(new SelectItem(getJpaService().getIdentifier(o), o.toString()));
            }

            SelectItem[] selectArray = new SelectItem[selectItems.size()];
            return selectItems.toArray(selectArray);

        } else if (comparableField.getFieldTypeId().equals(EnumFieldType.CHECKBOX.getFieldType())) {
            //Boolean comparation
            //El valor es de tipo boolean, usar el String parseado a un boolean
            //two values, true or false.
            List<SelectItem> selectItems = new ArrayList<>();
            selectItems.add(new SelectItem("true", "Si"));
            selectItems.add(new SelectItem("false", "No"));
            SelectItem[] selectArray = new SelectItem[selectItems.size()];
            return selectItems.toArray(selectArray);

        } else {
            return null;
        }
    }

    /**
     * TODO: Tambien hay q tener en cuenta algunas restricciones de acceso a la
     * lista de posibles valores para un campo. Por ejemplo si un agente solo
     * tiene accesso a ciertas categorias o areas no podemos retornar un
     * findall, sino que hay que filtrar el resultado a solamente lo que puede
     * ver. Tarea pa la casa, ojala alguna dia alguien se aplique.
     *
     * @param idCampo
     * @param who
     * @return SelectItem[]
     * @throws java.lang.Exception
     */
    public SelectItem[] findPosibleOptions(String idCampo, Usuario who) throws Exception {
        return findPosibleOptionsFor(idCampo, false, false, false, who);
    }

    public SelectItem[] findPosibleOptionsIncludingAllPlaceHolders(String idCampo, Usuario who) throws Exception {
        return findPosibleOptionsFor(idCampo, true, true, true, who);
    }

    public SelectItem[] findPosibleOptionsGenericEntityPlaceHolders(String idCampo) throws Exception {
        ComparableField comparableField = comparableFieldsMap.get(idCampo);

        if (comparableField == null) {
            return null;
        }

        if (comparableField.getFieldTypeId() == null) {
            return null;
        }

        if (comparableField.getFieldTypeId().equals(EnumFieldType.SELECTONE_PLACE_HOLDER.getFieldType())) {
            //El valor es el id de un entity, que tipo de Entity?= comparableField.tipo
            List<SelectItem> selectItems = new ArrayList<>(2);
            selectItems.add(new SelectItem(AbstractJPAController.PLACE_HOLDER_ANY, PLACE_HOLDER_ANY_LABEL));
            selectItems.add(new SelectItem(AbstractJPAController.PLACE_HOLDER_NULL, PLACE_HOLDER_NULL_LABEL));
            SelectItem[] selectArray = new SelectItem[selectItems.size()];
            return selectItems.toArray(selectArray);

        } else {
            return null;
        }
    }

    public SelectItem[] findPosibleOptionsSelectManyIncludeNullFor(String idCampo, Usuario who) throws Exception {
        return findPosibleOptionsFor(idCampo, false, true, false, who);
    }

    public SelectItem[] findPosibleOptionsSelectManyIncludeNullAndUserInSessionFor(String idCampo, Usuario who) throws Exception {
        return findPosibleOptionsFor(idCampo, false, true, true, who);
    }

    /**
     * TODO add a param to identify if this should show the CHANGE TO operator
     * option
     *
     * @param idCampo
     * @param changeOps flag indicating that we want to display the CT Change To
     * operator in the operator list.
     * @return
     */
    public List<TipoComparacion> findTipoComparacionesAvailable(String idCampo, boolean changeOps) {
//        System.out.println("findTipoComparacionesAvailable(idCampo=" + idCampo + ")");
        try {
            final ComparableField comparableField = getComparableField(idCampo);
            if (comparableField != null) {
                return getTipoComparacionesAvailable(comparableField, changeOps);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JPAFilterHelper.class.getName()).log(Level.SEVERE, "findTipoComparacionesAvailable", ex);
        } catch (Exception ex) {
            Logger.getLogger(JPAFilterHelper.class.getName()).log(Level.SEVERE, "getTipoComparacionesAvailable", ex);
        }
        return null;
    }

    public ComparableField getComparableField(String idCampo) throws ClassNotFoundException {
        final ComparableField comparableField = getComparableFieldsMap().get(idCampo);
        return comparableField;
    }

    private List<TipoComparacion> getTipoComparacionesAvailable(ComparableField comparable) throws Exception {
        return getTipoComparacionesAvailable(comparable, false);
    }

    /**
     * maybe this should be better to have cached in a map as application
     * variable.
     *
     * @param fieldType
     * @return
     */
    private List<TipoComparacion> getTipoComparacionesAvailable(ComparableField comparable, boolean changeOps) throws Exception {
        ArrayList<TipoComparacion> lista = null;
        try {
            FieldType fieldType = comparable.getFieldTypeId();
            if (fieldType.equals(EnumFieldType.TEXT.getFieldType()) || fieldType.equals(EnumFieldType.TEXTAREA.getFieldType())) {
                lista = new ArrayList<>();
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                lista.add(EnumTipoComparacion.CO.getTipoComparacion());
                if (changeOps) {
                    lista.add(EnumTipoComparacion.CT.getTipoComparacion());
                }
            } else if (fieldType.equals(EnumFieldType.NUMBER.getFieldType())) {

                lista = new ArrayList<>();
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                lista.add(EnumTipoComparacion.GE.getTipoComparacion());
                lista.add(EnumTipoComparacion.GT.getTipoComparacion());
                lista.add(EnumTipoComparacion.LE.getTipoComparacion());
                lista.add(EnumTipoComparacion.LT.getTipoComparacion());

            } else if (fieldType.equals(EnumFieldType.CALENDAR.getFieldType())) {
                //El valor es de tipo Fecha, usar el String parseado a una fecha
                lista = new ArrayList<>();
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                lista.add(EnumTipoComparacion.LE.getTipoComparacion());
                lista.add(EnumTipoComparacion.GE.getTipoComparacion());
                lista.add(EnumTipoComparacion.LT.getTipoComparacion());
                lista.add(EnumTipoComparacion.GT.getTipoComparacion());
                lista.add(EnumTipoComparacion.BW.getTipoComparacion());
            } else if (fieldType.equals(EnumFieldType.CHECKBOX.getFieldType())) {
                lista = new ArrayList<>(2);
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                if (changeOps) {
                    lista.add(EnumTipoComparacion.CT.getTipoComparacion());
                }
            } else if (fieldType.equals(EnumFieldType.SELECTONE_ENTITY.getFieldType())) {
                if (comparable.getTipo().equals(List.class)) {
                    lista = new ArrayList<>(1);
                    lista.add(EnumTipoComparacion.SC.getTipoComparacion());
                } else {
                    lista = new ArrayList<>(3);
                    lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                    lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                    lista.add(EnumTipoComparacion.SC.getTipoComparacion());
                    if (changeOps) {
                        lista.add(EnumTipoComparacion.CT.getTipoComparacion());
                    }
                }

            } else if (fieldType.equals(EnumFieldType.SELECTONE_PLACE_HOLDER.getFieldType())) {
                lista = new ArrayList<>(2);
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                if (changeOps) {
                    lista.add(EnumTipoComparacion.CT.getTipoComparacion());
                }
            } else if (fieldType.equals(EnumFieldType.COMMA_SEPARATED_VALUELIST.getFieldType())) {
                lista = new ArrayList<>(1);
                lista.add(EnumTipoComparacion.IM.getTipoComparacion());
            } else {
                throw new NotSupportedException("fieldType " + fieldType.getFieldTypeId() + " is not supported yet!!");
            }
        } catch (Exception ex) {
            Logger.getLogger(AbstractJPAController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista;
    }

    /**
     * Aplies the list of Filters to the logic that query the datasource. it
     * should be used inside the createPageDataModel implementation specific to
     * the controller.
     *
     * @param filtro FiltroVista object
     */
//    public abstract void filter();
    public void handleIdCampoChangeEvent(FiltroVista filtro) {
        filtro.setIdComparador(null);
        filtro.setValor(null);
        filtro.setValor2(null);
    }

    public void handleOperadorChangeEvent(FiltroVista filtro) {
//        filtro.setValor(null);
//        filtro.setValor2(null);
    }

//    /**
//     * @return the vista
//     */
//    public Vista getVista() {
//        return vista;
//    }
//
//    /**
//     * @param vista the vista to set
//     */
//    public void setVista(Vista vista) {
//        this.vista = vista;
//    }
    /**
     * @return the comparableFieldsMap
     */
    public Map<String, ComparableField> getComparableFieldsMap() {
        if (comparableFieldsMap == null) {
            comparableFieldsMap = getJpaService().getAnnotatedComparableFieldsMap(getComparableFields());
        }
        return comparableFieldsMap;
    }

    /**
     * @return the baseEntityClassName
     */
    public String getBaseEntityClassName() {
        return baseEntityClassName;
    }
}
