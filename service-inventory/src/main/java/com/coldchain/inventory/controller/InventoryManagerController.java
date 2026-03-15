package com.coldchain.inventory.controller;

import com.coldchain.common.result.Result;
import com.coldchain.inventory.dto.AdjustStockRequest;
import com.coldchain.inventory.dto.InventoryItemDTO;
import com.coldchain.inventory.entity.Warehouse;
import com.coldchain.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存管理端接口（管理后台前端调用）
 *
 * @author Alnnt
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/manager")
@RequiredArgsConstructor
@Tag(name = "库存管理端", description = "仓库列表、库存列表、库存调整")
public class InventoryManagerController {

    private final InventoryService inventoryService;

    @GetMapping("/warehouses")
    @Operation(summary = "查询所有仓库")
    public Result<List<Warehouse>> listWarehouses() {
        List<Warehouse> list = inventoryService.listWarehouses();
        return Result.success(list);
    }

    @GetMapping("/items")
    @Operation(summary = "查询库存列表")
    public Result<List<InventoryItemDTO>> listInventoryItems() {
        List<InventoryItemDTO> list = inventoryService.listInventoryItems();
        return Result.success(list);
    }

    @PostMapping("/adjust")
    @Operation(summary = "调整库存")
    public Result<Boolean> adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        Long inventoryId = parseId(request.getInventoryId(), "库存ID");
        boolean ok = inventoryService.adjustStock(inventoryId, request.getDelta());
        return ok ? Result.success("调整成功", true) : Result.fail("调整失败（数量不足或违反约束）");
    }

    private static Long parseId(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + "不能为空");
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + "格式无效");
        }
    }
}
