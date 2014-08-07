package com.itcs.helpdesk.jsfcontrollers.util;

import com.itcs.helpdesk.persistence.entities.FieldType;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.TipoComparacion;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumFieldType;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.persistence.jpa.AbstractJPAController;
import com.itcs.helpdesk.persistence.jpa.custom.CasoJPACustomController;
import com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade;
import com.itcs.helpdesk.persistence.utils.ComparableField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManagerFactory;
import javax.resource.NotSupportedException;

/**
 * Managed Beans that allow filtering of the data displayed should use/implement
 * helper class.
 *
 * @author jonathan
 */
public abstract class JPAFilterHelper implements Serializable {

    public static final String PLACE_HOLDER_CURRENT_USER_LABEL = "Usuario en Sesión";
    public static final String PLACE_HOLDER_NULL_LABEL = "Sin valor asignado (nulo)";
    public static final String PLACE_HOLDER_ANY_LABEL = "Cualquier valor distinto de nulo";
    private Vista vista;
    public List<ComparableField> comparableFields;
    private Map<String, ComparableField> comparableFieldsMap;

    private final transient EntityManagerFactory emf;

    public JPAFilterHelper(Vista v, EntityManagerFactory emf) {
        this.vista = v;
        this.emf = emf;
    }

    public Long count(Usuario who) throws IllegalStateException, ClassNotFoundException {
        return getJpaService().countEntities(vista, who);
    }

    public abstract JPAServiceFacade getJpaService();

    public List<ComparableField> getComparableFields() throws ClassNotFoundException {
        if (comparableFields == null) {
            comparableFields = getJpaService().getAnnotatedComparableFieldsByClass(Class.forName(vista.getBaseEntityType()));
        }
        return comparableFields;
    }

    public List<ComparableField> getEntityComparableFields() throws ClassNotFoundException {
        System.out.println("getComparableFieldsByFieldType()");
        List<ComparableField> comparableFieldsOfType = new ArrayList<ComparableField>();
        if (comparableFields == null) {
            comparableFields = getJpaService().getAnnotatedComparableFieldsByClass(Class.forName(vista.getBaseEntityType()));
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
            List<SelectItem> selectItems = new ArrayList<SelectItem>();

            if (includeAny) {
                selectItems.add(new SelectItem(CasoJPACustomController.PLACE_HOLDER_ANY, PLACE_HOLDER_ANY_LABEL));
            }
            if (includeNull) {
                selectItems.add(new SelectItem(CasoJPACustomController.PLACE_HOLDER_NULL, PLACE_HOLDER_NULL_LABEL));
            }

            if (includeCurrentUser && comparableField.getTipo().equals(Usuario.class)) {
                selectItems.add(new SelectItem(CasoJPACustomController.PLACE_HOLDER_CURRENT_USER, PLACE_HOLDER_CURRENT_USER_LABEL));
            }

            for (Object o : entities) {
                selectItems.add(new SelectItem(emf.getPersistenceUnitUtil().getIdentifier(o), o.toString()));
            }

            SelectItem[] selectArray = new SelectItem[selectItems.size()];
            return selectItems.toArray(selectArray);

        } else if (comparableField.getFieldTypeId().equals(EnumFieldType.CHECKBOX.getFieldType())) {
            //Boolean comparation
            //El valor es de tipo boolean, usar el String parseado a un boolean
            //two values, true or false.
            List<SelectItem> selectItems = new ArrayList<SelectItem>();
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
     * @param filtro
     * @return
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
            List<SelectItem> selectItems = new ArrayList<SelectItem>(2);
            selectItems.add(new SelectItem(CasoJPACustomController.PLACE_HOLDER_ANY, PLACE_HOLDER_ANY_LABEL));
            selectItems.add(new SelectItem(CasoJPACustomController.PLACE_HOLDER_NULL, PLACE_HOLDER_NULL_LABEL));
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

    public List<TipoComparacion> findTipoComparacionesAvailable(String idCampo) {
//        System.out.println("findTipoComparacionesAvailable(idCampo=" + idCampo + ")");
        try {
            final ComparableField comparableField = getComparableField(idCampo);
            if (comparableField != null) {
                return getTipoComparacionesAvailable(comparableField);
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

    /**
     * maybe this should be better to have cached in a map as application
     * variable.
     *
     * @param fieldType
     * @return
     */
    private List<TipoComparacion> getTipoComparacionesAvailable(ComparableField comparable) throws Exception {
        ArrayList<TipoComparacion> lista = null;
        try {
            FieldType fieldType = comparable.getFieldTypeId();
            if (fieldType.equals(EnumFieldType.TEXT.getFieldType()) || fieldType.equals(EnumFieldType.TEXTAREA.getFieldType())) {
                lista = new ArrayList<TipoComparacion>();
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                lista.add(EnumTipoComparacion.CO.getTipoComparacion());
            } else if (fieldType.equals(EnumFieldType.CALENDAR.getFieldType())) {
                //El valor es de tipo Fecha, usar el String parseado a una fecha
                lista = new ArrayList<TipoComparacion>();
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                lista.add(EnumTipoComparacion.LE.getTipoComparacion());
                lista.add(EnumTipoComparacion.GE.getTipoComparacion());
                lista.add(EnumTipoComparacion.LT.getTipoComparacion());
                lista.add(EnumTipoComparacion.GT.getTipoComparacion());
                lista.add(EnumTipoComparacion.BW.getTipoComparacion());
            } else if (fieldType.equals(EnumFieldType.CHECKBOX.getFieldType())) {
                lista = new ArrayList<TipoComparacion>(2);
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
            } else if (fieldType.equals(EnumFieldType.SELECTONE_ENTITY.getFieldType())) {
                if (comparable.getTipo().equals(List.class)) {
                    lista = new ArrayList<TipoComparacion>(1);
                    lista.add(EnumTipoComparacion.SC.getTipoComparacion());
                } else {
                    lista = new ArrayList<TipoComparacion>(3);
                    lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                    lista.add(EnumTipoComparacion.NE.getTipoComparacion());
                    lista.add(EnumTipoComparacion.SC.getTipoComparacion());
                }

            } else if (fieldType.equals(EnumFieldType.SELECTONE_PLACE_HOLDER.getFieldType())) {
                lista = new ArrayList<TipoComparacion>(2);
                lista.add(EnumTipoComparacion.EQ.getTipoComparacion());
                lista.add(EnumTipoComparacion.NE.getTipoComparacion());
            } else if (fieldType.equals(EnumFieldType.COMMA_SEPARATED_VALUELIST.getFieldType())) {
                lista = new ArrayList<TipoComparacion>(1);
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

    /**
     * @return the vista
     */
    public Vista getVista() {
        return vista;
    }

    /**
     * @param vista the vista to set
     */
    public void setVista(Vista vista) {
        this.vista = vista;
    }

    /**
     * @return the comparableFieldsMap
     */
    public Map<String, ComparableField> getComparableFieldsMap() throws ClassNotFoundException {
        if (comparableFieldsMap == null) {
            comparableFieldsMap = getJpaService().getAnnotatedComparableFieldsMap(getComparableFields());
        }
        return comparableFieldsMap;
    }
}
