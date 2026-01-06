package com.coldchain.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * IoT 服务配置
 *
 * @author ColdChain
 */
@Data
@Component
@ConfigurationProperties(prefix = "iot")
public class IoTConfig {

    /**
     * 温度报警阈值 - 最低温度
     */
    private Double temperatureMin = -25.0;

    /**
     * 温度报警阈值 - 最高温度
     */
    private Double temperatureMax = 8.0;

    /**
     * 湿度报警阈值 - 最低湿度
     */
    private Double humidityMin = 30.0;

    /**
     * 湿度报警阈值 - 最高湿度
     */
    private Double humidityMax = 90.0;

    /**
     * 低电量报警阈值
     */
    private Integer lowBatteryThreshold = 20;

    /**
     * RocketMQ Topic
     */
    private String mqTopic = "topic-iot-data";

    /**
     * RocketMQ Tag
     */
    private String mqTag = "device-data";

    /**
     * 报警 Topic
     */
    private String alarmTopic = "topic-iot-alarm";
}
