package com.huangxy.rpc.client;

import com.huangxy.rpc.api.bean.NetModel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerInvoker;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author huangxy
 * @date 2021/06/17 00:42:13
 */
public class NettyClientInvokerHandler extends ChannelHandlerAdapter {

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        NetModel netModel = new NetModel();
//        netModel.setClassName("hello");
//        ctx.writeAndFlush(netModel);
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
    }
}
