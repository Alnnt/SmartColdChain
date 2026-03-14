package com.coldchain.iot.controller;

import com.coldchain.common.result.Result;
import com.coldchain.iot.model.DeviceData;
import com.coldchain.iot.netty.handler.IoTDataHandler;
import com.coldchain.iot.service.DeviceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备数据控制器
 *
 * @author Alnnt
 */
@Tag(name = "设备数据", description = "IoT设备数据查询接口")
@RestController
@RequestMapping("/api/iot/device")
@RequiredArgsConstructor
public class DeviceDataController {

    private final DeviceDataService deviceDataService;
    private final IoTDataHandler ioTDataHandler;

    @Operation(summary = "获取设备最新数据")
    @GetMapping("/{deviceId}/latest")
    public Result<DeviceData> getLatestData(
            @Parameter(description = "设备ID") @PathVariable String deviceId) {
        DeviceData data = deviceDataService.getLatestData(deviceId);
        return Result.success(data);
    }

    @Operation(summary = "分页查询设备历史数据")
    @GetMapping("/{deviceId}/history")
    public Result<Page<DeviceData>> getHistoryData(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Page<DeviceData> data = deviceDataService.getHistoryData(deviceId, page, size);
        return Result.success(data);
    }

    @Operation(summary = "查询时间范围内的数据")
    @GetMapping("/{deviceId}/range")
    public Result<List<DeviceData>> getDataByTimeRange(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Parameter(description = "开始时间") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<DeviceData> data = deviceDataService.getDataByTimeRange(deviceId, startTime, endTime);
        return Result.success(data);
    }

    @Operation(summary = "获取运单轨迹数据")
    @GetMapping("/waybill/{waybillId}/track")
    public Result<List<DeviceData>> getWaybillTrack(
            @Parameter(description = "运单ID") @PathVariable Long waybillId) {
        List<DeviceData> data = deviceDataService.getWaybillTrack(waybillId);
        return Result.success(data);
    }

    @Operation(summary = "获取报警数据")
    @GetMapping("/alarm")
    public Result<List<DeviceData>> getAlarmData(
            @Parameter(description = "最近N小时") @RequestParam(defaultValue = "24") int hours) {
        List<DeviceData> data = deviceDataService.getAlarmData(hours);
        return Result.success(data);
    }

    @Operation(summary = "获取IoT服务状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getServerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("onlineDevices", ioTDataHandler.getOnlineDeviceCount());
        status.put("totalMessages", ioTDataHandler.getMessageCount());
        status.put("serverTime", LocalDateTime.now());
        return Result.success(status);
    }
}
