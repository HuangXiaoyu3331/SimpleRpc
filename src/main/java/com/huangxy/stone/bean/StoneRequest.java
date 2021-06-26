package com.huangxy.stone.bean;

import java.io.Serializable;

/**
 * @author huangxy
 * @date 2021/06/18 14:19:43
 */
public class StoneRequest implements Serializable {

    private static final long serialVersionUID = -8986548625993769125L;

    private String uniqueKey;
    private ProviderService providerService;
    private String invokeMethodName;
    private Object[] args;
    private long invokeTimeout;

    public String getUniqueKey() {
        return uniqueKey;
    }

    public long getInvokeTimeout() {
        return invokeTimeout;
    }

    public void setInvokeTimeout(long invokeTimeout) {
        this.invokeTimeout = invokeTimeout;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public ProviderService getProviderService() {
        return providerService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public String getInvokeMethodName() {
        return invokeMethodName;
    }

    public void setInvokeMethodName(String invokeMethodName) {
        this.invokeMethodName = invokeMethodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
