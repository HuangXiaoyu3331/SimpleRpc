package com.huangxy.stone.revoker;

import com.google.common.collect.Maps;
import com.huangxy.stone.bean.StoneResponse;
import com.huangxy.stone.bean.StoneResponseWrapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author huangxy
 * @date 2021/06/19 18:34:17
 */
public class RevokerResponseHolder {

    // 服务返回结果map
    private static final Map<String, StoneResponseWrapper> responseMap = Maps.newConcurrentMap();
    // 清除过期的返回结果
    private static final ExecutorService removeExpireKeyExecutor = Executors.newSingleThreadExecutor();

    static {
        // 删除超时未获取到结果的key，防止内存泄漏
        removeExpireKeyExecutor.execute(() -> {
            // 使用 ConcurrentHashMap，不会有 fail-fast 问题
            responseMap.forEach((serviceItf, stoneResponseWrapper) -> {
                boolean isExpire = stoneResponseWrapper.isExpire();
                if (isExpire) {
                    responseMap.remove(serviceItf);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static void initResponseData(String requestUniqueKey) {
        responseMap.put(requestUniqueKey, StoneResponseWrapper.of());
    }

    /**
     * 从阻塞队列中获取netty异步返回的结果值
     *
     * @param requestUniqueKey
     * @param timeout
     * @return
     */
    public static StoneResponse getValue(String requestUniqueKey, long timeout) {
        StoneResponseWrapper responseWrapper = responseMap.get(requestUniqueKey);
        try {
            return responseWrapper.getResponseQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 超时未获取到结果，清除过期的返回结果，防止内存泄漏
            responseMap.remove(requestUniqueKey);
        }
    }

    // todo: 这里有个bug，服务端超时返回，仍会调用putResultValue将结果放到responseMap，但是没有线程清理responseMap的对象，可能导致内存泄漏
    public static void putResultValue(StoneResponse response) {
        long currentTime = System.currentTimeMillis();
        StoneResponseWrapper responseWrapper = responseMap.get(response.getUniqueKey());
        responseWrapper.setResponseTime(currentTime);
        responseWrapper.getResponseQueue().add(response);
        responseMap.put(response.getUniqueKey(), responseWrapper);
    }
}
