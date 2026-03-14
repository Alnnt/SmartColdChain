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
 * IoT 鏁版嵁澶勭悊鍣?
 * 瑙ｆ瀽璁惧涓婃姤鐨凧SON鏁版嵁骞跺彂閫佸埌RocketMQ
 *
 * @author Alnnt
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class IoTDataHandler extends SimpleChannelInboundHandler<String> {

    private final ObjectMapper objectMapper;
    private final DeviceDataService deviceDataService;

    /**
     * 璁惧杩炴帴鏄犲皠琛紙deviceId -> ChannelHandlerContext锛?
     */
    private static final ConcurrentHashMap<String, ChannelHandlerContext> DEVICE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * 娑堟伅璁℃暟鍣?
     */
    private static final AtomicLong MESSAGE_COUNTER = new AtomicLong(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("璁惧杩炴帴: {}:{}", address.getHostString(), address.getPort());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("璁惧鏂紑杩炴帴: {}:{}", address.getHostString(), address.getPort());

        // 绉婚櫎璁惧閫氶亾鏄犲皠
        DEVICE_CHANNELS.entrySet().removeIf(entry -> entry.getValue().equals(ctx));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (StrUtil.isBlank(msg)) {
            return;
        }

        long count = MESSAGE_COUNTER.incrementAndGet();
        if (count % 1000 == 0) {
            log.info("宸插鐞嗘秷鎭暟: {}", count);
        }

        try {
            // 瑙ｆ瀽JSON娑堟伅
            DeviceMessage deviceMessage = objectMapper.readValue(msg, DeviceMessage.class);

            if (StrUtil.isBlank(deviceMessage.getDeviceId())) {
                log.warn("璁惧ID涓虹┖锛屼涪寮冩秷鎭? {}", msg);
                sendResponse(ctx, buildErrorResponse("INVALID_DEVICE_ID"));
                return;
            }

            // 娉ㄥ唽璁惧閫氶亾
            DEVICE_CHANNELS.put(deviceMessage.getDeviceId(), ctx);

            log.debug("鏀跺埌璁惧鏁版嵁: deviceId={}, temperature={}, humidity={}, gps={}",
                    deviceMessage.getDeviceId(),
                    deviceMessage.getTemperature(),
                    deviceMessage.getHumidity(),
                    deviceMessage.getGps());

            // 澶勭悊璁惧鏁版嵁锛堝瓨鍌?+ 鍙戦€丮Q锛?
            deviceDataService.processDeviceData(deviceMessage);

            // 鍙戦€佺‘璁ゅ搷搴?
            sendResponse(ctx, buildSuccessResponse(deviceMessage.getDeviceId()));

        } catch (Exception e) {
            log.error("瑙ｆ瀽璁惧鏁版嵁澶辫触: {}", msg, e);
            sendResponse(ctx, buildErrorResponse("PARSE_ERROR"));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent idleEvent) {
            if (idleEvent.state() == IdleState.READER_IDLE) {
                log.warn("璁惧璇荤┖闂茶秴鏃讹紝鍏抽棴杩炴帴: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("閫氶亾寮傚父: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    /**
     * 鍙戦€佸搷搴?
     */
    private void sendResponse(ChannelHandlerContext ctx, String response) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(response + "\n");
        }
    }

    /**
     * 鏋勫缓鎴愬姛鍝嶅簲
     */
    private String buildSuccessResponse(String deviceId) {
        return String.format("{\"code\":200,\"deviceId\":\"%s\",\"msg\":\"OK\",\"timestamp\":%d}",
                deviceId, System.currentTimeMillis());
    }

    /**
     * 鏋勫缓閿欒鍝嶅簲
     */
    private String buildErrorResponse(String error) {
        return String.format("{\"code\":400,\"msg\":\"%s\",\"timestamp\":%d}",
                error, System.currentTimeMillis());
    }

    /**
     * 鍚戞寚瀹氳澶囧彂閫佹秷鎭?
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
     * 鑾峰彇鍦ㄧ嚎璁惧鏁伴噺
     */
    public int getOnlineDeviceCount() {
        return DEVICE_CHANNELS.size();
    }

    /**
     * 鑾峰彇娑堟伅澶勭悊鎬绘暟
     */
    public long getMessageCount() {
        return MESSAGE_COUNTER.get();
    }
}
