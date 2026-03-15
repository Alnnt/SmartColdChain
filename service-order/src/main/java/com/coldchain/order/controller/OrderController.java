package com.coldchain.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coldchain.common.result.Result;
import com.coldchain.order.dto.OrderCreateDTO;
import com.coldchain.order.dto.OrderVO;
import com.coldchain.order.dto.PaymentCallbackRequest;
import com.coldchain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制器
 *
 * @author Alnnt
 */
@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单", description = "创建冷链物流订单，包含库存扣减和运单创建")
    @PostMapping("/create")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        OrderVO order = orderService.createOrder(dto, userId);
        return Result.success(order);
    }

    @Operation(summary = "查询我的订单", description = "根据当前登录用户查询订单列表（分页）")
    @GetMapping("/my")
    public Result<IPage<OrderVO>> listMyOrders(
            HttpServletRequest request,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        IPage<OrderVO> orders = orderService.listByUserId(userId, page, pageSize);
        return Result.success(orders);
    }

    @Operation(summary = "查询订单", description = "根据订单ID查询订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrder(
            @Parameter(description = "订单ID（文本）") @PathVariable("orderId") String orderIdStr) {
        Long orderId = parseOrderId(orderIdStr);
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
            @Parameter(description = "订单ID（文本）") @PathVariable("orderId") String orderIdStr) {
        Long orderId = parseOrderId(orderIdStr);
        Boolean result = orderService.cancelOrder(orderId);
        return Result.success(result);
    }

    @Operation(summary = "支付完成回调", description = "支付网关回调接口，订单支付完成后调用，状态从待支付更新为已支付")
    @PostMapping("/payment/callback")
    public Result<Boolean> paymentCallback(
            @Valid @RequestBody PaymentCallbackRequest request) {
        Boolean result = orderService.markOrderAsPaid(request.getOrderNo(), request.getPaidAmount());
        return Result.success("支付完成，订单状态已更新", result);
    }

    @Operation(summary = "管理端按仓库查询订单", description = "仓库管理员看本仓订单，超级管理员可传 warehouseId 或查全部")
    @GetMapping("/manager/orders")
    public Result<IPage<OrderVO>> listManagerOrders(
            HttpServletRequest request,
            @Parameter(description = "仓库ID（可选，超管不传则查全部）") @RequestParam(value = "warehouseId", required = false) String warehouseId,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        String rolesHeader = request.getHeader("X-Roles");
        String boundWarehouseId = request.getHeader("X-Warehouse-Id");
        boolean isSuperAdmin = rolesHeader != null && rolesHeader.contains("ROLE_ADMIN");
        Long resolvedWarehouseId = null;
        if (StringUtils.hasText(warehouseId)) {
            try {
                resolvedWarehouseId = Long.parseLong(warehouseId.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (!isSuperAdmin && StringUtils.hasText(boundWarehouseId)) {
            try {
                resolvedWarehouseId = Long.parseLong(boundWarehouseId.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        IPage<OrderVO> orders = orderService.listByWarehouseId(resolvedWarehouseId, page, pageSize);
        return Result.success(orders);
    }

    @Operation(summary = "发货", description = "将订单状态更新为已发货（仅已支付可发货）")
    @PutMapping("/{orderId}/ship")
    public Result<Boolean> shipOrder(
            HttpServletRequest request,
            @Parameter(description = "订单ID（文本）") @PathVariable("orderId") String orderIdStr) {
        Long orderId = parseOrderId(orderIdStr);
        String rolesHeader = request.getHeader("X-Roles");
        String boundWarehouseId = request.getHeader("X-Warehouse-Id");
        boolean isSuperAdmin = rolesHeader != null && rolesHeader.contains("ROLE_ADMIN");
        Long warehouseId = null;
        if (StringUtils.hasText(boundWarehouseId)) {
            try {
                warehouseId = Long.parseLong(boundWarehouseId.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        Boolean result = orderService.shipOrder(orderId, warehouseId, isSuperAdmin);
        return Result.success("发货成功", result);
    }

    /**
     * 从请求中获取用户ID（由网关转发时在Header中携带）
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userId)) {
            try {
                return Long.valueOf(userId.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static Long parseOrderId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("订单ID格式无效: " + id);
        }
    }
}
