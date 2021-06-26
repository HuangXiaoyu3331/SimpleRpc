package com.huangxy.rpc.client;

import com.huangxy.rpc.api.bean.NetModel;
import com.huangxy.rpc.api.util.SerializeUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author huangxy
 * @date 2021/06/16 01:29:22
 */
public class ProxyFactory {

    public static <T> T getInstance(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, handler);
    }

    private static InvocationHandler handler = (proxy, method, args) -> {
        Class<?>[] classes = proxy.getClass().getInterfaces();
        String className = classes[0].getName();

        NetModel netModel = new NetModel();
        netModel.setClassName(className);
        netModel.setMethod(method.getName());
        netModel.setArgs(args);
        if (args != null) {
            String[] types = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass().getName();
            }
            netModel.setTypes(types);
        }
        byte[] bytes = SerializeUtil.serialize(netModel);
        return RpcClient.send(bytes);
    };

}
