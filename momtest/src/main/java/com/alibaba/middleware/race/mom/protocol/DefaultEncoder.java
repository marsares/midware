package com.alibaba.middleware.race.mom.protocol;

import com.alibaba.middleware.race.mom.context.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by marsares on 15/8/9.
 */
public class DefaultEncoder extends ChannelOutboundHandlerAdapter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof Context){
            Context context=(Context)msg;
            Object objToEncode;
            if(context.getConsumeRegist()!=null){
                objToEncode=context.getConsumeRegist();
                byte[]bytes;
                int type=1;
                bytes=objectMapper.writeValueAsBytes(objToEncode);
                ByteBuf byteBuf=ctx.alloc().buffer(Constants.headerLen1+bytes.length);
                byteBuf.writeInt(type);
                byteBuf.writeInt(bytes.length);
                byteBuf.writeBytes(bytes);
                ctx.write(byteBuf,promise);
            }
            if(context.getMessage()!=null){
                objToEncode=context.getMessage();
                byte[]bytes;
                int type=2;
                bytes=objectMapper.writeValueAsBytes(objToEncode);
                ByteBuf byteBuf=ctx.alloc().buffer(Constants.headerLen1+bytes.length);
                byteBuf.writeInt(type);
                byteBuf.writeInt(bytes.length);
                byteBuf.writeBytes(bytes);
                ctx.write(byteBuf,promise);
            }
            if(context.getSendAck()!=null){
                objToEncode=context.getSendAck();
                byte[]bytes;
                int type=3;
                bytes=objectMapper.writeValueAsBytes(objToEncode);
                ByteBuf byteBuf=ctx.alloc().buffer(Constants.headerLen1+bytes.length);
                byteBuf.writeInt(type);
                byteBuf.writeInt(bytes.length);
                byteBuf.writeBytes(bytes);
                ctx.write(byteBuf,promise);
            }
        }
    }
}
