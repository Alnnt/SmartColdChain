package com.coldchain.iot.model;

import com.coldchain.common.entity.GpsLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设备上报消息（TCP协议解析后的数据）
 *
 * @author Alnnt
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
     * 运单ID
     */
    private Long waybillId;

    /**
     * 时间戳（设备端时间，毫秒）
     */
    private Long timestamp;
}
