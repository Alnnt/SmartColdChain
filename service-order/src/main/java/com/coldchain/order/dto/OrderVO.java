package com.coldchain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应 VO
 *
 * @author Alnnt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    /**
     * 订单ID（文本传输，避免前端 Long 溢出）
     */
    private String id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID（文本传输）
     */
    private String userId;

    /**
     * 商品ID（文本传输）
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 履约仓库ID（文本传输）
     */
    private String warehouseId;

    /**
     * 收货地址ID（文本传输）
     */
    private String addressId;

    /**
     * 运单ID（文本传输）
     */
    private String waybillId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
