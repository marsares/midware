package com.alibaba.middleware.race.mom.broker;

import java.util.HashMap;
import java.util.List;

/**
 * Created by marsares on 15/8/11.
 */
public class Group {
    public HashMap<String, FilterList> getFilterLists() {
        return filterLists;
    }

    public void setFilterLists(HashMap<String, FilterList> filterLists) {
        this.filterLists = filterLists;
    }

    HashMap<String,FilterList>filterLists=new HashMap<String,FilterList>();
}
