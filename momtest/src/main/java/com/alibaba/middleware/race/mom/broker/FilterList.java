package com.alibaba.middleware.race.mom.broker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marsares on 15/8/11.
 */
public class FilterList {
    boolean Pending=false;

    public int size(){
        return list.size();
    }

    public boolean isPending() {
        return Pending;
    }

    public void setPending(boolean pending) {
        Pending = pending;
    }

    List<DefaultBrokerHandler> list=new ArrayList<DefaultBrokerHandler>();
    int index=0;
    public void put(DefaultBrokerHandler defaultBrokerHandler){
        list.add(defaultBrokerHandler);
    }
    public DefaultBrokerHandler get(){
        DefaultBrokerHandler defaultBrokerHandler=list.get(index);
        index=(index+1)%list.size();
        if(index==0)setPending(true);
        return defaultBrokerHandler;
    }
}
