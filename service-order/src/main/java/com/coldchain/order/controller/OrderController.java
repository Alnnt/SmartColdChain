package com.coldchain.order.controller;

import com.coldchain.common.result.Result;
import com.coldchain.order.dto.OrderCreateDTO;
import com.coldchain.order.dto.OrderVO;
import com.coldchain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制器
 *
 * @author ColdChain
 */
@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单", description = "创建冷链物流订单，包含库存扣减和运单创建")
    @PostMapping("/create")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        OrderVO order = orderService.createOrder(dto);
        return Result.success(order);
    }

    @Operation(summary = "查询订单", description = "根据订单ID查询订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        OrderVO order = orderService.getOrderById(orderId);
        return Result.success(order);
    }

    @Operation(summary = "根据订单编号查询", description = "根据订单编号查询订单详情")
    @GetMapping("/no/{orderNo}")
    public Result<OrderVO> getOrderByNo(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        OrderVO order = orderService.getOrderByOrderNo(orderNo);
        return Result.success(order);
    }

    @Operation(summary = "取消订单", description = "取消待支付的订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Boolean result = orderService.cancelOrder(orderId);
        return Result.success(result);
    }
}
