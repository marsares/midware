package com.alibaba.middleware.race.rpc.model;

/**
 * Created by huangsheng.hs on 2015/5/7.
 */
public class RpcRequest {
    private long seqNum;
    private Object[]args;
    private String ObjName;
    private String FuncName;
    private int callType;
    private int FuncType;

    public int getFuncType() {
        return FuncType;
    }

    public void setFuncType(int funcType) {
        FuncType = funcType;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(long seqNum) {
        this.seqNum = seqNum;
    }

    public String getFuncName() {
        return FuncName;
    }

    public void setFuncName(String funcName) {
        FuncName = funcName;
    }

    public String getObjName() {

        return ObjName;
    }

    public void setObjName(String objName) {
        ObjName = objName;
    }

    public Object[] getArgs() {

        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
