package com.coldchain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仓库库存信息DTO（用于智能调度计算）
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仓库库存信息")
public class WarehouseStockDTO {

    /**
     * 库存ID
     */
    @Schema(description = "库存ID")
    private Long inventoryId;

    /**
     * 仓库ID
     */
    @Schema(description = "仓库ID")
    private Long warehouseId;

    /**
     * 仓库名称
     */
    @Schema(description = "仓库名称")
    private String warehouseName;

    /**
     * 仓库纬度
     */
    @Schema(description = "仓库纬度")
    private Double latitude;

    /**
     * 仓库经度
     */
    @Schema(description = "仓库经度")
    private Double longitude;

    /**
     * 可用库存（总库存 - 冻结库存）
     */
    @Schema(description = "可用库存")
    private Integer availableStock;

    /**
     * 总库存
     */
    @Schema(description = "总库存")
    private Integer totalStock;
}
