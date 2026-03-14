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
 * 璁惧鏁版嵁鏈嶅姟瀹炵幇
 *
 * @author Alnnt
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
     * 璁惧鐘舵€佺紦瀛樺墠缂€
     */
    private static final String DEVICE_STATUS_PREFIX = "iot:device:status:";

    /**
     * 璁惧鐘舵€佺紦瀛樿繃鏈熸椂闂达紙鍒嗛挓锛?
     */
    private static final long DEVICE_STATUS_EXPIRE = 5;

    @Override
    public void processDeviceData(DeviceMessage message) {
        // 1. 杞崲涓哄瓨鍌ㄥ疄浣?
        DeviceData deviceData = convertToDeviceData(message);

        // 3. 瀛樺偍鍒?MongoDB
        deviceDataRepository.save(deviceData);

        // 4. 鏇存柊璁惧鐘舵€佺紦瀛?
        updateDeviceStatusCache(message.getDeviceId());

        // 5. 鍙戦€佸埌 RocketMQ
        messageProducer.sendDeviceData(deviceData);
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
     * 杞崲涓哄瓨鍌ㄥ疄浣?
     */
    private DeviceData convertToDeviceData(DeviceMessage message) {
        DeviceData.DeviceDataBuilder builder = DeviceData.builder()
                .deviceId(message.getDeviceId())
                .temperature(message.getTemperature())
                .humidity(message.getHumidity())
                .waybillId(message.getWaybillId())
                .timestamp(LocalDateTime.now());

        // GPS鍧愭爣
        if (message.getGps() != null) {
            builder.longitude(message.getGps().getLongitude());
            builder.latitude(message.getGps().getLatitude());
        }

        // 璁惧绔椂闂存埑杞崲
        if (message.getTimestamp() != null) {
            builder.timestamp(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(message.getTimestamp()),
                    ZoneId.systemDefault()));
        } else {
            builder.timestamp(LocalDateTime.now());
        }

        return builder.build();
    }

    /**
     * 妫€娴嬫姤璀︾姸鎬?
     */
    private Integer checkAlarmStatus(DeviceData data) {
        // 娓╁害寮傚父妫€娴?
        if (data.getTemperature() != null) {
            if (data.getTemperature() < ioTConfig.getTemperatureMin() ||
                    data.getTemperature() > ioTConfig.getTemperatureMax()) {
                return AlarmStatus.TEMPERATURE_ABNORMAL.getCode();
            }
        }

        // 婀垮害寮傚父妫€娴?
        if (data.getHumidity() != null) {
            if (data.getHumidity() < ioTConfig.getHumidityMin() ||
                    data.getHumidity() > ioTConfig.getHumidityMax()) {
                return AlarmStatus.HUMIDITY_ABNORMAL.getCode();
            }
        }

        return AlarmStatus.NORMAL.getCode();
    }

    /**
     * 鏇存柊璁惧鐘舵€佺紦瀛?
     */
    private void updateDeviceStatusCache(String deviceId) {
        String key = DEVICE_STATUS_PREFIX + deviceId;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),
                DEVICE_STATUS_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 妫€鏌ヨ澶囨槸鍚﹀湪绾?
     */
    public boolean isDeviceOnline(String deviceId) {
        String key = DEVICE_STATUS_PREFIX + deviceId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
