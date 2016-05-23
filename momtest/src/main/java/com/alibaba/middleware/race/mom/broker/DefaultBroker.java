package com.alibaba.middleware.race.mom.broker;

import com.alibaba.middleware.race.mom.consumer.DefaultConsumerHandler;
import com.alibaba.middleware.race.mom.protocol.DefaultDecoder;
import com.alibaba.middleware.race.mom.protocol.DefaultEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marsares on 15/8/9.
 */
public class DefaultBroker {
    private int port;
    private ConcurrentHashMap<String,HashMap<String,Group>> groupLists=new ConcurrentHashMap<String, HashMap<String,Group>>();
    public DefaultBroker(){
        this.port=8888;
    }

    public void publish(){
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    DefaultBrokerHandler handler = new DefaultBrokerHandler();
                    handler.setGroupLists(groupLists);
                    ch.pipeline().addLast(new DefaultDecoder());
                    ch.pipeline().addLast(new DefaultEncoder());
                    ch.pipeline().addLast(handler);
                }
            });
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
