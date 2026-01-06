package com.coldchain.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * AI服务启动类
 * 提供LLM集成、智能助手、风险分析等AI能力
 *
 * @author coldchain
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
