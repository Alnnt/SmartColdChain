package com.coldchain.transport.controller;

import com.coldchain.common.result.Result;
import com.coldchain.transport.dto.CreateWaybillRequest;
import com.coldchain.transport.entity.Driver;
import com.coldchain.transport.entity.TransportOrder;
import com.coldchain.transport.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运输控制器
 *
 * @author Alnnt
 */
@Slf4j
@RestController
@RequestMapping("/transport")
@RequiredArgsConstructor
@Tag(name = "运输管理", description = "运单创建、状态更新、司机管理接口")
public class TransportController {

    private final TransportService transportService;

    @PostMapping("/waybill/create")
    @Operation(summary = "创建运单", description = "根据订单ID创建运单，自动分配空闲司机")
    public Result<TransportOrder> createWaybill(@Valid @RequestBody CreateWaybillRequest request) {
        log.info("接收到创建运单请求: {}", request);

        try {
            TransportOrder transportOrder = transportService.createWaybill(
                    request.getOrderId(),
                    request.getFromAddress(),
                    request.getToAddress());
            log.info("运单创建成功: {}", transportOrder.getId());
            return Result.success("运单创建成功", transportOrder);
        } catch (RuntimeException e) {
            log.error("创建运单失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/waybill/order/{orderId}")
    @Operation(summary = "根据订单ID查询运单", description = "根据订单ID查询关联的运单信息")
    public Result<TransportOrder> getByOrderId(
            @Parameter(description = "订单ID（文本）") @PathVariable("orderId") String orderIdStr) {
        Long orderId = parseId(orderIdStr, "订单ID");
        TransportOrder transportOrder = transportService.getByOrderId(orderId);
        if (transportOrder == null) {
            return Result.fail("运单不存在");
        }
        return Result.success(transportOrder);
    }

    @PostMapping("/waybill/{transportOrderId}/start")
    @Operation(summary = "开始运输", description = "司机取货后调用，将运单状态更新为运输中")
    public Result<Boolean> startTransport(
            @Parameter(description = "运单ID（文本）") @PathVariable("transportOrderId") String transportOrderIdStr) {
        Long transportOrderId = parseId(transportOrderIdStr, "运单ID");
        log.info("开始运输请求: transportOrderId={}", transportOrderId);

        boolean success = transportService.startTransport(transportOrderId);
        if (success) {
            return Result.success("运输已开始", true);
        } else {
            return Result.fail("更新状态失败");
        }
    }

    @PostMapping("/waybill/{transportOrderId}/complete")
    @Operation(summary = "完成运输", description = "司机送达后调用，将运单状态更新为已送达")
    public Result<Boolean> completeTransport(
            @Parameter(description = "运单ID（文本）") @PathVariable("transportOrderId") String transportOrderIdStr) {
        Long transportOrderId = parseId(transportOrderIdStr, "运单ID");
        log.info("完成运输请求, transportOrderId={}", transportOrderId);

        boolean success = transportService.completeTransport(transportOrderId);
        if (success) {
            return Result.success("运输已完成", true);
        } else {
            return Result.fail("更新状态失败");
        }
    }

    @PostMapping("/waybill/{transportOrderId}/cancel")
    @Operation(summary = "取消运单", description = "取消待取货状态的运单")
    public Result<Boolean> cancelTransport(
            @Parameter(description = "运单ID（文本）") @PathVariable("transportOrderId") String transportOrderIdStr) {
        Long transportOrderId = parseId(transportOrderIdStr, "运单ID");
        log.info("取消运单请求, transportOrderId={}", transportOrderId);

        boolean success = transportService.cancelTransport(transportOrderId);
        if (success) {
            return Result.success("运单已取消", true);
        } else {
            return Result.fail("取消失败，运单状态不允许取消");
        }
    }

    @GetMapping("/waybill/driver/{driverId}/active")
    @Operation(summary = "查询司机的进行中运单", description = "根据司机ID查询所有进行中的运单")
    public Result<List<TransportOrder>> getActiveTransportsByDriver(
            @Parameter(description = "司机ID（文本）") @PathVariable("driverId") String driverIdStr) {
        Long driverId = parseId(driverIdStr, "司机ID");
        List<TransportOrder> orders = transportService.getActiveTransportsByDriver(driverId);
        return Result.success(orders);
    }

    @GetMapping("/driver/available")
    @Operation(summary = "获取空闲司机列表", description = "查询所有状态为空闲的司机")
    public Result<List<Driver>> getAvailableDrivers() {
        List<Driver> drivers = transportService.getAvailableDrivers();
        return Result.success(drivers);
    }

    @PostMapping("/driver/{driverId}/location")
    @Operation(summary = "更新司机位置", description = "更新司机的GPS位置信息")
    public Result<Boolean> updateDriverLocation(
            @Parameter(description = "司机ID（文本）") @PathVariable("driverId") String driverIdStr,
            @Parameter(description = "纬度") @RequestParam("latitude") Double latitude,
            @Parameter(description = "经度") @RequestParam("longitude") Double longitude) {
        Long driverId = parseId(driverIdStr, "司机ID");
        boolean success = transportService.updateDriverLocation(driverId, latitude, longitude);
        if (success) {
            return Result.success("位置更新成功", true);
        } else {
            return Result.fail("位置更新失败");
        }
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
