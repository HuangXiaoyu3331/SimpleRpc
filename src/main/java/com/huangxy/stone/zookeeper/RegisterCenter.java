package com.huangxy.stone.zookeeper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.framwork.InvokerService;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author huangxy
 * @date 2021/06/18 15:42:07
 */
public class RegisterCenter implements IRegisterCenter4Provider, IRegisterCenter4Invoker {

    private static final String INVOKER_TYPE = "consumer";
    private static RegisterCenter registerCenter = new RegisterCenter();
    //服务提供者列表,Key:服务提供者接口  value:服务提供者服务方法列表
    private static final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();
    //服务端ZK服务元信息,选择服务(第一次直接从ZK拉取,后续由ZK的监听机制主动更新)
    private static final Map<String, List<ProviderService>> serviceMetaDataMap4Consume = Maps.newConcurrentMap();

    private static volatile ZkClient zkClient = null;
    private static String ROOT_PATH = "/stone";
    private static String PROVIDER_TYPE = "provider";

    public RegisterCenter() {
    }

    public static RegisterCenter singleton() {
        return registerCenter;
    }

    @Override
    public void registerProvider(List<ProviderService> serviceMetaData) {
        if (CollectionUtils.isEmpty(serviceMetaData)) {
            return;
        }

        synchronized (RegisterCenter.class) {
            for (ProviderService provider : serviceMetaData) {
                String serviceItfKey = provider.getServiceItf().getName();

                List<ProviderService> providers = providerServiceMap.get(serviceItfKey);
                if (providers == null) {
                    providers = new ArrayList<>();
                }
                providers.add(provider);
                providerServiceMap.put(serviceItfKey, providers);
            }

            if (zkClient == null) {
                zkClient = new ZkClient("localhost", 1000, 1000, new SerializableSerializer());
            }

            String APP_KEY = serviceMetaData.get(0).getAppKey();
            String ZK_PATH = ROOT_PATH + "/" + APP_KEY;

            if (!zkClient.exists(ZK_PATH)) {
                zkClient.createPersistent(ZK_PATH, true);
            }

            providerServiceMap.forEach((serviceItf, providerList) -> {
                // 服务分组
                String groupName = providerList.get(0).getGroupName();
                // 创建服务提供者
                String servicePath = ZK_PATH + "/" + groupName + "/" + serviceItf + "/" + PROVIDER_TYPE;
                if (!zkClient.exists(servicePath)) {
                    zkClient.createPersistent(servicePath, true);
                }

                int serverPort = providerList.get(0).getServerPort();
                int weight = providerList.get(0).getWeight();
                int workerThreads = providerList.get(0).getWorkerThreads();
                String localIp = "127.0.0.1";
                String currentServiceIpNode = servicePath + "/" + localIp + "|" + serverPort + "|" + weight + "|" + workerThreads + "|" + groupName;
                if (!zkClient.exists(currentServiceIpNode)) {
                    zkClient.createEphemeral(currentServiceIpNode);
                }

                // 监听注册服务的变化，同时更新本地缓存
                zkClient.subscribeChildChanges(servicePath, (parentPath, currentChildes) -> {
                    if (currentChildes == null) {
                        currentChildes = new ArrayList<>();
                    }

                    List<String> activityServiceIpList = currentChildes.stream().map(input -> StringUtils.split(input, "|")[0])
                            .collect(Collectors.toList());
                    refreshActivityService(activityServiceIpList);
                });
            });
        }
    }

    // 刷新当前存活的服务提供者列表
    private void refreshActivityService(List<String> serviceIpList) {
        serviceIpList = serviceIpList == null ? new ArrayList<>() : serviceIpList;
        Map<String, List<ProviderService>> currentServiceMetaDataMap = Maps.newConcurrentMap();
        for (Map.Entry<String, List<ProviderService>> entry : serviceMetaDataMap4Consume.entrySet()) {
            String serviceItfKey = entry.getKey();
            List<ProviderService> serviceList = entry.getValue();

            List<ProviderService> providerServiceList = currentServiceMetaDataMap.get(serviceItfKey);
            if (providerServiceList == null) {
                providerServiceList = new ArrayList<>();
            }

            for (ProviderService providerService : serviceList) {
                if (serviceIpList.contains(providerService.getServerIp())) {
                    providerServiceList.add(providerService);
                }
            }
            currentServiceMetaDataMap.put(serviceItfKey, providerServiceList);
        }
        serviceMetaDataMap4Consume.clear();
        serviceMetaDataMap4Consume.putAll(currentServiceMetaDataMap);
    }

    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return providerServiceMap;
    }

    @Override
    public void initProviderMap(String remoteAppKey, String groupName) {
        // 这里使用的是ConcurrentMap，没有并发问题，不用同步
        if (CollectionUtils.isEmpty(serviceMetaDataMap4Consume)) {
            serviceMetaDataMap4Consume.putAll(fetchOrUpdateServiceMetaData(remoteAppKey, groupName));
        }
    }

    @Override
    public Map<String, List<ProviderService>> getServiceMetaDataMap4Consume() {
        return serviceMetaDataMap4Consume;
    }

    @Override
    public void registerInvoker(InvokerService invoker) {
        if (invoker == null) {
            return;
        }
        synchronized (RegisterCenter.class) {
            if (zkClient == null) {
                zkClient = new ZkClient("127.0.0.1", 1000, 1000, new SerializableSerializer());
            }
            // 创建znode -> /zk命名空间/当前部署应用APP命名空间
            if (!zkClient.exists(ROOT_PATH)) {
                zkClient.createPersistent(ROOT_PATH, true);
            }

            // 创建服务消费者节点
            String remoteAppKey = invoker.getRemoteAppKey();
            String groupName = invoker.getGroupName();
            String serviceNode = invoker.getServiceItf().getName();

            String servicePath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName + "/" + serviceNode + "/" + INVOKER_TYPE;
            if (!zkClient.exists(servicePath)) {
                zkClient.createPersistent(servicePath, true);
            }

            // 创建当前服务器节点
            String localIp = "127.0.0.1";
            String currentServiceIpNode = servicePath + "/" + localIp;
            if (!zkClient.exists(currentServiceIpNode)) {
                zkClient.createEphemeral(currentServiceIpNode);
            }
        }
    }

    private Map<? extends String, ? extends List<ProviderService>> fetchOrUpdateServiceMetaData(String remoteAppKey, String groupName) {
        final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();

        // 连接zk
        synchronized (RegisterCenter.class) {
            if (zkClient == null) {
                zkClient = new ZkClient("127.0.0.1", 1000, 1000, new SerializableSerializer());
            }

            // 从zk获取服务提供者列表
            String providerPath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName;
            List<String> providerServices = zkClient.getChildren(providerPath);

            providerServices.forEach(serviceName -> {
                String servicePath = providerPath + "/" + serviceName + "/" + PROVIDER_TYPE;
                List<String> ipPathList = zkClient.getChildren(servicePath);

                ipPathList.forEach(ipPath -> {
                    String serverIp = StringUtils.split(ipPath, "|")[0];
                    String serverPort = StringUtils.split(ipPath, "|")[1];
                    int weight = Integer.parseInt(StringUtils.split(ipPath, "|")[2]);
                    int workerThreads = Integer.parseInt(StringUtils.split(ipPath, "|")[3]);
                    String group = StringUtils.split(ipPath, "|")[4];

                    List<ProviderService> providerServiceList = providerServiceMap.get(serviceName);
                    if (providerServiceList == null) {
                        providerServiceList = new ArrayList<>();
                    }

                    ProviderService providerService = new ProviderService();
                    try {
                        providerService.setServiceItf(ClassUtils.getClass(serviceName));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    providerService.setServerIp(serverIp);
                    providerService.setServerPort(Integer.parseInt(serverPort));
                    providerService.setWeight(weight);
                    providerService.setWorkerThreads(workerThreads);
                    providerService.setGroupName(group);
                    providerServiceList.add(providerService);

                    providerServiceMap.put(serviceName, providerServiceList);
                });
                //监听注册服务的变化,同时更新数据到本地缓存
                zkClient.subscribeChildChanges(servicePath, (parentPath, currentChildes) -> {
                    if (currentChildes == null) {
                        currentChildes = Lists.newArrayList();
                    }
                    currentChildes = Lists.newArrayList(Lists.transform(currentChildes, input -> StringUtils.split(input, "|")[0]));
                    refreshServiceMetaDataMap(currentChildes);
                });
            });
            return providerServiceMap;
        }
    }

    private void refreshServiceMetaDataMap(List<String> serviceIpList) {
        if (serviceIpList == null) {
            serviceIpList = Lists.newArrayList();
        }

        Map<String, List<ProviderService>> currentServiceMetaDataMap = Maps.newHashMap();
        for (Map.Entry<String, List<ProviderService>> entry : serviceMetaDataMap4Consume.entrySet()) {
            String serviceItfKey = entry.getKey();
            List<ProviderService> serviceList = entry.getValue();

            List<ProviderService> providerServiceList = currentServiceMetaDataMap.get(serviceItfKey);
            if (providerServiceList == null) {
                providerServiceList = Lists.newArrayList();
            }

            for (ProviderService serviceMetaData : serviceList) {
                if (serviceIpList.contains(serviceMetaData.getServerIp())) {
                    providerServiceList.add(serviceMetaData);
                }
            }
            currentServiceMetaDataMap.put(serviceItfKey, providerServiceList);
        }

        serviceMetaDataMap4Consume.clear();
        serviceMetaDataMap4Consume.putAll(currentServiceMetaDataMap);
    }
}
