package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.protocol.DefaultDecoder;
import com.alibaba.middleware.race.rpc.protocol.DefaultEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by marsares on 15/7/24.
 */
public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation
        final ChannelPipeline p = ch.pipeline();
        p.addLast(new DefaultDecoder(true));
        p.addLast(new DefaultEncoder());
        p.addLast(new DefaultHandler());
    }
}
