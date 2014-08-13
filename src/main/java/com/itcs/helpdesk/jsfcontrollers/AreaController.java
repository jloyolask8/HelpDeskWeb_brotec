package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.persistence.entities.Area;
import com.itcs.helpdesk.persistence.entities.Categoria;
import com.itcs.helpdesk.persistence.entities.Grupo;
import com.itcs.helpdesk.persistence.entities.Usuario;
import com.itcs.helpdesk.quartz.DownloadEmailJob;
import com.itcs.helpdesk.quartz.HelpDeskScheluder;
import com.itcs.helpdesk.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

@ManagedBean(name = "areaController")
@SessionScoped
public class AreaController extends AbstractManagedBean<Area> implements Serializable {

    private long selectedItemIndex;
    private transient TreeNode root = null;
    private int editActiveIndex;
    private int editActiveIndex2;
    private boolean expanded = false;

    public AreaController() {
        super(Area.class);
    }

    public void onTabChange() {

    }

    public void handleUseJndiChange() {
    }

    public void handleSimpleChange() {
    }

    public void handleMailStoreProtocolChange() {
    }

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(Area.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().queryByRange(Area.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "/script/area/List";
    }

    public String prepareView(String idArea) {
        current = getJpaController().getAreaFindByIdArea(idArea);
        editActiveIndex = 0;
        return "/script/area/View";
    }

    public String prepareCreate() {
        current = new Area();
        selectedItemIndex = -1;
        return "/script/area/Create";
    }

    public String create() {
        try {
            current.setEditable(true);
            getJpaController().persistArea(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AreaCreated"));
            return prepareEdit(current.getIdArea());
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit(String idArea) {
        current = getJpaController().getAreaFindByIdArea(idArea);
        editActiveIndex = 0;
        return "/script/area/Edit";
    }

    public String update() {
        try {
            getJpaController().merge(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AreaUpdated"));

            try {
                //disable Job revisar correo!
                final String downloadEmailJobId = DownloadEmailJob.formatJobId(current.getIdArea());
                final JobKey jobKey = JobKey.jobKey(downloadEmailJobId, HelpDeskScheluder.GRUPO_CORREO);
                HelpDeskScheluder.unschedule(jobKey);
            } catch (SchedulerException ex) {
                JsfUtil.addWarningMessage("Error del sistema, No se pudo desabilitar La revision de correo del area " + current.getIdArea());
                Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "No se pudo inicializar La revision de correo del area " + current.getIdArea(), ex);
            }

            return prepareList();
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).log(Level.SEVERE, "update", e);
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        if (current == null) {
            return "";
        }
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "/script/area/List";
    }

    public void destroySelected(ActionEvent actionEvent) {
        destroySelected();
    }

    public String destroySelected() {
        performDestroy();
        recreateModel();
        return "/script/area/List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "/script/area/View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "/script/area/List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().removeArea(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("AreaDeleted"));
        } catch (Exception e) {
            Log.createLogger(this.getClass().getName()).logSevere(e.getMessage());
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        long count = getJpaController().count(Area.class);
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = (Area) getJpaController().queryByRange(Area.class, 1, (int) selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        Iterator iter = items.iterator();
        List<Area> listOfArea = new ArrayList<Area>();
        while (iter.hasNext()) {
            listOfArea.add((Area) iter.next());
        }

        return new AreaDataModel(listOfArea);
    }

    public void toggleAllTree() {
        setExpanded(!isExpanded());
        toggleChildNodes(root, isExpanded());
    }
    
    public void refreshTree() {
        this.root = null;
    }

    private void toggleChildNodes(TreeNode node, boolean expanded) {
        if (node.getChildCount() > 0) {
            node.setExpanded(expanded);
            for (TreeNode treeNode : node.getChildren()) {
                toggleChildNodes(treeNode, expanded);
            }
        }
    }

    /**
     * @return the root
     */
    public TreeNode getRoot() {

        if (root == null) {
            root = new DefaultTreeNode("Áreas", null);
//        TreeNode areasNode = new DefaultTreeNode("Areas", root); 

            List<Area> areas = getJpaController().getAreaFindAll();
            for (Area area : areas) {
                TreeNode areaNode = new DefaultTreeNode("areas", area, root);
//                TreeNode gruposNode = new DefaultTreeNode("Grupos", areaNode);
//                TreeNode catsNode = new DefaultTreeNode("Categorías", areaNode);

                for (Grupo grupo : area.getGrupoList()) {
                    TreeNode grupoNode = new DefaultTreeNode("grupos", grupo, areaNode);
//                 TreeNode agentsNode = new DefaultTreeNode("Agentes", grupoNode);
                    for (Usuario usuario : grupo.getUsuarioList()) {
                        TreeNode agentNode = new DefaultTreeNode("agentes", usuario, grupoNode);
                    }
                }

//                for (Categoria cat : area.getCategoriaList()) {
//                    TreeNode catNode = new DefaultTreeNode("categorias", cat, catsNode);
//                }
            }
            
             toggleChildNodes(root, isExpanded());
        }

        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    /**
     * @return the editActiveIndex
     */
    public int getEditActiveIndex() {
        return editActiveIndex;
    }

    /**
     * @param editActiveIndex the editActiveIndex to set
     */
    public void setEditActiveIndex(int editActiveIndex) {
        this.editActiveIndex = editActiveIndex;
    }

    /**
     * @return the editActiveIndex2
     */
    public int getEditActiveIndex2() {
        return editActiveIndex2;
    }

    /**
     * @param editActiveIndex2 the editActiveIndex2 to set
     */
    public void setEditActiveIndex2(int editActiveIndex2) {
        this.editActiveIndex2 = editActiveIndex2;
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * @param expanded the expanded to set
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @FacesConverter(forClass = Area.class)
    public static class AreaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
            return controller.getJpaController().getAreaFindByIdArea(getKey(value));
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getIdArea());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Area.class.getName());
            }
        }
    }
}

class AreaDataModel extends ListDataModel<Area> implements SelectableDataModel<Area> {

    public AreaDataModel() {
        //nothing
    }

    public AreaDataModel(List<Area> data) {
        super(data);
    }

    @Override
    public Area getRowData(String rowKey) {
        List<Area> listOfArea = (List<Area>) getWrappedData();

        for (Area obj : listOfArea) {
            if (obj.getIdArea().equals(rowKey)) {
                return obj;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Area classname) {
        return classname.getIdArea();
    }
}
