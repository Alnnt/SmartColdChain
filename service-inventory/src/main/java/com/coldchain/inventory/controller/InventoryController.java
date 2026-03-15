package com.coldchain.inventory.controller;

import com.coldchain.common.result.Result;
import com.coldchain.inventory.dto.DeductStockRequest;
import com.coldchain.inventory.dto.DeductStockResponse;
import com.coldchain.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搴撳瓨鎺у埗鍣?
 *
 * @author Alnnt
 */
@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "库存管理", description = "库存扣减、智能调度接口")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 智能库存扣减
     * <p>
     * 根据用户位置自动选择最近的仓库进行库存扣减
     */
    @PostMapping("/deduct")
    @Operation(summary = "智能库存扣减", description = "根据用户位置自动选择最近的仓库进行库存扣减")
    public Result<DeductStockResponse> deductStock(@Valid @RequestBody DeductStockRequest request) {
        log.info("接收到库存扣减请求 {}", request);

        DeductStockResponse response = inventoryService.deductStock(
                request.getProductId(),
                request.getCount(),
                request.getUserLat(),
                request.getUserLon());

        if (response.getSuccess()) {
            log.info("库存扣减成功, 选中仓库: {}, 距离: {} km",
                    response.getWarehouseName(), response.getDistance());
            return Result.success("库存扣减成功", response);
        } else {
            log.warn("库存扣减失败，库存不足");
            return Result.fail("库存不足，扣减失败");
        }
    }

    /**
     * 冻结库存（TCC事务Try阶段使用）
     */
    @PostMapping("/freeze")
    @Operation(summary = "冻结库存", description = "TCC服务Try阶段使用，预扣库存")
    public Result<DeductStockResponse> freezeStock(@Valid @RequestBody DeductStockRequest request) {
        log.info("接收到库存冻结请求 {}", request);

        DeductStockResponse response = inventoryService.freezeStock(
                request.getProductId(),
                request.getCount(),
                request.getUserLat(),
                request.getUserLon());

        if (response.getSuccess()) {
            return Result.success("库存冻结成功", response);
        } else {
            return Result.fail("库存不足，冻结失败");
        }
    }
}
