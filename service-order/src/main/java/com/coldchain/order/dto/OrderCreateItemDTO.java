package com.coldchain.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建订单时的单个商品项
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateItemDTO {

    /**
     * 商品ID（文本传输，避免前端 Long 溢出）
     */
    @NotNull(message = "商品ID不能为空")
    private String productId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer productCount;

    /**
     * 商品名称（用于订单展示，下单时由前端传入并落库）
     */
    private String productName;

    /**
     * 本行金额（数量×单价）
     */
    @NotNull(message = "本行金额不能为空")
    @DecimalMin(value = "0.01", message = "本行金额必须大于0")
    private BigDecimal amount;
}
