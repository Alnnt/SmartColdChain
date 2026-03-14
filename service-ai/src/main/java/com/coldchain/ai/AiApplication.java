package com.coldchain.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * AI鏈嶅姟鍚姩绫?
 * 鎻愪緵LLM闆嗘垚銆佹櫤鑳藉姪鎵嬨€侀闄╁垎鏋愮瓑AI鑳藉姏
 *
 * @author Alnnt
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
