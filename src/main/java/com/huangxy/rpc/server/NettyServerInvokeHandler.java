package com.huangxy.rpc.server;

import com.huangxy.rpc.api.bean.NetModel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author huangxy
 * @date 2021/06/16 21:18:41
 */
public class NettyServerInvokeHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        NetModel netModel = (NetModel) msg;
        System.out.println(((NetModel) msg).getClassName());
    }

}
