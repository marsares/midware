package com.alibaba.middleware.race.momtest;

import com.alibaba.middleware.race.mom.producer.DefaultProducer;
import com.alibaba.middleware.race.mom.model.Message;
import com.alibaba.middleware.race.mom.producer.Producer;
import com.alibaba.middleware.race.mom.async.SendCallback;
import com.alibaba.middleware.race.mom.model.SendResult;
import com.alibaba.middleware.race.mom.model.SendStatus;

public class ProducerAsyncTest {
	public static void main(String[] args) {
		Producer producer=new DefaultProducer();
		producer.setGroupId("PG-test");
		producer.setTopic("T-test");
		producer.start();
		Message message=new Message();
		message.setBody("Hello MOM".getBytes());
		message.setProperty("area", "us");
		//调用此方法，当前线程不阻塞
		producer.asyncSendMessage(message, new SendCallback() {
			
			@Override
			public void onResult(SendResult result) {
				if (result.getStatus().equals(SendStatus.SUCCESS)) {
					System.out.println("send success:"+result.getMsgId());
				}
			}
		});
		
	}
}
