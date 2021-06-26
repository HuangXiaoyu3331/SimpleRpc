package com.huangxy.stone.bean;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * netty 异步返回结果包装类
 *
 * @author huangxy
 * @date 2021/06/19 18:35:57
 */
public class StoneResponseWrapper {

    // 存储返回结果阻塞队列
    private ArrayBlockingQueue<StoneResponse> responseQueue = new ArrayBlockingQueue<>(1);
    // 结果返回时间
    private long responseTime;

    public static StoneResponseWrapper of() {
        return new StoneResponseWrapper();
    }

    public boolean isExpire() {
        StoneResponse response = responseQueue.peek();
        if (response == null) {
            return false;
        }

        long timeout = response.getInvokeTimeout();
        if ((System.currentTimeMillis() - responseTime) > timeout) {
            return true;
        }
        return false;
    }

    public ArrayBlockingQueue<StoneResponse> getResponseQueue() {
        return responseQueue;
    }

    public void setResponseQueue(ArrayBlockingQueue<StoneResponse> responseQueue) {
        this.responseQueue = responseQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
