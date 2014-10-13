package com.itcs.helpdesk.jsfcontrollers.util;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.DataModel;

public abstract class PaginationHelper {

    public static final int PAGINATOR_DEFAULT_SIZE = 10;
    private int pageSize;
    private int page;
    private DataModel datamodel = null;

    public PaginationHelper() {
        this.pageSize = PAGINATOR_DEFAULT_SIZE;
    }

    public PaginationHelper(int pageSize) {
        this.pageSize = pageSize;
    }

    public abstract int getItemsCount();

    public abstract DataModel createPageDataModel();

    public List<Integer> getPageSizesAvailable() {
        List<Integer> list = new ArrayList<>();
//       list.add(1);//Just for testing pagination.
        list.add(10);
        list.add(20);
        list.add(30);
        list.add(40);
        list.add(50);
         list.add(100);
        return list;
    }

    public int getPageFirstItem() {
        return page * pageSize;
    }

    public int getPageLastItem() {
        int i = getPageFirstItem() + pageSize - 1;
        int count = getItemsCount() - 1;
        if (i > count) {
            i = count;
        }
        if (i < 0) {
            i = 0;
        }
        return i;
    }

    public boolean isHasNextPage() {
        return (page + 1) * pageSize + 1 <= getItemsCount();
    }

    public void nextPage() {
        if (isHasNextPage()) {
            page++;
        }
    }

    public void lastPage() {
        int itemCount = getItemsCount();
        while ((page + 1) * pageSize + 1 <= itemCount) {
            page++;
        }
    }

    public void firstPage() {
        page = 0;
    }

    public boolean isHasPreviousPage() {
        return page > 0;
    }

    public void previousPage() {
        if (isHasPreviousPage()) {
            page--;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int p) {
        this.pageSize = p;
    }

    /**
     * @return the datamodel
     */
    public DataModel getDatamodel() {
        return datamodel;
    }

    /**
     * @param datamodel the datamodel to set
     */
    public void setDatamodel(DataModel datamodel) {
        this.datamodel = datamodel;
    }
}
