package com.alibaba.middleware.race.mom.broker;

import com.alibaba.middleware.race.mom.context.Context;
import com.alibaba.middleware.race.mom.model.ConsumeRegist;
import com.alibaba.middleware.race.mom.model.Message;
import com.alibaba.middleware.race.mom.model.SendAck;
import com.alibaba.middleware.race.mom.producer.SendFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by marsares on 15/8/9.
 */
public class DefaultBrokerHandler extends ChannelInboundHandlerAdapter {
    private volatile Channel channel;
    private ConcurrentHashMap<String,SendFuture> pendingSend=new ConcurrentHashMap<String,SendFuture>();

    private ConcurrentHashMap<String, HashMap<String, Group>> groupLists;

    public ConcurrentHashMap<String, HashMap<String, Group>> getGroupLists() {
        return groupLists;
    }

    public void setGroupLists(ConcurrentHashMap<String, HashMap<String, Group>> groupLists) {
        this.groupLists = groupLists;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Context) {
            Context context = (Context) msg;
            if (context.getConsumeRegist() != null) {
                processRegist(ctx, context);
            }
            if (context.getMessage() != null) {
                //waitRegist();
                System.out.println("msg"+context.getMessage().getMsgId()+"delay="+(System.currentTimeMillis()-context.getMessage().getBornTime()));
                System.out.println("msg"+context.getMessage().getMsgId()+"current time="+System.currentTimeMillis());
                //sendToHandlers(context);
                //saveMessage(context);
                //processMessage(ctx, context);
            }
            if(context.getSendAck()!=null){
                SendAck sendAck=context.getSendAck();
                String ackId=sendAck.getAckId();
                System.out.println("receive ack="+ackId);
                SendFuture sendFuture=pendingSend.get(ackId);
                if(sendFuture!=null){
                    pendingSend.remove(ackId);
                    sendFuture.done(sendAck);
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("connected");
        channel = ctx.channel();
    }

    public void processRegist(ChannelHandlerContext ctx, Context context) {
        System.out.println("process regist");
        ConsumeRegist consumeRegist = context.getConsumeRegist();
        String topic = consumeRegist.getTopic();
        String groupId = consumeRegist.getGroupId();
        String filter = consumeRegist.getFilter();
        synchronized(groupLists){
            if (groupLists.containsKey(topic)) {
                HashMap<String, Group> hm = groupLists.get(topic);
                if (hm.containsKey(groupId)) {
                    Group group = hm.get(groupId);
                    HashMap<String, FilterList> filterLists = group.getFilterLists();
                    if (filterLists.containsKey(filter)) {
                        FilterList filterList = filterLists.get(filter);
                        filterList.put(this);
                    } else {
                        FilterList filterList = new FilterList();
                        filterList.put(this);
                        filterLists.put(filter, filterList);
                    }
                } else {
                    Group group = new Group();
                    HashMap<String, FilterList> filterLists = group.getFilterLists();
                    FilterList filterList = new FilterList();
                    filterList.put(this);
                    filterLists.put(filter, filterList);
                    hm.put(groupId, group);
                }
            } else {
                HashMap<String, Group> hm = new HashMap<String, Group>();
                Group group = new Group();
                HashMap<String, FilterList> filterLists = group.getFilterLists();
                FilterList filterList = new FilterList();
                filterList.put(this);
                filterLists.put(filter, filterList);
                hm.put(groupId, group);
                groupLists.put(topic, hm);
            }
        }
    }


    public void processMessage(ChannelHandlerContext ctx, Context context) {
        SendAck sendAck = new SendAck();
        sendAck.setAckId(context.getMessage().getMsgId());
        sendAck.setAck("success");
        Context backContext = new Context();
        backContext.setSendAck(sendAck);
        ctx.writeAndFlush(backContext);
    }

    public void sendMessage(Context context) {
        System.out.println("send message id="+context.getMessage().getMsgId());
        SendFuture sendFuture=new SendFuture(context);
        pendingSend.put(context.getMessage().getMsgId(), sendFuture);
        channel.writeAndFlush(context);
        try{
            sendFuture.get(10000, TimeUnit.MILLISECONDS);
        }catch(Exception e){
            sendMessage(context);
        }
    }

    public void sendToHandlers(Context context){
        Message message = context.getMessage();
        String topic = message.getTopic();
        String filter = getFilter(message.getProperties());

        if (groupLists.containsKey(topic)) {
            HashMap<String, Group> hm = groupLists.get(topic);
            for (String key : hm.keySet()) {
                Group group = hm.get(key);
                HashMap<String, FilterList> filterLists = group.getFilterLists();
                if (filterLists.containsKey(filter)) {
                    if (!filterLists.containsKey("")) {
                        FilterList filterList = filterLists.get(filter);
                        DefaultBrokerHandler defaultBrokerHandler = filterList.get();
                        defaultBrokerHandler.sendMessage(context);
                    } else {
                        FilterList filterList = filterLists.get(filter);
                        if (!filterList.isPending()) {
                            DefaultBrokerHandler defaultBrokerHandler = filterList.get();
                            defaultBrokerHandler.sendMessage(context);
                        } else {
                            FilterList filterList1 = filterLists.get("");
                            DefaultBrokerHandler defaultBrokerHandler = filterList1.get();
                            defaultBrokerHandler.sendMessage(context);
                            if (filterList1.isPending()) {
                                filterList1.setPending(false);
                                filterList.setPending(false);
                            }
                        }
                    }
                } else if (filterLists.containsKey("")) {
                    FilterList filterList = filterLists.get("");
                    DefaultBrokerHandler defaultBrokerHandler = filterList.get();
                    defaultBrokerHandler.sendMessage(context);
                }
            }
        }
    }

    public String getFilter(Map<String, String> properties){
        for(String key:properties.keySet()){
            return key+"="+properties.get(key);
        }
        return null;
    }

    public void waitRegist(){
        while(groupLists.size()==0){

        }
    }

    public void saveMessage(Context context){
        Message message=context.getMessage();
        String MsgId = message.getMsgId();
        String userhome = System.getProperty("user.home");
        String filename = userhome + "/store/" + MsgId;
        System.out.println("save "+MsgId);
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(message);
            out.close();
            fileOut.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public Message getMessage(String MsgId){
        String userhome = System.getProperty("user.home");
        String filename = userhome + "/store/" + MsgId;
        try{
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Message message = (Message) in.readObject();
            return message;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
