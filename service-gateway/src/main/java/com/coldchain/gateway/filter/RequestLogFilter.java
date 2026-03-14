package com.coldchain.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 璇锋眰鏃ュ織杩囨护鍣?
 *
 * @author Alnnt
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 鐢熸垚璇锋眰ID
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 璁板綍寮€濮嬫椂闂?
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        // 娣诲姞璇锋眰ID鍒拌姹傚ご
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String clientIp = getClientIp(request);

        log.info("[{}] 璇锋眰寮€濮? {} {} | IP: {} | Query: {}",
                requestId, method, path, clientIp, query);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    Long start = exchange.getAttribute(START_TIME_ATTR);
                    if (start != null) {
                        long duration = System.currentTimeMillis() - start;
                        int statusCode = exchange.getResponse().getStatusCode() != null
                                ? exchange.getResponse().getStatusCode().value()
                                : 0;
                        log.info("[{}] 璇锋眰缁撴潫: {} {} | 鐘舵€? {} | 鑰楁椂: {}ms",
                                requestId, method, path, statusCode, duration);
                    }
                }));
    }

    /**
     * 鑾峰彇瀹㈡埛绔湡瀹濱P
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        // 澶氭鍙嶅悜浠ｇ悊鍚庝細鏈夊涓狪P鍊硷紝绗竴涓狪P鎵嶆槸鐪熷疄IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        // 鏈€楂樹紭鍏堢骇锛岀涓€涓墽琛?
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
