package com.coldchain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加库存商品请求（仓库管理员对本仓添加）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加库存商品")
public class AddInventoryItemRequest {

    @Schema(description = "仓库ID（可选，仓库管理员时用绑定仓）")
    private String warehouseId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
