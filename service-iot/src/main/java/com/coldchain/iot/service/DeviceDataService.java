package com.coldchain.iot.service;

import com.coldchain.iot.model.DeviceData;
import com.coldchain.iot.model.DeviceMessage;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备数据服务接口
 *
 * @author ColdChain
 */
public interface DeviceDataService {

    /**
     * 处理设备数据（核心方法）
     * 1. 解析并转换数据
     * 2. 检测报警
     * 3. 存储到MongoDB
     * 4. 发送到RocketMQ
     *
     * @param message 设备消息
     */
    void processDeviceData(DeviceMessage message);

    /**
     * 获取设备最新数据
     *
     * @param deviceId 设备ID
     * @return 最新数据
     */
    DeviceData getLatestData(String deviceId);

    /**
     * 分页查询设备历史数据
     *
     * @param deviceId 设备ID
     * @param page     页码
     * @param size     每页大小
     * @return 分页数据
     */
    Page<DeviceData> getHistoryData(String deviceId, int page, int size);

    /**
     * 查询时间范围内的数据
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 数据列表
     */
    List<DeviceData> getDataByTimeRange(String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取运单轨迹数据
     *
     * @param waybillId 运单ID
     * @return 轨迹数据
     */
    List<DeviceData> getWaybillTrack(Long waybillId);

    /**
     * 获取报警数据
     *
     * @param hours 最近N小时
     * @return 报警数据
     */
    List<DeviceData> getAlarmData(int hours);
}
