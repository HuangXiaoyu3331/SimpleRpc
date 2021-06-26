package com.huangxy.stone.loadbalance.impl;

import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.loadbalance.LoadbalanceStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 随机负载均衡算法
 *
 * @author huangxy
 * @date 2021/06/19 18:03:40
 */
public class RandomLoadbalanceStrategyImpl implements LoadbalanceStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        int MAX_LEN = providerServiceList.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerServiceList.get(index);
    }

}
