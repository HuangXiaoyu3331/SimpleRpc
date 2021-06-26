package com.huangxy.rpc.server.service.impl;

import com.huangxy.rpc.api.bean.Person;
import com.huangxy.rpc.server.service.HelloService;

/**
 * @author huangxy
 * @date 2021/06/16 00:51:43
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello " + name + "!";
    }

    @Override
    public Person getPerson(String name) {
        Person person = new Person();
        person.setName(name);
        person.setAge(18);
        return person;
    }
}
