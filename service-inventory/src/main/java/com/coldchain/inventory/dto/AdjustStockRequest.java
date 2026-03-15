package com.coldchain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存调整请求 DTO（管理端）
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存调整请求")
public class AdjustStockRequest {

    @NotNull(message = "库存ID不能为空")
    @Schema(description = "库存记录ID（文本，避免前端 Long 溢出）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inventoryId;

    @NotNull(message = "调整数量不能为空")
    @Schema(description = "调整数量，正数增加、负数减少", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer delta;
}
