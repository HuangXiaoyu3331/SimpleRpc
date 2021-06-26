package com.huangxy.stone.loadbalance.impl;

import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.loadbalance.LoadbalanceStrategy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 轮询负载均衡算法
 *
 * @author huangxy
 * @date 2021/06/23 14:33:17
 */
public class RoundrobinLoadbalanceStrategyImpl implements LoadbalanceStrategy {

    private Lock lock = new ReentrantLock();
    private int index = 0;

    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        ProviderService providerService = null;
        try {
            lock.tryLock(10, TimeUnit.MICROSECONDS);

            if (index > providerServiceList.size()) {
                index = 0;
            }

            providerService = providerServiceList.get(index);
            index++;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        // 兜底，保证程序健壮性，若获取不到服务，则取第一个
        if (providerService == null) {
            providerService = providerServiceList.get(0);
        }

        return providerService;

    }

}
