package com.coldchain.iot.netty;

import com.coldchain.iot.config.NettyServerProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Netty TCP 服务器
 * 用于接收IoT设备上报的数据
 *
 * @author ColdChain
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServer {

    private final NettyServerProperties properties;
    private final NettyServerInitializer serverInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    /**
     * 启动 Netty 服务器
     */
    @PostConstruct
    public void start() {
        CompletableFuture.runAsync(this::doStart);
    }

    /**
     * 执行启动逻辑
     */
    private void doStart() {
        bossGroup = new NioEventLoopGroup(properties.getBossThreads());
        workerGroup = new NioEventLoopGroup(properties.getWorkerThreads());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP连接队列大小
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 开启TCP心跳
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 禁用Nagle算法，保证数据实时性
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 连接超时
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeout())
                    // 初始化器
                    .childHandler(serverInitializer);

            // 绑定端口，同步等待
            channelFuture = bootstrap.bind(properties.getPort()).sync();

            log.info("========================================");
            log.info("  Netty TCP Server 启动成功!");
            log.info("  监听端口: {}", properties.getPort());
            log.info("  Boss线程: {}", properties.getBossThreads());
            log.info("  Worker线程: {}", properties.getWorkerThreads());
            log.info("========================================");

            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Netty Server 启动被中断", e);
        } catch (Exception e) {
            log.error("Netty Server 启动失败", e);
        } finally {
            shutdown();
        }
    }

    /**
     * 关闭服务器
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭 Netty Server...");

        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        log.info("Netty Server 已关闭");
    }
}
