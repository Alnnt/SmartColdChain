package com.coldchain.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 缃戝叧鏈嶅姟鍚姩绫?
 *
 * @author Alnnt
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  Cold Chain Gateway Started Successfully  ");
        System.out.println("  Port: 8080                               ");
        System.out.println("===========================================");
    }
}
