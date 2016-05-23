package com.alibaba.middleware.race.mom.protocol;

import com.alibaba.middleware.race.mom.context.Context;
import com.alibaba.middleware.race.mom.model.ConsumeRegist;
import com.alibaba.middleware.race.mom.model.Message;
import com.alibaba.middleware.race.mom.model.SendAck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by marsares on 15/8/9.
 */
public class DefaultDecoder extends ByteToMessageDecoder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int packetLength=0;
        int bodyLength=0;
        int readerIndex=in.readerIndex();
        int headerLength=Constants.headerLen1;
        int type=0;
        if(in.readableBytes()<headerLength){
            return;
        }
        else if(in.readableBytes()>=headerLength){
            type=in.getInt(readerIndex);
            bodyLength=in.getInt(readerIndex+4);
            packetLength=headerLength+bodyLength;
        }
        if(type==1){
            if(packetLength>headerLength){
                if(in.readableBytes()<packetLength){
                    return;
                }else{
                    byte[]body=new byte[bodyLength];
                    in.getBytes(readerIndex+headerLength,body);
                    Context context=new Context();
                    try{
                        ConsumeRegist consumeRegist=objectMapper.readValue(body,ConsumeRegist.class);
                        context.setConsumeRegist(consumeRegist);
                    }catch(Exception e){
                        ctx.close();
                    }
                    out.add(context);
                    in.skipBytes(packetLength);
                    if (in.readableBytes() > headerLength) {//decode next request package
                        decode(ctx, in, out);
                    }
                    return;
                }
            }
            if (bodyLength <= 0) { //Unrecoverable error, have to close connection.
                ctx.close();
            }
        }
        if(type==2){
            if(packetLength>headerLength){
                if(in.readableBytes()<packetLength){
                    return;
                }else{
                    byte[]body=new byte[bodyLength];
                    in.getBytes(readerIndex+headerLength,body);
                    Context context=new Context();
                    try{
                        Message message=objectMapper.readValue(body,Message.class);
                        context.setMessage(message);
                    }catch(Exception e){
                        System.out.println("exception happens");
                        ctx.close();
                    }
                    out.add(context);
                    in.skipBytes(packetLength);
                    if (in.readableBytes() > headerLength) {//decode next request package
                        decode(ctx, in, out);
                    }
                    return;
                }
            }
            if (bodyLength <= 0) { //Unrecoverable error, have to close connection.
                ctx.close();
            }
        }
        if(type==3){
            if(packetLength>headerLength){
                if(in.readableBytes()<packetLength){
                    return;
                }else{
                    byte[]body=new byte[bodyLength];
                    in.getBytes(readerIndex+headerLength,body);
                    Context context=new Context();
                    try{
                        SendAck sendAck=objectMapper.readValue(body,SendAck.class);
                        context.setSendAck(sendAck);
                    }catch(Exception e){
                        ctx.close();
                    }
                    out.add(context);
                    in.skipBytes(packetLength);
                    if (in.readableBytes() > headerLength) {//decode next request package
                        decode(ctx, in, out);
                    }
                    return;
                }
            }
            if (bodyLength <= 0) { //Unrecoverable error, have to close connection.
                ctx.close();
            }
        }
    }
}
