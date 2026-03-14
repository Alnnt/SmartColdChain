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
 * Netty 閫氶亾鍒濆鍖栧櫒
 *
 * @author Alnnt
 */
@Component
@RequiredArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServerProperties properties;
    private final IoTDataHandler ioTDataHandler;

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        // 绌洪棽妫€娴嬪鐞嗗櫒
        pipeline.addLast("idleStateHandler", new IdleStateHandler(
                properties.getReaderIdleTime(),
                properties.getWriterIdleTime(),
                properties.getAllIdleTime(),
                TimeUnit.SECONDS));

        // JSON瀵硅薄瑙ｇ爜鍣紙鑷姩鍒嗗壊JSON瀵硅薄锛?
        pipeline.addLast("jsonDecoder", new JsonObjectDecoder(properties.getMaxFrameLength()));

        // 瀛楃涓茬紪瑙ｇ爜鍣?
        pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
        pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));

        // IoT鏁版嵁澶勭悊鍣?
        pipeline.addLast("ioTDataHandler", ioTDataHandler);
    }
}
