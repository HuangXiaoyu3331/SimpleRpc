package com.huangxy.rpc.api.util;

import java.io.*;

/**
 * @author huangxy
 * @date 2021/06/16 00:38:34
 */
public class SerializeUtil {

    /**
     * 序列化
     *
     * @param object 序列化对象
     * @return 序列化后的字节数组
     */
    public static byte[] serialize(Object object) {
        try (
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(os)
        ) {
            outputStream.writeObject(object);
            outputStream.flush();
            return os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to serialize!");
    }

    /**
     * 反序列化
     *
     * @param bytes 需要反序列化的字节数组
     * @return 反序列化后的对象x
     */
    public static Object deserialize(byte[] bytes) {
        try (
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                ObjectInputStream inputStream = new ObjectInputStream(is)
        ) {
            return inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new RuntimeException("Fail to deserialize!");
    }

}
