package com.huangxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author huangxy
 * @date 2021/06/18 13:28:53
 */
@SpringBootApplication
@ImportResource("classpath:stone-server.xml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
