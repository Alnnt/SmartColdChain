package com.coldchain.iot.producer;

import com.coldchain.iot.config.IoTConfig;
import com.coldchain.iot.model.DeviceData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * IoT 消息生产者
 * 将设备数据发送到 RocketMQ
 *
 * @author Alnnt
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IoTMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;
    private final IoTConfig ioTConfig;

    /**
     * 发送设备数据到 MQ
     *
     * @param deviceData 设备数据
     */
    public void sendDeviceData(DeviceData deviceData) {
        try {
            String json = objectMapper.writeValueAsString(deviceData);
            String destination = ioTConfig.getMqTopic() + ":" + ioTConfig.getMqTag();

            Message<String> message = MessageBuilder
                    .withPayload(json)
                    .setHeader("deviceId", deviceData.getDeviceId())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                    log.debug("设备数据发送成功: deviceId={}, msgId={}",
                            deviceData.getDeviceId(), sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("设备数据发送失败: deviceId={}", deviceData.getDeviceId(), e);
                }
            });

        } catch (JsonProcessingException e) {
            log.error("设备数据序列化失败: deviceId={}", deviceData.getDeviceId(), e);
        }
    }

    /**
     * 发送报警消息到 MQ
     *
     * @param deviceData 设备数据（包含报警信息）
     */
    public void sendAlarmMessage(DeviceData deviceData) {
        try {
            String json = objectMapper.writeValueAsString(deviceData);
            String destination = ioTConfig.getAlarmTopic() + ":alarm";

            Message<String> message = MessageBuilder
                    .withPayload(json)
                    .setHeader("deviceId", deviceData.getDeviceId())
                    .setHeader("alarmStatus", deviceData.getAlarmStatus())
                    .build();

            // 报警消息同步发送，保证可靠性
            rocketMQTemplate.syncSend(destination, message);
            log.warn("报警消息发送成功: deviceId={}, alarmStatus={}",
                    deviceData.getDeviceId(), deviceData.getAlarmStatus());

        } catch (Exception e) {
            log.error("报警消息发送失败: deviceId={}", deviceData.getDeviceId(), e);
        }
    }
}
