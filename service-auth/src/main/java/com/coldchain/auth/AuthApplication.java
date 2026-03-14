package com.coldchain.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证授权服务启动类
 *
 * @author Alnnt
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.coldchain.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("===========================================");
        System.out.println("   Cold Chain Auth Started Successfully    ");
        System.out.println("   Port: 8071                              ");
        System.out.println("===========================================");
    }
}
