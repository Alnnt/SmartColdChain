package com.coldchain.iot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备数据实体（MongoDB文档）
 *
 * @author ColdChain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "device_data")
@CompoundIndex(name = "idx_device_time", def = "{'deviceId': 1, 'timestamp': -1}")
public class DeviceData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * MongoDB 文档ID
     */
    @Id
    private String id;

    /**
     * 设备ID
     */
    @Indexed
    @Field("device_id")
    private String deviceId;


    /**
     * 温度（摄氏度）
     */
    @Field("temperature")
    private Double temperature;

    /**
     * 湿度（百分比）
     */
    @Field("humidity")
    private Double humidity;

    /**
     * GPS经度
     */
    @Field("longitude")
    private Double longitude;

    /**
     * GPS纬度
     */
    @Field("latitude")
    private Double latitude;

    /**
     * 报警状态（0-正常，1-温度异常，2-湿度异常）
     */
    @Field("alarm_status")
    private Integer alarmStatus;

    /**
     * 运单ID（关联的运输任务）
     */
    @Field("waybill_id")
    private Long waybillId;

    /**
     * 数据采集时间（设备上报时间）
     */
    @Indexed
    @Field("timestamp")
    private LocalDateTime timestamp;
}
