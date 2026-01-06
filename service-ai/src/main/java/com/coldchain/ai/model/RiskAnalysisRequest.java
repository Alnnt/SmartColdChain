package com.coldchain.ai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险分析请求
 *
 * @author coldchain
 */
@Data
@Schema(description = "运输风险分析请求")
public class RiskAnalysisRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "IoT设备ID", example = "DEVICE-001")
    private String deviceId;

    /**
     * 运输单ID
     */
    @Schema(description = "运输单ID", example = "TRANS-2024-001")
    private String transportId;

    /**
     * 货物类型
     */
    @Schema(description = "货物类型", example = "冷冻海鲜")
    private String cargoType;

    /**
     * 可接受最低温度
     */
    @Schema(description = "可接受最低温度(°C)", example = "-20")
    private Double acceptableMinTemp;

    /**
     * 可接受最高温度
     */
    @Schema(description = "可接受最高温度(°C)", example = "-15")
    private Double acceptableMaxTemp;

    /**
     * 温度读数列表
     */
    @Schema(description = "温度读数列表")
    private List<TemperatureReading> temperatureReadings;

    /**
     * 附加说明
     */
    @Schema(description = "附加说明")
    private String additionalNotes;

    /**
     * 温度读数
     */
    @Data
    @Schema(description = "温度读数")
    public static class TemperatureReading {

        /**
         * 时间戳
         */
        @Schema(description = "采集时间")
        private LocalDateTime timestamp;

        /**
         * 温度值
         */
        @Schema(description = "温度(°C)", example = "-18.5")
        private Double temperature;

        /**
         * 湿度值
         */
        @Schema(description = "湿度(%)", example = "65")
        private Double humidity;
    }
}
