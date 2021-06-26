package com.huangxy.stone.loadbalance;


import com.google.common.collect.Maps;
import com.huangxy.stone.loadbalance.impl.RoundrobinLoadbalanceStrategyImpl;
import com.huangxy.stone.loadbalance.impl.RandomLoadbalanceStrategyImpl;

import java.util.Map;

/**
 * @author huangxy
 * @date 2021/06/23 14:55:05
 */
public class LoadbalanceEngine {

    public static final Map<LoadbalanceStrategyEnum, LoadbalanceStrategy> clusterStrategyMap = Maps.newConcurrentMap();

    static {
        clusterStrategyMap.put(LoadbalanceStrategyEnum.Random, new RandomLoadbalanceStrategyImpl());
        clusterStrategyMap.put(LoadbalanceStrategyEnum.Roundrobin, new RoundrobinLoadbalanceStrategyImpl());
    }

    public static LoadbalanceStrategy queryClusterStrategy(String loadbalance) {
        LoadbalanceStrategyEnum loadbalanceStrategyEnum = LoadbalanceStrategyEnum.getByCode(loadbalance);
        if (loadbalanceStrategyEnum == null) {
            return new RandomLoadbalanceStrategyImpl();
        }
        return clusterStrategyMap.get(loadbalanceStrategyEnum);
    }

}
