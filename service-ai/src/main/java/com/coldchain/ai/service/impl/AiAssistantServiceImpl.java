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
 * AI鍔╂墜鏈嶅姟瀹炵幇绫?
 * 浣跨敤Spring AI ChatClient涓嶭LM浜や簰
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
        log.debug("寮€濮嬭В鏋愯鍗? 杈撳叆: {}", naturalLanguageInput);

        String response = null;
        try {
            response = orderAgent.call(naturalLanguageInput).getText();
            log.debug("LLM鍝嶅簲: {}", response);
            return new ObjectMapper().readValue(response, OrderChatResponse.class);
        } catch (Exception e) {
            log.error("璁㈠崟瑙ｆ瀽澶辫触", e);
            OrderChatResponse fallback = new OrderChatResponse();
            fallback.setRawResponse(response);
            return fallback;
        }
    }

    @Override
    public RiskAnalysisResponse analyzeTransportRisk(RiskAnalysisRequest request) {
        log.debug("寮€濮嬪垎鏋愯繍杈撻闄? 璁惧ID: {}", request.getDeviceId());

        String userInput = buildRiskAnalysisInput(request);
        System.out.println(userInput);
        String response = null;
        try {
            response = riskAgent.call(userInput).getText();
            log.debug("LLM椋庨櫓鍒嗘瀽鍝嶅簲: {}", response);
            return new ObjectMapper().readValue(response, RiskAnalysisResponse.class);
        } catch (Exception e) {
            log.error("椋庨櫓鍒嗘瀽澶辫触", e);
            RiskAnalysisResponse fallback = new RiskAnalysisResponse();
            fallback.setRiskLevel("UNKNOWN");
            fallback.setSummary("Error during risk analysis: " + e.getMessage());
            fallback.setRawResponse(response);
            return fallback;
        }
    }

    /**
     * 鏋勫缓椋庨櫓鍒嗘瀽杈撳叆鏂囨湰
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
                    .append("掳C to ")
                    .append(request.getAcceptableMaxTemp())
                    .append("掳C\n");
        }

        if (request.getTemperatureReadings() != null && !request.getTemperatureReadings().isEmpty()) {
            sb.append("\nTemperature Readings:\n");
            request.getTemperatureReadings().forEach(reading -> sb.append("  - Time: ").append(reading.getTimestamp())
                    .append(", Temp: ").append(reading.getTemperature()).append("掳C")
                    .append(", Humidity: ").append(reading.getHumidity()).append("%\n"));

            // 璁＄畻缁熻淇℃伅
            var temps = request.getTemperatureReadings().stream()
                    .map(RiskAnalysisRequest.TemperatureReading::getTemperature)
                    .toList();

            double avgTemp = temps.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double minTemp = temps.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double maxTemp = temps.stream().mapToDouble(Double::doubleValue).max().orElse(0);

            sb.append("\nStatistics:\n");
            sb.append("  - Average Temperature: ").append(String.format("%.2f", avgTemp)).append("掳C\n");
            sb.append("  - Min Temperature: ").append(minTemp).append("掳C\n");
            sb.append("  - Max Temperature: ").append(maxTemp).append("掳C\n");
            sb.append("  - Total Readings: ").append(temps.size()).append("\n");
        }

        if (request.getAdditionalNotes() != null) {
            sb.append("\nAdditional Notes: ").append(request.getAdditionalNotes()).append("\n");
        }

        return sb.toString();
    }
}
