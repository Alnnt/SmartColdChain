package com.coldchain.transport.controller;

import com.coldchain.common.result.Result;
import com.coldchain.transport.dto.CreateWaybillRequest;
import com.coldchain.transport.dto.WaybillCreateApiDTO;
import com.coldchain.transport.entity.TransportOrder;
import com.coldchain.transport.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运单 Feign 对接 API（路径 /api/waybill，供订单服务 Feign 调用）
 */
@Slf4j
@RestController
@RequestMapping("/api/waybill")
@RequiredArgsConstructor
@Tag(name = "运单Feign接口", description = "订单服务调用的创建运单接口")
public class ApiWaybillController {

    private final TransportService transportService;

    @PostMapping("/create")
    @Operation(summary = "创建运单")
    public Result<TransportOrder> create(@RequestBody WaybillCreateApiDTO dto) {
        if (dto == null || dto.getOrderId() == null) {
            return Result.fail("订单ID不能为空");
        }
        String toAddress = dto.getOrderNo() != null ? "订单" + dto.getOrderNo() : "收货地址";
        CreateWaybillRequest request = CreateWaybillRequest.builder()
                .orderId(dto.getOrderId())
                .fromAddress("仓库")
                .toAddress(toAddress)
                .build();
        try {
            TransportOrder order = transportService.createWaybill(
                    request.getOrderId(),
                    request.getFromAddress(),
                    request.getToAddress());
            return Result.success("运单创建成功", order);
        } catch (RuntimeException e) {
            log.error("创建运单失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
}
