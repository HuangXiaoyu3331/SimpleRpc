package com.huangxy.stone;

import com.huangxy.stone.test.HelloService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author huangxy
 * @date 2021/06/19 22:00:15
 */
public class MainClient {

    public static void main(String[] args) throws Exception {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("stone-client.xml");
        final HelloService helloService = (HelloService) context.getBean("helloService");

        String result = helloService.sayHello("huangxy");
        System.out.println(result);

    }

}
