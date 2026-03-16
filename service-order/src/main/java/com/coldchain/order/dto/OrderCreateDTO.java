package com.coldchain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求 DTO（一个订单包含多个商品项）
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

    /**
     * 商品项列表（每个项包含 productId、productCount）
     */
    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderCreateItemDTO> items;

    /**
     * 订单总金额
     */
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    private BigDecimal amount;

    /**
     * 收货地址ID（文本传输，避免前端 Long 溢出）
     */
    @NotNull(message = "收货地址ID不能为空")
    private String addressId;
}
