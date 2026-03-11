package com.coldchain.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.coldchain.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 库存实体
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_inventory")
@Schema(description = "库存实体")
public class Inventory extends BaseEntity {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * 仓库ID
     */
    @Schema(description = "仓库ID")
    private Long warehouseId;

    /**
     * 总库存
     */
    @Schema(description = "总库存")
    private Integer totalStock;

    /**
     * 冻结库存（已预留但未出库）
     */
    @Schema(description = "冻结库存")
    private Integer frozenStock;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @Schema(description = "版本号")
    private Integer version;
}
