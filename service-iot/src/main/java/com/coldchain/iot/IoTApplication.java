package com.coldchain.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IoT 服务启动类
 * 
 * 启动后会同时运行：
 * 1. Spring Boot Web 服务（端口 8084）
 * 2. Netty TCP 服务器（端口 9000）
 *
 * @author Alnnt
 */
@SpringBootApplication(scanBasePackages = { "com.coldchain.iot", "com.coldchain.common" })
@EnableDiscoveryClient
public class IoTApplication {

    public static void main(String[] args) {
        SpringApplication.run(IoTApplication.class, args);
        System.out.println("========================================");
        System.out.println("     IoT服务 (Service IoT) 启动成功!     ");
        System.out.println("     HTTP端口: 8084                      ");
        System.out.println("     TCP端口: 9000                       ");
        System.out.println("========================================");
    }
}
