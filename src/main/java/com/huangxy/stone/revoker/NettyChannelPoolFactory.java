package com.huangxy.stone.revoker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huangxy.stone.bean.ProviderService;
import com.huangxy.stone.serialization.NettyDecoderHandler;
import com.huangxy.stone.serialization.NettyEncoderHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author huangxy
 * @date 2021/06/19 16:34:03
 */
public class NettyChannelPoolFactory {

    private static final NettyChannelPoolFactory nettyChannelPoolFactory = new NettyChannelPoolFactory();
    private static final int MAX_CHANNEL_CONNECT_SIZE = 15;
    //服务提供者列表
    private List<ProviderService> serviceMetaDataList = Lists.newArrayList();
    //Key为服务提供者地址,value为Netty Channel阻塞队列
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> channelPoolMap = Maps.newConcurrentMap();

    private NettyChannelPoolFactory() {
    }

    public static NettyChannelPoolFactory channelPoolFactoryInstance() {
        return nettyChannelPoolFactory;
    }

    public void initChannelPoolFactory(Map<String, List<ProviderService>> providerMap) {
        Collection<List<ProviderService>> collectionServiceMetaDataList = providerMap.values();
        collectionServiceMetaDataList.forEach(serviceMetaDataModels -> {
            if (CollectionUtils.isEmpty(serviceMetaDataModels)) {
                return;
            }
            serviceMetaDataList.addAll(serviceMetaDataModels);
        });

        // 获取服务提供者地址列表
        Set<InetSocketAddress> socketAddressSet = Sets.newHashSet();
        serviceMetaDataList.forEach(serviceMetaData -> {
            String serviceIp = serviceMetaData.getServerIp();
            int servicePort = serviceMetaData.getServerPort();
            InetSocketAddress socketAddress = new InetSocketAddress(serviceIp, servicePort);
            socketAddressSet.add(socketAddress);
        });

        socketAddressSet.forEach(socketAddress -> {
            try {
                int realChannelConnectSize = 0;
                while (realChannelConnectSize < MAX_CHANNEL_CONNECT_SIZE) {
                    Channel channel = null;
                    // 若 channel 建立失败，则注册新的netty channel
                    while (channel == null) {
                        channel = registerChannel(socketAddress);
                    }
                    // 计数器，初始化的时候存入阻塞队列的netty channel 个数不得超过MAX_CHANNEL_CONNECT_SIZE
                    realChannelConnectSize++;

                    // 将新创建的netty channel存入阻塞队列channelArrayBlockingQueue
                    // 并将阻塞队列channelArrayBlockingQueue作为value存入channelPoolMap
                    ArrayBlockingQueue<Channel> channelArrayBlockingQueue = channelPoolMap.get(socketAddress);
                    if (channelArrayBlockingQueue == null) {
                        channelArrayBlockingQueue = new ArrayBlockingQueue<>(MAX_CHANNEL_CONNECT_SIZE);
                        channelPoolMap.put(socketAddress, channelArrayBlockingQueue);
                    }
                    channelArrayBlockingQueue.offer(channel);
                }
            } catch (Exception e) {

            }
        });

    }

    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {

            NioEventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyEncoderHandler());
                            ch.pipeline().addLast(new NettyDecoderHandler());
                            ch.pipeline().addLast(new NettyClientInvokeHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel newChannel = channelFuture.channel();
            final CountDownLatch connectedLatch = new CountDownLatch(1);
            final List<Boolean> isSuccessHolder = Lists.newArrayListWithCapacity(1);

            // 监听channel是否建立成功
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    isSuccessHolder.add(Boolean.TRUE);
                } else {
                    // channel 建立失败，保存建立失败的标记
                    future.cause().printStackTrace();
                    isSuccessHolder.add(Boolean.FALSE);
                }
                connectedLatch.countDown();
            });
            connectedLatch.await();
            // 如果channel建立成功，返回新的channel
            if (isSuccessHolder.get(0)) {
                return newChannel;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        return channelPoolMap.get(socketAddress);
    }

    /**
     * channel 使用完之后会收到阻塞队列
     *
     * @param arrayBlockingQueue
     * @param channel
     * @param inetSocketAddress
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue, Channel channel, InetSocketAddress inetSocketAddress) {
        if (arrayBlockingQueue == null) {
            return;
        }
        //回收之前先检查channel是否可用,不可用的话,重新注册一个,放入阻塞队列
        if (channel == null || !channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
            if (channel != null) {
                // todo Netty5 更新了该方法
//                channel.deregister().syncUninterruptibly().awaitUninterruptibly();

                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }
            Channel newChannel = null;
            while (newChannel == null) {
                newChannel = registerChannel(inetSocketAddress);
            }
            arrayBlockingQueue.offer(newChannel);
            return;
        }
        arrayBlockingQueue.offer(channel);
    }
}
