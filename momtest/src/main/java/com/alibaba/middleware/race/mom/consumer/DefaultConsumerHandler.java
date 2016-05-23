package com.alibaba.middleware.race.mom.consumer;

import com.alibaba.middleware.race.mom.async.MessageListener;
import com.alibaba.middleware.race.mom.context.Context;
import com.alibaba.middleware.race.mom.model.Message;
import com.alibaba.middleware.race.mom.model.SendAck;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Random;

/**
 * Created by marsares on 15/8/9.
 */
public class DefaultConsumerHandler extends ChannelInboundHandlerAdapter {
    private volatile Channel channel;
    private MessageListener listener;
    private int id;
    private static Random random=new Random();

    public DefaultConsumerHandler(){
        id=random.nextInt(100);
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Context){
            Context context=(Context)msg;
            Message message=context.getMessage();
            if(message!=null){
                System.out.println("consumer id="+id);
                listener.onMessage(message);
                processMessage(ctx,context);
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

    public void regist(Context context){
        System.out.println("registing");
        channel.writeAndFlush(context);
    }

    public void processMessage(ChannelHandlerContext ctx, Context context) {
        SendAck sendAck = new SendAck();
        sendAck.setAckId(context.getMessage().getMsgId());
        sendAck.setAck("success");
        Context backContext = new Context();
        backContext.setSendAck(sendAck);
        ctx.writeAndFlush(backContext);
    }

}
