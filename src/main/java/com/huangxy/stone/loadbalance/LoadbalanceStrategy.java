package com.huangxy.stone.loadbalance;

import com.huangxy.stone.bean.ProviderService;

import java.util.List;

/**
 * @author huangxy
 * @date 2021/06/19 18:02:22
 */
public interface LoadbalanceStrategy {

    /**
     * 负载均衡算法
     * @param providerServiceList
     * @return
     */
    ProviderService select(List<ProviderService> providerServiceList);

}
