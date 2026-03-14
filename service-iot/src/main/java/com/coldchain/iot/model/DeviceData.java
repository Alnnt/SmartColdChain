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
 * 璁惧鏁版嵁瀹炰綋锛圡ongoDB鏂囨。锛?
 *
 * @author Alnnt
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
     * MongoDB 鏂囨。ID
     */
    @Id
    private String id;

    /**
     * 璁惧ID
     */
    @Indexed
    @Field("device_id")
    private String deviceId;

    /**
     * 娓╁害锛堟憚姘忓害锛?
     */
    @Field("temperature")
    private Double temperature;

    /**
     * 婀垮害锛堢櫨鍒嗘瘮锛?
     */
    @Field("humidity")
    private Double humidity;

    /**
     * GPS缁忓害
     */
    @Field("longitude")
    private Double longitude;

    /**
     * GPS绾害
     */
    @Field("latitude")
    private Double latitude;

    /**
     * 鎶ヨ鐘舵€侊紙0-姝ｅ父锛?-娓╁害寮傚父锛?-婀垮害寮傚父锛?
     */
    @Field("alarm_status")
    private Integer alarmStatus;

    /**
     * 杩愬崟ID锛堝叧鑱旂殑杩愯緭浠诲姟锛?
     */
    @Field("waybill_id")
    private Long waybillId;

    /**
     * 鏁版嵁閲囬泦鏃堕棿锛堣澶囦笂鎶ユ椂闂达級
     */
    @Indexed
    @Field("timestamp")
    private LocalDateTime timestamp;
}
