package com.huangxy.stone.framwork;

import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.bean.StoneRequest;
import com.huangxy.stone.bean.StoneResponse;
import com.huangxy.stone.loadbalance.LoadbalanceEngine;
import com.huangxy.stone.loadbalance.LoadbalanceStrategy;
import com.huangxy.stone.revoker.RevokerServiceCallable;
import com.huangxy.stone.zookeeper.IRegisterCenter4Invoker;
import com.huangxy.stone.zookeeper.RegisterCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * netty 消费端bean代理工厂
 *
 * @author huangxy
 * @date 2021/06/19 17:07:26
 */
public class RevokerProxyBeanFactory implements InvocationHandler {

    private static volatile RevokerProxyBeanFactory singleton;
    private Class<?> targetInterface;
    private long consumeTimeout;
    private String loadbalance;
    private ExecutorService fixedThreadPool = null;
    private static final int THREAD_WORKER_NUMBER = 10;

    private RevokerProxyBeanFactory(Class<?> targetInterface, long consumeTimeout, String loadbalance) {
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.loadbalance = loadbalance;
    }

    public static RevokerProxyBeanFactory singleton(Class<?> targetInterface, int consumeTimeout, String loadbalance) {
        if (singleton == null) {
            synchronized (RevokerProxyBeanFactory.class) {
                if (singleton == null) {
                    singleton = new RevokerProxyBeanFactory(targetInterface, consumeTimeout, loadbalance);
                }
            }
        }
        return singleton;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 服务接口名称
        String serviceKey = targetInterface.getName();
        // 获取某个接口的服务提供者列表
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        List<ProviderService> providerServiceList = registerCenter4Consumer.getServiceMetaDataMap4Consume().get(serviceKey);
        // 根据软负载策略，从服务提供者列表获取本次调用的服务提供者
        LoadbalanceStrategy loadbalanceStrategy = LoadbalanceEngine.queryClusterStrategy(loadbalance);
        ProviderService providerService = loadbalanceStrategy.select(providerServiceList);
        // 复制一份服务提供者信息
        ProviderService newProvider = providerService.copy();
        // 设置本次调用服务的方法及接口
        newProvider.setServiceItf(targetInterface);
        newProvider.setServiceMethod(method);

        // 组装StoneRequest对象
        final StoneRequest request = new StoneRequest();
        // 设置本次调用的唯一标识
        request.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
        // 设置本次调用的服务提供者信息
        request.setProviderService(newProvider);
        // 设置本次调用的超时时间
        request.setInvokeTimeout(consumeTimeout);
        // 设置本次调用方法名称
        request.setInvokeMethodName(method.getName());
        // 设置本次调用的方法参数
        request.setArgs(args);

        try {
            // 构建用来发起调用的线程池
            if (fixedThreadPool == null) {
                synchronized (RevokerProxyBeanFactory.class) {
                    if (fixedThreadPool == null) {
                        fixedThreadPool = Executors.newFixedThreadPool(THREAD_WORKER_NUMBER);
                    }
                }
            }

            // 根据服务提供者的ip，port，构建InetSocketAddress对象，标识服务提供者地址
            String serverIp = request.getProviderService().getServerIp();
            int serverPort = request.getProviderService().getServerPort();
            InetSocketAddress socketAddress = new InetSocketAddress(serverIp, serverPort);
            // 提交本次调用信息到线程池，发起调用
            Future<StoneResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(socketAddress, request));
            StoneResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            if (response != null) {
                return response.getResult();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this);
    }
}
