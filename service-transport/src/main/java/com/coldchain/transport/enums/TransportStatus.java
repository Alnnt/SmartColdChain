package com.coldchain.transport.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 运单状态枚举
 *
 * @author ColdChain
 */
@Getter
@AllArgsConstructor
public enum TransportStatus {

    /**
     * 待取货
     */
    WAITING(0, "待取货"),

    /**
     * 运输中
     */
    IN_TRANSIT(1, "运输中"),

    /**
     * 已送达
     */
    DELIVERED(2, "已送达"),

    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

    private final Integer code;
    private final String desc;

    public static TransportStatus fromCode(Integer code) {
        for (TransportStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
