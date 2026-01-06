package com.coldchain.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAgent {
    /**
     * 订单解析系统提示词
     */
    private static final String ORDER_SYSTEM_PROMPT = """
            You are a logistics assistant for a cold chain logistics system.
            Your task is to extract order details from natural language input.
            
            Extract the following information and return it in JSON format:
            - item: The name of the goods/product being shipped
            - quantity: The quantity of items (as a number)
            - unit: The unit of measurement (e.g., "箱", "吨", "件", "kg")
            - pickupAddress: The pickup/origin address
            - deliveryAddress: The delivery/destination address
            - specialRequirements: Any special requirements mentioned (e.g., temperature requirements, handling instructions)
            - estimatedWeight: Estimated weight if mentioned
            - contactName: Contact person name if mentioned
            - contactPhone: Contact phone number if mentioned
            
            If any field is not mentioned, set it to null.
            Always respond with valid JSON only, no additional text.
            """;


    @Bean
    public ReactAgent orderAgent(ChatModel chatModel,
                                        MemorySaver memorySaver) {
        return ReactAgent.builder()
                .name("order_agent")
                .model(chatModel)
                .instruction(ORDER_SYSTEM_PROMPT)
                .enableLogging(true)
                .saver(memorySaver)
                .build();
    }



    @Bean
    public MemorySaver memorySaver() {
        return new MemorySaver();
    }
}
