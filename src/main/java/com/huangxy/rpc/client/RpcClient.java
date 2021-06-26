package com.huangxy.rpc.client;

import com.huangxy.rpc.api.util.SerializeUtil;
import com.huangxy.rpc.server.service.HelloService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author huangxy
 * @date 2021/06/16 01:29:03
 */
public class RpcClient {

    public static final int SERVER_PORT = 8091;

    public static void main(String[] args) {
        HelloService helloService = ProxyFactory.getInstance(HelloService.class);
        System.out.println(helloService.sayHello("huangxy"));
        System.out.println(helloService.getPerson("huangxy"));
    }

    public static Object send(byte[] bytes) {
        try (
                Socket socket = new Socket("127.0.0.1", SERVER_PORT);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream()
        ) {
            out.write(bytes);

            // 制作简单演示，这里使用 1m 的缓存是足够的
            byte[] buf = new byte[1024];
            in.read(buf);

            return SerializeUtil.deserialize(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to send data!");
    }

}
