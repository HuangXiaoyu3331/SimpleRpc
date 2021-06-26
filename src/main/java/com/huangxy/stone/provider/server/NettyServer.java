package com.huangxy.stone.provider.server;

import com.huangxy.stone.serialization.NettyDecoderHandler;
import com.huangxy.stone.serialization.NettyEncoderHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author huangxy
 * @date 2021/06/18 13:52:38
 */
public class NettyServer {

    private static NettyServer nettyServer = new NettyServer();
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private Channel channel;

    private NettyServer() {
    }

    public static NettyServer singleton() {
        return nettyServer;
    }

    public void start(final int port) {
        synchronized (NettyServer.class) {
            if (bossGroup != null && workerGroup != null) {
                return;
            }

            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024) // 设置 accept queue 的长度为 1024
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 启用 keep alive
                    .childOption(ChannelOption.TCP_NODELAY, true) // rpc 调用对时延敏感，需要禁用Nagle算法
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //注册解码器NettyDecoderHandler
                            ch.pipeline().addLast(new NettyDecoderHandler());
                            //注册编码器NettyEncoderHandler
                            ch.pipeline().addLast(new NettyEncoderHandler());
                            //注册服务端业务逻辑处理器NettyServerInvokeHandler
                            ch.pipeline().addLast(new NettyServerInvokeHandler());
                        }
                    });
            try {
                channel = serverBootstrap.bind(port).sync().channel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (null == channel) {
            throw new RuntimeException("Netty Server Stoped");
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        // 关闭 channel，不可中断
        channel.closeFuture().syncUninterruptibly();
    }

}
