package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Caso_;
import com.itcs.helpdesk.persistence.entities.Categoria;
import com.itcs.helpdesk.persistence.entities.FiltroVista;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.persistence.entities.Vista;
import com.itcs.helpdesk.persistence.entityenums.EnumEstadoCaso;
import com.itcs.helpdesk.persistence.entityenums.EnumTipoComparacion;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.resource.NotSupportedException;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;


@ManagedBean(name = "categoriaController")
@SessionScoped
public class CategoriaController extends AbstractManagedBean<Categoria> implements Serializable {

    @ManagedProperty(value = "#{UserSessionBean}")
    private UserSessionBean userSessionBean;
//    private Categoria current;
//    private Categoria[] selectedItems;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
    private int selectedItemIndex;
    private transient TreeNode categorias;
    private transient TreeNode selectedNode;
    private transient TreeNode categoria = null;
    private Categoria portapapeles = null;
    private int ordenMayor = 0;
    private String textoFiltro;
    private List<Categoria> emptyList;
//    private Area idArea;

    public CategoriaController() {
        super(Categoria.class);
        emptyList = new LinkedList<Categoria>();
    }

    public String getTextoFiltro() {
        return textoFiltro;
    }

    public void setTextoFiltro(String textoFiltro) {
        this.textoFiltro = textoFiltro;
    }

    public int calculateOpenCases(Categoria cat) {
        Vista vista1 = new Vista(Caso.class);
        vista1.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
        vista1.setNombre("Casos de la Categoria " + cat.getNombre());

        FiltroVista filtroEstado = new FiltroVista();
        filtroEstado.setIdCampo(Caso_.ESTADO_FIELD_NAME);
        filtroEstado.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroEstado.setValor(EnumEstadoCaso.ABIERTO.getEstado().getIdEstado());
        filtroEstado.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroEstado);

        FiltroVista filtroCategoria = new FiltroVista();
        filtroCategoria.setIdCampo(Caso_.CATEGORIA_FIELD_NAME);
        filtroCategoria.setIdComparador(EnumTipoComparacion.EQ.getTipoComparacion());
        filtroCategoria.setValor(cat.getIdCategoria().toString());
        filtroCategoria.setIdVista(vista1);

        vista1.getFiltrosVistaList().add(filtroCategoria);
        try {
            return getJpaController().countCasoEntities(vista1, userSessionBean.getCurrent());

//            return getJpaController().countByCatFilter(cat, ((UserSessionBean) JsfUtil.getManagedBean("UserSessionBean")).getCurrent());
        } catch (NotSupportedException ex) {
            JsfUtil.addErrorMessage(ex.getMessage());
            Logger.getLogger(CategoriaController.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } catch (ClassNotFoundException ex) {
            JsfUtil.addErrorMessage(ex.getMessage());
            Logger.getLogger(CategoriaController.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public Categoria getSelected() {
        if (current == null) {
            current = new Categoria();
            selectedItemIndex = -1;
        }
        return current;
    }

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(Categoria.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(Categoria.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        textoFiltro = null;
        categorias = null;
        setCategoria(null);
        recreateModel();
        return "/script/categoria/Admin";
    }

    public String prepareView() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Es requerido que seleccione una fila para visualizar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/categoria/View";
    }

    public String prepareCreate() {
        current = new Categoria();
        selectedItemIndex = -1;
        return "/script/categoria/Create";
    }

    public String create() {
        try {
            Categoria selected = null;
            if (categoria != null) {
                selected = (Categoria) categoria.getData();
                current.setIdCategoriaPadre(selected);
            }
            current.setOrden(ordenMayor + 1);
            getJpaController().persistCategoria(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CategoriaCreated"));
            categorias = null;
            setCategoria(null);
            return "/script/categoria/Admin";
        } catch (Exception e) {
            e.printStackTrace();
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(Integer idCategoria) {
        current = getJpaController().getCategoriaFindByIdCategoria(idCategoria);
        return "/script/categoria/Edit";
    }

    public String prepareEdit() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/categoria/Edit";
    }

    public String update() {
        try {
            getJpaController().mergeCategoria(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CategoriaUpdated"));
            categorias = null;
            return "/script/categoria/View";
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
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
        return "/script/categoria/List";
    }

    public String destroySelected() {

        if (getSelectedItems().size() <= 0) {
            return "";
        } else {
            for (int i = 0; i < getSelectedItems().size(); i++) {
                current = getSelectedItems().get(0);
                performDestroy();
            }
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        recreateModel();
        return "/script/categoria/Admin";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/categoria/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/categoria/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeCategoria(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CategoriaDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        long count = getJpaController().count(Categoria.class);
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = (int) count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Categoria) getJpaController().queryByRange(Categoria.class, 1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<Categoria> listOfCategoria = new ArrayList<Categoria>();
        while (iter.hasNext()) {
            listOfCategoria.add((Categoria) iter.next());
        }

        return new CategoriaDataModel(listOfCategoria);
    }

    public List<Categoria> getCategoriasForUser(Usuario usuario, String textFiltro) {

        List<Categoria> lista = new LinkedList<Categoria>();

        for (Grupo grupo : usuario.getGrupoList()) {
            for (Categoria cat : grupo.getCategoriaList()) {
                if (!lista.contains(cat)) {
                    lista.add(cat);
                }
            }
        }
        if ((textFiltro != null) && (!textFiltro.trim().isEmpty())) {
            List<Categoria> filteredList = new LinkedList<Categoria>();
            for (Categoria localCat : lista) {
                if (localCat.getNombre().toLowerCase().contains(textFiltro.toLowerCase())) {
                    localCat.setIdCategoriaPadre(null);
                    localCat.setCategoriaList(emptyList);
                    filteredList.add(localCat);
                }
            }
            lista = filteredList;
        }
        Collections.sort(lista, new Comparator<Categoria>() {
            @Override
            public int compare(Categoria o1, Categoria o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        return lista;
    }

    public DataModel getItemsNoPagination() {
        List<Categoria> lista = null;

        if ((textoFiltro != null) && (!textoFiltro.trim().isEmpty())) {
            lista = getJpaController().getCategoriaFindByNombreLike(textoFiltro);
            for (Categoria localCat : lista) {
                localCat.setIdCategoriaPadre(null);
                localCat.setCategoriaList(emptyList);
            }
        } else {
            lista = getJpaController().getCategoriaFindAll();
        }
        items = new ListDataModel(lista);

        Iterator iter = items.iterator();
        List<Categoria> listOfCategoria = new ArrayList<Categoria>();
        while (iter.hasNext()) {
            listOfCategoria.add((Categoria) iter.next());
        }
        Collections.sort(listOfCategoria, new Comparator<Categoria>() {
            @Override
            public int compare(Categoria o1, Categoria o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });

        return new CategoriaDataModel(listOfCategoria);
    }

    public boolean puedeCortar() {
        if (categoria == null) {
            return false;
        }
        return ((Categoria) categoria.getData()).isEditable();
        //return !((Categoria) categoria.getData()).equals(EnumCategorias.TODOS.getCategoria());
    }

    public boolean puedePegar() {
        if (portapapeles == null || categoria == null) {
            return false;
        }
        Categoria selected = (Categoria) categoria.getData();

        if (selected == null) {
            return false;
        }

        if (selected.equals(portapapeles)) {
            return false;
        }
        if (selected.equals(portapapeles.getIdCategoriaPadre())) {
            return false;
        }
        if (esDescendienteDeB(selected, portapapeles)) {
            return false;
        }

        return true;
    }

    public boolean esDescendienteDeB(Categoria catA, Categoria catB) {
        if (catA.getIdCategoriaPadre() == null) {
            return false;
        }
        if (catA.getIdCategoriaPadre().equals(catB)) {
            return true;
        } else {
            return esDescendienteDeB(catA.getIdCategoriaPadre(), catB);
        }
    }

    public void cortarCategoria(ActionEvent actionEvent) {
        portapapeles = (Categoria) categoria.getData();
        JsfUtil.addSuccessMessage("Categoria " + portapapeles.getNombre() + "\nalmacenada en el portapapeles");
    }

    public void crearCategoria(ActionEvent actionEvent) {
        try {
            Categoria categoriaPadre = null;
            if (categoria != null && categoria.getData() != null) {
                categoriaPadre = (Categoria) categoria.getData();
                current.setIdArea(categoriaPadre.getIdArea());
            }
            current.setIdCategoriaPadre(categoriaPadre);
            current.setOrden(ordenMayor + 1);
            current.setEditable(true);
            getJpaController().persistCategoria(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CategoriaCreated"));
            categorias = null;
            setCategoria(null);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e);
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void eliminaCategoria(ActionEvent actionEvent) {
        current = (Categoria) categoria.getData();
        performDestroy();
        current = null;
        categorias = null;
        portapapeles = null;
        setCategoria(null);
    }

    public void pegarCategoria(ActionEvent actionEvent) {
        Categoria selected = (Categoria) categoria.getData();
        portapapeles.setIdCategoriaPadre(selected);
        try {
            getJpaController().mergeCategoria(portapapeles);
            categorias = null;
            portapapeles = null;
            setCategoria(null);

        } catch (Exception ex) {
            Log.createLogger(CategoriaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean puedeRenombrar() {
        if (categoria == null) {
            return false;
        }
        Categoria selected = (Categoria) categoria.getData();
        if (!selected.isEditable()) {
            return false;
        }
        return true;
    }

    public boolean puedeBorrarCategoria() {
        if (categoria == null) {
            return false;
        }
        Categoria selected = (Categoria) categoria.getData();
        if (!selected.isEditable()) {
            return false;
        }
        if (selected.getCategoriaList().size() > 0) {
            return false;
        }
        if (selected.getCasoList().size() > 0) {
            return false;
        }
        return true;
    }

//    public boolean puedeCrearCategoria() {
//        if (categoria == null) {
//            return false;
//        }
//        Categoria selected = ((Categoria) categoria.getData());
//        if (selected == null) {
//            return false;
//        }
//        return true;
//    }
    public boolean puedeBajarCategoria() {
        if (categoria == null) {
            return false;
        }
        Categoria selected = ((Categoria) categoria.getData());
        if (!selected.isEditable()) {
            return false;
        }
        if (selected.getIdCategoriaPadre() == null) {
            return false;
        }
        if (selected.getIdCategoriaPadre().getCategoriaList().size() <= 1) {
            return false;
        }
        List<Categoria> cats = (List) selected.getIdCategoriaPadre().getCategoriaList();
        Collections.sort(cats, new Comparator<Categoria>() {
            @Override
            public int compare(Categoria o1, Categoria o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        if (selected.equals(cats.get(cats.size() - 1))) {
            return false;
        }
        return true;
    }

    public boolean puedeSubirCategoria() {
        if (categoria == null) {
            return false;
        }
        Categoria selected = ((Categoria) categoria.getData());
        if (!selected.isEditable()) {
            return false;
        }
        if (selected.getIdCategoriaPadre() == null) {
            return false;
        }
        if (selected.getIdCategoriaPadre().getCategoriaList().size() <= 1) {
            return false;
        }
        List<Categoria> cats = (List) selected.getIdCategoriaPadre().getCategoriaList();
        Collections.sort(cats, new Comparator<Categoria>() {
            @Override
            public int compare(Categoria o1, Categoria o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        if (selected.equals(cats.get(0))) {
            return false;
        }
        return true;
    }

    public void bajarCategoria(ActionEvent actionEvent) throws Exception {
        Categoria selected = (Categoria) categoria.getData();
        List<Categoria> cats = (List) selected.getIdCategoriaPadre().getCategoriaList();
        Collections.sort(cats, new Comparator<Categoria>() {
            @Override
            public int compare(Categoria o1, Categoria o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        for (int i = 0; i < cats.size(); i++) {
            if (cats.get(i).equals(selected)) {
                Categoria catAbajo = cats.get(i + 1);
                int ordenA = selected.getOrden();
                int ordenB = catAbajo.getOrden();
                selected.setOrden(ordenB);
                catAbajo.setOrden(ordenA);
                getJpaController().mergeCategoria(selected);
                getJpaController().mergeCategoria(catAbajo);
                categorias = null;
                setCategoria(null);
                break;
            }
        }

    }

    public void prepareCreateAjax(ActionEvent actionEvent) {
        current = new Categoria();
        Categoria categoriaPadre = null;
        if (categoria != null && categoria.getData() != null) {
            categoriaPadre = (Categoria) categoria.getData();
            current.setIdArea(categoriaPadre.getIdArea());
            current.setIdCategoriaPadre(categoriaPadre);
        }
        selectedItemIndex = -1;
    }

    public void prepareRenombrarAjax(ActionEvent actionEvent) {
        Categoria selected = (Categoria) categoria.getData();
        current = new Categoria();
        current.setNombre(selected.getNombre());
    }

    public void renombrar(ActionEvent actionEvent) {
        try {
            Categoria selected = (Categoria) categoria.getData();
            selected.setNombre(current.getNombre());
            getJpaController().mergeCategoria(selected);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CategoriaUpdated"));
            categorias = null;
            setCategoria(null);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void subirCategoria(ActionEvent actionEvent) throws Exception {
        Categoria selected = (Categoria) categoria.getData();
        List<Categoria> cats = (List) selected.getIdCategoriaPadre().getCategoriaList();
        Collections.sort(cats, new Comparator<Categoria>() {
            @Override
            public int compare(Categoria o1, Categoria o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        for (int i = 0; i < cats.size(); i++) {
            if (cats.get(i).equals(selected)) {
                Categoria catArriba = cats.get(i - 1);
                int ordenA = selected.getOrden();
                int ordenB = catArriba.getOrden();
                selected.setOrden(ordenB);
                catArriba.setOrden(ordenA);
                getJpaController().mergeCategoria(selected);
                getJpaController().mergeCategoria(catArriba);
                categorias = null;
                setCategoria(null);
                break;
            }
        }
    }

    public String getNombreCategoriaSeleccionada() {
        if (categoria == null) {
            return "";
        }
        Categoria selected = (Categoria) categoria.getData();
        return selected.getNombre();
    }

    public TreeNode getCategoria() {
        return categoria;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        //System.out.println("categoria " + ((Categoria) categoria.getData()).getNombre() + " seleccionada");
        filtrarCategorias();
    }

    public void setCategoria(TreeNode categoria) {
        //System.out.println(getNombreCategoriaSeleccionada());
        this.categoria = categoria;
    }

    public void filtrarCategorias() {
        categorias = null;
    }

    public TreeNode getCategorias(Usuario user) {
        if (categorias != null) {
            if(categorias.getChildCount() != 0)
            {
                return categorias;
            }
        }
        Usuario usuario = getJpaController().getUsuarioFindByIdUsuario(user.getIdUsuario());
        List<Categoria> localCats = getCategoriasForUser(usuario, this.textoFiltro);
        DataModel localDataModel = new CategoriaDataModel(localCats);

        if (localDataModel != null) {
            //System.out.println("se reconstruye el arbol");
            Iterator it = localDataModel.iterator();
            categorias = new DefaultTreeNode("Categoria", null);
            categorias.setExpanded(true);
            while (it.hasNext()) {
                Categoria cat = (Categoria) it.next();
                verificaSiEsOrdenMayor(cat.getOrden());
                if ((cat.getIdCategoriaPadre() == null) || (!localCats.contains(cat.getIdCategoriaPadre()))) {
                    if (cat.getCategoriaList().isEmpty()) {
                        TreeNode subCategorias = new DefaultTreeNode(cat, categorias);
                        subCategorias.setExpanded(true);
                    } else {
                        TreeNode subCategorias = new DefaultTreeNode(cat, categorias);
                        subCategorias.setExpanded(true);
                        setCategorias(cat, subCategorias);
                    }
                }
            }
        } else {
            categorias = new DefaultTreeNode("Categoria", null);
        }

        return categorias;
    }

    /**
     * Lista de categorias para Tree PrimeFace
     *
     * @return
     */
    public TreeNode getCategorias() {

        if (categorias != null) {
            return categorias;
        }

        items = getItemsNoPagination();

        if (items != null) {
            //System.out.println("se reconstruye el arbol");
            Iterator it = items.iterator();
            categorias = new DefaultTreeNode("Categoria", null);
            categorias.setExpanded(true);
            while (it.hasNext()) {
                Categoria cat = (Categoria) it.next();
                verificaSiEsOrdenMayor(cat.getOrden());
                if (cat.getIdCategoriaPadre() == null) {
                    if (cat.getCategoriaList().isEmpty()) {
                        TreeNode subCategorias = new DefaultTreeNode(cat, categorias);
                        subCategorias.setExpanded(true);
                    } else {
                        TreeNode subCategorias = new DefaultTreeNode(cat, categorias);
                        subCategorias.setExpanded(true);
                        setCategorias(cat, subCategorias);
                    }
                }
            }
        } else {
            categorias = new DefaultTreeNode("Categoria", null);
        }

        return categorias;
    }

    private void verificaSiEsOrdenMayor(int orden) {
        if (orden > ordenMayor) {
            ordenMayor = orden;
        }
    }

    /**
     * Crea el arbol de categoria desde un Padre bastardo.
     *
     * @param categoria Categoria Padre
     * @param subCategorias TreeNode Padre
     * @return
     */
    private void setCategorias(Categoria categoria, TreeNode subCategorias) {

        List<Categoria> cats = (List) categoria.getCategoriaList();
        Collections.sort(cats, new Comparator<Categoria>() {
            @Override
            public int compare(Categoria o1, Categoria o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        Iterator it = cats.iterator();
        while (it.hasNext()) {
            Categoria cat = (Categoria) it.next();
            verificaSiEsOrdenMayor(cat.getOrden());
            if (cat.getCategoriaList().isEmpty()) {
                TreeNode cate = new DefaultTreeNode(cat, subCategorias);
                cate.setExpanded(true);
            } else {
                TreeNode cate = new DefaultTreeNode(cat, subCategorias);
                cate.setExpanded(true);
                setCategorias(cat, cate);
            }

        }
    }

    public void setCategorias(TreeNode categorias) {
        this.categorias = categorias;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

//    /**
//     * @return the selectedItems
//     */
//    public Categoria[] getSelectedItems() {
//        return selectedItems;
//    }
//
//    /**
//     * @param selectedItems the selectedItems to set
//     */
//    public void setSelectedItems(Categoria[] selectedItems) {
//        this.selectedItems = selectedItems;
//    }

//    private void recreateModel() {
//        items = null;
//    }

//    public String next() {
//        getPagination().nextPage();
//        recreateModel();
//        return "/script/categoria/List";
//    }
//
//    public String previous() {
//        getPagination().previousPage();
//        recreateModel();
//        return "/script/categoria/List";
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

//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(getJpaController().getCategoriaFindAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(getJpaController().getCategoriaFindAll(), true);
//    }

    /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FacesConverter(value = "CategoriaConverter", forClass = Categoria.class)
    public static class CategoriaControllerConverter implements Converter, Serializable {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            //System.out.println("CategoriaControllerConverter.getAsObject()");
            if (value == null || value.length() == 0) {
                return null;
            }
            CategoriaController controller = (CategoriaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "categoriaController");
            return controller.getJpaController().getCategoriaFindByIdCategoria(getKey(value));
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
//             System.out.println("CategoriaControllerConverter.getAsString()");
            if (object == null) {
                return null;
            }
            if (object instanceof Categoria) {
                Categoria o = (Categoria) object;
                return getStringKey(o.getIdCategoria());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CategoriaController.class.getName());
            }
        }
    }
}
class CategoriaDataModel extends ListDataModel<Categoria> implements SelectableDataModel<Categoria>, Serializable {

    public CategoriaDataModel() {
        //nothing
    }

    public CategoriaDataModel(List<Categoria> data) {
        super(data);
    }

    @Override
    public Categoria getRowData(String rowKey) {
        List<Categoria> listOfCategoria = (List<Categoria>) getWrappedData();

        for (Categoria obj : listOfCategoria) {
            if (obj.getIdCategoria().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Categoria classname) {
        return classname.getIdCategoria();
    }
}