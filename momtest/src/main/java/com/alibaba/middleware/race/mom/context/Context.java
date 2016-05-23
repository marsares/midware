package com.alibaba.middleware.race.mom.context;

import com.alibaba.middleware.race.mom.model.*;

/**
 * Created by marsares on 15/8/10.
 */
public class Context {
    private ConsumeResult consumeResult;
    private ConsumeStatus consumeStatus;
    private Message message;
    private SendResult sendResult;
    private SendStatus sendStatus;
    private ConsumeRegist consumeRegist;
    private SendAck sendAck;

    public SendAck getSendAck() {
        return sendAck;
    }

    public void setSendAck(SendAck sendAck) {
        this.sendAck = sendAck;
    }

    public ConsumeRegist getConsumeRegist() {
        return consumeRegist;
    }

    public void setConsumeRegist(ConsumeRegist consumeRegist) {
        this.consumeRegist = consumeRegist;
    }

    public SendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(SendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    public SendResult getSendResult() {

        return sendResult;
    }

    public void setSendResult(SendResult sendResult) {
        this.sendResult = sendResult;
    }

    public Message getMessage() {

        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public ConsumeStatus getConsumeStatus() {

        return consumeStatus;
    }

    public void setConsumeStatus(ConsumeStatus consumeStatus) {
        this.consumeStatus = consumeStatus;
    }

    public ConsumeResult getConsumeResult() {

        return consumeResult;
    }

    public void setConsumeResult(ConsumeResult consumeResult) {
        this.consumeResult = consumeResult;
    }
}
