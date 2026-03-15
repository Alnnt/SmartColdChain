package com.coldchain.inventory.controller;

import com.coldchain.common.result.Result;
import com.coldchain.inventory.dto.WarehouseCreateUpdateDTO;
import com.coldchain.inventory.entity.Warehouse;
import com.coldchain.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仓库管理接口（仅超级管理员可 CRUD）
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/manager/warehouse")
@RequiredArgsConstructor
@Tag(name = "仓库管理", description = "仓库 CRUD，仅超管")
public class WarehouseManagerController {

    private static final String ROLES_HEADER = "X-Roles";

    private final WarehouseService warehouseService;

    private static boolean isSuperAdmin(HttpServletRequest request) {
        String roles = request.getHeader(ROLES_HEADER);
        return StringUtils.hasText(roles) && roles.contains("ROLE_ADMIN");
    }

    @PostMapping
    @Operation(summary = "创建仓库")
    public Result<Warehouse> create(@Valid @RequestBody WarehouseCreateUpdateDTO dto, HttpServletRequest request) {
        if (!isSuperAdmin(request)) {
            return Result.fail(403, "仅超级管理员可操作");
        }
        Warehouse warehouse = Warehouse.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
        warehouse = warehouseService.create(warehouse);
        return Result.success("创建成功", warehouse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询仓库")
    public Result<Warehouse> getById(
            @Parameter(description = "仓库ID（文本）") @PathVariable("id") String idStr,
            HttpServletRequest request) {
        if (!isSuperAdmin(request)) {
            return Result.fail(403, "仅超级管理员可操作");
        }
        Long id = parseId(idStr, "仓库ID");
        Warehouse warehouse = warehouseService.getById(id);
        if (warehouse == null) {
            return Result.fail("仓库不存在");
        }
        return Result.success(warehouse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新仓库")
    public Result<Warehouse> update(
            @Parameter(description = "仓库ID（文本）") @PathVariable("id") String idStr,
            @Valid @RequestBody WarehouseCreateUpdateDTO dto,
            HttpServletRequest request) {
        if (!isSuperAdmin(request)) {
            return Result.fail(403, "仅超级管理员可操作");
        }
        Long id = parseId(idStr, "仓库ID");
        Warehouse warehouse = warehouseService.getById(id);
        if (warehouse == null) {
            return Result.fail("仓库不存在");
        }
        warehouse.setName(dto.getName());
        warehouse.setAddress(dto.getAddress());
        warehouse.setLatitude(dto.getLatitude());
        warehouse.setLongitude(dto.getLongitude());
        boolean ok = warehouseService.updateById(warehouse);
        return ok ? Result.success("更新成功", warehouse) : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除仓库（逻辑删除）")
    public Result<Boolean> delete(
            @Parameter(description = "仓库ID（文本）") @PathVariable("id") String idStr,
            HttpServletRequest request) {
        if (!isSuperAdmin(request)) {
            return Result.fail(403, "仅超级管理员可操作");
        }
        Long id = parseId(idStr, "仓库ID");
        boolean ok = warehouseService.removeById(id);
        return ok ? Result.success("删除成功", true) : Result.fail("删除失败");
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
