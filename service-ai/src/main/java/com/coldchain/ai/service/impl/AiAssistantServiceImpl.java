package com.coldchain.ai.service.impl;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.coldchain.ai.model.OrderChatResponse;
import com.coldchain.ai.model.RiskAnalysisRequest;
import com.coldchain.ai.model.RiskAnalysisResponse;
import com.coldchain.ai.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * AI助手服务实现类
 * 使用Spring AI ChatClient与LLM交互
 *
 * @author coldchain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final ChatClient.Builder chatClientBuilder;

    private final ReactAgent orderAgent;

    /**
     * 风险分析系统提示词
     */
    private static final String RISK_ANALYSIS_SYSTEM_PROMPT = """
            You are a cold chain logistics risk analyst.
            Analyze the provided temperature data and transport statistics to generate a comprehensive risk report.
            
            Consider the following factors:
            1. Temperature deviations from the acceptable range
            2. Duration of temperature excursions
            3. Frequency of temperature fluctuations
            4. Overall trend (stable, increasing, decreasing)
            5. Potential impact on cargo quality
            
            Provide your analysis in the following JSON format:
            - riskLevel: Overall risk level ("LOW", "MEDIUM", "HIGH", "CRITICAL")
            - riskScore: Numeric risk score from 0-100
            - summary: Brief summary of the risk assessment
            - temperatureAnalysis: Analysis of temperature patterns
            - recommendations: Array of recommended actions
            - potentialIssues: Array of potential issues identified
            - estimatedCargoCondition: Estimated condition of cargo ("OPTIMAL", "ACCEPTABLE", "COMPROMISED", "DAMAGED")
            
            Always respond with valid JSON only.
            """;

    @Override
    public OrderChatResponse parseOrderFromNaturalLanguage(String naturalLanguageInput) {
        log.debug("开始解析订单, 输入: {}", naturalLanguageInput);

        BeanOutputConverter<OrderChatResponse> converter = 
                new BeanOutputConverter<>(OrderChatResponse.class);

//        ChatClient chatClient = chatClientBuilder.build();

//        String response = chatClient.prompt()
//                .system(ORDER_SYSTEM_PROMPT + "\n\n" + converter.getFormat())
//                .user(naturalLanguageInput)
//                .call()
//                .content();

        String response = "";
        try {
            response = orderAgent.call(naturalLanguageInput).getText();
            log.debug("LLM响应: {}", response);
        } catch (GraphRunnerException e) {
            log.error("调用订单解析Agent失败", e);
            OrderChatResponse fallback = new OrderChatResponse();
            fallback.setRawResponse("Agent call failed: " + e.getMessage());
            return fallback;
        }


        try {
            return converter.convert(response);
        } catch (Exception e) {
            log.error("解析LLM响应失败", e);
            // 返回一个包含原始响应的对象
            OrderChatResponse fallback = new OrderChatResponse();
            fallback.setRawResponse(response);
            return fallback;
        }
    }

    @Override
    public RiskAnalysisResponse analyzeTransportRisk(RiskAnalysisRequest request) {
        log.debug("开始分析运输风险, 设备ID: {}", request.getDeviceId());

        BeanOutputConverter<RiskAnalysisResponse> converter = 
                new BeanOutputConverter<>(RiskAnalysisResponse.class);

        // 构建用户输入
        String userInput = buildRiskAnalysisInput(request);

        ChatClient chatClient = chatClientBuilder.build();

        String response = chatClient.prompt()
                .system(RISK_ANALYSIS_SYSTEM_PROMPT + "\n\n" + converter.getFormat())
                .user(userInput)
                .call()
                .content();

        log.debug("LLM风险分析响应: {}", response);

        try {
            return converter.convert(response);
        } catch (Exception e) {
            log.error("解析风险分析响应失败", e);
            RiskAnalysisResponse fallback = new RiskAnalysisResponse();
            fallback.setRiskLevel("UNKNOWN");
            fallback.setSummary("Unable to parse risk analysis: " + e.getMessage());
            fallback.setRawResponse(response);
            return fallback;
        }
    }

    /**
     * 构建风险分析输入文本
     */
    private String buildRiskAnalysisInput(RiskAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Transport Risk Analysis Request:\n\n");
        
        sb.append("Device ID: ").append(request.getDeviceId()).append("\n");
        sb.append("Transport ID: ").append(request.getTransportId()).append("\n");
        sb.append("Cargo Type: ").append(request.getCargoType()).append("\n");
        
        if (request.getAcceptableMinTemp() != null && request.getAcceptableMaxTemp() != null) {
            sb.append("Acceptable Temperature Range: ")
              .append(request.getAcceptableMinTemp())
              .append("°C to ")
              .append(request.getAcceptableMaxTemp())
              .append("°C\n");
        }
        
        if (request.getTemperatureReadings() != null && !request.getTemperatureReadings().isEmpty()) {
            sb.append("\nTemperature Readings:\n");
            request.getTemperatureReadings().forEach(reading -> 
                sb.append("  - Time: ").append(reading.getTimestamp())
                  .append(", Temp: ").append(reading.getTemperature()).append("°C")
                  .append(", Humidity: ").append(reading.getHumidity()).append("%\n")
            );
            
            // 计算统计信息
            var temps = request.getTemperatureReadings().stream()
                    .map(RiskAnalysisRequest.TemperatureReading::getTemperature)
                    .toList();
            
            double avgTemp = temps.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double minTemp = temps.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double maxTemp = temps.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            
            sb.append("\nStatistics:\n");
            sb.append("  - Average Temperature: ").append(String.format("%.2f", avgTemp)).append("°C\n");
            sb.append("  - Min Temperature: ").append(minTemp).append("°C\n");
            sb.append("  - Max Temperature: ").append(maxTemp).append("°C\n");
            sb.append("  - Total Readings: ").append(temps.size()).append("\n");
        }
        
        if (request.getAdditionalNotes() != null) {
            sb.append("\nAdditional Notes: ").append(request.getAdditionalNotes()).append("\n");
        }
        
        return sb.toString();
    }
}
