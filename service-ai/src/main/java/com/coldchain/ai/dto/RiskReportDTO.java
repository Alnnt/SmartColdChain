package com.coldchain.ai.dto;

import com.coldchain.ai.dto.enums.CargoCondition;
import com.coldchain.ai.dto.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 冷链风险分析报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskReportDTO {

    /**
     * 整体风险等级
     * 取值范围: "LOW", "MEDIUM", "HIGH", "CRITICAL"
     */
    private RiskLevel riskLevel;

    /**
     * 风险评分 (0-100)
     */
    private Integer riskScore;

    /**
     * 风险评估简述
     */
    private String summary;

    /**
     * 温度模式分析
     */
    private String temperatureAnalysis;

    /**
     * 建议采取的措施
     */
    private List<String> recommendations;

    /**
     * 识别出的潜在问题
     */
    private List<String> potentialIssues;

    /**
     * 预估货物状况
     * 取值范围: "OPTIMAL", "ACCEPTABLE", "COMPROMISED", "DAMAGED"
     */
    private CargoCondition estimatedCargoCondition;
}