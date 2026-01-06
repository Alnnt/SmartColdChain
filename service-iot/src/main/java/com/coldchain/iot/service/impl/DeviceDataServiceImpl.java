package com.coldchain.iot.service.impl;

import com.coldchain.iot.config.IoTConfig;
import com.coldchain.iot.model.DeviceData;
import com.coldchain.iot.model.DeviceMessage;
import com.coldchain.iot.model.enums.AlarmStatus;
import com.coldchain.iot.producer.IoTMessageProducer;
import com.coldchain.iot.repository.DeviceDataRepository;
import com.coldchain.iot.service.DeviceDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 设备数据服务实现
 *
 * @author ColdChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDataServiceImpl implements DeviceDataService {

    private final DeviceDataRepository deviceDataRepository;
    private final IoTMessageProducer messageProducer;
    private final StringRedisTemplate redisTemplate;
    private final IoTConfig ioTConfig;

    /**
     * 设备状态缓存前缀
     */
    private static final String DEVICE_STATUS_PREFIX = "iot:device:status:";

    /**
     * 设备状态缓存过期时间（分钟）
     */
    private static final long DEVICE_STATUS_EXPIRE = 5;

    @Override
    public void processDeviceData(DeviceMessage message, String rawData) {
        // 1. 转换为存储实体
        DeviceData deviceData = convertToDeviceData(message, rawData);

        // 2. 检测报警状态
        Integer alarmStatus = checkAlarmStatus(deviceData);
        deviceData.setAlarmStatus(alarmStatus);

        // 3. 存储到 MongoDB
        deviceDataRepository.save(deviceData);

        // 4. 更新设备状态缓存
        updateDeviceStatusCache(message.getDeviceId());

        // 5. 发送到 RocketMQ
        messageProducer.sendDeviceData(deviceData);

        // 6. 如果有报警，发送报警消息
        if (!AlarmStatus.NORMAL.getCode().equals(alarmStatus)) {
            messageProducer.sendAlarmMessage(deviceData);
            log.warn("检测到设备报警: deviceId={}, alarmStatus={}, temperature={}, humidity={}",
                    deviceData.getDeviceId(),
                    AlarmStatus.of(alarmStatus).getDesc(),
                    deviceData.getTemperature(),
                    deviceData.getHumidity());
        }
    }

    @Override
    public DeviceData getLatestData(String deviceId) {
        return deviceDataRepository.findTopByDeviceIdOrderByTimestampDesc(deviceId);
    }

    @Override
    public Page<DeviceData> getHistoryData(String deviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return deviceDataRepository.findByDeviceIdOrderByTimestampDesc(deviceId, pageRequest);
    }

    @Override
    public List<DeviceData> getDataByTimeRange(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return deviceDataRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                deviceId, startTime, endTime);
    }

    @Override
    public List<DeviceData> getWaybillTrack(Long waybillId) {
        return deviceDataRepository.findByWaybillIdOrderByTimestampAsc(waybillId);
    }

    @Override
    public List<DeviceData> getAlarmData(int hours) {
        LocalDateTime afterTime = LocalDateTime.now().minusHours(hours);
        return deviceDataRepository.findByAlarmStatusNotAndTimestampAfter(
                AlarmStatus.NORMAL.getCode(), afterTime);
    }

    /**
     * 转换为存储实体
     */
    private DeviceData convertToDeviceData(DeviceMessage message, String rawData) {
        DeviceData.DeviceDataBuilder builder = DeviceData.builder()
                .deviceId(message.getDeviceId())
                .deviceType(message.getDeviceType())
                .temperature(message.getTemperature())
                .humidity(message.getHumidity())
                .battery(message.getBattery())
                .signalStrength(message.getSignalStrength())
                .waybillId(message.getWaybillId())
                .rawData(rawData)
                .receiveTime(LocalDateTime.now());

        // GPS坐标
        if (message.getGps() != null) {
            builder.longitude(message.getGps().getLongitude());
            builder.latitude(message.getGps().getLatitude());
        }

        // 设备端时间戳转换
        if (message.getTimestamp() != null) {
            builder.timestamp(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(message.getTimestamp()),
                    ZoneId.systemDefault()
            ));
        } else {
            builder.timestamp(LocalDateTime.now());
        }

        return builder.build();
    }

    /**
     * 检测报警状态
     */
    private Integer checkAlarmStatus(DeviceData data) {
        // 温度异常检测
        if (data.getTemperature() != null) {
            if (data.getTemperature() < ioTConfig.getTemperatureMin() ||
                    data.getTemperature() > ioTConfig.getTemperatureMax()) {
                return AlarmStatus.TEMPERATURE_ABNORMAL.getCode();
            }
        }

        // 湿度异常检测
        if (data.getHumidity() != null) {
            if (data.getHumidity() < ioTConfig.getHumidityMin() ||
                    data.getHumidity() > ioTConfig.getHumidityMax()) {
                return AlarmStatus.HUMIDITY_ABNORMAL.getCode();
            }
        }

        // 低电量检测
        if (data.getBattery() != null && data.getBattery() < ioTConfig.getLowBatteryThreshold()) {
            return AlarmStatus.LOW_BATTERY.getCode();
        }

        return AlarmStatus.NORMAL.getCode();
    }

    /**
     * 更新设备状态缓存
     */
    private void updateDeviceStatusCache(String deviceId) {
        String key = DEVICE_STATUS_PREFIX + deviceId;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),
                DEVICE_STATUS_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 检查设备是否在线
     */
    public boolean isDeviceOnline(String deviceId) {
        String key = DEVICE_STATUS_PREFIX + deviceId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
