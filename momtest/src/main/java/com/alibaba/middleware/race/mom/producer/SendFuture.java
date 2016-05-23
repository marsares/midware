package com.alibaba.middleware.race.mom.producer;

import com.alibaba.middleware.race.mom.context.Context;
import com.alibaba.middleware.race.mom.model.SendAck;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Created by marsares on 15/8/10.
 */
public class SendFuture implements Future<Object> {
    private Context context;
    private Sync sync;

    public SendFuture(Context context){
        this.context=context;
        this.sync=new Sync();
    }

    static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        protected boolean tryAcquire(int acquires) {
            if(getState()==done){
                return true;
            }
            return false;
        }

        protected  boolean tryRelease(int releases) {
            if (getState() == pending) {
                if (compareAndSetState(pending, 1)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone(){
            getState();
            return getState()==done;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        return getAck();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if(success){
            return getAck();
        }else{
            throw new RuntimeException("Timeout exception");
        }
    }

    public void done(SendAck sendAck){
        this.context.setSendAck(sendAck);
        sync.release(1);
    }

    private String getAck(){
        return context.getSendAck().getAck();
    }
}
