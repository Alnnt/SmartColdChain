package com.coldchain.iot.netty;

import com.coldchain.iot.config.NettyServerProperties;
import com.coldchain.iot.netty.handler.IoTDataHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Netty 通道初始化器
 *
 * @author ColdChain
 */
@Component
@RequiredArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServerProperties properties;
    private final IoTDataHandler ioTDataHandler;

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        // 空闲检测处理器
        pipeline.addLast("idleStateHandler", new IdleStateHandler(
                properties.getReaderIdleTime(),
                properties.getWriterIdleTime(),
                properties.getAllIdleTime(),
                TimeUnit.SECONDS
        ));

        // JSON对象解码器（自动分割JSON对象）
        pipeline.addLast("jsonDecoder", new JsonObjectDecoder(properties.getMaxFrameLength()));

        // 字符串编解码器
        pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
        pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));

        // IoT数据处理器
        pipeline.addLast("ioTDataHandler", ioTDataHandler);
    }
}
