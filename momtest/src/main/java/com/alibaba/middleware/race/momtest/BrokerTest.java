package com.alibaba.middleware.race.momtest;

import com.alibaba.middleware.race.mom.broker.DefaultBroker;

/**
 * Created by marsares on 15/8/11.
 */
public class BrokerTest {
    public static void main(String[]args){
        DefaultBroker broker=new DefaultBroker();
        broker.publish();
    }
}
