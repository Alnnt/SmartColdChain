package com.coldchain.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 风险分析响应
 *
 * @author coldchain
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "运输风险分析响应")
public class RiskAnalysisResponse {

    /**
     * 风险等级
     */
    @Schema(description = "风险等级", example = "MEDIUM", 
            allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private String riskLevel;

    /**
     * 风险评分 (0-100)
     */
    @Schema(description = "风险评分(0-100)", example = "45")
    private Integer riskScore;

    /**
     * 风险摘要
     */
    @Schema(description = "风险评估摘要")
    private String summary;

    /**
     * 温度分析
     */
    @Schema(description = "温度模式分析")
    private String temperatureAnalysis;

    /**
     * 建议措施
     */
    @Schema(description = "建议措施列表")
    private List<String> recommendations;

    /**
     * 潜在问题
     */
    @Schema(description = "识别的潜在问题")
    private List<String> potentialIssues;

    /**
     * 预估货物状况
     */
    @Schema(description = "预估货物状况", 
            allowableValues = {"OPTIMAL", "ACCEPTABLE", "COMPROMISED", "DAMAGED"})
    private String estimatedCargoCondition;

    /**
     * 原始响应（解析失败时使用）
     */
    @Schema(description = "原始响应", hidden = true)
    private String rawResponse;
}
