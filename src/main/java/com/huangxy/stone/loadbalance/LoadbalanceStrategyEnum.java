package com.huangxy.stone.loadbalance;

/**
 * @author huangxy
 * @date 2021/06/23 14:57:14
 */
public enum LoadbalanceStrategyEnum {

    Random("Random"),
    Roundrobin("Roundrobin"),
    ;

    private String code;

    public static LoadbalanceStrategyEnum getByCode(String code) {
        for (LoadbalanceStrategyEnum strategy : LoadbalanceStrategyEnum.values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    LoadbalanceStrategyEnum(String code) {
        this.code = code;
    }
}
