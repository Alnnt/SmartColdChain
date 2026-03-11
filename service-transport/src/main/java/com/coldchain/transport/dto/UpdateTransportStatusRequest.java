package com.coldchain.transport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新运单状态请求DTO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新运单状态请求")
public class UpdateTransportStatusRequest {

    /**
     * 运单ID
     */
    @NotNull(message = "运单ID不能为空")
    @Schema(description = "运单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long transportOrderId;

    /**
     * 新状态（0-待取货，1-运输中，2-已送达，3-已取消）
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 3, message = "状态值无效")
    @Schema(description = "新状态（0-待取货，1-运输中，2-已送达，3-已取消）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
