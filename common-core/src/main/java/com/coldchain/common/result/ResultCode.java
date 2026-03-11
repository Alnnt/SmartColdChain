package com.coldchain.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author Alnnt
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ========== 成功 ==========
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 4xx ==========
    FAIL(400, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "资源冲突"),
    PARAM_VALID_ERROR(422, "参数校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ========== 服务端错误 5xx ==========
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ========== 业务错误 1xxx ==========
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    TOKEN_INVALID(1005, "Token无效"),

    // ========== 订单相关 2xxx ==========
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态异常"),
    ORDER_ALREADY_PAID(2003, "订单已支付"),
    ORDER_CANCELLED(2004, "订单已取消"),

    // ========== 库存相关 3xxx ==========
    INVENTORY_NOT_ENOUGH(3001, "库存不足"),
    WAREHOUSE_NOT_FOUND(3002, "仓库不存在"),

    // ========== 运输相关 4xxx ==========
    TRANSPORT_NOT_FOUND(4001, "运单不存在"),
    DRIVER_NOT_AVAILABLE(4002, "司机不可用"),
    VEHICLE_NOT_AVAILABLE(4003, "车辆不可用"),

    // ========== IoT相关 5xxx ==========
    DEVICE_OFFLINE(5001, "设备离线"),
    DEVICE_NOT_FOUND(5002, "设备不存在"),
    TEMPERATURE_ABNORMAL(5003, "温度异常报警"),
    HUMIDITY_ABNORMAL(5004, "湿度异常报警");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;
}
