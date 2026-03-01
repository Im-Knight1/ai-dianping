package com.zndp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.zndp.mapper")
@SpringBootApplication
public class ZnDianPingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZnDianPingApplication.class, args);
    }

}
