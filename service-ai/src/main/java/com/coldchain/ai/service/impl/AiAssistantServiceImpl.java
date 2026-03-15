package com.coldchain.ai.service.impl;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.coldchain.ai.model.OrderChatResponse;
import com.coldchain.ai.model.RiskAnalysisRequest;
import com.coldchain.ai.model.RiskAnalysisResponse;
import com.coldchain.ai.service.AiAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * AI助手服务实现类
 * 使用Spring AI ChatClient与LLM交互
 *
 * @author Alnnt
 */
@Slf4j
@Service
public class AiAssistantServiceImpl implements AiAssistantService {
    private final ReactAgent orderAgent;
    private final ReactAgent riskAgent;

    public AiAssistantServiceImpl(@Qualifier("orderAgent") ReactAgent orderAgent,
            @Qualifier("riskAnalysisAgent") ReactAgent riskAgent) {
        this.orderAgent = orderAgent;
        this.riskAgent = riskAgent;
    }

    @Override
    public OrderChatResponse parseOrderFromNaturalLanguage(String naturalLanguageInput) {
        log.debug("开始解析订单 输入: {}", naturalLanguageInput);

        String response = null;
        try {
            response = orderAgent.call(naturalLanguageInput).getText();
            log.debug("LLM响应: {}", response);
            return new ObjectMapper().readValue(response, OrderChatResponse.class);
        } catch (Exception e) {
            log.error("订单解析失败", e);
            OrderChatResponse fallback = new OrderChatResponse();
            fallback.setRawResponse(response);
            return fallback;
        }
    }

    @Override
    public RiskAnalysisResponse analyzeTransportRisk(RiskAnalysisRequest request) {
        log.debug("开始分析运输风险 设备ID: {}", request.getDeviceId());

        String userInput = buildRiskAnalysisInput(request);
        System.out.println(userInput);
        String response = null;
        try {
            response = riskAgent.call(userInput).getText();
            log.debug("LLM风险分析响应: {}", response);
            return new ObjectMapper().readValue(response, RiskAnalysisResponse.class);
        } catch (Exception e) {
            log.error("风险分析失败", e);
            RiskAnalysisResponse fallback = new RiskAnalysisResponse();
            fallback.setRiskLevel("UNKNOWN");
            fallback.setSummary("Error during risk analysis: " + e.getMessage());
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
            request.getTemperatureReadings().forEach(reading -> sb.append("  - Time: ").append(reading.getTimestamp())
                    .append(", Temp: ").append(reading.getTemperature()).append("°C")
                    .append(", Humidity: ").append(reading.getHumidity()).append("%\n"));

            // 璁＄畻缁熻淇℃伅
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
