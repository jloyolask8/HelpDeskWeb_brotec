package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.jsfcontrollers.util.JsfUtil;
import com.itcs.helpdesk.jsfcontrollers.util.PaginationHelper;
import com.itcs.helpdesk.jsfcontrollers.util.UserSessionBean;
import com.itcs.helpdesk.persistence.entities.Clipping;
import com.itcs.helpdesk.persistence.utils.OrderBy;
import com.itcs.helpdesk.util.ClippingsPlaceHolders;
import com.itcs.helpdesk.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
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
import javax.resource.NotSupportedException;

@ManagedBean(name = "clippingController")
@SessionScoped
public class ClippingController extends AbstractManagedBean<Clipping> implements Serializable {

//    private Clipping current;
//    private transient DataModel items = null;
//    private transient PaginationHelper pagination;
//    private JPAFilterHelper filterHelper;
//    private int selectedItemIndex;
    private Integer visibilityOption = 1;
    //!--Filter
//    private Vista vista;
    @ManagedProperty(value = "#{UserSessionBean}")
    protected UserSessionBean userSessionBean;
    
       /**
     * @param userSessionBean the userSessionBean to set
     */
    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }
    
    
     

    //--Filter
    public ClippingController() {
        super(Clipping.class);
    }

    public void saveListener() {
        //Well all happens behinde the scene in jsf dude! 
    }
    
    public void handleAnyChangeEvent(){
        
    }
 

    @Override
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(getPaginationPageSize()) {
                @Override
                public int getItemsCount() {
                    return getJpaController().count(Clipping.class).intValue();
                }

                @Override
                public DataModel createPageDataModel() {
                    if (getFilterHelper().getVista().getFiltrosVistaList() != null && !getFilterHelper().getVista().getFiltrosVistaList().isEmpty()) {
                        try {
                            return new ListDataModel(getJpaController().findEntities(Clipping.class, getFilterHelper().getVista(), getPageSize(), getPageFirstItem(), new OrderBy("idClipping", OrderBy.OrderType.DESC), userSessionBean.getCurrent()));
                        } catch (NotSupportedException ex) {
                            JsfUtil.addErrorMessage(ex, "Error on findClippingEntities");
                            Logger.getLogger(ClippingController.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            JsfUtil.addErrorMessage(ex, "Error on findClippingEntities");
                            Logger.getLogger(ClippingController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return new ListDataModel(getJpaController().queryByRange(Clipping.class, getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    //-- this will be util in primefaces 4.0
//////////    public MenuModel getClippingsMenuModel() {
//////////
//////////        if (clippingsMenuModel != null) {
//////////            return clippingsMenuModel;
//////////        }
//////////
//////////        clippingsMenuModel = new DefaultMenuModel();
//////////        //hay que traerse solo los roots.
//////////        List<Clipping> clippings = getJpaController().getClippingJpaController().selectRootClippings();
//////////
//////////        //Ordenar los clippings
//////////        Collections.sort(clippings, new Comparator<Clipping>() {
//////////            @Override
//////////            public int compare(Clipping o1, Clipping o2) {
//////////                return o1.getOrden() - o2.getOrden();
//////////            }
//////////        });
//////////
//////////        for (Clipping root : clippings) {
//////////            if (root.isFolder()) {
//////////                DefaultSubMenu rootFolder = new DefaultSubMenu(root.getNombre());
//////////                addChildClippingsToSubMenu(root, rootFolder);
//////////                clippingsMenuModel.addElement(rootFolder);
//////////            } else {
//////////                //is empty... so is a leaf, a fucking clipping as root.
//////////                DefaultMenuItem item = new DefaultMenuItem(root.getNombre());
//////////                item.setIcon("ui-icon-disk");
//////////                item.setCommand("#{menuBean.save}");
//////////                item.setUpdate("messages");
//////////                clippingsMenuModel.addElement(item);
//////////            }
//////////        }
//////////        return clippingsMenuModel;
//////////
//////////    }
//////////
//////////    private void addChildClippingsToSubMenu(Clipping c, DefaultSubMenu subMenu) {
//////////
//////////        Collections.sort(c.getClippingList(), new Comparator<Clipping>() {
//////////            @Override
//////////            public int compare(Clipping o1, Clipping o2) {
//////////                return o1.getOrden() - o2.getOrden();
//////////            }
//////////        });
//////////
//////////        Iterator iterator = c.getClippingList().iterator();
//////////        while (iterator.hasNext()) {
//////////            Clipping clip = (Clipping) iterator.next();
//////////            //Does it have more childs?
//////////            if (clip.isFolder()) {
//////////                DefaultSubMenu folder = new DefaultSubMenu(clip.getNombre());
//////////                addChildClippingsToSubMenu(clip, folder);
//////////            } else {
//////////                //is empty... so is a leaf, a fucking clipping.
//////////                DefaultMenuItem item = new DefaultMenuItem(clip.getNombre());
//////////                item.setIcon("ui-icon-disk");
//////////                item.setCommand("#{menuBean.save}");
//////////                item.setUpdate("messages");
//////////                subMenu.addElement(item);
//////////            }
//////////
//////////        }
//////////    }
    //--
    public String prepareList() {
        recreateModel();
        return "/script/clipping/List";
    }

    public void prepareCreate() {
        current = new Clipping();
//        selectedItemIndex = -1;
//        return "/script/clipping/Create";
    }

    public Integer determineVisibility(Clipping clipping) {
        if (clipping.getVisibleToAll()) {
            return Constants.VISIBILITY_ALL;
        } else {
            if (clipping.getIdArea() != null) {
                return Constants.VISIBILITY_AREA;
            } else if (clipping.getIdGrupo() != null) {
                return Constants.VISIBILITY_GRUPO;
            } else {
                return Constants.VISIBILITY_ALL;
            }
        }
    }

    public String create() {
        try {
            visibilityOption = determineVisibility(current);

            current.setIdUsuarioCreadaPor(userSessionBean.getCurrent());
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

            getJpaController().getClippingJpaController().create(current);
            addInfoMessage(ResourceBundle.getBundle("/Bundle").getString("ClippingCreated"));
            getPrimefacesRequestContext().execute("editCreateDialog.hide()");
            getPrimefacesRequestContext().update("form:panelG1");
            return prepareList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareView() {
        current = (Clipping) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/clipping/View";
    }

    public String prepareEdit() {
        current = (Clipping) getItems().getRowData();
        visibilityOption = determineVisibility(current);
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "/script/clipping/Edit";
    }

    public String update() {
        try {
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
                default:
                    break;//Error

            }
            getJpaController().getClippingJpaController().edit(current);
            addInfoMessage(ResourceBundle.getBundle("/Bundle").getString("ClippingUpdated"));
            getPrimefacesRequestContext().execute("editCreateDialog.hide()");
            getPrimefacesRequestContext().update("form:panelG1");
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Clipping) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "/script/clipping/List";
    }

    public void selectItem() {
        current = (Clipping) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
    }

    public String destroySelected() {
        performDestroy();
        recreatePagination();
        recreateModel();
        return "/script/clipping/List";
    }

    private void performDestroy() {
        try {
            getJpaController().getClippingJpaController().destroy(current.getIdClipping());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClippingDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

//    private void updateCurrentItem() {
//        int count = getJpaController().count(Clipping.class).intValue();
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = getJpaController().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
//        }
//    }
    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    public List<Object> getAvailablePlaceHolders() {
        final ArrayList<Object> arrayList = new ArrayList<Object>(ClippingsPlaceHolders.getAvailablePlaceHolders());
        arrayList.add(ClippingsPlaceHolders.SALUDO_CLIENTE);
        return arrayList;
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

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("getDataModelImplementationClass Not supported yet.");
    }

    @FacesConverter(forClass = Clipping.class)
    public static class ClippingControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClippingController controller = (ClippingController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clippingController");
            System.out.println("converting clipping id " + getKey(value).toString());
            final Clipping clip = controller.getJpaController().getClippingJpaController().findClipping(getKey(value));
            System.out.println("Found " + clip);
            return clip;
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
            if (object instanceof Clipping) {
                Clipping o = (Clipping) object;
                return getStringKey(o.getIdClipping());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Clipping.class.getName());
            }
        }
    }
}