package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Created by marsares on 15/7/26.
 */
public class RpcFuture implements Future<Object> {
    private RpcContext rpcCtx;
    private Sync sync;

    public RpcFuture(RpcContext rpcCtx){
        this.rpcCtx=rpcCtx;
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
    public Object get()throws InterruptedException,ExecutionException{
        sync.acquire(1);
        return processResponse();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if(success){
            return processResponse();
        }else{
            throw new RuntimeException("Timeout exception|objName="+rpcCtx.getRequest().getObjName()+"|funcName="+rpcCtx.getRequest().getFuncName());
        }
    }

    public void done(RpcResponse response){
        this.rpcCtx.setResponse(response);
        sync.release(1);
    }

    private Object processResponse(){
        if(rpcCtx.getResponse().getException()!=null){
            return rpcCtx.getResponse().getException();
        }
        return rpcCtx.getResponse().getAppResponse();
    }
}
