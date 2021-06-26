package com.huangxy.rpc.server.service;

import com.huangxy.rpc.api.bean.Person;

/**
 * hello 服务接口
 *
 * @author huangxy
 * @date 2021/06/16 00:50:15
 */
public interface HelloService {

    String sayHello(String name);

    Person getPerson(String name);

}
