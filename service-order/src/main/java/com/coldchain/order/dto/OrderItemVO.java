package com.coldchain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单明细项 VO（ID 等以文本形式避免前端 Long 溢出）
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

    private String id;
    private String productId;
    private String productName;
    private Integer count;
    private BigDecimal amount;
}
