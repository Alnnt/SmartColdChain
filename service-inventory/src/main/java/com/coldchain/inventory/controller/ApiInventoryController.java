package com.coldchain.inventory.controller;

import com.coldchain.common.result.Result;
import com.coldchain.inventory.dto.DeductStockRequest;
import com.coldchain.inventory.dto.DeductStockResponse;
import com.coldchain.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存服务 Feign 对接 API（与订单服务调用路径一致）
 * 路径为 /api/inventory，供网关及订单服务 Feign 调用。
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "库存Feign接口", description = "订单服务调用的扣减/回滚接口")
public class ApiInventoryController {

    private final InventoryService inventoryService;

    /**
     * 按商品扣减库存（无地理位置时使用 0,0 选仓），返回选中的仓库ID供订单落库
     */
    @PostMapping("/decrease")
    @Operation(summary = "扣减库存")
    public Result<DeductStockResponse> decrease(
            @RequestParam(value = "productId") String productIdStr,
            @RequestParam("count") Integer count) {
        Long productId = parseLong(productIdStr, "商品ID");
        DeductStockRequest request = DeductStockRequest.builder()
                .productId(productId)
                .count(count)
                .userLat(0.0)
                .userLon(0.0)
                .build();
        DeductStockResponse response = inventoryService.deductStock(
                request.getProductId(),
                request.getCount(),
                request.getUserLat(),
                request.getUserLon());
        if (response.getSuccess()) {
            return Result.success(response);
        }
        return Result.fail("库存不足，扣减失败");
    }

    /**
     * 冻结库存（下单时预占，不实际扣减）
     */
    @PostMapping("/freeze")
    @Operation(summary = "冻结库存")
    public Result<DeductStockResponse> freeze(
            @RequestParam(value = "productId") String productIdStr,
            @RequestParam("count") Integer count) {
        Long productId = parseLong(productIdStr, "商品ID");
        DeductStockResponse response = inventoryService.freezeStock(productId, count, 0.0, 0.0);
        if (response.getSuccess()) {
            return Result.success(response);
        }
        return Result.fail("库存不足，冻结失败");
    }

    /**
     * 确认扣减（支付成功时：冻结转实际扣减）
     */
    @PostMapping("/confirm-deduct")
    @Operation(summary = "确认扣减")
    public Result<Boolean> confirmDeduct(
            @RequestParam(value = "inventoryId") String inventoryIdStr,
            @RequestParam("count") Integer count) {
        Long inventoryId = parseLong(inventoryIdStr, "库存ID");
        boolean ok = inventoryService.confirmDeduct(inventoryId, count);
        return ok ? Result.success(true) : Result.fail("确认扣减失败");
    }

    /**
     * 取消冻结（订单取消时释放预占）
     */
    @PostMapping("/cancel-freeze")
    @Operation(summary = "取消冻结")
    public Result<Boolean> cancelFreeze(
            @RequestParam(value = "inventoryId") String inventoryIdStr,
            @RequestParam("count") Integer count) {
        Long inventoryId = parseLong(inventoryIdStr, "库存ID");
        boolean ok = inventoryService.cancelFreeze(inventoryId, count);
        return ok ? Result.success(true) : Result.fail("取消冻结失败");
    }

    /**
     * 按商品回滚库存（订单取消时加回，仅用于未冻结直接扣减的兼容场景）
     */
    @PostMapping("/rollback")
    @Operation(summary = "回滚库存")
    public Result<Boolean> rollback(
            @RequestParam(value = "productId") String productIdStr,
            @RequestParam("count") Integer count) {
        Long productId = parseLong(productIdStr, "商品ID");
        boolean ok = inventoryService.rollbackStock(productId, count);
        return ok ? Result.success(true) : Result.fail("回滚失败");
    }

    private static Long parseLong(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + "不能为空");
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + "格式无效");
        }
    }
}
