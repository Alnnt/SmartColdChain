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
    private Long warehouseId;
}
