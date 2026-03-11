package com.coldchain.ai.service;

import com.coldchain.ai.model.OrderChatResponse;
import com.coldchain.ai.model.RiskAnalysisRequest;
import com.coldchain.ai.model.RiskAnalysisResponse;

/**
 * AI助手服务接口
 *
 * @author Alnnt
 */
public interface AiAssistantService {

    /**
     * 从自然语言描述中解析订单信息
     *
     * @param naturalLanguageInput 自然语言订单描述
     * @return 结构化的订单信息
     */
    OrderChatResponse parseOrderFromNaturalLanguage(String naturalLanguageInput);

    /**
     * 分析运输风险
     *
     * @param request 运输数据统计请求
     * @return 风险分析报告
     */
    RiskAnalysisResponse analyzeTransportRisk(RiskAnalysisRequest request);
}
