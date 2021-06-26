package com.huangxy.stone.loadbalance.impl;

import com.google.common.collect.Lists;
import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.loadbalance.LoadbalanceStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 加权随机负载均衡算法
 *
 * @author huangxy
 * @date 2021/06/23 17:00:40
 */
public class WeigthRandomLoadbalanceStrategyImpl implements LoadbalanceStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        // 存放加权后的服务提供者列表
        List<ProviderService> providerList = Lists.newArrayList();
        providerServiceList.forEach(item -> {
            int weight = item.getWeight();
            for (int i = 0; i < weight; i++) {
                providerList.add(item.copy());
            }
        });
        int MAX_LEN = providerList.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerList.get(index);
    }


}
