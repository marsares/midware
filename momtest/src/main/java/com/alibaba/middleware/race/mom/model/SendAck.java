package com.alibaba.middleware.race.mom.model;

/**
 * Created by marsares on 15/8/10.
 */
public class SendAck {
    String ack;
    String ackId;

    public String getAckId() {
        return ackId;
    }

    public void setAckId(String ackId) {
        this.ackId = ackId;
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }
}
