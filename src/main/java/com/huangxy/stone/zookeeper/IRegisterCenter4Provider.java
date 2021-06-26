package com.huangxy.stone.zookeeper;

import com.huangxy.stone.bean.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * 服务端注册中心接口定义
 *
 * @author huangxy
 * @date 2021/06/18 15:39:37
 */
public interface IRegisterCenter4Provider {

    /**
     * 将服务提供者信息注册到zk对应节点下
     *
     * @param serviceMetaData
     */
    void registerProvider(List<ProviderService> serviceMetaData);

    /**
     * 获取服务提供者信息
     *
     * @return 服务提供者信息
     */
    Map<String, List<ProviderService>> getProviderServiceMap();

}
