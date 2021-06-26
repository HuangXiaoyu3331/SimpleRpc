package com.huangxy.rpc.api.bean;

import java.io.Serializable;

/**
 * @author huangxy
 * @date 2021/06/16 00:37:36
 */
public class Person implements Serializable {

    private static final long serialVersionUID = -8688919168645973956L;
    private String name;
    private Integer age;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
