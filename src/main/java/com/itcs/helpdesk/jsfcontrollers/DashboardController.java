package com.itcs.helpdesk.jsfcontrollers;

import com.itcs.helpdesk.persistence.entities.Caso;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean(name = "dashboardController")
@SessionScoped
public class DashboardController extends AbstractManagedBean<Caso>  implements Serializable {


    public DashboardController() {
        super(Caso.class);
    }

    @Override
    public Class getDataModelImplementationClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String getListPage() {
        return "/script/dashboard?faces-redirect=true";
    }
    
    

    
}

