package com.alibaba.middleware.race.mom.producer;


import com.alibaba.middleware.race.mom.async.SendCallback;
import com.alibaba.middleware.race.mom.context.Context;
import com.alibaba.middleware.race.mom.model.Message;
import com.alibaba.middleware.race.mom.model.SendResult;
import com.alibaba.middleware.race.mom.model.SendStatus;
import com.alibaba.middleware.race.mom.protocol.DefaultDecoder;
import com.alibaba.middleware.race.mom.protocol.DefaultEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultProducer implements Producer {
	private volatile Channel channel;
	private DefaultProducerHandler handler;
	private EventLoopGroup workerGroup;
	private String groupId;
	private String topic;

	public DefaultProducer() {
		//String brokerIp=System.getProperty("SIP");
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
					handler = new DefaultProducerHandler();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTopic(String topic) {
		this.topic=topic;
	}

	@Override
	public void setGroupId(String groupId) {
		this.groupId=groupId;
	}

	@Override
	public SendResult sendMessage(Message message) {
		message.setTopic(topic);
		SendResult sendResult=new SendResult();
		sendResult.setMsgId(message.getMsgId());
		Context context=new Context();
		context.setMessage(message);
		String ack=handler.send(context);
		if(ack==null)sendResult.setStatus(SendStatus.FAIL);
		if(ack.equals("success"))sendResult.setStatus(SendStatus.SUCCESS);
		return sendResult;
	}

	@Override
	public void asyncSendMessage(Message message, SendCallback callback) {
		handler.setCallback(message.getMsgId(), callback);
		message.setTopic(topic);
		Context context=new Context();
		context.setMessage(message);
		System.out.println("msg"+context.getMessage().getMsgId()+"current time="+System.currentTimeMillis());
		handler.sendAsync(context);
	}

	@Override
	public void stop() {
		workerGroup.shutdownGracefully();
	}
	
}
