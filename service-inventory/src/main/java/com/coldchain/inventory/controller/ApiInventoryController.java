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
     * 按商品扣减库存（无地理位置时使用 0,0 选仓）
     */
    @PostMapping("/decrease")
    @Operation(summary = "扣减库存")
    public Result<Boolean> decrease(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
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
            return Result.success(true);
        }
        return Result.fail("库存不足，扣减失败");
    }

    /**
     * 按商品回滚库存（订单取消时加回）
     */
    @PostMapping("/rollback")
    @Operation(summary = "回滚库存")
    public Result<Boolean> rollback(
            @RequestParam("productId") Long productId,
            @RequestParam("count") Integer count) {
        boolean ok = inventoryService.rollbackStock(productId, count);
        return ok ? Result.success(true) : Result.fail("回滚失败");
    }
}
