package com.coldchain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存扣减响应DTO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存扣减响应")
public class DeductStockResponse {

    /**
     * 是否扣减成功
     */
    @Schema(description = "是否扣减成功")
    private Boolean success;

    /**
     * 选中的仓库ID
     */
    @Schema(description = "选中的仓库ID")
    private Long warehouseId;

    /**
     * 选中的仓库名称
     */
    @Schema(description = "选中的仓库名称")
    private String warehouseName;

    /**
     * 仓库与用户的距离（公里）
     */
    @Schema(description = "仓库与用户的距离（公里）")
    private Double distance;

    /**
     * 扣减后的剩余库存
     */
    @Schema(description = "扣减后的剩余库存")
    private Integer remainingStock;
}
