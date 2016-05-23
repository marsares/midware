package com.alibaba.middleware.race.rpc.protocol;

import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by marsares on 15/7/26.
 */
public class DefaultDecoder extends ByteToMessageDecoder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private boolean decodeRequest;

    public DefaultDecoder(boolean decodeRequest) {
        this.decodeRequest = decodeRequest;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int packetLength = 0;
        int type=0;
        int bodyLength = 0;
        int bodyLength1 = 0;
        int bodyLength2 = 0;
        int bodyLength3 = 0;
        int readerIndex = in.readerIndex();
        if (decodeRequest) {
            int headerLength = Constants.headerLen1;
            if (in.readableBytes() < headerLength) {
                System.out.println(in.readableBytes());
                return;
            } else if (in.readableBytes() >= headerLength) {
                bodyLength = in.getInt(readerIndex);
                packetLength = headerLength + bodyLength;
            }

            //  we have got the length of the package
            if (packetLength > headerLength) {
                if (in.readableBytes() < packetLength) {
                    return;
                } else {
                    byte[] body = new byte[bodyLength];
                    in.getBytes(readerIndex + headerLength, body);             //todo  : avoid memory copy
                    RpcContext context = new RpcContext();
                    try {
                        RpcRequest req = objectMapper.readValue(body, RpcRequest.class);
                        System.out.println(req.getObjName() + req.getFuncName() + req.getSeqNum());
                        context.setRequest(req);
                    } catch (Exception e) {
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
        } else {
            int headerLength = Constants.headerLen2;
            if (in.readableBytes() < headerLength) {
                System.out.println(in.readableBytes());
                return;
            } else if (in.readableBytes() >= headerLength) {
                type=in.getInt(readerIndex);
                bodyLength1 = in.getInt(readerIndex+4);
                bodyLength2 = in.getInt(readerIndex + 8);
                bodyLength3 = in.getInt(readerIndex + 12);
                packetLength = headerLength + bodyLength1 + bodyLength2 + bodyLength3;
            }
            if (packetLength > headerLength) {
                if (in.readableBytes() < packetLength) {
                    return;
                } else {
                    byte[] body1 = new byte[bodyLength1];
                    byte[] body2 = new byte[bodyLength2];
                    byte[] body3 = new byte[bodyLength3];
                    in.getBytes(readerIndex + headerLength, body1);
                    in.getBytes(readerIndex + headerLength + bodyLength1, body2);
                    in.getBytes(readerIndex + headerLength + bodyLength1 + bodyLength2, body3);
                    RpcContext context = new RpcContext();
                    try {
                        RpcResponse res = objectMapper.readValue(body1, RpcResponse.class);
                        Class<?> resultClass = objectMapper.readValue(body2, Class.class);
                        Object result = objectMapper.readValue(body3, resultClass);
                        if(type==1)res.setAppResponse(result);
                        else res.setException(result);
                        context.setResponse(res);
                    } catch (Exception e) {
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
            if (bodyLength1+bodyLength2+bodyLength3 <= 0) { //Unrecoverable error, have to close connection.
                ctx.close();
            }
        }
    }
}

