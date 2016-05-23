package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;
import io.netty.channel.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by marsares on 15/7/24.
 */
public class DefaultHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        if(msg instanceof RpcContext){
            RpcContext rpcContext=(RpcContext)msg;
            processRequest(ctx, rpcContext);
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx){
        System.out.println("connected");
    }

    public void processRequest(ChannelHandlerContext ctx, RpcContext rpcContext){
        RpcRequest req=rpcContext.getRequest();
        RpcResponse res=new RpcResponse();
        res.setSeqNum(req.getSeqNum());
        res.setCallType(req.getCallType());
        res.setFuncName(req.getFuncName());
        try{
            Object[] args = req.getArgs();
            Class[] argTypes = new Class[args.length];
            if(req.getFuncType()==0){
                argTypes[0]=String.class;
                argTypes[1]=Object.class;
                Object obj= RpcProviderImpl.getServiceInstance(req.getObjName());
                Class clazz= obj.getClass();
                Object[]args1=req.getArgs();
                Method func = clazz.getMethod(req.getFuncName(), argTypes);
                func.invoke(obj, req.getArgs());
                return;
            }
            for(int i=0;i<args.length;i++){
                argTypes[i]=args[i].getClass();
            }
            Object obj= RpcProviderImpl.getServiceInstance(req.getObjName());
            Class clazz= obj.getClass();
            Method func = clazz.getMethod(req.getFuncName(), argTypes);
            Object result= func.invoke(obj, req.getArgs());
            res.setAppResponse(result);
            rpcContext.setResponse(res);
            ctx.writeAndFlush(rpcContext);
        }catch(InvocationTargetException e){
            Throwable ex=e.getCause();
            res.setException(ex);
            rpcContext.setResponse(res);
            ctx.writeAndFlush(rpcContext);
        }catch(Exception e){

        }
    }
}
