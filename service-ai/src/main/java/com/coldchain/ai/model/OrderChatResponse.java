package com.coldchain.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单解析响应
 * 从自然语言中提取的结构化订单信息
 *
 * @author Alnnt
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "订单智能解析响应")
public class OrderChatResponse {

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "冷冻海鲜")
    private String item;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "100")
    private Integer quantity;

    /**
     * 单位
     */
    @Schema(description = "单位", example = "箱")
    private String unit;

    /**
     * 取货地址
     */
    @Schema(description = "取货地址", example = "北京朝阳区")
    private String pickupAddress;

    /**
     * 配送地址
     */
    @Schema(description = "配送地址", example = "上海浦东新区")
    private String deliveryAddress;

    /**
     * 特殊要求
     */
    @Schema(description = "特殊要求", example = "全程-18度以下")
    private String specialRequirements;

    /**
     * 预估重量
     */
    @Schema(description = "预估重量")
    private String estimatedWeight;

    /**
     * 联系人姓名
     */
    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    /**
     * 原始LLM响应（解析失败时使用）
     */
    @Schema(description = "原始响应", hidden = true)
    private String rawResponse;
}
