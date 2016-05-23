package com.alibaba.middleware.race.momtest;

import com.alibaba.middleware.race.mom.model.Message;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by marsares on 15/8/13.
 */
public class SerializeTest {
    public static void main(String[] args) {
        Message messagew = new Message();
        String BODY="hello mom ";
        Charset charset=Charset.forName("utf-8");
        messagew.setBody(BODY.getBytes(charset));
        messagew.setProperty("area", "hz");
        messagew.setTopic("topic");
        String MsgId = messagew.getMsgId();
        String userhome = System.getProperty("user.home");
        String filename = userhome + "/store/" + MsgId;
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(messagew);
            out.close();
            fileOut.close();
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Message messager = (Message) in.readObject();
            System.out.println("topic=" + messager.getTopic() + ",msgId=" + messager.getMsgId() + ",borntime=" + messager.getBornTime());
            byte[]body=messager.getBody();
            System.out.println("body="+new String(body));
            Map<String, String> properties=messager.getProperties();
            for(String key:properties.keySet()){
                System.out.println(key+"="+properties.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
