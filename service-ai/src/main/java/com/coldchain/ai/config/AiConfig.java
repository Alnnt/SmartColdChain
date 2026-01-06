package com.coldchain.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 *
 * @author coldchain
 */
@Configuration
public class AiConfig {

    /**
     * 配置ChatClient Builder
     * Spring AI会自动注入配置好的ChatModel
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(org.springframework.ai.chat.model.ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }



}
