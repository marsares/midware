package com.alibaba.middleware.race.mom.model;

import com.alibaba.middleware.race.mom.async.MessageListener;

/**
 * Created by marsares on 15/8/10.
 */
public class ConsumeRegist {
    private String groupId;
    private String topic;
    private String filter;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getTopic() {

        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupId() {

        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
