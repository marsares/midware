package com.alibaba.middleware.race.rpc.context;


import com.alibaba.middleware.race.rpc.api.impl.DefaultClientHandler;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangsheng.hs on 2015/4/8.
 */
public class RpcContext {
    private RpcRequest request;
    private RpcResponse response;
    public static DefaultClientHandler handler;

    public static DefaultClientHandler getHandler() {
        return handler;
    }

    public static void setHandler(DefaultClientHandler clientHandler) {
        handler=clientHandler;
    }

    public RpcResponse getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
    }

    public RpcRequest getRequest() {
        return request;
    }

    public void setRequest(RpcRequest request) {
        this.request = request;
    }

    //TODO how can I get props as a provider? tip:ThreadLocal
    /*public static Map<String,Object> props = new HashMap<String, Object>();

    public static void addProp(String key ,Object value){
        props.put(key,value);
    }

    public static Object getProp(String key){
        return props.get(key);
    }

    public static Map<String,Object> getProps(){
       return Collections.unmodifiableMap(props);
    }*/
    public static ThreadLocal<HashMap<String,Object>> props = new ThreadLocal<HashMap<String, Object>>();

    public static void addProp(String key,Object value){
        Object[]args={key,value};
        RpcContext rc=new RpcContext();
        RpcContext rpcContext=rc.createRequest(args,rc.getClass().getName(),"addPropLocal",0,0);
        getHandler().setProp(rpcContext);
    }

    public static void addPropLocal(String key ,Object value){
        HashMap<String,Object>hm=props.get();
        if(hm==null)hm=new HashMap<String,Object>();
        hm.put(key,value);
        props.set(hm);
    }

    public static Object getProp(String key){
        HashMap<String,Object>hm=props.get();
        if(hm==null)return null;
        Object result=hm.get(key);
        props.set(hm);
        return result;
    }

    public static Map<String,Object> getProps(){
        HashMap<String,Object>hm=props.get();
        if(hm==null){
            return null;
        }
        props.set(hm);
        return Collections.unmodifiableMap(hm);
    }

    public RpcContext createRequest(Object[] args, String ObjName, String FuncName,int callType,int FuncType) {
        RpcContext ctx = new RpcContext();
        RpcRequest request = new RpcRequest();
        request.setSeqNum(getHandler().getNextSequenceNumber());
        request.setArgs(args);
        request.setFuncName(FuncName);
        request.setObjName(ObjName);
        request.setCallType(callType);
        request.setFuncType(FuncType);
        ctx.setRequest(request);
        return ctx;
    }
}
