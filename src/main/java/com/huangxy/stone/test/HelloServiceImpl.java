package com.huangxy.stone.test;

/**
 * @author huangxy
 * @date 2021/06/19 00:45:26
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hello " + name + "!";
    }

}
