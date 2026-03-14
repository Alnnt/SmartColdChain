package com.coldchain.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局异常处理
 *
 * @author Alnnt
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        String path = exchange.getRequest().getURI().getPath();
        log.error("网关异常: {} - {}", path, ex.getMessage(), ex);

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        int code;
        String message;

        if (ex instanceof NotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            code = 404;
            message = "服务不可用或路由未找到";
        } else if (ex instanceof ResponseStatusException rse) {
            response.setStatusCode(rse.getStatusCode());
            code = rse.getStatusCode().value();
            message = rse.getReason() != null ? rse.getReason() : "请求处理失败";
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            code = 503;
            message = "服务暂不可用，请稍后重试";
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            code = 500;
            message = "网关内部错误";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        result.put("path", path);
        result.put("timestamp", System.currentTimeMillis());

        try {
            String json = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return response.setComplete();
        }
    }
}
