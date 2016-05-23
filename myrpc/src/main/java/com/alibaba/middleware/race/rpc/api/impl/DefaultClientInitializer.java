package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.protocol.DefaultDecoder;
import com.alibaba.middleware.race.rpc.protocol.DefaultEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by marsares on 15/7/27.
 */
public class DefaultClientInitializer extends ChannelInitializer<SocketChannel> {
    DefaultClientHandler handler;

    public DefaultClientHandler getHandler() {
        return handler;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        handler = new DefaultClientHandler();
        ch.pipeline().addLast(new DefaultDecoder(false));
        ch.pipeline().addLast(new DefaultEncoder());
        ch.pipeline().addLast(handler);
    }
}
