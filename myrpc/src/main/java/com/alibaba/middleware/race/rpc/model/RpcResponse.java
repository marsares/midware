package com.alibaba.middleware.race.rpc.model;

import java.io.Serializable;

/**
 * Created by huangsheng.hs on 2015/3/27.
 */
public class RpcResponse implements Serializable {
    //static private final long serialVersionUID = -4364536436151723421L;
    private long seqNum;
    //private String errorMsg;
    private Object appResponse;

    private Object exception;

    private String funcName;

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    private int callType;

    public Object getException() {
        return exception;
    }

    public void setException(Object exception) {
        this.exception = exception;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(long seqNum) {
        this.seqNum = seqNum;
    }

    public Object getAppResponse() {
        return appResponse;
    }

    /*public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isError() {
        return errorMsg == null ? false : true;
    }*/

    public void setAppResponse(Object appResponse) {
        this.appResponse = appResponse;
    }
}
