package com.alibaba.middleware.race.mom.consumer;


import com.alibaba.middleware.race.mom.async.MessageListener;
import com.alibaba.middleware.race.mom.context.Context;
import com.alibaba.middleware.race.mom.model.ConsumeRegist;
import com.alibaba.middleware.race.mom.protocol.DefaultDecoder;
import com.alibaba.middleware.race.mom.protocol.DefaultEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class DefaultConsumer implements Consumer {
    private volatile Channel channel;
    private DefaultConsumerHandler handler;
    private EventLoopGroup workerGroup;
    private String groupId;
    private MessageListener listener;
    private String topic;
    private String filter;

    public DefaultConsumer() {
        //String brokerIp = System.getProperty("SIP");
        String brokerIp="127.0.0.1";
        int port = 8888;
        workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    handler = new DefaultConsumerHandler();
                    ch.pipeline().addLast(new DefaultDecoder());
                    ch.pipeline().addLast(new DefaultEncoder());
                    ch.pipeline().addLast(handler);
                }
            });
            channel = b.connect(brokerIp, port).awaitUninterruptibly().channel();
            handler.setChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        Context context=new Context();
        ConsumeRegist consumeRegist=new ConsumeRegist();
        consumeRegist.setFilter(filter);
        consumeRegist.setGroupId(groupId);
        consumeRegist.setTopic(topic);
        context.setConsumeRegist(consumeRegist);
        handler.regist(context);
        handler.setListener(listener);
    }

    @Override
    public void subscribe(String topic, String filter, MessageListener listener) {
        this.topic=topic;
        this.filter=filter;
        this.listener=listener;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId=groupId;

    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }

}
