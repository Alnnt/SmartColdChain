package com.coldchain.gateway.filter;

import com.coldchain.auth.common.AuthConstants;
import com.coldchain.auth.common.JwtTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局认证过滤器
 *
 * @author Alnnt
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单路径（无需认证）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 认证服务
            "/api/auth/login",
            "/api/auth/user/login",
            "/api/auth/register",
            "/api/auth/refresh",
            // API鏂囨。
            "/*/doc.html",
            "/*/v3/api-docs/**",
            "/*/swagger-ui/**",
            "/*/webjars/**",
            // 健康检查
            "/actuator/**",
            // IoT设备数据上报
            "/api/iot/device/report"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        log.debug("Gateway收到请求: {} {}", method, path);

        // 检查是否在白名单中
        if (isWhiteListed(path)) {
            log.debug("请求在白名单中，放行: {}", path);
            return chain.filter(exchange);
        }

        // 获取Token
        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("请求未携带Token: {}", path);
            return unauthorized(exchange, "未授权，请先登录");
        }

        // 验证Token
        if (!jwtTokenUtil.validateToken(token)) {
            log.warn("Token验证失败: {}", path);
            return unauthorized(exchange, "Token无效或已过期");
        }

        // 获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUsernameFromToken(token);

        if (userId == null) {
            log.warn("无法从Token中获取用户信息: {}", path);
            return unauthorized(exchange, "Token解析失败");
        }

        List<String> roles = jwtTokenUtil.getRolesFromToken(token);
        Long warehouseId = jwtTokenUtil.getWarehouseIdFromToken(token);
        String rolesHeader = (roles != null && !roles.isEmpty()) ? String.join(",", roles) : "";
        String warehouseIdHeader = (warehouseId != null) ? String.valueOf(warehouseId) : "";

        // 将用户信息添加到请求头，传递给下游服务
        ServerHttpRequest.Builder headerBuilder = request.mutate()
                .header(AuthConstants.USER_ID_HEADER, String.valueOf(userId))
                .header(AuthConstants.USERNAME_HEADER, username != null ? username : "");
        if (!rolesHeader.isEmpty()) {
            headerBuilder.header(AuthConstants.ROLES_HEADER, rolesHeader);
        }
        if (!warehouseIdHeader.isEmpty()) {
            headerBuilder.header(AuthConstants.WAREHOUSE_ID_HEADER, warehouseIdHeader);
        }
        ServerHttpRequest mutatedRequest = headerBuilder.build();

        log.debug("认证通过，用户ID: {}, 用户名: {}", userId, username);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求中获取Token
     */
    private String getToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(AuthConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AuthConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(AuthConstants.TOKEN_PREFIX.length());
        }
        // 也支持从查询参数获取
        String tokenParam = request.getQueryParams().getFirst("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);

        try {
            String json = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 优先级较高，在其他过滤器之前执行
        return -100;
    }
}
