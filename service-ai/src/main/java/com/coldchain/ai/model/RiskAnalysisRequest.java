package com.coldchain.ai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 椋庨櫓鍒嗘瀽璇锋眰
 *
 * @author Alnnt
 */
@Data
@Schema(description = "杩愯緭椋庨櫓鍒嗘瀽璇锋眰")
public class RiskAnalysisRequest {

    /**
     * 璁惧ID
     */
    @NotBlank(message = "璁惧ID涓嶈兘涓虹┖")
    @Schema(description = "IoT璁惧ID", example = "DEVICE-001")
    private String deviceId;

    /**
     * 杩愯緭鍗旾D
     */
    @Schema(description = "杩愯緭鍗旾D", example = "TRANS-2024-001")
    private String transportId;

    /**
     * 璐х墿绫诲瀷
     */
    @Schema(description = "璐х墿绫诲瀷", example = "鍐峰喕娴烽矞")
    private String cargoType;

    /**
     * 鍙帴鍙楁渶浣庢俯搴?
     */
    @Schema(description = "鍙帴鍙楁渶浣庢俯搴?掳C)", example = "-20")
    private Double acceptableMinTemp;

    /**
     * 鍙帴鍙楁渶楂樻俯搴?
     */
    @Schema(description = "鍙帴鍙楁渶楂樻俯搴?掳C)", example = "-15")
    private Double acceptableMaxTemp;

    /**
     * 娓╁害璇绘暟鍒楄〃
     */
    @Schema(description = "娓╁害璇绘暟鍒楄〃")
    private List<TemperatureReading> temperatureReadings;

    /**
     * 闄勫姞璇存槑
     */
    @Schema(description = "闄勫姞璇存槑")
    private String additionalNotes;

    /**
     * 娓╁害璇绘暟
     */
    @Data
    @Schema(description = "娓╁害璇绘暟")
    public static class TemperatureReading {

        /**
         * 鏃堕棿鎴?
         */
        @Schema(description = "閲囬泦鏃堕棿")
        private LocalDateTime timestamp;

        /**
         * 娓╁害鍊?
         */
        @Schema(description = "娓╁害(掳C)", example = "-18.5")
        private Double temperature;

        /**
         * 婀垮害鍊?
         */
        @Schema(description = "婀垮害(%)", example = "65")
        private Double humidity;
    }
}
