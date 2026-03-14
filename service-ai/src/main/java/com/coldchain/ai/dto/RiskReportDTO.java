package com.coldchain.ai.dto;

import com.coldchain.ai.dto.enums.CargoCondition;
import com.coldchain.ai.dto.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 鍐烽摼椋庨櫓鍒嗘瀽鎶ュ憡
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskReportDTO {

    /**
     * 鏁翠綋椋庨櫓绛夌骇
     * 鍙栧€艰寖鍥? "LOW", "MEDIUM", "HIGH", "CRITICAL"
     */
    private RiskLevel riskLevel;

    /**
     * 椋庨櫓璇勫垎 (0-100)
     */
    private Integer riskScore;

    /**
     * 椋庨櫓璇勪及绠€杩?
     */
    private String summary;

    /**
     * 娓╁害妯″紡鍒嗘瀽
     */
    private String temperatureAnalysis;

    /**
     * 寤鸿閲囧彇鐨勬帾鏂?
     */
    private List<String> recommendations;

    /**
     * 璇嗗埆鍑虹殑娼滃湪闂
     */
    private List<String> potentialIssues;

    /**
     * 棰勪及璐х墿鐘跺喌
     * 鍙栧€艰寖鍥? "OPTIMAL", "ACCEPTABLE", "COMPROMISED", "DAMAGED"
     */
    private CargoCondition estimatedCargoCondition;
}