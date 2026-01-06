package com.coldchain.iot.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型枚举
 *
 * @author ColdChain
 */
@Getter
@AllArgsConstructor
public enum DeviceType {

    TEMPERATURE_SENSOR(1, "温度传感器"),
    HUMIDITY_SENSOR(2, "湿度传感器"),
    GPS_TRACKER(3, "GPS定位器"),
    INTEGRATED_DEVICE(4, "综合监测设备");

    private final Integer code;
    private final String desc;

    public static DeviceType of(Integer code) {
        for (DeviceType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return INTEGRATED_DEVICE;
    }
}
