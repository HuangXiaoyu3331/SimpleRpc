package com.huangxy.rpc.serialize;

import com.huangxy.rpc.api.bean.NetModel;
import com.huangxy.rpc.api.util.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author huangxy
 * @date 2021/06/16 21:16:28
 */
public class NettyDecoderHandler extends ByteToMessageDecoder {

    private static final Integer HEADER_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("NettyDecoderHandler");
        // 可读字节数 < 4，证明消息头不完整
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }
        in.markReaderIndex();

        // 获取消息头所标识的字节数组长度
        int dataLength = in.readInt();

        // 若当前可读字节长度 < 消息头标识的字节数组长度，则直接返回，直到当前可以获取到的字节数等于实际长度
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        // 读取完整的字节数组
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 反序列化
        Object obj = SerializeUtil.deserialize(data);
        out.add(obj);
    }

}
