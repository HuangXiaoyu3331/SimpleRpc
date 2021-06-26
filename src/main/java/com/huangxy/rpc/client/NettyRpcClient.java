package com.huangxy.rpc.client;

import com.huangxy.rpc.serialize.NettyDecoderHandler;
import com.huangxy.rpc.serialize.NettyEncoderHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author huangxy
 * @date 2021/06/17 00:38:51
 */
public class NettyRpcClient {

    public static void main(String[] args) {
        // 配置NIO客户端线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new NettyDecoderHandler());
                            ch.pipeline().addLast(new NettyEncoderHandler());
                            ch.pipeline().addLast(new NettyClientInvokerHandler());
                        }
                    });

            // 发起异步连接操作
            ChannelFuture f = bootstrap.connect("127.0.0.1", 8091).sync();

            // 当代客户端链路关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

}
