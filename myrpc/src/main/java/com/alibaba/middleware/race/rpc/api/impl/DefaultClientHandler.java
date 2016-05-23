package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.async.ResponseCallbackListener;
import com.alibaba.middleware.race.rpc.async.ResponseFuture;
import com.alibaba.middleware.race.rpc.context.RpcContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by marsares on 15/7/25.
 */
public class DefaultClientHandler extends ChannelInboundHandlerAdapter {
    private volatile Channel channel;
    private ConcurrentHashMap<Long,RpcFuture>pendingRpc=new ConcurrentHashMap<Long,RpcFuture>();
    private ConcurrentHashMap<String,Integer>asynRpc=new ConcurrentHashMap<String,Integer>();
    private AtomicLong seqNumGenerator=new AtomicLong();
    private RpcFuture AsynRpcFuture;
    private ResponseCallbackListener listener;

    public ResponseCallbackListener getListener() {
        return listener;
    }

    public void setListener(ResponseCallbackListener listener) {
        this.listener = listener;
    }

    public long getNextSequenceNumber(){
        return seqNumGenerator.getAndAdd(1);
    }

    public void setChannel(Channel channel){
        this.channel=channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
        if(msg instanceof RpcContext){
            RpcContext rpcCtx=(RpcContext)msg;
            if(rpcCtx.getResponse().getCallType()==0){
                RpcFuture rpcFuture=pendingRpc.get(rpcCtx.getResponse().getSeqNum());
                if(rpcFuture!=null){
                    pendingRpc.remove(rpcCtx.getResponse().getSeqNum());
                    rpcFuture.done(rpcCtx.getResponse());
                }
            }
            else if(rpcCtx.getResponse().getCallType()==1){
                AsynRpcFuture.done(rpcCtx.getResponse());
            }else{
                listener.onResponse(rpcCtx.getResponse().getAppResponse());
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx)throws Exception{
        System.out.println("registered");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel is active");
        super.channelActive(ctx);
        /*System.out.println("invoking " + "getString");
        Object[]args={};
        RpcContext rpcCtx = createRequest(args, "com.alibaba.middleware.race.rpc.demo.service.RaceTestServiceImpl", "getString");
        //RpcFuture rpcFuture=doRpc(rpcCtx);
        System.out.println("waiting for future");
        channel.writeAndFlush(rpcCtx);*/
        //System.out.println("final result="+rpcFuture.get());
    }

    public RpcFuture doRpc(RpcContext rpcCtx)throws Exception{
        if(!asynRpc.containsKey(rpcCtx.getRequest().getFuncName())){
            RpcFuture rpcFuture=new RpcFuture(rpcCtx);
            pendingRpc.put(rpcCtx.getRequest().getSeqNum(), rpcFuture);
            channel.writeAndFlush(rpcCtx);
            return rpcFuture;
        }else{
            return null;
        }
    }

    public void setProp(RpcContext rpcContext){
        channel.writeAndFlush(rpcContext);
    }

    public void doAsynRpc(RpcContext rpcCtx){
        RpcFuture rpcFuture=new RpcFuture(rpcCtx);
        asynRpc.put(rpcCtx.getRequest().getFuncName(), 1);
        AsynRpcFuture=rpcFuture;
        ResponseFuture.futureThreadLocal.set(rpcFuture);
        channel.writeAndFlush(rpcCtx);
    }

    public void doAsynRpcWithListener(RpcContext rpcCtx){
        asynRpc.put(rpcCtx.getRequest().getFuncName(), 1);
        channel.writeAndFlush(rpcCtx);
    }

    public void cancelAsyn(String methodName){
        if(asynRpc.contains(methodName))asynRpc.remove(methodName);
    }
}
