package com.huangxy.stone.loadbalance.impl;

import com.google.common.collect.Lists;
import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.loadbalance.LoadbalanceStrategy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 轮询加权负载均衡算法
 *
 * @author huangxy
 * @date 2021/06/23 16:11:15
 */
public class WeightRoundrobinLoadbanlanceStrategyImpl implements LoadbalanceStrategy {

    private Lock lock = new ReentrantLock();
    private int index = 0;

    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        ProviderService providerService = null;
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);

            // 存放加权后的服务提供者列表
            List<ProviderService> providerList = Lists.newArrayList();
            providerServiceList.forEach(item -> {
                int weigth = item.getWeight();
                for (int i = 0; i < weigth; i++) {
                    providerList.add(item.copy());
                }
            });

            // 若计数>=服务提供者数量，将计数器归0
            if (index >= providerList.size()) {
                index = 0;
            }

            providerService = providerList.get(index);
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
