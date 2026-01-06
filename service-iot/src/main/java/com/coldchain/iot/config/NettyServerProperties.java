package com.coldchain.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Netty 服务器配置属性
 *
 * @author ColdChain
 */
@Data
@Component
@ConfigurationProperties(prefix = "netty.server")
public class NettyServerProperties {

    /**
     * 服务器端口
     */
    private int port = 9000;

    /**
     * Boss线程数
     */
    private int bossThreads = 1;

    /**
     * Worker线程数
     */
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 30000;

    /**
     * 读空闲时间（秒）
     */
    private int readerIdleTime = 60;

    /**
     * 写空闲时间（秒）
     */
    private int writerIdleTime = 60;

    /**
     * 读写空闲时间（秒）
     */
    private int allIdleTime = 120;

    /**
     * 最大帧长度
     */
    private int maxFrameLength = 1024 * 1024;
}
