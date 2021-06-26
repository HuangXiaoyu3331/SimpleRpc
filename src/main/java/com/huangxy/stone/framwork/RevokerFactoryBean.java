package com.huangxy.stone.framwork;

import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.revoker.NettyChannelPoolFactory;
import com.huangxy.stone.zookeeper.IRegisterCenter4Invoker;
import com.huangxy.stone.zookeeper.RegisterCenter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author huangxy
 * @date 2021/06/19 01:28:28
 */
public class RevokerFactoryBean implements FactoryBean, InitializingBean {

    //服务接口
    private Class<?> targetInterface;
    //服务bean
    private Object serviceObject;
    //服务提供者唯一标识
    private String remoteAppKey = "ares";
    //服务分组组名
    private String groupName = "default";
    //负载均衡策略
    private String loadbalance;
    //超时时间
    private int timeout = 3000;

    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }

    public Class<?> getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 获取服务注册中心
        IRegisterCenter4Invoker registerCenter = RegisterCenter.singleton();
        // 初始化服务提供者列表到本地缓存
        registerCenter.initProviderMap(remoteAppKey, groupName);
        // 初始化netty channel
        Map<String, List<ProviderService>> providerMap = registerCenter.getServiceMetaDataMap4Consume();
        if (CollectionUtils.isEmpty(providerMap)) {
            throw new RuntimeException("service provider list is empty");
        }
        NettyChannelPoolFactory.channelPoolFactoryInstance().initChannelPoolFactory(providerMap);

        // 获取服务提供者代理对象
        RevokerProxyBeanFactory proxyFactory = RevokerProxyBeanFactory.singleton(targetInterface, timeout, loadbalance);
        this.serviceObject = proxyFactory.getProxy();

        // 将消费者信息注册到注册中心
        InvokerService invoker = new InvokerService();
        invoker.setServiceItf(targetInterface);
        invoker.setRemoteAppKey(remoteAppKey);
        invoker.setGroupName(groupName);
        registerCenter.registerInvoker(invoker);
    }
}
