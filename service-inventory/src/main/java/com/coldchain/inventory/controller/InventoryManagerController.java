package com.coldchain.inventory.controller;

import com.coldchain.common.result.Result;
import com.coldchain.inventory.dto.AddInventoryItemRequest;
import com.coldchain.inventory.dto.AdjustStockRequest;
import com.coldchain.inventory.dto.InventoryItemDTO;
import com.coldchain.inventory.entity.Warehouse;
import com.coldchain.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@Tag(name = "库存管理端", description = "仓库列表、库存列表、库存调整、添加库存")
public class InventoryManagerController {

    private static final String ROLES_HEADER = "X-Roles";
    private static final String WAREHOUSE_ID_HEADER = "X-Warehouse-Id";

    private final InventoryService inventoryService;

    private static boolean isSuperAdmin(HttpServletRequest request) {
        String roles = request.getHeader(ROLES_HEADER);
        return StringUtils.hasText(roles) && roles.contains("ROLE_ADMIN");
    }

    private static Long resolveWarehouseId(HttpServletRequest request, String queryWarehouseId) {
        if (isSuperAdmin(request) && StringUtils.hasText(queryWarehouseId)) {
            try {
                return Long.parseLong(queryWarehouseId.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String bound = request.getHeader(WAREHOUSE_ID_HEADER);
        if (StringUtils.hasText(bound)) {
            try {
                return Long.parseLong(bound.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @GetMapping("/warehouses")
    @Operation(summary = "查询仓库列表（可选按仓库ID过滤）")
    public Result<List<Warehouse>> listWarehouses(
            HttpServletRequest request,
            @Parameter(description = "仓库ID（可选，超管传则只返回该仓）") @RequestParam(value = "warehouseId", required = false) String warehouseId) {
        Long resolved = resolveWarehouseId(request, warehouseId);
        List<Warehouse> list = inventoryService.listWarehouses(resolved);
        return Result.success(list);
    }

    @GetMapping("/items")
    @Operation(summary = "查询库存列表（可选按仓库ID过滤）")
    public Result<List<InventoryItemDTO>> listInventoryItems(
            HttpServletRequest request,
            @Parameter(description = "仓库ID（可选，超管传则只返回该仓）") @RequestParam(value = "warehouseId" ,required = false) String warehouseId) {
        Long resolved = resolveWarehouseId(request, warehouseId);
        List<InventoryItemDTO> list = inventoryService.listInventoryItems(resolved);
        return Result.success(list);
    }

    @PostMapping("/adjust")
    @Operation(summary = "调整库存")
    public Result<Boolean> adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        Long inventoryId = parseId(request.getInventoryId(), "库存ID");
        boolean ok = inventoryService.adjustStock(inventoryId, request.getDelta());
        return ok ? Result.success("调整成功", true) : Result.fail("调整失败（数量不足或违反约束）");
    }

    @PostMapping("/items")
    @Operation(summary = "添加库存商品（有则加数量，无则新增）")
    public Result<Boolean> addInventoryItem(@Valid @RequestBody AddInventoryItemRequest request, HttpServletRequest req) {
        Long warehouseId = null;
        if (StringUtils.hasText(request.getWarehouseId())) {
            try {
                warehouseId = Long.parseLong(request.getWarehouseId().trim());
            } catch (NumberFormatException e) {
                return Result.fail("仓库ID格式无效");
            }
        }
        if (warehouseId == null) {
            String bound = req.getHeader(WAREHOUSE_ID_HEADER);
            if (StringUtils.hasText(bound)) {
                try {
                    warehouseId = Long.parseLong(bound.trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (warehouseId == null) {
            return Result.fail("请指定仓库或使用仓库管理员账号");
        }
        if (!isSuperAdmin(req)) {
            String bound = req.getHeader(WAREHOUSE_ID_HEADER);
            if (StringUtils.hasText(bound)) {
                try {
                    long boundId = Long.parseLong(bound.trim());
                    if (boundId != warehouseId) {
                        return Result.fail("无权操作该仓库");
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        Long productId = parseId(request.getProductId(), "商品ID");
        boolean ok = inventoryService.addInventoryItem(warehouseId, productId, request.getQuantity());
        return ok ? Result.success("添加成功", true) : Result.fail("添加失败");
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
