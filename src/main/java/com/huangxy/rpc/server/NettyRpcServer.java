package com.huangxy.rpc.server;

import com.huangxy.rpc.serialize.NettyDecoderHandler;
import com.huangxy.rpc.serialize.NettyEncoderHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author huangxy
 * @date 2021/06/16 21:08:45
 */
public class NettyRpcServer {

    public static final int PORT = 8091;
    public static final String CONFIG_FILE_NAME = "config.properties";
    private static Properties properties;
    private static NioEventLoopGroup bossGroup;
    private static NioEventLoopGroup workerGroup;
    private static Channel channel;

    static {
        properties = new Properties();
        try (InputStream in = RpcServer.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        start(PORT);
    }

    private static void start(final int port) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        //注册解码器NettyDecoderHandler
                        ch.pipeline().addLast(new NettyDecoderHandler());
                        //注册编码器NettyEncoderHandler
                        ch.pipeline().addLast(new NettyEncoderHandler());
                        //注册服务端业务逻辑处理器NettyServerInvokeHandler
                        ch.pipeline().addLast(new NettyServerInvokeHandler());
                    }
                });
        try {
            channel = bootstrap.bind(port).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
