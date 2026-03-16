package com.coldchain.order.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存扣减结果（Feign 反序列化用，与 DeductStockResponse 字段兼容）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecreaseStockVO {

    private Boolean success;
    /** 库存记录ID（冻结时返回，支付时确认扣减用） */
    private Long inventoryId;
    private Long warehouseId;
}
