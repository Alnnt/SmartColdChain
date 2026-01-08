package com.coldchain.iot.netty.handler;

import cn.hutool.core.util.StrUtil;
import com.coldchain.iot.model.DeviceMessage;
import com.coldchain.iot.service.DeviceDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IoT 数据处理器
 * 解析设备上报的JSON数据并发送到RocketMQ
 *
 * @author ColdChain
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class IoTDataHandler extends SimpleChannelInboundHandler<String> {

    private final ObjectMapper objectMapper;
    private final DeviceDataService deviceDataService;

    /**
     * 设备连接映射表（deviceId -> ChannelHandlerContext）
     */
    private static final ConcurrentHashMap<String, ChannelHandlerContext> DEVICE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * 消息计数器
     */
    private static final AtomicLong MESSAGE_COUNTER = new AtomicLong(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("设备连接: {}:{}", address.getHostString(), address.getPort());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("设备断开连接: {}:{}", address.getHostString(), address.getPort());

        // 移除设备通道映射
        DEVICE_CHANNELS.entrySet().removeIf(entry -> entry.getValue().equals(ctx));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (StrUtil.isBlank(msg)) {
            return;
        }

        long count = MESSAGE_COUNTER.incrementAndGet();
        if (count % 1000 == 0) {
            log.info("已处理消息数: {}", count);
        }

        try {
            // 解析JSON消息
            DeviceMessage deviceMessage = objectMapper.readValue(msg, DeviceMessage.class);

            if (StrUtil.isBlank(deviceMessage.getDeviceId())) {
                log.warn("设备ID为空，丢弃消息: {}", msg);
                sendResponse(ctx, buildErrorResponse("INVALID_DEVICE_ID"));
                return;
            }

            // 注册设备通道
            DEVICE_CHANNELS.put(deviceMessage.getDeviceId(), ctx);

            log.debug("收到设备数据: deviceId={}, temperature={}, humidity={}, gps={}",
                    deviceMessage.getDeviceId(),
                    deviceMessage.getTemperature(),
                    deviceMessage.getHumidity(),
                    deviceMessage.getGps());

            // 处理设备数据（存储 + 发送MQ）
            deviceDataService.processDeviceData(deviceMessage);

            // 发送确认响应
            sendResponse(ctx, buildSuccessResponse(deviceMessage.getDeviceId()));

        } catch (Exception e) {
            log.error("解析设备数据失败: {}", msg, e);
            sendResponse(ctx, buildErrorResponse("PARSE_ERROR"));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent idleEvent) {
            if (idleEvent.state() == IdleState.READER_IDLE) {
                log.warn("设备读空闲超时，关闭连接: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("通道异常: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    /**
     * 发送响应
     */
    private void sendResponse(ChannelHandlerContext ctx, String response) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(response + "\n");
        }
    }

    /**
     * 构建成功响应
     */
    private String buildSuccessResponse(String deviceId) {
        return String.format("{\"code\":200,\"deviceId\":\"%s\",\"msg\":\"OK\",\"timestamp\":%d}",
                deviceId, System.currentTimeMillis());
    }

    /**
     * 构建错误响应
     */
    private String buildErrorResponse(String error) {
        return String.format("{\"code\":400,\"msg\":\"%s\",\"timestamp\":%d}",
                error, System.currentTimeMillis());
    }

    /**
     * 向指定设备发送消息
     */
    public boolean sendToDevice(String deviceId, String message) {
        ChannelHandlerContext ctx = DEVICE_CHANNELS.get(deviceId);
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(message + "\n");
            return true;
        }
        return false;
    }

    /**
     * 获取在线设备数量
     */
    public int getOnlineDeviceCount() {
        return DEVICE_CHANNELS.size();
    }

    /**
     * 获取消息处理总数
     */
    public long getMessageCount() {
        return MESSAGE_COUNTER.get();
    }
}
