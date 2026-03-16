package com.coldchain.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 *
 * @author Alnnt
 */
@SpringBootApplication(scanBasePackages = { "com.coldchain.order", "com.coldchain.common" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.coldchain.order.feign")
@MapperScan("com.coldchain.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("========================================");
        System.out.println("    订单服务 (Service Order) 启动成功!   ");
        System.out.println("========================================");
    }
}
