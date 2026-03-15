package com.coldchain.inventory.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存管理列表项 DTO（Long 型 ID 序列化为字符串，避免前端溢出）
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存列表项")
public class InventoryItemDTO {

    @Schema(description = "库存ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "商品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    @Schema(description = "仓库ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long warehouseId;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "冻结库存")
    private Integer frozenStock;

    @Schema(description = "可用库存")
    private Integer availableStock;
}
