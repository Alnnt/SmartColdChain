package com.coldchain.ai.controller;

import com.coldchain.ai.model.OrderChatRequest;
import com.coldchain.ai.model.OrderChatResponse;
import com.coldchain.ai.model.RiskAnalysisRequest;
import com.coldchain.ai.model.RiskAnalysisResponse;
import com.coldchain.ai.service.AiAssistantService;
import com.coldchain.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI助手控制器
 * 提供订单智能解析和运输风险分析功能
 *
 * @author Alnnt
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI助手", description = "智能对话与风险分析接口")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    /**
     * 订单智能解析
     * 接收自然语言描述，提取订单详情（商品、数量、地址）并返回JSON格式
     *
     * @param request 包含自然语言订单描述的请求
     * @return 结构化的订单信息JSON
     */
    @PostMapping("/chat/order")
    @Operation(summary = "订单智能解析", description = "通过自然语言描述提取订单详情，返回结构化JSON")
    public Result<OrderChatResponse> parseOrder(@Valid @RequestBody OrderChatRequest request) {
        log.info("收到订单解析请求: {}", request.getMessage());
        OrderChatResponse response = aiAssistantService.parseOrderFromNaturalLanguage(request.getMessage());
        return Result.success(response);
    }

    /**
     * 运输风险分析
     * 分析温度数据并生成风险报告
     *
     * @param request 包含运输统计数据的请求
     * @return 风险分析报告
     */
    @PostMapping("/analyze/risk")
    @Operation(summary = "运输风险分析", description = "分析温度数据并生成风险报告")
    public Result<RiskAnalysisResponse> analyzeRisk(@Valid @RequestBody RiskAnalysisRequest request) {
        log.info("收到风险分析请求, 设备ID: {}, 数据点数量: {}", 
                request.getDeviceId(), 
                request.getTemperatureReadings() != null ? request.getTemperatureReadings().size() : 0);
        RiskAnalysisResponse response = aiAssistantService.analyzeTransportRisk(request);
        return Result.success(response);
    }
}
