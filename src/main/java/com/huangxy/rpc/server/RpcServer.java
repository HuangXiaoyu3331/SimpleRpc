package com.huangxy.rpc.server;

import com.huangxy.rpc.api.bean.NetModel;
import com.huangxy.rpc.api.util.SerializeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * Rpc 服务端
 * 服务端开启端口监听客户端请求，对数据进行处理，将处理结果返回给客户端
 *
 * @author huangxy
 * @date 2021/06/16 00:53:07
 */
public class RpcServer {

    public static final int PORT = 8091;
    public static final String CONFIG_FILE_NAME = "config.properties";
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream in = RpcServer.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        openServer();
    }

    /**
     * 启动服务端Socket，接受客户端数据，并返回处理结果
     */
    private static void openServer() {
        Socket socket = null;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Service starting... listen on port " + PORT);

            while (true) {
                socket = serverSocket.accept();
                System.out.println(socket.getInetAddress() + " connected!");

                InputStream in = socket.getInputStream();
                // 制作简单演示，这里使用 1m 的缓存是足够的
                byte[] buf = new byte[1024];
                in.read(buf);

                byte[] result = invoke(buf);
                OutputStream out = socket.getOutputStream();
                out.write(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param buf
     * @return
     */
    private static byte[] invoke(byte[] buf) {
        try {

            NetModel netModel = (NetModel) SerializeUtil.deserialize(buf);
            String className = netModel.getClassName();
            String methodName = netModel.getMethod();
            Object[] args = netModel.getArgs();
            String[] types = netModel.getTypes();

            Class<?> clazz = Class.forName( getPropertyValue(className));
            Class<?>[] typeClazzs = null;
            if (types != null) {
                typeClazzs = new Class[types.length];
                for (int i = 0; i < types.length; i++) {
                    typeClazzs[i] = Class.forName(types[i]);
                }
            }

            Method method = clazz.getMethod(methodName, typeClazzs);
            Object result = method.invoke(clazz.newInstance(), args);
            return SerializeUtil.serialize(result);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to invoke");
    }

    private static String getPropertyValue(String key) {
        return properties.getProperty(key);
    }

}
