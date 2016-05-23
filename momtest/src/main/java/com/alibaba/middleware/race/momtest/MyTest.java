package com.alibaba.middleware.race.momtest;

import com.alibaba.middleware.race.mom.async.MessageListener;
import com.alibaba.middleware.race.mom.broker.DefaultBroker;
import com.alibaba.middleware.race.mom.consumer.Consumer;
import com.alibaba.middleware.race.mom.consumer.DefaultConsumer;
import com.alibaba.middleware.race.mom.model.*;
import com.alibaba.middleware.race.mom.producer.DefaultProducer;
import com.alibaba.middleware.race.mom.producer.Producer;

import java.nio.charset.Charset;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by marsares on 15/8/11.
 */
public class MyTest {
    private static String TOPIC="MOM-RACE-";
    private static String PID="PID_";
    private static String CID="CID_";
    private static String BODY="hello mom ";
    private static String AREA="area_";
    private static Charset charset=Charset.forName("utf-8");
    private static Random random=new Random();
    private static Queue<String> sendMsg=new LinkedBlockingQueue<String>();
    private static Queue<String> recvMsg=new LinkedBlockingQueue<String>();
    private static TestResult testResult=new TestResult();

    public static void main(String[]args){
        int code=random.nextInt(100);
        final ConsumeResult consumeResult=new ConsumeResult();
        consumeResult.setStatus(ConsumeStatus.SUCCESS);
        final String topic=TOPIC+code;
        try {
            Consumer consumer=new DefaultConsumer();
            consumer.setGroupId(CID);
            consumer.subscribe(topic, "area=hz"+code, new MessageListener() {

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
                    recvMsg.add(message.getMsgId());
                    return consumeResult;
                }
            });
            consumer.start();
            Consumer consumer1=new DefaultConsumer();
            consumer1.setGroupId(CID);
            consumer1.subscribe(topic, "area=hz" + code, new MessageListener() {

                @Override
                public ConsumeResult onMessage(Message message) {
                    if (!message.getTopic().equals(topic)) {
                        testResult.setSuccess(false);
                        testResult.setInfo("expect topic:" + topic + ", actual topic:" + message.getTopic());
                    }
                    if (System.currentTimeMillis() - message.getBornTime() > 1000) {
                        testResult.setSuccess(false);
                        testResult.setInfo("msg " + message.getMsgId() + " delay " + (System.currentTimeMillis() - message.getBornTime()) + " ms");
                    }
                    recvMsg.add(message.getMsgId());
                    return consumeResult;
                }
            });
            consumer1.start();
            Consumer consumer2=new DefaultConsumer();
            consumer2.setGroupId(CID + 2);
            consumer2.subscribe(topic, "", new MessageListener() {

                @Override
                public ConsumeResult onMessage(Message message) {
                    if (!message.getTopic().equals(topic)) {
                        testResult.setSuccess(false);
                        testResult.setInfo("expect topic:" + topic + ", actual topic:" + message.getTopic());
                    }
                    if (System.currentTimeMillis() - message.getBornTime() > 1000) {
                        testResult.setSuccess(false);
                        testResult.setInfo("msg " + message.getMsgId() + " delay " + (System.currentTimeMillis() - message.getBornTime()) + " ms");
                    }
                    //recvMsg.add(message.getMsgId());
                    return consumeResult;
                }
            });
            consumer2.start();
            Consumer consumer3=new DefaultConsumer();
            consumer3.setGroupId(CID);
            consumer3.subscribe(topic, "", new MessageListener() {

                @Override
                public ConsumeResult onMessage(Message message) {
                    if (!message.getTopic().equals(topic)) {
                        testResult.setSuccess(false);
                        testResult.setInfo("expect topic:" + topic + ", actual topic:" + message.getTopic());
                    }
                    if (System.currentTimeMillis() - message.getBornTime() > 1000) {
                        testResult.setSuccess(false);
                        testResult.setInfo("msg " + message.getMsgId() + " delay " + (System.currentTimeMillis() - message.getBornTime()) + " ms");
                    }
                    recvMsg.add(message.getMsgId());
                    return consumeResult;
                }
            });
            consumer3.start();
            Producer producer=new DefaultProducer();
            producer.setGroupId(PID);
            producer.setTopic(topic);
            producer.start();
            for(int i=0;i<12;i++){
                Message msg=new Message();
                msg.setBody(BODY.getBytes(charset));
                msg.setProperty("area", "hz" + code);
                SendResult result=producer.sendMessage(msg);
                if(result.getStatus()==SendStatus.SUCCESS){
                    System.out.println("success");
                }
                if (result.getStatus()!=SendStatus.SUCCESS) {
                    testResult.setSuccess(false);
                    testResult.setInfo(result.toString());
                    return;
                }
                sendMsg.add(result.getMsgId());
            }
            Thread.sleep(5000);
            if (!testResult.isSuccess()) {
                System.out.println(testResult);
                return ;
            }
            checkMsg(sendMsg, recvMsg);
            System.out.println("result=" + testResult.isSuccess());
            producer.stop();
            consumer.stop();
            consumer1.stop();
            consumer2.stop();
            consumer3.stop();
        } catch (Exception e) {
            testResult.setSuccess(false);
            testResult.setInfo(e.getMessage());
        }
    }

    private static void checkMsg(Queue<String> sendMsg,Queue<String> recvMsg){
        System.out.println("start checking");
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
