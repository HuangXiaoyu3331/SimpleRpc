package com.huangxy.rpc.api.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 网络通信模型类
 *
 * @author huangxy
 * @date 2021/06/16 00:32:07
 */
public class NetModel implements Serializable {

    private static final long serialVersionUID = -1265065806372195528L;
    private String className;
    private String method;
    // 方法参数数组
    private Object[] args;
    // 方法参数类型数组
    private String[] types;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    @Override
    public String toString() {
        return "NetModel{" +
                "className='" + className + '\'' +
                ", method='" + method + '\'' +
                ", args=" + Arrays.toString(args) +
                ", types=" + Arrays.toString(types) +
                '}';
    }
}
