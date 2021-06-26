package com.huangxy.stone.framwork;

import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.provider.server.NettyServer;
import com.huangxy.stone.zookeeper.IRegisterCenter4Provider;
import com.huangxy.stone.zookeeper.RegisterCenter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangxy
 * @date 2021/06/18 13:35:33
 */
public class ProviderFactoryBean implements FactoryBean<Object>, InitializingBean {

    public static final int SERVER_PORT = 8091;
    // 服务接口
    private Class<?> serviceItf;
    // 服务实现类
    private Object serviceObject;


    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceItf;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动 netty 服务
        NettyServer.singleton().start(SERVER_PORT);
        // 注册服务到zk，元数据注册中心
        List<ProviderService> providerServiceList = buildProviderServiceInfos();
        IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
        registerCenter4Provider.registerProvider(providerServiceList);
    }

    private List<ProviderService> buildProviderServiceInfos() {
        List<ProviderService> providerList = new ArrayList<>();
        Method[] methods = serviceObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            ProviderService providerService = new ProviderService();
            providerService.setServiceItf(serviceItf);
            providerService.setServiceObject(serviceObject);
            providerService.setServerIp("127.0.0.1");
            providerService.setServerPort(SERVER_PORT);
            providerService.setTimeout(600);
            providerService.setServiceMethod(method);
            providerService.setWeight(2);
            providerService.setWorkerThreads(10);
            providerService.setAppKey("ares");
            providerService.setGroupName("default");
            providerList.add(providerService);
        }
        return providerList;
    }

    public Class<?> getServiceItf() {
        return serviceItf;
    }

    public void setServiceItf(Class<?> serviceItf) {
        this.serviceItf = serviceItf;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }
}
