package com.alibaba.middleware.race.rpc.protocol;

import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by marsares on 15/7/26.
 */
public class DefaultEncoder extends ChannelOutboundHandlerAdapter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RpcContext) {
            RpcContext rpcContext = (RpcContext) msg;
            Object objToEncode;
            if (rpcContext.getResponse() != null) {
                objToEncode = rpcContext.getResponse();
                byte[]bytes1;
                byte[]bytes2;
                byte[]bytes3;
                int type;
                Class<?> classType;
                bytes1=objectMapper.writeValueAsBytes(objToEncode);
                if(rpcContext.getResponse().getException()!=null){
                    type=0;
                    classType=rpcContext.getResponse().getException().getClass();
                    bytes2=objectMapper.writeValueAsBytes(classType);
                    bytes3=objectMapper.writeValueAsBytes(rpcContext.getResponse().getException());
                }else{
                    type=1;
                    classType=rpcContext.getResponse().getAppResponse().getClass();
                    bytes2=objectMapper.writeValueAsBytes(classType);
                    bytes3=objectMapper.writeValueAsBytes(rpcContext.getResponse().getAppResponse());
                }

                /*Class<?>result=objectMapper.readValue(bytes2,Class.class);
                Object o=objectMapper.readValue(bytes1,result);
                if(o instanceof RaceDO){
                    RaceDO rd=(RaceDO)o;
                    System.out.println("racedo.str="+rd.getStr());
                }*/
                System.out.println(classType);
                Object result = objectMapper.readValue(bytes3, classType);
                ByteBuf byteBuf = ctx.alloc().buffer(Constants.headerLen2+bytes1.length+bytes2.length+bytes3.length);
                byteBuf.writeInt(type);
                byteBuf.writeInt(bytes1.length);
                byteBuf.writeInt(bytes2.length);
                byteBuf.writeInt(bytes3.length);
                byteBuf.writeBytes(bytes1);
                byteBuf.writeBytes(bytes2);
                byteBuf.writeBytes(bytes3);
                ctx.write(byteBuf, promise);
            } else {
                objToEncode = rpcContext.getRequest();
                RpcRequest r=(RpcRequest)objToEncode;
                byte[] bytes;
                bytes = objectMapper.writeValueAsBytes(objToEncode);
                ByteBuf byteBuf = ctx.alloc().buffer(Constants.headerLen1+bytes.length);
                //header
                byteBuf.writeInt(bytes.length);
                //body
                byteBuf.writeBytes(bytes);
                ctx.write(byteBuf, promise);
            }
        }
    }
}
