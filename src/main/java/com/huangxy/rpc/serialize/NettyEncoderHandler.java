package com.huangxy.rpc.serialize;

import com.huangxy.rpc.api.util.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author huangxy
 * @date 2021/06/16 21:16:52
 */
public class NettyEncoderHandler extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        System.out.println("NettyEncoderHandler");
        byte[] data = SerializeUtil.serialize(o);
        // 将字节数组的长度作为消息头写入，解决拆包/粘包问题
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
