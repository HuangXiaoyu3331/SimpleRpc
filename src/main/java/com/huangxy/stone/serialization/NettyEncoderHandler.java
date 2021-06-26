package com.huangxy.stone.serialization;

import com.huangxy.stone.bean.StoneRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author huangxy
 * @date 2021/06/18 14:27:19
 */
public class NettyEncoderHandler extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 将消息序列化为字节数组，用于网络传输
        byte[] data = JDKSerializeUtil.serialize(msg);
        // 将消息长度作为消息头写入，解决半包/粘包问题
        out.writeInt(data.length);
        // 写入序列化后的字节数组
        out.writeBytes(data);
    }

}
