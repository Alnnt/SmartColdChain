package com.coldchain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应 VO（一个订单包含多个明细项）
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private String id;
    private String orderNo;
    private String userId;
    /**
     * 订单总金额
     */
    private BigDecimal amount;
    private Integer status;
    private String statusDesc;
    private String warehouseId;
    private String addressId;
    private String waybillId;
    private LocalDateTime createTime;
    /**
     * 订单明细列表
     */
    private List<OrderItemVO> items;
}
