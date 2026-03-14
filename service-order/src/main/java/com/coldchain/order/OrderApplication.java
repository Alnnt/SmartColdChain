package com.coldchain.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 璁㈠崟鏈嶅姟鍚姩绫?
 *
 * @author Alnnt
 */
@SpringBootApplication(scanBasePackages = { "com.coldchain.order", "com.coldchain.common" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.coldchain.order.feign")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("========================================");
        System.out.println("    璁㈠崟鏈嶅姟 (Service Order) 鍚姩鎴愬姛!   ");
        System.out.println("========================================");
    }
}
