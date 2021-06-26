package com.huangxy.stone.zookeeper;

import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.framwork.InvokerService;

import java.util.List;
import java.util.Map;

/**
 * @author huangxy
 * @date 2021/06/19 01:31:03
 */
public interface IRegisterCenter4Invoker {

    /**
     * 客户端初始化服务提供者信息到本地缓存
     *
     * @param remoteAppKey
     * @param groupName
     */
    void initProviderMap(String remoteAppKey, String groupName);

    /**
     * 消费端获取服务提供者信息
     *
     * @return
     */
    Map<String, List<ProviderService>> getServiceMetaDataMap4Consume();

    /**
     * 消费端将服务提供者信息注册到zk对应的节点下面
     *
     * @param invoker
     */
    void registerInvoker(InvokerService invoker);
}
