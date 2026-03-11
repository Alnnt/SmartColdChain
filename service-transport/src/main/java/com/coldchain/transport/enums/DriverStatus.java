package com.coldchain.transport.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 司机状态枚举
 *
 * @author Alnnt
 */
@Getter
@AllArgsConstructor
public enum DriverStatus {

    /**
     * 空闲
     */
    FREE(0, "空闲"),

    /**
     * 忙碌
     */
    BUSY(1, "忙碌");

    private final Integer code;
    private final String desc;

    public static DriverStatus fromCode(Integer code) {
        for (DriverStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
