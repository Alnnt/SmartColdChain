package com.coldchain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存扣减请求DTO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存扣减请求")
public class DeductStockRequest {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    /**
     * 扣减数量
     */
    @NotNull(message = "扣减数量不能为空")
    @Min(value = 1, message = "扣减数量必须大于0")
    @Schema(description = "扣减数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer count;

    /**
     * 用户纬度
     */
    @NotNull(message = "用户纬度不能为空")
    @Schema(description = "用户纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double userLat;

    /**
     * 用户经度
     */
    @NotNull(message = "用户经度不能为空")
    @Schema(description = "用户经度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double userLon;
}
