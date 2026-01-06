package com.coldchain.iot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设备上报消息（TCP协议解析后的数据）
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 温度
     */
    private Double temperature;

    /**
     * 湿度
     */
    private Double humidity;

    /**
     * GPS坐标
     */
    private GpsLocation gps;

    /**
     * 电池电量
     */
    private Integer battery;

    /**
     * 信号强度
     */
    private Integer signalStrength;

    /**
     * 运单ID
     */
    private Long waybillId;

    /**
     * 时间戳（设备端时间，毫秒）
     */
    private Long timestamp;

    /**
     * GPS坐标内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GpsLocation implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /**
         * 经度
         */
        private Double longitude;

        /**
         * 纬度
         */
        private Double latitude;
    }
}
