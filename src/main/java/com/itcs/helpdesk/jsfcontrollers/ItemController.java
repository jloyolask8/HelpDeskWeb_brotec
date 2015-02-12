package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Item;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "itemController")
@SessionScoped
public class ItemController extends AbstractManagedBean<Item> implements Serializable {

    private int selectedItemIndex;
    private transient TreeNode itemsTree;
    private transient TreeNode selectedNode;
//    private transient TreeNode[] selectedNodeItems;
    private transient TreeNode itemNode = null;
    private Item portapapeles = null;
    private int ordenMayor = 0;
    private String textoFiltro;
    private List<Item> emptyList;
    private Area selectedArea;
//    private Area idArea;

    public ItemController() {
        super(Item.class);
        emptyList = new LinkedList<>();
    }

    public String getTextoFiltro() {
        return textoFiltro;
    }

    public void setTextoFiltro(String textoFiltro) {
        this.textoFiltro = textoFiltro;
    }

    @Override
    public Item getSelected() {
        if (current == null) {
            current = new Item();
            selectedItemIndex = -1;
        }
        return current;
    }

//    @Override
//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(getPaginationPageSize()) {
//                @Override
//                public int getItemsCount() {
//                    return getJpaController().count(Item.class).intValue();
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    return new ListDataModel(getJpaController().queryByRange(Item.class, getPageSize(), getPageFirstItem()));
//                }
//            };
//        }
//        return pagination;
//    }

    @Override
    public String prepareList() {
        textoFiltro = null;
        itemsTree = null;
        setItem(null);
        recreateModel();
        return "/script/item/AdminItems";
    }

    public String prepareView() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Es requerido que seleccione una fila para visualizar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/item/AdminItems";
    }

    public String prepareCreate() {
        current = new Item();
        selectedItemIndex = -1;
        return "/script/item/AdminItems";
    }

    public String create() {
        try {
            Item selected = null;
            if (itemNode != null) {
                selected = (Item) itemNode.getData();
                current.setIdItemPadre(selected);
            }
            current.setOrden(ordenMayor + 1);
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ItemCreated"));
            itemsTree = null;
            setItem(null);
            return "/script/item/AdminItems";
        } catch (Exception e) {
            e.printStackTrace();
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(Integer idItem) {
        current = getJpaController().find(Item.class, idItem);
        return "/script/item/AdminItems";
    }

    public String prepareEdit() {
        if (getSelectedItems().size() != 1) {
            JsfUtil.addSuccessMessage("Se requiere que seleccione una fila para editar.");
            return "";
        } else {
            current = getSelectedItems().get(0);
        }
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/item/AdminItems";
    }

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ItemUpdated"));
            itemsTree = null;
            return "/script/item/AdminItems";
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
        return "/script/item/AdminItems";
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
        return "/script/item/AdminItems";
    }

//    public String destroyAndView() {
//        performDestroy();
//        recreateModel();
//        updateCurrentItem();
//        if (selectedItemIndex >= 0) {
//            return "/script/item/AdminItems";
//        } else {
//            // all items were removed - go back to list
//            recreateModel();
//            return "/script/item/AdminItems";
//        }
//    }

    private void performDestroy() {
        try {
            getJpaController().remove(Item.class, current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ItemDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    private void updateCurrentItem() {
//        long count = getJpaController().count(Item.class);
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = (int) count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = (Item) getJpaController().queryByRange(Item.class, 1, selectedItemIndex).get(0);
//        }
//    }
//
//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//        Iterator iter = items.iterator();
//        List<Item> listOfItem = new ArrayList<Item>();
//        while (iter.hasNext()) {
//            listOfItem.add((Item) iter.next());
//        }
//
//        return new ItemDataModel(listOfItem);
//    }

    public List<Item> getItemsForUser(Usuario usuario, String textFiltro) {

        List<Item> lista = (List<Item>)getJpaController().findAll(Item.class);

        if ((textFiltro != null) && (!textFiltro.trim().isEmpty())) {
            List<Item> filteredList = new LinkedList<Item>();
            for (Item localCat : lista) {
                if (localCat.getNombre().toLowerCase().contains(textFiltro)) {
                    localCat.setIdItemPadre(null);
                    localCat.setItemList(emptyList);
                    filteredList.add(localCat);
                }
            }
            lista = filteredList;
        }
        Collections.sort(lista, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        return lista;
    }

    private void removeNotListedNodes(List<Item> listed, Item root) {
        if (root.getItemList() != null && !root.getItemList().isEmpty()) {
            int index = 0;
            while ((!root.getItemList().isEmpty()) && (root.getItemList().size() > index)) {
                if (!listed.contains(root.getItemList().get(index))) {
                    root.getItemList().remove(index);
                } else {
                    index++;
                }
            }
            if (!root.getItemList().isEmpty()) {
                for (Item item : root.getItemList()) {
                    removeNotListedNodes(listed, item);
                }
            }
        }
    }

    public DataModel getItemsNoPagination() {
        List<Item> lista;
        if ((textoFiltro != null) && (!textoFiltro.trim().isEmpty()) && textoFiltro.trim().length() > 3) {
            List<Item> listaTmp = getJpaController().getItemFindByNombreLike(textoFiltro);
            List<Item> listaNodos = new LinkedList<Item>();
            List<Item> roots = new LinkedList<Item>();
            for (Item item : listaTmp) { //se guardan todos los nodos encontrados con sus antepasados
                Item itemActual = item;
                while (itemActual != null) {
                    if (!listaNodos.contains(itemActual)) {
                        listaNodos.add(itemActual);
                    }
                    if (itemActual.getIdItemPadre() == null) { //Es root
                        if (!roots.contains(itemActual)) {
                            roots.add(itemActual);
                        }
                    }
                    itemActual = itemActual.getIdItemPadre();
                }
            }
            lista = new LinkedList<>();

            for (Item root : roots) {
                removeNotListedNodes(listaNodos, root);
                lista.add(root);
            }

        } else {
            lista = (List<Item>)getJpaController().findAll(Item.class);
        }

        items = new ListDataModel(lista);

        Collections.sort(lista, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });

        return new ItemDataModel(lista);
    }

    public boolean puedeCortar() {
        if (itemNode == null) {
            return false;
        }
        return ((Item) itemNode.getData()).isEditable();
        //return !((Item) categoria.getData()).equals(EnumItems.TODOS.getItem());
    }

    public boolean puedePegar() {
        if (portapapeles == null || itemNode == null) {
            return false;
        }
        Item selected = (Item) itemNode.getData();

        if (selected == null) {
            return false;
        }

        if (selected.equals(portapapeles)) {
            return false;
        }
        if (selected.equals(portapapeles.getIdItemPadre())) {
            return false;
        }
        if (esDescendienteDeB(selected, portapapeles)) {
            return false;
        }

        return true;
    }

    public boolean esDescendienteDeB(Item catA, Item catB) {
        if (catA.getIdItemPadre() == null) {
            return false;
        }
        if (catA.getIdItemPadre().equals(catB)) {
            return true;
        } else {
            return esDescendienteDeB(catA.getIdItemPadre(), catB);
        }
    }

    private boolean itemsAreEquals(Item itemA, Item itemB) {
        boolean equals = false;
        if ((itemA.getIdItemPadre() != null) && (itemB.getIdItemPadre() != null)) {
            equals = itemsAreEquals(itemA.getIdItemPadre(), itemB.getIdItemPadre());
        } else if ((itemA.getIdItemPadre() == null) && (itemB.getIdItemPadre() == null)) {
            equals = true;
        }
        return equals && (itemA.getNombre().toLowerCase().equals(itemB.getNombre().toLowerCase()));
    }

    public void handleFileUpload(FileUploadEvent event) {
        InputStream is = null;
        String str = "";
        LinkedHashMap<String, Item> mapaItems = new LinkedHashMap<>();
        try {
            is = event.getFile().getInputstream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                    String valores[] = str.split(";");
                    if (valores.length == 2) {
                        Item itemPadre = null;
                        //es un item hijo
                        if (valores[0].indexOf('.') > 0) {
                            String idPadre = valores[0].substring(0, valores[0].lastIndexOf('.'));
                            //buscar en el map item padre
                            itemPadre = mapaItems.get(idPadre);
                        }
                        Item nuevoItem = new Item();
                        nuevoItem.setEditable(true);
                        nuevoItem.setIdArea(selectedArea);
                        nuevoItem.setIdItemPadre(itemPadre);
                        nuevoItem.setNombre(valores[1]);
                        nuevoItem.setOrden(ordenMayor + 1);
                        getJpaController().persist(nuevoItem);
                        mapaItems.put(valores[0], nuevoItem);
                    }
                }
            }

            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is loaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception ex) {
//            Logger.getLogger(NodoCalculoController.class.getName()).log(Level.ERROR, null, ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ItemController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void cortarItem(ActionEvent actionEvent) {
        portapapeles = (Item) itemNode.getData();
        JsfUtil.addSuccessMessage("Item " + portapapeles.getNombre() + "\nalmacenado en el portapapeles");
    }

    public void crearItem(ActionEvent actionEvent) {
        try {
            Item itemPadre = null;
            if (itemNode != null && itemNode.getData() != null) {
                itemPadre = (Item) itemNode.getData();
                current.setIdArea(itemPadre.getIdArea());
            }
            current.setIdItemPadre(itemPadre);
            current.setOrden(ordenMayor + 1);
            current.setEditable(true);
            getJpaController().persist(current);
            JsfUtil.addSuccessMessage("Item creado con exito");
            itemsTree = null;
            setItem(null);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e);
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void eliminaItem(ActionEvent actionEvent) {
        current = (Item) itemNode.getData();
        deleteItemAndSubItems(current);
        JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ItemDeleted"));
//        performDestroy();
        current = null;
        itemsTree = null;
        portapapeles = null;
        setItem(null);
    }

    private void deleteItemAndSubItems(Item item) {
        if (item.getItemList() != null) {
            while (!item.getItemList().isEmpty()) {
                deleteItemAndSubItems(item.getItemList().get(0));
                item.getItemList().remove(0);
            }
        }
        try {
            getJpaController().remove(Item.class, item);
        } catch (Exception ex) {
            Logger.getLogger(ItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pegarItem(ActionEvent actionEvent) {
        Item selected = (Item) itemNode.getData();
        portapapeles.setIdItemPadre(selected);
        try {
            getJpaController().merge(portapapeles);
            itemsTree = null;
            portapapeles = null;
            setItem(null);

        } catch (Exception ex) {
            Log.createLogger(ItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean puedeRenombrar() {
        if (itemNode == null) {
            return false;
        }
        Item selected = (Item) itemNode.getData();
        if (!selected.isEditable()) {
            return false;
        }
        return true;
    }

    public boolean puedeBorrarItem() {
        if (itemNode == null) {
            return false;
        }
        Item selected = (Item) itemNode.getData();
        if (!selected.isEditable()) {
            return false;
        }
        return selected.getCasoList().size() <= 0;
    }

//    public boolean puedeCrearItem() {
//        if (categoria == null) {
//            return false;
//        }
//        Item selected = ((Item) categoria.getData());
//        if (selected == null) {
//            return false;
//        }
//        return true;
//    }
    public boolean puedeBajarItem() {
        if (itemNode == null) {
            return false;
        }
        Item selected = ((Item) itemNode.getData());
        if (!selected.isEditable()) {
            return false;
        }
        if (selected.getIdItemPadre() == null) {
            return false;
        }
        if (selected.getIdItemPadre().getItemList().size() <= 1) {
            return false;
        }
        List<Item> cats = (List) selected.getIdItemPadre().getItemList();
        Collections.sort(cats, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        if (selected.equals(cats.get(cats.size() - 1))) {
            return false;
        }
        return true;
    }

    public boolean puedeSubirItem() {
        if (itemNode == null) {
            return false;
        }
        Item selected = ((Item) itemNode.getData());
        if (!selected.isEditable()) {
            return false;
        }
        if (selected.getIdItemPadre() == null) {
            return false;
        }
        if (selected.getIdItemPadre().getItemList().size() <= 1) {
            return false;
        }
        List<Item> cats = (List) selected.getIdItemPadre().getItemList();
        Collections.sort(cats, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        if (selected.equals(cats.get(0))) {
            return false;
        }
        return true;
    }

    public void bajarItem(ActionEvent actionEvent) throws Exception {
        Item selected = (Item) itemNode.getData();
        List<Item> cats = (List) selected.getIdItemPadre().getItemList();
        Collections.sort(cats, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        for (int i = 0; i < cats.size(); i++) {
            if (cats.get(i).equals(selected)) {
                Item catAbajo = cats.get(i + 1);
                int ordenA = selected.getOrden();
                int ordenB = catAbajo.getOrden();
                selected.setOrden(ordenB);
                catAbajo.setOrden(ordenA);
                getJpaController().merge(selected);
                getJpaController().merge(catAbajo);
                itemsTree = null;
                setItem(null);
                break;
            }
        }

    }

    public void prepareCreateAjax(ActionEvent actionEvent) {
        current = new Item();
        Item itemPadre = null;
        if (itemNode != null && itemNode.getData() != null) {
            itemPadre = (Item) itemNode.getData();
            current.setIdArea(itemPadre.getIdArea());
            current.setIdItemPadre(itemPadre);
        }
        selectedItemIndex = -1;
    }

    public void prepareRenombrarAjax(ActionEvent actionEvent) {
        Item selected = (Item) itemNode.getData();
        current = new Item();
        current.setNombre(selected.getNombre());
    }

    public void renombrar(ActionEvent actionEvent) {
        try {
            Item selected = (Item) itemNode.getData();
            selected.setNombre(current.getNombre());
            getJpaController().merge(selected);
            JsfUtil.addSuccessMessage("Item renombrado con exito");
            itemsTree = null;
            setItem(null);
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void subirItem(ActionEvent actionEvent) throws Exception {
        Item selected = (Item) itemNode.getData();
        List<Item> cats = (List) selected.getIdItemPadre().getItemList();
        Collections.sort(cats, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getOrden() - o2.getOrden();
            }
        });
        for (int i = 0; i < cats.size(); i++) {
            if (cats.get(i).equals(selected)) {
                Item catArriba = cats.get(i - 1);
                int ordenA = selected.getOrden();
                int ordenB = catArriba.getOrden();
                selected.setOrden(ordenB);
                catArriba.setOrden(ordenA);
                getJpaController().merge(selected);
                getJpaController().merge(catArriba);
                itemsTree = null;
                setItem(null);
                break;
            }
        }
    }

    public String getNombreItemSeleccionada() {
        if (itemNode == null) {
            return "";
        }
        Item selected = (Item) itemNode.getData();
        return selected.getNombre();
    }

    public TreeNode getItem() {
        return itemNode;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        filtrarItems();
    }

    public void setItem(TreeNode itemTreeNode) {
        ////System.out.println(getNombreItemSeleccionada());
        this.itemNode = itemTreeNode;
    }

    public void filtrarItems() {
        itemsTree = null;

    }

    public TreeNode getItems(Usuario user) {
        if (itemsTree != null) {
            return itemsTree;
        }
        Usuario usuario = getJpaController().find(Usuario.class, user.getIdUsuario());
        List<Item> localCats = getItemsForUser(usuario, this.textoFiltro);

        if (localCats != null && !localCats.isEmpty()) {
            DataModel localDataModel = new ItemDataModel(localCats);
            ////System.out.println("se reconstruye el arbol");
            Iterator it = localDataModel.iterator();
            itemsTree = new DefaultTreeNode("Item", null);
            itemsTree.setExpanded(true);
            while (it.hasNext()) {
                Item cat = (Item) it.next();
                verificaSiEsOrdenMayor(cat.getOrden());
                if ((cat.getIdItemPadre() == null) || (!localCats.contains(cat.getIdItemPadre()))) {
                    if (cat.getItemList() != null && cat.getItemList().isEmpty()) {
                        TreeNode subItems = new DefaultTreeNode(cat, itemsTree);
                        subItems.setExpanded(true);
                    } else {
                        TreeNode subItems = new DefaultTreeNode(cat, itemsTree);
                        subItems.setExpanded(true);
                        setItems(cat, subItems);
                    }
                }
            }
        } else {
            itemsTree = new DefaultTreeNode("Item", null);
        }

        return itemsTree;
    }

    /**
     * Lista de categorias para Tree PrimeFace
     *
     * @return
     */
    public TreeNode getTree() {
        try {
            if (itemsTree != null) {
                return itemsTree;
            }

            items = getItemsNoPagination();

            if (items != null) {
                ////System.out.println("se reconstruye el arbol");
                Iterator it = items.iterator();
                itemsTree = new DefaultTreeNode("Item", null);
                itemsTree.setExpanded(true);
                while (it.hasNext()) {
                    Item cat = (Item) it.next();
                    verificaSiEsOrdenMayor(cat.getOrden());
                    if (cat.getIdItemPadre() == null) {
                        if (cat.getItemList() != null && cat.getItemList().isEmpty()) {
                            TreeNode subItems = new DefaultTreeNode(cat, itemsTree);
                            subItems.setExpanded(true);
                        } else {
                            TreeNode subItems = new DefaultTreeNode(cat, itemsTree);
                            subItems.setExpanded(true);
                            setItems(cat, subItems);
                        }
                    }
                }
            } else {
                itemsTree = new DefaultTreeNode("Item", null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemsTree;
    }

    private void verificaSiEsOrdenMayor(int orden) {
        if (orden > ordenMayor) {
            ordenMayor = orden;
        }
    }

    /**
     * Crea el arbol de categoria desde un Padre bastardo.
     *
     * @param item Item Padre
     * @param subItems TreeNode Padre
     * @return
     */
    private void setItems(Item item, TreeNode subItems) {

        List<Item> cats = (List) item.getItemList();
        if (cats != null) {
            Collections.sort(cats, new Comparator<Item>() {
                @Override
                public int compare(Item o1, Item o2) {
                    return o1.getOrden() - o2.getOrden();
                }
            });
            for (Item cat : cats) {
                verificaSiEsOrdenMayor(cat.getOrden());
                if (cat.getItemList() != null && cat.getItemList().isEmpty()) {
                    TreeNode cate = new DefaultTreeNode(cat, subItems);
                    cate.setExpanded(true);
                } else {
                    TreeNode cate = new DefaultTreeNode(cat, subItems);
                    cate.setExpanded(true);
                    setItems(cat, cate);
                }
            }
        }
    }

    public void setItems(TreeNode items) {
        this.itemsTree = items;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the selectedArea
     */
    public Area getSelectedArea() {
        return selectedArea;
    }

    /**
     * @param selectedArea the selectedArea to set
     */
    public void setSelectedArea(Area selectedArea) {
        this.selectedArea = selectedArea;
    }

  

    @FacesConverter(value = "ItemConverter", forClass = Item.class)
    public static class ItemControllerConverter implements Converter, Serializable {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            ////System.out.println("ItemControllerConverter.getAsObject()");
            if (value == null || value.length() == 0) {
                return null;
            }
           UserSessionBean controller = (UserSessionBean) facesContext.getApplication().getELResolver().
                getValue(facesContext.getELContext(), null, "UserSessionBean");
        return controller.getJpaController().find(Item.class, getKey(value));
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
//             //System.out.println("ItemControllerConverter.getAsString()");
            if (object == null) {
                return null;
            }
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getIdItem());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ItemController.class.getName());
            }
        }
    }
}

class ItemDataModel extends ListDataModel<Item> implements SelectableDataModel<Item>, Serializable {

    public ItemDataModel() {
        //nothing
    }

    public ItemDataModel(List<Item> data) {
        super(data);
    }

    @Override
    public Item getRowData(String rowKey) {
        List<Item> listOfItem = (List<Item>) getWrappedData();

        for (Item obj : listOfItem) {
            if (obj.getIdItem().toString().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Item classname) {
        return classname.getIdItem();
    }
}
