package com.alibaba.middleware.race.momtest;

import java.nio.charset.Charset;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.middleware.race.mom.model.ConsumeResult;
import com.alibaba.middleware.race.mom.model.ConsumeStatus;
import com.alibaba.middleware.race.mom.consumer.Consumer;
import com.alibaba.middleware.race.mom.consumer.DefaultConsumer;
import com.alibaba.middleware.race.mom.producer.DefaultProducer;
import com.alibaba.middleware.race.mom.model.Message;
import com.alibaba.middleware.race.mom.async.MessageListener;
import com.alibaba.middleware.race.mom.producer.Producer;
import com.alibaba.middleware.race.mom.async.SendCallback;
import com.alibaba.middleware.race.mom.model.SendResult;
import com.alibaba.middleware.race.mom.model.SendStatus;

public class MomStressTest {
	private static AtomicLong callAmount = new AtomicLong(0L);
	private static String TOPIC="MOM-RACE-";
	private static String PID="PID_";
	private static String CID="CID_";
	private static String BODY="hello mom ";
	private static String AREA="area_";
	private static Charset charset=Charset.forName("utf-8");
	private static Random random=new Random();
	private static AtomicLong sendCount=new AtomicLong();
	private static AtomicLong recvCount=new AtomicLong();
	private static AtomicLong totalRT=new AtomicLong();
	private static AtomicLong totalDelay=new AtomicLong();

	private static ExecutorService executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static TestResult testResult=new TestResult();
	public static void main(String[] args) {
		testBasic();
		System.out.println("after basic");
		if (!testResult.isSuccess()) {
			System.out.println(testResult);
			FileIO.write(testResult.toString());
			System.out.println("after write");
			Runtime.getRuntime().exit(0);
			//return ;
		}
		System.out.println(testResult);
		FileIO.write(testResult.toString());
		Runtime.getRuntime().exit(0);
	}
	private static void testBasic() {
		final int code=random.nextInt(100);
		final ConsumeResult consumeResult=new ConsumeResult();
		consumeResult.setStatus(ConsumeStatus.SUCCESS);
		final String topic=TOPIC+code;
		try {
			String ip=System.getProperty("SIP");
			Consumer consumer=new DefaultConsumer();
			consumer.setGroupId(CID);
			consumer.subscribe(topic, "", new MessageListener() {
				
				@Override
				public ConsumeResult onMessage(Message message) {
					if (!message.getTopic().equals(topic)) {
						testResult.setSuccess(false);
						testResult.setInfo("expect topic:"+topic+", actual topic:"+message.getTopic());
					}
					long delay=System.currentTimeMillis()-message.getBornTime();
					if (delay>1000) {
						testResult.setSuccess(false);
						testResult.setInfo("msg "+message.getMsgId()+" delay "+(System.currentTimeMillis()-message.getBornTime())+" ms");
					}
					totalDelay.addAndGet(delay);
					recvCount.incrementAndGet();
					return consumeResult;
				}
			});
			consumer.start();
			final Producer producer=new DefaultProducer();
			producer.setGroupId(PID);
			producer.setTopic(topic);
			producer.start();
			long start=System.currentTimeMillis();
			for (int i = 0; i < 1;i++){//(Runtime.getRuntime().availableProcessors()-1); i++) {
				executorService.execute(new Runnable() {
					
					@Override
					public void run() {
						while (callAmount.get() < 10000) {
							try {
								System.out.println("call amount="+callAmount.incrementAndGet());
								Message msg=new Message();
								System.out.println("msg"+msg.getMsgId()+"born at "+msg.getBornTime());
								msg.setBody(BODY.getBytes(charset));
								msg.setProperty("area", "hz"+code);
								final long startRt=System.currentTimeMillis();
								producer.asyncSendMessage(msg, new SendCallback() {
									
									@Override
									public void onResult(SendResult result) {
										if (result.getStatus()==SendStatus.SUCCESS) {
											sendCount.incrementAndGet();
											totalRT.addAndGet(System.currentTimeMillis()-startRt);
										}
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					}
				});
			}
			Thread.sleep(3000);
			System.out.println("after sleep");
			if (!testResult.isSuccess()) {
				return ;
			}
			long totalTime=System.currentTimeMillis()-start;
			long sendTps=sendCount.get()*1000/totalTime;
			long recvTps=recvCount.get()*1000/totalTime;
			long rt=totalRT.get()/sendCount.get();
			long delay=totalDelay.get()/recvCount.get();
			testResult.setInfo("send tps:"+sendTps+", recv tps:"+recvTps+", send rt:"+rt+", avg delay:"+delay);
			
		} catch (Exception e) {
			testResult.setSuccess(false);
			testResult.setInfo(e.getMessage());
		}
	}
	private static void testFilter() {
		int code=random.nextInt(100);
		final ConsumeResult consumeResult=new ConsumeResult();
		consumeResult.setStatus(ConsumeStatus.SUCCESS);
		final String topic=TOPIC+code;
		final String k=AREA+code;
		final String v="hz_"+code;
		try {
			String ip=System.getProperty("SIP");
			Consumer consumer=new DefaultConsumer();
			consumer.setGroupId(CID);
			consumer.subscribe(topic, k+"="+v, new MessageListener() {
				
				@Override
				public ConsumeResult onMessage(Message message) {
					if (!message.getTopic().equals(topic)) {
						testResult.setSuccess(false);
						testResult.setInfo("expect topic:"+topic+", actual topic:"+message.getTopic());
					}
					if (System.currentTimeMillis()-message.getBornTime()>1000) {
						testResult.setSuccess(false);
						testResult.setInfo("msg "+message.getMsgId()+" delay "+(System.currentTimeMillis()-message.getBornTime())+" ms");
					}
					if (!message.getProperty(k).equals(v)) {
						testResult.setSuccess(false);
						testResult.setInfo("msg "+message.getMsgId()+" expect k"+k+"  value is "+ v+", but actual value is "+message.getProperty(k));
					}
					return consumeResult;
				}
			});
			consumer.start();
			Producer producer=new DefaultProducer();
			producer.setGroupId(PID);
			producer.setTopic(topic);
			producer.start();
			Message msg=new Message();
			msg.setBody(BODY.getBytes(charset));
			msg.setProperty(k, v);
			SendResult result=producer.sendMessage(msg);
			if (result.getStatus()!=SendStatus.SUCCESS) {
				testResult.setSuccess(false);
				testResult.setInfo(result.toString());
				return;
			}
			msg=new Message();
			msg.setBody(BODY.getBytes(charset));
			result=producer.sendMessage(msg);
			if (result.getStatus()!=SendStatus.SUCCESS) {
				testResult.setSuccess(false);
				testResult.setInfo(result.toString());
				return;
			}
			Thread.sleep(5000);
			if (!testResult.isSuccess()) {
				return ;
			}


		} catch (Exception e) {
			testResult.setSuccess(false);
			testResult.setInfo(e.getMessage());
		}
	}
	private static void checkMsg(Queue<String> sendMsg,Queue<String> recvMsg){
		if (sendMsg.size()>recvMsg.size()) {
			testResult.setSuccess(false);
			testResult.setInfo("send msg num is "+sendMsg.size()+",but recv msg num is "+recvMsg.size());
			return ;
		}
		if ((recvMsg.size()-sendMsg.size())/sendMsg.size()>0.001) {
			testResult.setSuccess(false);
			testResult.setInfo("repeat rate too big "+(recvMsg.size()-sendMsg.size())/sendMsg.size());
			return ;
		}
		for (String send : sendMsg) {
			boolean find=false;
			for (String recv : recvMsg) {
				if (recv.equals(send)) {
					find=true;
					break;
				}
			}
			if (!find) {
				testResult.setSuccess(false);
				testResult.setInfo("msg "+send+" is miss");
				return ;
			}
		}

	}
}
