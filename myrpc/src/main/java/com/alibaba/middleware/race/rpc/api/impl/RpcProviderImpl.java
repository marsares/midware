package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.api.RpcProvider;
import com.alibaba.middleware.race.rpc.context.RpcContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;

/**
 * Created by marsares on 15/7/23.
 */
public class RpcProviderImpl extends RpcProvider {
    private int port;
    private static HashMap<String,Object>objects=new HashMap<String,Object>();

    public static Object getServiceInstance(String objName) {
        return objects.get(objName);
    }

    public RpcProviderImpl() {
        this.port = 8888;
        objects.put("com.alibaba.middleware.race.rpc.context.RpcContext",new RpcContext());
    }

    @Override
    public RpcProvider impl(Object serviceInstance){
        objects.put(serviceInstance.getClass().getName(),serviceInstance);
        return this;
    }

    @Override
    public void publish(){
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new DefaultServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
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
