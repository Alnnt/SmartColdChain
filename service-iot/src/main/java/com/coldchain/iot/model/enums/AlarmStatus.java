package com.coldchain.iot.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报警状态枚举
 *
 * @author ColdChain
 */
@Getter
@AllArgsConstructor
public enum AlarmStatus {

    NORMAL(0, "正常"),
    TEMPERATURE_ABNORMAL(1, "温度异常"),
    HUMIDITY_ABNORMAL(2, "湿度异常"),
    DEVICE_OFFLINE(3, "设备离线"),
    LOW_BATTERY(4, "电量不足");

    private final Integer code;
    private final String desc;

    public static AlarmStatus of(Integer code) {
        for (AlarmStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return NORMAL;
    }
}
