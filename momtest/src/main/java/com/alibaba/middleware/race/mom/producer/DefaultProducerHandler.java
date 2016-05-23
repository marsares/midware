package com.alibaba.middleware.race.mom.producer;

import com.alibaba.middleware.race.mom.async.SendCallback;
import com.alibaba.middleware.race.mom.context.Context;
import com.alibaba.middleware.race.mom.model.SendAck;
import com.alibaba.middleware.race.mom.model.SendResult;
import com.alibaba.middleware.race.mom.model.SendStatus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by marsares on 15/8/9.
 */
public class DefaultProducerHandler extends ChannelInboundHandlerAdapter {
    private volatile Channel channel;
    private ConcurrentHashMap<String,SendFuture> pendingSend=new ConcurrentHashMap<String,SendFuture>();
    private ConcurrentHashMap<String,SendCallback> pendingAsyncSend=new ConcurrentHashMap<String, SendCallback>();

    public void setCallback(String MsgId,SendCallback callback) {
        pendingAsyncSend.put(MsgId,callback);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
        if(msg instanceof Context){
            Context context=(Context)msg;
            if(context.getSendAck()!=null){
                SendAck sendAck=context.getSendAck();
                String ackId=sendAck.getAckId();
                System.out.println(ackId);
                if(pendingSend.containsKey(ackId)){
                    SendFuture sendFuture=pendingSend.get(ackId);
                    pendingSend.remove(ackId);
                    sendFuture.done(sendAck);
                }
                if(pendingAsyncSend.containsKey(ackId)){
                    SendCallback sendCallback=pendingAsyncSend.get(ackId);
                    pendingAsyncSend.remove(ackId);
                    SendResult sendResult=new SendResult();
                    sendResult.setMsgId(ackId);
                    sendResult.setStatus(SendStatus.SUCCESS);
                    sendCallback.onResult(sendResult);
                }
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
    }

    public String send(Context context){
        SendFuture sendFuture=new SendFuture(context);
        pendingSend.put(context.getMessage().getMsgId(), sendFuture);
        channel.writeAndFlush(context);
        try{
            return (String)sendFuture.get(3000, TimeUnit.MILLISECONDS);
        }catch(Exception e){

        }
        return null;
    }

    public void sendAsync(Context context){
        channel.writeAndFlush(context);
    }
}
