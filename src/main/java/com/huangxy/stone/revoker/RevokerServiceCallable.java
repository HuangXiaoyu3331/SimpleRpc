package com.huangxy.stone.revoker;

import com.huangxy.stone.bean.StoneRequest;
import com.huangxy.stone.bean.StoneResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author huangxy
 * @date 2021/06/19 18:15:18
 */
public class RevokerServiceCallable implements Callable<StoneResponse> {

    private Channel channel;
    private StoneRequest request;
    private InetSocketAddress socketAddress;

    private RevokerServiceCallable(InetSocketAddress socketAddress, StoneRequest request) {
        this.socketAddress = socketAddress;
        this.request = request;
    }

    public static RevokerServiceCallable of(InetSocketAddress socketAddress, StoneRequest request) {
        return new RevokerServiceCallable(socketAddress, request);
    }

    @Override
    public StoneResponse call() throws Exception {
        //初始化返回结果容器,将本次调用的唯一标识作为Key存入返回结果的Map
        RevokerResponseHolder.initResponseData(request.getUniqueKey());

        // 获取对应的 netty channel 队列
        ArrayBlockingQueue<Channel> blockingQueue = NettyChannelPoolFactory.channelPoolFactoryInstance().acquire(socketAddress);
        try {
            if (channel == null) {
                // 从队列中获取本次调用的netty channel
                channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            }
            // todo 逻辑有待优化
            while (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                System.out.println("retry get new channel");
                channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
                if (channel == null) {
                    channel = NettyChannelPoolFactory.channelPoolFactoryInstance().registerChannel(socketAddress);
                }
            }

            // 将本次调用的信息写入netty channel，发起异步调用
            ChannelFuture channelFuture = channel.writeAndFlush(request);
            channelFuture.syncUninterruptibly();

            // 从返回结果容器中获取返回结果,同时设置等待超时时间为invokeTimeout
            return RevokerResponseHolder.getValue(request.getUniqueKey(), request.getInvokeTimeout());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            NettyChannelPoolFactory.channelPoolFactoryInstance().release(blockingQueue, channel, socketAddress);
        }
        return null;
    }
}
