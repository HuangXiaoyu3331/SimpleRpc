package com.huangxy.stone.bean;

import java.io.Serializable;

/**
 * @author huangxy
 * @date 2021/06/19 18:16:04
 */
public class StoneResponse implements Serializable {

    private static final long serialVersionUID = 6758989256192343959L;
    // UUID,返回结果唯一标识
    private String uniqueKey;
    // 客户端指定的服务器超时时间
    private long invokeTimeout;
    // 接口返回结果对象
    private Object result;


    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public long getInvokeTimeout() {
        return invokeTimeout;
    }

    public void setInvokeTimeout(long invokeTimeout) {
        this.invokeTimeout = invokeTimeout;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
